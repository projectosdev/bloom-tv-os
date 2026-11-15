package dev.projectos.bloom

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
