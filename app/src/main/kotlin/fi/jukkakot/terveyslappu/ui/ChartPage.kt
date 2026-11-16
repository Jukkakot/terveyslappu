package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.chart.CustomChart
import fi.jukkakot.terveyslappu.chart.CustomChartData
import fi.jukkakot.terveyslappu.chart.SeriesKind
import fi.jukkakot.terveyslappu.chart.CustomChartSpec
import fi.jukkakot.terveyslappu.burn.BurnUiState
import fi.jukkakot.terveyslappu.steps.StepsAccess
import fi.jukkakot.terveyslappu.steps.StepsUiState
import java.time.LocalDate
import java.time.ZoneId

/** Whether a kind can be read (weights always, once Historia shows anything). */
internal fun chartAccess(kind: SeriesKind, food: FoodUiState, steps: StepsUiState, burn: BurnUiState = BurnUiState()): Boolean = when (kind) {
    SeriesKind.WEIGHT -> true
    SeriesKind.ENERGY -> food.access == FoodAccess.GRANTED
    SeriesKind.BURN -> burn.access == StepsAccess.GRANTED
    SeriesKind.STEPS -> steps.access == StepsAccess.GRANTED
}

/** One chart's data from the screens' states; data without access or not loaded is null (design 7, 15). */
@Composable
internal fun rememberChartData(chart: CustomChart, state: MainUiState, food: FoodUiState, steps: StepsUiState, today: LocalDate, zone: ZoneId, burn: BurnUiState = BurnUiState()): CustomChartData {
    val foodItems = food.items.takeIf { food.access == FoodAccess.GRANTED }
    val stepDays = steps.days.takeIf { steps.access == StepsAccess.GRANTED }
    val burnDays = burn.days.takeIf { burn.access == StepsAccess.GRANTED }
    return remember(chart, state.weights, state.questioned, foodItems, stepDays, burnDays, today) {
        CustomChartData.of(chart, today, zone, state.weights, foodItems, stepDays, state.questioned, burnDays)
    }
}

/** Why each kind of a chart has nothing to draw (spec "Chart data without values"); the chart never asks by itself. */
@Composable
internal fun chartStates(
    food: FoodUiState,
    foodActions: FoodActions,
    steps: StepsUiState,
    stepsActions: StepsActions,
    burn: BurnUiState = BurnUiState(),
    burnActions: BurnActions = BurnActions(),
): Map<SeriesKind, PanelState> {
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
    // Kulutus's panel button may ask its permissions too, as for steps (energy-burn design 9).
    val burnState = when (burn.access) {
        StepsAccess.GRANTED -> when {
            burn.days != null -> PanelState.Ready
            burn.loadFailed -> PanelState.Failed(stringResource(R.string.burn_read_failed))
            else -> PanelState.Loading
        }
        StepsAccess.CHECKING -> PanelState.Loading
        StepsAccess.UNAVAILABLE -> PanelState.Failed(stringResource(R.string.burn_unavailable))
        StepsAccess.NEEDS -> PanelState.Gated(stringResource(R.string.burn_reason), listOf(grant to burnActions.onGrant))
        StepsAccess.DENIED -> PanelState.Gated(
            stringResource(R.string.burn_denied_reason),
            listOf(stringResource(R.string.steps_ask_again) to burnActions.onGrant, stringResource(R.string.food_permission_settings) to burnActions.onOpenSettings),
        )
    }
    return mapOf(SeriesKind.ENERGY to energy, SeriesKind.BURN to burnState, SeriesKind.STEPS to stepsState)
}

/**
 * An own chart's Historia page (custom-charts design 11): the range line, Muokkaa and the chart on
 * MPAndroidChart (`CustomChartSpec`); no statistics and no list.
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
    val today = LocalDate.now()
    val spec = remember(data, today) { CustomChartSpec.of(data, today, access) }
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
                    if (spec.empty) Spacer(Modifier.weight(1f)) else RangeLine(data.axis.from, data.axis.to, "chartRange", Modifier.weight(1f))
                    TextButton(onClick = onEdit, modifier = Modifier.testTag("chartEdit")) {
                        Icon(painterResource(R.drawable.ic_edit), contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.chart_edit))
                    }
                }
                HistoryChart(
                    spec,
                    crosshair,
                    onDay,
                    stringResource(R.string.custom_chart_description, data.chart.name),
                    tag = "customChart-${data.chart.id}",
                    states = states,
                )
            }
        }
    }
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
