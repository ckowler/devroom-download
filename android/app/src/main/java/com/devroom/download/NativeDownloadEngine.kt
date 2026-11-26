package com.devroom.download

import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class NativeDownloadResult(
    val bytesWritten: Long,
    val usedParallelRanges: Boolean,
    val connections: Int
)

/**
 * Keeps the blocking JNI transfer off the main thread.
 * Lifecycle, notifications, cancellation and durable scheduling are owned by
 * the Android worker/service that calls this adapter.
 */
internal object NativeDownloadEngine {
    suspend fun download(
        url: String,
        destination: File,
        connections: Int = 4
    ): NativeDownloadResult = withContext(Dispatchers.IO) {
        val response = NativeBridge.nativeDownload(
            url,
            destination.absolutePath,
            connections.coerceIn(1, 8)
        )
        val fields = response.split('|')
        if (fields.size != 4 || fields[0] != "OK") {
            val reason = fields.drop(1).joinToString("|").ifBlank { response }
            throw IOException("Native download failed: $reason")
        }

        NativeDownloadResult(
            bytesWritten = fields[1].toLongOrNull()
                ?: throw IOException("Native engine returned an invalid byte count"),
            usedParallelRanges = fields[2].toBooleanStrictOrNull()
                ?: throw IOException("Native engine returned an invalid range flag"),
            connections = fields[3].toIntOrNull()
                ?: throw IOException("Native engine returned an invalid connection count")
        )
    }
}
