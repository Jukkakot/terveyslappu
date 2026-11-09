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

/**
 * Stats of [entries] in any order, or null when none has a value. [value] gives the shown quantity
 * (kg by default; null leaves the entry out); [rate] false leaves out the per-week rate.
 */
fun periodStats(entries: List<WeightEntry>, value: (WeightEntry) -> Double? = { it.kg }, rate: Boolean = true): PeriodStats? {
    val points = entries.sortedBy { it.time }.mapNotNull { e -> value(e)?.let { e.time to it } }
    if (points.isEmpty()) return null
    val (firstTime, first) = points.first()
    val (latestTime, latest) = points.last()
    val values = points.map { it.second }
    val change = if (points.size > 1) latest - first else null
    val days = Duration.between(firstTime, latestTime).seconds / DAY_SECONDS
    val perWeek = if (rate && change != null && days >= 1.0) change / days * 7 else null
    return PeriodStats(
        latest = latest,
        change = change,
        perWeek = perWeek,
        average = values.average(),
        min = values.min(),
        max = values.max(),
        count = points.size,
    )
}
