package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.derived.Tone
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.floor

/**
 * The pure definition of a Historia chart (charts-on-mpandroidchart design 2): what each panel
 * draws, on which axis and in which symbolic tone, plus the legend and the marker card's rows. The
 * library-backed renderer (`ui/HistoryChart`) maps it onto MPAndroidChart; the spec itself knows no
 * Android or Compose type, so builders are unit-tested.
 *
 * x is days since [ChartAxis.from]: a day's centre at its index, the axis from `-0.5` to
 * `days - 0.5` (see [ChartX]).
 */
class ChartSpec(
    val axis: ChartAxis,
    val panels: List<PanelSpec>,
    val legend: List<LegendItem>,
    /** The marker card of a chosen day (a weekly bar's key = the week's first day inside the axis). */
    val readout: (LocalDate) -> ReadoutCard,
    /** Nothing in the period at all: one message instead of the panels. */
    val empty: Boolean = false,
    /** Päällekkäin data without values, named under the plot (with their grant buttons). */
    val notDrawn: List<SeriesKind> = emptyList(),
    /**
     * An x range in place of [ChartX]'s (home-cards-on-chart-library design 2): the weight card's
     * line by time, x in days from its period's start. Null = the axis's day slots.
     */
    val xRange: ClosedFloatingPointRange<Double>? = null,
) {
    val x: ChartX get() = ChartX(axis)

    /** The drawn x range: [xRange], else the axis's day slots. */
    val xMin: Double get() = xRange?.start ?: x.min
    val xMax: Double get() = xRange?.endInclusive ?: x.max

    /** The key of the day under [x] (a weekly bar's key on weekly axes). */
    fun dayAt(x: Double): LocalDate = this.x.dayAt(x)

    /** Panels that draw a chart (not a message). */
    val drawn: List<PanelSpec> get() = panels.filter { it.message == null }
}

/** Mapping between days and the chart's x (design 2). */
class ChartX(val axis: ChartAxis) {
    val min: Double get() = -0.5
    val max: Double get() = axis.days - 0.5

    /** The centre of [day]. */
    fun of(day: LocalDate): Double = ChronoUnit.DAYS.between(axis.from, day).toDouble()

    /** A weight at its time of day: the day's slot runs from `index - 0.5` to `index + 0.5`. */
    fun of(time: Instant, zone: ZoneId): Double {
        val at = time.atZone(zone)
        val day = at.toLocalDate()
        val seconds = at.toLocalTime().toSecondOfDay()
        return of(day) - 0.5 + seconds / 86_400.0
    }

    /**
     * A weekly bar's centre: the middle of the week's days inside the axis (a partial first or last
     * week is centred on its own days, review decision 11).
     */
    fun ofWeek(monday: LocalDate): Double {
        val first = maxOf(monday, axis.from)
        val last = minOf(monday.plusDays(6), axis.to)
        return (of(first) + of(last)) / 2
    }

    /** The day under [x], clamped into the axis, as a chosen key ([ChartAxis.pick]). */
    fun dayAt(x: Double): LocalDate {
        val index = floor(x + 0.5).toLong().coerceIn(0, axis.days - 1L)
        return axis.pick(axis.from.plusDays(index))
    }
}

/** The chosen key for [day]: the day itself, or on weekly bars its week's first day inside the axis. */
fun ChartAxis.pick(day: LocalDate): LocalDate = if (weekly) maxOf(weekOf(day), from) else day

/** Whether a chosen key is still on this axis. */
fun ChartAxis.shows(day: LocalDate): Boolean = if (weekly) !day.plusDays(6).isBefore(from) && !day.isAfter(to) else day in this

/** A symbolic tone, resolved to a colour in the UI from the colour scheme or the palette. */
sealed interface ToneRef {
    val alpha: Float

    enum class Role { PRIMARY, SECONDARY, TERTIARY, ON_SURFACE_VARIANT }

    data class Scheme(val role: Role, override val alpha: Float = 1f) : ToneRef
    data class Palette(val colour: SeriesColour, override val alpha: Float = 1f) : ToneRef
    data class Band(val tone: Tone, override val alpha: Float = BAND_ALPHA) : ToneRef

    fun alpha(a: Float): ToneRef = when (this) {
        is Scheme -> copy(alpha = alpha * a)
        is Palette -> copy(alpha = alpha * a)
        is Band -> copy(alpha = alpha * a)
    }

    companion object {
        val PRIMARY = Scheme(Role.PRIMARY)
        val SECONDARY = Scheme(Role.SECONDARY)
        val TERTIARY = Scheme(Role.TERTIARY)
        val MUTED = Scheme(Role.ON_SURFACE_VARIANT)

        /** The BMI bands' faint fill (weight-chart "BMI chart"). */
        const val BAND_ALPHA = 0.14f

        /** The lighter tone (treats of a chosen colour, lepo) and a kokonais Kulutus day. */
        const val LIGHT_ALPHA = 0.45f
        const val TOTAL_ALPHA = 0.7f
    }
}

/** How an axis value is written: "82 kg", "25", "2 500 kcal", "14 000 askelta". */
enum class ValueUnit { KG, BMI, KCAL, STEPS }

/**
 * One value axis: [min]..[max] in plot units with three labels (bottom, middle, top). The labels
 * show [valueMin]..[valueMax] in [unit] (an overlaid chart plots fractions 0..1 but labels the
 * data's own scale); [tone] null = the muted label colour.
 */
data class AxisSpec(
    val min: Double,
    val max: Double,
    val unit: ValueUnit,
    val tone: ToneRef? = null,
    val valueMin: Double = min,
    val valueMax: Double = max,
) {
    /** The labelled values in plot units. */
    val ticks: List<Double> get() = listOf(min, (min + max) / 2, max)

    /** The value a plot-unit [v] stands for. */
    fun value(v: Double): Double = if (max == min) valueMin else valueMin + (v - min) / (max - min) * (valueMax - valueMin)
}

enum class Side { LEFT, RIGHT, HIDDEN }

/** One drawn point; [parts] are a stacked bar's parts from the bottom (their sum is [y]). */
data class ChartPoint(val x: Double, val y: Double, val day: LocalDate, val parts: List<Double>? = null)

/** Which dots a line draws (weight-chart "Dots when they fit"). */
sealed interface Dots {
    /** Only a lone point (a line of one value) gets its dot. */
    data object None : Dots
    data object All : Dots
    /** Every dot when they fit, else the points far from both neighbours ([ChartData.lone]). */
    data class FitOrLone(val lone: Set<Int>) : Dots
    /** Every dot when they fit, else the last one (own charts' weight line). */
    data object FitOrLast : Dots
    /** Every dot but the last when they fit, else none (the weight card: its last dot is a series of its own). */
    data object FitButLast : Dots

    /** Indices of the [count] points that get a dot on a plot [widthDp] wide. */
    fun shown(count: Int, widthDp: Float): Set<Int> {
        if (this == FitButLast) return if (ChartDots.fit(count, widthDp)) (0 until count - 1).toSet() else emptySet()
        if (count <= 1) return (0 until count).toSet()
        val fit = ChartDots.fit(count, widthDp)
        return when (this) {
            None -> emptySet()
            All -> (0 until count).toSet()
            is FitOrLone -> if (fit) (0 until count).toSet() else lone
            FitOrLast -> if (fit) (0 until count).toSet() else setOf(count - 1)
            FitButLast -> emptySet()
        }
    }
}

sealed interface SeriesShape {
    /** Bars of [widthDays] slot width (7 on weekly bars); drawn at 70 % of it, divided by [slots]. */
    data class Bars(val points: List<ChartPoint>, val widthDays: Double = 1.0, val slots: Int = 1) : SeriesShape
    /** Stacked bars: each point's [ChartPoint.parts] in [partTones] from the bottom. */
    data class StackedBars(val points: List<ChartPoint>, val partTones: List<ToneRef>, val widthDays: Double = 1.0) : SeriesShape
    /** A line [widthDp] wide with dots of radius [dotDp] (Historia's defaults; the cards set their own). */
    data class Line(val points: List<ChartPoint>, val dots: Dots, val widthDp: Float = LINE_DP, val dotDp: Float = DOT_DP) : SeriesShape

    companion object {
        const val LINE_DP = 2f
        const val DOT_DP = 3.5f
    }

    val all: List<ChartPoint>
        get() = when (this) {
            is Bars -> points
            is StackedBars -> points
            is Line -> points
        }
}

/**
 * One series of a panel. [average]: the flat dashed line in plot units, in [averageTone]; [kind]
 * names the data (an own chart's tag and the readout); bars are tappable only inside their slot.
 */
data class SeriesSpec(
    val id: String,
    val tone: ToneRef,
    val shape: SeriesShape,
    val side: Side = Side.LEFT,
    val average: Double? = null,
    val averageTone: ToneRef = ToneRef.MUTED,
)

/** A faint band behind the data, [from] to [to] in plot units (already clipped to the scale). */
data class BandSpec(val from: Double, val to: Double, val tone: ToneRef)

/** A panel without a chart: an own chart's data that is empty, not loaded or not allowed. */
data class PanelMessage(val kind: SeriesKind)

/**
 * One plot. [heightDp] is the plot's height; the renderer adds room for the date labels on the
 * last panel. [kind] names an own chart's panel (null on the fixed tabs).
 *
 * The card look (home-cards-on-chart-library design 3), read only in card mode: [grid] =
 * guidelines at the left axis's three values; [baseline] = the 1 dp line along the plot's bottom
 * (null: in the grid colour when [grid], else none); [insetDp] = room on every side (the weight
 * card's end dot).
 */
data class PanelSpec(
    val heightDp: Int,
    val left: AxisSpec?,
    val right: AxisSpec? = null,
    val series: List<SeriesSpec> = emptyList(),
    val bands: List<BandSpec> = emptyList(),
    val message: PanelMessage? = null,
    val kind: SeriesKind? = null,
    val grid: Boolean = true,
    val baseline: ToneRef? = null,
    val insetDp: Float = 0f,
) {
    /**
     * The day a tap or drag at [x] chooses (review decision 5): bars only inside a bar's slot, lines
     * the nearest point within [maxDx] (x units); null = nothing near.
     */
    fun pick(x: Double, maxDx: Double, axisX: ChartX): LocalDate? {
        var best: ChartPoint? = null
        var bestDx = Double.MAX_VALUE
        for (s in series) {
            when (val shape = s.shape) {
                is SeriesShape.Line -> shape.points.forEach { p ->
                    val dx = abs(p.x - x)
                    if (dx <= maxDx && dx < bestDx) { best = p; bestDx = dx }
                }
                is SeriesShape.Bars, is SeriesShape.StackedBars -> {
                    val width = when (shape) {
                        is SeriesShape.Bars -> shape.widthDays
                        is SeriesShape.StackedBars -> shape.widthDays
                        else -> 1.0
                    }
                    shape.all.forEach { p ->
                        // A bar's whole slot counts: the day (or week) it stands for.
                        val slotCentre = if (width > 1) p.x else axisX.of(p.day)
                        val dx = abs(slotCentre - x)
                        if (dx <= width / 2 && dx < bestDx) { best = p; bestDx = dx }
                    }
                }
            }
        }
        return best?.let { axisX.axis.pick(it.day) }
    }

    /** The exact point of each series for a chosen key (a weight day's last weight), by series index. */
    fun pointsAt(key: LocalDate, axis: ChartAxis): Map<Int, ChartPoint> = buildMap {
        series.forEachIndexed { i, s ->
            s.shape.all.lastOrNull { axis.pick(it.day) == key }?.let { put(i, it) }
        }
    }
}

/** A legend entry; [label] is resolved to text in the UI. */
data class LegendItem(val label: LegendLabel, val tone: ToneRef?, val form: LegendForm)

enum class LegendForm { SQUARE, LINE, DASHED, NONE }

enum class LegendLabel { WEIGHT, BMI, FOOD, TREAT, STEPS, BURN, REST, ACTIVE, TOTAL, AVERAGE, WEEKLY }

/** One row of the marker card: a dot in [tone], the data's [name] and its [value] with unit. */
data class ReadoutRow(val tone: ToneRef, val name: String, val value: String)

/** The marker card (design 5): the date head and one row per series. */
data class ReadoutCard(val head: String, val rows: List<ReadoutRow>) {
    /** What TalkBack announces: "Su 8.11. · 7.05, paino 82,6 kg". */
    val text: String get() = (listOf(head) + rows.map { "${it.name} ${it.value}" }).joinToString(", ")
}
