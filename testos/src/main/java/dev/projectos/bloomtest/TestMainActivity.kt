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
import androidx.compose.foundation.text.BasicTextField
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
private val RAISED = Color(0xFF20334D)
private val TEXT = Color(0xFFF4F6FA)
private val MUTED = Color(0xFFA7B6CB)
private val BLUE = Color(0xFFB9D9FF)
private val RED = Color(0xFFFF817B)
private val GREEN = Color(0xFF9ED7B2)

class TestMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("bloom_test_os", Context.MODE_PRIVATE)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = BLUE, background = BG, surface = CARD)) {
                var restricted by remember { mutableStateOf(prefs.getBoolean("restricted", false)) }
                var maintenance by remember { mutableStateOf(false) }
                var alertVisible by remember { mutableStateOf(false) }
                var section by remember { mutableStateOf("Home") }
                var alertTitle by remember { mutableStateOf("Bloom service notice") }
                var alertMessage by remember { mutableStateOf("This is a test announcement for Bloom Test OS.") }
                var status by remember { mutableStateOf("TEST ENVIRONMENT READY") }
                var soundStatus by remember { mutableStateOf("Sound preview ready") }
                val sections = listOf("Home", "Apps", "Media", "Games", "Settings", "Network", "Accessibility", "About")
                val mode = when { restricted -> "RESTRICTED"; maintenance -> "MAINTENANCE"; alertVisible -> "ALERT"; else -> "NORMAL" }

                Box(Modifier.fillMaxSize().background(BG)) {
                    Column(Modifier.fillMaxSize().padding(26.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("BLOOM", color = TEXT, fontSize = 15.sp, letterSpacing = 4.sp, fontWeight = FontWeight.Black)
                                Text("TEST OS  /  DEVICE SIMULATOR", color = BLUE, fontSize = 9.sp, letterSpacing = 2.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("TEST BUILD · NOT LIVE", color = BLUE, fontSize = 9.sp, modifier = Modifier.background(BLUE.copy(alpha = .12f), RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 7.dp))
                                Text(status, color = MUTED, fontSize = 9.sp)
                            }
                        }
                        Spacer(Modifier.height(20.dp))

                        if (restricted || maintenance || alertVisible) {
                            val headline = when { restricted -> "ACCESS RESTRICTED"; maintenance -> "WE'LL BE RIGHT BACK"; else -> "PRIORITY NOTICE" }
                            val body = when { restricted -> "This test device is temporarily restricted"; maintenance -> "Bloom is undergoing maintenance"; else -> alertTitle }
                            val detail = when { restricted -> "A review is required. This is a local test simulation."; maintenance -> "The test environment is temporarily unavailable."; else -> alertMessage }
                            Box(Modifier.fillMaxWidth().weight(1f).background(Brush.linearGradient(listOf(if (restricted) Color(0xFF462B38) else Color(0xFF304E76), BG)), RoundedCornerShape(24.dp)).padding(28.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(headline, color = if (restricted) RED else BLUE, fontSize = 11.sp, letterSpacing = 3.sp)
                                    Text(body, color = TEXT, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                    Text(detail, color = MUTED, fontSize = 13.sp)
                                    Text("SIMULATION ONLY · THIS DEVICE", color = MUTED, fontSize = 9.sp, letterSpacing = 1.5.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (restricted) Button(onClick = { restricted = false; prefs.edit().putBoolean("restricted", false).apply(); status = "LOCAL TEST DEVICE RECOVERED" }) { Text("Owner recovery") }
                                        OutlinedButton(onClick = { restricted = false; maintenance = false; alertVisible = false; status = "NORMAL TEST MODE" }) { Text("Restore normal screen") }
                                    }
                                }
                            }
                        } else {
                            Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                                Column(Modifier.weight(1.3f)) {
                                    Text("Your screen. Your Bloom.", color = TEXT, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                                    Text("A separate sandbox for testing the Bloom TV experience.", color = MUTED, fontSize = 13.sp)
                                    Spacer(Modifier.height(16.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(sections) { item ->
                                            Text(item, color = if (section == item) BG else TEXT, modifier = Modifier.background(if (section == item) BLUE else CARD, RoundedCornerShape(10.dp)).clickable { section = item }.focusable().padding(horizontal = 14.dp, vertical = 10.dp), fontSize = 11.sp)
                                        }
                                    }
                                    Spacer(Modifier.height(16.dp))
                                    Box(Modifier.fillMaxWidth().weight(1f).background(Brush.linearGradient(listOf(Color(0xFF304E76), Color(0xFF192A43), CARD)), RoundedCornerShape(22.dp)).padding(22.dp)) {
                                        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                                            Text("BLOOM TEST EXPERIENCE", color = BLUE, fontSize = 9.sp, letterSpacing = 2.sp)
                                            Text(section, color = TEXT, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                            Text(sectionDescription(section), color = MUTED, fontSize = 13.sp)
                                            Spacer(Modifier.height(8.dp))
                                            Text("PREVIEW CONTENT", color = BLUE, fontSize = 9.sp, letterSpacing = 1.5.sp)
                                            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                                                listOf("Featured", "Recently added", "Recommended").forEach { label ->
                                                    Column(Modifier.weight(1f).height(88.dp).background(Color.White.copy(alpha = .07f), RoundedCornerShape(12.dp)).padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
                                                        Text("✦", color = BLUE, fontSize = 19.sp)
                                                        Text(label, color = TEXT, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                Column(Modifier.width(245.dp).fillMaxHeight().background(CARD, RoundedCornerShape(20.dp)).padding(15.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                                    Text("TEST DEVICE STATUS", color = BLUE, fontSize = 10.sp, letterSpacing = 1.5.sp)
                                    StatusLine("Environment", "Sandbox")
                                    StatusLine("Remote control", "Not connected")
                                    StatusLine("Device policy", "Test only")
                                    StatusLine("Current mode", mode)
                                    StatusLine("Build channel", "Preview")
                                    Divider(color = MUTED.copy(alpha = .2f))
                                    Text("LOCAL SIMULATIONS", color = MUTED, fontSize = 9.sp, letterSpacing = 1.sp)
                                    TestButton("Preview priority alert", RED) { alertVisible = true; status = "LOCAL ALERT PREVIEW" }
                                    TestButton("Maintenance screen", BLUE) { maintenance = true; status = "LOCAL MAINTENANCE PREVIEW" }
                                    OutlinedButton(onClick = { restricted = true; prefs.edit().putBoolean("restricted", true).apply(); status = "LOCAL RESTRICTION PREVIEW" }, modifier = Modifier.fillMaxWidth()) { Text("Simulate restriction", fontSize = 10.sp) }
                                    Text("Only this test installation is affected. No global TV controls are connected.", color = MUTED, fontSize = 9.sp)
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Column(Modifier.weight(1f).background(CARD, RoundedCornerShape(14.dp)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                Text("ALERT MESSAGE BUILDER", color = BLUE, fontSize = 9.sp, letterSpacing = 1.5.sp)
                                Field(alertTitle, { alertTitle = it }, "Alert title")
                                Field(alertMessage, { alertMessage = it }, "Alert message")
                                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                    SmallButton("Show on TV screen") { alertVisible = true; maintenance = false; restricted = false; status = "ALERT SCREEN ACTIVE" }
                                    SmallButton("Clear / restore") { alertVisible = false; maintenance = false; restricted = false; status = "NORMAL TEST MODE" }
                                }
                            }
                            Column(Modifier.weight(1f).background(CARD, RoundedCornerShape(14.dp)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                Text("SOUND & RECOVERY", color = BLUE, fontSize = 9.sp, letterSpacing = 1.5.sp)
                                Text("Play the original Bloom test warning tone on this device.", color = MUTED, fontSize = 10.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                    SmallButton("Play test tone") { playTestTone(); soundStatus = "Played local Bloom test tone" }
                                    SmallButton("Restore normal") { restricted = false; maintenance = false; alertVisible = false; prefs.edit().putBoolean("restricted", false).apply(); status = "NORMAL TEST MODE" }
                                }
                                Text(soundStatus, color = GREEN, fontSize = 9.sp)
                            }
                        }
                        Text("LOCAL TEST ENVIRONMENT · NO PUBLIC ALERTS · NO REMOTE DEVICE ACTIONS", color = MUTED, fontSize = 8.sp, letterSpacing = 1.sp)
                    }
                }
            }
        }
    }
}

private fun sectionDescription(section: String): String = when (section) {
    "Apps" -> "Browse the app library layout and test focus navigation."
    "Media" -> "A dedicated space for video, music and media experiences."
    "Games" -> "Explore the games hub and controller-friendly tiles."
    "Settings" -> "Device preferences, display options and system controls."
    "Network" -> "Connection status and network diagnostics for this test device."
    "Accessibility" -> "Preview readable layouts and accessibility settings."
    "About" -> "Build details and information about this isolated test application."
    else -> "Explore layouts, navigation, app tiles and system behaviour in a safe test sandbox."
}

@Composable private fun StatusLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MUTED, fontSize = 9.sp)
        Text(value, color = TEXT, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable private fun TestButton(label: String, color: Color, action: () -> Unit) {
    Button(onClick = action, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = BG), contentPadding = PaddingValues(8.dp)) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable private fun SmallButton(label: String, action: () -> Unit) {
    Text(label, color = BG, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(BLUE, RoundedCornerShape(8.dp)).clickable { action() }.padding(horizontal = 11.dp, vertical = 8.dp))
}

@Composable private fun Field(value: String, onChange: (String) -> Unit, hint: String) {
    BasicTextField(value, onChange, modifier = Modifier.fillMaxWidth().background(RAISED, RoundedCornerShape(8.dp)).padding(9.dp), textStyle = androidx.compose.ui.text.TextStyle(color = TEXT, fontSize = 10.sp), decorationBox = { inner ->
        if (value.isEmpty()) Text(hint, color = MUTED, fontSize = 10.sp)
        inner()
    })
}

private fun playTestTone() {
    try {
        val tone = ToneGenerator(AudioManager.STREAM_ALARM, 70)
        tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 700)
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({ tone.release() }, 900)
    } catch (_: Exception) { }
}
