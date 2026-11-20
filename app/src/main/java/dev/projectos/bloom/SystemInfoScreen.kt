package dev.projectos.bloom

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun SystemInfoScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val initialFocusRequester = remember { FocusRequester() }
    var backFocused by remember { mutableStateOf(false) }
    RequestFocusOnEntry(initialFocusRequester)
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName ?: "Unknown"
    val versionCode = if (Build.VERSION.SDK_INT >= 28) {
        packageInfo.longVersionCode
    } else {
        @Suppress("DEPRECATION")
        packageInfo.versionCode.toLong()
    }

    Column(
        Modifier.fillMaxSize().padding(horizontal = 54.dp, vertical = 30.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.focusRequester(initialFocusRequester).onFocusChanged { backFocused = it.isFocused },
                onClick = onBack,
                color = if (backFocused) Color(0xFF3A4A6D) else Color(0x22FFFFFF),
                shape = CircleShape,
                border = BorderStroke(if (backFocused) 2.dp else 1.dp, if (backFocused) Color(0xFFB8C7FF) else Color.White.copy(alpha = 0.15f))
            ) {
                Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.ArrowBack, "Back", tint = Color(0xFFE9ECF6))
                }
            }
            Spacer(Modifier.width(18.dp))
            Column {
                Text("About this device", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Light)
                Text("Bloom system information", color = Color(0x99E9ECF6), fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(28.dp))
        Text("BLOOM", color = Color(0xFFFFB8D2), fontSize = 11.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        InfoCard {
            InfoLine("Software", "Bloom TV OS Lite")
            InfoLine("App version", versionName)
            InfoLine("Build number", versionCode.toString())
            InfoLine("Package", context.packageName)
        }

        Spacer(Modifier.height(24.dp))
        Text("DEVICE", color = Color(0xFFFFB8D2), fontSize = 11.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        InfoCard {
            InfoLine("Manufacturer", Build.MANUFACTURER.orUnknown())
            InfoLine("Model", Build.MODEL.orUnknown())
            InfoLine("Device", Build.DEVICE.orUnknown())
            InfoLine("Android version", Build.VERSION.RELEASE.orUnknown())
            InfoLine("API level", Build.VERSION.SDK_INT.toString())
        }

        Spacer(Modifier.weight(1f))
        Text(
            "Device details are read locally from Android. Bloom does not send this information anywhere.",
            color = Color(0x77E9ECF6),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth()
            .background(Color(0x22FFFFFF), RoundedCornerShape(18.dp))
            .then(Modifier),
        content = content
    )
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color(0x99E9ECF6), fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

private fun String?.orUnknown(): String = this?.takeIf { it.isNotBlank() } ?: "Unknown"
