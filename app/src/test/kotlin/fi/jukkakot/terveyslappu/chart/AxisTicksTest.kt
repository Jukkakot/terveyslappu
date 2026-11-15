package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.weight.weightEntry
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AxisTicksTest {
    private val today = LocalDate.of(2026, 11, 15)

    @Test
    fun `bar tops are round and the middle is half`() {
        assertEquals(listOf(0.0, 6_000.0, 12_000.0), AxisTicks.bars(9_400.0, 1_000.0))
        assertEquals(listOf(0.0, 1_250.0, 2_500.0), AxisTicks.bars(2_200.0, 250.0))
        assertEquals(listOf(0.0, 1_000.0, 2_000.0), AxisTicks.bars(0.0, 1_000.0))
    }

    @Test
    fun `a line axis is the scale's ends and midpoint`() {
        assertEquals(listOf(95.0, 97.5, 100.0), AxisTicks.line(95.0, 100.0))
    }

    @Test
    fun `a week fits four dates two days apart`() {
        val from = LocalDate.of(2026, 11, 9)
        assertEquals((9..15 step 2).map { LocalDate.of(2026, 11, it) }, AxisTicks.dates(from, today, 4))
    }

    @Test
    fun `dates run from start to end, evenly, for every span`() {
        for (span in listOf(2L, 7L, 29L, 89L, 363L, 364L, 3 * 365L)) {
            val from = today.minusDays(span)
            for (count in 3..5) {
                val dates = AxisTicks.dates(from, today, count)
                assertEquals(from, dates.first())
                assertEquals(today, dates.last())
                assertTrue(dates.size in 3..5)
                val gaps = dates.zipWithNext().map { (a, b) -> AxisTicks.days(a, b) }
                assertTrue(gaps.max() - gaps.min() <= 1, "span $span count $count gaps $gaps")
            }
        }
        assertEquals(listOf(today), AxisTicks.dates(today, today, 5))
        assertEquals(listOf(today.minusDays(1), today), AxisTicks.dates(today.minusDays(1), today, 5))
        assertEquals(3, AxisTicks.dates(today.minusDays(2), today, 5).size)
    }

    @Test
    fun `a month shows five dates about a week apart`() {
        val dates = AxisTicks.dates(LocalDate.of(2026, 10, 17), today, 5)
        assertEquals(LocalDate.of(2026, 10, 17), dates.first())
        assertEquals(5, dates.size)
        assertTrue(dates.zipWithNext().all { (a, b) -> AxisTicks.days(a, b) in 7..8 })
    }

    @Test
    fun `fitCount takes the most labels that fit, never fewer than three`() {
        val from = today.minusDays(29)
        assertEquals(5, AxisTicks.fitCount(from, today, 300f, 8f) { 30f })
        assertEquals(4, AxisTicks.fitCount(from, today, 300f, 8f) { 60f })
        assertEquals(3, AxisTicks.fitCount(from, today, 300f, 8f) { 200f })
    }

    @Test
    fun `fitted labels never overlap`() {
        val from = today.minusDays(6)
        val width = 280f
        val n = AxisTicks.fitCount(from, today, width, 6f) { 40f }
        val bounds = AxisTicks.labelBounds(from, today, AxisTicks.dates(from, today, n), width) { 40f }
        bounds.zipWithNext().forEach { (a, b) -> assertTrue(b.start - a.endInclusive >= 6f) }
        assertEquals(0f, bounds.first().start)
        assertEquals(width, bounds.last().endInclusive)
    }

    @Test
    fun `month and year from 365 days`() {
        assertFalse(AxisTicks.monthYear(today.minusDays(363), today))
        assertTrue(AxisTicks.monthYear(today.minusDays(364), today))
        val (from, to) = AxisTicks.periodDates(Period.YEAR, today, null)
        assertTrue(AxisTicks.monthYear(from, to))
    }

    @Test
    fun `weekly and daily bars use the same dates`() {
        // Positions are plain days, also for weekly bars: the dates do not depend on the bar width.
        val (from, to) = AxisTicks.periodDates(Period.Last(120), today, null)
        assertEquals(AxisTicks.dates(from, to, 5), AxisTicks.dates(from, to, 5))
        assertEquals(0.5f / 120, AxisTicks.position(from, to, from), 1e-6f)
    }

    @Test
    fun `Paino's labels fall on the bar charts' dates`() {
        val zone = ZoneId.of("Europe/Helsinki")
        val now = today.atTime(9, 0).atZone(zone).toInstant()
        val entries = listOf(weightEntry("a", now.minusSeconds(400L * 86_400), null, 90.0, "me", "me"), weightEntry("b", now.minusSeconds(3_600), null, 82.0, "me", "me"))
        for (period in listOf(Period.WEEK, Period.MONTH, Period.QUARTER, Period.YEAR, Period.All)) {
            val data = ChartData.of(period, period.filter(entries, now, zone), now, zone) ?: continue
            val first = if (period == Period.All) data.origin else null
            val (from, to) = AxisTicks.periodDates(period, today, first)
            for (count in 3..5) {
                val expected = AxisTicks.labelDays(from, to, count)
                assertEquals(expected, data.labelDays(count), "$period")
                data.labelXs(count).zip(expected).forEach { (x, day) ->
                    assertTrue(abs(x - (AxisTicks.days(data.origin, day) + 0.5)) < 1e-9)
                    assertEquals(day, data.dayAt(x - 0.5))
                }
            }
        }
    }

    private fun weekdays(from: LocalDate, to: LocalDate) = AxisTicks.labelDays(from, to, 5).map { AxisTicks.weekday(it) }

    @Test
    fun `a week is labelled with weekdays`() {
        val sunday = LocalDate.of(2026, 11, 15)
        assertEquals(listOf("Ma", "Ti", "Ke", "To", "Pe", "La", "Su"), weekdays(sunday.minusDays(6), sunday))
        val wednesday = LocalDate.of(2026, 11, 18)
        assertEquals(listOf("To", "Pe", "La", "Su", "Ma", "Ti", "Ke"), weekdays(wednesday.minusDays(6), wednesday))
        assertEquals(listOf("Ma", "Ti", "Ke"), weekdays(LocalDate.of(2026, 11, 16), wednesday))
    }

    @Test
    fun `eight days still gives dates`() {
        val to = LocalDate.of(2026, 11, 15)
        assertFalse(AxisTicks.isWeek(to.minusDays(7), to))
        assertEquals(AxisTicks.dates(to.minusDays(7), to, 5), AxisTicks.labelDays(to.minusDays(7), to, 5))
    }

    @Test
    fun `weekday labels are centred on their days`() {
        val to = LocalDate.of(2026, 11, 15)
        val from = to.minusDays(6)
        val b = AxisTicks.labelBounds(from, to, AxisTicks.labelDays(from, to, 5), 700f, centred = true) { 20f }
        assertEquals(50f - 10f, b.first().start, 1e-3f)
        assertEquals(650f + 10f, b.last().endInclusive, 1e-3f)
    }
}
