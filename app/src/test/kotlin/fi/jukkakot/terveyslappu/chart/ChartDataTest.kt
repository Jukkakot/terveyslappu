package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.weight.weightEntry
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ChartDataTest {
    private val zone = ZoneId.of("Europe/Helsinki")
    private val now = Instant.parse("2026-11-09T06:00:00Z")
    private fun at(daysAgo: Long, kg: Double) = weightEntry("$daysAgo", now.minusSeconds(daysAgo * 86_400), null, kg, "me", "me")

    @Test
    fun `a week starts seven days back and spans the whole week`() {
        val entries = (0L..6L).map { at(it, 82.0 + it / 10.0) }
        val data = ChartData.of(Period.WEEK, entries, now, zone)!!
        assertEquals(LocalDate.of(2026, 11, 2), data.origin)
        assertEquals(7, data.xs.size)
        assertEquals(data.xs.sorted(), data.xs)
        assertEquals(8.0, data.maxX)
        assertEquals(81.0, data.minY)
        assertEquals(84.0, data.maxY)
        assertEquals(2, data.labelSpacing)
        assertEquals(LocalDate.of(2026, 11, 9), data.dayAt(7.0))
    }

    @Test
    fun `all starts from the first weight and the nearest dot is found`() {
        val data = ChartData.of(Period.All, listOf(at(400, 90.0), at(0, 82.0)), now, zone)!!
        assertEquals(0.0, data.xs.first(), 0.5)
        assertEquals(82.0, data.entryAt(399.0)!!.kg)
        assertEquals(90.0, data.entryAt(3.0)!!.kg)
        assertEquals(81, data.labelSpacing)
    }

    @Test
    fun `line breaks at gaps over 30 days`() {
        assertEquals(listOf(0..2), ChartData.segments(listOf(0.0, 10.0, 40.0)))
        assertEquals(listOf(0..1, 2..2), ChartData.segments(listOf(0.0, 10.0, 40.5)))
        assertEquals(listOf(0..0, 1..1, 2..3), ChartData.segments(listOf(0.0, 100.0, 200.0, 201.0)))
        assertEquals(emptyList(), ChartData.segments(emptyList()))
    }

    @Test
    fun `labels carry the year only over a year`() {
        val all = ChartData.of(Period.All, listOf(at(400, 90.0), at(0, 82.0)), now, zone)!!
        assertEquals(true, all.overYear)
        assertEquals(listOf(0..0, 1..1), all.segments)
        val year = ChartData.of(Period.All, listOf(at(300, 90.0), at(0, 82.0)), now, zone)!!
        assertEquals(false, year.overYear)
        assertEquals(false, ChartData.of(Period.WEEK, listOf(at(1, 82.0)), now, zone)!!.overYear)
    }

    @Test
    fun `no weights draw nothing`() {
        assertNull(ChartData.of(Period.MONTH, emptyList(), now, zone))
    }
}
