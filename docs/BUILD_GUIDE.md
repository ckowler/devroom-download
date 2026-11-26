# Build guide

## Android requirements
- JDK 17
- Android SDK Platform 36 and matching build tools
- Gradle 8.13

From repository root:

    gradle :android:app:assembleDebug

APK output: android/app/build/outputs/apk/debug/app-debug.apk

## Rust
Install stable Rust using rustup:

    cd native
    cargo fmt --all -- --check
    cargo test --workspace

The initial Android screen and Rust crate are scaffolding only. Native networking and the Kotlin/Rust bridge are not yet wired. Do not distribute a debug APK as a production release.
