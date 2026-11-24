package dev.projectos.bloom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.focus.FocusRequester

/** Requests focus and reports whether the request completed without throwing. */
internal fun requestBloomFocus(focusRequester: FocusRequester): Boolean =
    runBloomCancellable { focusRequester.requestFocus() }.isSuccess

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
