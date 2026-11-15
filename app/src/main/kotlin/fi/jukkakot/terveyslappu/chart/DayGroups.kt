package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.burn.BurnDay
import fi.jukkakot.terveyslappu.food.EnergyDay
import fi.jukkakot.terveyslappu.steps.StepsDay
import java.time.LocalDate
import java.time.temporal.IsoFields
import kotlin.math.roundToLong

/** A row of a grouped day list (weight-chart "Grouped day lists"): a year/month/week group or one day. */
sealed interface DayNode<out T> {
    val key: String

    /** [members] = every listed day under the group, newest first; [from]..[to] its days (a week cut at the month border). */
    data class Group<T>(
        val level: GroupLevel,
        override val key: String,
        val from: LocalDate,
        val to: LocalDate,
        /** ISO week number (WEEK only). */
        val week: Int,
        val members: List<T>,
        val children: List<DayNode<T>>,
    ) : DayNode<T>

    data class Row<T>(val item: T, val day: LocalDate) : DayNode<T> {
        override val key: String get() = "d$day"
    }
}

data class DayListRow<T>(val node: DayNode<T>, val depth: Int)

/**
 * Year → month → week (cut at months) → day tree for Energia, Askeleet and Kulutus, by the weight list's
 * rule ([ListGroups]): newest first, every group its own row, year level left out when all days are in one
 * year, flat up to [ListGroups.FLAT_MAX] days, no gap rows.
 */
object DayGroups {
    fun <T> build(items: List<T>, flatMax: Int = ListGroups.FLAT_MAX, dayOf: (T) -> LocalDate): List<DayNode<T>> {
        val newest = items.sortedByDescending(dayOf)
        if (newest.size <= flatMax) return newest.map { DayNode.Row(it, dayOf(it)) }
        val oneYear = newest.map { dayOf(it).year }.distinct().size == 1
        return level(newest, dayOf, if (oneYear) GroupLevel.MONTH else GroupLevel.YEAR)
    }

    private fun <T> level(newest: List<T>, dayOf: (T) -> LocalDate, level: GroupLevel?): List<DayNode<T>> {
        if (level == null) return newest.map { DayNode.Row(it, dayOf(it)) }
        return newest.groupBy { ListGroups.key(level, dayOf(it)) }.values.map { members ->
            val day = dayOf(members.first())
            val (from, to) = ListGroups.span(level, day)
            DayNode.Group(
                level = level,
                key = ListGroups.key(level, day),
                from = from,
                to = to,
                week = ListGroups.weekMonday(day).get(IsoFields.WEEK_OF_WEEK_BASED_YEAR),
                members = members,
                children = level(members, dayOf, GroupLevel.entries.getOrNull(level.ordinal + 1)),
            )
        }
    }

    /** The rows to draw: every node whose parents are all in [open]. */
    fun <T> flatten(nodes: List<DayNode<T>>, open: Set<String>, depth: Int = 0): List<DayListRow<T>> = buildList {
        nodes.forEach { node ->
            add(DayListRow(node, depth))
            if (node is DayNode.Group && node.key in open) addAll(flatten(node.children, open, depth + 1))
        }
    }
}

/**
 * A group row's numbers: [days] listed days, [main] the per-day average of the tab's main value, [extra] the
 * secondary average (null: left out). Today counts in [days] but not in the averages, unless it is the group's
 * only day.
 */
data class GroupSummary(val days: Int, val main: Long, val extra: Long?)

object GroupSummaries {
    private fun <T> counted(members: List<T>, dayOf: (T) -> LocalDate, today: LocalDate): List<T> =
        members.filter { dayOf(it) != today }.ifEmpty { members }

    private fun List<Long>.avg(): Long? = if (isEmpty()) null else average().roundToLong()

    /** Energia: kcal, treats over all counted days; a part that rounds to 0 is left out. */
    fun energy(members: List<EnergyDay>, today: LocalDate): GroupSummary {
        val c = counted(members, { it.day }, today)
        return GroupSummary(members.size, c.map { it.total.toLong() }.avg() ?: 0, c.map { it.treat.toLong() }.avg()?.takeIf { it > 0 })
    }

    fun steps(members: List<StepsDay>, today: LocalDate): GroupSummary {
        val c = counted(members, { it.day }, today)
        return GroupSummary(members.size, c.map { it.steps }.avg() ?: 0, null)
    }

    /** Kulutus: total over counted days, active over those with an active value. */
    fun burn(members: List<BurnDay>, today: LocalDate): GroupSummary {
        val c = counted(members, { it.day }, today)
        return GroupSummary(members.size, c.map { it.total }.avg() ?: 0, c.mapNotNull { it.active }.avg()?.takeIf { it > 0 })
    }
}
