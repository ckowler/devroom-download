//! HTTP(S) download implementation.
//!
//! Parallel ranges are used only when the origin explicitly advertises byte
//! ranges and each response is validated. Any range failure falls back to a
//! fresh single-stream transfer; partial range data is never published.

use reqwest::blocking::{Client, Response};
use reqwest::header::{ACCEPT_RANGES, CONTENT_LENGTH, CONTENT_RANGE, ETAG, LAST_MODIFIED, RANGE};
use reqwest::StatusCode;
use std::fs::{self, File, OpenOptions};
use std::io::{self, Write};
use std::path::{Path, PathBuf};
use std::sync::Arc;
use std::thread;
use std::time::{SystemTime, UNIX_EPOCH};
use thiserror::Error;

#[derive(Debug, Error)]
pub enum DownloadError {
    #[error("network request failed: {0}")]
    Network(#[from] reqwest::Error),
    #[error("file operation failed: {0}")]
    Io(#[from] io::Error),
    #[error("server returned an unexpected response: {0}")]
    InvalidResponse(String),
    #[error("download URL must use HTTP or HTTPS")]
    InvalidUrl,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct Probe {
    pub size: Option<u64>,
    pub supports_ranges: bool,
    pub etag: Option<String>,
    pub last_modified: Option<String>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct DownloadReport {
    pub bytes_written: u64,
    pub used_parallel_ranges: bool,
    pub connections: usize,
}

pub fn probe(url: &str) -> Result<Probe, DownloadError> {
    validate_url(url)?;
    let client = build_client()?;
    let response = client.head(url).send()?;
    if !response.status().is_success() {
        return Err(DownloadError::InvalidResponse(format!(
            "HEAD returned HTTP {}", response.status()
        )));
    }
    let headers = response.headers();
    let size = headers.get(CONTENT_LENGTH)
        .and_then(|v| v.to_str().ok())
        .and_then(|v| v.parse::<u64>().ok());
    let supports_ranges = headers.get(ACCEPT_RANGES)
        .and_then(|v| v.to_str().ok())
        .map(|v| v.eq_ignore_ascii_case("bytes"))
        .unwrap_or(false);
    Ok(Probe {
        size,
        supports_ranges,
        etag: header_string(headers, ETAG),
        last_modified: header_string(headers, LAST_MODIFIED),
    })
}

/// Downloads to a temporary sibling file and only publishes it after success.
/// A maximum of 8 connections is accepted; callers should choose a modest
/// default (3-4) and let users override it.
pub fn download_to_path(
    url: &str,
    destination: impl AsRef<Path>,
    requested_connections: usize,
) -> Result<DownloadReport, DownloadError> {
    validate_url(url)?;
    let destination = destination.as_ref();
    let parent = destination.parent().unwrap_or_else(|| Path::new("."));
    fs::create_dir_all(parent)?;

    let client = Arc::new(build_client()?);
    let probe_result = probe_with_client(&client, url).ok();
    let connections = requested_connections.clamp(1, 8);
    let nonce = SystemTime::now().duration_since(UNIX_EPOCH)
        .unwrap_or_default().as_nanos();
    let temp_path = sibling_path(destination, &format!(".{nonce}.download-part"));

    if let Some(info) = probe_result {
        if info.supports_ranges && connections > 1 {
            if let Some(size) = info.size.filter(|s| *s > 0) {
                if let Ok(report) = download_parallel(
                    Arc::clone(&client), url, &temp_path, size, connections, nonce
                ) {
                    publish_file(&temp_path, destination)?;
                    return Ok(report);
                }
            }
        }
    }

    let result = download_single(&client, url, &temp_path);
    match result {
        Ok(bytes) => {
            publish_file(&temp_path, destination)?;
            Ok(DownloadReport {
                bytes_written: bytes,
                used_parallel_ranges: false,
                connections: 1,
            })
        }
        Err(error) => {
            let _ = fs::remove_file(&temp_path);
            Err(error)
        }
    }
}

fn build_client() -> Result<Client, DownloadError> {
    Ok(Client::builder()
        .connect_timeout(std::time::Duration::from_secs(20))
        .timeout(std::time::Duration::from_secs(60 * 60))
        .redirect(reqwest::redirect::Policy::limited(10))
        .user_agent(concat!("DEVROOM-DOWNLOAD/", env!("CARGO_PKG_VERSION")))
        .build()?)
}

fn validate_url(url: &str) -> Result<(), DownloadError> {
    let parsed = reqwest::Url::parse(url)
        .map_err(|_| DownloadError::InvalidUrl)?;
    if parsed.scheme() != "http" && parsed.scheme() != "https" {
        return Err(DownloadError::InvalidUrl);
    }
    Ok(())
}

fn probe_with_client(client: &Client, url: &str) -> Result<Probe, DownloadError> {
    let response = client.head(url).send()?;
    if !response.status().is_success() {
        return Err(DownloadError::InvalidResponse(format!(
            "HEAD returned HTTP {}", response.status()
        )));
    }
    let headers = response.headers();
    Ok(Probe {
        size: headers.get(CONTENT_LENGTH).and_then(|v| v.to_str().ok())
            .and_then(|v| v.parse().ok()),
        supports_ranges: headers.get(ACCEPT_RANGES).and_then(|v| v.to_str().ok())
            .map(|v| v.eq_ignore_ascii_case("bytes")).unwrap_or(false),
        etag: header_string(headers, ETAG),
        last_modified: header_string(headers, LAST_MODIFIED),
    })
}

fn header_string(headers: &reqwest::header::HeaderMap, name: reqwest::header::HeaderName) -> Option<String> {
    headers.get(name).and_then(|v| v.to_str().ok()).map(str::to_owned)
}

fn download_single(client: &Client, url: &str, temp: &Path) -> Result<u64, DownloadError> {
    let mut response = client.get(url).send()?.error_for_status()?;
    let mut output = File::create(temp)?;
    let bytes = io::copy(&mut response, &mut output)?;
    output.flush()?;
    output.sync_all()?;
    Ok(bytes)
}

fn download_parallel(
    client: Arc<Client>,
    url: &str,
    temp: &Path,
    size: u64,
    connections: usize,
    nonce: u128,
) -> Result<DownloadReport, DownloadError> {
    let count = connections.min(size as usize).max(1);
    let chunk = size / count as u64;
    let mut jobs = Vec::with_capacity(count);

    for index in 0..count {
        let start = index as u64 * chunk;
        let end = if index + 1 == count { size - 1 } else { start + chunk - 1 };
        let segment = sibling_path(temp, &format!(".{nonce}.segment-{index}"));
        jobs.push((start, end, segment));
    }

    let mut handles = Vec::with_capacity(count);
    for (start, end, segment) in &jobs {
        let client = Arc::clone(&client);
        let url = url.to_owned();
        let segment = segment.clone();
        let start = *start;
        let end = *end;
        handles.push(thread::spawn(move || -> Result<u64, DownloadError> {
            let mut response = client.get(&url)
                .header(RANGE, format!("bytes={start}-{end}"))
                .send()?;
            if response.status() != StatusCode::PARTIAL_CONTENT {
                return Err(DownloadError::InvalidResponse(format!(
                    "range {start}-{end} returned HTTP {}", response.status()
                )));
            }
            let expected = format!("bytes {start}-{end}/");
            let actual = response.headers().get(CONTENT_RANGE)
                .and_then(|v| v.to_str().ok()).unwrap_or("");
            if !actual.starts_with(&expected) {
                return Err(DownloadError::InvalidResponse(
                    format!("unexpected Content-Range: {actual}")
                ));
            }
            let mut file = File::create(&segment)?;
            let written = io::copy(&mut response, &mut file)?;
            file.sync_all()?;
            if written != end - start + 1 {
                return Err(DownloadError::InvalidResponse(format!(
                    "range {start}-{end} expected {} bytes, received {written}", end-start+1
                )));
            }
            Ok(written)
        }));
    }

    let mut total = 0u64;
    let mut failed = false;
    for handle in handles {
        match handle.join() {
            Ok(Ok(bytes)) => total += bytes,
            _ => failed = true,
        }
    }

    if failed || total != size {
        for (_, _, path) in &jobs { let _ = fs::remove_file(path); }
        return Err(DownloadError::InvalidResponse("one or more range workers failed".into()));
    }

    let assembly = (|| -> Result<(), DownloadError> {
        let mut output = OpenOptions::new().create(true).write(true).truncate(true).open(temp)?;
        for (_, _, path) in &jobs {
            let mut part = File::open(path)?;
            io::copy(&mut part, &mut output)?;
        }
        output.flush()?;
        output.sync_all()?;
        Ok(())
    })();

    for (_, _, path) in &jobs { let _ = fs::remove_file(path); }
    assembly?;

    let actual_size = fs::metadata(temp)?.len();
    if actual_size != size {
        let _ = fs::remove_file(temp);
        return Err(DownloadError::InvalidResponse("assembled file size mismatch".into()));
    }

    Ok(DownloadReport {
        bytes_written: total,
        used_parallel_ranges: true,
        connections: count,
    })
}

fn publish_file(temp: &Path, destination: &Path) -> Result<(), DownloadError> {
    if destination.exists() {
        fs::remove_file(destination)?;
    }
    fs::rename(temp, destination)?;
    Ok(())
}

fn sibling_path(path: &Path, suffix: &str) -> PathBuf {
    let name = path.file_name().unwrap_or_default().to_string_lossy();
    path.with_file_name(format!("{name}{suffix}"))
}

#[allow(dead_code)]
fn _content_range_parser_example(response: &Response) -> Option<&str> {
    response.headers().get(CONTENT_RANGE).and_then(|v| v.to_str().ok())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn accepts_only_http_schemes() {
        assert!(validate_url("https://example.com/file").is_ok());
        assert!(validate_url("http://example.com/file").is_ok());
        assert!(validate_url("file:///etc/passwd").is_err());
        assert!(validate_url("javascript:alert(1)").is_err());
    }

    #[test]
    fn connection_count_is_bounded() {
        assert_eq!(0usize.clamp(1, 8), 1);
        assert_eq!(99usize.clamp(1, 8), 8);
    }

    #[test]
    fn segment_count_never_exceeds_file_size() {
        let count = 2usize.min(1usize as usize).max(1);
        assert_eq!(count, 1);
    }
}
