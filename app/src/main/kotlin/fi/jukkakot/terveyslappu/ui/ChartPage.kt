package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.chart.ChartReadout
import fi.jukkakot.terveyslappu.chart.CustomChart
import fi.jukkakot.terveyslappu.chart.CustomChartData
import fi.jukkakot.terveyslappu.chart.Readout
import fi.jukkakot.terveyslappu.chart.SeriesKind
import fi.jukkakot.terveyslappu.steps.StepsAccess
import fi.jukkakot.terveyslappu.steps.StepsUiState
import java.time.LocalDate
import java.time.ZoneId

/** Whether a kind can be read (weights always, once Historia shows anything). */
internal fun chartAccess(kind: SeriesKind, food: FoodUiState, steps: StepsUiState): Boolean = when (kind) {
    SeriesKind.WEIGHT -> true
    SeriesKind.ENERGY -> food.access == FoodAccess.GRANTED
    SeriesKind.STEPS -> steps.access == StepsAccess.GRANTED
}

/** One chart's data from the screens' states; data without access or not loaded is null (design 7, 15). */
@Composable
internal fun rememberChartData(chart: CustomChart, state: MainUiState, food: FoodUiState, steps: StepsUiState, today: LocalDate, zone: ZoneId): CustomChartData {
    val foodItems = food.items.takeIf { food.access == FoodAccess.GRANTED }
    val stepDays = steps.days.takeIf { steps.access == StepsAccess.GRANTED }
    return remember(chart, state.weights, state.questioned, foodItems, stepDays, today) {
        CustomChartData.of(chart, today, zone, state.weights, foodItems, stepDays, state.questioned)
    }
}

/** Why each kind of a chart has nothing to draw (spec "Chart data without values"); the chart never asks by itself. */
@Composable
internal fun chartStates(food: FoodUiState, foodActions: FoodActions, steps: StepsUiState, stepsActions: StepsActions): Map<SeriesKind, PanelState> {
    val grant = stringResource(R.string.food_permission_grant)
    val energy = when (food.access) {
        FoodAccess.GRANTED -> if (food.items == null) PanelState.Loading else PanelState.Ready
        FoodAccess.CHECKING -> PanelState.Loading
        FoodAccess.UNAVAILABLE -> PanelState.Failed(stringResource(R.string.energy_reason))
        FoodAccess.NEEDS, FoodAccess.DENIED -> PanelState.Gated(stringResource(R.string.energy_reason), listOf(grant to foodActions.onGrant))
    }
    val stepsState = when (steps.access) {
        StepsAccess.GRANTED -> when {
            steps.days != null -> PanelState.Ready
            steps.loadFailed -> PanelState.Failed(stringResource(R.string.steps_read_failed))
            else -> PanelState.Loading
        }
        StepsAccess.CHECKING -> PanelState.Loading
        StepsAccess.UNAVAILABLE -> PanelState.Failed(stringResource(R.string.steps_unavailable))
        StepsAccess.NEEDS -> PanelState.Gated(stringResource(R.string.steps_reason), listOf(grant to stepsActions.onGrant))
        StepsAccess.DENIED -> PanelState.Gated(
            stringResource(R.string.steps_denied_reason),
            listOf(stringResource(R.string.steps_ask_again) to stepsActions.onGrant, stringResource(R.string.food_permission_settings) to stepsActions.onOpenSettings),
        )
    }
    return mapOf(SeriesKind.ENERGY to energy, SeriesKind.STEPS to stepsState)
}

/**
 * An own chart's Historia page (custom-charts design 11): Muokkaa, the readout slot and the chart; no
 * statistics and no list.
 */
@Composable
internal fun ChartPage(
    data: CustomChartData,
    states: Map<SeriesKind, PanelState>,
    access: (SeriesKind) -> Boolean,
    crosshair: LocalDate?,
    onDay: (LocalDate?) -> Unit,
    onEdit: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().testTag("page-chart-${data.chart.id}")) {
        Box(
            Modifier
                .padding(start = 16.dp, end = 16.dp, top = 12.dp)
                .clip(groupShape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
        ) {
            Column {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    ReadoutSlot(crosshair?.let { ChartReadout.of(it, data, access) }, onClear = { onDay(null) }, Modifier.weight(1f))
                    TextButton(onClick = onEdit, modifier = Modifier.testTag("chartEdit")) {
                        Icon(painterResource(R.drawable.ic_edit), contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.chart_edit))
                    }
                }
                CustomChartView(data, ChartMode.Tab, states, crosshair, onDay = { onDay(it) })
            }
        }
    }
}

/** The day's values over the chart, or the hint; keeps its height; a tap clears (design 10). */
@Composable
private fun ReadoutSlot(readout: Readout?, onClear: () -> Unit, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    val text = readout?.let { readoutText(it) } ?: stringResource(R.string.chart_hint)
    Box(
        modifier
            .heightIn(min = 48.dp)
            .then(if (readout != null) Modifier.clickable(onClickLabel = stringResource(R.string.chart_clear), onClick = onClear) else Modifier)
            .semantics { liveRegion = LiveRegionMode.Polite }
            .testTag("chartReadout"),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text,
            style = if (readout != null) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelMedium,
            color = if (readout != null) colors.onSurface else colors.onSurfaceVariant,
            modifier = Modifier.testTag(if (readout != null) "chartReadoutText" else "chartHint"),
        )
    }
}

/** "12.11. · 82,4 kg · 1 900 kcal · 6 420 askelta"; missing parts "ei painoa", "ei kirjauksia", "ei tietoa". */
@Composable
internal fun readoutText(readout: Readout): String {
    val parts = readout.values.map { (kind, value) ->
        if (value != null) {
            when (kind) {
                SeriesKind.WEIGHT -> stringResource(R.string.kg_value, formatKg(value.value))
                else -> valueText(kind, value.value)
            }
        } else {
            stringResource(
                when (kind) {
                    SeriesKind.WEIGHT -> R.string.chart_no_weight
                    SeriesKind.ENERGY -> R.string.chart_no_food
                    SeriesKind.STEPS -> R.string.chart_no_steps
                },
            )
        }
    }
    return (listOf(formatShortDay(readout.day)) + parts).joinToString(" · ")
}

/** The last tab: what own charts are and the Uusi kaavio button. */
@Composable
internal fun NewChartPage(onNew: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp)
            .clip(groupShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(24.dp)
            .testTag("page-newChart"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.chart_new_intro), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        Button(onClick = onNew, modifier = Modifier.testTag("chartNew")) {
            Icon(painterResource(R.drawable.ic_add), contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.chart_new))
        }
    }
}
