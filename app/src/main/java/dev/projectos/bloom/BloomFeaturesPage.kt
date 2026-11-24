package dev.projectos.bloom

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class BloomModule(
    val name: String,
    val description: String,
    val status: String,
    val actionLabel: String? = null,
    val actionKey: String? = null
)

@Composable
fun BloomFeaturesPage(
    apps: List<TvApp>,
    recentPackages: List<String>,
    accent: Color,
    onOpen: (TvApp) -> Unit,
    onStore: () -> Unit,
    onPrivacy: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedModule by remember { mutableStateOf<BloomModule?>(null) }
    val matches = remember(query, apps) {
        if (query.isBlank()) emptyList()
        else apps.filter { it.title.contains(query, true) || it.packageName.contains(query, true) }
            .sortedBy { it.title.lowercase() }
    }
    val recent = recentPackages.mapNotNull { pkg -> apps.firstOrNull { it.packageName == pkg } }
    val modules = listOf(
        BloomModule("Bloom Remote", "Phone remote control, keyboard input and device pairing.", "COMPANION APP REQUIRED"),
        BloomModule("Bloom Family", "Profiles, age settings and family-friendly viewing controls.", "BASIC PROFILE AVAILABLE"),
        BloomModule("Bloom Ambient", "Screensaver and display options. Uses Android's supported display controls.", "ANDROID SETTINGS"),
        BloomModule("Bloom Device Manager", "Check device information, storage, network and system settings.", "ANDROID SETTINGS"),
        BloomModule("Bloom Cloud Sync", "Synchronise favourites and preferences between Bloom devices.", "CLOUD SERVICE NOT CONNECTED"),
        BloomModule("Bloom Game Mode", "Find installed games and open compatible controller settings.", "DEVICE DEPENDENT"),
        BloomModule("Bloom Recovery", "Open Android app settings and use the device's own recovery options.", "ANDROID CONTROLS"),
        BloomModule("Bloom Store", "Discover apps through the official store available on this device.", "OPEN PLAY STORE", "store"),
        BloomModule("Bloom Connect", "Connect compatible Bloom devices and companion services.", "PAIRING SERVICE NOT CONNECTED"),
        BloomModule("Bloom Privacy Centre", "Review app permissions and Android privacy controls.", "OPEN PRIVACY SETTINGS", "privacy"),
        BloomModule("Bloom Security", "Security centre for app trust, permissions and future threat scanning.", "BASIC CHECKS ONLY"),
        BloomModule("Bloom PirateCheck", "Review suspicious app indicators. IPTV and streaming apps are not labelled illegal without evidence.", "DETECTION ENGINE NOT IMPLEMENTED"),
        BloomModule("Bloom Certificate", "Bloom's own app review and approval programme for the future Bloom Store.", "REVIEW SERVICE NOT CONNECTED"),
        BloomModule("Bloom AI Guard", "Cloud-assisted AI app analysis. No app files or viewing history are uploaded by this preview.", "CLOUD AI NOT CONNECTED")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(13.dp),
        contentPadding = PaddingValues(bottom = 18.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Bloom features", color = Color.White, fontSize = 31.sp, fontWeight = FontWeight.Light)
                Text("Your Bloom tools, services and device controls in one place.", color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Surface(color = Panel, shape = RoundedCornerShape(15.dp), border = BorderStroke(1.dp, Hairline)) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Search, null, tint = accent)
                        Spacer(Modifier.width(10.dp))
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it.take(60) },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(accent),
                            modifier = Modifier.weight(1f),
                            decorationBox = { inner ->
                                if (query.isEmpty()) Text("Search installed apps on this device", color = Muted, fontSize = 13.sp)
                                inner()
                            }
                        )
                        if (query.isNotEmpty()) TextButton(onClick = { query = "" }) { Text("Clear", color = accent) }
                    }
                }
            }
        }
        if (query.isNotBlank()) {
            item { Text("SEARCH RESULTS", color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp) }
            if (matches.isEmpty()) {
                item { Text("No installed apps match that search.", color = Muted, fontSize = 12.sp) }
            } else {
                items(matches, key = { it.packageName }) { app ->
                    FeatureAppRow(app = app, accent = accent, onOpen = { onOpen(app) })
                }
            }
        } else {
            item {
                Text("BLOOM CONTINUE", color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp)
                if (recent.isEmpty()) {
                    Text("Apps you launch from Bloom will appear here. Playback progress is controlled by each streaming service.", color = Muted, fontSize = 11.sp)
                }
            }
            items(recent, key = { "recent-${it.packageName}" }) { app ->
                FeatureAppRow(app = app, accent = accent, onOpen = { onOpen(app) })
            }
            item {
                Text("BLOOM SERVICES", color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp)
            }
            items(modules, key = { it.name }) { module ->
                FeatureModuleCard(module = module, accent = accent, onAction = {
                    when (module.actionKey) {
                        "store" -> onStore()
                        "privacy" -> onPrivacy()
                        else -> selectedModule = module
                    }
                })
            }
            item {
                Text(
                    "This is an early launcher preview. Items marked as not connected or not implemented are roadmap features, not active security or cloud services.",
                    color = Muted.copy(alpha = .72f),
                    fontSize = 10.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }

    selectedModule?.let { module ->
        AlertDialog(
            onDismissRequest = { selectedModule = null },
            containerColor = Panel,
            title = { Text(module.name, color = Color.White) },
            text = {
                Text(
                    "${module.description}\n\nCurrent status: ${module.status.lowercase().replaceFirstChar { it.uppercase() }}. This preview does not claim the service is active.",
                    color = Muted,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { selectedModule = null }) { Text("Got it", color = accent) }
            }
        )
    }
}

@Composable
private fun FeatureAppRow(app: TvApp, accent: Color, onOpen: () -> Unit) {
    Surface(
        onClick = onOpen,
        color = Panel,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Hairline)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.SmartDisplay, null, tint = accent, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(app.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(app.packageName, color = Muted, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("OPEN", color = accent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FeatureModuleCard(module: BloomModule, accent: Color, onAction: () -> Unit) {
    Surface(
        color = Panel,
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(1.dp, Hairline)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(module.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(module.description, color = Muted, fontSize = 10.sp, lineHeight = 14.sp)
                Text(module.status, color = accent, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Spacer(Modifier.width(10.dp))
            TextButton(onClick = onAction) {
                Text(module.actionLabel ?: "DETAILS", color = accent, fontSize = 10.sp)
            }
        }
    }
}
