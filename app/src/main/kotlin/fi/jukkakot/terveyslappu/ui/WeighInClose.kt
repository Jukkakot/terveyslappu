package fi.jukkakot.terveyslappu.ui

import fi.jukkakot.terveyslappu.scale.WeighInState
import kotlinx.coroutines.delay

/** How long the saved result stays before the dialog closes by itself (scale-weigh-in). */
const val CLOSE_AFTER_SAVED_MS = 10_000L

/**
 * When the weigh-in dialog closes by itself: at once when discarded or nothing was found, after
 * [CLOSE_AFTER_SAVED_MS] on the saved result unless [keepOpen] (a tap on the card, or Poista,
 * whose delete must not be cut short); null means it stays.
 */
fun autoCloseDelayMs(state: WeighInState, keepOpen: Boolean): Long? = when {
    state is WeighInState.Discarded || state is WeighInState.NotFound -> 0L
    state is WeighInState.Saved && !keepOpen -> CLOSE_AFTER_SAVED_MS
    else -> null
}

/** Runs for one (state, keepOpen) pair, restarted whenever either changes, and closes when due. */
suspend fun closeWhenDue(state: WeighInState, keepOpen: Boolean, close: () -> Unit) {
    val ms = autoCloseDelayMs(state, keepOpen) ?: return
    if (ms > 0) delay(ms)
    close()
}

/** Tapping outside the card and Back close the dialog, except while the weight is being saved. */
fun closesOnRequest(state: WeighInState): Boolean = state !is WeighInState.Saving
