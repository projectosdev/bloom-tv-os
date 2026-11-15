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
    HOME("Overview"), LIBRARY("Control library"), DEVICES("Devices"), APPS("Applications"),
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
                Column { Text("BLOOM", color = White, fontWeight = FontWeight.Bold, letterSpacing = 2.sp); Text("OWNER OS", color = accent, fontSize = 10.sp, letterSpacing = 2.sp) }
            }
            Spacer(Modifier.height(24.dp))
            Page.values().forEach { item -> NavItem(item.title, page == item, accent) { page = item } }
            Spacer(Modifier.weight(1f))
            Text("OWNER EDITION", color = Muted, fontSize = 10.sp, letterSpacing = 1.5.sp)
            Text("Independent preview · 1.0", color = Muted, fontSize = 11.sp)
        }
        Column(Modifier.weight(1f).fillMaxHeight()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text(page.title, color = White, fontSize = 28.sp, fontWeight = FontWeight.Bold); Text("Bloom Owner OS  /  Management console", color = Muted, fontSize = 12.sp) }
                Column(horizontalAlignment = Alignment.End) { Text(owner, color = White, fontWeight = FontWeight.SemiBold); Text("LOCAL OWNER SESSION", color = accent, fontSize = 9.sp) }
            }
            Spacer(Modifier.height(18.dp))
            when (page) {
                Page.HOME -> Home(accent) { page = it }
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
                Text("Welcome to Owner Control.", color = White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                Text("A separate management environment for the Bloom ecosystem.", color = Muted, fontSize = 13.sp)
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
            listOf(Page.LIBRARY, Page.DEVICES, Page.APPS, Page.UPDATES).forEach { p ->
                Column(Modifier.weight(1f).height(90.dp).background(Panel, RoundedCornerShape(15.dp)).clickable { open(p) }.focusable().padding(13.dp)) {
                    Text(p.title, color = White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Open management", color = accent, fontSize = 10.sp)
                }
            }
        }
        Text("The catalogue is a roadmap index. Planned entries are not live system controls.", color = Muted, fontSize = 11.sp)
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
