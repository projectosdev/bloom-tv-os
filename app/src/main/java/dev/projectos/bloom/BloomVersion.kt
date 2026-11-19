package dev.projectos.bloom

import java.net.URI
import java.util.Locale

/**
 * Accepts APK assets only from Bloom's GitHub release-download path.
 */
internal fun isTrustedBloomReleaseAssetUrl(value: String): Boolean {
    val uri = runCatching { URI(value) }.getOrNull() ?: return false
    if (!uri.scheme.equals("https", ignoreCase = true)) return false
    if (uri.host?.lowercase(Locale.ROOT) != "github.com") return false
    if (uri.userInfo != null || (uri.port != -1 && uri.port != 443)) return false
    if (uri.query != null || uri.fragment != null) return false

    val prefix = "/projectosdev/bloom-tv-os/releases/download/"
    val path = uri.path.orEmpty()
    if (!path.startsWith(prefix)) return false
    val segments = path.removePrefix(prefix).split('/')
    return segments.size == 2 &&
        segments.all { it.isNotBlank() && it != "." && it != ".." } &&
        segments.last().endsWith(".apk", ignoreCase = true)
}

/**
 * Compares numeric dotted Bloom versions such as 0.9.12 and 0.10.0.
 *
 * Release tags are intentionally restricted to numeric dotted versions with an
 * optional leading "v". Rejecting other formats avoids treating prereleases as
 * stable releases by accidentally stripping their suffix.
 */
internal fun compareBloomVersions(left: String, right: String): Int {
    fun parts(version: String): List<Long> {
        val normalized = version.trim().removePrefix("v")
        require(Regex("^\\d+(?:\\.\\d+)*$").matches(normalized)) {
            "Invalid Bloom version: $version"
        }
        return normalized.split('.').map { segment ->
            segment.toLongOrNull()
                ?: throw IllegalArgumentException("Bloom version segment is too large: $segment")
        }
    }

    val leftParts = parts(left)
    val rightParts = parts(right)
    for (index in 0 until maxOf(leftParts.size, rightParts.size)) {
        val a = leftParts.getOrElse(index) { 0L }
        val b = rightParts.getOrElse(index) { 0L }
        if (a != b) return a.compareTo(b)
    }
    return 0
}
