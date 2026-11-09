package fi.jukkakot.terveyslappu.chart

/**
 * Whether a weight chart draws its weights as dots (weight-chart "Dots when they fit"). Chart guides
 * put markers on sparse, discrete points and leave them off dense daily series but give no number,
 * so the rule is by room: dots (5–7 dp) need [MIN_DP_PER_WEIGHT] of width each or they crowd the line.
 */
object ChartDots {
    const val MIN_DP_PER_WEIGHT = 10f

    fun fit(count: Int, widthDp: Float): Boolean = count <= 1 || widthDp / count >= MIN_DP_PER_WEIGHT
}
