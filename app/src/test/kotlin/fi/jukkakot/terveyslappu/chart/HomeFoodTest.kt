package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.food.EnergyBar
import fi.jukkakot.terveyslappu.food.FoodItem
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Made-up foods only (NFR-1). */
class HomeFoodTest {
    private val zone = ZoneId.of("Europe/Helsinki")
    private val today = LocalDate.of(2026, 11, 14)

    private fun item(day: LocalDate, kcal: Double?, treat: Boolean = false): FoodItem {
        val t = day.atTime(12, 0).atZone(zone).toInstant()
        return FoodItem("$day-$kcal-$treat", day, 1, "x", kcal, null, treat, t, zone.rules.getOffset(t), true)
    }

    @Test
    fun `today's sum with the treats' share`() {
        val food = HomeFood.of(listOf(item(today, 1000.0), item(today, 230.0, treat = true)), 7, today)
        assertEquals(1230, food.todayTotal)
        assertEquals(230, food.todayTreat)
        assertTrue(food.todayLogged)
    }

    @Test
    fun `nothing logged today is not logged`() {
        val food = HomeFood.of(listOf(item(today.minusDays(1), 2000.0), item(today, null)), 7, today)
        assertFalse(food.todayLogged)
        assertEquals(0, food.todayTotal)
    }

    @Test
    fun `bars are the logged days of the period ending today, older ones left out`() {
        val items = listOf(item(today, 500.0), item(today.minusDays(1), 2000.0), item(today.minusDays(3), 1800.0), item(today.minusDays(8), 9000.0))
        val week = HomeFood.of(items, 7, today)
        assertEquals(today.minusDays(6), week.from)
        assertEquals(today, week.to)
        assertEquals(listOf(today.minusDays(3), today.minusDays(1), today), week.bars.map { it.from })
        assertEquals(EnergyBar(today.minusDays(1), 1, 2000, 0), week.bars[1])
        // 30 days reaches the 8 days old one; bars stay daily.
        val month = HomeFood.of(items, 30, today)
        assertEquals(4, month.bars.size)
        assertTrue(month.bars.all { it.days == 1 })
    }

    @Test
    fun `average is per logged day and leaves today out`() {
        val items = listOf(item(today, 5000.0), item(today.minusDays(1), 2000.0), item(today.minusDays(3), 1800.0))
        assertEquals(1900, HomeFood.of(items, 7, today).averageKcal)
        assertEquals(null, HomeFood.of(listOf(item(today, 5000.0)), 7, today).averageKcal)
    }

    @Test
    fun `no items gives empty bars`() {
        val food = HomeFood.of(emptyList(), 90, today)
        assertTrue(food.bars.isEmpty())
        assertEquals(today.minusDays(89), food.from)
    }
}
