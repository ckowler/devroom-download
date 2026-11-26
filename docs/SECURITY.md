# Security model

- Treat URLs, headers, redirects, filenames, MIME types, archive entries, torrent metadata, and media metadata as untrusted.
- Never log bearer tokens, cookies, signed query parameters, or private URLs.
- HTTPS by default. Cleartext HTTP requires explicit per-download warning and choice.
- Prevent path traversal and unsafe archive extraction, including absolute paths, parent traversal, symlink escapes, and decompression bombs.
- Do not auto-open or execute downloaded content.
- Validate TLS normally; never ship a trust-all certificate manager.
- Bound redirects, retries, parser resource usage, and untrusted response handling.
- Keep signing keys and credentials out of Git.
- Review native binaries and dependencies before release.
- No DRM circumvention, credential theft, paywall bypass, or access-control evasion.
