package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.weight.WeightEntry
import java.time.LocalDate
import java.time.ZoneId

/**
 * The Oma calendar's selection (weight-chart "Days with weights in the calendar"): the first tap
 * sets the start, the second the end; a day before the start becomes the new start (as in the
 * Material date-range picker); a tap after a full range starts a new one.
 */
data class RangePick(val start: LocalDate? = null, val end: LocalDate? = null) {
    fun tap(day: LocalDate): RangePick = when {
        start == null || end != null -> RangePick(day, null)
        day.isBefore(start) -> RangePick(day, null)
        else -> RangePick(start, day)
    }

    fun contains(day: LocalDate): Boolean = when {
        start == null -> false
        end == null -> day == start
        else -> !day.isBefore(start) && !day.isAfter(end)
    }

    /** The range OK applies: the single start day when no end is set; null before any tap. */
    fun result(): Pair<LocalDate, LocalDate>? = start?.let { it to (end ?: it) }

    companion object {
        /** The latest weight of each local day: what the calendar shows under the day. */
        fun dayWeights(weights: List<WeightEntry>, zone: ZoneId): Map<LocalDate, Double> =
            weights.groupBy { it.time.atZone(zone).toLocalDate() }
                .mapValues { (_, day) -> day.maxBy { it.time }.kg }
    }
}
