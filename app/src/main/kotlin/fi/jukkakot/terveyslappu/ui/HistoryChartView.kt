package fi.jukkakot.terveyslappu.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry as MpLegendEntry
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.renderer.YAxisRenderer
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler
import fi.jukkakot.terveyslappu.chart.AxisSpec
import fi.jukkakot.terveyslappu.chart.AxisTicks
import fi.jukkakot.terveyslappu.chart.ChartAxis
import fi.jukkakot.terveyslappu.chart.ChartPoint
import fi.jukkakot.terveyslappu.chart.ChartSpec
import fi.jukkakot.terveyslappu.chart.LegendForm
import fi.jukkakot.terveyslappu.chart.LegendItem
import fi.jukkakot.terveyslappu.chart.LegendLabel
import fi.jukkakot.terveyslappu.chart.PanelSpec
import fi.jukkakot.terveyslappu.chart.SeriesShape
import fi.jukkakot.terveyslappu.chart.ToneRef
import fi.jukkakot.terveyslappu.chart.ValueUnit
import fi.jukkakot.terveyslappu.derived.Tone
import java.time.LocalDate
import kotlin.math.roundToLong

/**
 * The MPAndroidChart side of a Historia chart (charts-on-mpandroidchart design 3, 4, 5): one
 * [CombinedChart] per panel, configured from a [PanelSpec]. Everything Compose-free lives here so
 * render tests can draw a bound view straight to a bitmap.
 */

/** A theme's colours as ARGB ints, resolved from the colour scheme and the palette. */
internal data class ChartColors(
    val primary: Int,
    val secondary: Int,
    val tertiary: Int,
    val muted: Int,
    val grid: Int,
    val onSurface: Int,
    val dark: Boolean,
) {
    fun of(tone: ToneRef): Int {
        val base = when (tone) {
            is ToneRef.Scheme -> when (tone.role) {
                ToneRef.Role.PRIMARY -> primary
                ToneRef.Role.SECONDARY -> secondary
                ToneRef.Role.TERTIARY -> tertiary
                ToneRef.Role.ON_SURFACE_VARIANT -> muted
            }
            is ToneRef.Palette -> ChartPalette.shades.getValue(tone.colour).let { if (dark) it.dark else it.light }.toArgbInt()
            is ToneRef.Band -> bandColour(tone.tone)
        }
        val alpha = (AndroidColor.alpha(base) * tone.alpha).toInt().coerceIn(0, 255)
        return AndroidColor.argb(alpha, AndroidColor.red(base), AndroidColor.green(base), AndroidColor.blue(base))
    }

    /** The BMI class tones, as `toneColor` (DerivedUi) gives them for text. */
    private fun bandColour(tone: Tone): Int = when (tone) {
        Tone.GREEN -> if (dark) 0xFF81C995.toInt() else 0xFF1E7B34.toInt()
        Tone.YELLOW -> if (dark) 0xFFFDD663.toInt() else 0xFF8A5A00.toInt()
        Tone.RED -> if (dark) 0xFFF28B82.toInt() else 0xFFB3261E.toInt()
    }
}

private fun androidx.compose.ui.graphics.Color.toArgbInt(): Int = android.graphics.Color.argb(
    (alpha * 255).toInt(), (red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt(),
)

/** The texts a chart needs: value labels with units and legend words (from string resources). */
internal class ChartTexts(val unit: (ValueUnit, Double) -> String, val legend: (LegendLabel) -> String)

/** The plain Finnish texts, for render tests without resources (the app passes the resource texts). */
internal val plainTexts = ChartTexts(
    unit = { unit, v ->
        when (unit) {
            ValueUnit.KG -> "${formatAxisKg(v)} kg"
            ValueUnit.BMI -> formatAxisKg(v)
            ValueUnit.KCAL -> "${formatKcal(v)} kcal"
            ValueUnit.STEPS -> "${formatSteps(v.toLong())} askelta"
        }
    },
    legend = {
        when (it) {
            LegendLabel.WEIGHT -> "paino"
            LegendLabel.BMI -> "BMI"
            LegendLabel.FOOD -> "syöty"
            LegendLabel.TREAT -> "herkut"
            LegendLabel.STEPS -> "askeleet"
            LegendLabel.BURN -> "kulutus"
            LegendLabel.REST -> "lepo"
            LegendLabel.ACTIVE -> "aktiivinen"
            LegendLabel.TOTAL -> "yhteensä"
            LegendLabel.AVERAGE -> "keskiarvo"
            LegendLabel.WEEKLY -> "viikoittain"
        }
    },
)

/** How one panel is drawn: its place in the stack and the text size (dp, already × font scale). */
internal data class PanelStyle(
    val colors: ChartColors,
    val texts: ChartTexts,
    val textDp: Float,
    /** The last panel draws the date labels. */
    val dates: Boolean,
    /** The library legend under this panel (single-panel charts). */
    val legend: Boolean,
    /**
     * A front page card (home-cards-on-chart-library design 3): no labels, legend, marker or
     * gestures; averages as dashed lines between the bars and the value lines; the card look of
     * [PanelSpec] (grid, baseline, insets); bars at least 1 dp wide.
     */
    val card: Boolean = false,
)

/** A card's dashed average: 1 dp, 4 dp dashes and gaps (as the cards drew it). */
private const val CARD_AVERAGE_DP = 1f
private const val CARD_DASH_DP = 4f
/** A card's thinnest bar and its baseline. */
private const val CARD_MIN_BAR_DP = 1f
private const val CARD_BASELINE_DP = 1f

/** Gap kept between neighbouring date labels (the same gap as before). */
private const val LABEL_GAP_DP = 8f

/** Room reserved for one row of date labels and one legend row, in multiples of the text size. */
internal fun datesRoomDp(textDp: Float) = textDp * 1.4f + 8f
internal fun legendRoomDp(textDp: Float) = textDp * 1.6f + 10f

/** A chart view that remembers what it shows and lets the Compose host pick values (design 5). */
@SuppressLint("ViewConstructor")
internal class HistoryCombinedChart(context: Context) : CombinedChart(context) {
    /** The highlight a touch at a pixel chooses; replaces the library's nearest-by-distance search. */
    var picker: ((Float, Float) -> Highlight?)? = null
    var onResized: (() -> Unit)? = null

    var spec: ChartSpec? = null
    var panel: PanelSpec? = null
    var style: PanelStyle? = null
    /** The axis last fitted: a new one resets the zoom. */
    var boundAxis: ChartAxis? = null
    /** For each series of [panel]: its (data index, data set index) in the combined data. */
    var seriesSets: List<Pair<Int, Int>> = emptyList()
    private var boundWidth = 0

    val bands: BandYAxisRenderer = BandYAxisRenderer(viewPortHandler, axisLeft, getTransformer(YAxis.AxisDependency.LEFT))

    init {
        rendererLeftYAxis = bands
        description.isEnabled = false
        noDataText = ""
        setBackgroundColor(AndroidColor.TRANSPARENT)
        isDrawGridBackgroundEnabled = false
        minOffset = 0f
        isHighlightPerTapEnabled = false
        isHighlightPerDragEnabled = true
        isDragDecelerationEnabled = false
        isScaleXEnabled = true
        isScaleYEnabled = false
        isPinchZoomEnabled = false
        isDoubleTapToZoomEnabled = true
        isDragEnabled = true
        isDragYEnabled = false
        isHighlightFullBarEnabled = true
        drawOrder = listOf(DrawOrder.BAR, DrawOrder.LINE)
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            isDrawGridLinesEnabled = false
            isDrawAxisLineEnabled = false
            yOffset = 6f
        }
        for (axis in listOf(axisLeft, axisRight)) axis.apply {
            isDrawAxisLineEnabled = false
            gridLineWidth = 0.5f
            xOffset = 6f
        }
        axisRight.isDrawGridLinesEnabled = false
        legend.apply {
            isWordWrapEnabled = true
            verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
            orientation = Legend.LegendOrientation.HORIZONTAL
            isDrawInsideEnabled = false
            xEntrySpace = 12f
            formToTextSpace = 6f
            yOffset = 6f
        }
    }

    /**
     * A front page card's chart (home-cards-on-chart-library design 3): touch fully off, so the
     * card's own tap, long-press into edit mode and drag get every event.
     */
    var card: Boolean = false
        set(value) {
            field = value
            if (value) {
                isTouchEnabled = false
                isHighlightPerDragEnabled = false
                isDoubleTapToZoomEnabled = false
                isDragEnabled = false
                isScaleXEnabled = false
                isClickable = false
                isLongClickable = false
                isFocusable = false
            }
        }

    private var downX = 0f
    private var downY = 0f
    private var downInPlot = false

    /**
     * A sideways drag that starts inside the plot belongs to the chart (review decision 6): the
     * view asks its parents not to intercept, so the Historia pager never takes it (the library
     * asks only when it pans or zooms). Vertical drags stay free for the list. A card's chart
     * takes no event at all: false at once, no disallow-intercept, so Compose keeps the gesture.
     */
    override fun dispatchTouchEvent(event: android.view.MotionEvent): Boolean {
        if (card) return false
        when (event.actionMasked) {
            android.view.MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                downInPlot = viewPortHandler.contentRect.contains(event.x, event.y)
            }
            android.view.MotionEvent.ACTION_MOVE -> {
                val dx = kotlin.math.abs(event.x - downX)
                val dy = kotlin.math.abs(event.y - downY)
                val slop = android.view.ViewConfiguration.get(context).scaledTouchSlop / 2f
                if ((downInPlot && dx > slop && dx >= dy) || event.pointerCount > 1) parent?.requestDisallowInterceptTouchEvent(true)
            }
        }
        return super.dispatchTouchEvent(event)
    }

    /** A card squeezed to no plot height (a small card with a long title) draws nothing (tasks 3.4). */
    override fun onDraw(canvas: Canvas) {
        if (card && viewPortHandler.contentHeight < 1f) return
        super.onDraw(canvas)
    }

    override fun getHighlightByTouchPoint(x: Float, y: Float): Highlight? {
        picker?.let { return it(x, y) }
        return super.getHighlightByTouchPoint(x, y)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w != boundWidth && w > 0) {
            boundWidth = w
            // Dots and date labels depend on the width: bind again.
            val s = spec
            val p = panel
            val st = style
            if (s != null && p != null && st != null) {
                spec = null
                bind(s, p, st)
            }
            onResized?.invoke()
        }
    }

    /**
     * Design 4 / review decision 3: a week labels every day (granularity 1, labelCount 7, not
     * forced); longer spans take the library's placement with the most labels (5, 4, 3) whose
     * measured texts keep [LABEL_GAP_DP] apart and stay inside the view.
     */
    fun fitDateLabels(axis: ChartAxis) {
        xAxis.isForceLabelsEnabled = false
        if (axis.days <= AxisTicks.WEEK_DAYS) {
            xAxis.labelCount = AxisTicks.WEEK_DAYS
            return
        }
        for (n in AxisTicks.MAX_DATES downTo AxisTicks.MIN_DATES) {
            xAxis.labelCount = n
            rendererXAxis.computeAxis(xAxis.axisMinimum, xAxis.axisMaximum, false)
            if (n == AxisTicks.MIN_DATES || datesFit(this)) return
        }
    }
}

/** Builds a chart entry carrying its day. */
private fun entry(p: ChartPoint): Entry<LocalDate> = Entry(p.x.toFloat(), p.y.toFloat(), null, p.day)

/**
 * Configures [chart] for one panel of [spec] (design 3). The data is rebuilt only when the spec or
 * style changed; the caller re-applies the chosen day's highlights after.
 */
internal fun HistoryCombinedChart.bind(spec: ChartSpec, panel: PanelSpec, style: PanelStyle) {
    if (this.spec === spec && this.panel == panel && this.style == style) return
    this.spec = spec
    this.panel = panel
    this.style = style
    val c = style.colors
    val px = { dp: Float -> Utils.convertDpToPixel(dp) }

    // Value axes: three forced labels with units, guidelines at them (design 3).
    fun YAxis.set(axis: AxisSpec?) {
        isEnabled = axis != null
        removeAllLimitLines()
        if (axis == null) return
        axisMinimum = axis.min.toFloat()
        axisMaximum = axis.max.toFloat()
        labelCount = 3
        isForceLabelsEnabled = true
        textSize = style.textDp
        textColor = axis.tone?.let(c::of) ?: c.muted
        gridColor = c.grid
        valueFormatter = IAxisValueFormatter { v, _ -> style.texts.unit(axis.unit, axis.value(v.toDouble())) }
        // A card keeps the scale (no auto-scale) but draws no labels (home-cards-on-chart-library design 3).
        isDrawLabelsEnabled = !style.card
    }
    axisLeft.set(panel.left)
    axisRight.set(panel.right)
    axisLeft.isDrawGridLinesEnabled = !style.card || panel.grid
    bands.bands = panel.bands.map { Triple(it.from.toFloat(), it.to.toFloat(), c.of(it.tone)) }

    // The averages: dashed limit lines over the data (library default), on the left axis; a card
    // draws them as data sets between the bars and the value lines (below).
    if (!style.card) panel.series.forEach { s ->
        val average = s.average ?: return@forEach
        axisLeft.addLimitLine(
            LimitLine(average.toFloat()).apply {
                lineWidth = 1f
                lineColor = c.of(s.averageTone)
                enableDashedLine(px(4f), px(4f), 0f)
            },
        )
    }

    // Dates: whole days, weekday / d.M. / M/yyyy by the visible span (design 4).
    val axis = spec.axis
    xAxis.apply {
        isEnabled = true
        isDrawLabelsEnabled = style.dates && !style.card
        axisMinimum = spec.xMin.toFloat()
        axisMaximum = spec.xMax.toFloat()
        granularity = if (axis.weekly) 7f else 1f
        isGranularityEnabled = true
        isAvoidFirstLastClippingEnabled = false
        textSize = style.textDp
        textColor = c.muted
        // A label that would be cut at the view's edge is left out (the library would shift it onto its neighbour).
        valueFormatter = IAxisValueFormatter { v, _ -> dateLabelAt(axis, v, visibleXRange).takeUnless { labelCut(v, it) } ?: "" }
    }
    // A feature card's 1 dp baseline in the bar tone. An own chart card's bottom guideline in the
    // grid colour is 0.5 dp: today's clipped 1 dp line showed only its upper half (tasks 3.4).
    val baseline = if (!style.card) null else panel.baseline?.let(c::of) ?: c.grid.takeIf { panel.grid }
    xAxis.isDrawAxisLineEnabled = baseline != null
    if (baseline != null) {
        xAxis.axisLineColor = baseline
        xAxis.axisLineWidth = if (panel.baseline != null) CARD_BASELINE_DP else CARD_BASELINE_DP / 2
    }
    if (style.card) {
        val inset = panel.insetDp
        setExtraOffsets(inset, inset, inset, inset)
    } else {
        setExtraOffsets(0f, 8f, 0f, if (style.dates) 2f else 8f)
    }

    legend.isEnabled = !style.card && style.legend && spec.legend.isNotEmpty()
    if (legend.isEnabled) {
        legend.textSize = style.textDp
        legend.textColor = c.muted
        legend.entries = spec.legend.map { it.toLibrary(c, style.texts) }
    }

    // Data sets: lines, then their dot sets; bars (design 3). A card puts its averages first, so
    // they draw over the bars and under the value lines (home-cards-on-chart-library design 3).
    val averages = mutableListOf<ILineDataSet<*>>()
    val lines = mutableListOf<ILineDataSet<*>>()
    val dots = mutableListOf<ILineDataSet<*>>()
    val bars = mutableListOf<IBarDataSet<*>>()
    val where = mutableListOf<Pair<Boolean, Int>>()
    var barWidth = 0.7f
    val viewWidth = if (width > 0) width else resources.displayMetrics.widthPixels
    // The plot's width: a card has no axis labels, only its insets.
    val plotWidthDp = viewWidth / resources.displayMetrics.density - (if (style.card) 2 * panel.insetDp else 0f)
    if (style.card) panel.series.forEach { s ->
        val average = s.average ?: return@forEach
        val y = average.toFloat()
        val points = listOf(Entry<LocalDate>(spec.xMin.toFloat(), y, null, null), Entry<LocalDate>(spec.xMax.toFloat(), y, null, null))
        averages += LineDataSet(points, "${s.id}-average").apply {
            axisDependency = YAxis.AxisDependency.LEFT
            color = c.of(s.averageTone)
            lineWidth = CARD_AVERAGE_DP
            enableDashedLine(px(CARD_DASH_DP), px(CARD_DASH_DP), 0f)
            isDrawCirclesEnabled = false
            isDrawValuesEnabled = false
            isHighlightEnabled = false
        }
    }
    panel.series.forEach { s ->
        val tone = c.of(s.tone)
        when (val shape = s.shape) {
            is SeriesShape.Line -> {
                where += true to averages.size + lines.size
                lines += LineDataSet(shape.points.map(::entry), s.id).apply {
                    axisDependency = YAxis.AxisDependency.LEFT
                    color = tone
                    lineWidth = shape.widthDp
                    isDrawCirclesEnabled = false
                    isDrawValuesEnabled = false
                    highlightColor = c.muted
                    highlightLineWidth = 1f
                    isHorizontalHighlightIndicatorEnabled = false
                    isHighlightEnabled = !style.card
                }
                val shown = shape.dots.shown(shape.points.size, plotWidthDp)
                dots += LineDataSet(shape.points.filterIndexed { k, _ -> k in shown }.map(::entry), "${s.id}-dots").apply {
                    axisDependency = YAxis.AxisDependency.LEFT
                    color = AndroidColor.TRANSPARENT
                    isDrawCirclesEnabled = true
                    isDrawCircleHoleEnabled = false
                    circleRadius = shape.dotDp
                    setCircleColors(tone)
                    isDrawValuesEnabled = false
                    isHighlightEnabled = false
                }
            }
            is SeriesShape.Bars -> {
                where += false to bars.size
                barWidth = (0.7 * shape.widthDays / shape.slots).toFloat()
                bars += barSet(s.id, shape.points.map { BarEntry(it.x.toFloat(), it.y.toFloat(), null, it.day) }, listOf(tone), c)
            }
            is SeriesShape.StackedBars -> {
                where += false to bars.size
                barWidth = (0.7 * shape.widthDays).toFloat()
                val entries = shape.points.map { p -> BarEntry(p.x.toFloat(), (p.parts ?: listOf(p.y)).map { it.toFloat() }, null, p.day) }
                bars += barSet(s.id, entries, shape.partTones.map(c::of), c)
            }
        }
    }
    if (style.card) {
        // Bars at least 1 dp wide (design 4); recomputed on the size change's rebind.
        val plotPx = px(plotWidthDp)
        val span = (spec.xMax - spec.xMin).toFloat().coerceAtLeast(1e-3f)
        if (width > 0 && plotPx > 0f) barWidth = maxOf(barWidth, px(CARD_MIN_BAR_DP) / (plotPx / span))
        bars.forEach { it.isHighlightEnabled = false }
    }
    val lineData = (averages + lines + dots).takeIf { it.isNotEmpty() }?.let { LineData(it) }
    val barData = bars.takeIf { it.isNotEmpty() }?.let { BarData(it).apply { this.barWidth = barWidth } }
    val combined = CombinedData().apply {
        this.lineData = lineData
        this.barData = barData
    }
    val lineIndex = 0
    val barIndex = if (lineData != null) 1 else 0
    seriesSets = where.map { (line, index) -> (if (line) lineIndex else barIndex) to index }
    data = combined
    if (boundAxis != axis) {
        boundAxis = axis
        fitScreen()
    }
    if (style.dates && width > 0) {
        fitDateLabels(axis)
        notifyDataSetChanged()
    }
    invalidate()
}

private fun barSet(id: String, entries: List<BarEntry<LocalDate>>, tones: List<Int>, c: ChartColors) = BarDataSet(entries, id).apply {
    axisDependency = YAxis.AxisDependency.LEFT
    colors = tones
    isDrawValuesEnabled = false
    highlightColor = c.onSurface
    highlightAlpha = 90
}

private fun LegendItem.toLibrary(c: ChartColors, texts: ChartTexts): MpLegendEntry {
    val colour = tone?.let(c::of) ?: AndroidColor.TRANSPARENT
    return when (form) {
        LegendForm.SQUARE -> MpLegendEntry(texts.legend(label), Legend.LegendForm.SQUARE, 9f, Float.NaN, null, colour)
        LegendForm.LINE -> MpLegendEntry(texts.legend(label), Legend.LegendForm.LINE, 12f, 2f, null, colour)
        LegendForm.DASHED -> MpLegendEntry(
            texts.legend(label), Legend.LegendForm.LINE, 12f, 1.5f,
            DashPathEffect(floatArrayOf(Utils.convertDpToPixel(3f), Utils.convertDpToPixel(2f)), 0f), colour,
        )
        LegendForm.NONE -> MpLegendEntry(texts.legend(label), Legend.LegendForm.NONE, 0f, Float.NaN, null, colour)
    }
}

/** The date label of x [v]: a weekday on a week, else "9.11." or, over a year visible, "9/2022". */
internal fun dateLabelAt(axis: ChartAxis, v: Float, visibleRange: Float): String {
    val index = v.toDouble().roundToLong().coerceIn(0L, axis.days - 1L)
    val day = axis.from.plusDays(index)
    return when {
        axis.days <= AxisTicks.WEEK_DAYS -> AxisTicks.weekday(day)
        visibleRange + 1e-3 >= AxisTicks.YEAR_DAYS -> formatMonthYear(day)
        else -> formatShortDay(day)
    }
}

private fun HistoryCombinedChart.labelPaint() = Paint(rendererXAxis.paintAxisLabels).apply { textSize = Utils.convertDpToPixel(xAxis.textSize) }

/** Whether [label] centred at x value [v] would run past the view's edges. */
private fun HistoryCombinedChart.labelCut(v: Float, label: String): Boolean {
    if (width <= 0) return false
    val px = getPixelForValues(v, 0f, YAxis.AxisDependency.LEFT).x.toFloat()
    val half = Utils.calcTextWidth(labelPaint(), label) / 2f
    return px - half < 0f || px + half > width
}

/** The drawn date labels' bounds in px, centred on their entries (empty labels left out). */
internal fun dateLabelRects(chart: HistoryCombinedChart): List<RectF> {
    val xAxis = chart.xAxis
    val vp = chart.viewPortHandler
    val paint = chart.labelPaint()
    val trans = chart.getTransformer(YAxis.AxisDependency.LEFT)
    val count = xAxis.entryCount
    val positions = FloatArray(count * 2)
    for (i in 0 until count) positions[i * 2] = xAxis.entries[i]
    trans.pointValuesToPixel(positions)
    val rects = mutableListOf<RectF>()
    for (i in 0 until count) {
        val x = positions[i * 2]
        if (!vp.isInBoundsX(x)) continue
        val label = xAxis.valueFormatter.getFormattedValue(xAxis.entries[i], xAxis)
        if (label.isEmpty()) continue
        val w = Utils.calcTextWidth(paint, label).toFloat()
        rects += RectF(x - w / 2, 0f, x + w / 2, 1f)
    }
    return rects
}

/** Whether the current date labels keep their gap and stay inside the view. */
internal fun datesFit(chart: HistoryCombinedChart): Boolean {
    val rects = dateLabelRects(chart)
    val gap = Utils.convertDpToPixel(LABEL_GAP_DP)
    if (rects.any { it.left < 0f || it.right > chart.width }) return false
    return rects.zipWithNext().all { (a, b) -> b.left - a.right >= gap }
}

/** The chosen key's highlights in this panel: the exact entry of each series (review decision 2). */
internal fun HistoryCombinedChart.highlightsFor(key: LocalDate?): List<Highlight> {
    val spec = spec ?: return emptyList()
    val panel = panel ?: return emptyList()
    if (key == null) return emptyList()
    return panel.pointsAt(key, spec.axis).mapNotNull { (i, p) ->
        val (dataIndex, setIndex) = seriesSets.getOrNull(i) ?: return@mapNotNull null
        Highlight(p.x.toFloat(), p.y.toFloat(), setIndex, dataIndex)
    }
}

/** The key a touch at ([x], [y]) chooses in this panel: inside the plot, the nearest value (review decision 5). */
internal fun HistoryCombinedChart.pickAt(x: Float, y: Float, maxDp: Float = MAX_PICK_DP): LocalDate? {
    val spec = spec ?: return null
    val panel = panel ?: return null
    val vp = viewPortHandler
    if (x < vp.contentLeft || x > vp.contentRight || y < vp.contentTop || y > vp.contentBottom) return null
    val value = getValuesByTouchPoint(x, y, YAxis.AxisDependency.LEFT).x
    val pxPerX = vp.contentWidth / visibleXRange.coerceAtLeast(1e-3f)
    val maxDx = Utils.convertDpToPixel(maxDp) / pxPerX
    return panel.pick(value, maxDx.toDouble(), spec.x)
}

/** How far from a line's point a tap still chooses it (review decision 5). */
internal const val MAX_PICK_DP = 24f

/**
 * The BMI bands (design 3): filled behind the grid lines and the data, clipped to the plot. Each
 * band is (from, to, ARGB) in axis units.
 */
internal class BandYAxisRenderer(viewPortHandler: ViewPortHandler, yAxis: YAxis, private val trans: Transformer) :
    YAxisRenderer(viewPortHandler, yAxis, trans) {
    var bands: List<Triple<Float, Float, Int>> = emptyList()
    private val fill = Paint().apply { style = Paint.Style.FILL }

    override fun renderGridLines(c: Canvas) {
        if (bands.isNotEmpty()) {
            val save = c.save()
            c.clipRect(viewPortHandler.contentRect)
            for ((from, to, colour) in bands) {
                val pts = floatArrayOf(0f, to, 0f, from)
                trans.pointValuesToPixel(pts)
                fill.color = colour
                c.drawRect(viewPortHandler.contentLeft, pts[1], viewPortHandler.contentRight, pts[3], fill)
            }
            c.restoreToCount(save)
        }
        super.renderGridLines(c)
    }
}
