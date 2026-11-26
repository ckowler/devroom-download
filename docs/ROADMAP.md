# Engineering checklist

## Implemented foundation
- [x] Repository and initial project layout
- [x] Native Android Compose application shell
- [x] Direct HTTP(S) URL intake through Android DownloadManager
- [x] Live progress display, task cancellation, and queue restoration after app restart
- [x] Android Sharesheet and HTTP(S) VIEW link intake
- [x] Rust workspace and native HTTP(S) engine
- [x] Validated segmented Range requests with If-Range validators and single-stream fallback
- [x] ARM64 JNI library with health-check and native transfer entry points
- [x] GitHub Actions Android/Rust CI pipeline
- [x] Upstream GPL provenance review
- [x] Rust formatting and host unit tests pass in CI
- [ ] Confirm Android ARM64 native build and APK artifact in CI

## Download engine hardening
- [ ] Validator-aware resume persisted across process death
- [ ] Dynamic segment planner and adaptive concurrency
- [ ] Per-host concurrency and global speed limits
- [ ] Retry policy, network-change recovery, and pause/resume
- [ ] Content digest verification and checksum UI
- [x] Deterministic local HTTP Range integration tests (valid segments, unsupported ranges, rejected ranges)

## Android integration
- [ ] Route actual download execution through Rust JNI worker/service (bridge exists; lifecycle integration remains)
- [ ] Room persistence and startup reconciliation
- [ ] User-Initiated Data Transfer integration on supported Android versions
- [ ] WorkManager scheduling for deferrable work
- [ ] SAF/MediaStore public destination picker
- [ ] Notification actions and download detail screen
- [ ] Robust URL metadata and filename resolution

## Product modules
- [ ] Queue manager and scheduler
- [ ] File manager, categories, search, and history
- [ ] Statistics, speed limits, and network rules
- [ ] Media adapter (yt-dlp/FFmpeg, optional and license-audited)
- [ ] BitTorrent adapter (librqbit, optional and license-audited)
- [ ] Archive adapter and safe extraction
- [ ] Automation rules
- [ ] Russian and English localization

## Release quality
- [ ] Accessibility and adaptive layouts
- [ ] Device/OEM background execution tests
- [ ] Dependency/SBOM/license notices
- [ ] Secure release signing workflow
- [ ] Reproducible release build and signed APK
