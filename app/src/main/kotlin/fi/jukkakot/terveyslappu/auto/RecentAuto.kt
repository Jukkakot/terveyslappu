package fi.jukkakot.terveyslappu.auto

import fi.jukkakot.terveyslappu.weight.WeightEntry
import java.time.Duration
import java.time.Instant

/**
 * Which weight the weigh-in dialog shows instead of scanning (scale-weigh-in "Open on a recent
 * automatic weight", everyday-feedback design 4). Pure.
 */
object RecentAuto {
    /** How long after an automatic save the dialog shows it (user 2026-11-19). */
    val WINDOW: Duration = Duration.ofMinutes(2)

    /** The cap on the Health Connect check before scanning starts anyway. */
    const val READ_CAP_MS = 1_500L

    private const val SCALE_PREFIX = "terveyslappu-scale-"

    /**
     * The newest of [entries] that this app saved in the background ([isAuto] by the raw entry's
     * second: `via=auto`; the dialog writes the same Health Connect metadata), whose weighing time
     * is at most [WINDOW] before [now] and which is not in [marked] ("?"); null when none.
     */
    fun pick(entries: List<WeightEntry>, now: Instant, isAuto: (second: Long) -> Boolean, marked: Set<String>): WeightEntry? =
        entries
            .filter { it.own && it.clientRecordId?.startsWith(SCALE_PREFIX) == true }
            .filter { it.clientRecordId !in marked }
            .filter { !it.time.isBefore(now.minus(WINDOW)) && !it.time.isAfter(now.plusSeconds(60)) }
            .filter { isAuto(it.time.epochSecond) }
            .maxByOrNull { it.time }
}
