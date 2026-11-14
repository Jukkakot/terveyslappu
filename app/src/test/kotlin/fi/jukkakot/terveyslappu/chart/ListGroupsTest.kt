package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.weight.WeightEntry
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
    private fun tree(entries: List<WeightEntry>) = ListGroups.build(entries, zone, flatMax = 0)

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
    fun `two weights in a day sit under the week with no day row`() {
        val entries = listOf(w("2026-11-03", 82.0), w("2026-11-04", 82.4, "07:05"), w("2026-11-04", 82.9, "21:30"))
        val month = groups(tree(entries)).single()
        val week = groups(month.children).single()
        assertEquals(GroupLevel.WEEK, week.level)
        assertEquals(listOf("2026-11-04 21:30", "2026-11-04 07:05", "2026-11-03 07:05"), week.children.map { it.key })
    }

    @Test
    fun `several years show year rows newest first`() {
        val entries = (2022..2026).flatMap { y -> listOf(w("$y-03-01", 90.0), w("$y-03-15", 89.0)) }
        assertEquals(listOf("y2026", "y2025", "y2024", "y2023", "y2022"), groups(tree(entries)).map { it.key })
    }

    @Test
    fun `one year starts with the months`() {
        val entries = listOf(w("2026-01-10", 90.0), w("2026-02-10", 89.0), w("2026-03-01", 88.5))
        assertEquals(listOf("m2026-03", "m2026-02", "m2026-01"), groups(tree(entries)).map { it.key })
    }

    @Test
    fun `a month with one weight is still a month with a week`() {
        val entries = listOf(w("2024-03-12", 90.0), w("2024-04-02", 89.5), w("2023-12-20", 91.0))
        val y2024 = groups(tree(entries)).first()
        val march = groups(y2024.children).single { it.key == "m2024-03" }
        assertEquals(1, march.count)
        val week = groups(march.children).single()
        assertEquals(GroupLevel.WEEK, week.level)
        assertEquals(listOf("2024-03-12 07:05"), week.children.map { it.key })
    }

    @Test
    fun `a week across a month border splits into the two months`() {
        val entries = listOf(w("2025-10-30", 90.0), w("2025-11-03", 90.2), w("2025-11-04", 90.3))
        val months = groups(tree(entries))
        assertEquals(listOf("m2025-11", "m2025-10"), months.map { it.key })
        val oct = groups(months[1].children).single()
        val nov = groups(months[0].children).single()
        assertEquals(LocalDate.of(2025, 10, 27) to LocalDate.of(2025, 10, 31), oct.from to oct.to)
        assertEquals(LocalDate.of(2025, 11, 3) to LocalDate.of(2025, 11, 9), nov.from to nov.to)
    }

    @Test
    fun `month row values`() {
        val entries = (1..20).map { w("2025-11-%02d".format(it), 92.3 - (it - 1) * 1.3 / 19) } + w("2025-10-15", 94.0)
        val nov = groups(tree(entries)).single { it.key == "m2025-11" }
        assertEquals(20, nov.count)
        assertEquals(92.3, nov.first!!.kg, 1e-9)
        assertEquals(91.0, nov.last!!.kg, 1e-9)
        assertEquals(-1.3, nov.change, 1e-9)
    }

    @Test
    fun `a long pause shows a gap row between the years`() {
        val nodes = tree(listOf(w("2024-05-20", 92.0), w("2025-03-18", 97.5)))
        assertEquals(listOf("y2025", "gap-2024-05-21", "y2024"), nodes.map { it.key })
        assertEquals(ListNode.Gap(LocalDate.of(2024, 5, 21), LocalDate.of(2025, 3, 17)), nodes[1])
    }

    @Test
    fun `a short pause shows no gap`() {
        val entries = listOf(w("2025-11-01", 90.0), w("2025-11-12", 90.1), w("2025-11-13", 90.2))
        assertTrue(tree(entries).none { it is ListNode.Gap })
    }

    @Test
    fun `no change across a gap`() {
        val changes = changesFromPrevious(listOf(w("2024-05-20", 92.0), w("2025-03-18", 97.5)))
        assertNull(changes["2025-03-18 07:05"])
        val nodes = ListGroups.build(listOf(w("2024-05-20", 92.0), w("2025-03-18", 97.5)), zone)
        assertNull((nodes.first() as ListNode.Weight).change)
    }

    @Test
    fun `closed groups show only the top level and an opened one its children`() {
        val entries = (2024..2026).flatMap { y -> listOf(w("$y-03-02", 90.0), w("$y-04-07", 89.2)) }
        val nodes = tree(entries)
        assertTrue(ListGroups.flatten(nodes, emptySet()).none { it.node is ListNode.Weight })
        val opened = ListGroups.flatten(nodes, setOf("y2026", "m2026-04", "w2026-04-15"))
        assertTrue(opened.any { it.node.key == "2026-04-07 07:05" && it.depth == 3 })
    }
}
