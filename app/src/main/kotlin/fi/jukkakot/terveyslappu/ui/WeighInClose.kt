package fi.jukkakot.terveyslappu.ui

import fi.jukkakot.terveyslappu.a11y.ActionTimeouts
import fi.jukkakot.terveyslappu.scale.WeighInState
import kotlinx.coroutines.delay

/** How long the saved result stays before the dialog closes by itself, at the default accessibility timeout (scale-weigh-in). */
const val CLOSE_AFTER_SAVED_MS = ActionTimeouts.RESULT_MS

/**
 * When the weigh-in dialog closes by itself: at once when discarded, after [resultMs] (the
 * accessibility-lengthened ten seconds) on the saved result or the recent automatic weight unless
 * [keepOpen] (a tap on the card, or Poista, whose delete must not be cut short); null means it
 * stays, also on the end view.
 */
fun autoCloseDelayMs(state: WeighInState, keepOpen: Boolean, resultMs: Long = CLOSE_AFTER_SAVED_MS): Long? = when {
    state is WeighInState.Discarded -> 0L
    (state is WeighInState.Saved || state is WeighInState.Recent) && !keepOpen -> resultMs
    else -> null
}

/**
 * Runs for one (state, keepOpen) pair, restarted whenever either changes, and closes when due.
 * [onSeconds] gets the whole seconds left while a result counts down (Valmis shows them).
 */
suspend fun closeWhenDue(
    state: WeighInState,
    keepOpen: Boolean,
    resultMs: Long = CLOSE_AFTER_SAVED_MS,
    onSeconds: (Int) -> Unit = {},
    close: () -> Unit,
) {
    var left = autoCloseDelayMs(state, keepOpen, resultMs) ?: return
    while (left > 0) {
        onSeconds(((left + 999) / 1000).toInt())
        val step = if (left % 1000 == 0L) 1000L else left % 1000
        delay(step)
        left -= step
    }
    close()
}

/** Tapping outside the card and Back close the dialog, except while the weight is being saved. */
fun closesOnRequest(state: WeighInState): Boolean = state !is WeighInState.Saving

/**
 * The `weighin.end reason=` of a dialog left in [state] (everyday-feedback design 11).
 * [fromRecent]: the dialog showed the recent automatic weight, so Discarded is its Poista. A
 * Saving dialog cannot be closed by the user; if the system ends it the save still finishes in
 * the app scope, so it counts as saved.
 */
fun endReason(state: WeighInState, fromRecent: Boolean): String = when (state) {
    is WeighInState.Saved -> if (state.deleteFailed) "failed" else "saved"
    is WeighInState.Saving -> "saved"
    WeighInState.Discarded -> if (fromRecent) "recentdeleted" else "discarded"
    is WeighInState.Recent -> if (state.deleteFailed) "failed" else "recent"
    is WeighInState.NotFound -> state.reason.key
    WeighInState.Preparing, WeighInState.BluetoothOff, is WeighInState.Scanning, is WeighInState.Live -> "cancelled"
    WeighInState.NoHealthAccess, WeighInState.PermissionDenied, is WeighInState.ScanFailed, is WeighInState.SaveFailed -> "failed"
}

/**
 * True while the dialog could save the weighing itself, so the background saves nothing
 * (everyday-feedback design 10): checking access, scanning, a live weight, saving.
 */
fun blocksBackground(state: WeighInState): Boolean =
    state is WeighInState.Preparing || state is WeighInState.Scanning || state is WeighInState.Live || state is WeighInState.Saving
