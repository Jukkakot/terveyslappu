package fi.jukkakot.terveyslappu.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.chart.ChartAxis
import fi.jukkakot.terveyslappu.chart.ChartDots
import java.time.LocalDate
import kotlin.math.max

/**
 * The front page cards' drawing (custom-charts design 6): the own chart cards and the Kulutus card.
 * Each drawer fills the whole [DrawScope] with x from [ChartAxis] (day centres) and y from the
 * caller's scale. Historia's charts are drawn by MPAndroidChart (`HistoryChart`).
 */

/** One bar: a day or (fixed tabs only) a week from Monday; [treat] is stacked on top in its own tone. */
internal data class BarSpec(val from: LocalDate, val days: Int, val value: Double, val treat: Double = 0.0)

/**
 * Bars rising from [base] (the plot bottom by default) to [yOf] of their value, 70 % of their slot
 * and never narrower than 1 dp; [slot] of [slots] splits a day's slot side by side (overlaid layout).
 */
internal fun DrawScope.drawBars(
    axis: ChartAxis,
    bars: List<BarSpec>,
    yOf: (Double) -> Float,
    color: Color,
    treatColor: Color? = null,
    slot: Int = 0,
    slots: Int = 1,
    base: Float = size.height,
) {
    val dayW = axis.dayWidth(size.width)
    for (bar in bars) {
        val span = bar.days * dayW / slots
        val left = axis.left(bar.from, size.width) + span * slot + span * 0.15f
        val width = max(span * 0.7f, 1.dp.toPx())
        val split = treatColor != null && bar.treat > 0
        val baseTop = yOf(if (split) bar.value - bar.treat else bar.value).coerceAtMost(base)
        drawRect(color, Offset(left, baseTop), Size(width, base - baseTop))
        if (split) {
            val treatTop = yOf(bar.value)
            drawRect(treatColor!!, Offset(left, treatTop), Size(width, baseTop - treatTop))
        }
    }
}

/** A Kulutus bar: [total] high, [active] (null: a kokonais day) on top of the lepo part. */
internal data class BurnBarSpec(val from: LocalDate, val days: Int, val total: Double, val active: Double?)

/**
 * Kulutus's two-tone bars (energy-burn "Kulutus tab"): lepo at the bottom in the light tone, aktiivinen
 * on top in the full [color]; a kokonais day in the kokonais tone. Same geometry as [drawBars].
 */
internal fun DrawScope.drawBurnBars(axis: ChartAxis, bars: List<BurnBarSpec>, yOf: (Double) -> Float, color: Color, slot: Int = 0, slots: Int = 1, base: Float = size.height) {
    val dayW = axis.dayWidth(size.width)
    for (bar in bars) {
        val full = color
        val rest = full.copy(alpha = full.alpha * ChartPalette.LIGHT_ALPHA)
        val whole = full.copy(alpha = full.alpha * ChartPalette.TOTAL_ALPHA)
        val span = bar.days * dayW / slots
        val left = axis.left(bar.from, size.width) + span * slot + span * 0.15f
        val width = max(span * 0.7f, 1.dp.toPx())
        val top = yOf(bar.total).coerceAtMost(base)
        if (bar.active == null) {
            drawRect(whole, Offset(left, top), Size(width, base - top))
            continue
        }
        val restTop = yOf((bar.total - bar.active).coerceAtLeast(0.0)).coerceAtMost(base)
        if (restTop < base) drawRect(rest, Offset(left, restTop), Size(width, base - restTop))
        drawRect(full, Offset(left, top), Size(width, restTop - top))
    }
}

/**
 * A 2 dp line joining the [points] at their days' centres; [dots] (weight): every dot when they fit,
 * else the last.
 */
internal fun DrawScope.drawValueLine(axis: ChartAxis, points: List<Pair<LocalDate, Double>>, yOf: (Double) -> Float, color: Color, dots: Boolean = false) {
    if (points.isEmpty()) return
    val at = points.map { (day, v) -> Offset(axis.x(day, size.width), yOf(v)) }
    if (at.size > 1) {
        val path = Path()
        at.forEachIndexed { i, p -> if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y) }
        drawPath(path, color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
    }
    if (!dots && at.size > 1) return
    if (ChartDots.fit(at.size, size.width / density)) {
        at.forEach { drawCircle(color, radius = 3.dp.toPx(), center = it) }
    } else {
        drawCircle(color, radius = 3.dp.toPx(), center = at.last())
    }
}

/** The flat dashed average line (1 dp, 4 dp dashes, as on the big cards) across the whole plot at [y]. */
internal fun DrawScope.drawAverageLine(y: Float, color: Color) {
    val dash = 4.dp.toPx()
    drawLine(color, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash)))
}
