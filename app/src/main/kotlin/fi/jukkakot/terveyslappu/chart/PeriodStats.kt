package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.weight.WeightEntry
import java.time.Duration

/** Key numbers of a period's weights (weight-chart). Change and rate are null when they mean nothing. */
data class PeriodStats(
    val latest: Double,
    /** Latest minus first, kg; null with a single weight. */
    val change: Double?,
    /** [change] per week; null when the weights span less than a day. */
    val perWeek: Double?,
    val average: Double,
    val min: Double,
    val max: Double,
    val count: Int,
)

private const val DAY_SECONDS = 86_400.0

/** Stats of [entries] in any order, or null when there are none. */
fun periodStats(entries: List<WeightEntry>): PeriodStats? {
    if (entries.isEmpty()) return null
    val sorted = entries.sortedBy { it.time }
    val first = sorted.first()
    val latest = sorted.last()
    val change = if (sorted.size > 1) latest.kg - first.kg else null
    val days = Duration.between(first.time, latest.time).seconds / DAY_SECONDS
    val perWeek = if (change != null && days >= 1.0) change / days * 7 else null
    return PeriodStats(
        latest = latest.kg,
        change = change,
        perWeek = perWeek,
        average = sorted.map { it.kg }.average(),
        min = sorted.minOf { it.kg },
        max = sorted.maxOf { it.kg },
        count = sorted.size,
    )
}
