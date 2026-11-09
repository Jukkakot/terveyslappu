package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.history.ImportedWeight
import fi.jukkakot.terveyslappu.history.ParsedHistory
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val importDate = DateTimeFormatter.ofPattern("d.M.yyyy", Locale.forLanguageTag("fi"))
private val importDateTime = DateTimeFormatter.ofPattern("d.M.yyyy H.mm", Locale.forLanguageTag("fi"))

/** History import: reading, preview, progress, failure (history-import spec). */
@Composable
fun ImportDialog(state: ImportState, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val writing = state is ImportState.Writing || state is ImportState.Reading
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = !writing, dismissOnClickOutside = !writing),
        title = { Text(stringResource(R.string.import_title)) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when (state) {
                    ImportState.Reading -> {
                        Text(stringResource(R.string.import_reading))
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    }
                    ImportState.ReadFailed -> Text(stringResource(R.string.import_read_failed))
                    is ImportState.Preview -> Preview(state.parsed)
                    is ImportState.Writing -> {
                        val total = state.parsed.weights.size
                        Text(stringResource(R.string.import_writing, state.saved, total))
                        LinearProgressIndicator(
                            progress = { if (total == 0) 0f else state.saved.toFloat() / total },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    is ImportState.Failed -> Text(stringResource(R.string.import_failed, state.saved, state.parsed.weights.size))
                }
            }
        },
        confirmButton = {
            when (state) {
                is ImportState.Preview -> if (state.parsed.weights.isNotEmpty()) {
                    Button(onClick = onConfirm, modifier = Modifier.testTag("importConfirm")) {
                        Text(stringResource(R.string.import_confirm, state.parsed.weights.size))
                    }
                }
                is ImportState.Failed -> Button(onClick = onConfirm) { Text(stringResource(R.string.import_retry)) }
                else -> {}
            }
        },
        dismissButton = {
            if (!writing) {
                val label = if (state is ImportState.Preview && state.parsed.weights.isNotEmpty()) R.string.cancel else R.string.close
                TextButton(onClick = onDismiss) { Text(stringResource(label)) }
            }
        },
    )
}

@Composable
private fun Preview(parsed: ParsedHistory) {
    val weights = parsed.weights
    if (weights.isEmpty()) {
        Text(stringResource(R.string.import_none))
    } else {
        val zone = ZoneId.systemDefault()
        Text(
            stringResource(
                R.string.import_found, weights.size,
                importDate.format(weights.first().time.atZone(zone)), importDate.format(weights.last().time.atZone(zone)),
            ),
            modifier = Modifier.testTag("importFound"),
        )
        Text(stringResource(R.string.import_examples), style = MaterialTheme.typography.labelLarge)
        val examples = if (weights.size <= 6) weights else weights.take(3) + weights.takeLast(3)
        for (w in examples) ExampleRow(w, zone)
        if (parsed.layout?.ambiguousOrder == true) {
            Text(stringResource(R.string.import_ambiguous), color = MaterialTheme.colorScheme.error)
        }
    }
    val counts = parsed.skippedCounts()
    if (counts.isNotEmpty()) {
        Text(stringResource(R.string.import_skipped, parsed.skipped.size), style = MaterialTheme.typography.labelLarge)
        for ((reason, count) in counts) Text(stringResource(R.string.import_skip_line, stringResource(reason.label()), count))
    }
    if (parsed.unknownKinds.isNotEmpty()) {
        Text(stringResource(R.string.import_unknown), modifier = Modifier.testTag("importUnknown"))
    }
    if (weights.isNotEmpty()) {
        Text(stringResource(R.string.import_note), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ExampleRow(weight: ImportedWeight, zone: ZoneId) {
    Text(
        stringResource(R.string.import_row, importDateTime.format(weight.time.atZone(zone)), formatKg(weight.kg)),
        style = MaterialTheme.typography.bodyMedium,
    )
}
