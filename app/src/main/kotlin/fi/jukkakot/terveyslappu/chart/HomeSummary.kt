package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.weight.WeightEntry
import java.time.Duration
import java.time.Instant

/** What the home screen card and list show (home-screen spec, "Simple home screen"). */
data class HomeSummary(
    val latest: WeightEntry,
    /** Latest minus the weight before it, kg; null with a single weight. */
    val change: Double?,
    /** The last 30 days' weights, oldest first, for the sparkline; empty when the latest is older. */
    val spark: List<WeightEntry>,
    /** Last minus first of [spark]; null with fewer than two points. */
    val sparkChange: Double?,
    /** The three latest weights, newest first. */
    val recent: List<WeightEntry>,
    /** The sparkline's x range: the whole 30 days ending at [sparkTo]. */
    val sparkFrom: Instant,
    val sparkTo: Instant,
    /** The sparkline's y range ([WeightScale]); null when [spark] is empty. */
    val sparkScale: KgRange?,
) {
    companion object {
        val SPARK_DAYS: Duration = Duration.ofDays(30)

        /** Summary of [weights] in any order, or null when there are none. */
        fun of(weights: List<WeightEntry>, now: Instant): HomeSummary? {
            if (weights.isEmpty()) return null
            val newest = weights.sortedByDescending { it.time }
            val latest = newest.first()
            val since = now.minus(SPARK_DAYS)
            val spark = newest.filter { !it.time.isBefore(since) }.reversed()
            return HomeSummary(
                latest = latest,
                change = newest.getOrNull(1)?.let { latest.kg - it.kg },
                spark = spark,
                sparkChange = if (spark.size > 1) spark.last().kg - spark.first().kg else null,
                recent = newest.take(3),
                sparkFrom = since,
                sparkTo = maxOf(now, latest.time),
                sparkScale = if (spark.isEmpty()) null else WeightScale.of(spark.map { it.kg }),
            )
        }
    }
}
