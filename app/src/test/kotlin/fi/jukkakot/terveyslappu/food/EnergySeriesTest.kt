package fi.jukkakot.terveyslappu.food

import fi.jukkakot.terveyslappu.chart.Period
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Made-up foods only (NFR-1). */
class EnergySeriesTest {
    private val zone = ZoneId.of("Europe/Helsinki")
    private val today = LocalDate.of(2026, 11, 14) // a Saturday

    private fun item(day: LocalDate, kcal: Double?, treat: Boolean = false, uuid: String = "$day-$kcal-$treat"): FoodItem {
        val t = day.atTime(12, 0).atZone(zone).toInstant()
        return FoodItem(uuid, day, 1, "x", kcal, null, treat, t, zone.rules.getOffset(t), true)
    }

    @Test
    fun `week with a gap draws only logged days`() {
        val days = listOf(0L, 1, 4, 5, 6) // Wednesday and Thursday (2, 3 days back) missing
        val items = days.map { item(today.minusDays(it), 2000.0) } + item(today.minusDays(2), null)
        val s = EnergySeries.of(items, Period.WEEK, today)
        assertEquals(5, s.bars.size)
        assertTrue(s.bars.none { it.from == today.minusDays(2) || it.from == today.minusDays(3) })
        assertEquals(5, s.stats.loggedDays)
        assertEquals(7, s.stats.periodDays)
    }

    @Test
    fun `treat share is its own part`() {
        val day = today.minusDays(1)
        val s = EnergySeries.of(listOf(item(day, 1700.0), item(day, 300.0, treat = true)), Period.WEEK, today)
        assertEquals(EnergyBar(day, 1, 2000, 300), s.bars.single())
    }

    @Test
    fun `average needs three logged days in the window and leaves today out`() {
        val items = listOf(item(today, 5000.0), item(today.minusDays(1), 2000.0), item(today.minusDays(2), 1800.0), item(today.minusDays(4), 2200.0))
        val s = EnergySeries.of(items, Period.WEEK, today)
        assertEquals(listOf(EnergyPoint(today.minusDays(1), 2000)), s.average)
        assertEquals(2000, s.stats.averageKcal)
        assertEquals(4, s.days.size)
        assertEquals(today, s.days.first().day)
    }

    @Test
    fun `partial month statistics`() {
        val items = (1..12).map { item(today.minusDays(it.toLong()), 1690.0) } + (1..12).map { item(today.minusDays(it.toLong()), 210.0, treat = true) }
        val s = EnergySeries.of(items, Period.MONTH, today)
        assertEquals(1900, s.stats.averageKcal)
        assertEquals(210, s.stats.averageTreat)
        assertEquals(12, s.stats.loggedDays)
        assertEquals(30, s.stats.periodDays)
    }

    @Test
    fun `year view has one bar per week from Monday and no average line`() {
        val items = (0..20).map { item(today.minusDays(it.toLong()), 2000.0 + it) }
        val s = EnergySeries.of(items, Period.YEAR, today)
        assertTrue(s.weekly)
        assertTrue(s.average.isEmpty())
        assertTrue(s.bars.all { it.from.dayOfWeek == java.time.DayOfWeek.MONDAY && it.days == 7 })
        assertEquals(4, s.bars.size)
    }

    @Test
    fun `Kaikki counts days from the first log`() {
        val s = EnergySeries.of(listOf(item(today.minusDays(9), 1000.0)), Period.All, today)
        assertEquals(10, s.stats.periodDays)
    }
}
