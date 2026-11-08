package fi.jukkakot.terveyslappu.scale

import fi.jukkakot.terveyslappu.weight.MAX_KG
import fi.jukkakot.terveyslappu.weight.MIN_KG

/** The weight a weigh-in settled on, and when (ms since the scan started) it first read stable. */
data class StableResult(val kg: Double, val atMs: Long)

/**
 * Decides the weight of one weigh-in from the scale's readings (scale-weigh-in spec, "Decide the
 * stable weight"). The scale's stable flag is the truth: two stable readings of the same weight
 * in a row decide it, or one stable reading followed by [quietMs] without a different reading.
 * Any different reading drops the candidate. Weights outside 20–300 kg are never a result.
 * Pure and time-injected, so a logged packet stream replays exactly.
 */
class StableWeight(private val quietMs: Long = DEFAULT_QUIET_MS) {

    private var candidate: StableResult? = null

    var result: StableResult? = null
        private set

    /** Feeds one reading received at [atMs]; returns the result once decided. */
    fun onReading(reading: ScaleReading, atMs: Long): StableResult? {
        result?.let { return it }
        val current = candidate
        if (current != null && current.kg == reading.kg) {
            // Same weight again: a second stable reading confirms it, an unstable one only counts as quiet.
            return if (reading.stable || atMs - current.atMs >= quietMs) decide(current) else null
        }
        candidate = if (reading.stable && reading.kg in MIN_KG..MAX_KG) StableResult(reading.kg, atMs) else null
        return null
    }

    /** Called with the current time when no packet arrives (a tick); decides after [quietMs]. */
    fun onTick(nowMs: Long): StableResult? {
        result?.let { return it }
        val current = candidate ?: return null
        return if (nowMs - current.atMs >= quietMs) decide(current) else null
    }

    private fun decide(value: StableResult): StableResult {
        result = value
        return value
    }

    companion object {
        const val DEFAULT_QUIET_MS = 2_000L
    }
}
