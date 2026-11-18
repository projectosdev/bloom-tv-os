package dev.projectos.bloom

import org.junit.Assert.assertEquals
import org.junit.Test

class BloomUpdateStatusTest {
    @Test
    fun availableUpdateTakesPriority() {
        assertEquals(
            BloomUpdateStatus.AVAILABLE,
            resolveBloomUpdateStatus(
                hasAvailableUpdate = true,
                checking = false,
                checkFailed = false,
                hasChecked = true
            )
        )
    }

    @Test
    fun checkingStateIsShownBeforePreviousFailure() {
        assertEquals(
            BloomUpdateStatus.CHECKING,
            resolveBloomUpdateStatus(
                hasAvailableUpdate = false,
                checking = true,
                checkFailed = false,
                hasChecked = false
            )
        )
    }

    @Test
    fun failedCheckDoesNotReportCurrentVersion() {
        assertEquals(
            BloomUpdateStatus.ERROR,
            resolveBloomUpdateStatus(
                hasAvailableUpdate = false,
                checking = false,
                checkFailed = true,
                hasChecked = true
            )
        )
    }

    @Test
    fun currentVersionRequiresSuccessfulCheck() {
        assertEquals(
            BloomUpdateStatus.CURRENT,
            resolveBloomUpdateStatus(
                hasAvailableUpdate = false,
                checking = false,
                checkFailed = false,
                hasChecked = true
            )
        )
    }

    @Test
    fun initialStateIsNotChecked() {
        assertEquals(
            BloomUpdateStatus.NOT_CHECKED,
            resolveBloomUpdateStatus(
                hasAvailableUpdate = false,
                checking = false,
                checkFailed = false,
                hasChecked = false
            )
        )
    }
}
