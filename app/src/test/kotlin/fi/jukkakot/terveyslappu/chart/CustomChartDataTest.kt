package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.chart.SeriesKind.ENERGY
import fi.jukkakot.terveyslappu.chart.SeriesKind.STEPS
import fi.jukkakot.terveyslappu.chart.SeriesKind.WEIGHT
import fi.jukkakot.terveyslappu.derived.Quantity
import fi.jukkakot.terveyslappu.food.Amount
import fi.jukkakot.terveyslappu.food.AmountUnit
import fi.jukkakot.terveyslappu.food.FoodItem
import fi.jukkakot.terveyslappu.steps.StepsDay
import fi.jukkakot.terveyslappu.weight.weightEntry
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Made-up values (NFR-1). */
class CustomChartDataTest {
    private val zone: ZoneId = ZoneOffset.UTC
    private val today = LocalDate.of(2026, 11, 15)

    private fun chart(period: Period = Period.MONTH, vararg kinds: SeriesKind = SeriesKind.entries.toTypedArray()) =
        CustomChart(1, "K", kinds.map { ChartSeries(it, CustomCharts.defaultStyle(it)) }, ChartLayout.STACKED, period)

    private fun weight(id: String, day: LocalDate, hour: Int, kg: Double) = weightEntry(id, day.atTime(hour, 0).toInstant(ZoneOffset.UTC), null, kg, "me", "me")

    private fun food(day: LocalDate, kcal: Double) = FoodItem("f$day", day, 1, "Annos", kcal, Amount(1.0, AmountUnit.KPL), false, day.atTime(12, 0).toInstant(ZoneOffset.UTC), ZoneOffset.UTC, true)

    @Test
    fun `the day's last weight not marked "?"`() {
        val day = today.minusDays(3)
        val weights = listOf(weight("a", day, 7, 82.0), weight("b", day, 20, 82.4), weight("q", day, 22, 90.0))
        val data = CustomChartData.of(chart(), today, zone, weights, emptyList(), emptyList(), questioned = setOf("q"))
        assertEquals(82.4, data.of(WEIGHT)!!.at(day)!!.value)
        assertEquals(1, data.of(WEIGHT)!!.values!!.size)
    }

    @Test
    fun `long periods stay daily`() {
        for (period in listOf(Period.QUARTER, Period.YEAR)) {
            val items = (0L until 200L step 2).map { food(today.minusDays(it), 1500.0 + it) }
            val data = CustomChartData.of(chart(period, ENERGY), today, zone, null, items, null)
            val shown = items.count { it.day >= data.axis.from }
            assertEquals(shown, data.of(ENERGY)!!.values!!.size, "one value per logged day at $period")
            assertFalse(data.axis.weekly)
        }
    }

    @Test
    fun `Kaikki starts at the first loaded data, empty Kaikki is just today`() {
        val steps = listOf(StepsDay(today.minusDays(40), 5000))
        val weights = listOf(weight("a", today.minusDays(10), 8, 80.0))
        val notLoaded = CustomChartData.of(chart(Period.All), today, zone, weights, emptyList(), null)
        assertEquals(today.minusDays(10), notLoaded.axis.from, "steps not loaded yet")
        val loaded = CustomChartData.of(chart(Period.All), today, zone, weights, emptyList(), steps)
        assertEquals(today.minusDays(40), loaded.axis.from, "moves earlier when steps load")
        val empty = CustomChartData.of(chart(Period.All), today, zone, emptyList(), emptyList(), emptyList())
        assertEquals(today, empty.axis.from)
        assertEquals(today, empty.axis.to)
        assertTrue(empty.empty)
    }

    @Test
    fun `scales follow the fixed tabs`() {
        val data = CustomChartData.of(chart(), today, zone, null, listOf(food(today, 1900.0)), listOf(StepsDay(today.minusDays(1), 11_400), StepsDay(today, 3000)))
        assertEquals(14_000.0, data.of(STEPS)!!.max)
        assertEquals(2_500.0, data.of(ENERGY)!!.max)
        assertEquals(0.0, data.of(STEPS)!!.min)
        assertNull(data.of(WEIGHT)!!.values, "weights not loaded")
    }

    @Test
    fun `the axis maps day centres`() {
        val axis = ChartAxis(today.minusDays(9), today)
        assertEquals(10, axis.days)
        assertEquals(5f, axis.x(today.minusDays(9), 100f))
        assertEquals(95f, axis.x(today, 100f))
        assertEquals(today, axis.dayAt(99f, 100f))
        assertEquals(today.minusDays(9), axis.dayAt(-3f, 100f))
    }

    @Test
    fun `readout leaves gated kinds out and has missing parts`() {
        val day = today.minusDays(2)
        val data = CustomChartData.of(chart(), today, zone, listOf(weight("a", day, 8, 81.0)), emptyList(), null)
        val readout = ChartReadout.of(day, data) { it != STEPS }
        assertEquals(listOf(WEIGHT, ENERGY), readout.values.map { it.first })
        assertEquals(81.0, readout.values[0].second!!.value)
        assertNull(readout.values[1].second)
        assertEquals(today, ChartReadout.of(today, data) { true }.day)
    }

    @Test
    fun `history pages put own charts after Askeleet and Uusi kaavio last`() {
        val charts = listOf(chart().copy(id = 3), chart().copy(id = 5))
        val pages = HistoryPages.of(charts)
        assertEquals(7, pages.size)
        assertEquals(HistoryPage.Fixed(Quantity.WEIGHT), pages[0])
        assertEquals(HistoryPage.Steps, pages[HistoryPages.STEPS])
        assertEquals(4, pages.indexOfChart(3))
        assertEquals(5, pages.indexOfChart(5))
        assertEquals(HistoryPage.NewChart, pages[6])
        assertEquals(4, HistoryPages.afterDelete(charts, 5))
        assertEquals(HistoryPages.STEPS, HistoryPages.afterDelete(charts, 3))
    }

    @Test
    fun `averages leave today out for energy and steps but not for weight`() {
        val items = listOf(food(today, 400.0), food(today.minusDays(1), 2000.0), food(today.minusDays(2), 2200.0))
        val steps = listOf(StepsDay(today, 61), StepsDay(today.minusDays(1), 8000), StepsDay(today.minusDays(3), 9000))
        val weights = listOf(weight("a", today, 8, 80.0), weight("b", today.minusDays(1), 8, 82.0), weight("c", today.minusDays(1), 20, 83.0), weight("q", today.minusDays(2), 8, 99.0))
        val data = CustomChartData.of(chart(), today, zone, weights, items, steps, questioned = setOf("q"))
        assertEquals(2100.0, data.of(ENERGY)!!.average)
        assertEquals(8500.0, data.of(STEPS)!!.average)
        assertEquals(81.5, data.of(WEIGHT)!!.average)
    }

    @Test
    fun `no average when there is nothing to average`() {
        val data = CustomChartData.of(chart(), today, zone, emptyList(), listOf(food(today, 400.0)), null)
        assertNull(data.of(WEIGHT)!!.average)
        assertNull(data.of(ENERGY)!!.average, "only today logged")
        assertNull(data.of(STEPS)!!.average, "not loaded")
    }

    @Test
    fun `one weight averages to itself`() {
        val data = CustomChartData.of(chart(Period.MONTH, WEIGHT), today, zone, listOf(weight("a", today.minusDays(2), 8, 82.0)), null, null)
        assertEquals(82.0, data.of(WEIGHT)!!.average)
    }
}
