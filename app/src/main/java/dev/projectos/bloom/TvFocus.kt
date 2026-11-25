package dev.projectos.bloom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.focus.FocusRequester

/**
 * Returns true only when Compose actually grants the focus request.
 *
 * A completed call can still return false when the target is not eligible for focus.
 * Ordinary failures return false, while coroutine cancellation continues to propagate.
 */
internal fun focusRequestWasGranted(result: Result<Boolean>): Boolean =
    result.getOrDefault(false)

internal fun requestBloomFocus(focusRequester: FocusRequester): Boolean =
    focusRequestWasGranted(runBloomCancellable { focusRequester.requestFocus() })

/**
 * Gives newly displayed TV screens a predictable first D-pad focus target.
 * The frame wait lets Compose attach the focus target before requesting focus.
 */
@Composable
internal fun RequestFocusOnEntry(focusRequester: FocusRequester) {
    LaunchedEffect(focusRequester) {
        withFrameNanos { }
        // Keep a detached focus target from crashing screen entry, but never
        // swallow CancellationException when this screen leaves composition.
        requestBloomFocus(focusRequester)
    }
}
