package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.weight.WeightEntry
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToLong

/**
 * What the chart draws for a period (weight-chart): x is days since [origin] (fractional, so the
 * time of day counts), y is kg. The x range spans the whole period, the y range the weights with
 * some room, rounded to whole kilos.
 */
class ChartData(
    /** Oldest first. */
    val entries: List<WeightEntry>,
    val origin: LocalDate,
    val xs: List<Double>,
    val maxX: Double,
    val minY: Double,
    val maxY: Double,
    /** Days between x-axis labels. */
    val labelSpacing: Int,
) {
    /** The day an x value stands for. */
    fun dayAt(x: Double): LocalDate = origin.plusDays(x.roundToLong())

    /** The weight whose dot is nearest to [x]. */
    fun entryAt(x: Double): WeightEntry? = xs.indices.minByOrNull { kotlin.math.abs(xs[it] - x) }?.let { entries[it] }

    companion object {
        /** About this many labels fit under the chart on a phone. */
        private const val LABELS = 5

        /** Null when there is nothing to draw. */
        fun of(period: Period, periodEntries: List<WeightEntry>, now: Instant, zone: ZoneId): ChartData? {
            if (periodEntries.isEmpty()) return null
            val sorted = periodEntries.sortedBy { it.time }
            val (start, end) = period.range(now, zone)
            val origin = (start ?: sorted.first().time).atZone(zone).toLocalDate()
            val originInstant = origin.atStartOfDay(zone).toInstant()
            fun x(time: Instant) = Duration.between(originInstant, time).seconds / 86_400.0
            val xs = sorted.map { x(it.time) }
            val lastX = minOf(x(end), x(now)).coerceAtLeast(xs.last())
            val maxX = max(1.0, ceil(lastX))
            val kgs = sorted.map { it.kg }
            return ChartData(
                entries = sorted,
                origin = origin,
                xs = xs,
                maxX = maxX,
                minY = floor(kgs.min() - 0.5),
                maxY = ceil(kgs.max() + 0.5),
                labelSpacing = max(1, ceil(maxX / LABELS).toInt()),
            )
        }
    }
}
