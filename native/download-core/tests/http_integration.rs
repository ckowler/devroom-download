use devroom_download_core::{download_to_path, DownloadReport};
use std::fs;
use std::io::{Read, Write};
use std::net::{TcpListener, TcpStream};
use std::path::PathBuf;
use std::thread;

const BODY: &[u8] = b"DEVROOM DOWNLOAD integration test payload: segmented HTTP transfer.";

fn read_request(stream: &mut TcpStream) -> String {
    stream
        .set_read_timeout(Some(std::time::Duration::from_secs(5)))
        .unwrap();
    let mut bytes = Vec::new();
    let mut chunk = [0u8; 1024];
    loop {
        let n = stream.read(&mut chunk).unwrap();
        if n == 0 {
            break;
        }
        bytes.extend_from_slice(&chunk[..n]);
        if bytes.windows(4).any(|w| w == b"\r\n\r\n") {
            break;
        }
    }
    String::from_utf8_lossy(&bytes).into_owned()
}

fn respond(mut stream: TcpStream, range_enabled: bool, range_response_valid: bool) {
    let request = read_request(&mut stream);
    let first = request.lines().next().unwrap_or_default();
    if first.starts_with("HEAD ") {
        let ranges = if range_enabled { "bytes" } else { "none" };
        write!(
            stream,
            "HTTP/1.1 200 OK\r\nContent-Length: {}\r\nAccept-Ranges: {}\r\nETag: \"fixture-v1\"\r\nConnection: close\r\n\r\n",
            BODY.len(),
            ranges
        )
        .unwrap();
        return;
    }

    if let Some(value) = request.lines().find_map(|line| {
        line.strip_prefix("Range: bytes=")
            .or_else(|| line.strip_prefix("range: bytes="))
    }) {
        let (start, end) = value.trim().split_once('-').unwrap();
        let start: usize = start.parse().unwrap();
        let end: usize = end.parse().unwrap();
        if !range_response_valid {
            write!(
                stream,
                "HTTP/1.1 200 OK\r\nContent-Length: {}\r\nConnection: close\r\n\r\n",
                BODY.len()
            )
            .unwrap();
            stream.write_all(BODY).unwrap();
            return;
        }
        let slice = &BODY[start..=end];
        write!(
            stream,
            "HTTP/1.1 206 Partial Content\r\nContent-Length: {}\r\nContent-Range: bytes {}-{}/{}\r\nConnection: close\r\n\r\n",
            slice.len(),
            start,
            end,
            BODY.len()
        )
        .unwrap();
        stream.write_all(slice).unwrap();
    } else {
        write!(
            stream,
            "HTTP/1.1 200 OK\r\nContent-Length: {}\r\nConnection: close\r\n\r\n",
            BODY.len()
        )
        .unwrap();
        stream.write_all(BODY).unwrap();
    }
}

fn fixture(
    range_enabled: bool,
    range_response_valid: bool,
    requests: usize,
) -> (String, thread::JoinHandle<()>) {
    let listener = TcpListener::bind("127.0.0.1:0").unwrap();
    let address = listener.local_addr().unwrap();
    let handle = thread::spawn(move || {
        for stream in listener.incoming().take(requests) {
            if let Ok(stream) = stream {
                respond(stream, range_enabled, range_response_valid);
            }
        }
    });
    (format!("http://{address}/fixture.bin"), handle)
}

fn destination(label: &str) -> PathBuf {
    std::env::temp_dir().join(format!(
        "devroom-download-{label}-{}-{}.bin",
        std::process::id(),
        std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    ))
}

#[test]
fn downloads_and_assembles_validated_parallel_ranges() {
    let (url, server) = fixture(true, true, 4); // HEAD + three workers
    let path = destination("ranges");
    let report: DownloadReport = download_to_path(&url, &path, 3).unwrap();

    server.join().unwrap();
    assert!(report.used_parallel_ranges);
    assert_eq!(report.connections, 3);
    assert_eq!(fs::read(&path).unwrap(), BODY);
    fs::remove_file(path).unwrap();
}

#[test]
fn falls_back_to_single_stream_when_ranges_are_not_advertised() {
    let (url, server) = fixture(false, true, 2); // HEAD + one GET
    let path = destination("fallback");
    let report = download_to_path(&url, &path, 4).unwrap();

    server.join().unwrap();
    assert!(!report.used_parallel_ranges);
    assert_eq!(report.connections, 1);
    assert_eq!(fs::read(&path).unwrap(), BODY);
    fs::remove_file(path).unwrap();
}


#[test]
fn falls_back_when_server_ignores_range_requests() {
    let (url, server) = fixture(true, false, 5); // HEAD + three ranges + fallback GET
    let path = destination("range-rejected");
    let report = download_to_path(&url, &path, 3).unwrap();

    server.join().unwrap();
    assert!(!report.used_parallel_ranges);
    assert_eq!(report.connections, 1);
    assert_eq!(fs::read(&path).unwrap(), BODY);
    fs::remove_file(path).unwrap();
}
