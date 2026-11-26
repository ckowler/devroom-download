# Engineering checklist

## Foundation
- [x] Repository and initial project layout
- [x] Native Android Compose shell
- [x] Rust workspace and smoke tests
- [ ] Verify Gradle/SDK build in CI
- [ ] Audit upstream code provenance and licenses

## Download core
- [ ] Typed domain model and state machine
- [ ] HTTP probe and redirect policy
- [ ] Single-stream downloader
- [ ] Range capability detection
- [ ] Segmented scheduler and adaptive workers
- [ ] Validator-safe resume
- [ ] Crash-safe checkpoint and reconciliation
- [ ] Integrity verification and deterministic test server

## Android integration
- [ ] Room persistence
- [ ] Kotlin/Rust bridge
- [ ] UIDT and WorkManager integration
- [ ] Notifications and actions
- [ ] SAF/MediaStore destinations
- [ ] Share and open-link intents

## Product modules
- [ ] Queue and scheduler
- [ ] File manager and history
- [ ] Statistics and speed limits
- [ ] Media adapter
- [ ] Torrent adapter
- [ ] Archive adapter
- [ ] Automation rules
- [ ] Russian and English localization

## Release quality
- [ ] Accessibility and adaptive layouts
- [ ] Device/OEM background tests
- [ ] Dependency/SBOM/license notices
- [ ] Secure release signing workflow
- [ ] Reproducible release build
