package dev.projectos.bloom

internal enum class BloomUpdateStatus {
    AVAILABLE,
    CHECKING,
    ERROR,
    CURRENT,
    NOT_CHECKED
}

/** Resolves update UI state without presenting a failed check as an up-to-date result. */
internal fun resolveBloomUpdateStatus(
    hasAvailableUpdate: Boolean,
    checking: Boolean,
    checkFailed: Boolean,
    hasChecked: Boolean
): BloomUpdateStatus = when {
    hasAvailableUpdate -> BloomUpdateStatus.AVAILABLE
    checking -> BloomUpdateStatus.CHECKING
    checkFailed -> BloomUpdateStatus.ERROR
    hasChecked -> BloomUpdateStatus.CURRENT
    else -> BloomUpdateStatus.NOT_CHECKED
}
