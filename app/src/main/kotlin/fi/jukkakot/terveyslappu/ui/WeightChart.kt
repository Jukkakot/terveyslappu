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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.decoration.HorizontalBox
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerController
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.chart.AxisTicks
import fi.jukkakot.terveyslappu.chart.ChartData
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.rememberTextMeasurer
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.compose.common.Position
import fi.jukkakot.terveyslappu.chart.ChartDots

/** Screen padding, chart padding and the kg axis labels: the plot is about this much narrower than the screen. */
private const val CHART_INSET_DP = 80

/** No room before the first or after the last x: the plot starts right at the y-axis (bug 2026-11-09). */
private val NO_PADDING = CartesianLayerPadding(scalableStart = 0.dp, scalableEnd = 0.dp, unscalableStart = 0.dp, unscalableEnd = 0.dp)

/** Vico's x labels on fixed values: the bar charts' dates (design 8); no room kept at the ends. */
internal class FixedXPlacer(val values: List<Double>) : HorizontalAxis.ItemPlacer {
    override fun getShiftExtremeLabels(context: CartesianMeasuringContext) = true

    override fun getLabelValues(context: CartesianDrawingContext, visibleXRange: ClosedFloatingPointRange<Double>, fullXRange: ClosedFloatingPointRange<Double>, maxLabelWidth: Float) = values

    override fun getWidthMeasurementLabelValues(context: CartesianMeasuringContext, layerDimensions: CartesianLayerDimensions, fullXRange: ClosedFloatingPointRange<Double>) = values

    override fun getHeightMeasurementLabelValues(context: CartesianMeasuringContext, layerDimensions: CartesianLayerDimensions, fullXRange: ClosedFloatingPointRange<Double>, maxLabelWidth: Float) = values

    override fun getStartLayerMargin(context: CartesianMeasuringContext, layerDimensions: CartesianLayerDimensions, tickThickness: Float, maxLabelWidth: Float) = 0f

    override fun getEndLayerMargin(context: CartesianMeasuringContext, layerDimensions: CartesianLayerDimensions, tickThickness: Float, maxLabelWidth: Float) = 0f
}

/** Vico's y labels on the scale's bottom, middle and top; half a label of room keeps the ends inside. */
internal class FixedYPlacer(val values: List<Double>) : VerticalAxis.ItemPlacer {
    override fun getLabelValues(context: CartesianDrawingContext, axisHeight: Float, maxLabelHeight: Float, position: Axis.Position.Vertical) = values

    override fun getWidthMeasurementLabelValues(context: CartesianMeasuringContext, axisHeight: Float, maxLabelHeight: Float, position: Axis.Position.Vertical) = values

    override fun getHeightMeasurementLabelValues(context: CartesianMeasuringContext, position: Axis.Position.Vertical) = values

    override fun getTopLayerMargin(context: CartesianMeasuringContext, verticalLabelPosition: Position.Vertical, maxLabelHeight: Float, maxLineThickness: Float) = maxLabelHeight / 2

    override fun getBottomLayerMargin(context: CartesianMeasuringContext, verticalLabelPosition: Position.Vertical, maxLabelHeight: Float, maxLineThickness: Float) = maxLabelHeight / 2
}

/** A faint background band behind the line (BMI classes), [from] to [to] in y units. */
class ChartBand(val from: Double, val to: Double, val color: Color)

/**
 * The period's values as dots joined by a line; a tap shows a dot's date, time and value.
 * [axisLabel] formats a y-axis value, [pointLabel] a dot's value; [bands] lie behind the line.
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
) {
    val colors = MaterialTheme.colorScheme
    // The shared axis rule (steps-source-and-axes design 8): three y labels, 3–5 dates on the bar charts' days.
    val yTicks = remember(data) { AxisTicks.line(data.minY, data.maxY) }
    val measurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall
    val density = LocalDensity.current
    val widthDp = LocalConfiguration.current.screenWidthDp - CHART_INSET_DP
    val count = remember(data, labelStyle, density, widthDp, axisLabel) {
        val yWidth = yTicks.maxOf { measurer.measure(axisLabel(it), labelStyle).size.width }
        val plot = with(density) { widthDp.dp.toPx() } - yWidth
        AxisTicks.fitCount(data.from, data.to, plot, with(density) { 8.dp.toPx() }) {
            measurer.measure(dateLabel(it, data.overYear), labelStyle).size.width.toFloat()
        }
    }
    val xPlacer = remember(data, count) { FixedXPlacer(data.labelXs(count)) }
    val yPlacer = remember(yTicks) { FixedYPlacer(yTicks) }
    ProvideVicoTheme(rememberM3VicoTheme(lineColor = colors.outlineVariant, textColor = colors.onSurfaceVariant)) {
        val dot = rememberShapeComponent(fill = Fill(colors.primary), shape = CircleShape)
        val fill = remember(colors.primary) { LineCartesianLayer.LineFill.single(Fill(colors.primary)) }
        // One unbroken straight line through every weight, however far apart (chart-gap-lines);
        // in a dense chart only the weights far from both neighbours keep their dot.
        val widthDp = LocalConfiguration.current.screenWidthDp - CHART_INSET_DP
        val dots = remember(data, dot, widthDp) {
            val dense = !ChartDots.fit(data.xs.size, widthDp.toFloat())
            val point = LineCartesianLayer.Point(dot, 7.dp)
            val loneXs = data.lone.map { data.xs[it] }.toSet()
            object : LineCartesianLayer.PointProvider {
                override fun getPoint(entry: LineCartesianLayerModel.Entry, seriesIndex: Int, extraStore: ExtraStore) =
                    if (!dense || entry.x in loneXs) point else null
                override fun getLargestPoint(extraStore: ExtraStore) = point
            }
        }
        val line = LineCartesianLayer.rememberLine(fill = fill, pointProvider = dots)
        val layer = rememberLineCartesianLayer(
            lineProvider = remember(line) { LineCartesianLayer.LineProvider.series(line) },
            rangeProvider = remember(data) { CartesianLayerRangeProvider.fixed(minX = 0.0, maxX = data.maxX, minY = data.minY, maxY = data.maxY) },
        )
        val dayLabels = remember(data) { CartesianValueFormatter { _, x, _ -> dateLabel(data.dayAt(x - 0.5), data.overYear) } }
        val yLabels = remember(axisLabel) { CartesianValueFormatter { _, y, _ -> axisLabel(y) } }
        val markerLabel = rememberTextComponent(
            style = TextStyle(color = colors.onSurface, fontSize = 14.sp),
            padding = Insets(8.dp, 4.dp),
            background = rememberShapeComponent(fill = Fill(colors.surfaceContainerHighest), shape = RoundedCornerShape(8.dp)),
        )
        val markerText = remember(data, pointLabel) {
            DefaultCartesianMarker.ValueFormatter { _, targets ->
                targets.firstOrNull()?.let { data.indexAt(it.x) }?.let { formatPoint(data.entries[it].time, pointLabel(data.ys[it])) } ?: ""
            }
        }
        val bandBoxes = bands.map { band -> band to rememberShapeComponent(fill = Fill(band.color)) }
        val decorations = remember(data, bandBoxes) {
            bandBoxes.mapNotNull { (band, box) ->
                val lo = maxOf(band.from, data.minY)
                val hi = minOf(band.to, data.maxY)
                if (lo < hi) HorizontalBox(y = { lo..hi }, box = box) else null
            }
        }
        val chart = rememberCartesianChart(
            layer,
            startAxis = VerticalAxis.rememberStart(valueFormatter = yLabels, itemPlacer = yPlacer),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = dayLabels,
                guideline = null,
                // No room kept for the first and last label, so the plot is flush with the y-axis.
                itemPlacer = xPlacer,
            ),
            marker = rememberDefaultCartesianMarker(label = markerLabel, valueFormatter = markerText),
            layerPadding = { NO_PADDING },
            decorations = decorations,
            getXStep = { _, _, _ -> 1.0 },
            markerController = CartesianMarkerController.rememberToggleOnTap(),
        )
        val model = remember(data) { CartesianChartModel(LineCartesianLayerModel.build {
            series(data.xs, data.ys)
        }) }
        Column(modifier.fillMaxWidth()) {
            Box(Modifier.fillMaxWidth().testTag("chart").semantics { contentDescription = description }) {
                CartesianChartHost(
                    chart = chart,
                    model = model,
                    modifier = Modifier.fillMaxWidth().height(180.dp).padding(start = 8.dp, end = 8.dp),
                    scrollState = rememberVicoScrollState(scrollEnabled = false),
                    zoomState = rememberVicoZoomState(zoomEnabled = false, initialZoom = Zoom.Content),
                )
            }
            ChartLegend(listOf(LegendEntry(legend, colors.primary, line = true)), "chartLegend", Modifier.padding(start = 8.dp))
        }
    }
}
