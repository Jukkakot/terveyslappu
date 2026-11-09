package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import android.view.HapticFeedbackConstants
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import java.time.LocalDate
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.scale.WeighInState

/** What the dialog's buttons do; the activity wires them. */
class WeighInActions(
    val onClose: () -> Unit = {},
    val onRetryScan: () -> Unit = {},
    val onRetrySave: () -> Unit = {},
    val onOpenAppSettings: () -> Unit = {},
    val onEnableBluetooth: () -> Unit = {},
    val onOpenApp: () -> Unit = {},
    /** A tap on the card: the saved result stays open instead of closing by itself. */
    val onKeepOpen: () -> Unit = {},
    val onDiscard: () -> Unit = {},
)

/** The weigh-in card over a dimmed background; tapping outside closes it. */
@Composable
fun WeighInDialog(state: WeighInState, actions: WeighInActions) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = actions.onClose)
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                // Taps on the card itself must not close it; they keep the result open.
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = actions.onKeepOpen),
        ) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(stringResource(R.string.weighin_title), style = MaterialTheme.typography.titleLarge)
                Content(state, actions)
            }
        }
    }
}

@Composable
private fun Content(state: WeighInState, actions: WeighInActions) {
    when (state) {
        WeighInState.Preparing -> Progress(stringResource(R.string.weighin_preparing))
        WeighInState.Scanning -> Progress(stringResource(R.string.weighin_step_on))
        is WeighInState.Live -> {
            BigWeight(state.kg, dim = !state.stable)
            Message(stringResource(R.string.weighin_settling))
            Close(actions)
        }
        is WeighInState.Saving -> {
            BigWeight(state.kg)
            Progress(stringResource(R.string.weighin_saving))
        }
        is WeighInState.Saved -> {
            val view = LocalView.current
            LaunchedEffect(Unit) { view.performHapticFeedback(HapticFeedbackConstants.CONFIRM) }
            BigWeight(state.kg)
            state.change?.let { change ->
                Text(
                    stringResource(R.string.weighin_change, formatChange(change.deltaKg), formatDay(change.previousTime, LocalDate.now())),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.testTag("change"),
                )
            }
            Text(
                stringResource(R.string.weighin_saved),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("saved"),
            )
            if (state.deleteFailed) Message(stringResource(R.string.weighin_delete_failed))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = actions.onDiscard) { Text(stringResource(R.string.weighin_discard)) }
                Button(onClick = actions.onClose) { Text(stringResource(R.string.weighin_done)) }
            }
        }
        is WeighInState.SaveFailed -> {
            BigWeight(state.kg)
            Choice(stringResource(R.string.weighin_save_failed), stringResource(R.string.weighin_retry), actions.onRetrySave, actions)
        }
        is WeighInState.NotFound -> Choice(
            stringResource(if (state.heard) R.string.weighin_not_stable else R.string.weighin_not_found),
            stringResource(R.string.weighin_retry), actions.onRetryScan, actions,
        )
        is WeighInState.ScanFailed -> Choice(
            stringResource(R.string.weighin_scan_failed, state.code),
            stringResource(R.string.weighin_retry), actions.onRetryScan, actions,
        )
        WeighInState.PermissionDenied -> Choice(
            stringResource(R.string.weighin_permission_denied),
            stringResource(R.string.weighin_open_app_settings), actions.onOpenAppSettings, actions,
        )
        WeighInState.BluetoothOff -> Choice(
            stringResource(R.string.weighin_bluetooth_off),
            stringResource(R.string.weighin_enable_bluetooth), actions.onEnableBluetooth, actions,
        )
        WeighInState.Discarded -> Unit
        WeighInState.NoHealthAccess -> Choice(
            stringResource(R.string.weighin_no_health_access),
            stringResource(R.string.weighin_open_app), actions.onOpenApp, actions,
        )
    }
}

@Composable
private fun BigWeight(kg: Double, dim: Boolean = false) {
    Text(
        "${formatKg(kg)} kg",
        style = MaterialTheme.typography.displayMedium,
        fontWeight = FontWeight.SemiBold,
        color = if (dim) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.testTag("weight"),
    )
}

@Composable
private fun Progress(text: String) {
    CircularProgressIndicator(Modifier.size(40.dp))
    Message(text)
}

@Composable
private fun Message(text: String) {
    Text(text, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
}

@Composable
private fun Close(actions: WeighInActions) {
    TextButton(onClick = actions.onClose) { Text(stringResource(R.string.cancel)) }
}

@Composable
private fun Choice(text: String, button: String, onClick: () -> Unit, actions: WeighInActions) {
    Message(text)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = actions.onClose) { Text(stringResource(R.string.close)) }
        Button(onClick = onClick) { Text(button) }
    }
}
