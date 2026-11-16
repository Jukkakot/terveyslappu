package fi.jukkakot.terveyslappu.ui

import android.graphics.Matrix
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.chart.ChartSpec
import fi.jukkakot.terveyslappu.chart.LegendForm
import fi.jukkakot.terveyslappu.chart.LegendLabel
import fi.jukkakot.terveyslappu.chart.PanelSpec
import fi.jukkakot.terveyslappu.chart.ReadoutCard
import fi.jukkakot.terveyslappu.chart.SeriesKind
import fi.jukkakot.terveyslappu.chart.SeriesShape
import fi.jukkakot.terveyslappu.chart.ValueUnit
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.roundToInt

/** The colours of the current theme for the chart views. */
@Composable
internal fun rememberChartColors(): ChartColors {
    val c = MaterialTheme.colorScheme
    val dark = c.surface.run { (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f }
    return remember(c) {
        ChartColors(c.primary.toArgb(), c.secondary.toArgb(), c.tertiary.toArgb(), c.onSurfaceVariant.toArgb(), c.outlineVariant.toArgb(), c.onSurface.toArgb(), dark)
    }
}

/** Value and legend texts from the string resources. */
@Composable
internal fun rememberChartTexts(): ChartTexts {
    val context = LocalContext.current
    return remember(context) {
        val r = context.resources
        ChartTexts(
            unit = { unit, v ->
                when (unit) {
                    ValueUnit.KG -> r.getString(R.string.kg_value, formatAxisKg(v))
                    ValueUnit.BMI -> formatAxisKg(v)
                    ValueUnit.KCAL -> r.getString(R.string.food_kcal, formatKcal(v))
                    ValueUnit.STEPS -> r.getString(R.string.steps_value, formatSteps(v.toLong()))
                }
            },
            legend = { r.getString(legendText(it)) },
        )
    }
}

internal fun legendText(label: LegendLabel): Int = when (label) {
    LegendLabel.WEIGHT -> R.string.legend_weight
    LegendLabel.BMI -> R.string.legend_bmi
    LegendLabel.FOOD -> R.string.energy_legend_food
    LegendLabel.TREAT -> R.string.energy_legend_treat
    LegendLabel.STEPS -> R.string.steps_legend
    LegendLabel.BURN -> R.string.burn_legend
    LegendLabel.REST -> R.string.burn_legend_rest
    LegendLabel.ACTIVE -> R.string.burn_legend_active
    LegendLabel.TOTAL -> R.string.burn_legend_total
    LegendLabel.AVERAGE -> R.string.energy_legend_average
    LegendLabel.WEEKLY -> R.string.legend_weekly
}

/** The label text size in dp for the library: labelSmall (11 sp) × font scale (review decision 9). */
@Composable
internal fun chartTextDp(): Float {
    val density = LocalDensity.current
    return with(density) { 11.sp.toPx() } / density.density
}

/** The views of one chart and what the overlay reads from them. */
internal class HistoryChartState {
    val charts = mutableStateListOf<HistoryCombinedChart?>()
    val tops = mutableStateListOf<Float>()
    /** Bumped when a view moved, zoomed or resized: the marker card is placed again. */
    var version by mutableIntStateOf(0)
    private var syncing = false

    fun ensure(count: Int) {
        while (charts.size < count) charts.add(null)
        while (tops.size < count) tops.add(0f)
    }

    fun drawn(): List<HistoryCombinedChart> = charts.filterNotNull()

    /** Every drawn panel keeps the same plot left edge (design 3, "Days line up"). */
    fun alignPanels() {
        val drawn = drawn()
        if (drawn.size < 2) return
        drawn.forEach { it.axisLeft.minWidth = 0f }
        val widest = drawn.maxOf { it.axisLeft.getRequiredWidthSpace(it.rendererLeftYAxis.paintAxisLabels) }
        val dp = widest / it0Density(drawn.first())
        drawn.forEach {
            it.axisLeft.minWidth = dp
            it.notifyDataSetChanged()
        }
    }

    private fun it0Density(chart: HistoryCombinedChart) = chart.resources.displayMetrics.density

    /** Stacked panels zoom and pan together (review decision 7). */
    fun sync(from: HistoryCombinedChart) {
        if (syncing) return
        syncing = true
        try {
            val matrix = Matrix(from.viewPortHandler.matrixTouch)
            drawn().filter { it !== from }.forEach { it.viewPortHandler.refresh(Matrix(matrix), it, true) }
        } finally {
            syncing = false
        }
        version++
    }

    fun fitAll() {
        drawn().forEach { it.fitScreen() }
        version++
    }
}

/**
 * A Historia chart on MPAndroidChart (design 3, 5, 6): one view per panel of [spec], message
 * panels as Compose boxes, a Compose marker card over the stack at [chosen], TalkBack actions and
 * a polite live region with the card's text. [onChoose] is called with the new choice (null
 * clears). [resetKey] resets the zoom when it changes (period, tab).
 */
@Composable
internal fun HistoryChart(
    spec: ChartSpec,
    chosen: LocalDate?,
    onChoose: (LocalDate?) -> Unit,
    description: String,
    modifier: Modifier = Modifier,
    tag: String = "chart",
    plotTag: (index: Int, panel: PanelSpec) -> String = { i, p -> p.kind?.let { "plot-${it.name}" } ?: "plot-$i" },
    states: Map<SeriesKind, PanelState> = emptyMap(),
    resetKey: Any? = null,
) {
    val colors = rememberChartColors()
    val texts = rememberChartTexts()
    val textDp = chartTextDp()
    val state = remember { HistoryChartState() }
    state.ensure(spec.panels.size)
    val currentSpec by rememberUpdatedState(spec)
    val currentChosen by rememberUpdatedState(chosen)
    val choose by rememberUpdatedState(onChoose)
    // What TalkBack announces: set only on a tap, the end of a drag and the actions (review decision 8).
    var announced by remember { mutableStateOf<String?>(null) }
    var lastChoice by remember { mutableStateOf<LocalDate?>(null) }
    LaunchedEffect(chosen) {
        lastChoice = chosen
        if (chosen == null) announced = null
    }
    fun pick(day: LocalDate?, announce: Boolean) {
        lastChoice = day
        choose(day)
        if (announce) announced = day?.let { currentSpec.readout(it).text }
    }
    ClearOffAxis(spec.axis, chosen) { onChoose(null) }
    LaunchedEffect(resetKey, spec.axis) { state.fitAll() }

    val a11y = Modifier.semantics {
        contentDescription = description
        liveRegion = LiveRegionMode.Polite
        announced?.let { stateDescription = it }
    }.crosshairActions(spec.axis, chosen) { pick(it, announce = true) }

    Column(modifier.fillMaxWidth().then(a11y).testTag(tag)) {
        if (spec.empty) {
            Box(Modifier.fillMaxWidth().height(160.dp).testTag("chartEmpty"), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text(androidx.compose.ui.res.stringResource(R.string.chart_empty), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Column
        }
        val drawnIndices = spec.panels.indices.filter { spec.panels[it].message == null }
        val lastDrawn = drawnIndices.lastOrNull()
        val single = spec.panels.size == 1
        Box(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth()) {
                spec.panels.forEachIndexed { i, panel ->
                    val place = Modifier.fillMaxWidth().onGloballyPositioned { state.tops[i] = it.positionInParent().y; state.version++ }
                    if (panel.message != null) {
                        state.charts[i] = null
                        Box(place.height(panel.heightDp.dp).testTag(plotTag(i, panel))) {
                            PanelMessage(panel.message.kind, states[panel.message.kind] ?: PanelState.Ready, compact = false)
                        }
                        return@forEachIndexed
                    }
                    val dates = i == lastDrawn
                    val legend = single
                    val style = PanelStyle(colors, texts, textDp, dates = dates, legend = legend)
                    val extra = (if (dates) datesRoomDp(textDp) else 0f) + (if (legend && spec.legend.isNotEmpty()) legendRoomDp(textDp) else 0f)
                    AndroidView(
                        factory = { context ->
                            HistoryCombinedChart(context).also { chart ->
                                chart.picker = { x, y -> chart.pickAt(x, y)?.let { chart.highlightsFor(it).firstOrNull() } }
                                chart.onResized = {
                                    state.alignPanels()
                                    state.version++
                                }
                                chart.valueSelectedListeners += object : OnChartValueSelectedListener {
                                    // Only a drag selects through the library (taps are ours).
                                    override fun onValueSelected(e: Entry<*>, h: Highlight) {
                                        val day = currentSpec.dayAt(h.x.toDouble())
                                        if (day != lastChoice) pick(day, announce = false)
                                    }

                                    override fun onNothingSelected() = Unit
                                }
                                chart.gestureListeners += object : OnChartGestureListener {
                                    override fun onChartSingleTapped(me: MotionEvent) {
                                        val day = chart.pickAt(me.x, me.y)
                                        pick(if (day == null || day == lastChoice) null else day, announce = true)
                                    }

                                    override fun onChartLongPressed(me: MotionEvent) {
                                        chart.pickAt(me.x, me.y)?.let { pick(it, announce = true) }
                                    }

                                    override fun onChartGestureEnd(me: MotionEvent, lastPerformedGesture: ChartTouchListener.ChartGesture) {
                                        if (lastPerformedGesture == ChartTouchListener.ChartGesture.DRAG) {
                                            announced = lastChoice?.let { currentSpec.readout(it).text }
                                        }
                                        state.sync(chart)
                                    }

                                    override fun onChartScale(me: MotionEvent, scaleX: Float, scaleY: Float) = state.sync(chart)

                                    override fun onChartTranslate(me: MotionEvent, dX: Float, dY: Float) = state.sync(chart)
                                }
                            }
                        },
                        update = { chart ->
                            state.charts[i] = chart
                            chart.bind(spec, panel, style)
                            state.alignPanels()
                            chart.highlightValues(chart.highlightsFor(chosen))
                            state.version++
                        },
                        modifier = place
                            .height((panel.heightDp + extra).dp)
                            .plotOwnsSideways(state, i)
                            .testTag(plotTag(i, panel)),
                    )
                }
            }
            if (chosen != null && drawnIndices.isNotEmpty()) {
                ReadoutMarker(spec, chosen, state, drawnIndices, colors, Modifier.matchParentSize())
            }
        }
        if (!single && spec.legend.isNotEmpty()) {
            ChartLegend(spec.legend.map { it.toCompose(colors, texts) }, "chartLegend")
        }
        spec.notDrawn.forEach { kind -> PanelMessage(kind, states[kind] ?: PanelState.Ready, compact = false, line = true) }
    }
}

private fun fi.jukkakot.terveyslappu.chart.LegendItem.toCompose(colors: ChartColors, texts: ChartTexts): LegendEntry {
    val colour = tone?.let { Color(colors.of(it)) }
    return when (form) {
        LegendForm.SQUARE -> LegendEntry(texts.legend(label), colour)
        LegendForm.LINE -> LegendEntry(texts.legend(label), colour, line = true)
        LegendForm.DASHED -> LegendEntry(texts.legend(label), colour, dashed = true)
        LegendForm.NONE -> LegendEntry(texts.legend(label))
    }
}

/**
 * A sideways drag that starts inside the plot belongs to the chart (review decision 6): its moves
 * are consumed after the view has seen them, so the Historia pager never starts a swipe there.
 * Vertical moves stay free for the list.
 */
private fun Modifier.plotOwnsSideways(state: HistoryChartState, index: Int): Modifier = pointerInput(index) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val chart = state.charts.getOrNull(index) ?: return@awaitEachGesture
        val vp = chart.viewPortHandler
        if (!vp.contentRect.contains(down.position.x, down.position.y)) return@awaitEachGesture
        var sideways: Boolean? = null
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Main)
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            if (!change.pressed) break
            val moved = change.position - down.position
            if (sideways == null && moved.getDistance() > viewConfiguration.touchSlop / 2) sideways = abs(moved.x) >= abs(moved.y)
            if (sideways == true || event.changes.size > 1) event.changes.forEach { it.consume() }
        }
    }
}

/** Where the card goes (design 5): right of the day, or left of it near the right edge, clamped inside [min]..[max]. */
internal fun markerLeft(anchor: Float, width: Float, gap: Float, min: Float, max: Float): Float {
    var left = anchor + gap
    if (left + width > max) left = anchor - gap - width
    val hi = max - width
    return if (hi < min) min else left.coerceIn(min, hi)
}

/**
 * The marker card over the stack (review decision 1): the chosen day's head and rows, beside the
 * day at the top of the topmost drawn panel, clamped inside the plot; rings on the chosen line
 * points of every panel.
 */
@Composable
private fun ReadoutMarker(spec: ChartSpec, chosen: LocalDate, state: HistoryChartState, drawn: List<Int>, colors: ChartColors, modifier: Modifier) {
    @Suppress("UNUSED_VARIABLE") val tick = state.version
    val density = LocalDensity.current
    val top = drawn.first()
    val topChart = state.charts.getOrNull(top) ?: return
    if (topChart.width == 0) return
    val vp = topChart.viewPortHandler
    val point = spec.panels[top].pointsAt(chosen, spec.axis).values.firstOrNull()
    val x = point?.x ?: (spec.x.of(chosen) + if (spec.axis.weekly) 3.0 else 0.0)
    val anchor = topChart.getPixelForValues(x.toFloat(), 0f, YAxis.AxisDependency.LEFT).x.toFloat()
    if (anchor < vp.contentLeft - 1 || anchor > vp.contentRight + 1) return
    val card = remember(spec, chosen) { spec.readout(chosen) }
    // Rings on the chosen points of the lines (design 5).
    Canvas(modifier) {
        drawn.forEach { i ->
            val chart = state.charts.getOrNull(i) ?: return@forEach
            val panel = spec.panels[i]
            panel.pointsAt(chosen, spec.axis).forEach { (s, p) ->
                if (panel.series[s].shape !is SeriesShape.Line) return@forEach
                val px = chart.getPixelForValues(p.x.toFloat(), p.y.toFloat(), YAxis.AxisDependency.LEFT)
                val at = Offset(px.x.toFloat(), px.y.toFloat() + state.tops[i])
                drawCircle(Color(colors.of(panel.series[s].tone)), radius = 6.dp.toPx(), center = at, style = Stroke(width = 2.dp.toPx()))
            }
        }
    }
    val gap = with(density) { 8.dp.toPx() }
    val cardTop = state.tops[top] + vp.contentTop + with(density) { 2.dp.toPx() }
    Layout(content = { MarkerCard(card, colors) }, modifier = modifier) { measurables, constraints ->
        val p = measurables.first().measure(Constraints(maxWidth = constraints.maxWidth))
        val left = markerLeft(anchor, p.width.toFloat(), gap, vp.contentLeft, vp.contentRight.coerceAtMost(constraints.maxWidth.toFloat()))
        layout(constraints.maxWidth, constraints.maxHeight) { p.place(left.roundToInt(), cardTop.roundToInt()) }
    }
}

/** The card itself: bold head, then a dot, name and value per row (names left, values right). */
@Composable
private fun MarkerCard(card: ReadoutCard, colors: ChartColors) {
    val c = MaterialTheme.colorScheme
    Column(
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(c.surfaceContainerHighest)
            .border(1.dp, c.outlineVariant, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag("chartMarker"),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(card.head, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = c.onSurface, modifier = Modifier.testTag("chartMarkerHead"))
        Row {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                card.rows.forEach { row ->
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(Color(colors.of(row.tone))))
                        Spacer(Modifier.width(6.dp))
                        Text(row.name, style = MaterialTheme.typography.labelMedium, color = c.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                card.rows.forEach { row -> Text(row.value, style = MaterialTheme.typography.labelMedium, color = c.onSurface) }
            }
        }
    }
}
