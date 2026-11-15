package fi.jukkakot.terveyslappu.home

import fi.jukkakot.terveyslappu.home.HomeLayout.Companion.FOOD
import fi.jukkakot.terveyslappu.home.HomeLayout.Companion.STEPS
import fi.jukkakot.terveyslappu.home.HomeLayout.Companion.WEIGHT
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Chart cards in the front page layout (custom-charts design 2); pure. */
class ChartCardsLayoutTest {
    private val with2 = HomeLayoutCodec(allowedExtra = setOf("chart:1", "chart:2"))

    @Test
    fun `a chart id is kept only while listed and never appended`() {
        val stored = HomeLayoutCodec().encode(HomeLayout.DEFAULT.addChart("chart:1"))
        assertEquals(HomeLayout.DEFAULT.addChart("chart:1"), with2.decode(stored))
        assertFalse(with2.decode(HomeLayoutCodec().encode(HomeLayout.DEFAULT)).contains("chart:2"), "chart 2 exists but is not listed")
        assertEquals(HomeLayout.DEFAULT, HomeLayoutCodec(allowedExtra = setOf("chart:2")).decode(stored), "a deleted chart's card is dropped")
    }

    @Test
    fun `a chart card is added last and wide`() {
        val layout = HomeLayout.DEFAULT.addChart("chart:1")
        assertEquals(HomeCard("chart:1", CardSize.WIDE), layout.shown.last())
        assertEquals(layout, layout.addChart("chart:1"))
    }

    @Test
    fun `Piilota on a chart card leaves it in neither list`() {
        val layout = HomeLayout.DEFAULT.addChart("chart:1").resize("chart:1", CardSize.SMALL).hide("chart:1")
        assertFalse(layout.contains("chart:1"))
        assertEquals(HomeLayout.DEFAULT, layout)
        assertEquals(HomeCard("chart:1", CardSize.WIDE), layout.addChart("chart:1").shown.last(), "returns wide")
    }

    @Test
    fun `isDefault is false with a chart id and reset clears chart ids`() {
        val layout = HomeLayout.DEFAULT.addChart("chart:1")
        assertFalse(layout.isDefault)
        assertTrue(layout.reset().isDefault)
    }

    @Test
    fun `deleting the only shown card gives the default layout`() {
        val only = HomeLayout(listOf(HomeCard("chart:1", CardSize.WIDE)), HomeLayout.FIXED)
        assertEquals(HomeLayout.DEFAULT, only.withoutChart("chart:1"))
        val more = HomeLayout.DEFAULT.hide(STEPS).addChart("chart:1")
        assertEquals(listOf(WEIGHT, FOOD), more.withoutChart("chart:1").shown.map { it.id })
    }
}
