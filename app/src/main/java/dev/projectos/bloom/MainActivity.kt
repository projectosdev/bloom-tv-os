package dev.projectos.bloom

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Home
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
import androidx.compose.ui.window.Dialog
import android.content.pm.PackageManager

private val Night = Color(0xFF080B12)
private val Panel = Color(0xFF151A25)
private val Muted = Color(0xFFAAB2C1)
private val BloomAccent = Color(0xFFE8B7CF)
private val Hairline = Color.White.copy(alpha = 0.10f)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        setContent { MaterialTheme(colorScheme = darkColorScheme()) { BloomRoot() } }
    }
}

private data class TvApp(val title: String, val packageName: String, val detail: String, val mark: String)

@Composable
private fun BloomRoot() {
    var booting by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { kotlinx.coroutines.delay(1800); booting = false }
    Box(Modifier.fillMaxSize().background(Night)) {
        if (booting) BootScreen() else LauncherScreen()
    }
}

@Composable
private fun BootScreen() {
    val pulse by androidx.compose.animation.core.animateFloatAsState(
        targetValue = 1f, animationSpec = tween(1200), label = "boot-pulse"
    )
    Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF202538), Night, Color(0xFF241A28))))) {
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(92.dp).scale(pulse), contentAlignment = Alignment.Center) {
                Box(Modifier.size(76.dp).background(Brush.linearGradient(listOf(Color(0xFFF5DCE8), BloomAccent, Color(0xFF9C718E))), RoundedCornerShape(26.dp)))
                Text("b", color = Night, fontSize = 62.sp, fontWeight = FontWeight.Light)
            }
            Spacer(Modifier.height(22.dp))
            Text("bloom", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Light, letterSpacing = 1.sp)
            Text("A calmer place to watch.", color = Muted, fontSize = 13.sp)
        }
        Text("BLOOM TV OS LITE", Modifier.align(Alignment.BottomCenter).padding(bottom = 30.dp), color = Muted.copy(alpha = .45f), fontSize = 10.sp, letterSpacing = 2.sp)
    }
}

@Composable
private fun LauncherScreen() {
    val context = LocalContext.current
    var selectedPage by remember { mutableStateOf("Home") }
    var showAbout by remember { mutableStateOf(false) }
    val catalog = remember {
        listOf(
            TvApp("Netflix", "com.netflix.ninja", "Films and series", "N"),
            TvApp("YouTube", "com.google.android.youtube.tv", "Videos for every mood", "▶"),
            TvApp("Disney+", "com.disney.disneyplus", "Stories worth sharing", "D+"),
            TvApp("Prime Video", "com.amazon.amazonvideo.livingroom", "Films and television", "P"),
            TvApp("BBC iPlayer", "uk.co.bbc.iplayer", "BBC programmes", "B"),
            TvApp("ITVX", "com.itv.itvplayer", "ITV programmes", "I"),
            TvApp("Spotify", "com.spotify.tv.android", "Music and podcasts", "♪"),
            TvApp("Plex", "com.plexapp.android", "Your personal media", "P")
        )
    }
    val installed = remember {
        catalog.filter { app ->
            runCatching { context.packageManager.getPackageInfo(app.packageName, PackageManager.MATCH_ALL) }.isSuccess
        }
    }

    Column(Modifier.fillMaxSize().background(Night).padding(horizontal = 58.dp, vertical = 34.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("bloom", color = Color.White, fontSize = 29.sp, fontWeight = FontWeight.Light, letterSpacing = 1.sp)
                Text("TELEVISION, REIMAGINED", color = Muted.copy(alpha = .72f), fontSize = 9.sp, letterSpacing = 2.4.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                NavItem("Home", selectedPage == "Home", { selectedPage = "Home" }, Icons.Rounded.Home)
                NavItem("Apps", selectedPage == "Apps", { selectedPage = "Apps" }, Icons.Rounded.Apps)
                NavItem("Settings", selectedPage == "Settings", { selectedPage = "Settings" }, Icons.Rounded.Settings)
            }
            Spacer(Modifier.width(18.dp))
            Row(Modifier.background(Panel, CircleShape).padding(horizontal = 15.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Wifi, null, tint = Muted, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(7.dp))
                Text("Ready", color = Muted, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(34.dp))

        when (selectedPage) {
            "Settings" -> SettingsPage(onAndroidSettings = {
                runCatching { context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            }, onAbout = { showAbout = true })
            else -> {
                if (selectedPage == "Home") {
                    FeaturedPanel(
                        hasApp = installed.isNotEmpty(),
                        onOpen = {
                            installed.firstOrNull()?.let { app ->
                                context.packageManager.getLaunchIntentForPackage(app.packageName)?.let(context::startActivity)
                            }
                        }
                    )
                    Spacer(Modifier.height(28.dp))
                    Text("PICK UP WHERE YOU LEFT OFF", color = Muted.copy(alpha = .8f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
                    Spacer(Modifier.height(14.dp))
                } else {
                    Text("Your apps", color = Color.White, fontSize = 31.sp, fontWeight = FontWeight.Light)
                    Text("The apps already installed on this device.", color = Muted, fontSize = 13.sp)
                    Spacer(Modifier.height(25.dp))
                }
                if (installed.isEmpty()) {
                    EmptyApps()
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(15.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                        items(installed) { app ->
                            AppTile(app) {
                                context.packageManager.getLaunchIntentForPackage(app.packageName)?.let(context::startActivity)
                            }
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("BLOOM TV OS LITE  •  PREVIEW 0.1.0", color = Muted.copy(alpha = .42f), fontSize = 9.sp, letterSpacing = 1.2.sp)
                    Text("${installed.size} APPS READY", color = Muted.copy(alpha = .55f), fontSize = 9.sp, letterSpacing = 1.2.sp)
                }
            }
        }
    }
    if (showAbout) {
        Dialog(onDismissRequest = { showAbout = false }) {
            Surface(color = Panel, shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, Hairline)) {
                Column(Modifier.padding(26.dp)) {
                    Text("About Bloom", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(10.dp))
                    Text("Bloom TV OS Lite 0.1.0", color = BloomAccent, fontSize = 14.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("An early Android launcher preview. It does not replace the device firmware.", color = Muted, fontSize = 13.sp)
                    Spacer(Modifier.height(18.dp))
                    TextButton(onClick = { showAbout = false }) { Text("Done", color = Color.White) }
                }
            }
        }
    }
}

@Composable
private fun NavItem(label: String, selected: Boolean, onClick: () -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    var focused by remember { mutableStateOf(false) }
    val background by animateColorAsState(if (selected || focused) Color(0xFF292431) else Color.Transparent, label = "nav-bg")
    Surface(
        onClick = onClick,
        modifier = Modifier.onFocusChanged { focused = it.isFocused },
        color = background,
        shape = RoundedCornerShape(24.dp),
        border = if (selected || focused) BorderStroke(1.dp, BloomAccent.copy(alpha = .42f)) else null
    ) {
        Row(Modifier.padding(horizontal = 15.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (selected || focused) Color.White else Muted, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(7.dp))
            Text(label, color = if (selected || focused) Color.White else Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun FeaturedPanel(hasApp: Boolean, onOpen: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(292.dp)
            .background(Brush.horizontalGradient(listOf(Color(0xFF252C3C), Color(0xFF302536), Color(0xFF171A24))), RoundedCornerShape(28.dp))
    ) {
        Box(Modifier.align(Alignment.CenterEnd).size(300.dp).background(Brush.radialGradient(listOf(Color(0x55E8B7CF), Color.Transparent)), CircleShape))
        Column(Modifier.align(Alignment.CenterStart).padding(start = 38.dp, end = 28.dp)) {
            Text("YOUR SPACE. YOUR STORIES.", color = BloomAccent, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.4.sp)
            Spacer(Modifier.height(14.dp))
            Text("Everything\nin its place.", color = Color.White, fontSize = 39.sp, lineHeight = 43.sp, fontWeight = FontWeight.Light)
            Spacer(Modifier.height(11.dp))
            Text("A simpler way to get to what you love.", color = Muted, fontSize = 14.sp)
            Spacer(Modifier.height(22.dp))
            Surface(onClick = onOpen, enabled = hasApp, color = Color(0xFFF2E7ED), contentColor = Night, shape = RoundedCornerShape(30.dp)) {
                Text(if (hasApp) "Open an app  →" else "No apps available", Modifier.padding(horizontal = 21.dp, vertical = 12.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Text("BLOOM", Modifier.align(Alignment.BottomEnd).padding(28.dp), color = Color.White.copy(alpha = .10f), fontSize = 43.sp, fontWeight = FontWeight.Light, letterSpacing = 5.sp)
    }
}

@Composable
private fun EmptyApps() {
    Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.Start) {
        Text("Nothing here just yet", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Light)
        Spacer(Modifier.height(6.dp))
        Text("Install compatible TV apps and they will appear here automatically.", color = Muted, fontSize = 12.sp)
    }
}

@Composable
private fun AppTile(app: TvApp, launch: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.045f else 1f, animationSpec = tween(160), label = "tile-scale")
    val tileColor by animateColorAsState(if (focused) Color(0xFF292431) else Panel, label = "tile-color")
    Surface(
        onClick = launch,
        modifier = Modifier.width(174.dp).height(126.dp).scale(scale).onFocusChanged { focused = it.isFocused },
        color = tileColor,
        shape = RoundedCornerShape(19.dp),
        border = BorderStroke(if (focused) 1.5.dp else 1.dp, if (focused) BloomAccent.copy(alpha = .72f) else Hairline)
    ) {
        Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Box(Modifier.size(34.dp).background(Color.White.copy(alpha = .08f), RoundedCornerShape(11.dp)), contentAlignment = Alignment.Center) {
                Text(app.mark, color = BloomAccent, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
            Column {
                Text(app.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(app.detail, color = Muted.copy(alpha = .75f), fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun SettingsPage(onAndroidSettings: () -> Unit, onAbout: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Text("Settings", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Light)
        Text("Make Bloom feel at home.", color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(26.dp))
        SettingsRow("Device settings", "Open Android's system settings", onAndroidSettings)
        SettingsRow("About Bloom", "Version, platform and project information", onAbout)
        Spacer(Modifier.height(18.dp))
        Text("Bloom is an Android launcher preview. System-level updates and full-device reboot controls require platform privileges and are not represented as working features here.", color = Muted.copy(alpha = .7f), fontSize = 11.sp, lineHeight = 17.sp)
    }
}

@Composable
private fun SettingsRow(title: String, detail: String, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Panel, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Hairline)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 17.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Text(detail, color = Muted, fontSize = 11.sp)
            }
            Text("›", color = BloomAccent, fontSize = 25.sp)
        }
    }
    Spacer(Modifier.height(10.dp))
}
