package fi.jukkakot.terveyslappu.ui

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import fi.jukkakot.terveyslappu.a11y.ActionTimeouts
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** How long a message with Kumoa stays at the default accessibility timeout (app-appearance "Undo messages"). */
const val UNDO_MILLIS = ActionTimeouts.BAR_MS

/**
 * Shows one bar message (app-appearance "Undo messages"): a message with an action stays
 * [actionMillis], by default 4 s lengthened by Android's accessibility timeout (Material's Short
 * drops to indefinite under accessibility services, so a timer keeps it bounded); others keep
 * Material's Short. Whatever is shown first goes away.
 */
suspend fun SnackbarHostState.showTimed(message: String, actionLabel: String?, actionMillis: Long = ActionTimeouts.millis(UNDO_MILLIS)): SnackbarResult {
    currentSnackbarData?.dismiss()
    if (actionLabel == null) return showSnackbar(message, duration = SnackbarDuration.Short)
    return coroutineScope {
        val timer = launch {
            delay(actionMillis)
            currentSnackbarData?.takeIf { it.visuals.message == message }?.dismiss()
        }
        val result = showSnackbar(message, actionLabel, duration = SnackbarDuration.Indefinite)
        timer.cancel()
        result
    }
}

/** The app's bar host: every message can be swiped away sideways (nothing is undone). */
@Composable
fun AppSnackbarHost(host: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(host, modifier) { data ->
        key(data) {
            val swipe = rememberSwipeToDismissBoxState()
            SwipeToDismissBox(swipe, backgroundContent = {}, modifier = Modifier.testTag("snackbar"), onDismiss = { data.dismiss() }) {
                Snackbar(data)
            }
        }
    }
}
