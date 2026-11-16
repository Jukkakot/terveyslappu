package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R

/**
 * "Päivitetäänkö aiemmat kirjaukset?" (food-log "Confirming a correction"): what a product's energy
 * correction would change, over the item or product editor. Back and a tap outside are Peruuta.
 */
@Composable
internal fun EnergyDialog(ask: EnergyAsk, onChoice: (EnergyChoice) -> Unit) {
    AlertDialog(
        onDismissRequest = { onChoice(EnergyChoice.CANCEL) },
        title = { Text(stringResource(R.string.food_energy_title)) },
        text = {
            Column(Modifier.testTag("energyDialog"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(
                        R.string.food_energy_line,
                        ask.name,
                        pluralStringResource(R.plurals.food_energy_items, ask.count, ask.count),
                        pluralStringResource(R.plurals.food_energy_days, ask.days, ask.days),
                        formatKcal(ask.oldSum),
                        formatKcal(ask.newSum),
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.testTag("energyLine"),
                )
                if (ask.kept > 0) {
                    Text(
                        pluralStringResource(R.plurals.food_energy_kept, ask.kept, ask.kept),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("energyKept"),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onChoice(EnergyChoice.ALL) }, modifier = Modifier.testTag("energyAll")) { Text(stringResource(R.string.food_energy_all)) }
        },
        dismissButton = {
            Row {
                TextButton(onClick = { onChoice(EnergyChoice.CANCEL) }, modifier = Modifier.testTag("energyCancel")) { Text(stringResource(R.string.cancel)) }
                TextButton(onClick = { onChoice(EnergyChoice.FUTURE) }, modifier = Modifier.testTag("energyFuture")) { Text(stringResource(R.string.food_energy_future)) }
            }
        },
    )
}
