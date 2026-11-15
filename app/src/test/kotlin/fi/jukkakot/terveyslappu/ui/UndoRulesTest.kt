package fi.jukkakot.terveyslappu.ui

import fi.jukkakot.terveyslappu.chart.ChartLayout
import fi.jukkakot.terveyslappu.chart.ChartList
import fi.jukkakot.terveyslappu.chart.ChartSeries
import fi.jukkakot.terveyslappu.chart.CustomChart
import fi.jukkakot.terveyslappu.chart.CustomCharts
import fi.jukkakot.terveyslappu.chart.Period
import fi.jukkakot.terveyslappu.chart.SeriesKind
import fi.jukkakot.terveyslappu.chart.SeriesStyle
import fi.jukkakot.terveyslappu.food.AiItem
import fi.jukkakot.terveyslappu.food.AiProduct
import fi.jukkakot.terveyslappu.food.AmountUnit
import fi.jukkakot.terveyslappu.food.ClipOffers
import fi.jukkakot.terveyslappu.food.Energy
import fi.jukkakot.terveyslappu.home.CardSize
import fi.jukkakot.terveyslappu.home.HomeCard
import fi.jukkakot.terveyslappu.home.HomeLayout
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/** The pure rules behind Kumoa (ui-consistency "Delete and Kumoa"); made-up data only. */
class UndoRulesTest {
    private fun chart(id: Int, name: String) =
        CustomChart(id, name, listOf(ChartSeries(SeriesKind.STEPS, SeriesStyle.BARS)), ChartLayout.OVERLAID, Period.QUARTER)

    @Test
    fun `a restored chart is back at its tab with the same id and the counter unchanged`() {
        val list = ChartList(listOf(chart(1, "A"), chart(2, "B"), chart(3, "C")), next = 4)
        val gone = CustomCharts.delete(list, 2)
        val back = CustomCharts.restore(gone, chart(2, "B"), 1)
        assertEquals(list, back)
        assertSame(back, CustomCharts.restore(back, chart(2, "B"), 1))
        // Another chart deleted meanwhile: the index is clamped.
        assertEquals(listOf(1, 2), CustomCharts.restore(ChartList(listOf(chart(1, "A")), 4), chart(2, "B"), 5).charts.map { it.id })
    }

    @Test
    fun `a chart card comes back at its place and size`() {
        val card = HomeCard(CustomChart.cardId(2), CardSize.BIG)
        val before = HomeLayout(
            listOf(HomeCard(HomeLayout.WEIGHT, CardSize.WIDE), card, HomeCard(HomeLayout.FOOD, CardSize.SMALL)),
            listOf(HomeCard(HomeLayout.STEPS, CardSize.SMALL)),
        )
        val after = before.withoutChart(card.id)
        assertEquals(before, after.withChartBack(card.id, before))
        // A chart that had no card gets none.
        assertSame(after, after.withChartBack(CustomChart.cardId(9), before))
    }

    @Test
    fun `the only shown chart card brings its whole layout back`() {
        val card = HomeCard(CustomChart.cardId(1), CardSize.WIDE)
        val before = HomeLayout(listOf(card), HomeLayout.FIXED)
        val after = before.withoutChart(card.id)
        assertEquals(HomeLayout.DEFAULT, after)
        assertEquals(before, after.withChartBack(card.id, before))
    }

    @Test
    fun `a removed preview row returns to the same place`() {
        val items = listOf(AiItem("Rahka", null, 120.0, false), AiItem("Pulla", null, 250.0, true), AiItem("Omena", null, 50.0, false))
        val products = listOf(AiProduct("Keitto", Energy(60.0, AmountUnit.G), null, false), AiProduct("Leipä", Energy(250.0, AmountUnit.G), null, false))
        val preview = AiPreview(items, products, 0, LocalDate.of(2026, 1, 2))
        assertEquals(preview, preview.withoutItem(1).withItemBack(1, items[1]))
        assertEquals(preview, preview.withoutProduct(0).withProductBack(0, products[0]))
        // Rows removed after it: the index is clamped to the end.
        assertEquals(listOf("Rahka", "Pulla"), preview.withoutItem(1).withoutItem(1).withItemBack(9, items[1]).items.map { it.name })
    }

    @Test
    fun `undoing Ohita offers the content again`() {
        val done = ClipOffers.markDone(listOf("a"), "b")
        assertEquals(listOf("a"), ClipOffers.unmarkDone(done, "b"))
    }
}
