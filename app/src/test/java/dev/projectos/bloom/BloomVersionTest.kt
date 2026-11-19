package dev.projectos.bloom

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BloomVersionTest {
    @Test
    fun acceptsOnlyBloomGithubReleaseApkAssets() {
        assertTrue(isTrustedBloomReleaseAssetUrl(
            "https://github.com/projectosdev/bloom-tv-os/releases/download/v0.1.84/app-debug.apk"
        ))
        assertFalse(isTrustedBloomReleaseAssetUrl(
            "https://github.com.evil.example/projectosdev/bloom-tv-os/releases/download/v0.1.84/app-debug.apk"
        ))
        assertFalse(isTrustedBloomReleaseAssetUrl(
            "http://github.com/projectosdev/bloom-tv-os/releases/download/v0.1.84/app-debug.apk"
        ))
        assertFalse(isTrustedBloomReleaseAssetUrl(
            "https://github.com/other/repo/releases/download/v0.1.84/app-debug.apk"
        ))
        assertFalse(isTrustedBloomReleaseAssetUrl(
            "https://github.com/projectosdev/bloom-tv-os/releases/download/v0.1.84/notes.txt"
        ))
        assertFalse(isTrustedBloomReleaseAssetUrl(
            "https://github.com/projectosdev/bloom-tv-os/releases/download/v0.1.84/app-debug.apk?download=1"
        ))
        assertFalse(isTrustedBloomReleaseAssetUrl(
            "https://github.com/projectosdev/bloom-tv-os/releases/download/v0.1.84/../app-debug.apk"
        ))
    }

    @Test
    fun comparesNumericSegmentsInsteadOfLexicographicText() {
        assertTrue(compareBloomVersions("0.10.0", "0.9.12") > 0)
    }

    @Test
    fun treatsMissingTrailingSegmentsAsZero() {
        assertEquals(0, compareBloomVersions("1.2", "1.2.0.0"))
    }

    @Test
    fun acceptsOptionalLeadingV() {
        assertEquals(0, compareBloomVersions("v2.4.1", "2.4.1"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsPrereleaseTagsInsteadOfTreatingThemAsStable() {
        compareBloomVersions("1.2.0-beta", "1.2.0")
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNumericSegmentsThatOverflowLong() {
        compareBloomVersions("999999999999999999999999999", "1.0")
    }
}
