package fi.jukkakot.terveyslappu.auto

import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.roundToLong

/** One accepted own weight the reference is chosen from: its `clientRecordId`, time (epoch ms) and kg. */
@Serializable
data class RefPair(val id: String, val timeMs: Long, val kg: Double)

/**
 * The reference store's content (design 4): the latest [ReferenceRule.KEEP] accepted own weights and
 * whether the list is known at all ([known] false right after the update, before the first
 * foreground read).
 */
@Serializable
data class ReferenceState(val known: Boolean = false, val pairs: List<RefPair> = emptyList())

/** What an automatic weight is compared with (background-weigh-in "Reference weight"). */
sealed interface Reference {
    /** The store was never seeded: the weight is saved and marked "?". */
    data object Unknown : Reference
    /** Known and empty: this app has no accepted weight, so the new one is accepted. */
    data object None : Reference
    data class Weight(val kg: Double, val timeMs: Long) : Reference
}

/** The reference list's rules: every app-side change updates the list, the reference is recomputed. Pure. */
object ReferenceRule {
    const val KEEP = 10

    /** The latest accepted pair before [beforeMs]. */
    fun reference(state: ReferenceState, beforeMs: Long): Reference {
        if (!state.known) return Reference.Unknown
        val pair = state.pairs.filter { it.timeMs < beforeMs }.maxByOrNull { it.timeMs } ?: return Reference.None
        return Reference.Weight(pair.kg, pair.timeMs)
    }

    /** A save, undo, import or acceptance: the pair joins the list (replacing one with its id). */
    fun accepted(state: ReferenceState, pairs: Collection<RefPair>): ReferenceState {
        if (pairs.isEmpty()) return state
        val ids = pairs.map { it.id }.toSet()
        return state.copy(pairs = (state.pairs.filter { it.id !in ids } + pairs).sortedBy { it.timeMs }.takeLast(KEEP))
    }

    /** A delete (or a weight becoming "?"): the reference falls back to the previous pair. */
    fun removed(state: ReferenceState, id: String): ReferenceState = state.copy(pairs = state.pairs.filter { it.id != id })

    /** A foreground read of the own accepted weights replaces the list and makes it known. */
    fun seeded(pairs: Collection<RefPair>): ReferenceState = ReferenceState(true, pairs.sortedBy { it.timeMs }.takeLast(KEEP))
}

/** The "?" rule (background-weigh-in "Questioned weight"). Pure. */
object Plausibility {
    const val DEFAULT_LIMIT = 5.0
    const val MIN_LIMIT = 0.5
    const val MAX_LIMIT = 20.0
    const val STEP = 0.5

    /** True when [kg] is to be marked "?": unknown reference, or strictly over [limit] from it. */
    fun questioned(reference: Reference, kg: Double, limit: Double): Boolean = when (reference) {
        Reference.Unknown -> true
        Reference.None -> false
        is Reference.Weight -> !within(reference.kg, kg, limit)
    }

    /** Within the limit, compared in hundredths of a kilo so 87,0 vs 82,0 at 5 kg is exactly the limit. */
    fun within(a: Double, b: Double, limit: Double): Boolean = (abs(a - b) * 100).roundToLong() <= (limit * 100).roundToLong()

    /** A limit as stored or chosen, snapped to 0.5 kg steps within 0.5-20 kg. */
    fun limit(kg: Double): Double = ((kg / STEP).roundToLong() * STEP).coerceIn(MIN_LIMIT, MAX_LIMIT)
}

/** One "?" weight: its own record id, time (epoch ms) and kg (design 5: the chain needs no read). */
@Serializable
data class Mark(val id: String, val timeMs: Long, val kg: Double)

/** Chained acceptance (design 7). Pure. */
object AcceptChain {
    /**
     * The later "?" weights accepted along with [anchor]: walking forward in time from it, each
     * within [limit] of the previous accepted weight of the chain is accepted; the first outside
     * stops the chain. [marks] may hold the anchor itself and earlier ones; they are skipped.
     */
    fun chain(anchor: Mark, marks: Collection<Mark>, limit: Double): List<Mark> {
        val later = marks.filter { it.id != anchor.id && it.timeMs > anchor.timeMs }.sortedBy { it.timeMs }
        val accepted = mutableListOf<Mark>()
        var previous = anchor.kg
        for (mark in later) {
            if (!Plausibility.within(previous, mark.kg, limit)) break
            accepted += mark
            previous = mark.kg
        }
        return accepted
    }
}
