# DEVROOM DOWNLOAD

Free, local-first native Android download manager by DEVROOM.

**Status:** repository foundation. The feature list is a product target, not a claim that every feature is implemented.

## Principles
- No commercial APIs, mandatory paid services, advertising, account, or hosted backend.
- Kotlin + Jetpack Compose Android application with a Rust download core.
- Local-first operation; current Android downloads use the app-specific Downloads directory, with a public destination picker planned.
- Safe HTTP range downloads, validator-aware resume, retries, and integrity checks.
- Optional media, torrent, and archive adapters with independent license reviews.

## Layout
- android/ — native Android application
- native/ — Rust workspace
- docs/ — product, architecture, security, and release documentation
- .github/workflows/ — CI and APK builds

## Current implementation

- Android app accepts a direct HTTP(S) URL, queues it through Android DownloadManager, shows live progress, restores this app's queued tasks after restart, and supports cancellation.
- Android Sharesheet and HTTP(S) VIEW intents can prefill a shared link.
- Rust core contains an independent HTTP(S) transport with validated byte-range workers, If-Range validators, temporary segment files, and single-stream fallback. Local HTTP integration tests cover successful segmented assembly, required If-Range validators, servers without range support, and servers that ignore range requests.
- The ARM64 JNI library exposes a health check and a blocking native HTTP download entry point. A Kotlin coroutine adapter runs it off the main thread; durable background scheduling, cancellation, notifications, and UI routing are still pending.
- **The Android UI is not yet invoking the Rust downloader.** Its current working download path uses the Android platform DownloadManager while the native engine is tested and integrated further.

## Build
Requirements: JDK 17, Android SDK Platform 36, Gradle 8.13.

    gradle :android:app:assembleDebug

Rust checks:

    cd native
    cargo test --workspace

See docs/BUILD_GUIDE.md.

## Licensing
The project license is intentionally undecided until code provenance and dependency licenses have been audited. Do not copy GPL-licensed upstream code without complying with its obligations. See docs/LICENSE_AUDIT.md.
