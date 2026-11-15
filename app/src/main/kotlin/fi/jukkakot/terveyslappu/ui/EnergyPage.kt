package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.chart.AxisTicks
import fi.jukkakot.terveyslappu.chart.ChartAxis
import fi.jukkakot.terveyslappu.chart.TabReadouts
import fi.jukkakot.terveyslappu.food.EnergyDay
import fi.jukkakot.terveyslappu.food.EnergySeries
import java.time.LocalDate
import kotlin.math.max

/**
 * The Energia tab (weight-chart "Energia chart", "Energia statistics"): kcal bars with the treats'
 * share stacked, the 7-day average line, and under them the period's weights on the same days.
 */
@Composable
internal fun EnergyPage(series: EnergySeries, crosshair: LocalDate? = null, onDay: (LocalDate?) -> Unit = {}, onOpenDay: (LocalDate) -> Unit) {
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
                EnergyChart(series, crosshair, onDay, onOpenDay)
            }
        }
        if (series.bars.isNotEmpty()) EnergyStatsTiles(series)
    }
}

@Composable
private fun EnergyChart(series: EnergySeries, crosshair: LocalDate?, onDay: (LocalDate?) -> Unit, onOpenDay: (LocalDate) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val food = colors.primary
    val treat = colors.tertiary
    val line = colors.onSurfaceVariant
    val grid = colors.outlineVariant
    val marker = colors.onSurface
    val top = max(series.bars.maxOf { it.total }, series.stats.averageKcal ?: 0)
    val ticks = AxisTicks.bars(top.toDouble(), KCAL_STEP)
    val maxKcal = ticks.last()
    val description = stringResource(R.string.energy_chart_description, series.bars.size)
    val labels = ticks.map { stringResource(R.string.food_kcal, formatKcal(it)) }
    val labelWidth = axisLabelWidth(labels)
    val axis = remember(series) { ChartAxis(series.from, series.to, series.weekly) }
    ClearOffAxis(axis, crosshair) { onDay(null) }
    // The crosshair (weight-chart "Crosshair on every Historia chart"); a day's readout ends with Avaa päivä (food-log).
    val openDay = stringResource(R.string.chart_open_day)
    val action = crosshair?.takeIf { !series.weekly }?.let { day -> openDay to { onOpenDay(day) } }
    Column(Modifier.fillMaxWidth().testTag("energyChart").semantics { contentDescription = description }.crosshairActions(axis, crosshair) { onDay(it) }) {
        TabReadout(crosshair?.let { TabReadouts.energy(series, it, LocalDate.now()) }, onClear = { onDay(null) }, action = action)
        RangeLine(series.from, series.to, "energyRange")
        Row(Modifier.fillMaxWidth()) {
            YAxisLabels(labels, listOf(0f, 0.5f, 1f), labelWidth, BAR_PLOT, "energyAxis")
            Canvas(Modifier.weight(1f).height(BAR_PLOT).clipToBounds().crosshairGestures(axis) { onDay(it) }.testTag("energyBars")) {
                fun y(kcal: Double) = size.height * (1 - (kcal / maxKcal)).toFloat()
                drawGuidelines(grid, ticks.map { y(it) })
                drawBars(axis, series.bars.map { BarSpec(it.from, it.days, it.total.toDouble(), it.treat.toDouble()) }, ::y, food, treat, chosen = crosshair)
                series.stats.averageKcal?.let { drawAverageLine(y(it.toDouble()), line) }
                crosshair?.let { drawCrosshair(axis, it, marker) }
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
                if (series.stats.averageKcal != null) add(LegendEntry(stringResource(R.string.energy_legend_average), line, dashed = true))
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
internal fun EnergyDayRow(day: EnergyDay, today: LocalDate, depth: Int = 0, onOpenDay: (LocalDate) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onOpenDay(day.day) }.heightIn(min = 56.dp).padding(start = indent(depth), end = 16.dp, top = 8.dp, bottom = 8.dp).testTag("energyDay-${day.day}"),
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
