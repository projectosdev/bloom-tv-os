package dev.projectos.bloom

import kotlinx.coroutines.CancellationException

/**
 * Converts ordinary operation failures into a Result without swallowing coroutine cancellation.
 *
 * Cancellation is control flow: callers must be allowed to stop network checks and downloads
 * when their screen or owning coroutine is no longer active.
 */
internal inline fun <T> runBloomCancellable(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        Result.failure(error)
    }
