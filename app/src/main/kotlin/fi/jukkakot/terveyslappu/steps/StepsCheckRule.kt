package fi.jukkakot.terveyslappu.steps

import java.time.Duration
import java.time.Instant
import java.time.LocalDate

/** When the source check runs (steps-source-and-axes design 4). Pure. */
object StepsCheckRule {
    val RETRY_AFTER: Duration = Duration.ofHours(1)
    val SUB_SCREEN_GAP: Duration = Duration.ofMinutes(1)

    /**
     * Due on a [foreground] read when no check succeeded [today] ([autoPickDay] = the day of the last
     * successful one); after a failure ([lastFailureAt]) not before an hour has passed.
     */
    fun due(today: LocalDate, autoPickDay: LocalDate?, lastFailureAt: Instant?, now: Instant, foreground: Boolean): Boolean {
        if (!foreground || autoPickDay == today) return false
        return lastFailureAt == null || !now.isBefore(lastFailureAt.plus(RETRY_AFTER))
    }

    /** Entering the sub-screen checks unless the last check (any outcome) finished under a minute ago. */
    fun subScreenAllowed(lastFinishedAt: Instant?, now: Instant): Boolean =
        lastFinishedAt == null || !now.isBefore(lastFinishedAt.plus(SUB_SCREEN_GAP))
}
