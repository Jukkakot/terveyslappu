package fi.jukkakot.terveyslappu.burn

import fi.jukkakot.terveyslappu.chart.Period
import fi.jukkakot.terveyslappu.steps.GarminDay
import fi.jukkakot.terveyslappu.steps.GarminMerge
import fi.jukkakot.terveyslappu.steps.StepsSourceState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlin.math.roundToLong

/** How a day's burned energy is known (energy-burn "Burned energy is read, never computed or written"). */
enum class BurnKind {
    /** Total and active known: lepo = total − active. */
    SPLIT,

    /** Only the total ("kokonais"). */
    TOTAL,

    /** Only the active kcal. */
    ACTIVE,
}

/**
 * One day's burned energy: [total] = the bar height (the total, or the active kcal of an active-only
 * day), [active] = the active kcal (null on a kokonais day). Never computed from a formula.
 */
data class BurnDay(val day: LocalDate, val total: Long, val active: Long?, val kind: BurnKind) {
    /** Lepo = total − active, only on a split day. */
    val rest: Long? get() = if (kind == BurnKind.SPLIT) total - (active ?: 0) else null

    companion object {
        /**
         * A day from Health Connect's or the import's values (null: unknown); null when nothing is known.
         * Total < active (design 4): total = active, lepo 0 ([invalid] tells it).
         */
        fun of(day: LocalDate, total: Long?, active: Long?): BurnDay? = when {
            total != null && total > 0 && active != null -> BurnDay(day, maxOf(total, active), active.coerceAtLeast(0), BurnKind.SPLIT)
            total != null && total > 0 -> BurnDay(day, total, null, BurnKind.TOTAL)
            active != null && active > 0 -> BurnDay(day, active, active, BurnKind.ACTIVE)
            else -> null
        }

        fun invalid(total: Long?, active: Long?): Boolean = total != null && total > 0 && active != null && active > total
    }
}

/** A Kulutus bar: a day, or a week from Monday ([days] = 7) with the averages of design 5. */
data class BurnBar(val from: LocalDate, val days: Int, val total: Long, val active: Long?, val rest: Long?, val kind: BurnKind)

/** "aktiivinen keskim." averages days with an active value; [dataDays] counts today when it has data. */
data class BurnStats(val averageTotal: Long?, val averageActive: Long?, val dataDays: Int, val periodDays: Int)

/** The Health Connect days merged with the import (design 7); [imported] = days taken from the import. */
data class BurnMerged(val days: List<BurnDay>, val imported: Int)

/**
 * Imported Garmin kcal under the steps import's source rule (energy-burn "Imported Garmin days for
 * burned energy", design 7): per day the larger total wins with its own split; today never comes from
 * the import. Pure.
 */
object BurnMerge {
    fun importedDays(imported: List<GarminDay>, today: LocalDate): List<BurnDay> =
        imported.filter { it.day < today && it.hasKcal }.mapNotNull { BurnDay.of(it.day, it.total, it.active) }

    fun merge(hc: List<BurnDay>, imported: List<GarminDay>, source: StepsSourceState, today: LocalDate): BurnMerged {
        if (imported.isEmpty() || !GarminMerge.uses(source)) return BurnMerged(hc.sortedBy { it.day }, 0)
        val days = HashMap<LocalDate, BurnDay>()
        for (d in hc) days[d.day] = d
        val taken = HashSet<LocalDate>()
        for (d in importedDays(imported, today)) {
            val old = days[d.day]
            if (old == null || d.total > old.total) {
                days[d.day] = d
                taken += d.day
            }
        }
        return BurnMerged(days.values.sortedBy { it.day }, taken.size)
    }
}

/**
 * The Kulutus tab's pure data (energy-burn "Kulutus tab", "Kulutus days"): bars for days with data
 * only (weekly over 90 days), statistics (their average is the chart's flat line) and the day list.
 * Today is listed and drawn but left out of the averages, the line and the week averages (unless it is
 * its week's only day); it counts as a day with data.
 */
class BurnSeries(
    val from: LocalDate,
    val to: LocalDate,
    val weekly: Boolean,
    val bars: List<BurnBar>,
    val stats: BurnStats,
    /** Newest first. */
    val days: List<BurnDay>,
) {
    companion object {
        private const val WEEKLY_FROM_DAYS = 90

        fun burnDays(input: Collection<BurnDay>): List<BurnDay> = input.filter { it.total > 0 }.distinctBy { it.day }.sortedBy { it.day }

        /** Own charts' entry point: each day of [from]..[to] with its own values, never weekly. */
        fun daily(input: Collection<BurnDay>, from: LocalDate, to: LocalDate): List<BurnDay> = burnDays(input).filter { it.day in from..to }

        fun dates(period: Period, today: LocalDate, firstDay: LocalDate?): Pair<LocalDate, LocalDate> =
            fi.jukkakot.terveyslappu.chart.AxisTicks.periodDates(period, today, firstDay)

        fun of(input: Collection<BurnDay>, period: Period, today: LocalDate): BurnSeries {
            val all = burnDays(input)
            val (from, to) = dates(period, today, all.firstOrNull()?.day)
            val days = all.filter { it.day in from..to }
            val periodDays = (ChronoUnit.DAYS.between(from, to) + 1).toInt()
            val weekly = periodDays > WEEKLY_FROM_DAYS
            val bars = if (weekly) weeks(days, today) else days.map { BurnBar(it.day, 1, it.total, it.active, it.rest, it.kind) }
            val counted = days.filter { it.day != today }
            val stats = BurnStats(
                averageTotal = counted.map { it.total }.averageOrNull(),
                averageActive = counted.mapNotNull { it.active }.averageOrNull(),
                dataDays = days.size,
                periodDays = periodDays,
            )
            return BurnSeries(from, to, weekly, bars, stats, days.reversed())
        }

        /** Design 5: lepo, active and height each averaged over the week's days that have that value. */
        private fun weeks(days: List<BurnDay>, today: LocalDate): List<BurnBar> = days
            .groupBy { it.day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
            .map { (monday, all) ->
                val list = all.filter { it.day != today }.ifEmpty { all }
                val total = list.map { it.total }.averageOrNull()!!
                val active = list.mapNotNull { it.active }.averageOrNull()?.coerceAtMost(total)
                val rest = list.mapNotNull { it.rest }.averageOrNull()
                val kind = when {
                    active == null -> BurnKind.TOTAL
                    rest == null -> BurnKind.ACTIVE
                    else -> BurnKind.SPLIT
                }
                BurnBar(monday, 7, total, active, rest, kind)
            }
            .sortedBy { it.from }

        private fun List<Long>.averageOrNull(): Long? = if (isEmpty()) null else average().roundToLong()
    }
}

/**
 * What the front page's Kulutus card shows (energy-burn "Kulutus card"): today's day, the daily bars
 * of the card's own period and the average total per day with data (today left out).
 */
data class HomeBurn(
    /** Today's burned energy; null when there is none yet. */
    val today: BurnDay?,
    val from: LocalDate,
    val to: LocalDate,
    val bars: List<BurnBar>,
    val averageTotal: Long?,
) {
    companion object {
        fun of(input: Collection<BurnDay>, days: Long, today: LocalDate): HomeBurn {
            val series = BurnSeries.of(input, Period.Last(days), today)
            return HomeBurn(series.days.firstOrNull { it.day == today }, series.from, series.to, series.bars, series.stats.averageTotal)
        }
    }
}
