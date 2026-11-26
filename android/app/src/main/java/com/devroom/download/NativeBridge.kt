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

    /** Blocking call: invoke only from Dispatchers.IO or another worker thread. */
    external fun nativeDownload(url: String, destination: String, connections: Int): String
}
