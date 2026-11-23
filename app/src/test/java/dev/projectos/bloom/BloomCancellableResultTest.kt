package dev.projectos.bloom

import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test

class BloomCancellableResultTest {
    @Test
    fun wrapsSuccessfulOperations() {
        val result = runBloomCancellable { "ready" }

        assertEquals("ready", result.getOrNull())
    }

    @Test
    fun wrapsOrdinaryExceptionsAsFailures() {
        val expected = IllegalStateException("network unavailable")
        val result = runBloomCancellable<String> { throw expected }

        assertSame(expected, result.exceptionOrNull())
    }

    @Test
    fun propagatesCancellationInsteadOfConvertingItToFailure() {
        val expected = CancellationException("screen closed")

        val actual = assertThrows(CancellationException::class.java) {
            runBloomCancellable<String> { throw expected }
        }

        assertSame(expected, actual)
    }
}
