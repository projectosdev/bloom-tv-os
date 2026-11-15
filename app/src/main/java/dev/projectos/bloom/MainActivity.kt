package dev.projectos.bloom

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Midnight = Color(0xFF090D18)
private val Petal = Color(0xFFFFB8D2)
private val Ink = Color(0xFFE9ECF6)
private val Glass = Color(0x22FFFFFF)
private const val RELEASES_URL = "https://github.com/projectosdev/bloom-tv-os/releases"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        setContent { MaterialTheme { BloomRoot() } }
    }
}

private data class TvApp(val title: String, val packageName: String, val activityName: String, val launcherCategory: String, val detail: String)

private fun discoverTvApps(context: android.content.Context): List<TvApp> {
    val packageManager = context.packageManager
    val categories = listOf(Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER)
    val discovered = linkedMapOf<String, TvApp>()

    categories.forEach { category ->
        val query = Intent(Intent.ACTION_MAIN).addCategory(category)
        @Suppress("DEPRECATION")
        val activities = packageManager.queryIntentActivities(query, PackageManager.MATCH_DEFAULT_ONLY)
        activities.forEach { resolveInfo ->
            val activityInfo = resolveInfo.activityInfo ?: return@forEach
            val packageName = activityInfo.packageName
            if (packageName == context.packageName || discovered.containsKey(packageName)) return@forEach
            val label = resolveInfo.loadLabel(packageManager)?.toString()?.trim().orEmpty()
            if (label.isNotEmpty()) {
                discovered[packageName] = TvApp(label, packageName, activityInfo.name, category, "Installed app")
            }
        }
    }

    if (!discovered.containsKey("com.openai.chatgpt")) {
        discovered["com.openai.chatgpt"] = TvApp(
            "ChatGPT",
            "com.openai.chatgpt",
            "",
            Intent.CATEGORY_LAUNCHER,
            "OpenAI assistant"
        )
    }

    return discovered.values.sortedWith(
        compareBy<TvApp> { if (it.packageName == "com.openai.chatgpt") 0 else 1 }
            .thenBy { it.title.lowercase() }
    )
}

private fun launchBloomApp(context: android.content.Context, app: TvApp) {
    if (app.packageName == "com.openai.chatgpt") {
        val installedIntent = context.packageManager.getLaunchIntentForPackage("com.openai.chatgpt")
        if (installedIntent != null) {
            context.startActivity(installedIntent)
        } else {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://chatgpt.com")))
        }
        return
    }

    val launchIntent = Intent(Intent.ACTION_MAIN)
        .addCategory(app.launcherCategory)
        .setClassName(app.packageName, app.activityName)
    context.startActivity(launchIntent)
}

@Composable
private fun BloomRoot() {
    var booting by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { delay(2600); booting = false }
    Box(
        Modifier.fillMaxSize().background(
            Brush.linearGradient(listOf(Color(0xFF152039), Midnight, Color(0xFF28192C)))
        )
    ) {
        if (booting) BootScreen() else LauncherScreen()
    }
}

@Composable
private fun BootScreen() {
    val motion = rememberInfiniteTransition(label = "petal-motion")
    val scale by motion.animateFloat(
        0.94f, 1.06f,
        infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bloom-scale"
    )
    val glow by motion.animateFloat(
        0.35f, 0.75f,
        infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bloom-glow"
    )
    Column(
        Modifier.fillMaxSize().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(Modifier.size(190.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier.size(178.dp).scale(scale)
                    .background(Color(0x22FFB8D2), CircleShape)
            )
            listOf(-72f, 0f, 72f, 144f, 216f).forEach { angle ->
                Box(
                    Modifier.offset(y = (-34).dp).size(42.dp, 78.dp)
                        .scale(scale)
                        .rotate(angle)
                        .background(
                            Brush.verticalGradient(listOf(Color(0xFFFFEAF1), Petal, Color(0xFFB77CA8))),
                            RoundedCornerShape(50)
                        )
                )
            }
            Box(
                Modifier.size(31.dp)
                    .background(Color(0xFFFFE7A8), CircleShape)
                    .border(2.dp, Color(0xFFFFF4D6), CircleShape)
            )
        }
        Spacer(Modifier.height(24.dp))
        Text("bloom", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Light, letterSpacing = 5.sp)
        Text("TV OS LITE", color = Petal, fontSize = 12.sp, letterSpacing = 6.sp)
        Spacer(Modifier.height(14.dp))
        Text("A lighter way to watch.", color = Ink.copy(alpha = 0.76f), fontSize = 14.sp)
        Spacer(Modifier.height(34.dp))
        LinearProgressIndicator(
            modifier = Modifier.width(112.dp).height(3.dp),
            color = Petal,
            trackColor = Color.White.copy(alpha = 0.12f)
        )
        Spacer(Modifier.height(12.dp))
        Text("GETTING THINGS READY", color = Ink.copy(alpha = 0.42f), fontSize = 9.sp, letterSpacing = 2.sp)
    }
}

@Composable
private fun LauncherScreen() {
    val context = LocalContext.current
    var showSettings by remember { mutableStateOf(false) }
    var showAppLibrary by remember { mutableStateOf(false) }
    var showUpdates by remember { mutableStateOf(false) }
    var showSystemInfo by remember { mutableStateOf(false) }
    val preferences = remember { context.getSharedPreferences("bloom_settings", android.content.Context.MODE_PRIVATE) }
    var animationsEnabled by remember { mutableStateOf(preferences.getBoolean("animations_enabled", true)) }
    var reduceMotion by remember { mutableStateOf(preferences.getBoolean("reduce_motion", false)) }
    val motionAllowed = animationsEnabled && !reduceMotion
    val transitionDuration = if (motionAllowed) 260 else 0

    BackHandler(enabled = showSettings || showAppLibrary) {
        when {
            showUpdates -> showUpdates = false
            showSystemInfo -> showSystemInfo = false
            showSettings -> showSettings = false
            showAppLibrary -> showAppLibrary = false
        }
    }

    var installed by remember { mutableStateOf(discoverTvApps(context)) }
    var clockText by remember {
        mutableStateOf(SimpleDateFormat("EEE, d MMM  •  HH:mm", Locale.getDefault()).format(Date()))
    }
    LaunchedEffect(Unit) {
        while (true) {
            clockText = SimpleDateFormat("EEE, d MMM  •  HH:mm", Locale.getDefault()).format(Date())
            delay(30_000)
        }
    }

    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = !showSettings && !showAppLibrary,
            enter = fadeIn(tween(transitionDuration)) + slideInVertically(initialOffsetY = { if (motionAllowed) it / 30 else 0 }, animationSpec = tween(transitionDuration)),
            exit = fadeOut(tween(if (motionAllowed) 160 else 0)) + slideOutVertically(targetOffsetY = { if (motionAllowed) -it / 40 else 0 }, animationSpec = tween(if (motionAllowed) 180 else 0))
        ) {
            Column(Modifier.fillMaxSize().padding(horizontal = 54.dp, vertical = 34.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("bloom", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Light, letterSpacing = 2.sp)
                        Text("TV OS LITE", color = Petal, fontSize = 10.sp, letterSpacing = 3.sp)
                    }
                    Row(
                        Modifier.background(Glass, RoundedCornerShape(30.dp)).padding(horizontal = 15.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Wifi, null, tint = Ink, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(clockText, color = Ink, fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(14.dp))
                    Surface(
                        onClick = { showAppLibrary = true },
                        color = Glass,
                        shape = RoundedCornerShape(30.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
                    ) {
                        Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Apps", color = Ink, fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Surface(
                        onClick = { showSettings = true },
                        color = Glass,
                        shape = RoundedCornerShape(30.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
                    ) {
                        Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Settings, null, tint = Ink, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Settings", color = Ink, fontSize = 12.sp)
                        }
                    }
                }
                Spacer(Modifier.height(48.dp))
                Text("Welcome home.", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Light)
                Text("Your entertainment, all in one place.", color = Ink.copy(alpha = 0.7f), fontSize = 15.sp)
                Spacer(Modifier.height(24.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth().height(154.dp),
                    color = Color(0x332F2B43),
                    shape = RoundedCornerShape(26.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.13f))
                ) {
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.horizontalGradient(
                                listOf(Color(0x664F3C61), Color(0x332C3C5C), Color(0x225B405F))
                            )
                        ).padding(horizontal = 28.dp, vertical = 20.dp)
                    ) {
                        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("YOUR SPACE. YOUR SHOWS.", color = Petal, fontSize = 10.sp, letterSpacing = 2.5.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(9.dp))
                                Text("Everything starts here.", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Light)
                                Spacer(Modifier.height(5.dp))
                                Text("Open your apps and pick up where you want to go.", color = Ink.copy(alpha = 0.7f), fontSize = 12.sp)
                            }
                            Surface(
                                onClick = { showAppLibrary = true },
                                color = Color(0xFF684D70),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                            ) {
                                Text("Explore apps  ›", modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Box(
                            Modifier.align(Alignment.CenterEnd).offset(x = 12.dp).size(120.dp)
                                .background(Color(0x18FFB8D2), CircleShape)
                        )
                    }
                }
                Spacer(Modifier.height(25.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("YOUR APPS", color = Petal, fontSize = 11.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(18.dp))
                    TextButton(onClick = { installed = discoverTvApps(context) }) {
                        Text("Refresh apps", color = Ink.copy(alpha = 0.75f), fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(18.dp))
                if (installed.isEmpty()) {
                    Text("No supported streaming apps detected yet.", color = Ink, fontSize = 16.sp)
                    Text("Install apps on your device, then select Refresh apps.", color = Ink.copy(alpha = 0.6f), fontSize = 12.sp)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(installed) { app ->
                            AppTile(app, animationsEnabled && !reduceMotion) {
                                runCatching { launchBloomApp(context, app) }
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

        AnimatedVisibility(
            visible = showAppLibrary,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(tween(transitionDuration)),
            exit = fadeOut(tween(if (motionAllowed) 160 else 0))
        ) {
            AppLibraryScreen(
                apps = installed,
                onRefresh = { installed = discoverTvApps(context) },
                onLaunch = { app ->
                    runCatching { launchBloomApp(context, app) }
                },
                onBack = { showAppLibrary = false }
            )
        }

        AnimatedVisibility(
            visible = showSettings,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(tween(transitionDuration)) + slideInVertically(initialOffsetY = { if (motionAllowed) it / 25 else 0 }, animationSpec = tween(transitionDuration)),
            exit = fadeOut(tween(if (motionAllowed) 160 else 0)) + slideOutVertically(targetOffsetY = { if (motionAllowed) it / 35 else 0 }, animationSpec = tween(if (motionAllowed) 180 else 0))
        ) {
            when {
                showUpdates -> UpdateScreen(onBack = { showUpdates = false })
                showSystemInfo -> SystemInfoScreen(onBack = { showSystemInfo = false })
                else -> {
                SettingsScreen(
                    animationsEnabled = animationsEnabled,
                    onAnimationsChange = {
                        animationsEnabled = it
                        preferences.edit().putBoolean("animations_enabled", it).apply()
                    },
                    reduceMotion = reduceMotion,
                    onReduceMotionChange = {
                        reduceMotion = it
                        preferences.edit().putBoolean("reduce_motion", it).apply()
                    },
                    onUpdatesClick = { showUpdates = true },
                    onSystemInfoClick = { showSystemInfo = true },
                    onBack = { showSettings = false }
                )
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    animationsEnabled: Boolean,
    onAnimationsChange: (Boolean) -> Unit,
    reduceMotion: Boolean,
    onReduceMotionChange: (Boolean) -> Unit,
    onUpdatesClick: () -> Unit,
    onSystemInfoClick: () -> Unit,
    onBack: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 54.dp, vertical = 30.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(onClick = onBack, color = Glass, shape = CircleShape, border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))) {
                Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.ArrowBack, "Back", tint = Ink)
                }
            }
            Spacer(Modifier.width(18.dp))
            Column {
                Text("Settings", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Light)
                Text("Make Bloom feel like yours.", color = Ink.copy(alpha = 0.62f), fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(28.dp))
        Text("SYSTEM", color = Petal, fontSize = 11.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        SettingsRow(
            title = "Software update",
            subtitle = "Version 0.1.0  •  View release notes and available builds",
            trailing = "›",
            onClick = onUpdatesClick
        )
        Spacer(Modifier.height(10.dp))
        SettingsRow(
            title = "About this device",
            subtitle = "View Bloom version, device model and Android details",
            trailing = "›",
            onClick = onSystemInfoClick
        )
        Spacer(Modifier.height(26.dp))
        Text("APPEARANCE & MOTION", color = Petal, fontSize = 11.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        SettingsSwitchRow(
            title = "System animations",
            subtitle = "Animate screen transitions and app focus states.",
            checked = animationsEnabled,
            onCheckedChange = onAnimationsChange
        )
        Spacer(Modifier.height(10.dp))
        SettingsSwitchRow(
            title = "Reduce motion",
            subtitle = "Keep movement subtle and minimise animated effects.",
            checked = reduceMotion,
            onCheckedChange = onReduceMotionChange
        )
        Spacer(Modifier.weight(1f))
        Text("BLOOM TV OS LITE  •  PREVIEW 0.1.0", color = Ink.copy(alpha = 0.38f), fontSize = 10.sp, letterSpacing = 1.sp)
    }
}

@Composable
private fun SettingsRow(title: String, subtitle: String, trailing: String, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val background by animateColorAsState(
        if (focused) Color(0xFF3A3045) else Glass,
        animationSpec = tween(180),
        label = "settings-row-background"
    )
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused },
        color = background,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(if (focused) 2.dp else 1.dp, if (focused) Petal else Color.White.copy(alpha = 0.12f))
    ) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 17.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, color = Ink.copy(alpha = 0.62f), fontSize = 12.sp)
            }
            Text(trailing, color = Petal, fontSize = 28.sp)
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Glass,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, color = Ink.copy(alpha = 0.62f), fontSize = 12.sp)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun UpdateScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var openingReleases by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(horizontal = 54.dp, vertical = 30.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(onClick = onBack, color = Glass, shape = CircleShape, border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))) {
                Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.ArrowBack, "Back", tint = Ink)
                }
            }
            Spacer(Modifier.width(18.dp))
            Column {
                Text("Software update", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Light)
                Text("Bloom stays fresh.", color = Ink.copy(alpha = 0.62f), fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(28.dp))
        Surface(
            color = Color(0x332B6657),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, Color(0x667BD8B1))
        ) {
            Row(Modifier.fillMaxWidth().padding(22.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(54.dp).background(Color(0x334CC99A), CircleShape), contentAlignment = Alignment.Center) {
                    Text("✓", color = Color(0xFF9CE6C5), fontSize = 27.sp, fontWeight = FontWeight.Light)
                }
                Spacer(Modifier.width(18.dp))
                Column {
                    Text("You're running Bloom Lite", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Text("Installed version 0.1.0", color = Ink.copy(alpha = 0.72f), fontSize = 13.sp)
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("WHAT'S NEW", color = Petal, fontSize = 11.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Column(
            Modifier.fillMaxWidth().background(Glass, RoundedCornerShape(18.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(18.dp)).padding(20.dp)
        ) {
            Text("Preview build 0.1.0", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            Text("• Bloom welcome screen and launcher foundation", color = Ink.copy(alpha = 0.75f), fontSize = 13.sp)
            Text("• New Settings and Software Update pages", color = Ink.copy(alpha = 0.75f), fontSize = 13.sp)
            Text("• Smoother screen transitions and focus animations", color = Ink.copy(alpha = 0.75f), fontSize = 13.sp)
            Text("• Motion controls, including Reduce motion", color = Ink.copy(alpha = 0.75f), fontSize = 13.sp)
        }
        Spacer(Modifier.height(18.dp))
        Text(
            "Updates are published as official GitHub releases. Bloom will not claim an update is available until a release has actually been published.",
            color = Ink.copy(alpha = 0.55f),
            fontSize = 12.sp
        )
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = {
                openingReleases = true
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(RELEASES_URL)))
                }.onFailure { openingReleases = false }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B405F), contentColor = Color.White)
        ) {
            Text(if (openingReleases) "Opening GitHub releases…" else "View Bloom releases")
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.weight(1f))
        Text("Updates currently require installing a newer Bloom build manually.", color = Ink.copy(alpha = 0.4f), fontSize = 11.sp)
    }
}

@Composable
private fun AppLibraryScreen(
    apps: List<TvApp>,
    onRefresh: () -> Unit,
    onLaunch: (TvApp) -> Unit,
    onBack: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filteredApps = remember(apps, search) {
        apps.filter { it.title.contains(search, ignoreCase = true) }
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 54.dp, vertical = 30.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                onClick = onBack,
                color = Glass,
                shape = CircleShape,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.ArrowBack, "Back", tint = Ink)
                }
            }
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Text("App library", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Light)
                Text("${apps.size} installed apps", color = Ink.copy(alpha = 0.62f), fontSize = 13.sp)
            }
            TextButton(onClick = onRefresh) { Text("Refresh", color = Petal) }
        }
        Spacer(Modifier.height(22.dp))
        TextField(
            value = search,
            onValueChange = { search = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Search installed apps") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Glass,
                unfocusedContainerColor = Glass,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedPlaceholderColor = Ink.copy(alpha = 0.5f),
                unfocusedPlaceholderColor = Ink.copy(alpha = 0.5f),
                focusedIndicatorColor = Petal,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Petal
            )
        )
        Spacer(Modifier.height(16.dp))
        if (filteredApps.isEmpty()) {
            Text(
                if (apps.isEmpty()) "No compatible apps were found. Install an app and refresh." else "No apps match that search.",
                color = Ink.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                items(filteredApps) { app ->
                    Surface(
                        onClick = { onLaunch(app) },
                        modifier = Modifier.fillMaxWidth(),
                        color = Glass,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(app.title.take(1).uppercase(), color = Petal, fontSize = 23.sp, modifier = Modifier.width(42.dp))
                            Column(Modifier.weight(1f)) {
                                Text(app.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text(app.packageName, color = Ink.copy(alpha = 0.48f), fontSize = 10.sp)
                            }
                            Text("Open  ›", color = Petal, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppTile(app: TvApp, animationsEnabled: Boolean, launch: () -> Unit) {
    val context = LocalContext.current
    val appIcon = remember(app.packageName) {
        runCatching {
            val drawable = context.packageManager.getApplicationIcon(app.packageName)
            Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888).also { bitmap ->
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
            }
        }.getOrNull()
    }
    var focused by remember { mutableStateOf(false) }
    val targetScale = if (focused && animationsEnabled) 1.045f else 1f
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(if (animationsEnabled) 190 else 0, easing = FastOutSlowInEasing),
        label = "app-tile-scale"
    )
    val tileColor by animateColorAsState(
        targetValue = if (focused) Color(0xFF44364E) else Glass,
        animationSpec = tween(if (animationsEnabled) 180 else 0),
        label = "app-tile-color"
    )
    Surface(
        onClick = launch,
        modifier = Modifier.width(174.dp).height(142.dp).scale(scale).onFocusChanged { focused = it.isFocused },
        color = tileColor,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(if (focused) 2.dp else 1.dp, if (focused) Petal else Color.White.copy(alpha = 0.12f))
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.SpaceBetween) {
            if (appIcon != null) {
                Image(
                    bitmap = appIcon.asImageBitmap(),
                    contentDescription = "${app.title} app icon",
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Box(
                    Modifier.size(48.dp).background(Color(0x334F405F), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(app.title.take(1).uppercase(), color = Petal, fontSize = 25.sp, fontWeight = FontWeight.Light)
                }
            }
            Column {
                Text(app.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(app.detail, color = Ink.copy(alpha = 0.55f), fontSize = 11.sp)
            }
        }
    }
}
