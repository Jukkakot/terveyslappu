package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.chart.AxisTicks
import fi.jukkakot.terveyslappu.chart.ChartAxis
import fi.jukkakot.terveyslappu.chart.WeightScale
import fi.jukkakot.terveyslappu.food.EnergyDay
import fi.jukkakot.terveyslappu.food.EnergySeries
import fi.jukkakot.terveyslappu.weight.WeightEntry
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.max

/**
 * The Energia tab (weight-chart "Energia chart", "Energia statistics"): kcal bars with the treats'
 * share stacked, the 7-day average line, and under them the period's weights on the same days.
 */
@Composable
internal fun EnergyPage(series: EnergySeries, onOpenDay: (LocalDate) -> Unit) {
    Column(Modifier.fillMaxWidth().testTag("page-ENERGY")) {
        Box(
            Modifier
                .padding(start = 16.dp, end = 16.dp, top = 12.dp)
                .clip(groupShape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(12.dp),
        ) {
            if (series.bars.isEmpty()) {
                Text(
                    stringResource(R.string.energy_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth().height(180.dp).padding(16.dp).testTag("energyEmpty"),
                )
            } else {
                EnergyChart(series, onOpenDay)
            }
        }
        if (series.bars.isNotEmpty()) EnergyStatsTiles(series)
    }
}

@Composable
private fun EnergyChart(series: EnergySeries, onOpenDay: (LocalDate) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val food = colors.primary
    val treat = colors.tertiary
    val line = colors.onSurface
    val grid = colors.outlineVariant
    val days = (ChronoUnit.DAYS.between(series.from, series.to) + 1).toInt().coerceAtLeast(1)
    val top = max(series.bars.maxOf { it.total }, series.average.maxOfOrNull { it.kcal } ?: 0)
    val ticks = AxisTicks.bars(top.toDouble(), KCAL_STEP)
    val maxKcal = ticks.last()
    val description = stringResource(R.string.energy_chart_description, series.bars.size)
    val labels = ticks.map { stringResource(R.string.food_kcal, formatKcal(it)) }
    val labelWidth = axisLabelWidth(labels)
    Column(Modifier.fillMaxWidth().testTag("energyChart").semantics { contentDescription = description }) {
        Row(Modifier.fillMaxWidth()) {
            YAxisLabels(labels, listOf(0f, 0.5f, 1f), labelWidth, BAR_PLOT, "energyAxis")
            Canvas(
                Modifier.weight(1f).height(BAR_PLOT).pointerInput(series) {
                    detectTapGestures { offset ->
                        val index = (offset.x / size.width * days).toInt().coerceIn(0, days - 1)
                        val day = series.from.plusDays(index.toLong())
                        val logged: EnergyDay? = if (series.weekly) {
                            // A week's bar opens its latest logged day.
                            series.days.firstOrNull { it.day.with(java.time.DayOfWeek.MONDAY) == day.with(java.time.DayOfWeek.MONDAY) }
                        } else {
                            series.days.firstOrNull { it.day == day }
                        }
                        logged?.let { onOpenDay(it.day) }
                    }
                },
            ) {
                val axis = ChartAxis(series.from, series.to, series.weekly)
                fun y(kcal: Double) = size.height * (1 - (kcal / maxKcal)).toFloat()
                drawGuidelines(grid, ticks.map { y(it) })
                drawBars(axis, series.bars.map { BarSpec(it.from, it.days, it.total.toDouble(), it.treat.toDouble()) }, ::y, food, treat)
                if (series.average.size > 1) drawValueLine(axis, series.average.map { it.day to it.kcal.toDouble() }, ::y, line)
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
            Spacer(Modifier.width(labelWidth))
            DateAxis(series.from, series.to, "energyDates", Modifier.weight(1f))
        }
        ChartLegend(
            buildList {
                add(LegendEntry(stringResource(R.string.energy_legend_food), food))
                add(LegendEntry(stringResource(R.string.energy_legend_treat), treat))
                if (series.average.size > 1) add(LegendEntry(stringResource(R.string.energy_legend_average), line, line = true))
                if (series.weekly) add(LegendEntry(stringResource(R.string.legend_weekly)))
            },
            "energyLegend",
        )
    }
}

/** Three faint guidelines at [ys] (bottom, middle, top). */
internal fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGuidelines(color: Color, ys: List<Float>) {
    ys.forEachIndexed { i, y ->
        val yy = y.coerceIn(0f, size.height)
        drawLine(color, Offset(0f, yy), Offset(size.width, yy), strokeWidth = (if (i == 0) 1.dp else 0.5.dp).toPx())
    }
}

/** Height of a bar plot. */
internal val BAR_PLOT = 160.dp
private const val KCAL_STEP = 250.0

@Composable
private fun EnergyStatsTiles(series: EnergySeries) {
    val s = series.stats
    val tiles = listOf(
        R.string.energy_stat_average to (s.averageKcal?.let { stringResource(R.string.energy_per_day, formatKcal(it)) } ?: "–"),
        R.string.energy_stat_treats to (s.averageTreat?.let { stringResource(R.string.energy_per_day, formatKcal(it)) } ?: "–"),
        R.string.energy_stat_days to stringResource(R.string.energy_days, s.loggedDays, s.periodDays),
    )
    Row(Modifier.fillMaxWidth().padding(16.dp).testTag("energyStats"), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

/** One logged day of the Energia list: date, total and treat share; opens Ruoka on it. */
@Composable
internal fun EnergyDayRow(day: EnergyDay, today: LocalDate, onOpenDay: (LocalDate) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onOpenDay(day.day) }.heightIn(min = 52.dp).padding(horizontal = 16.dp, vertical = 8.dp).testTag("energyDay-${day.day}"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(dayTitle(day.day, today), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (day.treat > 0) {
            Text(stringResource(R.string.food_day_treats, formatKcal(day.treat)), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.size(12.dp))
        }
        Text(stringResource(R.string.food_kcal, formatKcal(day.total)), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}
