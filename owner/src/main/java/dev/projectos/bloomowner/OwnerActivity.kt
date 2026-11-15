package dev.projectos.bloomowner

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF090D16)
private val Panel = Color(0xFF151D2B)
private val Raised = Color(0xFF202B3D)
private val White = Color(0xFFF5F3F7)
private val Muted = Color(0xFFAAB5C5)
private val Pink = Color(0xFFE8B7CF)

class OwnerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = darkColorScheme(primary = Pink, background = Ink, surface = Panel)) { OwnerConsole() } }
    }
}

private enum class Page(val title: String) {
    HOME("Command centre"), ANNOUNCEMENTS("Announcements"), LIBRARY("Control library"), DEVICES("Devices"), APPS("Applications"),
    UPDATES("Updates"), SECURITY("Security"), AUTOMATION("Automation"), SETTINGS("Owner settings")
}
private val categories = listOf(
"Dashboard & overview","Device fleet","Device identity","Hardware information","System health","Performance","Memory management","Storage management","Network management","Wi-Fi controls","Bluetooth controls","Display controls","Audio controls","Remote controls","Input devices","App catalogue","App installation","App permissions","App updates","App launch rules","Home screen","Widgets","Themes & appearance","Wallpapers","Typography","Accessibility","Profiles","Family settings","Privacy","Security centre","Authentication","Owner access","Backup & restore","Diagnostics","Error reports","System logs","Notifications","Schedules","Automation rules","Power management","Startup behaviour","Restart tools","Update channels","Release management","Compatibility","Developer tools","Remote administration","Audit history","Data management","Help & support"
)
private val actions = listOf(
"View status","Configure defaults","Enable or disable","Set policy","Review activity","Inspect details","Create profile","Edit profile","Delete profile","Import configuration","Export configuration","Reset defaults","Set permissions","Review permissions","Set notifications","Configure schedules","Add exception","Remove exception","Run diagnostic","View history","Compare versions","Set limits","Set alerts","Review recommendations","Manage access","Open advanced options"
)
private data class Entry(val id: Int, val category: String, val title: String)
private val catalogue = categories.flatMapIndexed { c, category ->
    actions.mapIndexed { a, action -> Entry(c * actions.size + a + 1, category, "$action · ${category.lowercase()}") }
}

@Composable
private fun OwnerConsole() {
    val context = LocalContext.current
    var page by remember { mutableStateOf(Page.HOME) }
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("All categories") }
    var owner by remember { mutableStateOf("Owner") }
    var accentIndex by remember { mutableIntStateOf(0) }
    var reduceMotion by remember { mutableStateOf(false) }
    val accents = listOf(Pink, Color(0xFF9FC7F2), Color(0xFFB8DAB5), Color(0xFFEBC68E))
    val accent = accents[accentIndex]
    BackHandler(page != Page.HOME) { page = Page.HOME; query = "" }
    Row(Modifier.fillMaxSize().background(Ink).padding(22.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        Column(Modifier.width(225.dp).fillMaxHeight().background(Panel, RoundedCornerShape(22.dp)).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("✿", color = accent, fontSize = 30.sp)
                Column { Text("BLOOM", color = White, fontWeight = FontWeight.Bold, letterSpacing = 2.sp); Text("COMMAND CENTRE", color = accent, fontSize = 10.sp, letterSpacing = 2.sp) }
            }
            Spacer(Modifier.height(24.dp))
            Page.values().forEach { item -> NavItem(item.title, page == item, accent) { page = item } }
            Spacer(Modifier.weight(1f))
            Text("OWNER CONTROL", color = Muted, fontSize = 10.sp, letterSpacing = 1.5.sp)
            Text("Bloom management console", color = Muted, fontSize = 11.sp)
        }
        Column(Modifier.weight(1f).fillMaxHeight()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text(page.title, color = White, fontSize = 28.sp, fontWeight = FontWeight.Bold); Text("Bloom Command Centre  /  Platform administration", color = Muted, fontSize = 12.sp) }
                Column(horizontalAlignment = Alignment.End) { Text(owner, color = White, fontWeight = FontWeight.SemiBold); Text("LOCAL OWNER SESSION", color = accent, fontSize = 9.sp) }
            }
            Spacer(Modifier.height(18.dp))
            when (page) {
                Page.HOME -> Home(accent) { page = it }
                Page.ANNOUNCEMENTS -> Announcements(accent)
                Page.LIBRARY -> Library(query, { query = it }, category, { category = it }, accent)
                Page.DEVICES -> Management("Device management", "This preview manages the Android device on which Owner OS is installed.", listOf("Device identity","Hardware summary","Android version","Display information","Network settings"), accent) { context.startActivity(Intent(Settings.ACTION_DEVICE_INFO_SETTINGS)) }
                Page.APPS -> Management("Application management", "Manage applications using Android's supported settings.", listOf("Installed apps","Default launcher","App permissions","Special access","Storage usage"), accent) { context.startActivity(Intent(Settings.ACTION_APPLICATION_SETTINGS)) }
                Page.UPDATES -> Management("Release management", "Preview releases are published through GitHub. Automatic installation and fleet rollout are not enabled.", listOf("Owner OS release channel","Build history","Release notes","Update policy","Rollback planning"), accent) { context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/projectosdev/bloom-tv-os/actions"))) }
                Page.SECURITY -> Management("Security centre", "Shortcuts to Android's own security and permission controls.", listOf("App permissions","Screen lock","Privacy dashboard","Unknown app installs","Security updates"), accent) { context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS)) }
                Page.AUTOMATION -> Management("Automation studio", "A planned module. Rules will only run after explicit configuration and permission.", listOf("Schedules","Startup routines","App launch routines","Quiet hours","Automation history"), accent) {}
                Page.SETTINGS -> Settings(owner, { owner = it }, accentIndex, { accentIndex = it }, reduceMotion, { reduceMotion = it }, accent)
            }
        }
    }
}

@Composable
private fun NavItem(label: String, selected: Boolean, accent: Color, click: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp).background(if (selected) accent.copy(alpha=.14f) else Color.Transparent, RoundedCornerShape(11.dp)).border(if(focused) 1.dp else 0.dp, if(focused) accent else Color.Transparent, RoundedCornerShape(11.dp)).onFocusChanged { focused = it.isFocused }.clickable { click() }.focusable().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = if(selected) accent else White, fontSize = 13.sp, fontWeight = if(selected) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun Home(accent: Color, open: (Page) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(Modifier.fillMaxWidth().height(142.dp).background(Brush.linearGradient(listOf(Color(0xFF382A40), Color(0xFF17283A), Ink)), RoundedCornerShape(22.dp)).padding(22.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("YOUR SYSTEM. YOUR RULES.", color = accent, fontSize = 10.sp, letterSpacing = 2.sp)
                Text("Welcome to Bloom Command.", color = White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                Text("Operate, communicate and manage the Bloom platform from one place.", color = Muted, fontSize = 13.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Metric("CONTROL CATALOGUE", "${catalogue.size}+", "roadmap entries", accent, Modifier.weight(1f))
            Metric("CONNECTED DEVICES", "1", "this local device", accent, Modifier.weight(1f))
            Metric("RELEASE CHANNEL", "PREVIEW", "manual updates", accent, Modifier.weight(1f))
            Metric("OWNER ACCESS", "LOCAL", "this installation", accent, Modifier.weight(1f))
        }
        Text("MANAGEMENT CENTRE", color = Muted, fontSize = 10.sp, letterSpacing = 2.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(Page.ANNOUNCEMENTS, Page.LIBRARY, Page.DEVICES, Page.UPDATES).forEach { p ->
                Column(Modifier.weight(1f).height(90.dp).background(Panel, RoundedCornerShape(15.dp)).clickable { open(p) }.focusable().padding(13.dp)) {
                    Text(p.title, color = White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Open management", color = accent, fontSize = 10.sp)
                }
            }
        }
        Text("Owner controls are separated by capability. Local actions are clearly distinguished from server-wide operations.", color = Muted, fontSize = 11.sp)
    }
}


@Composable
private fun Announcements(accent: Color) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("bloom_owner_announcements", android.content.Context.MODE_PRIVATE) }
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var audience by remember { mutableStateOf("All Bloom devices (draft)") }
    var status by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(prefs.getString("draft", "") ?: "") }
    Column(Modifier.fillMaxWidth().fillMaxHeight().verticalScroll(androidx.compose.foundation.rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Announcement studio", color = White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Compose and save owner announcements. Publishing to other devices requires a connected Bloom service backend; this preview saves drafts locally only.", color = Muted, fontSize = 12.sp)
        Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(16.dp)).padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("NEW ANNOUNCEMENT", color = accent, fontSize = 10.sp, letterSpacing = 2.sp)
            BasicTextField(title, { title = it }, singleLine = true, modifier = Modifier.fillMaxWidth().background(Raised, RoundedCornerShape(9.dp)).padding(13.dp), textStyle = androidx.compose.ui.text.TextStyle(color = White, fontSize = 14.sp), decorationBox = { inner -> if(title.isEmpty()) Text("Announcement title", color = Muted); inner() })
            BasicTextField(message, { message = it }, modifier = Modifier.fillMaxWidth().heightIn(min = 110.dp).background(Raised, RoundedCornerShape(9.dp)).padding(13.dp), textStyle = androidx.compose.ui.text.TextStyle(color = White, fontSize = 13.sp), decorationBox = { inner -> if(message.isEmpty()) Text("Write your message…", color = Muted); inner() })
            Text("Audience: $audience", color = Muted, fontSize = 11.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { audience = "All Bloom devices (draft)" }, colors = ButtonDefaults.buttonColors(containerColor = Raised)) { Text("All devices", color = White) }
                Button(onClick = { audience = "Selected devices (draft)" }, colors = ButtonDefaults.buttonColors(containerColor = Raised)) { Text("Selected", color = White) }
            }
            Button(onClick = {
                if (title.isBlank() || message.isBlank()) status = "Add a title and message before saving."
                else {
                    saved = "$title\n$message\nAudience: $audience"
                    prefs.edit().putString("draft", saved).apply()
                    status = "Draft saved on this device. It has not been sent to other devices."
                }
            }, colors = ButtonDefaults.buttonColors(containerColor = accent)) { Text("Save announcement draft", color = Ink) }
            if (status.isNotBlank()) Text(status, color = accent, fontSize = 11.sp)
        }
        if (saved.isNotBlank()) {
            Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(16.dp)).padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("LATEST SAVED DRAFT", color = accent, fontSize = 10.sp, letterSpacing = 2.sp)
                Text(saved, color = White, fontSize = 13.sp)
                Text("LOCAL DRAFT · NOT PUBLISHED", color = Color(0xFFEBC68E), fontSize = 10.sp)
                TextButton(onClick = { prefs.edit().remove("draft").apply(); saved = ""; status = "Draft deleted." }) { Text("Delete draft", color = Muted) }
            }
        }
    }
}

@Composable
private fun Metric(title: String, value: String, detail: String, accent: Color, mod: Modifier) {
    Column(mod.background(Panel, RoundedCornerShape(15.dp)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, color = Muted, fontSize = 9.sp)
        Text(value, color = accent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(detail, color = Muted, fontSize = 10.sp)
    }
}

@Composable
private fun Library(query: String, setQuery: (String)->Unit, category: String, setCategory: (String)->Unit, accent: Color) {
    val filtered = remember(query, category) { catalogue.filter { (category == "All categories" || it.category == category) && (query.isBlank() || it.title.contains(query, true) || it.category.contains(query, true) || it.id.toString() == query) } }
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text("1,300+ owner capability catalogue", color = accent, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Text("Search the roadmap. Every entry is labelled planned until a real implementation is connected.", color = Muted, fontSize = 11.sp)
        BasicTextField(query, setQuery, singleLine = true, modifier = Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(11.dp)).padding(14.dp), textStyle = androidx.compose.ui.text.TextStyle(color = White, fontSize = 14.sp), decorationBox = { inner -> if(query.isEmpty()) Text("Search controls, categories or ID…", color = Muted); inner() })
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { expanded = true }, colors = ButtonDefaults.buttonColors(containerColor = Raised)) { Text(category, color = White) }
            DropdownMenu(expanded, { expanded = false }) {
                DropdownMenuItem(text = { Text("All categories") }, onClick = { setCategory("All categories"); expanded = false })
                categories.forEach { c -> DropdownMenuItem(text = { Text(c) }, onClick = { setCategory(c); expanded = false }) }
            }
            Spacer(Modifier.weight(1f))
            Text("${filtered.size} results", color = accent, fontSize = 11.sp)
        }
        LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(filtered, key = { it.id }) { item ->
                Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(10.dp)).padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(item.id.toString().padStart(4, '0'), color = accent, fontSize = 11.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) { Text(item.title, color = White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold); Text(item.category, color = Muted, fontSize = 10.sp) }
                    Text("PLANNED", color = Color(0xFFEBC68E), fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
private fun Management(title: String, description: String, rows: List<String>, accent: Color, open: () -> Unit) {
    Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp)).padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, color = White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text(description, color = Muted, fontSize = 12.sp)
        rows.forEach { Row(Modifier.fillMaxWidth().background(Raised, RoundedCornerShape(10.dp)).padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(it, color = White, fontSize = 13.sp); Text("›", color = accent) } }
        Button(onClick = open, colors = ButtonDefaults.buttonColors(containerColor = accent)) { Text("Open Android settings / source", color = Ink) }
    }
}

@Composable
private fun Settings(name: String, setName: (String)->Unit, selected: Int, setAccent: (Int)->Unit, reduce: Boolean, setReduce: (Boolean)->Unit, accent: Color) {
    Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp)).padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Owner preferences", color = White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text("Owner display name", color = Muted, fontSize = 12.sp)
        BasicTextField(name, setName, singleLine = true, modifier = Modifier.fillMaxWidth().background(Raised, RoundedCornerShape(10.dp)).padding(13.dp), textStyle = androidx.compose.ui.text.TextStyle(color = White))
        Text("Accent theme", color = Muted, fontSize = 12.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(Pink, Color(0xFF9FC7F2), Color(0xFFB8DAB5), Color(0xFFEBC68E)).forEachIndexed { i, color -> Button(onClick = { setAccent(i) }, colors = ButtonDefaults.buttonColors(containerColor = color)) { Text(if(i == selected) "Selected" else "Choose", color = Ink) } }
        }
        Row(verticalAlignment = Alignment.CenterVertically) { Text("Reduce motion", color = White, modifier = Modifier.weight(1f)); Switch(reduce, setReduce) }
        Text("These preferences belong only to Bloom Owner OS. They do not alter the standard Bloom launcher.", color = Muted, fontSize = 11.sp)
    }
}
