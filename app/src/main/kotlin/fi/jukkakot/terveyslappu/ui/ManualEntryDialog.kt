package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.weight.WeightInput
import fi.jukkakot.terveyslappu.weight.isInFuture
import fi.jukkakot.terveyslappu.weight.parseWeight
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

/** Lisää paino käsin (weight-entry): a dialog over Historia; stays open with an error on failure. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ManualEntryDialog(state: MainUiState, actions: MainActions) {
    BasicAlertDialog(onDismissRequest = actions.onCloseEntry) {
        Surface(
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) { ManualEntryContent(state, actions, autoFocus = true) }
    }
}

/**
 * The dialog's contents: weight, date and time, Cancel and Tallenna. Separate from the dialog window
 * because Robolectric never idles with a text field inside a dialog window; tests render this directly.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ManualEntryContent(state: MainUiState, actions: MainActions, autoFocus: Boolean = false) {
    val zone = ZoneId.systemDefault()
    val input = parseWeight(state.weightText)
    val shownTime = state.pickedTime ?: LocalDateTime.now()
    val future = state.pickedTime != null && isInFuture(state.pickedTime.atZone(zone).toInstant(), Instant.now())
    val canSave = input is WeightInput.Valid && !future && !state.saving
    var pickDate by remember { mutableStateOf(false) }
    var pickTime by remember { mutableStateOf(false) }
    val focus = remember { FocusRequester() }
    val error = when {
        input is WeightInput.Invalid || input is WeightInput.OutOfRange -> stringResource(R.string.weight_range)
        future -> stringResource(R.string.weight_future)
        else -> null
    }

    Column(
        Modifier.verticalScroll(rememberScrollState()).padding(24.dp).testTag("entryDialog"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(stringResource(R.string.entry_title), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = state.weightText,
            onValueChange = actions.onWeightText,
            label = { Text(stringResource(R.string.weight_label)) },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (canSave) actions.onSave() }),
            modifier = Modifier.fillMaxWidth().focusRequester(focus).testTag("weightField"),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { pickDate = true }, modifier = Modifier.weight(1f)) { Text(formatDate(shownTime)) }
            OutlinedButton(onClick = { pickTime = true }, modifier = Modifier.weight(1f)) { Text(formatTime(shownTime)) }
        }
        if (state.saveFailed) {
            Text(
                stringResource(R.string.entry_failed),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag("entryFailed"),
            )
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp, androidx.compose.ui.Alignment.End)) {
            TextButton(onClick = actions.onCloseEntry, enabled = !state.saving) { Text(stringResource(R.string.cancel)) }
            TextButton(onClick = actions.onSave, enabled = canSave, modifier = Modifier.testTag("save")) {
                if (state.saving) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) else Text(stringResource(R.string.weight_save))
            }
        }
    }
    // The keyboard opens with the dialog: typing the weight is the only thing to do.
    if (autoFocus) LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }

    if (pickDate) {
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = shownTime.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { pickDate = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        actions.onPickedTime(LocalDateTime.of(date, shownTime.toLocalTime()).withSecond(0).withNano(0))
                    }
                    pickDate = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { pickDate = false }) { Text(stringResource(R.string.cancel)) } },
        ) { DatePicker(state = dateState) }
    }

    if (pickTime) {
        val timeState = rememberTimePickerState(initialHour = shownTime.hour, initialMinute = shownTime.minute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { pickTime = false },
            confirmButton = {
                TextButton(onClick = {
                    actions.onPickedTime(LocalDateTime.of(shownTime.toLocalDate(), LocalTime.of(timeState.hour, timeState.minute)))
                    pickTime = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { pickTime = false }) { Text(stringResource(R.string.cancel)) } },
            text = { TimePicker(state = timeState) },
        )
    }
}
