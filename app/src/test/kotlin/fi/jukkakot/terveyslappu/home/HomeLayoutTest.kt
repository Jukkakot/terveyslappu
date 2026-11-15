package fi.jukkakot.terveyslappu.home

import fi.jukkakot.terveyslappu.home.CardSize.BIG
import fi.jukkakot.terveyslappu.home.CardSize.SMALL
import fi.jukkakot.terveyslappu.home.CardSize.WIDE
import fi.jukkakot.terveyslappu.home.HomeLayout.Companion.FOOD
import fi.jukkakot.terveyslappu.home.HomeLayout.Companion.STEPS
import fi.jukkakot.terveyslappu.home.HomeLayout.Companion.WEIGHT
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class HomeLayoutTest {
    private val weight = HomeCard(WEIGHT, WIDE)
    private val food = HomeCard(FOOD, SMALL)
    private val steps = HomeCard(STEPS, SMALL)

    private fun ids(rows: List<List<HomeCard>>) = rows.map { row -> row.map { it.id } }

    @Test
    fun `rows of all six orders at default sizes`() {
        val expected = mapOf(
            listOf(weight, food, steps) to listOf(listOf(WEIGHT), listOf(FOOD, STEPS)),
            listOf(weight, steps, food) to listOf(listOf(WEIGHT), listOf(STEPS, FOOD)),
            listOf(food, steps, weight) to listOf(listOf(FOOD, STEPS), listOf(WEIGHT)),
            listOf(steps, food, weight) to listOf(listOf(STEPS, FOOD), listOf(WEIGHT)),
            listOf(food, weight, steps) to listOf(listOf(FOOD), listOf(WEIGHT), listOf(STEPS)),
            listOf(steps, weight, food) to listOf(listOf(STEPS), listOf(WEIGHT), listOf(FOOD)),
        )
        for ((order, rows) in expected) assertEquals(rows, ids(HomeLayout.rows(order)), order.toString())
    }

    @Test
    fun `a lone small card before a wide one keeps its row and a big card is alone`() {
        assertEquals(listOf(listOf(FOOD), listOf(WEIGHT)), ids(HomeLayout.rows(listOf(food, weight))))
        val big = HomeLayout(listOf(weight.copy(size = BIG), food, steps))
        assertEquals(listOf(listOf(WEIGHT), listOf(FOOD, STEPS)), ids(big.rows()))
        assertEquals(listOf(listOf(FOOD), listOf(WEIGHT), listOf(STEPS)), ids(HomeLayout.rows(listOf(food, weight.copy(size = BIG), steps))))
        assertEquals(2, BIG.units)
        assertEquals(2, BIG.span)
        assertEquals(1, SMALL.span)
    }

    @Test
    fun `move and the menu moves at both ends`() {
        val layout = HomeLayout.DEFAULT
        assertEquals(listOf(STEPS, WEIGHT, FOOD), layout.move(2, 0).shown.map { it.id })
        assertEquals(listOf(FOOD, STEPS, WEIGHT), layout.move(0, 2).shown.map { it.id })
        assertSame(layout, layout.move(0, 3))
        assertEquals(listOf(WEIGHT, STEPS, FOOD), layout.moveEarlier(STEPS).shown.map { it.id })
        assertSame(layout, layout.moveEarlier(WEIGHT))
        assertEquals(listOf(FOOD, WEIGHT, STEPS), layout.moveLater(WEIGHT).shown.map { it.id })
        assertSame(layout, layout.moveLater(STEPS))
    }

    @Test
    fun `resize, hide and show`() {
        val layout = HomeLayout.DEFAULT
        assertEquals(SMALL, layout.resize(WEIGHT, SMALL).shown.first().size)
        val hidden = layout.resize(STEPS, BIG).hide(STEPS)
        assertEquals(listOf(WEIGHT, FOOD), hidden.shown.map { it.id })
        assertEquals(listOf(HomeCard(STEPS, BIG)), hidden.hidden)
        val back = hidden.hide(WEIGHT).show(STEPS)
        assertEquals(listOf(FOOD, STEPS), back.shown.map { it.id })
        assertEquals(BIG, back.shown.last().size, "show keeps the size")
        assertEquals(listOf(weight), back.hidden)
        assertSame(back, back.show("nothing"))
    }

    @Test
    fun `the last shown card cannot be hidden`() {
        val one = HomeLayout.DEFAULT.hide(WEIGHT).hide(FOOD)
        assertEquals(listOf(STEPS), one.shown.map { it.id })
        assertFalse(one.canHide(STEPS))
        assertSame(one, one.hide(STEPS))
        assertTrue(HomeLayout.DEFAULT.canHide(STEPS))
    }

    @Test
    fun `reset and isDefault`() {
        assertTrue(HomeLayout.DEFAULT.isDefault)
        val edited = HomeLayout.DEFAULT.hide(FOOD)
        assertFalse(edited.isDefault)
        assertFalse(HomeLayout.DEFAULT.move(0, 1).isDefault)
        assertTrue(edited.reset().isDefault)
    }
}
