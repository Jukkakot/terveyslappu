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
 * The shared drawing of Historia's bar and line charts (custom-charts design 6): the fixed Energia
 * and Askeleet tabs, the own charts and their cards. Each drawer fills the whole [DrawScope] with
 * x from [ChartAxis] (day centres) and y from the caller's scale.
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
    chosen: LocalDate? = null,
) {
    val dayW = axis.dayWidth(size.width)
    for (bar in bars) {
        val span = bar.days * dayW / slots
        val left = axis.left(bar.from, size.width) + span * slot + span * 0.15f
        val width = max(span * 0.7f, 1.dp.toPx())
        val split = treatColor != null && bar.treat > 0
        val dim = dimmed(bar.from, bar.days, chosen)
        val baseTop = yOf(if (split) bar.value - bar.treat else bar.value).coerceAtMost(base)
        drawRect(color.dimIf(dim), Offset(left, baseTop), Size(width, base - baseTop))
        if (split) {
            val treatTop = yOf(bar.value)
            drawRect(treatColor!!.dimIf(dim), Offset(left, treatTop), Size(width, baseTop - treatTop))
        }
    }
}

/** The alpha of the bars the crosshair did not choose (weight-chart "Crosshair on every Historia chart"). */
internal const val DIM_ALPHA = 0.6f

/** A bar from [from] over [days] is dimmed while a crosshair is on another day. */
private fun dimmed(from: LocalDate, days: Int, chosen: LocalDate?): Boolean =
    chosen != null && (chosen.isBefore(from) || !chosen.isBefore(from.plusDays(days.toLong())))

private fun Color.dimIf(dim: Boolean): Color = if (dim) copy(alpha = alpha * DIM_ALPHA) else this

/** A Kulutus bar: [total] high, [active] (null: a kokonais day) on top of the lepo part. */
internal data class BurnBarSpec(val from: LocalDate, val days: Int, val total: Double, val active: Double?)

/**
 * Kulutus's two-tone bars (energy-burn "Kulutus tab"): lepo at the bottom in the light tone, aktiivinen
 * on top in the full [color]; a kokonais day in the kokonais tone. Same geometry as [drawBars].
 */
internal fun DrawScope.drawBurnBars(axis: ChartAxis, bars: List<BurnBarSpec>, yOf: (Double) -> Float, color: Color, slot: Int = 0, slots: Int = 1, base: Float = size.height, chosen: LocalDate? = null) {
    val dayW = axis.dayWidth(size.width)
    for (bar in bars) {
        val full = color.dimIf(dimmed(bar.from, bar.days, chosen))
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
 * else the last; the [chosen] day's point gets the crosshair's ring.
 */
internal fun DrawScope.drawValueLine(axis: ChartAxis, points: List<Pair<LocalDate, Double>>, yOf: (Double) -> Float, color: Color, dots: Boolean = false, chosen: LocalDate? = null) {
    if (points.isEmpty()) return
    val at = points.map { (day, v) -> Offset(axis.x(day, size.width), yOf(v)) }
    chosen?.let { day -> points.indexOfFirst { it.first == day }.takeIf { it >= 0 }?.let { drawRing(at[it], color) } }
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

/** The crosshair: a 1.5 dp vertical line at [day]'s centre; on weekly bars at the centre of the week from [day] (its Monday). */
internal fun DrawScope.drawCrosshair(axis: ChartAxis, day: LocalDate, color: Color) {
    if (!axis.shows(day)) return
    val x = if (axis.weekly) axis.left(day, size.width) + 3.5f * axis.dayWidth(size.width) else axis.x(day, size.width)
    drawLine(color, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.5.dp.toPx())
}

/** The crosshair's marker on a line or dot: a 2 dp ring of 6 dp radius around [center]. */
internal fun DrawScope.drawRing(center: Offset, color: Color) {
    drawCircle(color, radius = 6.dp.toPx(), center = center, style = Stroke(width = 2.dp.toPx()))
}
