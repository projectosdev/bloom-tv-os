package dev.projectos.bloomtest

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.media.AudioTrack
import android.media.AudioFormat
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
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
                LaunchedEffect(alertVisible) { if (alertVisible) playBloomWarningSound() }
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

                        if (alertVisible) {
                            val pulse = rememberInfiniteTransition(label = "alertPulse")
                            val glow by pulse.animateFloat(
                                initialValue = 0.82f, targetValue = 1.08f,
                                animationSpec = infiniteRepeatable(tween(720, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                                label = "alertGlow"
                            )
                            val sweep by pulse.animateFloat(
                                initialValue = -1f, targetValue = 1f,
                                animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
                                label = "alertSweep"
                            )
                            Box(
                                Modifier.fillMaxSize().background(
                                    Brush.verticalGradient(listOf(Color(0xFFFF242D), Color(0xFFB00012), Color(0xFF5C0010)))
                                )
                            ) {
                                Box(
                                    Modifier.fillMaxWidth().height(10.dp).align(Alignment.TopCenter)
                                        .background(Color.White.copy(alpha = .16f))
                                )
                                Box(
                                    Modifier.fillMaxWidth(.55f).fillMaxHeight()
                                        .align(Alignment.CenterStart)
                                        .offset(x = (sweep * 520).dp)
                                        .background(Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(alpha = .10f), Color.Transparent)))
                                )
                                Column(
                                    Modifier.fillMaxSize().padding(30.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("BLOOM  •  TEST ALERT", color = Color.White.copy(alpha = .82f), fontSize = 12.sp, letterSpacing = 4.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(24.dp))
                                    Box(
                                        Modifier.size(150.dp).scale(glow)
                                            .background(Color.White.copy(alpha = .13f), RoundedCornerShape(38.dp))
                                            .border(2.dp, Color.White.copy(alpha = .7f), RoundedCornerShape(38.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AnimatedContent(
                                            targetState = alertVisible,
                                            transitionSpec = { (fadeIn(tween(350)) togetherWith fadeOut(tween(220))) },
                                            label = "bloomToWarning"
                                        ) { active ->
                                            Text(if (active) "⚠" else "✿", color = Color.White, fontSize = 92.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                    Spacer(Modifier.height(28.dp))
                                    Text("WARNING", color = Color.White, fontSize = 17.sp, letterSpacing = 7.sp, fontWeight = FontWeight.Black)
                                    Spacer(Modifier.height(12.dp))
                                    Text(alertTitle.uppercase(), color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    Spacer(Modifier.height(12.dp))
                                    Text(alertMessage, color = Color.White.copy(alpha = .94f), fontSize = 16.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    Spacer(Modifier.height(24.dp))
                                    Text("PLEASE PAY ATTENTION", color = Color.White, fontSize = 11.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(30.dp))
                                    Button(
                                        onClick = { alertVisible = false; status = "NORMAL TEST MODE" },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF9C0010)),
                                        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp)
                                    ) { Text("DISMISS TEST ALERT", fontWeight = FontWeight.Black, letterSpacing = 1.sp) }
                                    Spacer(Modifier.height(12.dp))
                                    Text("SIMULATION ONLY · THIS DEVICE", color = Color.White.copy(alpha = .72f), fontSize = 9.sp, letterSpacing = 2.sp)
                                }
                            }
                        } else if (restricted || maintenance) {
                            val headline = if (restricted) "ACCESS RESTRICTED" else "WE'LL BE RIGHT BACK"
                            val body = if (restricted) "This test device is temporarily restricted" else "Bloom is undergoing maintenance"
                            val detail = if (restricted) "A review is required. This is a local test simulation." else "The test environment is temporarily unavailable."
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
                            }{
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
                                Text("Play the original Bloom warning sweep on this device.", color = MUTED, fontSize = 10.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                    SmallButton("Play warning sound") { playBloomWarningSound(); soundStatus = "Played Bloom warning sound" }
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

private fun playBloomWarningSound() {
    try {
        val sampleRate = 22050
        val durationSeconds = 3
        val samples = sampleRate * durationSeconds
        val data = ShortArray(samples)
        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val phase = (t % 1.0)
            val active = phase < 0.78
            val frequency = 620.0 + 520.0 * (0.5 - 0.5 * kotlin.math.cos(2.0 * Math.PI * (t % 1.2) / 1.2))
            val envelope = if (active) kotlin.math.min(1.0, phase * 18.0) * kotlin.math.min(1.0, (0.78 - phase) * 18.0) else 0.0
            data[i] = (kotlin.math.sin(2.0 * Math.PI * frequency * t) * envelope * 10500.0).toInt().toShort()
        }
        val track = AudioTrack(
            AudioManager.STREAM_ALARM, sampleRate,
            AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
            data.size * 2, AudioTrack.MODE_STATIC
        )
        track.write(data, 0, data.size)
        track.setNotificationMarkerPosition(samples)
        track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
            override fun onMarkerReached(player: AudioTrack) { player.release() }
            override fun onPeriodicNotification(player: AudioTrack) { }
        })
        track.play()
    } catch (_: Exception) {
        playTestTone()
    }
}

private fun playTestTone() {
    try {
        val tone = ToneGenerator(AudioManager.STREAM_ALARM, 70)
        tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 700)
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({ tone.release() }, 900)
    } catch (_: Exception) { }
}
