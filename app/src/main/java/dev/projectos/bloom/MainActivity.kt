package dev.projectos.bloom

import android.content.ActivityNotFoundException
import android.content.Intent
import android.app.DownloadManager
import android.os.Environment
import android.graphics.Bitmap
import android.graphics.Canvas
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Build
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
import android.widget.Toast

private val Midnight = Color(0xFF050812)
private val BloomAccent = mutableStateOf(Color(0xFFB8C7FF))
private val BloomThemes = listOf(Color(0xFFB8C7FF), Color(0xFFFFB8D2), Color(0xFF9DE4D3), Color(0xFFFFD18C), Color(0xFFC7AEFF))
private val BloomThemeNames = listOf("Glacier", "Rose", "Mint", "Sunset", "Lilac")
private val Ink = Color(0xFFE9ECF6)
private val Glass = Color(0x20FFFFFF)

private const val RELEASES_URL = "https://github.com/projectosdev/bloom-tv-os/releases"
private const val LATEST_RELEASE_API = "https://api.github.com/repos/projectosdev/bloom-tv-os/releases/latest"

private data class BloomUpdate(
    val versionName: String,
    val notes: String,
    val apkUrl: String,
    val releaseUrl: String
)

private suspend fun checkForBloomUpdate(context: android.content.Context): BloomUpdate? =
    withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            connection = (URL(LATEST_RELEASE_API).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "Bloom-TV-OS-Lite")
            }
            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("GitHub returned HTTP $responseCode while checking for updates.")
            }
            val release = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            val tag = release.optString("tag_name").trim()
            val latestVersion = tag.removePrefix("v")
            if (latestVersion.isBlank()) {
                throw IOException("The latest Bloom release has an invalid version tag.")
            }
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val installedVersion = packageInfo.versionName
                ?: throw IOException("Bloom could not read the installed app version.")
            val comparison = try {
                compareBloomVersions(latestVersion, installedVersion)
            } catch (error: IllegalArgumentException) {
                throw IOException("Bloom received an unsupported release version tag.", error)
            }
            if (comparison <= 0) return@withContext null
            val assets = release.optJSONArray("assets")
                ?: throw IOException("The latest Bloom release does not include downloadable assets.")
            var apkUrl: String? = null
            for (index in 0 until assets.length()) {
                val asset = assets.optJSONObject(index) ?: continue
                if (asset.optString("name").endsWith(".apk", ignoreCase = true)) {
                    apkUrl = asset.optString("browser_download_url").takeIf {
                        it.startsWith("https://github.com/projectosdev/bloom-tv-os/releases/download/")
                    }
                    break
                }
            }
            val download = apkUrl
                ?: throw IOException("The latest Bloom release does not include a trusted APK download.")
            val releaseUrl = release.optString("html_url")
                .takeIf { it.startsWith("https://github.com/projectosdev/bloom-tv-os/releases/") }
                ?: RELEASES_URL
            BloomUpdate(
                versionName = tag.removePrefix("v"),
                notes = release.optString("body").ifBlank { "Bug fixes and improvements." },
                apkUrl = download,
                releaseUrl = releaseUrl
            )
        } finally {
            connection?.disconnect()
        }
    }


private suspend fun downloadAndInstallBloomUpdate(
    context: android.content.Context,
    update: BloomUpdate,
    onStatus: (String) -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
        !context.packageManager.canRequestPackageInstalls()
    ) {
        withContext(Dispatchers.Main) {
            val settingsIntent = Intent(
                android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(settingsIntent)
                onStatus("Allow Bloom to install apps in Android settings, then return and choose Update now again.")
            } catch (_: ActivityNotFoundException) {
                onStatus("Android is blocking app installation. Enable Install unknown apps for Bloom in Settings, then try again.")
            }
        }
        return
    }

    val manager = context.getSystemService(android.content.Context.DOWNLOAD_SERVICE) as DownloadManager
    val filename = "bloom-update-${update.versionName.replace(Regex("[^A-Za-z0-9._-]"), "_")}.apk"
    val request = DownloadManager.Request(Uri.parse(update.apkUrl))
        .setTitle("Bloom update ${update.versionName}")
        .setDescription("Downloading the latest Bloom update")
        .setMimeType("application/vnd.android.package-archive")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, filename)
    val downloadId = manager.enqueue(request)
    onStatus("Downloading Bloom ${update.versionName}…")
    val apkUri = withContext(Dispatchers.IO) {
        repeat(180) {
            val cursor = manager.query(DownloadManager.Query().setFilterById(downloadId))
            cursor.use {
                if (it != null && it.moveToFirst()) {
                    when (it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_SUCCESSFUL -> return@withContext manager.getUriForDownloadedFile(downloadId)
                        DownloadManager.STATUS_FAILED -> {
                            val reason = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                            throw IllegalStateException("Download failed (code $reason)")
                        }
                    }
                }
            }
            delay(1000)
        }
        throw IllegalStateException("The download is taking too long. Check your connection and try again.")
    } ?: throw IllegalStateException("Bloom couldn't access the downloaded update.")
    withContext(Dispatchers.Main) {
        onStatus("Download complete. Waiting for installation approval…")
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(installIntent)
    }
}

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

    return discovered.values.sortedWith(
        compareBy<TvApp> { if (it.packageName == "com.openai.chatgpt") 0 else 1 }
            .thenBy { it.title.lowercase() }
    )
}

private fun launchBloomApp(context: android.content.Context, app: TvApp) {
    val launchIntent = if (app.packageName == "com.openai.chatgpt") {
        context.packageManager.getLaunchIntentForPackage("com.openai.chatgpt")
            ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://chatgpt.com"))
    } else {
        Intent(Intent.ACTION_MAIN)
            .addCategory(app.launcherCategory)
            .setClassName(app.packageName, app.activityName)
    }

    try {
        context.startActivity(launchIntent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "${app.title} is no longer available on this device.", Toast.LENGTH_SHORT).show()
    } catch (_: SecurityException) {
        Toast.makeText(context, "Android blocked ${app.title} from opening.", Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun BloomRoot() {
    var booting by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { delay(2600); booting = false }
    val atmosphere = rememberInfiniteTransition(label = "bloom-atmosphere")
    val drift by atmosphere.animateFloat(-38f, 38f, infiniteRepeatable(tween(9000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "atmosphere-drift")
    val glow by atmosphere.animateFloat(0.55f, 0.95f, infiniteRepeatable(tween(5200), RepeatMode.Reverse), label = "atmosphere-glow")
    Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF111D39), Midnight, Color(0xFF21172F))))) {
        Box(Modifier.offset(x = drift.dp, y = (-110).dp).size(520.dp).background(Brush.radialGradient(listOf(Color(0x665D78D9).copy(alpha = glow), Color.Transparent)), CircleShape))
        Box(Modifier.align(Alignment.BottomEnd).offset(x = 130.dp, y = 150.dp).size(560.dp).background(Brush.radialGradient(listOf(Color(0x554D9FAD), Color.Transparent)), CircleShape))
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
                            Brush.verticalGradient(listOf(Color(0xFFFFEAF1), BloomAccent.value, Color(0xFFB77CA8))),
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
        Text("TV OS LITE", color = BloomAccent.value, fontSize = 12.sp, letterSpacing = 6.sp)
        Spacer(Modifier.height(14.dp))
        Text("A lighter way to watch.", color = Ink.copy(alpha = 0.76f), fontSize = 14.sp)
        Spacer(Modifier.height(34.dp))
        LinearProgressIndicator(
            modifier = Modifier.width(112.dp).height(3.dp),
            color = BloomAccent.value,
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
    var showStore by remember { mutableStateOf(false) }
    var showUpdates by remember { mutableStateOf(false) }
    var showSystemInfo by remember { mutableStateOf(false) }
    var showControlCentre by remember { mutableStateOf(false) }
    var showBloomSearch by remember { mutableStateOf(false) }
    var showAmbient by remember { mutableStateOf(false) }
    val preferences = remember { context.getSharedPreferences("bloom_settings", android.content.Context.MODE_PRIVATE) }
    var showProfiles by remember { mutableStateOf(false) }
    var themeIndex by remember { mutableIntStateOf(preferences.getInt("theme_index", 0).coerceIn(0, BloomThemes.lastIndex)) }
    BloomAccent.value = BloomThemes[themeIndex]
    var profileName by remember { mutableStateOf(preferences.getString("profile_name", "").orEmpty()) }
    var showSignUp by remember { mutableStateOf(!preferences.getBoolean("profile_setup_complete", false)) }
    var availableUpdate by remember { mutableStateOf<BloomUpdate?>(null) }
    var updateBusy by remember { mutableStateOf(false) }
    var updateCheckInProgress by remember { mutableStateOf(false) }
    var hasCompletedUpdateCheck by remember { mutableStateOf(false) }
    var updateMessage by remember { mutableStateOf("") }
    val updateScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        while (true) {
            updateCheckInProgress = true
            val check = runCatching { checkForBloomUpdate(context) }
            if (check.isSuccess) {
                availableUpdate = check.getOrNull()
                hasCompletedUpdateCheck = true
                updateMessage = if (availableUpdate == null) {
                    "Bloom is up to date."
                } else {
                    "A new version is ready to install."
                }
            } else {
                updateMessage = "Couldn't check for updates. Check your internet connection and try again."
            }
            updateCheckInProgress = false
            delay(6 * 60 * 60 * 1000L)
        }
    }
    var animationsEnabled by remember { mutableStateOf(preferences.getBoolean("animations_enabled", true)) }
    var reduceMotion by remember { mutableStateOf(preferences.getBoolean("reduce_motion", false)) }
    var showSystemCards by remember { mutableStateOf(preferences.getBoolean("show_system_cards", true)) }
    var showWelcomePanel by remember { mutableStateOf(preferences.getBoolean("show_welcome_panel", true)) }
    var compactTiles by remember { mutableStateOf(preferences.getBoolean("compact_tiles", false)) }
    val motionAllowed = animationsEnabled && !reduceMotion
    val transitionDuration = if (motionAllowed) 260 else 0

    BackHandler(enabled = showSettings || showAppLibrary || showStore || showSignUp || showControlCentre || showBloomSearch || showAmbient || showProfiles) {
        when {
            showSignUp -> showSignUp = false
            showControlCentre -> showControlCentre = false
            showBloomSearch -> showBloomSearch = false
            showAmbient -> showAmbient = false
            showProfiles -> showProfiles = false
            showUpdates -> showUpdates = false
            showSystemInfo -> showSystemInfo = false
            showSettings -> showSettings = false
            showAppLibrary -> showAppLibrary = false
            showStore -> showStore = false
        }
    }

    var installed by remember { mutableStateOf(discoverTvApps(context)) }
    val activity = context as? ComponentActivity
    DisposableEffect(activity) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                installed = discoverTvApps(context)
            }
        }
        activity?.lifecycle?.addObserver(observer)
        onDispose { activity?.lifecycle?.removeObserver(observer) }
    }

    val homeFocusRequester = remember { FocusRequester() }
    val storeFocusRequester = remember { FocusRequester() }
    val homeVisible = !showSettings && !showAppLibrary && !showStore && !showSignUp &&
        !showControlCentre && !showBloomSearch && !showAmbient && !showProfiles
    LaunchedEffect(homeVisible, installed.firstOrNull()?.packageName) {
        if (homeVisible) {
            withFrameNanos { }
            runCatching {
                if (installed.isNotEmpty()) homeFocusRequester.requestFocus()
                else storeFocusRequester.requestFocus()
            }
        }
    }

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
            visible = homeVisible,
            enter = fadeIn(tween(transitionDuration)) + slideInVertically(initialOffsetY = { if (motionAllowed) it / 30 else 0 }, animationSpec = tween(transitionDuration)),
            exit = fadeOut(tween(if (motionAllowed) 160 else 0)) + slideOutVertically(targetOffsetY = { if (motionAllowed) -it / 40 else 0 }, animationSpec = tween(if (motionAllowed) 180 else 0))
        ) {
            Column(Modifier.fillMaxSize().padding(horizontal = 54.dp, vertical = 34.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("bloom", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Light, letterSpacing = 2.sp)
                        Text("TV OS LITE", color = BloomAccent.value, fontSize = 10.sp, letterSpacing = 3.sp)
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
                        modifier = Modifier.focusRequester(storeFocusRequester),
                        onClick = { showStore = true },
                        color = Color(0xFF684D70),
                        shape = RoundedCornerShape(30.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Store", color = Color.White, fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.width(10.dp))
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
                        onClick = { showProfiles = true },
                        color = Color(0x334F6A9A),
                        shape = RoundedCornerShape(30.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
                    ) {
                        Row(Modifier.padding(horizontal = 13.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(25.dp).background(Color(0xFF879ED8), CircleShape), contentAlignment = Alignment.Center) {
                                Text(profileName.take(1).ifBlank { "B" }.uppercase(), color = Color(0xFF11182A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(profileName.ifBlank { "Profile" }, color = Ink, fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Surface(
                        onClick = { showControlCentre = true },
                        color = Color(0x334F6A9A),
                        shape = RoundedCornerShape(30.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
                    ) {
                        Text("Quick settings", modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp), color = Ink, fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(8.dp))
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
                Spacer(Modifier.height(28.dp))
                Text(if (profileName.isBlank()) "Your screen. Reimagined." else "Welcome back, $profileName.", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Light, letterSpacing = (-1).sp)
                Text("A little more atmosphere. A lot more to explore.", color = Ink.copy(alpha = 0.72f), fontSize = 14.sp)
                Spacer(Modifier.height(18.dp))
                if (showWelcomePanel) Surface(
                    modifier = Modifier.fillMaxWidth().height(158.dp),
                    color = Color(0x443D4A70),
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
                                Text("WELCOME TO BLOOM", color = BloomAccent.value, fontSize = 10.sp, letterSpacing = 2.5.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(9.dp))
                                Text("Make tonight yours.", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Light)
                                Spacer(Modifier.height(5.dp))
                                Text("Your favourite entertainment, gathered in one beautiful place.", color = Ink.copy(alpha = 0.76f), fontSize = 13.sp)
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
                Spacer(Modifier.height(13.dp))
                if (showSystemCards) {
                    Text("AT A GLANCE", color = BloomAccent.value, fontSize = 9.sp, letterSpacing = 2.5.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SystemMetricCard("APP LIBRARY", installed.size.toString(), "Available on this device", Modifier.weight(1f))
                        SystemMetricCard("BLOOM PROFILE", profileName.ifBlank { "Not set" }, "Personal space", Modifier.weight(1f))
                        SystemMetricCard("SYSTEM", "Android ${Build.VERSION.RELEASE}", "${Build.MANUFACTURER} ${Build.MODEL}".take(34), Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(13.dp))
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("YOUR APPS", color = BloomAccent.value, fontSize = 11.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(18.dp))
                    TextButton(onClick = { installed = discoverTvApps(context) }) {
                        Text("Refresh apps", color = Ink.copy(alpha = 0.75f), fontSize = 12.sp)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextButton(onClick = { showBloomSearch = true }) { Text("⌕  Search Bloom", color = BloomAccent.value) }
                    TextButton(onClick = { showAmbient = true }) { Text("✦  Bloom Ambient", color = BloomAccent.value) }
                    TextButton(onClick = { runCatching { context.packageManager.getLaunchIntentForPackage("com.openai.chatgpt")?.let { context.startActivity(it) } ?: context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://chatgpt.com"))) } }) { Text("✧  Bloom Assist", color = BloomAccent.value) }
                }
                Spacer(Modifier.height(8.dp))
                if (installed.isEmpty()) {
                    Text("No supported streaming apps detected yet.", color = Ink, fontSize = 16.sp)
                    Text("Install apps on your device, then select Refresh apps.", color = Ink.copy(alpha = 0.6f), fontSize = 12.sp)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(installed, key = { it.packageName }) { app ->
                            AppTile(
                                app,
                                animationsEnabled && !reduceMotion,
                                compactTiles,
                                focusRequester = if (installed.firstOrNull()?.packageName == app.packageName) homeFocusRequester else null
                            ) {
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
            visible = showSignUp,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(tween(transitionDuration)) + slideInVertically(initialOffsetY = { if (motionAllowed) it / 24 else 0 }, animationSpec = tween(transitionDuration)),
            exit = fadeOut(tween(if (motionAllowed) 160 else 0))
        ) {
            BloomSignUpScreen(
                initialName = profileName,
                onComplete = { name ->
                    profileName = name
                    preferences.edit().putString("profile_name", name).putBoolean("profile_setup_complete", true).apply()
                    showSignUp = false
                },
                onSkip = {
                    preferences.edit().putBoolean("profile_setup_complete", true).apply()
                    showSignUp = false
                }
            )
        }

        AnimatedVisibility(
            visible = showStore,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(tween(transitionDuration)),
            exit = fadeOut(tween(if (motionAllowed) 160 else 0))
        ) {
            BloomStoreScreen(onBack = { showStore = false })
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

        AnimatedVisibility(visible = showControlCentre, modifier = Modifier.fillMaxSize(), enter = fadeIn(tween(180)), exit = fadeOut(tween(140))) {
            BloomControlCentre(
                animationsEnabled = animationsEnabled, onAnimations = { animationsEnabled = it; preferences.edit().putBoolean("animations_enabled", it).apply() },
                reduceMotion = reduceMotion, onReduceMotion = { reduceMotion = it; preferences.edit().putBoolean("reduce_motion", it).apply() },
                showCards = showSystemCards, onShowCards = { showSystemCards = it; preferences.edit().putBoolean("show_system_cards", it).apply() },
                compactTiles = compactTiles, onCompactTiles = { compactTiles = it; preferences.edit().putBoolean("compact_tiles", it).apply() },
                onClose = { showControlCentre = false }, onSettings = { showControlCentre = false; showSettings = true }
            )
        }

        AnimatedVisibility(visible = showBloomSearch, modifier = Modifier.fillMaxSize(), enter = fadeIn(tween(180)), exit = fadeOut(tween(140))) {
            BloomSearchScreen(apps = installed, onBack = { showBloomSearch = false }, onLaunch = { app -> showBloomSearch = false; runCatching { launchBloomApp(context, app) } }, onSettings = { showBloomSearch = false; showSettings = true })
        }

        AnimatedVisibility(visible = showProfiles, modifier = Modifier.fillMaxSize(), enter = fadeIn(tween(180)), exit = fadeOut(tween(140))) {
            BloomProfilesScreen(currentName = profileName, onSelect = { name -> profileName = name; preferences.edit().putString("profile_name", name).putBoolean("profile_setup_complete", true).apply(); showProfiles = false }, onBack = { showProfiles = false })
        }

        AnimatedVisibility(visible = showAmbient, modifier = Modifier.fillMaxSize(), enter = fadeIn(tween(350)), exit = fadeOut(tween(220))) {
            BloomAmbientScreen(onExit = { showAmbient = false })
        }

        AnimatedVisibility(
            visible = showSettings,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(tween(transitionDuration)) + slideInVertically(initialOffsetY = { if (motionAllowed) it / 25 else 0 }, animationSpec = tween(transitionDuration)),
            exit = fadeOut(tween(if (motionAllowed) 160 else 0)) + slideOutVertically(targetOffsetY = { if (motionAllowed) it / 35 else 0 }, animationSpec = tween(if (motionAllowed) 180 else 0))
        ) {
            when {
                showUpdates -> UpdateScreen(
                    update = availableUpdate,
                    message = updateMessage,
                    busy = updateBusy || updateCheckInProgress,
                    checking = updateCheckInProgress,
                    downloading = updateBusy,
                    hasChecked = hasCompletedUpdateCheck,
                    onCheck = {
                        if (!updateCheckInProgress && !updateBusy) {
                            updateScope.launch {
                                updateCheckInProgress = true
                                updateMessage = "Checking for updates…"
                                try {
                                    val check = runCatching { checkForBloomUpdate(context) }
                                    if (check.isSuccess) {
                                        availableUpdate = check.getOrNull()
                                        hasCompletedUpdateCheck = true
                                        updateMessage = if (availableUpdate == null) "Bloom is up to date." else "A new version is ready to install."
                                    } else {
                                        updateMessage = "Couldn't check for updates. Check your internet connection and try again."
                                    }
                                } finally {
                                    updateCheckInProgress = false
                                }
                            }
                        }
                    },
                    onInstall = { update ->
                        if (!updateBusy && !updateCheckInProgress) {
                            updateScope.launch {
                                updateBusy = true
                                runCatching { downloadAndInstallBloomUpdate(context, update) { updateMessage = it } }
                                    .onFailure { updateMessage = it.message ?: "Couldn't install this update." }
                                updateBusy = false
                            }
                        }
                    },
                    onBack = { showUpdates = false }
                )
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
                    showSystemCards = showSystemCards,
                    showWelcomePanel = showWelcomePanel,
                    onShowWelcomePanelChange = { showWelcomePanel = it; preferences.edit().putBoolean("show_welcome_panel", it).apply() },
                    onShowSystemCardsChange = {
                        showSystemCards = it
                        preferences.edit().putBoolean("show_system_cards", it).apply()
                    },
                    themeIndex = themeIndex,
                    onThemeChange = { index -> themeIndex = index.coerceIn(BloomThemes.indices); BloomAccent.value = BloomThemes[themeIndex]; preferences.edit().putInt("theme_index", themeIndex).apply() },
                    compactTiles = compactTiles,
                    onCompactTilesChange = {
                        compactTiles = it
                        preferences.edit().putBoolean("compact_tiles", it).apply()
                    },
                    onUpdatesClick = { showUpdates = true },
                    onProfileClick = { showSettings = false; showProfiles = true },
                    onSystemInfoClick = { showSystemInfo = true },
                    onBack = { showSettings = false }
                )
                }
            }
        }
    }

    availableUpdate?.let { update ->
        AlertDialog(
            onDismissRequest = { if (!updateBusy && !updateCheckInProgress) availableUpdate = null },
            title = { Text("A Bloom update is ready") },
            text = {
                Column {
                    Text("Bloom ${update.versionName} is ready. Download the APK and approve installation through Android.")
                    if (update.notes.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text(update.notes.take(700), style = MaterialTheme.typography.bodySmall)
                    }
                    if (updateMessage.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text(updateMessage, style = MaterialTheme.typography.bodySmall, color = BloomAccent.value)
                    }
                }
            },
            confirmButton = {
                TextButton(enabled = !updateBusy && !updateCheckInProgress, onClick = {
                    updateScope.launch {
                        updateBusy = true
                        runCatching { downloadAndInstallBloomUpdate(context, update) { updateMessage = it } }
                            .onFailure { updateMessage = it.message ?: "Couldn't download the update." }
                        updateBusy = false
                    }
                }) { Text(if (updateBusy) "Downloading…" else "Update Bloom") }
            },
            dismissButton = {
                TextButton(enabled = !updateBusy, onClick = { availableUpdate = null }) { Text("Later") }
            },
            containerColor = Color(0xFF171A24),
            titleContentColor = Color.White,
            textContentColor = Ink
        )
    }
}

@Composable
private fun SettingsScreen(
    animationsEnabled: Boolean,
    onAnimationsChange: (Boolean) -> Unit,
    reduceMotion: Boolean,
    onReduceMotionChange: (Boolean) -> Unit,
    showSystemCards: Boolean,
    onShowSystemCardsChange: (Boolean) -> Unit,
    showWelcomePanel: Boolean,
    onShowWelcomePanelChange: (Boolean) -> Unit,
    compactTiles: Boolean,
    onCompactTilesChange: (Boolean) -> Unit,
    themeIndex: Int,
    onThemeChange: (Int) -> Unit,
    onUpdatesClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSystemInfoClick: () -> Unit,
    onBack: () -> Unit
) {
    val initialFocusRequester = remember { FocusRequester() }
    RequestFocusOnEntry(initialFocusRequester)
    Column(Modifier.fillMaxSize().padding(horizontal = 54.dp, vertical = 28.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BloomBackButton(onClick = onBack, focusRequester = initialFocusRequester)
            Spacer(Modifier.width(20.dp))
            Column {
                Text("Bloom Settings", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Light, letterSpacing = (-1).sp)
                Text("Your space, your rules.", color = Ink.copy(alpha = 0.65f), fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(1.15f)) {
                Text("SYSTEM", color = BloomAccent.value, fontSize = 10.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                SettingsRow("Your Profile", "Personalise your Bloom experience", "↗", onProfileClick)
                Spacer(Modifier.height(12.dp))
                SettingsRow("Software Update", "New features and Bloom releases", "↗", onUpdatesClick)
                Spacer(Modifier.height(12.dp))
                SettingsRow("About Bloom", "Version, device and system information", "↗", onSystemInfoClick)
            }
            Column(Modifier.weight(1f)) {
                Text("EXPERIENCE", color = BloomAccent.value, fontSize = 10.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                SettingsSwitchRow("Bloom Motion", "Expressive transitions and focus effects.", animationsEnabled, onAnimationsChange)
                Spacer(Modifier.height(12.dp))
                SettingsSwitchRow("Reduce Motion", "Tone down movement and visual effects.", reduceMotion, onReduceMotionChange)
                Spacer(Modifier.height(12.dp))
                SettingsSwitchRow("System overview", "Show device and profile cards on Home.", showSystemCards, onShowSystemCardsChange)
                Spacer(Modifier.height(12.dp))
                SettingsSwitchRow("Welcome widget", "Show or hide the featured Bloom panel.", showWelcomePanel, onShowWelcomePanelChange)
                Spacer(Modifier.height(12.dp))
                SettingsSwitchRow("Compact app tiles", "Fit more apps into the home carousel.", compactTiles, onCompactTilesChange)
                Spacer(Modifier.height(12.dp))
                ThemePickerRow(selected = themeIndex, onSelect = onThemeChange)
            }
        }
        Spacer(Modifier.weight(1f))
        Text("BLOOM ATMOSPHERE  •  TV OS LITE", color = Ink.copy(alpha = 0.38f), fontSize = 10.sp, letterSpacing = 2.sp)
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
        border = BorderStroke(if (focused) 2.dp else 1.dp, if (focused) BloomAccent.value else Color.White.copy(alpha = 0.12f))
    ) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 17.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, color = Ink.copy(alpha = 0.62f), fontSize = 12.sp)
            }
            Text(trailing, color = BloomAccent.value, fontSize = 28.sp)
        }
    }
}

@Composable
private fun BloomActionSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Glass,
    content: @Composable () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val background by animateColorAsState(
        targetValue = if (focused) Color(0xFF3A4A6D) else color,
        animationSpec = tween(140),
        label = "bloom-action-focus"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.onFocusChanged { focused = it.isFocused },
        color = background,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            if (focused) 2.dp else 1.dp,
            if (focused) BloomAccent.value else Color.White.copy(alpha = 0.12f)
        ),
        content = content
    )
}

@Composable
private fun BloomBackButton(
    onClick: () -> Unit,
    focusRequester: FocusRequester
) {
    var focused by remember { mutableStateOf(false) }
    Surface(
        onClick = onClick,
        modifier = Modifier
            .focusRequester(focusRequester)
            .onFocusChanged { focused = it.isFocused },
        color = if (focused) Color(0xFF3A4A6D) else Glass,
        shape = CircleShape,
        border = BorderStroke(
            if (focused) 2.dp else 1.dp,
            if (focused) BloomAccent.value else Color.White.copy(alpha = 0.15f)
        )
    ) {
        Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.ArrowBack, "Back", tint = Ink)
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
private fun UpdateScreen(
    update: BloomUpdate?,
    message: String,
    busy: Boolean,
    checking: Boolean,
    downloading: Boolean,
    hasChecked: Boolean,
    onCheck: () -> Unit,
    onInstall: (BloomUpdate) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val initialFocusRequester = remember { FocusRequester() }
    RequestFocusOnEntry(initialFocusRequester)
    val packageInfo = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0) }.getOrNull()
    }
    val installedVersion = packageInfo?.versionName ?: "Preview"
    Column(Modifier.fillMaxSize().padding(horizontal = 54.dp, vertical = 30.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BloomBackButton(onClick = onBack, focusRequester = initialFocusRequester)
            Spacer(Modifier.width(18.dp))
            Column {
                Text("Software Update", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Light)
                Text("Keep Bloom up to date.", color = Ink.copy(alpha = 0.62f), fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(28.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF171D2A),
            shape = RoundedCornerShape(26.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(Modifier.padding(26.dp)) {
                Text("BLOOM SOFTWARE", color = BloomAccent.value, fontSize = 10.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text("Bloom ${update?.versionName ?: installedVersion}", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Light)
                Spacer(Modifier.height(6.dp))
                Text(
                    when {
                        update != null -> "A new version is available."
                        checking -> "Checking for updates…"
                        message.startsWith("Couldn't check") -> "Update status couldn't be verified."
                        hasChecked -> "You're up to date."
                        else -> "Checking for updates…"
                    },
                    color = Ink.copy(alpha = 0.68f),
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(20.dp))
                if (update != null) {
                    Button(onClick = { onInstall(update) }, enabled = !busy,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF111522))) {
                        Text(when {
                            downloading -> "Preparing update…"
                            checking -> "Checking…"
                            else -> "Update now"
                        })
                    }
                    TextButton(onClick = {
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(update.releaseUrl)))
                        }.onFailure {
                            Toast.makeText(context, "No browser is available to open the release page.", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("View release notes")
                    }
                    if (update.notes.isNotBlank()) {
                        Spacer(Modifier.height(18.dp))
                        Text("WHAT'S NEW", color = BloomAccent.value, fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(update.notes.take(1000), color = Ink.copy(alpha = 0.76f), fontSize = 13.sp)
                    }
                } else {
                    Text("You're running the latest version currently known to Bloom.", color = Ink.copy(alpha = 0.76f), fontSize = 14.sp)
                    Spacer(Modifier.height(18.dp))
                    Button(onClick = onCheck, enabled = !busy,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF111522))) {
                        Text(if (busy) "Checking…" else "Check for updates")
                    }
                }
                if (message.isNotBlank()) {
                    Spacer(Modifier.height(14.dp))
                    Text(message, color = BloomAccent.value, fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Bloom checks for new versions automatically. When one is ready, choose Update now. Your device will ask you to approve the installation.", color = Ink.copy(alpha = 0.52f), fontSize = 12.sp)
        Spacer(Modifier.weight(1f))
        Text("BLOOM TV OS LITE  •  SOFTWARE", color = Ink.copy(alpha = 0.35f), fontSize = 10.sp, letterSpacing = 1.sp)
    }
}

private data class StoreApp(
    val name: String,
    val category: String,
    val packageName: String,
    val description: String,
    val badge: String
)

private val bloomStoreApps = listOf(
    StoreApp("YouTube", "Video", "com.google.android.youtube.tv", "Watch videos, creators, music and live streams.", "POPULAR"),
    StoreApp("Netflix", "Video", "com.netflix.mediaclient", "Explore films, series and award-winning originals.", "ENTERTAINMENT"),
    StoreApp("Prime Video", "Video", "com.amazon.amazonvideo.livingroom", "Films, series and entertainment from Prime Video.", "ENTERTAINMENT"),
    StoreApp("Disney+", "Video", "com.disney.disneyplus", "Stories from Disney, Pixar, Marvel, Star Wars and more.", "FAMILY"),
    StoreApp("Spotify", "Music", "com.spotify.tv.android", "Bring music, podcasts and playlists to your screen.", "MUSIC"),
    StoreApp("Plex", "Media", "com.plexapp.android", "Organise and enjoy your personal media library.", "MEDIA"),
    StoreApp("ChatGPT", "Productivity", "com.openai.chatgpt", "Ask questions, explore ideas and get help with everyday tasks.", "BLOOM PICK")
)

@Composable
private fun BloomStoreScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val initialFocusRequester = remember { FocusRequester() }
    RequestFocusOnEntry(initialFocusRequester)
    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedApp by remember { mutableStateOf<StoreApp?>(null) }
    val categories = listOf("All", "Video", "Music", "Media", "Productivity")
    val visibleApps = remember(search, selectedCategory) {
        bloomStoreApps.filter { app ->
            (selectedCategory == "All" || app.category == selectedCategory) &&
                (app.name.contains(search, ignoreCase = true) ||
                    app.category.contains(search, ignoreCase = true) ||
                    app.description.contains(search, ignoreCase = true))
        }
    }

    fun openStoreListing(app: StoreApp) {
        val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${app.packageName}"))
        val web = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/details?id=${app.packageName}"))
        runCatching {
            if (market.resolveActivity(context.packageManager) != null) context.startActivity(market)
            else context.startActivity(web)
        }.onFailure {
            runCatching {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=${Uri.encode(app.name)}&c=apps")))
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BloomBackButton(onClick = onBack, focusRequester = initialFocusRequester)
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Text("Bloom Store", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Light, letterSpacing = (-1).sp)
                Text("A hand-picked collection for your screen.", color = Ink.copy(alpha = 0.62f), fontSize = 13.sp)
            }
            Text("BLOOM MARKETPLACE", color = BloomAccent.value.copy(alpha = 0.8f), fontSize = 9.sp, letterSpacing = 2.sp)
        }
        Spacer(Modifier.height(17.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text("Search apps and categories") },
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Glass, unfocusedContainerColor = Glass,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedPlaceholderColor = Ink.copy(alpha = 0.5f), unfocusedPlaceholderColor = Ink.copy(alpha = 0.5f),
                    focusedIndicatorColor = BloomAccent.value, unfocusedIndicatorColor = Color.Transparent, cursorColor = BloomAccent.value
                )
            )
            Text("${bloomStoreApps.size} CURATED APPS", color = Ink.copy(alpha = 0.48f), fontSize = 9.sp, letterSpacing = 1.4.sp)
        }
        Spacer(Modifier.height(14.dp))
        Surface(
            modifier = Modifier.fillMaxWidth().height(112.dp),
            color = Color(0x553E456D),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.13f))
        ) {
            Row(Modifier.fillMaxSize().background(
                Brush.horizontalGradient(listOf(Color(0x665F4B83), Color(0x332B4966), Color(0x224B6680)))
            ).padding(horizontal = 24.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("THE BLOOM EDIT", color = BloomAccent.value, fontSize = 9.sp, letterSpacing = 2.5.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(5.dp))
                    Text("Find your next favourite.", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Light)
                    Text("Discover apps selected for the Bloom experience.", color = Ink.copy(alpha = 0.7f), fontSize = 11.sp)
                }
                Surface(onClick = { selectedApp = bloomStoreApps.first { it.name == "ChatGPT" } },
                    color = Color(0xFFB8C7FF), shape = RoundedCornerShape(13.dp)) {
                    Text("Editor's pick  ›", color = Color(0xFF111526), fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 17.dp, vertical = 12.dp))
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEach { category ->
                val active = selectedCategory == category
                Surface(onClick = { selectedCategory = category },
                    color = if (active) Color(0xFFB8C7FF) else Glass,
                    shape = RoundedCornerShape(30.dp),
                    border = BorderStroke(1.dp, if (active) BloomAccent.value else Color.White.copy(alpha = 0.1f))) {
                    Text(category, color = if (active) Color(0xFF111526) else Ink,
                        fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 9.dp))
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(if (selectedCategory == "All") "EXPLORE THE STORE" else selectedCategory.uppercase(),
                color = BloomAccent.value, fontSize = 10.sp, letterSpacing = 2.5.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(10.dp))
            Text("${visibleApps.size} results", color = Ink.copy(alpha = 0.42f), fontSize = 10.sp)
        }
        Spacer(Modifier.height(8.dp))
        if (visibleApps.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("No apps match that search. Try another name or category.", color = Ink.copy(alpha = 0.65f), fontSize = 13.sp)
            }
        } else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(visibleApps, key = { it.packageName }) { app ->
                    BloomActionSurface(onClick = { selectedApp = app }, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(46.dp).background(
                                Brush.linearGradient(listOf(Color(0x665E6795), Color(0x443E345C))),
                                RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                                Text(app.name.take(1), color = BloomAccent.value, fontSize = 23.sp, fontWeight = FontWeight.Light)
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(app.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.width(9.dp))
                                    Text(app.badge, color = BloomAccent.value.copy(alpha = 0.8f), fontSize = 8.sp, letterSpacing = 1.sp)
                                }
                                Spacer(Modifier.height(3.dp))
                                Text(app.description, color = Ink.copy(alpha = 0.56f), fontSize = 10.sp, maxLines = 1)
                            }
                            Text("Details  ›", color = BloomAccent.value, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("Apps are listed by Bloom and installed through their official Google Play pages. Availability and device compatibility are controlled by each publisher and Google Play.",
            color = Ink.copy(alpha = 0.43f), fontSize = 10.sp)
    }

    selectedApp?.let { app ->
        AlertDialog(
            onDismissRequest = { selectedApp = null },
            title = { Text(app.name, fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    Text(app.category.uppercase() + "  •  " + app.badge, color = Color(0xFF53658F), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Text(app.description)
                    Spacer(Modifier.height(10.dp))
                    Text("Bloom will open the official store listing. Your device handles compatibility checks and installation approval.",
                        style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedApp = null; openStoreListing(app) }) { Text("Get app") }
            },
            dismissButton = {
                TextButton(onClick = { selectedApp = null }) { Text("Close") }
            },
            containerColor = Color(0xFF171A24),
            titleContentColor = Color.White,
            textContentColor = Ink
        )
    }
}

@Composable
private fun AppLibraryScreen(
    apps: List<TvApp>,
    onRefresh: () -> Unit,
    onLaunch: (TvApp) -> Unit,
    onBack: () -> Unit
) {
    val initialFocusRequester = remember { FocusRequester() }
    RequestFocusOnEntry(initialFocusRequester)
    var search by remember { mutableStateOf("") }
    val filteredApps = remember(apps, search) {
        apps.filter { it.title.contains(search, ignoreCase = true) }
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 54.dp, vertical = 30.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BloomBackButton(onClick = onBack, focusRequester = initialFocusRequester)
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Text("App library", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Light)
                Text("${apps.size} installed apps", color = Ink.copy(alpha = 0.62f), fontSize = 13.sp)
            }
            TextButton(onClick = onRefresh) { Text("Refresh", color = BloomAccent.value) }
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
                focusedIndicatorColor = BloomAccent.value,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = BloomAccent.value
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
                items(filteredApps, key = { it.packageName }) { app ->
                    BloomActionSurface(
                        onClick = { onLaunch(app) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(app.title.take(1).uppercase(), color = BloomAccent.value, fontSize = 23.sp, modifier = Modifier.width(42.dp))
                            Column(Modifier.weight(1f)) {
                                Text(app.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text(app.packageName, color = Ink.copy(alpha = 0.48f), fontSize = 10.sp)
                            }
                            Text("Open  ›", color = BloomAccent.value, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppTile(
    app: TvApp,
    animationsEnabled: Boolean,
    compact: Boolean,
    focusRequester: FocusRequester? = null,
    launch: () -> Unit
) {
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
    val targetScale = if (focused && animationsEnabled) 1.085f else 1f
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(if (animationsEnabled) 360 else 0, easing = FastOutSlowInEasing),
        label = "app-tile-scale"
    )
    val tileColor by animateColorAsState(
        targetValue = if (focused) Color(0xFF44364E) else Glass,
        animationSpec = tween(if (animationsEnabled) 320 else 0),
        label = "app-tile-color"
    )
    Surface(
        onClick = launch,
        modifier = Modifier
            .width(if (compact) 174.dp else 210.dp)
            .height(if (compact) 116.dp else 146.dp)
            .scale(scale)
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .onFocusChanged { focused = it.isFocused },
        color = tileColor,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(if (focused) 2.dp else 1.dp, if (focused) BloomAccent.value else Color.White.copy(alpha = 0.12f))
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.SpaceBetween) {
            if (appIcon != null) {
                Image(
                    bitmap = appIcon.asImageBitmap(),
                    contentDescription = "${app.title} app icon",
                    modifier = Modifier.size(if (compact) 38.dp else 48.dp)
                )
            } else {
                Box(
                    Modifier.size(48.dp).background(Color(0x334F405F), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(app.title.take(1).uppercase(), color = BloomAccent.value, fontSize = 25.sp, fontWeight = FontWeight.Light)
                }
            }
            Column {
                Text(app.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(app.detail, color = Ink.copy(alpha = 0.55f), fontSize = 11.sp)
            }
        }
    }
}


@Composable
private fun BloomSignUpScreen(
    initialName: String,
    onComplete: (String) -> Unit,
    onSkip: () -> Unit
) {
    val nameFocusRequester = remember { FocusRequester() }
    RequestFocusOnEntry(nameFocusRequester)
    var name by remember(initialName) { mutableStateOf(initialName) }
    var focused by remember { mutableStateOf(false) }
    val cleanName = name.trim()
    val initial = cleanName.firstOrNull()?.uppercaseChar()?.toString() ?: "B"
    val validName = cleanName.length in 2..32

    Box(Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 20.dp), contentAlignment = Alignment.Center) {
        Row(
            Modifier.fillMaxWidth().widthIn(max = 1040.dp),
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text("B L O O M   /   YOUR SPACE", color = BloomAccent.value, fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(18.dp))
                Text(
                    if (initialName.isBlank()) "A screen that\nfeels like yours." else "Make Bloom\nyour own.",
                    color = Color.White, fontSize = 43.sp, lineHeight = 49.sp, fontWeight = FontWeight.Light
                )
                Spacer(Modifier.height(13.dp))
                Text(
                    "Create your local profile to personalise the Bloom experience. It only takes a moment.",
                    color = Ink.copy(alpha = 0.66f), fontSize = 14.sp, lineHeight = 21.sp
                )
                Spacer(Modifier.height(28.dp))
                listOf(
                    Triple("01", "A personal welcome", "See your name on the Bloom home screen."),
                    Triple("02", "Your own settings", "Keep your layout and viewing preferences on this device."),
                    Triple("03", "Your app space", "Make the launcher feel like your own.")
                ).forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 17.dp)) {
                        Box(Modifier.size(34.dp).background(Color(0x334F638D), RoundedCornerShape(11.dp)), contentAlignment = Alignment.Center) {
                            Text(item.first, color = BloomAccent.value, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(item.second, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(item.third, color = Ink.copy(alpha = 0.52f), fontSize = 10.sp)
                        }
                    }
                }
                Spacer(Modifier.height(5.dp))
                Text("BLOOM ATMOSPHERE  •  TV OS LITE", color = Ink.copy(alpha = 0.3f), fontSize = 9.sp, letterSpacing = 1.7.sp)
            }

            Surface(
                modifier = Modifier.weight(1f).widthIn(max = 470.dp),
                color = Color(0xDD111827),
                shape = RoundedCornerShape(30.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.17f)),
                shadowElevation = 22.dp
            ) {
                Column(
                    Modifier.padding(horizontal = 30.dp, vertical = 27.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(46.dp).background(
                                Brush.linearGradient(listOf(Color(0xFF9CB8FF), Color(0xFF8B70B7))),
                                RoundedCornerShape(15.dp)
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("b", color = Color.White, fontSize = 31.sp, fontWeight = FontWeight.Light)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("BLOOM PROFILE", color = BloomAccent.value, fontSize = 9.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                            Text("Your setup", color = Ink.copy(alpha = 0.6f), fontSize = 11.sp)
                        }
                    }
                    Spacer(Modifier.height(23.dp))
                    Text(
                        if (initialName.isBlank()) "Welcome to Bloom" else "Edit your profile",
                        color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Light
                    )
                    Spacer(Modifier.height(6.dp))
                    Text("First, let's give your space a name.", color = Ink.copy(alpha = 0.6f), fontSize = 12.sp)
                    Spacer(Modifier.height(20.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0x252F4167),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.09f))
                    ) {
                        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(48.dp).background(
                                    Brush.linearGradient(listOf(Color(0xFFB8C7FF), Color(0xFF8A78B8))),
                                    CircleShape
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(initial, color = Color(0xFF101526), fontSize = 21.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(13.dp))
                            Column(Modifier.weight(1f)) {
                                Text(cleanName.ifBlank { "Your profile" }, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text("BLOOM MEMBER  •  THIS DEVICE", color = Ink.copy(alpha = 0.48f), fontSize = 8.sp, letterSpacing = 1.sp)
                            }
                            Text("PREVIEW", color = BloomAccent.value.copy(alpha = 0.8f), fontSize = 8.sp, letterSpacing = 1.sp)
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it.filter { ch -> !ch.isISOControl() }.take(32) },
                        modifier = Modifier.fillMaxWidth().focusRequester(nameFocusRequester),
                        singleLine = true,
                        label = { Text("Display name") },
                        placeholder = { Text("What should Bloom call you?") },
                        supportingText = {
                            Text(
                                when {
                                    cleanName.isBlank() -> "Use at least 2 characters."
                                    !validName -> "Your name needs at least 2 characters."
                                    else -> "${cleanName.length}/32 characters"
                                }
                            )
                        },
                        shape = RoundedCornerShape(15.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = BloomAccent.value, unfocusedBorderColor = Color.White.copy(alpha = 0.22f),
                            focusedLabelColor = BloomAccent.value, unfocusedLabelColor = Ink.copy(alpha = 0.58f),
                            cursorColor = BloomAccent.value, focusedSupportingTextColor = Ink.copy(alpha = 0.55f),
                            unfocusedSupportingTextColor = Ink.copy(alpha = 0.45f)
                        )
                    )
                    Spacer(Modifier.height(13.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Text("●", color = Color(0xFF8FD5B1), fontSize = 10.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Private by design. This profile is saved locally on this device. Bloom does not ask for an email address or password.",
                            color = Ink.copy(alpha = 0.56f), fontSize = 10.sp, lineHeight = 15.sp
                        )
                    }
                    Spacer(Modifier.height(19.dp))
                    Button(
                        onClick = { onComplete(cleanName) },
                        enabled = validName,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB8C7FF),
                            contentColor = Color(0xFF101526),
                            disabledContainerColor = Color(0xFF56617D),
                            disabledContentColor = Color(0xFFB8BDCA)
                        )
                    ) {
                        Text("Create my profile  →", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
                        Text("I'll do this later", color = Ink.copy(alpha = 0.65f), fontSize = 12.sp)
                    }
                    Text(
                        "By continuing, you create a profile stored only within this Bloom installation.",
                        color = Ink.copy(alpha = 0.34f), fontSize = 9.sp, lineHeight = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SystemMetricCard(title: String, value: String, detail: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(66.dp),
        color = Color(0x241FFFFFFF),
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.09f))
    ) {
        Column(Modifier.padding(horizontal = 13.dp, vertical = 9.dp), verticalArrangement = Arrangement.Center) {
            Text(title, color = BloomAccent.value.copy(alpha = 0.86f), fontSize = 8.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(3.dp))
            Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(detail, color = Ink.copy(alpha = 0.48f), fontSize = 9.sp, maxLines = 1)
        }
    }
}


@Composable
private fun BloomControlCentre(
    animationsEnabled: Boolean, onAnimations: (Boolean) -> Unit,
    reduceMotion: Boolean, onReduceMotion: (Boolean) -> Unit,
    showCards: Boolean, onShowCards: (Boolean) -> Unit,
    compactTiles: Boolean, onCompactTiles: (Boolean) -> Unit,
    onClose: () -> Unit, onSettings: () -> Unit
) {
    val context = LocalContext.current
    Box(Modifier.fillMaxSize().background(Color(0xCC030611)).padding(30.dp), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.fillMaxWidth().widthIn(max = 900.dp),
            color = Color(0xF0182032), shape = RoundedCornerShape(30.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)), shadowElevation = 28.dp
        ) {
            Column(Modifier.padding(28.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("BLOOM CONTROL CENTRE", color = BloomAccent.value, fontSize = 10.sp, letterSpacing = 2.5.sp, fontWeight = FontWeight.Bold)
                        Text("Your quick settings", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Light)
                    }
                    TextButton(onClick = onClose) { Text("Done  ✕", color = BloomAccent.value) }
                }
                Spacer(Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickToggleCard("Bloom Motion", "Expressive transitions", animationsEnabled, onAnimations, Modifier.weight(1f))
                    QuickToggleCard("Reduce Motion", "Less movement", reduceMotion, onReduceMotion, Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickToggleCard("Home overview", "Show dashboard cards", showCards, onShowCards, Modifier.weight(1f))
                    QuickToggleCard("Compact tiles", "Fit more apps on screen", compactTiles, onCompactTiles, Modifier.weight(1f))
                }
                Spacer(Modifier.height(20.dp))
                Text("DEVICE SETTINGS", color = BloomAccent.value, fontSize = 9.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(9.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SystemSettingButton("Wi-Fi", Modifier.weight(1f)) { context.startActivity(Intent(android.provider.Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                    SystemSettingButton("Bluetooth", Modifier.weight(1f)) { context.startActivity(Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                    SystemSettingButton("Display", Modifier.weight(1f)) { context.startActivity(Intent(android.provider.Settings.ACTION_DISPLAY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                    SystemSettingButton("Sound", Modifier.weight(1f)) { context.startActivity(Intent(android.provider.Settings.ACTION_SOUND_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                    SystemSettingButton("Accessibility", Modifier.weight(1f)) { context.startActivity(Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                }
                Spacer(Modifier.height(8.dp))
                Text("These open Android's own settings pages. Android keeps control of protected device features.", color = Ink.copy(alpha = 0.5f), fontSize = 10.sp, lineHeight = 14.sp)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onSettings, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB8C7FF), contentColor = Color(0xFF101526))) { Text("Open all Bloom settings  →") }
            }
        }
    }
}

@Composable
private fun QuickToggleCard(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = Glass, shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = Ink.copy(alpha = 0.52f), fontSize = 10.sp)
            }
            Switch(checked = checked, onCheckedChange = onChange)
        }
    }
}

@Composable
private fun BloomSearchScreen(apps: List<TvApp>, onBack: () -> Unit, onLaunch: (TvApp) -> Unit, onSettings: () -> Unit) {
    val context = LocalContext.current
    val initialFocusRequester = remember { FocusRequester() }
    RequestFocusOnEntry(initialFocusRequester)
    var query by remember { mutableStateOf("") }
    val matches = remember(apps, query) { apps.filter { it.title.contains(query, ignoreCase = true) || it.packageName.contains(query, ignoreCase = true) } }
    val storeMatches = remember(query) { bloomStoreApps.filter { query.isBlank() || it.name.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true) } }
    Column(Modifier.fillMaxSize().background(Color(0xF8070B16)).padding(horizontal = 54.dp, vertical = 30.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BloomBackButton(onClick = onBack, focusRequester = initialFocusRequester)
            Spacer(Modifier.width(14.dp))
            Text("Bloom Search", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Light)
        }
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(value = query, onValueChange = { query = it.take(60) }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Search Bloom") }, placeholder = { Text("Installed apps, Store apps or categories") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = BloomAccent.value, unfocusedBorderColor = Color.White.copy(alpha = 0.2f), focusedLabelColor = BloomAccent.value, unfocusedLabelColor = Ink.copy(alpha = 0.6f), cursorColor = BloomAccent.value))
        Spacer(Modifier.height(10.dp))
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            item { Text("INSTALLED APPS  •  ${matches.size}", color = BloomAccent.value, fontSize = 9.sp, letterSpacing = 2.sp) }
            if (matches.isEmpty()) item { Text("No installed apps match.", color = Ink.copy(alpha = 0.58f), fontSize = 11.sp) }
            items(matches, key = { it.packageName }) { app ->
                BloomActionSurface(onClick = { onLaunch(app) }, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(app.title.take(1).uppercase(), color = BloomAccent.value, fontSize = 21.sp, modifier = Modifier.width(38.dp))
                        Column(Modifier.weight(1f)) { Text(app.title, color = Color.White, fontWeight = FontWeight.SemiBold); Text(app.packageName, color = Ink.copy(alpha = 0.45f), fontSize = 9.sp) }
                        Text("Open  ›", color = BloomAccent.value, fontSize = 11.sp)
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)); Text("BLOOM STORE  •  ${storeMatches.size}", color = BloomAccent.value, fontSize = 9.sp, letterSpacing = 2.sp) }
            items(storeMatches, key = { it.packageName }) { app ->
                BloomActionSurface(onClick = {
                    val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${app.packageName}"))
                    val web = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/details?id=${app.packageName}"))
                    runCatching { if (market.resolveActivity(context.packageManager) != null) context.startActivity(market) else context.startActivity(web) }
                }, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(app.name.take(1).uppercase(), color = BloomAccent.value, fontSize = 21.sp, modifier = Modifier.width(38.dp))
                        Column(Modifier.weight(1f)) { Text(app.name, color = Color.White, fontWeight = FontWeight.SemiBold); Text(app.category + "  •  " + app.badge, color = Ink.copy(alpha = 0.45f), fontSize = 9.sp) }
                        Text("Get app  ↗", color = BloomAccent.value, fontSize = 11.sp)
                    }
                }
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text("DEVICE SETTINGS", color = BloomAccent.value, fontSize = 9.sp, letterSpacing = 2.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    SystemSettingButton("Wi-Fi", Modifier.weight(1f)) { context.startActivity(Intent(android.provider.Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                    SystemSettingButton("Bluetooth", Modifier.weight(1f)) { context.startActivity(Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                    SystemSettingButton("Display", Modifier.weight(1f)) { context.startActivity(Intent(android.provider.Settings.ACTION_DISPLAY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                    SystemSettingButton("Sound", Modifier.weight(1f)) { context.startActivity(Intent(android.provider.Settings.ACTION_SOUND_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                }
            }
        }
        TextButton(onClick = onSettings) { Text("Open all Bloom settings  →", color = BloomAccent.value) }
    }
}

@Composable
private fun BloomAmbientScreen(onExit: () -> Unit) {
    val motion = rememberInfiniteTransition(label = "ambient")
    val drift by motion.animateFloat(-55f, 55f, infiniteRepeatable(tween(11000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "ambient-drift")
    val pulse by motion.animateFloat(0.5f, 0.95f, infiniteRepeatable(tween(5000), RepeatMode.Reverse), label = "ambient-pulse")
    var clock by remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) }
    LaunchedEffect(Unit) { while (true) { clock = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()); delay(15000) } }
    Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF101B36), Midnight, Color(0xFF30213C))))) {
        Box(Modifier.offset(x = drift.dp, y = (-80).dp).size(520.dp).background(Brush.radialGradient(listOf(Color(0x665C80D8).copy(alpha = pulse), Color.Transparent)), CircleShape))
        Box(Modifier.align(Alignment.BottomEnd).offset(x = 100.dp, y = 100.dp).size(480.dp).background(Brush.radialGradient(listOf(Color(0x557B527D), Color.Transparent)), CircleShape))
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("bloom", color = Color.White.copy(alpha = 0.8f), fontSize = 20.sp, letterSpacing = 8.sp, fontWeight = FontWeight.Light)
            Spacer(Modifier.height(12.dp))
            Text(clock, color = Color.White, fontSize = 104.sp, fontWeight = FontWeight.ExtraLight, letterSpacing = (-4).sp)
            Text(SimpleDateFormat("EEEE  •  d MMMM", Locale.getDefault()).format(Date()).uppercase(), color = BloomAccent.value.copy(alpha = 0.78f), fontSize = 11.sp, letterSpacing = 3.sp)
        }
        TextButton(onClick = onExit, modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)) { Text("Exit ambient  ✕", color = Ink.copy(alpha = 0.75f)) }
    }
}


@Composable
private fun ThemePickerRow(selected: Int, onSelect: (Int) -> Unit) {
    Surface(Modifier.fillMaxWidth(), color = Glass, shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))) {
        Column(Modifier.padding(15.dp)) {
            Text("Bloom Themes", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text("Choose an accent colour", color = Ink.copy(alpha = 0.55f), fontSize = 10.sp)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                BloomThemes.forEachIndexed { index, color ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(onClick = { onSelect(index) }, modifier = Modifier.size(30.dp), shape = CircleShape, color = color, border = BorderStroke(if (selected == index) 3.dp else 1.dp, if (selected == index) Color.White else Color.White.copy(alpha = 0.3f))) {}
                        Text(BloomThemeNames[index], color = Ink.copy(alpha = 0.55f), fontSize = 8.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun BloomProfilesScreen(currentName: String, onSelect: (String) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("bloom_settings", android.content.Context.MODE_PRIVATE) }
    val initialFocusRequester = remember { FocusRequester() }
    RequestFocusOnEntry(initialFocusRequester)
    val profileSet = remember { mutableStateListOf<String>().also { list ->
        list.addAll((prefs.getStringSet("profile_names", emptySet()) ?: emptySet()).toList())
        if (currentName.isNotBlank() && !list.contains(currentName)) list.add(currentName)
    } }
    var newName by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().background(Color(0xF8070B16)).padding(horizontal = 54.dp, vertical = 30.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BloomBackButton(onClick = onBack, focusRequester = initialFocusRequester)
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Who's watching?", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Light)
                Text("Choose a local Bloom profile.", color = Ink.copy(alpha = 0.6f), fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(22.dp))
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(profileSet.distinct(), key = { it }) { profile ->
                BloomActionSurface(
                    onClick = { onSelect(profile) },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (profile == currentName) Color(0x443E5278) else Glass
                ) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).background(BloomAccent.value, CircleShape), contentAlignment = Alignment.Center) { Text(profile.take(1).uppercase(), color = Midnight, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) { Text(profile, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold); Text(if (profile == currentName) "CURRENT PROFILE" else "LOCAL PROFILE", color = Ink.copy(alpha = 0.5f), fontSize = 9.sp) }
                        Text("Select  ›", color = BloomAccent.value)
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = newName, onValueChange = { newName = it.filter { c -> !c.isISOControl() }.take(32) }, modifier = Modifier.weight(1f), singleLine = true, label = { Text("New profile name") })
            Button(enabled = newName.trim().length >= 2, onClick = {
                val name = newName.trim()
                if (!profileSet.contains(name)) profileSet.add(name)
                prefs.edit().putStringSet("profile_names", profileSet.toSet()).apply()
                onSelect(name)
            }, colors = ButtonDefaults.buttonColors(containerColor = BloomAccent.value, contentColor = Midnight)) { Text("Create") }
        }
        Text("Profiles are stored locally. They do not create separate Android accounts or lock apps.", color = Ink.copy(alpha = 0.42f), fontSize = 10.sp)
    }
}


@Composable
private fun SystemSettingButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier.height(46.dp), contentPadding = PaddingValues(horizontal = 5.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))) {
        Text(label, fontSize = 10.sp, maxLines = 1)
    }
}
