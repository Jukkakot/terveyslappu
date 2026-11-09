package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.weight.weightEntry
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Made-up weights only (NFR-1). */
class ListGroupsTest {
    private val zone = ZoneId.of("Europe/Helsinki")
    private fun w(day: String, kg: Double, time: String = "07:05") =
        weightEntry("$day $time", LocalDate.parse(day).atTime(LocalTime.parse(time)).atZone(zone).toInstant(), null, kg, "me", "me")

    /** Tree without the short-list rule, for the level tests. */
    private fun tree(entries: List<fi.jukkakot.terveyslappu.weight.WeightEntry>) = ListGroups.build(entries, zone, flatMax = 0)

    private fun groups(nodes: List<ListNode>) = nodes.filterIsInstance<ListNode.Group>()

    @Test
    fun `a week of one weight a day is flat`() {
        val week = (3..9).map { w("2026-11-%02d".format(it), 82.0 + it / 10.0) }
        val nodes = ListGroups.build(week, zone)
        assertEquals(7, nodes.size)
        assertTrue(nodes.all { it is ListNode.Weight })
        assertEquals("2026-11-09 07:05", nodes.first().key)
    }

    @Test
    fun `two weights in a day make a day group`() {
        val entries = listOf(w("2026-11-03", 82.0), w("2026-11-04", 82.4, "07:05"), w("2026-11-04", 82.9, "21:30"), w("2026-11-05", 82.1))
        val nodes = ListGroups.build(entries, zone)
        val day = groups(nodes).single()
        assertEquals(GroupLevel.DAY, day.level)
        assertEquals("d2026-11-04", day.key)
        assertEquals(2, day.count)
        assertEquals(listOf("2026-11-04 21:30", "2026-11-04 07:05"), day.children.map { it.key })
    }

    @Test
    fun `several years show year rows newest first`() {
        val entries = (2022..2026).flatMap { y -> listOf(w("$y-03-01", 90.0), w("$y-04-15", 89.0)) }
        val nodes = tree(entries)
        assertEquals(listOf("y2026", "y2025", "y2024", "y2023", "y2022"), groups(nodes).map { it.key })
    }

    @Test
    fun `a week across a month border splits into the two months`() {
        val entries = listOf(w("2025-10-29", 90.0), w("2025-10-30", 90.1), w("2025-11-03", 90.2), w("2025-11-04", 90.3))
        val months = groups(tree(entries))
        // Each month holds one week, so the weeks stand in for the months.
        assertEquals(listOf("w2025-11-45", "w2025-10-44"), months.map { it.key })
        // Months are cut apart; each part holds its own weights.
        assertEquals(2, months[0].count)
        assertEquals(2, months[1].count)
    }

    @Test
    fun `a week cut at a month border keeps its days in the month`() {
        val entries = listOf(w("2025-10-30", 90.0), w("2025-10-31", 90.1), w("2025-11-01", 90.2), w("2025-11-02", 90.3), w("2025-10-20", 90.5), w("2025-11-05", 90.4))
        val months = groups(tree(entries))
        val octWeek = groups(months.single { it.key == "m2025-10" }.children).single()
        assertEquals(LocalDate.of(2025, 10, 27) to LocalDate.of(2025, 10, 31), octWeek.from to octWeek.to)
        val novWeek = groups(months.single { it.key == "m2025-11" }.children).single()
        assertEquals(LocalDate.of(2025, 11, 1) to LocalDate.of(2025, 11, 2), novWeek.from to novWeek.to)
        assertEquals(octWeek.week, novWeek.week)
    }

    @Test
    fun `month row values`() {
        val entries = (1..20).map { w("2025-11-%02d".format(it), 92.3 - (it - 1) * 1.3 / 19) } + w("2025-10-01", 95.0) + w("2025-10-15", 94.0)
        val nov = groups(tree(entries)).single { it.key == "m2025-11" }
        assertEquals(20, nov.count)
        assertEquals(92.3, nov.first.kg, 1e-9)
        assertEquals(91.0, nov.last.kg, 1e-9)
        assertEquals(-1.3, nov.change, 1e-9)
    }

    @Test
    fun `a long pause shows a gap row between the years`() {
        val entries = listOf(w("2024-04-25", 93.0), w("2024-05-10", 92.5), w("2024-05-20", 92.0), w("2025-03-18", 97.5), w("2025-04-01", 97.0))
        val nodes = tree(entries)
        assertEquals(listOf("y2025", "gap-2024-05-21", "y2024"), nodes.map { it.key })
        assertEquals(ListNode.Gap(LocalDate.of(2024, 5, 21), LocalDate.of(2025, 3, 17)), nodes[1])
    }

    @Test
    fun `a short pause shows no gap`() {
        val entries = listOf(w("2025-11-01", 90.0), w("2025-11-12", 90.1), w("2025-11-13", 90.2))
        val nodes = ListGroups.build(entries, zone)
        assertTrue(nodes.none { it is ListNode.Gap })
    }

    @Test
    fun `no change across a gap`() {
        val changes = changesFromPrevious(listOf(w("2024-05-20", 92.0), w("2025-03-18", 97.5)))
        assertNull(changes["2025-03-18 07:05"])
        val nodes = ListGroups.build(listOf(w("2024-05-20", 92.0), w("2025-03-18", 97.5)), zone)
        assertNull((nodes.first() as ListNode.Weight).change)
    }

    @Test
    fun `default open follows the newest path and flatten shows its rows`() {
        val entries = (2024..2026).flatMap { y -> listOf(w("$y-03-02", 90.0), w("$y-03-03", 89.5), w("$y-04-06", 89.0), w("$y-04-07", 89.2)) }
        val nodes = tree(entries)
        val open = ListGroups.defaultOpen(nodes)
        // April 2026 holds one week, so the week stands in for the month.
        assertEquals(setOf("y2026", "w2026-04-15"), open)
        assertTrue("y2025" !in open)
        val rows = ListGroups.flatten(nodes, open)
        assertTrue(rows.any { it.node.key == "2026-04-07 07:05" && it.depth == 2 })
        assertTrue(rows.none { it.node.key == "2025-04-07 07:05" })
    }
}
