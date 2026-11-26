# Product specification

DEVROOM DOWNLOAD is a free, local-first Android download manager. No mandatory account, hosted backend, advertising, commercial API, or paid runtime service is part of the product.

## Scope
1. HTTP/HTTPS download management: add, pause, resume, cancel, retry, remove, inspect.
2. Segmented downloads only when byte ranges and validators make them safe.
3. Single-stream fallback when range requests are unsupported.
4. Persistent queue and crash-safe recovery.
5. Android-compliant background execution and notifications.
6. User-selected destinations through Storage Access Framework and MediaStore.
7. Link intake through Android Sharesheet and supported intents.
8. File categories, search, sorting, history, and file actions.
9. Scheduling and network constraints.
10. Optional media, BitTorrent, and archive adapters with independent license reviews.
11. Russian and English localization.
12. Accessible, adaptive, dark-first UI.

## Reliability
- Never resume against a resource whose validator indicates it changed.
- Persist state atomically and reconcile database state with partial files at startup.
- Handle redirects, timeouts, throttling, HTTP errors, storage exhaustion, and permission loss.
- Treat filenames and remote metadata as untrusted.
- Verify expected length and available checksums before marking a task complete.
- Parallelism is an optimization, never a correctness requirement.

## Privacy and safety
- No telemetry by default.
- No cloud synchronization requirement.
- Never log authorization headers, cookies, signed URLs, or personal file paths.
- HTTPS by default; cleartext HTTP requires an explicit per-download choice.
- Do not execute downloaded files automatically.
- Do not bypass DRM, paywalls, authentication, or access controls.

## Release acceptance
- Small, large, empty, interrupted, segmented, and single-stream fixtures pass.
- Resume validates resource identity before continuing.
- Process death and restart do not silently corrupt output.
- Background behavior is tested on supported Android versions and documented OEM variants.
- No critical/high security findings.
- All distributed components have documented licenses and notices.
