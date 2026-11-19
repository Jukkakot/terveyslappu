package fi.jukkakot.terveyslappu.food

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class AddSearchExitTest {
    private val day = LocalDate.of(2026, 3, 4)

    @Test
    fun `nothing added returns to the origin`() {
        assertEquals(AddSearchTarget.Origin, AddSearchExit.target(day, emptyList()))
    }

    @Test
    fun `one added goes to Ruoka on that day`() {
        assertEquals(AddSearchTarget.Ruoka(day, listOf("a")), AddSearchExit.target(day, listOf("a")))
    }

    @Test
    fun `an add that was undone or failed no longer counts`() {
        val added = linkedSetOf("a", "b")
        added.remove("a")
        added.remove("b")
        assertEquals(AddSearchTarget.Origin, AddSearchExit.target(day, added))
    }

    @Test
    fun `several adds keep their order, import ids included`() {
        val added = linkedSetOf("a")
        added.addAll(listOf("i1", "i2"))
        assertEquals(AddSearchTarget.Ruoka(day, listOf("a", "i1", "i2")), AddSearchExit.target(day, added))
    }
}
