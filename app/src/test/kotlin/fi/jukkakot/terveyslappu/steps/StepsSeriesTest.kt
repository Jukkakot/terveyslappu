package fi.jukkakot.terveyslappu.steps

import fi.jukkakot.terveyslappu.chart.Period
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Made-up step counts only (NFR-1). */
class StepsSeriesTest {
    // A Saturday.
    private val today = LocalDate.of(2026, 11, 14)

    private fun day(daysAgo: Long, steps: Long) = StepsDay(today.minusDays(daysAgo), steps)

    @Test
    fun `a week with a gap draws bars only for days with steps`() {
        // Wednesday and Thursday (3 and 2 days ago) without data.
        val input = listOf(day(0, 3000), day(1, 8000), day(4, 7000), day(5, 9000), day(6, 8000))
        val s = StepsSeries.of(input, Period.WEEK, today)
        assertEquals(today.minusDays(6), s.from)
        assertEquals(5, s.bars.size)
        assertTrue(s.bars.none { it.from == today.minusDays(2) || it.from == today.minusDays(3) })
        assertTrue(s.bars.all { it.days == 1 })
        assertTrue(!s.weekly)
    }

    @Test
    fun `today is left out of the average but counts as a day with steps`() {
        val input = listOf(day(0, 3000), day(1, 8000), day(2, 8000))
        val stats = StepsSeries.of(input, Period.WEEK, today).stats
        assertEquals(8000, stats.averageSteps)
        assertEquals(3, stats.stepDays)
        assertEquals(7, stats.periodDays)
    }

    @Test
    fun `a zero day is a gap, not a bar`() {
        val s = StepsSeries.of(listOf(day(1, 0), day(2, 5000)), Period.WEEK, today)
        assertEquals(1, s.bars.size)
        assertEquals(1, s.stats.stepDays)
    }

    @Test
    fun `the 7-day line needs three days with steps in the window and skips today`() {
        val input = listOf(day(0, 1000), day(1, 6000), day(2, 8000), day(3, 10000), day(10, 5000))
        val s = StepsSeries.of(input, Period.MONTH, today)
        // Only 3 days ago? window holds 3 and 10 days ago -> too few; 2 days ago: 2,3 -> too few; 1 day ago: 1,2,3 -> ok.
        assertEquals(listOf(StepsPoint(today.minusDays(1), 8000)), s.average)
    }

    @Test
    fun `three months stay daily, a year is weekly with the average per day with steps`() {
        val input = (1L..120L).map { day(it, if (it % 2 == 0L) 6000 else 8000) }
        val quarter = StepsSeries.of(input, Period.QUARTER, today)
        assertTrue(!quarter.weekly)
        assertTrue(quarter.bars.all { it.days == 1 })
        val year = StepsSeries.of(input, Period.YEAR, today)
        assertTrue(year.weekly)
        assertTrue(year.average.isEmpty())
        assertTrue(year.bars.all { it.days == 7 && it.from.dayOfWeek == java.time.DayOfWeek.MONDAY })
        // A full past week alternates 6000 and 8000 -> average between them.
        assertTrue(year.bars.dropLast(1).drop(1).all { it.steps in 6000..8000 })
    }

    @Test
    fun `a weekly bar leaves today out`() {
        // Today is a Saturday; Monday to Friday of this week 5000, today 100.
        val input = (0L..5L).map { if (it == 0L) day(0, 100) else day(it, 5000) } + day(200, 4000)
        val year = StepsSeries.of(input, Period.YEAR, today)
        assertEquals(5000, year.bars.last().steps)
    }

    @Test
    fun `Kaikki starts at the first day with steps, or today without any`() {
        val s = StepsSeries.of(listOf(day(20, 5000), day(3, 6000)), Period.All, today)
        assertEquals(today.minusDays(20), s.from)
        val empty = StepsSeries.of(emptyList(), Period.All, today)
        assertEquals(today, empty.from)
        assertTrue(empty.bars.isEmpty())
        assertNull(empty.stats.averageSteps)
        assertEquals(1, empty.stats.periodDays)
    }

    @Test
    fun `the list is newest first and only the period's days`() {
        val s = StepsSeries.of(listOf(day(2, 1), day(0, 3), day(1, 2), day(40, 9)), Period.MONTH, today)
        assertEquals(listOf(today, today.minusDays(1), today.minusDays(2)), s.days.map { it.day })
    }

    @Test
    fun `home card shows today, bars of its own period and the average without today`() {
        val input = listOf(day(0, 6420), day(1, 8000), day(3, 7600), day(10, 9000))
        val week = HomeSteps.of(input, 7, today)
        assertEquals(6420, week.today)
        assertEquals(today.minusDays(6), week.from)
        assertEquals(today, week.to)
        assertEquals(3, week.bars.size)
        assertEquals(7800, week.averageSteps)
        assertEquals(4, HomeSteps.of(input, 30, today).bars.size)
    }

    @Test
    fun `home card without steps today or at all`() {
        val noToday = HomeSteps.of(listOf(day(1, 8000)), 7, today)
        assertTrue(noToday.noDataToday)
        assertEquals(8000, noToday.averageSteps)
        val none = HomeSteps.of(emptyList(), 90, today)
        assertTrue(none.noDataToday)
        assertNull(none.averageSteps)
        assertTrue(none.bars.isEmpty())
        assertEquals(today.minusDays(89), none.from)
    }
}
