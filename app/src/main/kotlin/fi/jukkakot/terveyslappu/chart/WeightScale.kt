package fi.jukkakot.terveyslappu.chart

import kotlin.math.ceil
import kotlin.math.floor

/** A chart's y range in kg. */
data class KgRange(val min: Double, val max: Double) {
    val span: Double get() = max - min
}

/**
 * The y-scale of every weight chart (weight-chart "Weight scale"): the weights with [ROOM] on each
 * side, widened evenly to at least [MIN_SPAN] (daily weight moves 0.5–2 kg with water and glycogen,
 * so a narrower scale makes that noise look like a trend), rounded out to whole kilos. The same
 * rule on every screen and period keeps a change looking the same size everywhere.
 */
object WeightScale {
    const val ROOM = 0.5
    const val MIN_SPAN = 4.0

    fun of(kgs: Collection<Double>): KgRange {
        require(kgs.isNotEmpty())
        var lo = kgs.min() - ROOM
        var hi = kgs.max() + ROOM
        val missing = MIN_SPAN - (hi - lo)
        if (missing > 0) {
            lo -= missing / 2
            hi += missing / 2
        }
        return KgRange(floor(lo + EPS), ceil(hi - EPS))
    }

    /** Float noise must not push an exact whole kilo to the next one. */
    private const val EPS = 1e-9
}
