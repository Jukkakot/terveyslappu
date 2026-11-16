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

/** "12 000 askelta", "97,5 kg": a y value with at most one decimal. */
internal fun formatAxisKg(kg: Double): String {
    val tenths = Math.round(kg * 10)
    return if (tenths % 10 == 0L) "${tenths / 10}" else formatKg(tenths / 10.0)
}

/** The period's first and last day with years, at the top of a chart card (period-range-line). */
@Composable
internal fun RangeLine(from: LocalDate, to: LocalDate, tag: String, modifier: Modifier = Modifier) {
    Text(
        AxisTicks.rangeText(from, to),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth().padding(bottom = 4.dp).testTag(tag),
    )
}

/** One legend entry: a bar swatch, a line, or plain text ("viikoittain"). */
data class LegendEntry(val label: String, val color: Color? = null, val line: Boolean = false, val dashed: Boolean = false)

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
                    entry.dashed -> Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(2) { Box(Modifier.size(width = 5.dp, height = 2.dp).background(entry.color)) }
                    }
                    entry.line -> Box(Modifier.size(width = 12.dp, height = 2.dp).background(entry.color))
                    else -> Box(Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(entry.color))
                }
                Text(entry.label, style = small, color = muted)
            }
        }
    }
}
