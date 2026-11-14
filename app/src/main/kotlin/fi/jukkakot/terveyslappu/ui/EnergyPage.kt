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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.chart.WeightScale
import fi.jukkakot.terveyslappu.food.EnergyDay
import fi.jukkakot.terveyslappu.food.EnergySeries
import fi.jukkakot.terveyslappu.weight.WeightEntry
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.ceil
import kotlin.math.max

/**
 * The Energia tab (weight-chart "Energia chart", "Energia statistics"): kcal bars with the treats'
 * share stacked, the 7-day average line, and under them the period's weights on the same days.
 */
@Composable
internal fun EnergyPage(series: EnergySeries, weights: List<WeightEntry>, zone: ZoneId, onOpenDay: (LocalDate) -> Unit) {
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
                EnergyChart(series, weights, zone, onOpenDay)
            }
        }
        if (series.bars.isNotEmpty()) EnergyStatsTiles(series)
    }
}

@Composable
private fun EnergyChart(series: EnergySeries, weights: List<WeightEntry>, zone: ZoneId, onOpenDay: (LocalDate) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val food = colors.primary
    val treat = colors.tertiary
    val line = colors.onSurface
    val grid = colors.outlineVariant
    val weightColor = colors.secondary
    val days = (ChronoUnit.DAYS.between(series.from, series.to) + 1).toInt().coerceAtLeast(1)
    val top = max(series.bars.maxOf { it.total }, series.average.maxOfOrNull { it.kcal } ?: 0)
    val maxKcal = (ceil(top * 1.1 / 500.0) * 500).coerceAtLeast(500.0)
    val description = stringResource(R.string.energy_chart_description, series.bars.size)
    val small = MaterialTheme.typography.labelSmall
    Column(Modifier.fillMaxWidth().testTag("energyChart").semantics { contentDescription = description }) {
        Row(Modifier.fillMaxWidth().clearAndSetSemantics {}) {
            Text(stringResource(R.string.food_kcal, formatKcal(maxKcal)), style = small, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
            if (series.weekly) Text(stringResource(R.string.energy_weekly), style = small, color = colors.onSurfaceVariant)
        }
        Canvas(
            Modifier.fillMaxWidth().height(160.dp).pointerInput(series) {
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
            val dayW = size.width / days
            fun x(day: LocalDate) = ChronoUnit.DAYS.between(series.from, day) * dayW
            fun y(kcal: Double) = size.height * (1 - (kcal / maxKcal)).toFloat()
            drawLine(grid, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
            drawLine(grid, Offset(0f, y(maxKcal / 2)), Offset(size.width, y(maxKcal / 2)), strokeWidth = 0.5.dp.toPx())
            for (bar in series.bars) {
                val span = bar.days * dayW
                val left = x(bar.from).toFloat() + span * 0.15f
                val width = max(span * 0.7f, 1.dp.toPx())
                val baseTop = y((bar.total - bar.treat).toDouble())
                drawRect(food, Offset(left, baseTop), Size(width, size.height - baseTop))
                if (bar.treat > 0) {
                    val treatTop = y(bar.total.toDouble())
                    drawRect(treat, Offset(left, treatTop), Size(width, baseTop - treatTop))
                }
            }
            if (series.average.size > 1) {
                val path = Path()
                series.average.forEachIndexed { i, p ->
                    val px = x(p.day).toFloat() + dayW / 2
                    val py = y(p.kcal.toDouble())
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                drawPath(path, line, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
            }
        }
        Legend(food, treat, line, series.average.size > 1)
        // The weights of the same days, on the same x positions, with the shared weight scale.
        val shown = weights.filter { val d = it.time.atZone(zone).toLocalDate(); d >= series.from && d <= series.to }.sortedBy { it.time }
        if (shown.isNotEmpty()) {
            val scale = WeightScale.of(shown.map { it.kg })
            Row(Modifier.fillMaxWidth().padding(top = 8.dp).clearAndSetSemantics {}) {
                Text("${formatKg(scale.max)} kg", style = small, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
            }
            Canvas(Modifier.fillMaxWidth().height(64.dp).testTag("energyWeights")) {
                val start = series.from.atStartOfDay(zone).toInstant().epochSecond
                val spanSeconds = days * 86_400f
                fun at(e: WeightEntry) = Offset(
                    size.width * ((e.time.epochSecond - start) / spanSeconds).coerceIn(0f, 1f),
                    size.height * ((scale.max - e.kg) / scale.span).toFloat(),
                )
                if (shown.size > 1) {
                    val path = Path()
                    shown.forEachIndexed { i, e -> at(e).let { p -> if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y) } }
                    drawPath(path, weightColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                }
                drawCircle(weightColor, radius = 3.dp.toPx(), center = at(shown.last()))
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 4.dp).clearAndSetSemantics {}) {
            Text(formatShortDay(series.from), style = small, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text(formatShortDay(series.to), style = small, color = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun Legend(food: Color, treat: Color, line: Color, average: Boolean) {
    val small = MaterialTheme.typography.labelSmall
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Swatch(food)
        Text(stringResource(R.string.energy_legend_food), style = small, color = muted)
        Swatch(treat)
        Text(stringResource(R.string.energy_legend_treat), style = small, color = muted)
        if (average) {
            Box(Modifier.size(width = 12.dp, height = 2.dp).background(line))
            Text(stringResource(R.string.energy_legend_average), style = small, color = muted)
        }
    }
}

@Composable
private fun Swatch(color: Color) {
    Box(Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(color))
}

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
