package fi.jukkakot.terveyslappu.derived

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * A value in whole tenths as the app shows it: rounded exactly like `%.1f` (half up on the value's
 * decimal text), so 27.049999999999997 is 270. Display and arithmetic share it (weight-chart
 * "Changes from shown weights").
 */
fun shownTenths(value: Double): Long =
    BigDecimal(value.toString()).setScale(1, RoundingMode.HALF_UP).movePointRight(1).toLong()

/** The difference of two values as shown, in whole tenths. */
fun shownDeltaTenths(from: Double, to: Double): Long = shownTenths(to) - shownTenths(from)

/** [shownDeltaTenths] as a number with one decimal (a multiple of 0.1). */
fun shownDelta(from: Double, to: Double): Double = shownDeltaTenths(from, to) / 10.0
