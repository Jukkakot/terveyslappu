package fi.jukkakot.terveyslappu.home

import fi.jukkakot.terveyslappu.home.CardSize.BIG
import fi.jukkakot.terveyslappu.home.CardSize.SMALL
import fi.jukkakot.terveyslappu.home.CardSize.WIDE
import fi.jukkakot.terveyslappu.home.HomeLayout.Companion.FOOD
import fi.jukkakot.terveyslappu.home.HomeLayout.Companion.STEPS
import fi.jukkakot.terveyslappu.home.HomeLayout.Companion.WEIGHT
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HomeLayoutCodecTest {
    private val codec = HomeLayoutCodec()

    private fun stored(shown: String, hidden: String = "", v: Int = 1) = """{"v":$v,"shown":[$shown],"hidden":[$hidden]}"""
    private fun card(id: String, size: String) = """{"id":"$id","size":"$size"}"""

    @Test
    fun `unusable values mean the default`() {
        for (text in listOf(null, "", "  ", "{nope", "[]", stored("", v = 2), stored(card(STEPS, "big"), v = 2), stored(""), stored("", card(WEIGHT, "wide")))) {
            assertEquals(HomeLayout.DEFAULT, codec.decode(text), text.toString())
        }
    }

    @Test
    fun `round trip`() {
        val layout = HomeLayout.DEFAULT.resize(STEPS, BIG).move(2, 0).hide(FOOD)
        assertEquals(layout, codec.decode(codec.encode(layout)))
        assertEquals("""{"v":1,"shown":[{"id":"steps","size":"big"},{"id":"weight","size":"wide"}],"hidden":[{"id":"burn","size":"small"},{"id":"food","size":"small"}]}""", codec.encode(layout))
    }

    @Test
    fun `a layout stored before Kulutus looks the same and has Kulutus hidden`() {
        val old = codec.decode(stored(listOf(card(WEIGHT, "wide"), card(FOOD, "small"), card(STEPS, "small")).joinToString(",")))
        assertEquals(listOf(WEIGHT, FOOD, STEPS), old.shown.map { it.id })
        assertEquals(listOf(HomeCard(HomeLayout.BURN, SMALL)), old.hidden)
        assertTrue(old.isDefault)
        val shown = old.show(HomeLayout.BURN)
        assertEquals(HomeCard(HomeLayout.BURN, SMALL), shown.shown.last())
        assertEquals(shown, codec.decode(codec.encode(shown)))
        assertEquals(HomeLayout.DEFAULT, shown.reset())
        assertEquals(HomeLayout.DEFAULT, codec.decode(null))
    }

    @Test
    fun `unknown ids are dropped, duplicates keep the first, unknown sizes fall back`() {
        val layout = codec.decode(stored(listOf(card("mystery", "big"), card(STEPS, "huge"), card(WEIGHT, "small"), card(STEPS, "big")).joinToString(","), card(WEIGHT, "big")))
        assertEquals(listOf(HomeCard(STEPS, SMALL), HomeCard(WEIGHT, SMALL), HomeCard(FOOD, SMALL)), layout.shown)
        assertEquals(listOf(HomeCard(HomeLayout.BURN, SMALL)), layout.hidden)
    }

    @Test
    fun `missing fixed cards are appended at their default size in default order`() {
        val layout = codec.decode(stored(card(STEPS, "wide")))
        assertEquals(listOf(HomeCard(STEPS, WIDE), HomeCard(WEIGHT, WIDE), HomeCard(FOOD, SMALL)), layout.shown)
        val hiddenKept = codec.decode(stored(card(STEPS, "small"), card(FOOD, "big")))
        assertEquals(listOf(HomeCard(STEPS, SMALL), HomeCard(WEIGHT, WIDE)), hiddenKept.shown)
        assertEquals(listOf(HomeCard(FOOD, BIG), HomeCard(HomeLayout.BURN, SMALL)), hiddenKept.hidden)
    }

    @Test
    fun `extra ids are kept when allowed and never appended`() {
        val charts = HomeLayoutCodec(allowedExtra = setOf("chart:1", "chart:2"))
        val layout = charts.decode(stored(listOf(card(WEIGHT, "wide"), card("chart:1", "big"), card("chart:9", "big"), card(FOOD, "small"), card(STEPS, "small")).joinToString(",")))
        assertEquals(listOf(WEIGHT, "chart:1", FOOD, STEPS), layout.shown.map { it.id })
        assertEquals(listOf(HomeLayout.BURN), layout.hidden.map { it.id }, "a stored layout without chart:2 decodes without it")
        assertEquals(listOf(WEIGHT, FOOD, STEPS), codec.decode(charts.encode(layout)).shown.map { it.id }, "not allowed: dropped")
    }
}
