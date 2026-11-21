package dev.projectos.bloom

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BloomProfileCatalogTest {
    @Test
    fun trimsBlankNamesAndDeduplicatesCaseInsensitively() {
        assertEquals(
            listOf("alex", "Mira", "Zoe"),
            sortBloomProfileNames(listOf("  Zoe ", "alex", "ALEx", "", " Mira "))
        )
    }

    @Test
    fun sortsNamesDeterministically() {
        assertEquals(
            listOf("Ada", "alex", "Mira", "Zoe"),
            sortBloomProfileNames(listOf("Zoe", "Mira", "alex", "Ada"))
        )
    }

    @Test
    fun findsStoredNameRegardlessOfInputCaseAndWhitespace() {
        assertEquals("Alex", findBloomProfileName(listOf("Alex", "Mira"), "  aLEX "))
    }

    @Test
    fun returnsNullForBlankOrUnknownProfileName() {
        assertNull(findBloomProfileName(listOf("Alex"), " "))
        assertNull(findBloomProfileName(listOf("Alex"), "Mira"))
    }
}
