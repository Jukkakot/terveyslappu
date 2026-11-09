package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.weight.WeightEntry
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.IsoFields
import java.time.temporal.TemporalAdjusters

/** Levels of Historia's grouped list (weight-entry "Grouped list"), widest first. */
enum class GroupLevel { YEAR, MONTH, WEEK, DAY }

/** A row of the grouped list: a group, a single weight or a pause without weights. */
sealed interface ListNode {
    val key: String

    /**
     * A time span holding more than one child. [from]..[to] are its days (a week cut at the month
     * border); [first] and [last] are its oldest and newest weight; [count] counts all its weights.
     */
    data class Group(
        val level: GroupLevel,
        override val key: String,
        val from: LocalDate,
        val to: LocalDate,
        /** ISO week number (WEEK only). */
        val week: Int,
        val first: WeightEntry,
        val last: WeightEntry,
        val count: Int,
        /** Newest first. */
        val children: List<ListNode>,
    ) : ListNode {
        val change: Double get() = last.kg - first.kg
    }

    /** One weight with its change from the weight before it (null: oldest, or after a long pause). */
    data class Weight(val entry: WeightEntry, val change: Double?) : ListNode {
        override val key: String get() = entry.id
    }

    /** No weights from [from] to [to] (both inclusive), over [MAX_GAP_DAYS] long. */
    data class Gap(val from: LocalDate, val to: LocalDate) : ListNode {
        override val key: String get() = "gap-$from"
    }
}

/** A row to draw: the node and how deep it sits. */
data class ListRow(val node: ListNode, val depth: Int)

/**
 * Builds Historia's list tree: year → month → week (cut at months) → day → weight, newest first on
 * every level. A group with a single child is replaced by that child, bottom up, and the top level
 * too, so short periods stay flat. Gap rows go between siblings more than [MAX_GAP_DAYS] apart.
 */
object ListGroups {
    const val MAX_GAP_DAYS = ChartData.MAX_GAP_DAYS

    /**
     * Up to this many weights stay a flat list (only days with several weights group): a rolling
     * week would otherwise split at its Monday, and a short list needs no overview.
     */
    const val FLAT_MAX = 14

    fun build(entries: List<WeightEntry>, zone: ZoneId, flatMax: Int = FLAT_MAX): List<ListNode> {
        val newest = entries.sortedByDescending { it.time }
        val changes = changesFromPrevious(newest)
        val top = if (newest.size <= flatMax) GroupLevel.DAY else GroupLevel.YEAR
        var nodes = level(newest, top, zone, changes)
        while (nodes.size == 1 && nodes[0] is ListNode.Group) nodes = (nodes[0] as ListNode.Group).children
        return nodes
    }

    /** Keys of the groups open by default: the newest group on each level, down the newest path. */
    fun defaultOpen(nodes: List<ListNode>): Set<String> {
        val open = mutableSetOf<String>()
        var level = nodes
        while (true) {
            val group = level.firstOrNull { it is ListNode.Group } as? ListNode.Group ?: break
            open += group.key
            level = group.children
        }
        return open
    }

    /** The rows to draw: every node whose parents are all in [open]. */
    fun flatten(nodes: List<ListNode>, open: Set<String>, depth: Int = 0): List<ListRow> = buildList {
        nodes.forEach { node ->
            add(ListRow(node, depth))
            if (node is ListNode.Group && node.key in open) addAll(flatten(node.children, open, depth + 1))
        }
    }

    private fun level(newest: List<WeightEntry>, level: GroupLevel?, zone: ZoneId, changes: Map<String, Double>): List<ListNode> {
        if (level == null) return newest.map { ListNode.Weight(it, changes[it.id]) }
        val groups = newest.groupBy { key(level, it.time.atZone(zone).toLocalDate()) }
        val nodes = groups.values.map { members ->
            val day = members.first().time.atZone(zone).toLocalDate()
            val children = level(members, GroupLevel.entries.getOrNull(level.ordinal + 1), zone, changes)
            if (children.size == 1) {
                children[0]
            } else {
                val (from, to) = span(level, day)
                ListNode.Group(
                    level = level,
                    key = key(level, day),
                    from = from,
                    to = to,
                    week = weekMonday(day).get(IsoFields.WEEK_OF_WEEK_BASED_YEAR),
                    first = members.last(),
                    last = members.first(),
                    count = members.size,
                    children = children,
                )
            }
        }
        return withGaps(nodes, zone)
    }

    /** Puts a gap row between neighbours (newest first) whose weights are over [MAX_GAP_DAYS] apart. */
    private fun withGaps(nodes: List<ListNode>, zone: ZoneId): List<ListNode> = buildList {
        nodes.forEachIndexed { i, node ->
            if (i > 0) {
                val newer = oldest(nodes[i - 1])
                val older = newest(node)
                if (Duration.between(older.time, newer.time).seconds / 86_400.0 > MAX_GAP_DAYS) {
                    add(ListNode.Gap(older.time.atZone(zone).toLocalDate().plusDays(1), newer.time.atZone(zone).toLocalDate().minusDays(1)))
                }
            }
            add(node)
        }
    }

    private fun oldest(node: ListNode): WeightEntry = when (node) {
        is ListNode.Group -> node.first
        is ListNode.Weight -> node.entry
        is ListNode.Gap -> error("gap has no weight")
    }

    private fun newest(node: ListNode): WeightEntry = when (node) {
        is ListNode.Group -> node.last
        is ListNode.Weight -> node.entry
        is ListNode.Gap -> error("gap has no weight")
    }

    private fun weekMonday(day: LocalDate): LocalDate = day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    private fun key(level: GroupLevel, day: LocalDate): String = when (level) {
        GroupLevel.YEAR -> "y${day.year}"
        GroupLevel.MONTH -> "m${day.year}-%02d".format(day.monthValue)
        // Month + week, so a week split by a month border is two groups.
        GroupLevel.WEEK -> "w${day.year}-%02d-%02d".format(day.monthValue, weekMonday(day).get(IsoFields.WEEK_OF_WEEK_BASED_YEAR))
        GroupLevel.DAY -> "d$day"
    }

    private fun span(level: GroupLevel, day: LocalDate): Pair<LocalDate, LocalDate> = when (level) {
        GroupLevel.YEAR -> day.withDayOfYear(1) to day.with(TemporalAdjusters.lastDayOfYear())
        GroupLevel.MONTH -> day.withDayOfMonth(1) to day.with(TemporalAdjusters.lastDayOfMonth())
        GroupLevel.WEEK -> {
            val monday = weekMonday(day)
            maxOf(monday, day.withDayOfMonth(1)) to minOf(monday.plusDays(6), day.with(TemporalAdjusters.lastDayOfMonth()))
        }
        GroupLevel.DAY -> day to day
    }
}
