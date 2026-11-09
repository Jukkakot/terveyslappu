package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerController
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import fi.jukkakot.terveyslappu.chart.ChartData

/** Above this many weights the dots are left out and only the line is drawn (dense "Kaikki"). */
private const val MAX_DOTS = 120

/** The period's weights as dots joined by a line; a tap shows a dot's date, time and weight. */
@Composable
fun WeightChart(data: ChartData, description: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    ProvideVicoTheme(rememberM3VicoTheme(lineColor = colors.outlineVariant, textColor = colors.onSurfaceVariant)) {
        val dot = rememberShapeComponent(fill = Fill(colors.primary), shape = CircleShape)
        val fill = remember(colors.primary) { LineCartesianLayer.LineFill.single(Fill(colors.primary)) }
        val dots = remember(dot) { LineCartesianLayer.PointProvider.single(LineCartesianLayer.Point(dot, 7.dp)) }
        val dotted = LineCartesianLayer.rememberLine(fill = fill, pointProvider = dots)
        val plain = LineCartesianLayer.rememberLine(fill = fill, pointProvider = null)
        // One series per segment; a lone weight between gaps keeps its dot even in a dense chart.
        val lines = remember(data, dotted, plain) {
            val dense = data.xs.size > MAX_DOTS
            data.segments.map { if (!dense || it.first == it.last) dotted else plain }
        }
        val layer = rememberLineCartesianLayer(
            lineProvider = remember(lines) { LineCartesianLayer.LineProvider.series(lines) },
            rangeProvider = remember(data) { CartesianLayerRangeProvider.fixed(minX = 0.0, maxX = data.maxX, minY = data.minY, maxY = data.maxY) },
        )
        val dayLabels = remember(data) { CartesianValueFormatter { _, x, _ -> if (data.overYear) formatMonthYear(data.dayAt(x)) else formatShortDay(data.dayAt(x)) } }
        val kgLabels = remember { CartesianValueFormatter { _, y, _ -> "%.0f".format(y) } }
        val markerLabel = rememberTextComponent(
            style = TextStyle(color = colors.onSurface, fontSize = 14.sp),
            padding = Insets(8.dp, 4.dp),
            background = rememberShapeComponent(fill = Fill(colors.surfaceContainerHighest), shape = RoundedCornerShape(8.dp)),
        )
        val markerText = remember(data) {
            DefaultCartesianMarker.ValueFormatter { _, targets ->
                targets.firstOrNull()?.let { data.entryAt(it.x) }?.let { formatPoint(it.time, it.kg) } ?: ""
            }
        }
        val chart = rememberCartesianChart(
            layer,
            startAxis = VerticalAxis.rememberStart(valueFormatter = kgLabels),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = dayLabels,
                guideline = null,
                itemPlacer = remember(data) { HorizontalAxis.ItemPlacer.aligned(spacing = { data.labelSpacing }) },
            ),
            marker = rememberDefaultCartesianMarker(label = markerLabel, valueFormatter = markerText),
            getXStep = { _, _, _ -> 1.0 },
            markerController = CartesianMarkerController.rememberToggleOnTap(),
        )
        val model = remember(data) { CartesianChartModel(LineCartesianLayerModel.build {
            data.segments.forEach { seg -> series(data.xs.slice(seg), data.entries.slice(seg).map { it.kg }) }
        }) }
        Box(modifier.fillMaxWidth().testTag("chart").semantics { contentDescription = description }) {
            CartesianChartHost(
                chart = chart,
                model = model,
                modifier = Modifier.fillMaxWidth().height(180.dp).padding(start = 8.dp, end = 8.dp),
                scrollState = rememberVicoScrollState(scrollEnabled = false),
                zoomState = rememberVicoZoomState(zoomEnabled = false, initialZoom = Zoom.Content),
            )
        }
    }
}
