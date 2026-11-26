package com.devroom.download

import android.os.Bundle
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Graphite = Color(0xFF101411)
private val Panel = Color(0xFF1A211C)
private val NeonGreen = Color(0xFF8BFF5A)
private val Muted = Color(0xFF9AA69B)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = NeonGreen, background = Graphite, surface = Panel,
                onPrimary = Graphite, onBackground = Color.White, onSurface = Color.White
            )) { DownloadHome() }
        }
    }
}

@Composable
private fun DownloadHome() {
    Surface(modifier = Modifier.fillMaxSize(), color = Graphite) {
        Column(
            modifier = Modifier.fillMaxSize().padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("DEVROOM", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                Text("DOWNLOAD", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                Text("Ваш центр загрузок", color = Muted, fontSize = 14.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard("АКТИВНЫЕ", "0", Modifier.weight(1f))
                MetricCard("СКОРОСТЬ", "—", Modifier.weight(1f))
            }
            Column(
                modifier = Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(20.dp)).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Загрузки", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Пока здесь пусто", color = Muted, fontSize = 14.sp)
                Text("Добавление ссылок и управление загрузками появятся по мере реализации движка.",
                    color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text("ЛОКАЛЬНО  •  БЕЗ РЕКЛАМЫ  •  БЕЗ АККАУНТА",
                modifier = Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(16.dp)).padding(16.dp),
                color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
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
