package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.steps.StepsAccess
import fi.jukkakot.terveyslappu.steps.StepsDay
import fi.jukkakot.terveyslappu.steps.StepsSeries
import fi.jukkakot.terveyslappu.steps.StepsUiState
import fi.jukkakot.terveyslappu.weight.WeightEntry
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlin.math.ceil
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
)

/** "6 420": thousands with a narrow no-break space. */
fun formatSteps(steps: Long): String = NumberFormat.getIntegerInstance(java.util.Locale.forLanguageTag("fi")).format(steps)
    .replace(' ', ' ').replace(' ', ' ')

/**
 * The Askeleet tab (weight-chart "Askeleet chart" … "Askeleet list"): the reason without access,
 * otherwise bars of the days with steps, the 7-day line, the weights under them and the statistics.
 */
@Composable
internal fun StepsTab(steps: StepsUiState, series: StepsSeries, weights: List<WeightEntry>, zone: ZoneId, actions: StepsActions) {
    when {
        steps.access != StepsAccess.GRANTED -> StepsReason(steps.access, actions)
        steps.days == null && steps.loadFailed -> StepsBox { StepsMessage(stringResource(R.string.steps_read_failed), "stepsFailed") }
        steps.days == null -> Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        else -> StepsPage(series, weights, zone, steps.stale)
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

/** Askeleet without access: the reason and its buttons (steps "Denied steps permission"). */
@Composable
private fun StepsReason(access: StepsAccess, actions: StepsActions) {
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
private fun StepsPage(series: StepsSeries, weights: List<WeightEntry>, zone: ZoneId, stale: Boolean) {
    Column(Modifier.fillMaxWidth().testTag("page-STEPS")) {
        StepsBox {
            if (series.bars.isEmpty()) {
                StepsMessage(stringResource(R.string.steps_empty), "stepsEmpty")
            } else {
                Box(Modifier.padding(12.dp)) { StepsChart(series, weights, zone) }
            }
        }
        if (stale) {
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
private fun StepsChart(series: StepsSeries, weights: List<WeightEntry>, zone: ZoneId) {
    val colors = MaterialTheme.colorScheme
    val barColor = colors.primary
    val line = colors.onSurface
    val grid = colors.outlineVariant
    val days = (ChronoUnit.DAYS.between(series.from, series.to) + 1).toInt().coerceAtLeast(1)
    val top = max(series.bars.maxOf { it.steps }, series.average.maxOfOrNull { it.steps } ?: 0L)
    val maxSteps = (ceil(top * 1.1 / 2000.0) * 2000).coerceAtLeast(2000.0)
    val description = stringResource(R.string.steps_chart_description, series.bars.size)
    val small = MaterialTheme.typography.labelSmall
    // A tapped bar shows its day (a week: its Monday) and steps (weight-chart "Day details").
    var picked by remember(series) { mutableStateOf<StepsDay?>(null) }
    Column(Modifier.fillMaxWidth().testTag("stepsChart").semantics { contentDescription = description }) {
        Row(Modifier.fillMaxWidth().clearAndSetSemantics {}) {
            Text(formatSteps(maxSteps.toLong()), style = small, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
            if (series.weekly) Text(stringResource(R.string.steps_weekly), style = small, color = colors.onSurfaceVariant)
        }
        Canvas(
            Modifier.fillMaxWidth().height(160.dp).testTag("stepsBars").pointerInput(series) {
                detectTapGestures { offset ->
                    val index = (offset.x / size.width * days).toInt().coerceIn(0, days - 1)
                    val day = series.from.plusDays(index.toLong())
                    picked = if (series.weekly) {
                        val monday = day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                        series.bars.firstOrNull { it.from == monday }?.let { StepsDay(it.from, it.steps) }
                    } else {
                        series.days.firstOrNull { it.day == day }
                    }
                }
            },
        ) {
            val dayW = size.width / days
            fun x(day: LocalDate) = ChronoUnit.DAYS.between(series.from, day) * dayW
            fun y(steps: Double) = size.height * (1 - (steps / maxSteps)).toFloat()
            drawLine(grid, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
            drawLine(grid, Offset(0f, y(maxSteps / 2)), Offset(size.width, y(maxSteps / 2)), strokeWidth = 0.5.dp.toPx())
            for (bar in series.bars) {
                val span = bar.days * dayW
                val left = x(bar.from).toFloat() + span * 0.15f
                val width = max(span * 0.7f, 1.dp.toPx())
                val barTop = y(bar.steps.toDouble())
                drawRect(barColor, Offset(left, barTop), Size(width, size.height - barTop))
            }
            if (series.average.size > 1) {
                val path = Path()
                series.average.forEachIndexed { i, p ->
                    val px = x(p.day).toFloat() + dayW / 2
                    val py = y(p.steps.toDouble())
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                drawPath(path, line, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(barColor))
            Text(stringResource(R.string.steps_legend), style = small, color = colors.onSurfaceVariant)
            if (series.average.size > 1) {
                Box(Modifier.size(width = 12.dp, height = 2.dp).background(line))
                Text(stringResource(R.string.energy_legend_average), style = small, color = colors.onSurfaceVariant)
            }
        }
        picked?.let {
            Text(
                stringResource(R.string.steps_point, formatShortDay(it.day), formatSteps(it.steps)),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 4.dp).testTag("stepsPicked"),
            )
        }
        PeriodWeights(series.from, series.to, weights, zone, "stepsWeights")
        Row(Modifier.fillMaxWidth().padding(top = 4.dp).clearAndSetSemantics {}) {
            Text(formatShortDay(series.from), style = small, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text(formatShortDay(series.to), style = small, color = colors.onSurfaceVariant)
        }
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
internal fun StepsDayRow(day: StepsDay, today: LocalDate) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(horizontal = 16.dp, vertical = 8.dp).testTag("stepsDay-${day.day}"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(dayTitle(day.day, today), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(stringResource(R.string.steps_value, formatSteps(day.steps)), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}
