package dev.projectos.bloom

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TvFocusTest {
    @Test
    fun reportsGrantedFocusRequests() {
        assertTrue(focusRequestWasGranted(Result.success(true)))
    }

    @Test
    fun reportsDeclinedFocusRequestsAsNotGranted() {
        assertFalse(focusRequestWasGranted(Result.success(false)))
    }

    @Test
    fun treatsOrdinaryFocusRequestFailuresAsNotGranted() {
        assertFalse(
            focusRequestWasGranted(Result.failure<Boolean>(IllegalStateException("focus target detached")))
        )
    }
}
