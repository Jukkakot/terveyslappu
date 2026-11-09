package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.weight.weightEntry
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PeriodTest {
    private val zone = ZoneId.of("Europe/Helsinki")
    private val now = Instant.parse("2026-11-09T06:00:00Z")
    private fun at(daysAgo: Long, kg: Double = 80.0) =
        weightEntry("$daysAgo", now.minusSeconds(daysAgo * 86_400), null, kg, "me", "me")

    @Test
    fun `last week keeps the last seven days`() {
        val entries = listOf(at(0), at(6), at(8))
        assertEquals(listOf("0", "6"), Period.WEEK.filter(entries, now, zone).map { it.id })
        assertEquals(3, Period.All.filter(entries, now, zone).size)
    }

    @Test
    fun `custom range includes both days`() {
        val period = Period.Custom(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 2))
        val (start, end) = period.range(now, zone)
        assertEquals(Instant.parse("2026-10-31T22:00:00Z"), start)
        assertEquals(Instant.parse("2026-11-02T22:00:00Z"), end)
    }

    @Test
    fun `encode and decode round trip, unknown falls back to a week`() {
        val periods = Period.PRESETS + Period.Custom(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 15))
        periods.forEach { assertEquals(it, Period.decode(it.encode())) }
        assertEquals(Period.WEEK, Period.decode(null))
        assertEquals(Period.WEEK, Period.decode("custom:2026-10-15:2026-09-01"))
        assertEquals(Period.WEEK, Period.decode("rubbish"))
    }

    @Test
    fun `history is needed beyond 30 days`() {
        assertFalse(Period.MONTH.needsHistory(now, zone))
        assertTrue(Period.QUARTER.needsHistory(now, zone))
        assertTrue(Period.All.needsHistory(now, zone))
        assertFalse(Period.Custom(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 5)).needsHistory(now, zone))
    }

    @Test
    fun `losing a kilo in four weeks is a quarter kilo a week`() {
        val stats = periodStats(listOf(at(0, 82.0), at(14, 82.6), at(28, 83.0)))!!
        assertEquals(82.0, stats.latest)
        assertEquals(-1.0, stats.change!!, 1e-9)
        assertEquals(-0.25, stats.perWeek!!, 1e-9)
        assertEquals(82.0, stats.min)
        assertEquals(83.0, stats.max)
        assertEquals(82.533, stats.average, 1e-3)
        assertEquals(3, stats.count)
    }

    @Test
    fun `a single weight has no change or rate`() {
        val stats = periodStats(listOf(at(1, 81.5)))!!
        assertEquals(81.5, stats.latest)
        assertEquals(81.5, stats.average)
        assertNull(stats.change)
        assertNull(stats.perWeek)
        assertNull(periodStats(emptyList()))
    }

    @Test
    fun `weights within a day have a change but no rate`() {
        val a = at(0, 82.0)
        val b = a.copy(id = "b", time = a.time.minusSeconds(3_600), kg = 82.4)
        val stats = periodStats(listOf(a, b))!!
        assertEquals(-0.4, stats.change!!, 1e-9)
        assertNull(stats.perWeek)
    }
}
