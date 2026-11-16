package fi.jukkakot.terveyslappu.chart

import java.time.LocalDate
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
    fun `month and year from 365 days`() {
        assertFalse(AxisTicks.monthYear(today.minusDays(363), today))
        assertTrue(AxisTicks.monthYear(today.minusDays(364), today))
        val (from, to) = AxisTicks.periodDates(Period.YEAR, today, null)
        assertTrue(AxisTicks.monthYear(from, to))
    }


    @Test
    fun `range text of a week, across a year and one day`() {
        val to = LocalDate.of(2026, 11, 15)
        assertEquals("9.11.2026 \u2013 15.11.2026", AxisTicks.rangeText(to.minusDays(6), to))
        assertEquals("16.11.2025 \u2013 15.11.2026", AxisTicks.rangeText(to.minusYears(1).plusDays(1), to))
        assertEquals("3.11.2026", AxisTicks.rangeText(LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 3)))
    }
}
