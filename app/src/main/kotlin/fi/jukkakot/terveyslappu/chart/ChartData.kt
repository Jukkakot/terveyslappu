package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.weight.WeightEntry
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToLong

/**
 * What the chart draws for a period (weight-chart): x is days since [origin] (fractional, so the
 * time of day counts), y is kg. The x range spans the whole period, the y range follows [WeightScale].
 */
class ChartData(
    /** Oldest first. */
    val entries: List<WeightEntry>,
    /** The shown quantity's value of each of [entries] (kg by default). */
    val ys: List<Double> = entries.map { it.kg },
    val origin: LocalDate,
    val xs: List<Double>,
    val maxX: Double,
    val minY: Double,
    val maxY: Double,
    /** Days between x-axis labels. */
    val labelSpacing: Int,
    /** Index ranges into [entries], each drawn as its own line: a gap over [MAX_GAP_DAYS] breaks the line. */
    val segments: List<IntRange> = listOf(entries.indices),
    /** The chart spans more than a year, so x labels show month and year. */
    val overYear: Boolean = false,
) {
    /** The day an x value stands for. */
    fun dayAt(x: Double): LocalDate = origin.plusDays(x.roundToLong())

    /** The weight whose dot is nearest to [x]. */
    fun entryAt(x: Double): WeightEntry? = indexAt(x)?.let { entries[it] }

    /** Index of the dot nearest to [x]. */
    fun indexAt(x: Double): Int? = xs.indices.minByOrNull { kotlin.math.abs(xs[it] - x) }

    companion object {
        /** About this many labels fit under the chart on a phone. */
        const val LABELS = 5

        /** Consecutive weights further apart than this many days are not joined by the line. */
        const val MAX_GAP_DAYS = 30.0

        /** Splits sorted x values into runs whose neighbours are at most [MAX_GAP_DAYS] apart. */
        fun segments(xs: List<Double>): List<IntRange> {
            if (xs.isEmpty()) return emptyList()
            val result = mutableListOf<IntRange>()
            var start = 0
            for (i in 1 until xs.size) {
                if (xs[i] - xs[i - 1] > MAX_GAP_DAYS) {
                    result += start until i
                    start = i
                }
            }
            result += start until xs.size
            return result
        }

        /**
         * Null when there is nothing to draw. [value] gives the shown quantity (null leaves a weight
         * out), [scale] its y range rule.
         */
        fun of(
            period: Period,
            periodEntries: List<WeightEntry>,
            now: Instant,
            zone: ZoneId,
            value: (WeightEntry) -> Double? = { it.kg },
            scale: (Collection<Double>) -> KgRange = { WeightScale.of(it) },
            labels: Int = LABELS,
        ): ChartData? {
            val valued = periodEntries.sortedBy { it.time }.mapNotNull { e -> value(e)?.let { e to it } }
            if (valued.isEmpty()) return null
            val sorted = valued.map { it.first }
            val ys = valued.map { it.second }
            val (start, end) = period.range(now, zone)
            val origin = (start ?: sorted.first().time).atZone(zone).toLocalDate()
            val originInstant = origin.atStartOfDay(zone).toInstant()
            fun x(time: Instant) = Duration.between(originInstant, time).seconds / 86_400.0
            val xs = sorted.map { x(it.time) }
            val lastX = minOf(x(end), x(now)).coerceAtLeast(xs.last())
            val maxX = max(1.0, ceil(lastX))
            val range = scale(ys)
            return ChartData(
                entries = sorted,
                ys = ys,
                origin = origin,
                xs = xs,
                maxX = maxX,
                minY = range.min,
                maxY = range.max,
                labelSpacing = max(1, ceil(maxX / labels).toInt()),
                segments = segments(xs),
                overYear = origin.plusDays(maxX.toLong()) > origin.plusYears(1),
            )
        }
    }
}
