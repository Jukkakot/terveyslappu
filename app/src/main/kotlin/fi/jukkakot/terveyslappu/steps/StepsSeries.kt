package fi.jukkakot.terveyslappu.steps

import fi.jukkakot.terveyslappu.chart.Period
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlin.math.roundToInt

/** A day for which Health Connect holds steps: its merged daily total (steps design 1). */
data class StepsDay(val day: LocalDate, val steps: Long)

/** One bar: a day, or a week from Monday ([days] = 7) holding the average per day with steps. */
data class StepsBar(val from: LocalDate, val days: Int, val steps: Long)

data class StepsPoint(val day: LocalDate, val steps: Long)

data class StepsStats(val averageSteps: Long?, val stepDays: Int, val periodDays: Int)

/**
 * The Askeleet tab's pure data (weight-chart "Askeleet chart", "Askeleet statistics", "Askeleet
 * list"; steps "Days and gaps"): bars for days with steps only (weekly over 90 days), the 7-day
 * average over days with steps (≥3 in the window, daily mode only), statistics and the day list.
 * A missing day is a gap. Today is listed and drawn but left out of the averages and the line; it
 * counts as a day with steps.
 */
class StepsSeries(
    val from: LocalDate,
    val to: LocalDate,
    val weekly: Boolean,
    val bars: List<StepsBar>,
    val average: List<StepsPoint>,
    val stats: StepsStats,
    /** Newest first. */
    val days: List<StepsDay>,
) {
    companion object {
        private const val WEEKLY_FROM_DAYS = 90
        private const val WINDOW = 7
        private const val MIN_IN_WINDOW = 3

        /** The period's first and last day at [today]; Kaikki starts at the first day with steps (or today). */
        fun dates(period: Period, today: LocalDate, firstDay: LocalDate?): Pair<LocalDate, LocalDate> = when (period) {
            is Period.Last -> today.minusDays(period.days - 1) to today
            Period.All -> (firstDay?.takeIf { it <= today } ?: today) to today
            is Period.Custom -> period.from to period.to
        }

        /** [input] in any order; days with no steps (0 or less) count as gaps. */
        fun of(input: Collection<StepsDay>, period: Period, today: LocalDate): StepsSeries {
            val all = input.filter { it.steps > 0 }.distinctBy { it.day }.sortedBy { it.day }
            val (from, to) = dates(period, today, all.firstOrNull()?.day)
            val days = all.filter { it.day in from..to }
            val periodDays = (ChronoUnit.DAYS.between(from, to) + 1).toInt()
            val weekly = periodDays > WEEKLY_FROM_DAYS
            val counted = days.filter { it.day != today }
            val bars = if (weekly) weeks(days, today) else days.map { StepsBar(it.day, 1, it.steps) }
            val countedAll = all.filter { it.day != today }.associateBy { it.day }
            val average = if (weekly) {
                emptyList()
            } else {
                counted.mapNotNull { d ->
                    val window = (0 until WINDOW).mapNotNull { countedAll[d.day.minusDays(it.toLong())] }
                    if (window.size >= MIN_IN_WINDOW) StepsPoint(d.day, window.map { it.steps }.average().roundToInt().toLong()) else null
                }
            }
            val stats = StepsStats(
                averageSteps = counted.takeIf { it.isNotEmpty() }?.map { it.steps }?.average()?.roundToInt()?.toLong(),
                stepDays = days.size,
                periodDays = periodDays,
            )
            return StepsSeries(from, to, weekly, bars, average, stats, days.reversed())
        }

        /** A week's bar: the average per day with steps, today left out unless it is the week's only day. */
        private fun weeks(days: List<StepsDay>, today: LocalDate): List<StepsBar> = days
            .groupBy { it.day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
            .map { (monday, list) ->
                val counted = list.filter { it.day != today }.ifEmpty { list }
                StepsBar(monday, 7, counted.map { it.steps }.average().roundToInt().toLong())
            }
            .sortedBy { it.from }
    }
}

/**
 * What the front page's steps card shows (home-screen "Steps card on the home screen"): today's
 * steps, the daily bars of the last `days` days ending today and the average per day with steps
 * (today left out; the numbers come from [StepsSeries]).
 */
data class HomeSteps(
    /** Today's steps; null when Health Connect holds none for today ("ei tietoa"). */
    val today: Long?,
    val from: LocalDate,
    val to: LocalDate,
    /** One bar per day with steps, oldest first. */
    val bars: List<StepsBar>,
    val averageSteps: Long?,
) {
    val noDataToday: Boolean get() = today == null

    companion object {
        fun of(input: Collection<StepsDay>, days: Long, today: LocalDate): HomeSteps {
            val series = StepsSeries.of(input, Period.Last(days), today)
            val todaySteps = series.days.firstOrNull { it.day == today }?.steps
            return HomeSteps(todaySteps, series.from, series.to, series.bars, series.stats.averageSteps)
        }
    }
}
