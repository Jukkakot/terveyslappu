package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.chart.AxisTicks
import java.time.LocalDate
import kotlin.math.roundToInt

/** Gap kept between date labels and the label column's right padding. */
private val LABEL_GAP = 8.dp
private val COLUMN_PAD = 6.dp

/** Height of the weight line under the bars (steps-source-and-axes design 8). */
internal val WEIGHT_STRIP = 80.dp

/** "12 000 askelta", "97,5 kg": a y value with at most one decimal. */
internal fun formatAxisKg(kg: Double): String {
    val tenths = Math.round(kg * 10)
    return if (tenths % 10 == 0L) "${tenths / 10}" else formatKg(tenths / 10.0)
}

/** The date label of [day]: "9.11." or, for a year or more, "9/2022". */
internal fun dateLabel(day: LocalDate, monthYear: Boolean): String = if (monthYear) formatMonthYear(day) else formatShortDay(day)

/** The shared label column's width: the widest of [labels] plus padding, measured once per tab. */
@Composable
internal fun axisLabelWidth(labels: List<String>): Dp {
    val measurer = rememberTextMeasurer()
    val style = MaterialTheme.typography.labelSmall
    val density = LocalDensity.current
    return remember(labels, style, density) {
        val px = labels.maxOfOrNull { measurer.measure(it, style).size.width } ?: 0
        with(density) { px.toDp() } + COLUMN_PAD
    }
}

/**
 * Y labels right-aligned in a column of [width] × [height], each vertically centred on its value's
 * guideline ([fractions] 0 = bottom, 1 = top) and clamped inside the plot.
 */
@Composable
internal fun YAxisLabels(labels: List<String>, fractions: List<Float>, width: Dp, height: Dp, tag: String) {
    val style = MaterialTheme.typography.labelSmall
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    Layout(
        content = { labels.forEachIndexed { i, text -> Text(text, style = style, color = color, maxLines = 1, softWrap = false, modifier = Modifier.testTag("$tag-$i")) } },
        modifier = Modifier.width(width).height(height),
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(Constraints()) }
        val w = constraints.maxWidth
        val h = constraints.maxHeight
        val pad = COLUMN_PAD.roundToPx()
        layout(w, h) {
            placeables.forEachIndexed { i, p ->
                val centre = (1f - fractions[i]) * h
                val y = (centre - p.height / 2f).roundToInt().coerceIn(0, (h - p.height).coerceAtLeast(0))
                p.place((w - pad - p.width).coerceAtLeast(0), y)
            }
        }
    }
}

/**
 * The date row under a plot (design 8): 3 to 5 dates evenly spread, as many as fit at the measured
 * label widths; the first start-aligned, the last end-aligned, the others centred on their day.
 */
@Composable
internal fun DateAxis(from: LocalDate, to: LocalDate, tag: String, modifier: Modifier = Modifier) {
    val style: TextStyle = MaterialTheme.typography.labelSmall
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    val measurer = rememberTextMeasurer()
    val monthYear = AxisTicks.monthYear(from, to)
    BoxWithConstraints(modifier.fillMaxWidth().testTag(tag)) {
        val widthPx = constraints.maxWidth.toFloat()
        val gap = with(LocalDensity.current) { LABEL_GAP.toPx() }
        val dates = remember(from, to, widthPx, style) {
            val measure = { day: LocalDate -> measurer.measure(dateLabel(day, monthYear), style).size.width.toFloat() }
            AxisTicks.dates(from, to, AxisTicks.fitCount(from, to, widthPx, gap, measure))
        }
        Layout(
            content = { dates.forEach { Text(dateLabel(it, monthYear), style = style, color = color, maxLines = 1, softWrap = false, modifier = Modifier.testTag("$tag-label")) } },
            modifier = Modifier.fillMaxWidth(),
        ) { measurables, constraints ->
            val placeables = measurables.map { it.measure(Constraints()) }
            val w = constraints.maxWidth
            val h = placeables.maxOfOrNull { it.height } ?: 0
            val bounds = AxisTicks.labelBounds(from, to, dates, w.toFloat()) { day -> placeables[dates.indexOf(day)].width.toFloat() }
            layout(w, h) {
                placeables.forEachIndexed { i, p -> p.place(bounds[i].start.roundToInt(), 0) }
            }
        }
    }
}

/** One legend entry: a bar swatch, a line, or plain text ("viikoittain"). */
data class LegendEntry(val label: String, val color: Color? = null, val line: Boolean = false)

/** The legend under a Historia chart (design 9); wraps on a narrow phone instead of cutting text. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ChartLegend(entries: List<LegendEntry>, tag: String, modifier: Modifier = Modifier) {
    val small = MaterialTheme.typography.labelSmall
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    FlowRow(
        modifier.fillMaxWidth().padding(top = 6.dp).testTag(tag),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        entries.forEach { entry ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.testTag("$tag-entry")) {
                when {
                    entry.color == null -> Unit
                    entry.line -> Box(Modifier.size(width = 12.dp, height = 2.dp).background(entry.color))
                    else -> Box(Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(entry.color))
                }
                Text(entry.label, style = small, color = muted)
            }
        }
    }
}
