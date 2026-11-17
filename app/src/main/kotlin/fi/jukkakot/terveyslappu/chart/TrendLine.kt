package fi.jukkakot.terveyslappu.chart

import org.apache.commons.math3.analysis.interpolation.LoessInterpolator
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max

/**
 * One unbroken part of a trend line (period-trend-line design 3): one smoothed value per input
 * day, oldest first.
 */
data class TrendSegment(val days: List<LocalDate>, val values: List<Double>) {
    val first: LocalDate get() = days.first()
    val last: LocalDate get() = days.last()

    /** The trend at [day], linear between the segment's days; null outside the segment. */
    fun at(day: LocalDate): Double? {
        if (day.isBefore(first) || day.isAfter(last)) return null
        val i = days.binarySearch(day)
        if (i >= 0) return values[i]
        val after = -i - 1
        val a = days[after - 1]
        val b = days[after]
        val t = ChronoUnit.DAYS.between(a, day).toDouble() / ChronoUnit.DAYS.between(a, b)
        return values[after - 1] + t * (values[after] - values[after - 1])
    }

    /** The values clamped into [min]..[max] (the chart's scale). */
    fun clamped(min: Double, max: Double): TrendSegment = copy(values = values.map { it.coerceIn(min, max) })

    /** The values mapped through [f] (an overlaid chart's fraction of the scale). */
    fun map(f: (Double) -> Double): TrendSegment = copy(values = values.map(f))
}

/** The trend at [day] from whichever segment covers it; null when none does. */
fun List<TrendSegment>.at(day: LocalDate): Double? = firstNotNullOfOrNull { it.at(day) }

/**
 * The trend line of the Historia charts' long periods (weight-chart "Trend line on long periods",
 * period-trend-line design 1–3, 8): LOESS from Apache Commons Math over the daily values, split at
 * gaps over [GAP_DAYS], parts under [MIN_POINTS] values dropped. Pure.
 */
object TrendLine {
    /** Periods of this many axis days or fewer keep the flat average line. */
    const val FLAT_MAX_DAYS = 31

    /** Two values further apart than this break the line. */
    const val GAP_DAYS = 30L

    /** A part needs this many values to get a line. */
    const val MIN_POINTS = 10

    /** The window's floor and the axis divisor: `W = max(21, axisDays / 6)`. */
    const val MIN_WINDOW_DAYS = 21
    const val TREND_WINDOW_DIVISOR = 6

    /** LOESS robustness iterations: one odd day barely moves the line. */
    const val ROBUSTNESS_ITERS = 2

    /** The smoothing window in days for an axis of [axisDays]. */
    fun window(axisDays: Int): Int = max(MIN_WINDOW_DAYS, axisDays / TREND_WINDOW_DIVISOR)

    /** Whether an axis of [axisDays] draws a trend (else the flat average). */
    fun long(axisDays: Int): Boolean = axisDays > FLAT_MAX_DAYS

    /** The library's smoothing: x, y and the bandwidth (share of points) in, one value per x out. */
    fun interface Smoother {
        fun smooth(x: DoubleArray, y: DoubleArray, bandwidth: Double): DoubleArray
    }

    val loess = Smoother { x, y, bandwidth ->
        LoessInterpolator(bandwidth, ROBUSTNESS_ITERS).smooth(x, y)
    }

    /**
     * The trend segments of [points] (one value per day, any order) on an axis of [axisDays]. Empty
     * = too few values (no part with [MIN_POINTS] values, or the smoothing failed on every part);
     * the caller then falls back. Never throws.
     */
    fun of(points: List<Pair<LocalDate, Double>>, axisDays: Int, smoother: Smoother = loess): List<TrendSegment> {
        val sorted = points.filter { it.second.isFinite() }.distinctBy { it.first }.sortedBy { it.first }
        if (sorted.isEmpty()) return emptyList()
        val parts = mutableListOf<MutableList<Pair<LocalDate, Double>>>(mutableListOf(sorted[0]))
        for ((prev, next) in sorted.zipWithNext()) {
            if (ChronoUnit.DAYS.between(prev.first, next.first) > GAP_DAYS) parts += mutableListOf<Pair<LocalDate, Double>>()
            parts.last() += next
        }
        val window = window(axisDays).toDouble()
        return parts.filter { it.size >= MIN_POINTS }.mapNotNull { smooth(it, window, smoother) }
    }

    private fun smooth(part: List<Pair<LocalDate, Double>>, window: Double, smoother: Smoother): TrendSegment? {
        val n = part.size
        val x = DoubleArray(n) { part[it].first.toEpochDay().toDouble() }
        val y = DoubleArray(n) { part[it].second }
        if ((1 until n).any { x[it] <= x[it - 1] }) return null
        val span = x[n - 1] - x[0]
        val bandwidth = (window / span).coerceIn(4.0 / n, 1.0)
        val out = try {
            smoother.smooth(x, y, bandwidth)
        } catch (e: Exception) {
            return null
        }
        if (out.size != n || out.any { !it.isFinite() }) return null
        return TrendSegment(part.map { it.first }, out.toList())
    }
}

/** What a chart draws in place of its average (period-trend-line design 4). */
sealed interface AverageLine {
    data object None : AverageLine
    data class Flat(val value: Double) : AverageLine
    data class Trend(val segments: List<TrendSegment>) : AverageLine

    val flat: Double? get() = (this as? Flat)?.value
    val trend: List<TrendSegment>? get() = (this as? Trend)?.segments

    /** The highest value drawn (a bar scale's top covers it). */
    val highest: Double? get() = when (this) {
        None -> null
        is Flat -> value
        is Trend -> segments.maxOf { s -> s.values.max() }
    }

    companion object {
        /**
         * The flat [average] on an axis of 31 days or less, or when [values] give no trend part of
         * 10+ values; else the trend. [values]: the daily values the average uses.
         */
        fun of(axisDays: Int, values: List<Pair<LocalDate, Double>>, average: Double?, smoother: TrendLine.Smoother = TrendLine.loess): AverageLine {
            if (TrendLine.long(axisDays)) {
                val trend = TrendLine.of(values, axisDays, smoother)
                if (trend.isNotEmpty()) return Trend(trend)
            }
            return average?.let { Flat(it) } ?: None
        }
    }
}
