package dev.projectos.bloom

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BloomVersionTest {
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
