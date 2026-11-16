package fi.jukkakot.terveyslappu.chart

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ceil
import kotlin.math.max

/**
 * The shared axis rule of Historia's charts (steps-source-and-axes design 8): three y values
 * (bottom, middle, top), the date formats' thresholds and the label count bounds; the library
 * places the date labels (charts-on-mpandroidchart design 4). Pure.
 */
object AxisTicks {
    /** Bars: 0, the middle and a round top above [max] (top a multiple of 2 × [step]). */
    fun bars(max: Double, step: Double): List<Double> {
        val top = max(2 * step * ceil(max * 1.1 / (2 * step)), 2 * step)
        return listOf(0.0, top / 2, top)
    }

    /** A weight or BMI line: the scale's ends and their midpoint. */
    fun line(min: Double, max: Double): List<Double> = listOf(min, (min + max) / 2, max)

    /** The period range line (period-range-line): "15.8.2026 – 15.11.2026", one date when [from] equals [to]. */
    fun rangeText(from: LocalDate, to: LocalDate): String =
        if (from == to) dayYear(from) else "${dayYear(from)} – ${dayYear(to)}"

    private fun dayYear(day: LocalDate) = "${day.dayOfMonth}.${day.monthValue}.${day.year}"

    fun days(from: LocalDate, to: LocalDate): Long = ChronoUnit.DAYS.between(from, to)

    /** "Ma", "Ti" ... "Su". */
    fun weekday(day: LocalDate): String = WEEKDAYS[day.dayOfWeek.value - 1]

    /** Month and year ("9/2022") when the period holds 365 days or more (1 v included), else day and month. */
    fun monthYear(from: LocalDate, to: LocalDate): Boolean = days(from, to) + 1 >= YEAR_DAYS

    /** A period's first and last day at [today], as the bar charts draw it; Kaikki starts at [firstDay] (or today). */
    fun periodDates(period: Period, today: LocalDate, firstDay: LocalDate?): Pair<LocalDate, LocalDate> = when (period) {
        is Period.Last -> today.minusDays(period.days - 1) to today
        Period.All -> (firstDay?.takeIf { it <= today } ?: today) to today
        is Period.Custom -> period.from to period.to
    }

    const val WEEK_DAYS = 7
    private val WEEKDAYS = listOf("Ma", "Ti", "Ke", "To", "Pe", "La", "Su")
    const val MIN_DATES = 3
    const val MAX_DATES = 5
    const val YEAR_DAYS = 365
}
