package fi.jukkakot.terveyslappu.chart

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round

/** A chart's y range in the shown quantity's unit (kg or BMI). */
data class KgRange(val min: Double, val max: Double) {
    val span: Double get() = max - min
}

/**
 * The y-scale of every weight chart (weight-chart "Weight scale"): fitted to the data (user
 * 2026-11-16): the weights with [ROOM] on each side, at least [MIN_SPAN] wide, rounded outwards to
 * half units, and widened by half a unit at the top when the middle axis value would be off the
 * half-unit grid. Daily water swings look bigger than on the old 4 kg scale; the user chose fit
 * over calm. The same rule on every screen and period keeps a change looking the same size.
 */
object WeightScale {
    const val ROOM = 0.2
    const val MIN_SPAN = 1.0

    /**
     * [perKg]: how many units of the shown quantity one kg is worth (1 for weight, 1/h² for BMI),
     * so room and least span mean the same change on every chart. Rounding is in whole and half
     * units of the shown quantity.
     */
    fun of(values: Collection<Double>, perKg: Double = 1.0): KgRange {
        require(values.isNotEmpty())
        val padLo = values.min() - ROOM * perKg
        val padHi = values.max() + ROOM * perKg
        var lo = floorHalf(padLo)
        var hi = ceilHalf(padHi)
        val minSpan = MIN_SPAN * perKg
        if (hi - lo < minSpan - EPS) {
            val centre = (padLo + padHi) / 2
            lo = floorHalf(centre - minSpan / 2)
            hi = lo + minSpan
            if (hi < values.max() + EPS) hi = ceilHalf(padHi)
            hi = ceilHalf(hi)
        }
        // Whole-unit span puts the middle value on the half grid.
        val span = hi - lo
        if (abs(span - round(span)) > EPS) hi += 0.5
        return KgRange(lo, hi)
    }

    private fun floorHalf(x: Double) = floor(x * 2 + EPS) / 2
    private fun ceilHalf(x: Double) = ceil(x * 2 - EPS) / 2

    /** Float noise must not push an exact half unit to the next one. */
    private const val EPS = 1e-9
}
