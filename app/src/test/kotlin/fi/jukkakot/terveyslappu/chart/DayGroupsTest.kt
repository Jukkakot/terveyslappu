package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.burn.BurnDay
import fi.jukkakot.terveyslappu.food.EnergyDay
import fi.jukkakot.terveyslappu.steps.StepsDay
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Made-up numbers only (NFR-1). */
class DayGroupsTest {
    private fun steps(from: String, days: Int, value: Long = 8000) =
        (0 until days).map { StepsDay(LocalDate.parse(from).plusDays(it.toLong()), value) }

    private fun build(items: List<StepsDay>) = DayGroups.build(items) { it.day }

    @Test
    fun `up to 14 days stay flat, newest first`() {
        val nodes = build(steps("2026-11-02", 14))
        assertEquals(14, nodes.size)
        assertTrue(nodes.all { it is DayNode.Row })
        assertEquals(LocalDate.parse("2026-11-15"), (nodes.first() as DayNode.Row).day)
    }

    @Test
    fun `over 14 days in one year gives months on top, no year row`() {
        val nodes = build(steps("2026-10-20", 30))
        val groups = nodes.filterIsInstance<DayNode.Group<StepsDay>>()
        assertEquals(nodes.size, groups.size)
        assertEquals(listOf(GroupLevel.MONTH, GroupLevel.MONTH), groups.map { it.level })
        assertEquals(listOf("m2026-11", "m2026-10"), groups.map { it.key })
        assertEquals(18, groups.first().members.size)
    }

    @Test
    fun `days in two years get a year level`() {
        val nodes = build(steps("2025-12-10", 30))
        val groups = nodes.filterIsInstance<DayNode.Group<StepsDay>>()
        assertEquals(listOf("y2026", "y2025"), groups.map { it.key })
        assertEquals(GroupLevel.MONTH, (groups.first().children.first() as DayNode.Group<*>).level)
    }

    @Test
    fun `a week is cut at the month border`() {
        // 2026-10-26 is a Monday; the week 26.10.-1.11. splits into October and November.
        val nodes = build(steps("2026-10-26", 20))
        val november = nodes.filterIsInstance<DayNode.Group<StepsDay>>().first { it.key == "m2026-11" }
        val october = nodes.filterIsInstance<DayNode.Group<StepsDay>>().first { it.key == "m2026-10" }
        val novWeeks = november.children.filterIsInstance<DayNode.Group<StepsDay>>()
        val octWeeks = october.children.filterIsInstance<DayNode.Group<StepsDay>>()
        assertEquals(LocalDate.parse("2026-11-01"), novWeeks.last().from)
        assertEquals(LocalDate.parse("2026-11-01"), novWeeks.last().to)
        assertEquals(LocalDate.parse("2026-10-26"), octWeeks.single().from)
        assertEquals(LocalDate.parse("2026-10-31"), octWeeks.single().to)
    }

    @Test
    fun `flatten shows children only of open groups`() {
        val nodes = build(steps("2026-10-20", 30))
        assertEquals(2, DayGroups.flatten(nodes, emptySet()).size)
        val open = DayGroups.flatten(nodes, setOf("m2026-11"))
        assertTrue(open.size > 2)
        assertEquals(1, open[1].depth)
    }

    @Test
    fun `steps average leaves today out and counts it in the days`() {
        val today = LocalDate.parse("2026-11-11")
        val s = GroupSummaries.steps(listOf(StepsDay(today, 3000), StepsDay(today.minusDays(1), 10000), StepsDay(today.minusDays(2), 8000)), today)
        assertEquals(3, s.days)
        assertEquals(9000, s.main)
    }

    @Test
    fun `a group of only today shows today's value`() {
        val today = LocalDate.parse("2026-11-09")
        val s = GroupSummaries.energy(listOf(EnergyDay(today, 2000, 0)), today)
        assertEquals(1, s.days)
        assertEquals(2000, s.main)
        assertNull(s.extra)
    }

    @Test
    fun `energy treats average over counted days and a zero is left out`() {
        val today = LocalDate.parse("2026-11-15")
        val s = GroupSummaries.energy(listOf(EnergyDay(today.minusDays(1), 1800, 300), EnergyDay(today.minusDays(2), 1900, 160)), today)
        assertEquals(1850, s.main)
        assertEquals(230, s.extra)
        assertNull(GroupSummaries.energy(listOf(EnergyDay(today.minusDays(1), 1800, 0)), today).extra)
    }

    @Test
    fun `burn active averages only days with an active value`() {
        val today = LocalDate.parse("2026-11-15")
        val days = listOf(
            BurnDay.of(today.minusDays(1), 2300, 500)!!,
            BurnDay.of(today.minusDays(2), 2200, null)!!,
            BurnDay.of(today.minusDays(3), 2400, 400)!!,
        )
        val s = GroupSummaries.burn(days, today)
        assertEquals(2300, s.main)
        assertEquals(450, s.extra)
        assertNull(GroupSummaries.burn(listOf(days[1]), today).extra)
    }
}
