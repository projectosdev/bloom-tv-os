package dev.projectos.bloomtest

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BG = Color(0xFF080D18)
private val CARD = Color(0xFF152238)
private val TEXT = Color(0xFFF4F6FA)
private val MUTED = Color(0xFFA7B6CB)
private val BLUE = Color(0xFFB9D9FF)
private val RED = Color(0xFFFF817B)

class TestMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("bloom_test_os", Context.MODE_PRIVATE)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = BLUE, background = BG, surface = CARD)) {
                var locked by remember { mutableStateOf(prefs.getBoolean("restricted", false)) }
                var alert by remember { mutableStateOf(false) }
                var section by remember { mutableStateOf("Home") }
                var status by remember { mutableStateOf("TEST ENVIRONMENT READY") }
                val sections = listOf("Home", "Apps", "Media", "Games", "Settings", "Network", "Accessibility", "About")
                Box(Modifier.fillMaxSize().background(BG)) {
                    Column(Modifier.fillMaxSize().padding(26.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column { Text("BLOOM", color = TEXT, fontSize = 13.sp, letterSpacing = 4.sp, fontWeight = FontWeight.Bold); Text("TEST OS", color = BLUE, fontSize = 10.sp, letterSpacing = 3.sp) }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("TEST BUILD · NOT LIVE", color = BLUE, fontSize = 10.sp, modifier = Modifier.background(BLUE.copy(alpha=.12f), RoundedCornerShape(20.dp)).padding(9.dp))
                                Text(status, color = MUTED, fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterVertically))
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        if (locked) {
                            Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF341F2B), BG)), RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("ACCESS RESTRICTED", color = RED, fontSize = 12.sp, letterSpacing = 3.sp)
                                    Text("This test device is temporarily restricted", color = TEXT, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                                    Text("A Bloom test-environment owner has applied a review restriction.", color = MUTED, fontSize = 13.sp)
                                    Text("Restriction ID: TEST-LOCAL-001  ·  Appeal / review required", color = MUTED, fontSize = 11.sp)
                                    Button(onClick = { locked = false; prefs.edit().putBoolean("restricted", false).apply(); status = "LOCAL TEST DEVICE UNLOCKED" }, colors = ButtonDefaults.buttonColors(containerColor = BLUE)) { Text("Owner recovery: unlock test device", color = BG) }
                                }
                            }
                        } else {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                                Column(Modifier.weight(1.25f)) {
                                    Text("Your screen. Your Bloom.", color = TEXT, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                                    Text("A separate sandbox for testing the Bloom TV experience.", color = MUTED, fontSize = 14.sp)
                                    Spacer(Modifier.height(18.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        items(sections) { item -> Text(item, color = if(section == item) BG else TEXT, modifier = Modifier.background(if(section == item) BLUE else CARD, RoundedCornerShape(12.dp)).clickable { section = item }.focusable().padding(horizontal = 16.dp, vertical = 11.dp), fontSize = 12.sp) }
                                    }
                                    Spacer(Modifier.height(18.dp))
                                    Box(Modifier.fillMaxWidth().height(190.dp).background(Brush.linearGradient(listOf(Color(0xFF304E76), Color(0xFF192A43), CARD)), RoundedCornerShape(22.dp)).padding(22.dp)) {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("BLOOM TEST EXPERIENCE", color = BLUE, fontSize = 10.sp, letterSpacing = 2.sp)
                                            Text(section, color = TEXT, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                            Text("Explore layouts, navigation, app tiles and system behaviour in a safe test sandbox.", color = MUTED, fontSize = 13.sp)
                                        }
                                    }
                                    Spacer(Modifier.height(14.dp))
                                    Text("FEATURED", color = MUTED, fontSize = 10.sp, letterSpacing = 2.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        listOf("Bloom Player", "App Library", "Cloud Games", "Settings").forEach { tile ->
                                            Column(Modifier.weight(1f).height(105.dp).background(CARD, RoundedCornerShape(15.dp)).clickable { section = tile }.focusable().padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
                                                Text("✦", color = BLUE, fontSize = 22.sp); Text(tile, color = TEXT, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                                Column(Modifier.width(235.dp).background(CARD, RoundedCornerShape(20.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("TEST DEVICE STATUS", color = BLUE, fontSize = 10.sp, letterSpacing = 1.5.sp)
                                    StatusLine("Environment", "Sandbox")
                                    StatusLine("Remote control", "Not connected")
                                    StatusLine("Device policy", "Test only")
                                    StatusLine("Build channel", "Preview")
                                    Divider(color = MUTED.copy(alpha=.2f))
                                    Text("LOCAL TEST CONTROLS", color = MUTED, fontSize = 9.sp, letterSpacing = 1.sp)
                                    Button(onClick = { alert = true; status = "TEST ALERT PREVIEW" }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = RED)) { Text("Preview priority alert", color = BG, fontSize = 11.sp) }
                                    OutlinedButton(onClick = { locked = true; prefs.edit().putBoolean("restricted", true).apply(); status = "LOCAL TEST DEVICE RESTRICTED" }, modifier = Modifier.fillMaxWidth()) { Text("Simulate TV restriction", fontSize = 11.sp) }
                                    Text("These controls affect only this test installation. Global commands require the owner service.", color = MUTED, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                    if (alert) AlertDialog(onDismissRequest = { alert = false }, title = { Text("Bloom priority alert · TEST") }, text = { Text("This is a test-only priority notice. No public emergency service is contacted.") }, confirmButton = { TextButton(onClick = { alert = false; playTestTone() }) { Text("Play test tone") } }, dismissButton = { TextButton(onClick = { alert = false }) { Text("Close") } })
                }
            }
        }
    }
}

@Composable private fun StatusLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, color = MUTED, fontSize = 10.sp); Text(value, color = TEXT, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) }
}

private fun playTestTone() {
    try {
        val tone = ToneGenerator(AudioManager.STREAM_ALARM, 75)
        tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 650)
    } catch (_: Exception) { }
}
