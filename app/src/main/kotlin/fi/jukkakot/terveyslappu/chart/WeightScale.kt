package fi.jukkakot.terveyslappu.chart

import kotlin.math.ceil
import kotlin.math.floor

/** A chart's y range in the shown quantity's unit (kg, BMI or kcal). */
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

    /**
     * [perKg]: how many units of the shown quantity one kg is worth (1 for weight, 1/h² for BMI,
     * 10 for kcal), so room and least span mean the same change on every chart; [step]: rounding.
     */
    fun of(values: Collection<Double>, perKg: Double = 1.0, step: Double = 1.0): KgRange {
        require(values.isNotEmpty())
        var lo = values.min() - ROOM * perKg
        var hi = values.max() + ROOM * perKg
        val missing = MIN_SPAN * perKg - (hi - lo)
        if (missing > 0) {
            lo -= missing / 2
            hi += missing / 2
        }
        return KgRange(floor(lo / step + EPS) * step, ceil(hi / step - EPS) * step)
    }

    /** Float noise must not push an exact whole kilo to the next one. */
    private const val EPS = 1e-9
}
