package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.weight.WeightEntry
import java.time.Duration
import java.time.Instant

/** What the home screen card and list show (home-screen spec, "Simple home screen"). */
data class HomeSummary(
    val latest: WeightEntry,
    /** Latest minus the weight before it, kg; null with a single weight. */
    val change: Double?,
    /** The chosen period's weights, oldest first, for the sparkline; empty when the latest is older. */
    val spark: List<WeightEntry>,
    /** Last minus first of [spark]; null with fewer than two points. */
    val sparkChange: Double?,
    /** The three latest weights, newest first, "?" weights included. */
    val recent: List<WeightEntry>,
    /** The sparkline's x range: the whole period ending at [sparkTo]. */
    val sparkFrom: Instant,
    val sparkTo: Instant,
    /** The sparkline's y range ([WeightScale]); null when [spark] is empty. */
    val sparkScale: KgRange?,
) {
    companion object {
        /** The home card's period choices in days (1 vk, 1 kk, 3 kk) and the first-time one. */
        val HOME_DAYS: List<Long> = listOf(7, 30, 90)
        const val DEFAULT_DAYS = 7L

        /** [days] when it is a choice, else the default. */
        fun homeDays(days: Long): Long = if (days in HOME_DAYS) days else DEFAULT_DAYS

        /**
         * Summary of [weights] in any order over the last [days], or null when none is accepted.
         * Weights in [questioned] (entry ids, marked "?") are left out of everything but [recent].
         */
        fun of(weights: List<WeightEntry>, now: Instant, days: Long = DEFAULT_DAYS, questioned: Set<String> = emptySet()): HomeSummary? {
            val all = weights.sortedByDescending { it.time }
            val newest = all.filter { it.id !in questioned }
            if (newest.isEmpty()) return null
            val latest = newest.first()
            val since = now.minus(Duration.ofDays(days))
            val spark = newest.filter { !it.time.isBefore(since) }.reversed()
            return HomeSummary(
                latest = latest,
                change = newest.getOrNull(1)?.let { latest.kg - it.kg },
                spark = spark,
                sparkChange = if (spark.size > 1) spark.last().kg - spark.first().kg else null,
                recent = all.take(3),
                sparkFrom = since,
                sparkTo = maxOf(now, latest.time),
                sparkScale = if (spark.isEmpty()) null else WeightScale.of(spark.map { it.kg }),
            )
        }
    }
}
