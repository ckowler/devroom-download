# DEVROOM DOWNLOAD

Free, local-first native Android download manager by DEVROOM.

**Status:** repository foundation. The feature list is a product target, not a claim that every feature is implemented.

## Principles
- No commercial APIs, mandatory paid services, advertising, account, or hosted backend.
- Kotlin + Jetpack Compose Android application with a Rust download core.
- Local persistence and user-controlled file destinations.
- Safe HTTP range downloads, validator-aware resume, retries, and integrity checks.
- Optional media, torrent, and archive adapters with independent license reviews.

## Layout
- android/ — native Android application
- native/ — Rust workspace
- docs/ — product, architecture, security, and release documentation
- .github/workflows/ — CI and APK builds

## Build
Requirements: JDK 17, Android SDK Platform 36, Gradle 8.13.

    gradle :android:app:assembleDebug

Rust checks:

    cd native
    cargo test --workspace

See docs/BUILD_GUIDE.md.

## Licensing
The project license is intentionally undecided until code provenance and dependency licenses have been audited. Do not copy GPL-licensed upstream code without complying with its obligations. See docs/LICENSE_AUDIT.md.
