# Build guide

## Android requirements
- JDK 17
- Android SDK Platform 36 and Build Tools 36.0.0
- Android NDK 27.2.12479018
- Gradle 8.13
- Rust stable with `aarch64-linux-android` target
- `cargo-ndk`

## Build Rust Android library

Install the Rust target and cargo-ndk:

    rustup target add aarch64-linux-android
    cargo install cargo-ndk --locked

Set `ANDROID_NDK_HOME` to the installed NDK directory, then from repository root:

    cargo ndk -t arm64-v8a -o android/app/src/main/jniLibs build --manifest-path native/download-core/Cargo.toml --release

## Build Android APK

From repository root:

    gradle :android:app:assembleDebug

APK output:

    android/app/build/outputs/apk/debug/app-debug.apk

The app currently uses Android DownloadManager for its first working HTTP download path. The Rust HTTP engine is implemented as a separate native library and its JNI health bridge is packaged for ARM64; download execution has not yet been routed through JNI.

## Rust host tests

    cd native
    cargo fmt --all -- --check
    cargo test --workspace
    cargo clippy --workspace --all-targets -- -D warnings

The debug APK is for testing only. Do not distribute it as a production release.
