package dev.projectos.bloom

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val Midnight = Color(0xFF090D18)
private val Petal = Color(0xFFFFB8D2)
private val Ink = Color(0xFFE9ECF6)
private val Glass = Color(0x22FFFFFF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        setContent { MaterialTheme { BloomRoot() } }
    }
}

private data class TvApp(val title: String, val packageName: String, val detail: String)

@Composable
private fun BloomRoot() {
    var booting by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { delay(2600); booting = false }
    Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF152039), Midnight, Color(0xFF28192C))))) {
        if (booting) BootScreen() else LauncherScreen()
    }
}

@Composable
private fun BootScreen() {
    val motion = rememberInfiniteTransition(label = "petal-motion")
    val scale by motion.animateFloat(0.78f, 1.04f, infiniteRepeatable(tween(1700, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "bloom-scale")
    Column(Modifier.fillMaxSize().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(Modifier.size(170.dp).scale(scale), contentAlignment = Alignment.Center) {
            repeat(5) { index ->
                Box(Modifier.size(43.dp, 88.dp).scale(1f - index * 0.025f).background(Brush.verticalGradient(listOf(Color(0xFFFFE0EC), Petal, Color(0xFFB77CA8))), RoundedCornerShape(50)))
            }
            Box(Modifier.size(27.dp).background(Color(0xFFFFE7A8), CircleShape))
        }
        Spacer(Modifier.height(26.dp))
        Text("BLOOM", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Light, letterSpacing = 9.sp)
        Text("TV OS LITE", color = Petal, fontSize = 13.sp, letterSpacing = 5.sp)
        Spacer(Modifier.height(12.dp))
        Text("A lighter way to watch.", color = Ink.copy(alpha = 0.7f), fontSize = 14.sp)
    }
}

@Composable
private fun LauncherScreen() {
    val context = LocalContext.current
    val catalog = remember { listOf(
        TvApp("Netflix", "com.netflix.ninja", "Series & films"),
        TvApp("YouTube", "com.google.android.youtube.tv", "Videos"),
        TvApp("Disney+", "com.disney.disneyplus", "Stories"),
        TvApp("Prime Video", "com.amazon.amazonvideo.livingroom", "Films & TV"),
        TvApp("BBC iPlayer", "uk.co.bbc.iplayer", "BBC programmes"),
        TvApp("ITVX", "com.itv.itvplayer", "ITV programmes"),
        TvApp("Spotify", "com.spotify.tv.android", "Music"),
        TvApp("Plex", "com.plexapp.android", "Your media")
    ) }
    val installed = remember { catalog.filter { app -> runCatching { context.packageManager.getPackageInfo(app.packageName, 0) }.isSuccess } }
    Column(Modifier.fillMaxSize().padding(horizontal = 54.dp, vertical = 34.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("bloom", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Light, letterSpacing = 2.sp)
                Text("TV OS LITE", color = Petal, fontSize = 10.sp, letterSpacing = 3.sp)
            }
            Row(Modifier.background(Glass, RoundedCornerShape(30.dp)).padding(horizontal = 15.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Wifi, null, tint = Ink, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(8.dp)); Text("System ready", color = Ink, fontSize = 12.sp)
            }
            Spacer(Modifier.width(14.dp))
            Surface(onClick = { runCatching { context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } }, color = Glass, shape = RoundedCornerShape(30.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Settings, null, tint = Ink, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text("Settings", color = Ink, fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(48.dp))
        Text("Good evening.", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Light)
        Text("Your entertainment, all in one place.", color = Ink.copy(alpha = 0.7f), fontSize = 15.sp)
        Spacer(Modifier.height(42.dp))
        Text("YOUR APPS", color = Petal, fontSize = 11.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(18.dp))
        if (installed.isEmpty()) {
            Text("No supported streaming apps detected yet.", color = Ink, fontSize = 16.sp)
            Text("Install official TV apps on your Android TV device to see them here.", color = Ink.copy(alpha = 0.6f), fontSize = 12.sp)
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(installed) { app ->
                    AppTile(app) {
                        context.packageManager.getLaunchIntentForPackage(app.packageName)?.let(context::startActivity)
                    }
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("BLOOM TV OS LITE  •  PREVIEW BUILD 0.1.0", color = Ink.copy(alpha = 0.38f), fontSize = 10.sp, letterSpacing = 1.sp)
            Text("${installed.size} APPS AVAILABLE", color = Ink.copy(alpha = 0.55f), fontSize = 10.sp, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun AppTile(app: TvApp, launch: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Surface(onClick = launch, modifier = Modifier.width(174.dp).height(142.dp).onFocusChanged { focused = it.isFocused }, color = if (focused) Color(0xFF44364E) else Glass, shape = RoundedCornerShape(22.dp), border = BorderStroke(if (focused) 2.dp else 1.dp, if (focused) Petal else Color.White.copy(alpha = 0.12f))) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(app.title.take(1), color = Petal, fontSize = 30.sp, fontWeight = FontWeight.Light)
            Column { Text(app.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold); Text(app.detail, color = Ink.copy(alpha = 0.55f), fontSize = 11.sp) }
        }
    }
}
