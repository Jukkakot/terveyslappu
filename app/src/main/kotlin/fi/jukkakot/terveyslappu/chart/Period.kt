package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.weight.WeightEntry
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** The time span the chart, stats and list show (weight-chart). */
sealed interface Period {
    /** The last [days] days up to now. */
    data class Last(val days: Long) : Period

    data object All : Period

    /** [from] to [to], both days included. */
    data class Custom(val from: LocalDate, val to: LocalDate) : Period

    companion object {
        val WEEK = Last(7)
        val MONTH = Last(30)
        val QUARTER = Last(90)
        val YEAR = Last(365)

        /** The chips in their order; Oma comes after them. */
        val PRESETS: List<Period> = listOf(WEEK, MONTH, QUARTER, YEAR, All)

        /** The first-time choice. */
        val DEFAULT: Period = WEEK

        /** Reads [encode]'s text back; anything unknown gives [DEFAULT]. */
        fun decode(text: String?): Period {
            if (text == null) return DEFAULT
            if (text == "all") return All
            text.removePrefix("last:").takeIf { it != text }?.toLongOrNull()?.let { if (it > 0) return Last(it) }
            val parts = text.split(':')
            if (parts.size == 3 && parts[0] == "custom") {
                val from = runCatching { LocalDate.parse(parts[1]) }.getOrNull()
                val to = runCatching { LocalDate.parse(parts[2]) }.getOrNull()
                if (from != null && to != null && !to.isBefore(from)) return Custom(from, to)
            }
            return DEFAULT
        }
    }
}

/** Text kept in preferences: "last:7", "all" or "custom:2026-09-01:2026-10-15". */
fun Period.encode(): String = when (this) {
    is Period.Last -> "last:$days"
    Period.All -> "all"
    is Period.Custom -> "custom:$from:$to"
}

/** Start (null: no start) and end (exclusive) of the period at [now]. */
fun Period.range(now: Instant, zone: ZoneId): Pair<Instant?, Instant> = when (this) {
    is Period.Last -> now.minus(Duration.ofDays(days)) to now.plusSeconds(60)
    Period.All -> null to now.plusSeconds(60)
    is Period.Custom -> from.atStartOfDay(zone).toInstant() to to.plusDays(1).atStartOfDay(zone).toInstant()
}

/** The [entries] inside the period, in their given order. */
fun Period.filter(entries: List<WeightEntry>, now: Instant, zone: ZoneId): List<WeightEntry> {
    val (start, end) = range(now, zone)
    return entries.filter { (start == null || !it.time.isBefore(start)) && it.time.isBefore(end) }
}

/** Whether the period starts more than 30 days back, so it needs the history permission. */
fun Period.needsHistory(now: Instant, zone: ZoneId): Boolean {
    val start = range(now, zone).first ?: return true
    return start.isBefore(now.minus(Duration.ofDays(30)))
}
