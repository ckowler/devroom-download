package com.devroom.download

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale

private val Graphite = Color(0xFF101411)
private val Panel = Color(0xFF1A211C)
private val NeonGreen = Color(0xFF8BFF5A)
private val Muted = Color(0xFF9AA69B)

data class DownloadItem(
    val id: Long,
    val title: String,
    val status: String = "В очереди",
    val downloaded: Long = 0L,
    val total: Long = -1L
)

class MainActivity : ComponentActivity() {
    private var sharedLink by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedLink = extractSharedLink(intent)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = NeonGreen, background = Graphite, surface = Panel,
                onPrimary = Graphite, onBackground = Color.White, onSurface = Color.White
            )) { DownloadHome(sharedLink = sharedLink) }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        sharedLink = extractSharedLink(intent)
    }

    private fun extractSharedLink(intent: Intent?): String? {
        val candidate = when (intent?.action) {
            Intent.ACTION_VIEW -> intent.dataString
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
            else -> null
        } ?: return null
        return candidate.split(Regex("\\s+")).firstOrNull {
            it.startsWith("https://", true) || it.startsWith("http://", true)
        }?.trimEnd('.', ',', ')', ']', '}')
    }
}

@Composable
private fun DownloadHome(sharedLink: String?) {
    val context = LocalContext.current
    val nativeStatus = remember { NativeBridge.nativeHealthCheck() }
    val manager = remember { context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager }
    var urlText by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("Вставьте прямую ссылку на файл.") }
    val downloads = remember { mutableStateListOf<DownloadItem>() }

    LaunchedEffect(sharedLink) {
        if (!sharedLink.isNullOrBlank()) {
            urlText = sharedLink
            message = "Ссылка получена из другого приложения. Проверьте её и начните загрузку."
        }
    }

    LaunchedEffect(Unit) {
        manager.query(DownloadManager.Query())?.use { cursor ->
            val idColumn = cursor.getColumnIndex(DownloadManager.COLUMN_ID)
            val titleColumn = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)
            val statusColumn = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val bytesColumn = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            val totalColumn = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = if (titleColumn >= 0) cursor.getString(titleColumn) ?: "Загрузка" else "Загрузка"
                val statusCode = if (statusColumn >= 0) cursor.getInt(statusColumn) else -1
                val bytes = if (bytesColumn >= 0) cursor.getLong(bytesColumn) else 0L
                val total = if (totalColumn >= 0) cursor.getLong(totalColumn) else -1L
                downloads.add(DownloadItem(id, title, statusLabel(statusCode), bytes, total))
            }
        }
        while (true) {
            downloads.indices.forEach { index ->
                val current = downloads[index]
                val query = DownloadManager.Query().setFilterById(current.id)
                manager.query(query)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        val bytesIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                        val totalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                        val statusCode = if (statusIndex >= 0) cursor.getInt(statusIndex) else -1
                        val downloaded = if (bytesIndex >= 0) cursor.getLong(bytesIndex) else 0L
                        val total = if (totalIndex >= 0) cursor.getLong(totalIndex) else -1L
                        downloads[index] = current.copy(status = statusLabel(statusCode), downloaded = downloaded, total = total)
                    }
                }
            }
            delay(900)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Graphite) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("DEVROOM", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                Text("DOWNLOAD", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                Text("Ваш центр загрузок", color = Muted, fontSize = 14.sp)
                Text("CORE • $nativeStatus", color = NeonGreen, fontSize = 10.sp)
            }

            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Новая загрузка", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    OutlinedTextField(
                        value = urlText,
                        onValueChange = { urlText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Ссылка на файл") },
                        placeholder = { Text("https://example.com/file.zip") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                    )
                    Button(
                        onClick = {
                            val raw = urlText.trim()
                            val parsed = runCatching { Uri.parse(raw) }.getOrNull()
                            if (parsed == null || parsed.scheme?.lowercase(Locale.ROOT) !in listOf("http", "https") || parsed.host.isNullOrBlank()) {
                                message = "Укажите корректную HTTP или HTTPS ссылку."
                            } else {
                                runCatching {
                                    val fileName = safeFileName(parsed.lastPathSegment ?: "download")
                                    val request = DownloadManager.Request(parsed)
                                        .setTitle(fileName)
                                        .setDescription("DEVROOM DOWNLOAD")
                                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                        .setAllowedOverMetered(true)
                                        .setAllowedOverRoaming(false)
                                        .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
                                    val id = manager.enqueue(request)
                                    downloads.add(0, DownloadItem(id = id, title = fileName))
                                    urlText = ""
                                    message = "Загрузка добавлена."
                                }.onFailure { message = "Не удалось добавить загрузку: ${it.localizedMessage ?: "ошибка"}" }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Graphite)
                    ) {
                        Text("Начать загрузку", fontWeight = FontWeight.Bold)
                    }
                    Text(message, color = Muted, fontSize = 12.sp)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard("ЗАДАЧ", downloads.size.toString(), Modifier.weight(1f))
                MetricCard("ЗАВЕРШЕНО", downloads.count { it.status == "Завершено" }.toString(), Modifier.weight(1f))
            }

            Text("Загрузки", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            if (downloads.isEmpty()) {
                Text("Пока здесь пусто. Добавьте прямую ссылку выше.", color = Muted, fontSize = 13.sp)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                    items(downloads, key = { it.id }) { item ->
                        DownloadCard(item, onCancel = { id ->
                            manager.remove(id)
                            downloads.removeAll { it.id == id }
                        })
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text("ЛОКАЛЬНО  •  БЕЗ РЕКЛАМЫ  •  БЕЗ АККАУНТА",
                color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun DownloadCard(item: DownloadItem, onCancel: (Long) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(item.title, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 2)
            Text(item.status, color = NeonGreen, fontSize = 12.sp)
            val progress = if (item.total > 0) (item.downloaded.toFloat() / item.total).coerceIn(0f, 1f) else 0f
            androidx.compose.material3.LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = NeonGreen,
                trackColor = Color(0xFF344137)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${formatBytes(item.downloaded)} / ${if (item.total > 0) formatBytes(item.total) else "—"}",
                    color = Muted, fontSize = 11.sp)
                if (item.status != "Завершено" && item.status != "Ошибка загрузки") {
                    Button(onClick = { onCancel(item.id) }) { Text("Отменить") }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.background(Panel, RoundedCornerShape(18.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(label, color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text(value, color = NeonGreen, fontSize = 26.sp, fontWeight = FontWeight.Bold)
    }
}

private fun safeFileName(input: String): String {
    val cleaned = input.substringAfterLast('/').substringAfterLast('\\')
        .replace(Regex("[^A-Za-z0-9._ -]"), "_")
        .trim().take(120)
    return cleaned.ifBlank { "download.bin" }
}

private fun statusLabel(code: Int): String = when (code) {
    DownloadManager.STATUS_PENDING -> "Ожидает"
    DownloadManager.STATUS_RUNNING -> "Загружается"
    DownloadManager.STATUS_PAUSED -> "Приостановлено"
    DownloadManager.STATUS_SUCCESSFUL -> "Завершено"
    DownloadManager.STATUS_FAILED -> "Ошибка загрузки"
    else -> "Проверка состояния"
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 0) return "—"
    val units = listOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unit = 0
    while (value >= 1024 && unit < units.lastIndex) {
        value /= 1024
        unit++
    }
    return String.format(Locale.getDefault(), "%.1f %s", value, units[unit])
}
