package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.steps.GarminSummary
import fi.jukkakot.terveyslappu.steps.StepsAccess
import fi.jukkakot.terveyslappu.steps.StepsChoice
import fi.jukkakot.terveyslappu.steps.StepsSourceLabel
import fi.jukkakot.terveyslappu.steps.StepsSourceRow
import fi.jukkakot.terveyslappu.steps.StepsSourcesUi
import fi.jukkakot.terveyslappu.steps.StepsUiState

/** The Asetukset row's text for [line] (steps-source-and-axes design 7); [name] = the app in use. */
@Composable
internal fun stepsLineText(line: StepsLine, name: String?): String = when (line) {
    StepsLine.NoAccess -> stringResource(R.string.settings_steps_no_access)
    StepsLine.Unavailable -> stringResource(R.string.settings_steps_unavailable)
    StepsLine.Blank -> ""
    is StepsLine.Auto -> if (line.id == null) stringResource(R.string.settings_steps_auto_none) else stringResource(R.string.settings_steps_auto, name ?: line.id)
    is StepsLine.App -> if (line.noRecent) stringResource(R.string.settings_steps_no_recent, name ?: line.id) else name ?: line.id
    StepsLine.All -> stringResource(R.string.steps_source_all)
}

/**
 * Asetukset → Askeleet (steps "Steps source setting"): the reason without access, else "Lähde" as a
 * single choice of Automaattinen, the apps with steps in 30 days and Kaikki. Entering the
 * sub-screen asks for a check ([StepsActions.onSourceShown]); the permission is never asked on open.
 */
@Composable
internal fun StepsSourceContent(steps: StepsUiState, sources: StepsSourcesUi, actions: StepsActions) {
    DisposableEffect(Unit) {
        actions.onSourceShown()
        onDispose { actions.onSourceHidden() }
    }
    // The chosen source is Kulutus's too (energy-burn design 3).
    Text(
        stringResource(R.string.steps_source_burn),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).testTag("stepsSourceBurn"),
    )
    if (steps.access != StepsAccess.GRANTED) {
        StepsReason(steps.access, actions)
        return
    }
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().selectableGroup().padding(vertical = 4.dp).testTag("stepsSourceList")) {
        if (sources.checking) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CircularProgressIndicator(Modifier.size(18.dp).testTag("stepsSourceChecking"), strokeWidth = 2.dp)
                Text(stringResource(R.string.steps_source_checking), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
        } else if (sources.failed) {
            Text(
                stringResource(R.string.steps_source_failed),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.error,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).testTag("stepsSourceFailed"),
            )
        }
        val now = sources.pickName?.let { stringResource(R.string.steps_source_now, it) }
            ?: stringResource(R.string.steps_source_now, stringResource(R.string.steps_source_none_recent))
        ChoiceRow(stringResource(R.string.steps_source_auto), now, sources.choice == StepsChoice.Auto, "stepsSource-auto") { actions.onChoice(StepsChoice.Auto) }
        sourceRows(sources).forEach { row ->
            val detail = when {
                row.noRecent -> listOfNotNull(
                    stringResource(R.string.steps_source_none_recent),
                    row.lastSeen?.let { stringResource(R.string.steps_source_last_seen, formatShortDay(it)) },
                ).joinToString(" · ")
                row.average != null -> stringResource(R.string.steps_source_average, formatSteps(row.average))
                else -> null
            }
            val chosen = sources.choice == StepsChoice.App(row.id)
            ChoiceRow(row.name, detail, chosen, "stepsSource-${row.id}", warn = chosen && row.noRecent && sources.checked) { actions.onChoice(StepsChoice.App(row.id)) }
        }
        ChoiceRow(stringResource(R.string.steps_source_all), null, sources.choice == StepsChoice.All, "stepsSource-all") { actions.onChoice(StepsChoice.All) }
    }
}

/** The app rows: the checked list, or after a failure (or before the first check) only the chosen app. */
private fun sourceRows(sources: StepsSourcesUi): List<StepsSourceRow> {
    val chosen = (sources.choice as? StepsChoice.App)?.id
    if (sources.checked && !sources.failed) return sources.rows
    if (chosen == null) return emptyList()
    val known = sources.rows.firstOrNull { it.id == chosen }
    val noRecent = (sources.source.label as? StepsSourceLabel.App)?.noRecent == true
    // Unknown figures: total 1 only keeps the "no steps" line away until a check says so.
    return listOf(known ?: StepsSourceRow(chosen, sources.sourceName ?: chosen, if (noRecent) 0 else 1, null, null))
}

@Composable
private fun ChoiceRow(title: String, detail: String?, selected: Boolean, tag: String, warn: Boolean = false, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(selected = selected, onClick = null)
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (detail != null) Text(detail, style = MaterialTheme.typography.bodyMedium, color = if (warn) colors.error else colors.onSurfaceVariant)
        }
    }
}

/** The Askeleet source line's text (weight-chart "Askeleet source line"). */
@Composable
internal fun stepsSourceLineText(steps: StepsUiState): String {
    val name = steps.sourceName
    return when (val label = steps.source.label) {
        is StepsSourceLabel.Auto -> if (label.id == null) stringResource(R.string.steps_source_line_auto_all) else stringResource(R.string.steps_source_line_auto, name ?: label.id)
        is StepsSourceLabel.App -> stringResource(R.string.steps_source_line_app, name ?: label.id)
        StepsSourceLabel.All -> stringResource(R.string.steps_source_line_all)
    }
}

/**
 * Asetukset → Askeleet's "Garminin historia" (steps "Imported history in Asetukset"): the
 * imported range and day count with a red Poista, or the hint line; shown also without the steps permission.
 */
@Composable
internal fun GarminHistoryRow(garmin: GarminSummary?, actions: StepsActions) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(start = 16.dp, end = 8.dp, top = 6.dp, bottom = 6.dp).testTag("garminHistory"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.garmin_title), style = MaterialTheme.typography.bodyLarge)
            val detail = if (garmin == null) {
                stringResource(R.string.garmin_row_empty)
            } else {
                stringResource(R.string.garmin_row, formatFullDay(garmin.first), formatFullDay(garmin.last), formatSteps(garmin.count.toLong()))
            }
            Text(detail, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant, modifier = Modifier.testTag("garminHistoryLine"))
            if (garmin != null && !garmin.kcal) {
                Text(stringResource(R.string.garmin_row_no_kcal), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant, modifier = Modifier.testTag("garminNoKcal"))
            }
        }
        if (garmin != null) {
            TextButton(onClick = actions.onGarminDelete, colors = ButtonDefaults.textButtonColors(contentColor = colors.error), modifier = Modifier.testTag("garminDelete")) {
                Text(stringResource(R.string.garmin_delete))
            }
        }
    }
}

private val fullDay = java.time.format.DateTimeFormatter.ofPattern("d.M.yyyy")

private fun formatFullDay(day: java.time.LocalDate): String = fullDay.format(day)
