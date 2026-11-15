package dev.projectos.bloom

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Night = Color(0xFF080B12)
private val Panel = Color(0xFF151A25)
private val PanelRaised = Color(0xFF202638)
private val Muted = Color(0xFFAAB2C1)
private val Hairline = Color.White.copy(alpha = 0.10f)
private val Accent = Color(0xFFE8B7CF)
private const val PREFS = "bloom_preferences"
private const val VERSION_NAME = "0.2.0"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        setContent { MaterialTheme(colorScheme = darkColorScheme()) { BloomRoot() } }
    }
}

private data class TvApp(
    val title: String,
    val packageName: String,
    val activityName: String,
    val isFavourite: Boolean = false
)

private enum class BloomPage { HOME, APPS, SETTINGS, DISCOVER, UPDATES }

@Composable
private fun BloomRoot() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS, 0) }
    var page by remember { mutableStateOf(BloomPage.HOME) }
    var profileName by remember { mutableStateOf(prefs.getString("profile_name", "") ?: "") }
    var showWelcome by remember { mutableStateOf(!prefs.getBoolean("profile_setup_complete", false)) }
    var showAbout by remember { mutableStateOf(false) }
    var showQuickSettings by remember { mutableStateOf(false) }
    var selectedApp by remember { mutableStateOf<TvApp?>(null) }
    var search by remember { mutableStateOf("") }
    var onlyFavourites by remember { mutableStateOf(false) }
    var apps by remember { mutableStateOf(discoverApps(context.packageManager)) }
    var favourites by remember { mutableStateOf(prefs.getStringSet("favourites", emptySet())?.toSet() ?: emptySet()) }
    var animations by remember { mutableStateOf(prefs.getBoolean("animations", true)) }
    var reduceMotion by remember { mutableStateOf(prefs.getBoolean("reduce_motion", false)) }
    var compactTiles by remember { mutableStateOf(prefs.getBoolean("compact_tiles", false)) }
    var showClock by remember { mutableStateOf(prefs.getBoolean("show_clock", true)) }
    var accentIndex by remember { mutableIntStateOf(prefs.getInt("accent_index", 0).coerceIn(0, 3)) }
    var clock by remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) }
    val accents = listOf(Color(0xFFE8B7CF), Color(0xFF9FC7F2), Color(0xFFB6D9B4), Color(0xFFEBC68E))
    val activeAccent = accents[accentIndex]
    val motion = animations && !reduceMotion

    LaunchedEffect(showClock) {
        while (showClock) {
            clock = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            delay(30_000)
        }
    }

    fun saveBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }
    fun openApp(app: TvApp) {
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_LAUNCHER)
                    .setComponent(ComponentName(app.packageName, app.activityName))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
    fun toggleFavourite(app: TvApp) {
        favourites = if (app.packageName in favourites) favourites - app.packageName else favourites + app.packageName
        prefs.edit().putStringSet("favourites", favourites).apply()
    }
    fun back() {
        when {
            selectedApp != null -> selectedApp = null
            showAbout -> showAbout = false
            showQuickSettings -> showQuickSettings = false
            showWelcome -> showWelcome = false
            page != BloomPage.HOME -> page = BloomPage.HOME
            search.isNotEmpty() -> search = ""
        }
    }
    BackHandler(enabled = page != BloomPage.HOME || selectedApp != null || showAbout || showQuickSettings || showWelcome || search.isNotEmpty()) { back() }

    Box(Modifier.fillMaxSize().background(Night)) {
        Column(Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 25.dp)) {
            BloomTopBar(
                page = page,
                profileName = profileName,
                clock = clock,
                showClock = showClock,
                accent = activeAccent,
                onNavigate = { page = it; selectedApp = null },
                onQuickSettings = { showQuickSettings = true }
            )
            Spacer(Modifier.height(24.dp))
            when (page) {
                BloomPage.HOME -> HomePage(
                    apps = apps,
                    favourites = favourites,
                    profileName = profileName,
                    accent = activeAccent,
                    compactTiles = compactTiles,
                    onOpen = ::openApp,
                    onSelect = { selectedApp = it },
                    onToggleFavourite = ::toggleFavourite,
                    onRefresh = { apps = discoverApps(context.packageManager) },
                    onApps = { page = BloomPage.APPS },
                    onDiscover = { page = BloomPage.DISCOVER },
                    onQuickSettings = { showQuickSettings = true }
                )
                BloomPage.APPS -> AppsPage(
                    apps = apps,
                    favourites = favourites,
                    search = search,
                    onlyFavourites = onlyFavourites,
                    compactTiles = compactTiles,
                    accent = activeAccent,
                    onSearch = { search = it },
                    onFilter = { onlyFavourites = it },
                    onOpen = ::openApp,
                    onSelect = { selectedApp = it },
                    onToggleFavourite = ::toggleFavourite,
                    onRefresh = { apps = discoverApps(context.packageManager) }
                )
                BloomPage.SETTINGS -> SettingsPage(
                    profileName = profileName,
                    animations = animations,
                    reduceMotion = reduceMotion,
                    compactTiles = compactTiles,
                    showClock = showClock,
                    accentIndex = accentIndex,
                    onProfile = { showWelcome = true },
                    onAnimations = { animations = it; saveBoolean("animations", it) },
                    onReduceMotion = { reduceMotion = it; saveBoolean("reduce_motion", it) },
                    onCompactTiles = { compactTiles = it; saveBoolean("compact_tiles", it) },
                    onShowClock = { showClock = it; saveBoolean("show_clock", it) },
                    onAccent = { accentIndex = it; prefs.edit().putInt("accent_index", it).apply() },
                    onDeviceSettings = { runCatching { context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } },
                    onUpdates = { page = BloomPage.UPDATES },
                    onAbout = { showAbout = true },
                    accent = activeAccent
                )
                BloomPage.DISCOVER -> DiscoverPage(
                    apps = apps,
                    accent = activeAccent,
                    onOpenStore = { packageName ->
                        val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                        val web = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
                        runCatching { context.startActivity(market) }.onFailure { runCatching { context.startActivity(web) } }
                    },
                    onBack = { page = BloomPage.APPS }
                )
                BloomPage.UPDATES -> UpdatesPage(accent = activeAccent, onBack = { page = BloomPage.SETTINGS }, onOpenReleases = {
                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/projectosdev/bloom-tv-os/releases"))) }
                })
            }
            Spacer(Modifier.height(8.dp))
            BloomFooter(page = page, appCount = apps.size, accent = activeAccent)
        }

        AnimatedVisibility(visible = showQuickSettings, modifier = Modifier.fillMaxSize()) {
            QuickSettingsOverlay(
                accent = activeAccent,
                onClose = { showQuickSettings = false },
                onSystemSettings = {
                    showQuickSettings = false
                    runCatching { context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                },
                onWifi = {
                    showQuickSettings = false
                    runCatching { context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                },
                onDisplay = {
                    showQuickSettings = false
                    runCatching { context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                }
            )
        }

        if (showWelcome) {
            ProfileDialog(
                initialName = profileName,
                accent = activeAccent,
                onSave = { name ->
                    profileName = name.trim()
                    prefs.edit().putString("profile_name", profileName).putBoolean("profile_setup_complete", true).apply()
                    showWelcome = false
                },
                onSkip = {
                    prefs.edit().putBoolean("profile_setup_complete", true).apply()
                    showWelcome = false
                }
            )
        }

        if (showAbout) {
            InfoDialog(
                title = "About Bloom",
                message = "Bloom TV OS Lite $VERSION_NAME\nA TV-first Android launcher preview built with Kotlin and Jetpack Compose. Bloom does not replace Android firmware or bypass app restrictions.",
                accent = activeAccent,
                onClose = { showAbout = false }
            )
        }

        selectedApp?.let { app ->
            AppDetailsDialog(
                app = app,
                favourite = app.packageName in favourites,
                accent = activeAccent,
                onLaunch = { selectedApp = null; openApp(app) },
                onFavourite = { toggleFavourite(app) },
                onClose = { selectedApp = null },
                onAppInfo = {
                    selectedApp = null
                    runCatching { context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${app.packageName}"))) }
                }
            )
        }
    }
}

private fun discoverApps(pm: PackageManager): List<TvApp> {
    val intents = listOf(
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER),
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    )
    return intents.flatMap { intent -> runCatching { pm.queryIntentActivities(intent, 0) }.getOrDefault(emptyList()) }
        .mapNotNull { info ->
            val activity = info.activityInfo ?: return@mapNotNull null
            if (activity.packageName == "dev.projectos.bloom") return@mapNotNull null
            TvApp(
                title = runCatching { info.loadLabel(pm).toString() }.getOrDefault(activity.packageName),
                packageName = activity.packageName,
                activityName = activity.name
            )
        }
        .distinctBy { it.packageName }
        .sortedBy { it.title.lowercase(Locale.getDefault()) }
}

@Composable
private fun BloomTopBar(
    page: BloomPage,
    profileName: String,
    clock: String,
    showClock: Boolean,
    accent: Color,
    onNavigate: (BloomPage) -> Unit,
    onQuickSettings: () -> Unit
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("bloom", color = Color.White, fontSize = 29.sp, fontWeight = FontWeight.Light, letterSpacing = 1.sp)
            Text("TELEVISION, REIMAGINED", color = Muted.copy(alpha = .72f), fontSize = 9.sp, letterSpacing = 2.4.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
            PageNav("Home", page == BloomPage.HOME, accent, { onNavigate(BloomPage.HOME) })
            PageNav("Apps", page == BloomPage.APPS, accent, { onNavigate(BloomPage.APPS) })
            PageNav("Discover", page == BloomPage.DISCOVER, accent, { onNavigate(BloomPage.DISCOVER) })
            PageNav("Settings", page == BloomPage.SETTINGS || page == BloomPage.UPDATES, accent, { onNavigate(BloomPage.SETTINGS) })
        }
        Spacer(Modifier.width(17.dp))
        Surface(onClick = onQuickSettings, color = Panel, shape = CircleShape, border = BorderStroke(1.dp, Hairline)) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Tune, null, tint = accent, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(7.dp))
                Text("Quick settings", color = Muted, fontSize = 11.sp)
            }
        }
        if (showClock) {
            Spacer(Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(clock, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Light)
                Text(profileName.ifBlank { "Guest profile" }, color = Muted, fontSize = 9.sp)
            }
        }
    }
}

@Composable
private fun PageNav(label: String, selected: Boolean, accent: Color, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val bg by animateColorAsState(if (selected || focused) PanelRaised else Color.Transparent, animationSpec = tween(140), label = "nav")
    Surface(
        onClick = onClick,
        modifier = Modifier.onFocusChanged { focused = it.isFocused },
        color = bg,
        shape = RoundedCornerShape(24.dp),
        border = if (selected || focused) BorderStroke(1.dp, accent.copy(alpha = .45f)) else null
    ) {
        Text(label, Modifier.padding(horizontal = 14.dp, vertical = 10.dp), color = if (selected || focused) Color.White else Muted, fontSize = 11.sp)
    }
}

@Composable
private fun HomePage(
    apps: List<TvApp>,
    favourites: Set<String>,
    profileName: String,
    accent: Color,
    compactTiles: Boolean,
    onOpen: (TvApp) -> Unit,
    onSelect: (TvApp) -> Unit,
    onToggleFavourite: (TvApp) -> Unit,
    onRefresh: () -> Unit,
    onApps: () -> Unit,
    onDiscover: () -> Unit,
    onQuickSettings: () -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp), contentPadding = PaddingValues(bottom = 8.dp)) {
        item {
            HeroPanel(
                title = if (profileName.isBlank()) "Everything in its place." else "Welcome back, ${profileName}.",
                subtitle = "Your entertainment, gathered in one calmer space.",
                accent = accent,
                buttonText = if (apps.isEmpty()) "Discover apps" else "Explore your apps",
                onAction = if (apps.isEmpty()) onDiscover else onApps
            )
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                SectionHeading("YOUR FAVOURITES", accent)
                TextButton(onClick = onApps) { Text("All apps  ›", color = accent) }
            }
            val favouriteApps = apps.filter { it.packageName in favourites }
            if (favouriteApps.isEmpty()) {
                SmallHint("Your favourite apps will appear here. Open an app's details to pin it.")
            } else {
                AppShelf(favouriteApps, compactTiles, accent, onOpen, onSelect, onToggleFavourite)
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                SectionHeading("AVAILABLE ON THIS DEVICE", accent)
                TextButton(onClick = onRefresh) { Text("Refresh", color = Muted) }
            }
            if (apps.isEmpty()) EmptyApps(onDiscover) else AppShelf(apps.take(12), compactTiles, accent, onOpen, onSelect, onToggleFavourite)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionCard("Discover apps", "Find apps for your device", Icons.Rounded.Explore, accent, onDiscover, Modifier.weight(1f))
                QuickActionCard("Device settings", "Wi-Fi, display and more", Icons.Rounded.Settings, accent, onQuickSettings, Modifier.weight(1f))
                QuickActionCard("App library", "${apps.size} apps detected", Icons.Rounded.Apps, accent, onApps, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HeroPanel(title: String, subtitle: String, accent: Color, buttonText: String, onAction: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(220.dp)
            .background(Brush.horizontalGradient(listOf(Color(0xFF252C3C), Color(0xFF302536), Color(0xFF171A24))), RoundedCornerShape(27.dp))
    ) {
        Box(Modifier.align(Alignment.CenterEnd).size(280.dp).background(Brush.radialGradient(listOf(accent.copy(alpha = .30f), Color.Transparent)), CircleShape))
        Column(Modifier.align(Alignment.CenterStart).padding(start = 32.dp, end = 28.dp)) {
            Text("YOUR SPACE. YOUR STORIES.", color = accent, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.2.sp)
            Spacer(Modifier.height(11.dp))
            Text(title, color = Color.White, fontSize = 34.sp, lineHeight = 39.sp, fontWeight = FontWeight.Light, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(8.dp))
            Text(subtitle, color = Muted, fontSize = 12.sp)
            Spacer(Modifier.height(17.dp))
            Button(onClick = onAction, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2E7ED), contentColor = Night), shape = CircleShape, contentPadding = PaddingValues(horizontal = 19.dp, vertical = 9.dp)) {
                Text(buttonText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Text("BLOOM", Modifier.align(Alignment.BottomEnd).padding(24.dp), color = Color.White.copy(alpha = .10f), fontSize = 40.sp, fontWeight = FontWeight.Light, letterSpacing = 5.sp)
    }
}

@Composable
private fun AppsPage(
    apps: List<TvApp>,
    favourites: Set<String>,
    search: String,
    onlyFavourites: Boolean,
    compactTiles: Boolean,
    accent: Color,
    onSearch: (String) -> Unit,
    onFilter: (Boolean) -> Unit,
    onOpen: (TvApp) -> Unit,
    onSelect: (TvApp) -> Unit,
    onToggleFavourite: (TvApp) -> Unit,
    onRefresh: () -> Unit
) {
    val filtered = apps.filter {
        it.title.contains(search, true) || it.packageName.contains(search, true)
    }.filter { !onlyFavourites || it.packageName in favourites }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Your apps", color = Color.White, fontSize = 31.sp, fontWeight = FontWeight.Light)
                Text("${apps.size} apps found on this device", color = Muted, fontSize = 12.sp)
            }
            OutlinedTextField(
                value = search, onValueChange = onSearch, singleLine = true,
                placeholder = { Text("Search apps", color = Muted, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Rounded.Search, null, tint = Muted) },
                modifier = Modifier.width(270.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accent, unfocusedBorderColor = Hairline,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
                )
            )
            Spacer(Modifier.width(12.dp))
            FilterChip(selected = onlyFavourites, onClick = { onFilter(!onlyFavourites) }, label = { Text("Favourites") })
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onRefresh) { Icon(Icons.Rounded.Refresh, "Refresh apps", tint = accent) }
        }
        Spacer(Modifier.height(18.dp))
        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (apps.isEmpty()) "No launchable apps were found." else "No apps match that search.", color = Muted)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                items(filtered, key = { it.packageName }) { app ->
                    AppListRow(app, app.packageName in favourites, accent, onOpen = { onOpen(app) }, onDetails = { onSelect(app) }, onFavourite = { onToggleFavourite(app) }, compact = compactTiles)
                }
            }
        }
    }
}

@Composable
private fun AppShelf(
    apps: List<TvApp>,
    compact: Boolean,
    accent: Color,
    onOpen: (TvApp) -> Unit,
    onSelect: (TvApp) -> Unit,
    onFavourite: (TvApp) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(13.dp), contentPadding = PaddingValues(vertical = 7.dp)) {
        items(apps, key = { it.packageName }) { app ->
            AppTile(app, compact, accent, onOpen = { onOpen(app) }, onDetails = { onSelect(app) }, onFavourite = { onFavourite(app) })
        }
    }
}

@Composable
private fun AppTile(
    app: TvApp,
    compact: Boolean,
    accent: Color,
    onOpen: () -> Unit,
    onDetails: () -> Unit,
    onFavourite: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val bg by animateColorAsState(if (focused) PanelRaised else Panel, animationSpec = tween(150), label = "tile")
    Surface(
        onClick = onOpen,
        modifier = Modifier.width(if (compact) 142.dp else 166.dp).height(if (compact) 104.dp else 119.dp)
            .onFocusChanged { focused = it.isFocused }.scale(if (focused) 1.035f else 1f),
        color = bg,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(if (focused) 1.5.dp else 1.dp, if (focused) accent.copy(alpha = .8f) else Hairline)
    ) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(32.dp).background(accent.copy(alpha = .15f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Text(app.title.take(1).uppercase(), color = accent, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDetails, modifier = Modifier.size(30.dp)) { Icon(Icons.Rounded.MoreHoriz, "App details", tint = Muted) }
            }
            Column {
                Text(app.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(if (app.packageName.length > 24) app.packageName.take(22) + "…" else app.packageName, color = Muted.copy(alpha = .62f), fontSize = 8.sp, maxLines = 1)
            }
        }
    }
}

@Composable
private fun AppListRow(
    app: TvApp,
    favourite: Boolean,
    accent: Color,
    onOpen: () -> Unit,
    onDetails: () -> Unit,
    onFavourite: () -> Unit,
    compact: Boolean
) {
    var focused by remember { mutableStateOf(false) }
    Surface(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth().height(if (compact) 62.dp else 72.dp).onFocusChanged { focused = it.isFocused },
        color = if (focused) PanelRaised else Panel,
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(1.dp, if (focused) accent.copy(alpha = .7f) else Hairline)
    ) {
        Row(Modifier.fillMaxSize().padding(horizontal = 17.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(39.dp).background(accent.copy(alpha = .14f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Text(app.title.take(1).uppercase(), color = accent, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.width(15.dp))
            Column(Modifier.weight(1f)) {
                Text(app.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(app.packageName, color = Muted.copy(alpha = .65f), fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onFavourite) { Icon(if (favourite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, "Toggle favourite", tint = if (favourite) accent else Muted) }
            IconButton(onClick = onDetails) { Icon(Icons.Rounded.Info, "App details", tint = Muted) }
            TextButton(onClick = onOpen) { Text("Open", color = accent) }
        }
    }
}

@Composable
private fun DiscoverPage(apps: List<TvApp>, accent: Color, onOpenStore: (String) -> Unit, onBack: () -> Unit) {
    val suggestions = listOf(
        "YouTube" to "com.google.android.youtube.tv",
        "Netflix" to "com.netflix.ninja",
        "Disney+" to "com.disney.disneyplus",
        "Prime Video" to "com.amazon.amazonvideo.livingroom",
        "Plex" to "com.plexapp.android",
        "Spotify" to "com.spotify.tv.android"
    )
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Discover", color = Color.White, fontSize = 31.sp, fontWeight = FontWeight.Light)
                Text("Find entertainment apps for your device.", color = Muted, fontSize = 12.sp)
            }
            TextButton(onClick = onBack) { Text("Back to apps", color = accent) }
        }
        Spacer(Modifier.height(20.dp))
        Text("SUGGESTED APPS", color = accent, fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(suggestions) { (name, pkg) ->
                val installed = apps.any { it.packageName == pkg }
                Surface(color = Panel, shape = RoundedCornerShape(15.dp), border = BorderStroke(1.dp, Hairline)) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(38.dp).background(accent.copy(alpha = .15f), RoundedCornerShape(11.dp)), contentAlignment = Alignment.Center) {
                            Text(name.take(1), color = accent, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(if (installed) "Already detected on this device" else "Open the Play Store listing if available", color = Muted, fontSize = 10.sp)
                        }
                        if (!installed) TextButton(onClick = { onOpenStore(pkg) }) { Text("View listing  ›", color = accent) }
                        else Text("INSTALLED", color = accent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("Bloom does not host APK files. App availability depends on your device, region and the official store.", color = Muted.copy(alpha = .7f), fontSize = 10.sp)
    }
}

@Composable
private fun SettingsPage(
    profileName: String,
    animations: Boolean,
    reduceMotion: Boolean,
    compactTiles: Boolean,
    showClock: Boolean,
    accentIndex: Int,
    onProfile: () -> Unit,
    onAnimations: (Boolean) -> Unit,
    onReduceMotion: (Boolean) -> Unit,
    onCompactTiles: (Boolean) -> Unit,
    onShowClock: (Boolean) -> Unit,
    onAccent: (Int) -> Unit,
    onDeviceSettings: () -> Unit,
    onUpdates: () -> Unit,
    onAbout: () -> Unit,
    accent: Color
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
        item {
            Text("Settings", color = Color.White, fontSize = 31.sp, fontWeight = FontWeight.Light)
            Text("Make Bloom feel at home.", color = Muted, fontSize = 12.sp)
            Spacer(Modifier.height(10.dp))
        }
        item {
            SettingsAction("Profile", profileName.ifBlank { "Set up your Bloom profile" }, Icons.Rounded.Person, accent, onProfile)
        }
        item { SettingsToggle("Animations", "Enable smooth screen and focus transitions", animations, accent, onAnimations) }
        item { SettingsToggle("Reduce motion", "Use calmer transitions and fewer movement effects", reduceMotion, accent, onReduceMotion) }
        item { SettingsToggle("Compact app tiles", "Fit more apps on the screen", compactTiles, accent, onCompactTiles) }
        item { SettingsToggle("Show clock", "Display the time in the top bar", showClock, accent, onShowClock) }
        item {
            Surface(color = Panel, shape = RoundedCornerShape(15.dp), border = BorderStroke(1.dp, Hairline)) {
                Column(Modifier.fillMaxWidth().padding(17.dp)) {
                    Text("Accent colour", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text("Choose Bloom's highlight colour.", color = Muted, fontSize = 10.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                        listOf(Color(0xFFE8B7CF), Color(0xFF9FC7F2), Color(0xFFB6D9B4), Color(0xFFEBC68E)).forEachIndexed { index, color ->
                            Surface(
                                onClick = { onAccent(index) },
                                modifier = Modifier.size(34.dp),
                                shape = CircleShape,
                                color = color,
                                border = BorderStroke(if (accentIndex == index) 3.dp else 1.dp, if (accentIndex == index) Color.White else Hairline)
                            ) {}
                        }
                    }
                }
            }
        }
        item { SettingsAction("Device settings", "Wi-Fi, display, sound and Android options", Icons.Rounded.Settings, accent, onDeviceSettings) }
        item { SettingsAction("Software updates", "View release notes and check the project releases", Icons.Rounded.SystemUpdate, accent, onUpdates) }
        item { SettingsAction("About Bloom", "Version, platform and project information", Icons.Rounded.Info, accent, onAbout) }
        item {
            Text("Bloom is an Android launcher. Full-device reboot, firmware replacement and silent system updates require manufacturer or system privileges.", color = Muted.copy(alpha = .65f), fontSize = 10.sp, lineHeight = 15.sp, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
private fun SettingsToggle(title: String, detail: String, checked: Boolean, accent: Color, onChange: (Boolean) -> Unit) {
    Surface(color = Panel, shape = RoundedCornerShape(15.dp), border = BorderStroke(1.dp, Hairline)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 17.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(detail, color = Muted, fontSize = 10.sp)
            }
            Switch(checked = checked, onCheckedChange = onChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent))
        }
    }
}

@Composable
private fun SettingsAction(title: String, detail: String, icon: androidx.compose.ui.graphics.vector.ImageVector, accent: Color, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Panel, shape = RoundedCornerShape(15.dp), border = BorderStroke(1.dp, Hairline)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 17.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(21.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(detail, color = Muted, fontSize = 10.sp)
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = Muted)
        }
    }
}

@Composable
private fun UpdatesPage(accent: Color, onBack: () -> Unit, onOpenReleases: () -> Unit) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Bloom updates", color = Color.White, fontSize = 31.sp, fontWeight = FontWeight.Light)
                Text("Preview version $VERSION_NAME", color = Muted, fontSize = 12.sp)
            }
            TextButton(onClick = onBack) { Text("Back", color = accent) }
        }
        Surface(color = Panel, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Hairline)) {
            Column(Modifier.padding(22.dp)) {
                Text("YOU'RE ON THE PREVIEW CHANNEL", color = accent, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp)
                Spacer(Modifier.height(9.dp))
                Text("Keep Bloom moving forward.", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Light)
                Spacer(Modifier.height(7.dp))
                Text("New builds are published through the project's GitHub releases. This screen opens the official release page rather than pretending to install a system update.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onOpenReleases, colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Night)) { Text("View Bloom releases") }
            }
        }
        Text("RECENT PREVIEW CHANGES", color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp)
        listOf(
            "New home screen and cinematic featured panel",
            "App library with search and favourites",
            "Quick settings shortcuts for Android settings",
            "Profile setup and personalisation",
            "Accent colours, compact tiles and motion controls",
            "Discover page for official app listings"
        ).forEach { changelog ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.CheckCircle, null, tint = accent, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(10.dp))
                Text(changelog, color = Muted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun QuickSettingsOverlay(
    accent: Color,
    onClose: () -> Unit,
    onSystemSettings: () -> Unit,
    onWifi: () -> Unit,
    onDisplay: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .72f)), contentAlignment = Alignment.Center) {
        Surface(color = Panel, shape = RoundedCornerShape(26.dp), border = BorderStroke(1.dp, Hairline), modifier = Modifier.widthIn(max = 650.dp).fillMaxWidth(.82f)) {
            Column(Modifier.padding(25.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Quick settings", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Light)
                        Text("Shortcuts to Android device controls", color = Muted, fontSize = 11.sp)
                    }
                    IconButton(onClick = onClose) { Icon(Icons.Rounded.Close, "Close", tint = Muted) }
                }
                Spacer(Modifier.height(16.dp))
                SettingsAction("Wi-Fi and network", "Open Android network settings", Icons.Rounded.Wifi, accent, onWifi)
                Spacer(Modifier.height(9.dp))
                SettingsAction("Display", "Brightness and display options", Icons.Rounded.Tv, accent, onDisplay)
                Spacer(Modifier.height(9.dp))
                SettingsAction("All device settings", "Open the Android settings app", Icons.Rounded.Settings, accent, onSystemSettings)
            }
        }
    }
}

@Composable
private fun ProfileDialog(initialName: String, accent: Color, onSave: (String) -> Unit, onSkip: () -> Unit) {
    var name by remember { mutableStateOf(initialName) }
    Dialog(onDismissRequest = onSkip) {
        Surface(color = Panel, shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, Hairline)) {
            Column(Modifier.padding(25.dp)) {
                Text("Welcome to Bloom", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Light)
                Spacer(Modifier.height(7.dp))
                Text("Give this space a name. You can change it later in Settings.", color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = name, onValueChange = { name = it.take(24) }, label = { Text("Your name") }, singleLine = true)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onSkip) { Text("Skip", color = Muted) }
                    Button(onClick = { onSave(name.ifBlank { "Guest" }) }, colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Night)) { Text("Continue") }
                }
            }
        }
    }
}

@Composable
private fun AppDetailsDialog(
    app: TvApp,
    favourite: Boolean,
    accent: Color,
    onLaunch: () -> Unit,
    onFavourite: () -> Unit,
    onClose: () -> Unit,
    onAppInfo: () -> Unit
) {
    Dialog(onDismissRequest = onClose) {
        Surface(color = Panel, shape = RoundedCornerShape(23.dp), border = BorderStroke(1.dp, Hairline)) {
            Column(Modifier.padding(24.dp)) {
                Text(app.title, color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Light)
                Spacer(Modifier.height(5.dp))
                Text(app.packageName, color = Muted, fontSize = 10.sp)
                Spacer(Modifier.height(18.dp))
                Button(onClick = onLaunch, colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Night), modifier = Modifier.fillMaxWidth()) { Text("Open app") }
                OutlinedButton(onClick = onFavourite, modifier = Modifier.fillMaxWidth()) { Text(if (favourite) "Remove from favourites" else "Add to favourites") }
                TextButton(onClick = onAppInfo, modifier = Modifier.fillMaxWidth()) { Text("Android app information", color = Muted) }
                TextButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) { Text("Close", color = Muted) }
            }
        }
    }
}

@Composable
private fun InfoDialog(title: String, message: String, accent: Color, onClose: () -> Unit) {
    Dialog(onDismissRequest = onClose) {
        Surface(color = Panel, shape = RoundedCornerShape(23.dp), border = BorderStroke(1.dp, Hairline)) {
            Column(Modifier.padding(24.dp)) {
                Text(title, color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Light)
                Spacer(Modifier.height(12.dp))
                Text(message, color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
                Spacer(Modifier.height(14.dp))
                TextButton(onClick = onClose) { Text("Done", color = accent) }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    detail: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(onClick = onClick, modifier = modifier.height(83.dp), color = Panel, shape = RoundedCornerShape(15.dp), border = BorderStroke(1.dp, Hairline)) {
        Row(Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(21.dp))
            Spacer(Modifier.width(11.dp))
            Column {
                Text(title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Text(detail, color = Muted, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun SectionHeading(text: String, accent: Color) {
    Text(text, color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp)
}

@Composable
private fun SmallHint(text: String) {
    Text(text, color = Muted.copy(alpha = .75f), fontSize = 11.sp, modifier = Modifier.padding(vertical = 10.dp))
}

@Composable
private fun EmptyApps(onDiscover: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 15.dp)) {
        Text("No apps found yet", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Light)
        Spacer(Modifier.height(5.dp))
        Text("Install compatible apps, then refresh your library.", color = Muted, fontSize = 11.sp)
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onDiscover) { Text("Discover apps  ›", color = Accent) }
    }
}

@Composable
private fun BloomFooter(page: BloomPage, appCount: Int, accent: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("BLOOM TV OS LITE  •  PREVIEW $VERSION_NAME", color = Muted.copy(alpha = .42f), fontSize = 8.sp, letterSpacing = 1.1.sp)
        Text("${page.name}  •  $appCount APPS", color = Muted.copy(alpha = .55f), fontSize = 8.sp, letterSpacing = 1.1.sp)
    }
}
