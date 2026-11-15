package fi.jukkakot.terveyslappu.food

import fi.jukkakot.terveyslappu.chart.Period
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlin.math.roundToInt

/** A logged day (at least one item with kcal): its total and treat kcal. */
data class EnergyDay(val day: LocalDate, val total: Int, val treat: Int)

/** One bar: a day, or a week from Monday ([days] = 7) holding the average per logged day. */
data class EnergyBar(val from: LocalDate, val days: Int, val total: Int, val treat: Int)

data class EnergyStats(val averageKcal: Int?, val averageTreat: Int?, val loggedDays: Int, val periodDays: Int)

/**
 * The Energia tab's pure data (weight-chart "Energia chart", "Energia statistics", "Energia list"):
 * bars for logged days only (weekly from 90 days up), statistics (their average is the chart's flat
 * line) and the logged-day list. Today is listed and drawn but left out of the averages, the line
 * and the week bars unless it is its week's only day.
 */
class EnergySeries(
    val from: LocalDate,
    val to: LocalDate,
    val weekly: Boolean,
    val bars: List<EnergyBar>,
    val stats: EnergyStats,
    /** Newest first. */
    val days: List<EnergyDay>,
) {
    companion object {
        private const val WEEKLY_FROM_DAYS = 90

        fun loggedDays(items: Collection<FoodItem>): List<EnergyDay> = items.groupBy { it.day }
            .mapNotNull { (day, list) -> DaySum.of(list).takeIf { it.logged }?.let { EnergyDay(day, it.total, it.treat) } }
            .sortedBy { it.day }

        /** Own charts' entry point (custom-charts design 7): each logged day of [from]..[to] with its own kcal, never weekly. */
        fun daily(items: Collection<FoodItem>, from: LocalDate, to: LocalDate): List<EnergyDay> = loggedDays(items).filter { it.day in from..to }

        /** The period's first and last day at [today]; Kaikki starts at the first log (or today). */
        fun dates(period: Period, today: LocalDate, firstLog: LocalDate?): Pair<LocalDate, LocalDate> =
            fi.jukkakot.terveyslappu.chart.AxisTicks.periodDates(period, today, firstLog)

        fun of(items: Collection<FoodItem>, period: Period, today: LocalDate): EnergySeries {
            val all = loggedDays(items)
            val (from, to) = dates(period, today, all.firstOrNull()?.day)
            val days = all.filter { it.day in from..to }
            val periodDays = (ChronoUnit.DAYS.between(from, to) + 1).toInt()
            val weekly = periodDays > WEEKLY_FROM_DAYS
            val bars = if (weekly) weeks(days, today) else days.map { EnergyBar(it.day, 1, it.total, it.treat) }
            val counted = days.filter { it.day != today }
            val stats = EnergyStats(
                averageKcal = counted.takeIf { it.isNotEmpty() }?.map { it.total }?.average()?.roundToInt(),
                averageTreat = counted.takeIf { it.isNotEmpty() }?.map { it.treat }?.average()?.roundToInt(),
                loggedDays = days.size,
                periodDays = periodDays,
            )
            return EnergySeries(from, to, weekly, bars, stats, days.reversed())
        }

        private fun weeks(days: List<EnergyDay>, today: LocalDate): List<EnergyBar> = days
            .groupBy { it.day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
            .map { (monday, all) ->
                val list = all.filter { it.day != today }.ifEmpty { all }
                EnergyBar(monday, 7, list.map { it.total }.average().roundToInt(), list.map { it.treat }.average().roundToInt())
            }
            .sortedBy { it.from }
    }
}
