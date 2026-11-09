package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.ui.shownDirection
import fi.jukkakot.terveyslappu.weight.weightEntry
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RowChangesTest {
    private val now = Instant.parse("2026-11-09T06:00:00Z")
    private fun at(id: String, daysAgo: Long, kg: Double) =
        weightEntry(id, now.minusSeconds(daysAgo * 86_400), null, kg, "me", "me")

    @Test
    fun `each weight changes from the one before it in time, the oldest has none`() {
        val newestFirst = listOf(at("c", 0, 81.9), at("b", 1, 82.4), at("a", 2, 82.0))
        val changes = changesFromPrevious(newestFirst)
        assertEquals(-0.5, changes.getValue("c"), 1e-9)
        assertEquals(0.4, changes.getValue("b"), 1e-9)
        assertNull(changes["a"])
    }

    @Test
    fun `input order does not matter`() {
        val oldestFirst = listOf(at("a", 2, 82.0), at("b", 1, 82.4))
        assertEquals(0.4, changesFromPrevious(oldestFirst).getValue("b"), 1e-9)
    }

    @Test
    fun `empty and single lists have no changes`() {
        assertEquals(emptyMap(), changesFromPrevious(emptyList()))
        assertEquals(emptyMap(), changesFromPrevious(listOf(at("a", 0, 80.0))))
    }

    @Test
    fun `arrow follows the shown rounding`() {
        assertEquals(1, shownDirection(0.4, 1))
        assertEquals(-1, shownDirection(-0.5, 1))
        assertEquals(0, shownDirection(0.04, 1))
        assertEquals(0, shownDirection(-0.04, 1))
        assertEquals(-1, shownDirection(-0.04, 2))
        assertEquals(0, shownDirection(0.004, 2))
    }
}
