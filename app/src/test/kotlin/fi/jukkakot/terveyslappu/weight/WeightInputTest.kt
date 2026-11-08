package fi.jukkakot.terveyslappu.weight

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WeightInputTest {

    @Test
    fun `comma and dot both read as the same weight`() {
        assertEquals(WeightInput.Valid(82.4), parseWeight("82,4"))
        assertEquals(WeightInput.Valid(82.4), parseWeight("82.4"))
        assertEquals(WeightInput.Valid(82.0), parseWeight(" 82 "))
    }

    @Test
    fun `range is 20 to 300 kg inclusive`() {
        assertEquals(WeightInput.Valid(20.0), parseWeight("20"))
        assertEquals(WeightInput.Valid(300.0), parseWeight("300,0"))
        assertEquals(WeightInput.OutOfRange, parseWeight("7"))
        assertEquals(WeightInput.OutOfRange, parseWeight("450"))
        assertEquals(WeightInput.OutOfRange, parseWeight("19,9"))
        assertEquals(WeightInput.OutOfRange, parseWeight("300,1"))
    }

    @Test
    fun `empty and malformed input`() {
        assertEquals(WeightInput.Empty, parseWeight(""))
        assertEquals(WeightInput.Empty, parseWeight("  "))
        assertEquals(WeightInput.Invalid, parseWeight("82,45"))
        assertEquals(WeightInput.Invalid, parseWeight("8a"))
        assertEquals(WeightInput.Invalid, parseWeight("-82"))
        assertEquals(WeightInput.Invalid, parseWeight("82,"))
        assertEquals(WeightInput.Invalid, parseWeight("1000"))
    }

    @Test
    fun `a time after now is in the future`() {
        val now = Instant.parse("2026-11-08T07:00:00Z")
        assertTrue(isInFuture(now.plusSeconds(60), now))
        assertFalse(isInFuture(now, now))
        assertFalse(isInFuture(now.minusSeconds(86_400), now))
    }
}
