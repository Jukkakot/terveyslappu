package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.chart.AxisTicks
import fi.jukkakot.terveyslappu.chart.ChartAxis
import fi.jukkakot.terveyslappu.chart.TabCharts
import androidx.compose.ui.draw.clipToBounds
import fi.jukkakot.terveyslappu.steps.StepsAccess
import fi.jukkakot.terveyslappu.steps.StepsChoice
import fi.jukkakot.terveyslappu.steps.StepsSourceLabel
import fi.jukkakot.terveyslappu.steps.StepsDay
import fi.jukkakot.terveyslappu.steps.StepsSeries
import fi.jukkakot.terveyslappu.steps.StepsUiState
import java.text.NumberFormat
import java.time.LocalDate
import kotlin.math.max

/** What the steps card and the Askeleet tab can ask for; the activity wires these (steps design 5, 8). */
class StepsActions(
    /** Asks the steps permission (first time, or Kysy uudelleen). */
    val onGrant: () -> Unit = {},
    /** Health Connect's permission settings. */
    val onOpenSettings: () -> Unit = {},
    /** Historia on Askeleet (the card). */
    val onOpen: () -> Unit = {},
    /** The card's own period. */
    val onPeriod: (Long) -> Unit = {},
    /** A screen shows steps from this day (null: Kaikki): read them. */
    val onShown: (LocalDate?) -> Unit = {},
    /** The source line or "Vaihda lähde": Asetukset → Askeleet (steps-source-and-axes design 7). */
    val onOpenSource: () -> Unit = {},
    /** A source picked in Asetukset → Askeleet. */
    val onChoice: (StepsChoice) -> Unit = {},
    /** Asetukset → Askeleet entered / left. */
    val onSourceShown: () -> Unit = {},
    val onSourceHidden: () -> Unit = {},
    /** Asetukset → Askeleet: Poista of the imported Garmin history (Kumoa comes as a bar). */
    val onGarminDelete: () -> Unit = {},
)

/** "6 420": thousands with a narrow no-break space. */
fun formatSteps(steps: Long): String = NumberFormat.getIntegerInstance(java.util.Locale.forLanguageTag("fi")).format(steps)
    .replace(' ', ' ').replace(' ', ' ')

/**
 * The Askeleet tab (weight-chart "Askeleet chart" … "Askeleet list"): the reason without access,
 * otherwise bars of the days with steps, the 7-day line and the statistics.
 */
@Composable
internal fun StepsTab(steps: StepsUiState, series: StepsSeries, actions: StepsActions, crosshair: LocalDate? = null, onDay: (LocalDate?) -> Unit = {}) {
    when {
        steps.access != StepsAccess.GRANTED -> StepsReason(steps.access, actions)
        steps.days == null && steps.loadFailed -> Column {
            StepsBox { StepsMessage(stringResource(R.string.steps_read_failed), "stepsFailed") }
            StepsSourceLine(steps, actions)
        }
        steps.days == null -> Column {
            Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            StepsSourceLine(steps, actions)
        }
        else -> StepsPage(series, steps, actions, crosshair, onDay)
    }
}

/** The quiet source line under the chart (weight-chart "Askeleet source line"); opens the setting. */
@Composable
private fun StepsSourceLine(steps: StepsUiState, actions: StepsActions) {
    val colors = MaterialTheme.colorScheme
    val warn = (steps.source.label as? StepsSourceLabel.App)?.noRecent == true
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = actions.onOpenSource)
            .padding(horizontal = 16.dp)
            .testTag("stepsSource"),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(stepsSourceLineText(steps), style = MaterialTheme.typography.labelMedium, color = if (warn) colors.error else colors.onSurfaceVariant)
    }
}

@Composable
private fun StepsBox(content: @Composable () -> Unit) {
    Box(
        Modifier
            .padding(start = 16.dp, end = 16.dp, top = 12.dp)
            .clip(groupShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .fillMaxWidth(),
    ) { content() }
}

@Composable
private fun StepsMessage(text: String, tag: String) {
    Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth().padding(24.dp).testTag(tag))
}

/** Askeleet without access: the reason and its buttons (steps "Denied steps permission"); also Asetukset → Askeleet. */
@Composable
internal fun StepsReason(access: StepsAccess, actions: StepsActions) {
    StepsBox {
        Column(
            Modifier.fillMaxWidth().padding(24.dp).testTag("stepsReason"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (access) {
                StepsAccess.CHECKING -> CircularProgressIndicator()
                StepsAccess.UNAVAILABLE -> Text(stringResource(R.string.steps_unavailable), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                StepsAccess.NEEDS -> {
                    Text(stringResource(R.string.steps_reason), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                    TextButton(onClick = actions.onGrant, modifier = Modifier.testTag("stepsGrant")) { Text(stringResource(R.string.food_permission_grant)) }
                }
                StepsAccess.DENIED -> {
                    Text(stringResource(R.string.steps_denied_reason), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                    TextButton(onClick = actions.onGrant, modifier = Modifier.testTag("stepsAskAgain")) { Text(stringResource(R.string.steps_ask_again)) }
                    TextButton(onClick = actions.onOpenSettings, modifier = Modifier.testTag("stepsSettings")) { Text(stringResource(R.string.food_permission_settings)) }
                }
                StepsAccess.GRANTED -> Unit
            }
        }
    }
}

@Composable
private fun StepsPage(series: StepsSeries, steps: StepsUiState, actions: StepsActions, crosshair: LocalDate?, onDay: (LocalDate?) -> Unit) {
    Column(Modifier.fillMaxWidth().testTag("page-STEPS")) {
        StepsBox {
            val app = steps.source.appId
            if (series.bars.isEmpty() && app != null) {
                // One app as the source, chosen or picked: it has nothing in this period.
                Column(Modifier.fillMaxWidth().padding(24.dp).testTag("stepsEmpty"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.steps_empty_app, steps.sourceName ?: app), style = MaterialTheme.typography.bodyLarge)
                    TextButton(onClick = actions.onOpenSource, modifier = Modifier.testTag("stepsChangeSource")) { Text(stringResource(R.string.steps_change_source)) }
                }
            } else if (series.bars.isEmpty()) {
                StepsMessage(stringResource(R.string.steps_empty), "stepsEmpty")
            } else {
                Box(Modifier.padding(12.dp)) { StepsChart(series, crosshair, onDay) }
            }
        }
        StepsSourceLine(steps, actions)
        if (steps.stale) {
            Text(
                stringResource(R.string.steps_stale),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp).testTag("stepsStale"),
            )
        }
        if (series.bars.isNotEmpty()) StepsStatsTiles(series)
    }
}

@Composable
private fun StepsChart(series: StepsSeries, crosshair: LocalDate?, onDay: (LocalDate?) -> Unit) {
    val today = LocalDate.now()
    val spec = remember(series, today) { TabCharts.steps(series, today) }
    // The marker card (weight-chart "Crosshair on every Historia chart"): a day, or a week on weekly bars.
    Column(Modifier.fillMaxWidth()) {
        RangeLine(series.from, series.to, "stepsRange")
        HistoryChart(spec, crosshair, onDay, stringResource(R.string.steps_chart_description, series.bars.size), tag = "stepsChart", plotTag = { _, _ -> "stepsBars" })
    }
}

@Composable
private fun StepsStatsTiles(series: StepsSeries) {
    val s = series.stats
    val tiles = listOf(
        R.string.steps_stat_average to (s.averageSteps?.let { stringResource(R.string.steps_per_day, formatSteps(it)) } ?: "–"),
        R.string.steps_stat_days to stringResource(R.string.energy_days, s.stepDays, s.periodDays),
    )
    Row(Modifier.fillMaxWidth().padding(16.dp).testTag("stepsStats"), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        tiles.forEach { (label, value) ->
            Column(
                Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceContainer).padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(stringResource(label), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/** One day of the Askeleet list: date and steps; a row opens nothing. */
@Composable
internal fun StepsDayRow(day: StepsDay, today: LocalDate, depth: Int = 0) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(start = indent(depth), end = 16.dp, top = 8.dp, bottom = 8.dp).testTag("stepsDay-${day.day}"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(dayTitle(day.day, today), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(stringResource(R.string.steps_value, formatSteps(day.steps)), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}
