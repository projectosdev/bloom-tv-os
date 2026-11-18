package dev.projectos.bloom

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val FDROID_PACKAGE = "org.fdroid.fdroid"
private const val FDROID_HOME = "https://f-droid.org/"
private const val FDROID_CATALOGUE = "https://f-droid.org/en/packages/"

internal fun openBloomAppStore(context: Context) {
    val installedClient = context.packageManager.getLaunchIntentForPackage(FDROID_PACKAGE)
    val intent = installedClient ?: Intent(Intent.ACTION_VIEW, Uri.parse(FDROID_HOME))
    try {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "No browser or F-Droid client is available on this device.", Toast.LENGTH_LONG).show()
    } catch (_: SecurityException) {
        Toast.makeText(context, "Android blocked the app store from opening.", Toast.LENGTH_LONG).show()
    }
}

private fun openFdroidPage(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "No browser is available on this device.", Toast.LENGTH_LONG).show()
    } catch (_: SecurityException) {
        Toast.makeText(context, "Android blocked this link.", Toast.LENGTH_LONG).show()
    }
}

@Composable
internal fun BloomStoreScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var clientInstalled by remember { mutableStateOf(false) }
    val backFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        clientInstalled = context.packageManager.getLaunchIntentForPackage(FDROID_PACKAGE) != null
        runCatching { backFocus.requestFocus() }
    }

    Column(
        Modifier.fillMaxSize().background(Color(0xFF080D18)).padding(horizontal = 58.dp, vertical = 34.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StoreBackButton(onClick = onBack, focusRequester = backFocus)
            Spacer(Modifier.width(20.dp))
            Column(Modifier.weight(1f)) {
                Text("App store", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Light)
                Text("A real Android app repository for Bloom devices", color = Color(0xFFADB8CC), fontSize = 14.sp)
            }
            Text("F-DROID", color = Color(0xFF8DC9A8), fontSize = 11.sp, letterSpacing = 2.2.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(30.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(), color = Color(0xFF151F30),
            shape = RoundedCornerShape(26.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
        ) {
            Row(Modifier.padding(horizontal = 30.dp, vertical = 26.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(76.dp), color = Color(0xFF2D815C), shape = RoundedCornerShape(20.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("F", color = Color.White, fontSize = 46.sp, fontWeight = FontWeight.Light)
                    }
                }
                Spacer(Modifier.width(22.dp))
                Column(Modifier.weight(1f)) {
                    Text("F-Droid", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(5.dp))
                    Text("Free and open-source Android app repository", color = Color(0xFFD0D8E7), fontSize = 15.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (clientInstalled) "Official client detected on this device" else "Install the official client to browse and install apps",
                        color = Color(0xFF8DC9A8), fontSize = 12.sp
                    )
                }
                StorePrimaryButton(if (clientInstalled) "Open F-Droid" else "Get F-Droid") {
                    openBloomAppStore(context)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StoreInfoCard(
                "Signed repository metadata",
                "F-Droid uses signed repository indexes. Its client manages repository checks and package updates.",
                Modifier.weight(1f)
            )
            StoreInfoCard(
                "Android installation flow",
                "Android handles package compatibility and asks for any required installation approval.",
                Modifier.weight(1f)
            )
        }
        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            StoreSecondaryButton("Browse catalogue online", Modifier.weight(1f)) {
                openFdroidPage(context, FDROID_CATALOGUE)
            }
            StoreSecondaryButton("Open F-Droid website", Modifier.weight(1f)) {
                openFdroidPage(context, FDROID_HOME)
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(
            "For Android-based Raspberry Pi builds only. This does not install apps on Raspberry Pi OS/Linux. App compatibility depends on the device, Android version and CPU architecture.",
            color = Color(0xFF8793A8), fontSize = 11.sp, lineHeight = 16.sp
        )
    }
}

@Composable
private fun StoreBackButton(onClick: () -> Unit, focusRequester: FocusRequester) {
    var focused by remember { mutableStateOf(false) }
    Surface(
        onClick = onClick,
        modifier = Modifier.size(50.dp).focusRequester(focusRequester).onFocusChanged { focused = it.isFocused },
        color = if (focused) Color(0xFF34445F) else Color(0xFF182235),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (focused) 2.dp else 1.dp, if (focused) Color(0xFFB8C7FF) else Color.White.copy(alpha = 0.14f))
    ) {
        Box(contentAlignment = Alignment.Center) { Text("‹", color = Color.White, fontSize = 30.sp) }
    }
}

@Composable
private fun StorePrimaryButton(label: String, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Surface(
        onClick = onClick,
        modifier = Modifier.height(54.dp).onFocusChanged { focused = it.isFocused },
        color = if (focused) Color(0xFFB9E7C9) else Color(0xFF8DC9A8),
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(if (focused) 3.dp else 1.dp, if (focused) Color.White else Color.Transparent)
    ) {
        Box(Modifier.padding(horizontal = 22.dp), contentAlignment = Alignment.Center) {
            Text(label, color = Color(0xFF10251A), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StoreSecondaryButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Surface(
        onClick = onClick,
        modifier = modifier.height(60.dp).onFocusChanged { focused = it.isFocused },
        color = if (focused) Color(0xFF26354B) else Color(0xFF151F30),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (focused) 2.dp else 1.dp, if (focused) Color(0xFFB8C7FF) else Color.White.copy(alpha = 0.12f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun StoreInfoCard(title: String, detail: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(130.dp), color = Color(0xFF111A29),
        shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.09f))
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.Center) {
            Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(detail, color = Color(0xFFABB6C9), fontSize = 12.sp, lineHeight = 17.sp)
        }
    }
}
