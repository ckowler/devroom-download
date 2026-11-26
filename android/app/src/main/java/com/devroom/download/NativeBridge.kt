package com.devroom.download

/**
 * JNI entry point for the Rust core.
 * The Android build packages libdevroom_download_core.so for supported ABIs.
 */
object NativeBridge {
    init {
        System.loadLibrary("devroom_download_core")
    }

    external fun nativeHealthCheck(): String
}
