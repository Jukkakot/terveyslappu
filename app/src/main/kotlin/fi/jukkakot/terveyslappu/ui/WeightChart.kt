package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.chart.AxisTicks
import fi.jukkakot.terveyslappu.chart.ChartAxis
import fi.jukkakot.terveyslappu.chart.ChartData
import fi.jukkakot.terveyslappu.chart.ChartDots
import fi.jukkakot.terveyslappu.chart.TabReadouts
import java.time.LocalDate
import java.time.ZoneId

/** A faint background band behind the line (BMI classes), [from] to [to] in y units. */
class ChartBand(val from: Double, val to: Double, val color: Color)

/** Height of the Paino and BMI plot. */
private val WEIGHT_PLOT = 160.dp

/**
 * The period's values as dots joined by one unbroken straight line (chart-gap-lines) on the shared
 * axes of the bar charts (steps-source-and-axes design 8); in a dense chart only the weights far
 * from both neighbours keep their dot. A tap, hold or scrub places the crosshair (weight-chart
 * "Crosshair on every Historia chart"): the day's last weight with its time in the readout and a
 * ring on its dot. [axisLabel] formats a y-axis value, [pointLabel] a dot's value; [bands] lie
 * behind the line.
 */
@Composable
fun WeightChart(
    data: ChartData,
    description: String,
    modifier: Modifier = Modifier,
    axisLabel: (Double) -> String = { "${formatAxisKg(it)} kg" },
    pointLabel: (Double) -> String = { "${formatKg(it)} kg" },
    bands: List<ChartBand> = emptyList(),
    legend: String = stringResource(R.string.legend_weight),
    crosshair: LocalDate? = null,
    onDay: (LocalDate?) -> Unit = {},
    zone: ZoneId = ZoneId.systemDefault(),
) {
    val colors = MaterialTheme.colorScheme
    val tone = colors.primary
    val grid = colors.outlineVariant
    val marker = colors.onSurface
    val yTicks = remember(data) { AxisTicks.line(data.minY, data.maxY) }
    val span = (data.maxY - data.minY).takeIf { it > 0 } ?: 1.0
    fun fraction(v: Double) = ((v - data.minY) / span).toFloat()
    val labels = yTicks.map(axisLabel)
    val labelWidth = axisLabelWidth(labels)
    val axis = remember(data) { ChartAxis(data.from, data.to) }
    // ChartData's x counts days from its origin; the axis starts at its from.
    val offset = remember(data) { AxisTicks.days(data.origin, data.from).toDouble() }
    ClearOffAxis(axis, crosshair) { onDay(null) }
    val chosen = remember(data, crosshair, zone) {
        crosshair?.let { day -> data.entries.indices.filter { data.entries[it].time.atZone(zone).toLocalDate() == day }.maxByOrNull { data.entries[it].time } }
    }
    val today = LocalDate.now(zone)
    Column(modifier.fillMaxWidth().testTag("chart").semantics { contentDescription = description }.crosshairActions(axis, crosshair) { onDay(it) }) {
        TabReadout(crosshair?.let { TabReadouts.weight(data, it, zone, today, pointLabel) }, onClear = { onDay(null) })
        RangeLine(data.from, data.to, "chartRange")
        Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
            YAxisLabels(labels, yTicks.map(::fraction), labelWidth, WEIGHT_PLOT, "weightAxis")
            Canvas(Modifier.weight(1f).height(WEIGHT_PLOT).clipToBounds().crosshairGestures(axis) { onDay(it) }.testTag("plot-weight")) {
                fun y(v: Double) = size.height * (1 - fraction(v))
                val dayW = axis.dayWidth(size.width)
                val at = data.xs.indices.map { Offset(((data.xs[it] - offset) * dayW).toFloat(), y(data.ys[it])) }
                bands.forEach { band ->
                    val lo = maxOf(band.from, data.minY)
                    val hi = minOf(band.to, data.maxY)
                    if (lo < hi) drawRect(band.color, Offset(0f, y(hi)), Size(size.width, y(lo) - y(hi)))
                }
                drawGuidelines(grid, yTicks.map(::y))
                if (at.size > 1) {
                    val path = Path()
                    at.forEachIndexed { i, p -> if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y) }
                    drawPath(path, tone, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                }
                val dense = !ChartDots.fit(at.size, size.width / density)
                at.forEachIndexed { i, p -> if (!dense || i in data.lone) drawCircle(tone, radius = 3.5.dp.toPx(), center = p) }
                chosen?.let { drawRing(at[it], tone) }
                crosshair?.let { drawCrosshair(axis, it, marker) }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
            Spacer(Modifier.width(labelWidth))
            DateAxis(data.from, data.to, "weightDates", Modifier.weight(1f))
        }
        ChartLegend(listOf(LegendEntry(legend, tone, line = true)), "chartLegend")
    }
}
