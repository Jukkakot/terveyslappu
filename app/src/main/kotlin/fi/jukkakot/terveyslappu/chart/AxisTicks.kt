package fi.jukkakot.terveyslappu.chart

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToLong

/**
 * The shared axis rule of Historia's charts (steps-source-and-axes design 8): three y values
 * (bottom, middle, top) and 3 to 5 evenly spread dates. Pure and free of Compose types, so every
 * chart (and custom-charts later) uses the same numbers.
 */
object AxisTicks {
    /** Bars: 0, the middle and a round top above [max] (top a multiple of 2 × [step]). */
    fun bars(max: Double, step: Double): List<Double> {
        val top = max(2 * step * ceil(max * 1.1 / (2 * step)), 2 * step)
        return listOf(0.0, top / 2, top)
    }

    /** A weight or BMI line: the scale's ends and their midpoint. */
    fun line(min: Double, max: Double): List<Double> = listOf(min, (min + max) / 2, max)

    fun days(from: LocalDate, to: LocalDate): Long = ChronoUnit.DAYS.between(from, to)

    /**
     * [count] dates (clamped to 3..5) from [from] to [to], evenly by days, the first at [from] and the
     * last at [to]; a period of 1 or 2 days gives its 1 or 2 days.
     */
    fun dates(from: LocalDate, to: LocalDate, count: Int): List<LocalDate> {
        val span = days(from, to)
        if (span <= 0) return listOf(from)
        if (span == 1L) return listOf(from, to)
        val n = count.coerceIn(MIN_DATES, MAX_DATES).coerceAtMost((span + 1).toInt())
        return (0 until n).map { i -> from.plusDays((i * span.toDouble() / (n - 1)).roundToLong()) }.distinct()
    }

    /** Month and year ("9/2022") when the period holds 365 days or more (1 v included), else day and month. */
    fun monthYear(from: LocalDate, to: LocalDate): Boolean = days(from, to) + 1 >= YEAR_DAYS

    /** The centre of [day]'s slot as a fraction of the plot width (the bars' x). */
    fun position(from: LocalDate, to: LocalDate, day: LocalDate): Float {
        val slots = days(from, to) + 1
        return ((days(from, day) + 0.5) / slots).toFloat()
    }

    /**
     * The horizontal bounds of the date labels on a plot [plotWidth] wide: the first start-aligned,
     * the last end-aligned, the others centred on their day's slot.
     */
    fun labelBounds(from: LocalDate, to: LocalDate, dates: List<LocalDate>, plotWidth: Float, measure: (LocalDate) -> Float): List<ClosedFloatingPointRange<Float>> =
        dates.mapIndexed { i, day ->
            val w = measure(day)
            when {
                i == 0 -> 0f..w
                i == dates.lastIndex -> (plotWidth - w)..plotWidth
                else -> {
                    val x = position(from, to, day) * plotWidth
                    (x - w / 2)..(x + w / 2)
                }
            }
        }

    /** The most dates (5, 4 or 3) whose measured labels keep at least [gap] between neighbours; 3 if none fits. */
    fun fitCount(from: LocalDate, to: LocalDate, plotWidth: Float, gap: Float, measure: (LocalDate) -> Float): Int {
        for (n in MAX_DATES downTo MIN_DATES + 1) {
            val bounds = labelBounds(from, to, dates(from, to, n), plotWidth, measure)
            if (bounds.zipWithNext().all { (a, b) -> b.start - a.endInclusive >= gap }) return n
        }
        return MIN_DATES
    }

    /** A period's first and last day at [today], as the bar charts draw it; Kaikki starts at [firstDay] (or today). */
    fun periodDates(period: Period, today: LocalDate, firstDay: LocalDate?): Pair<LocalDate, LocalDate> = when (period) {
        is Period.Last -> today.minusDays(period.days - 1) to today
        Period.All -> (firstDay?.takeIf { it <= today } ?: today) to today
        is Period.Custom -> period.from to period.to
    }

    const val MIN_DATES = 3
    const val MAX_DATES = 5
    const val YEAR_DAYS = 365
}
