package fi.jukkakot.terveyslappu.chart

import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The trend rule (period-trend-line design 1–3, 8); made-up values only. */
class TrendLineTest {
    private val start = LocalDate.of(2025, 1, 1)

    private fun days(n: Int, from: LocalDate = start, value: (Int) -> Double) = (0 until n).map { from.plusDays(it.toLong()) to value(it) }

    @Test
    fun `a rising noisy year follows the rise`() {
        val rnd = Random(7)
        val points = days(365) { 6000.0 + 3000.0 * it / 364 + rnd.nextDouble(-4000.0, 6000.0) }
        val trend = TrendLine.of(points, 365).single()
        assertEquals(365, trend.values.size)
        assertTrue(abs(trend.values[30] - (6000.0 + 3000.0 * 30 / 364 + 1000)) < 900, "start ${trend.values[30]}")
        assertTrue(abs(trend.values[330] - (6000.0 + 3000.0 * 330 / 364 + 1000)) < 900, "end ${trend.values[330]}")
        assertTrue(trend.values[330] > trend.values[30] + 1500)
    }

    @Test
    fun `a gap of 30 days joins and 31 splits`() {
        val a = days(15) { 80.0 }
        val joined = a + days(15, a.last().first.plusDays(30)) { 80.0 }
        val split = a + days(15, a.last().first.plusDays(31)) { 80.0 }
        assertEquals(1, TrendLine.of(joined, 120).size)
        assertEquals(2, TrendLine.of(split, 120).size)
    }

    @Test
    fun `a part under 10 values gives nothing`() {
        assertTrue(TrendLine.of(days(9) { 80.0 }, 90).isEmpty())
        assertEquals(1, TrendLine.of(days(10) { 80.0 }, 90).size)
    }

    @Test
    fun `no part with 10 values falls back to the flat average`() {
        // 6 days in March and 8 in September: both parts have values, none is long enough.
        val points = days(6, LocalDate.of(2025, 3, 1)) { 2000.0 } + days(8, LocalDate.of(2025, 9, 1)) { 2200.0 }
        assertEquals(AverageLine.Flat(2100.0), AverageLine.of(300, points, 2100.0))
        assertEquals(AverageLine.None, AverageLine.of(300, points, null))
    }

    @Test
    fun `axis days decide flat or trend`() {
        val points = days(40) { 2000.0 + it }
        assertEquals(AverageLine.Flat(2000.0), AverageLine.of(31, points, 2000.0))
        assertTrue(AverageLine.of(32, points, 2000.0) is AverageLine.Trend)
    }

    @Test
    fun `one outlier barely moves the line`() {
        val plain = days(90) { 82.0 + 0.3 * sin(it / 5.0) }
        val odd = plain.mapIndexed { i, p -> if (i == 45) p.first to 120.0 else p }
        val a = TrendLine.of(plain, 90).single()
        val b = TrendLine.of(odd, 90).single()
        assertTrue(abs(a.values[45] - b.values[45]) < 0.3, "${a.values[45]} vs ${b.values[45]}")
    }

    @Test
    fun `3 kk is less smooth than 2 v`() {
        val rnd = Random(3)
        val points = days(365) { 82.0 + 0.8 * sin(it / 6.0) + rnd.nextDouble(-0.5, 0.5) }
        fun wiggle(s: TrendSegment) = s.values.zipWithNext { a, b -> abs(b - a) }.sum()
        assertTrue(wiggle(TrendLine.of(points, 90).single()) > wiggle(TrendLine.of(points, 730).single()) * 1.5)
        assertEquals(21, TrendLine.window(90))
        assertEquals(60, TrendLine.window(365))
        assertEquals(121, TrendLine.window(730))
    }

    @Test
    fun `a failing or non-finite smoothing gives no trend and no exception`() {
        val points = days(40) { 80.0 }
        assertTrue(TrendLine.of(points, 90) { _, _, _ -> throw IllegalStateException("boom") }.isEmpty())
        assertTrue(TrendLine.of(points, 90) { x, _, _ -> DoubleArray(x.size) { Double.NaN } }.isEmpty())
        assertEquals(AverageLine.Flat(80.0), AverageLine.of(90, points, 80.0) { _, _, _ -> throw ArithmeticException() })
    }

    @Test
    fun `at interpolates inside a segment and is null outside`() {
        val s = TrendSegment(listOf(start, start.plusDays(2)), listOf(80.0, 82.0))
        assertEquals(81.0, s.at(start.plusDays(1)))
        assertNull(s.at(start.minusDays(1)))
        assertNull(s.at(start.plusDays(3)))
        assertEquals(80.5, s.clamped(80.5, 81.5).values.first())
    }
}
