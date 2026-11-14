package fi.jukkakot.terveyslappu.listen

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/** Where now is against the window: inside one (its start and end), or outside until [nextStart]. */
sealed interface WindowPlan {
    data class Inside(val start: Instant, val end: Instant, val nextCheck: Instant) : WindowPlan
    data class Outside(val nextStart: Instant) : WindowPlan
}

/**
 * The daily listening window (background-weigh-in "Trial setting"): [start] to [end] in the
 * phone's zone; an end earlier than the start runs over midnight, start == end is refused.
 * Times go through `java.time`, so a start in a DST gap moves forward with the clock. Pure.
 */
data class ListenWindow(val start: LocalTime, val end: LocalTime) {
    init {
        require(valid(start, end)) { "start equals end" }
    }

    private fun startAt(day: LocalDate, zone: ZoneId): Instant = day.atTime(start).atZone(zone).toInstant()

    private fun endAt(startDay: LocalDate, zone: ZoneId): Instant {
        val endDay = if (end.isAfter(start)) startDay else startDay.plusDays(1)
        return endDay.atTime(end).atZone(zone).toInstant()
    }

    /** The window that contains [now] (start, end), or null. */
    fun current(now: Instant, zone: ZoneId): Pair<Instant, Instant>? {
        val today = now.atZone(zone).toLocalDate()
        for (day in listOf(today, today.minusDays(1))) {
            val s = startAt(day, zone)
            val e = endAt(day, zone)
            if (!now.isBefore(s) && now.isBefore(e)) return s to e
        }
        return null
    }

    /** The first window start after [now]. */
    fun nextStart(now: Instant, zone: ZoneId): Instant {
        val today = now.atZone(zone).toLocalDate()
        return (0L..2L).map { startAt(today.plusDays(it), zone) }.first { it.isAfter(now) }
    }

    /** Inside: the window and the next hourly check (never after the end); outside: the next start. */
    fun plan(now: Instant, zone: ZoneId): WindowPlan {
        val window = current(now, zone) ?: return WindowPlan.Outside(nextStart(now, zone))
        val check = now.plus(CHECK_EVERY).let { if (it.isAfter(window.second)) window.second else it }
        return WindowPlan.Inside(window.first, window.second, check)
    }

    companion object {
        val CHECK_EVERY: Duration = Duration.ofHours(1)
        val DEFAULT_START: LocalTime = LocalTime.of(6, 0)
        val DEFAULT_END: LocalTime = LocalTime.of(10, 0)

        fun valid(start: LocalTime, end: LocalTime): Boolean = start != end
    }
}
