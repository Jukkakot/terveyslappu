package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.weight.weightEntry
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Made-up weights only (NFR-1). */
class RangePickTest {
    private val zone = ZoneId.of("Europe/Helsinki")
    private fun d(day: Int) = LocalDate.of(2026, 11, day)
    private fun w(day: Int, time: String, kg: Double) =
        weightEntry("$day $time", d(day).atTime(LocalTime.parse(time)).atZone(zone).toInstant(), null, kg, "me", "me")

    @Test
    fun `the latest weight of each day`() {
        val days = RangePick.dayWeights(listOf(w(3, "07:05", 82.9), w(3, "21:30", 82.4), w(5, "07:00", 82.1)), zone)
        assertEquals(mapOf(d(3) to 82.4, d(5) to 82.1), days)
        assertNull(days[d(4)])
    }

    @Test
    fun `two taps make a range, also from an empty day`() {
        val pick = RangePick().tap(d(4)).tap(d(9))
        assertEquals(d(4) to d(9), pick.result())
        assertTrue(pick.contains(d(6)))
        assertFalse(pick.contains(d(10)))
    }

    @Test
    fun `a day before the start becomes the new start`() {
        val pick = RangePick().tap(d(9)).tap(d(2))
        assertEquals(RangePick(d(2), null), pick)
    }

    @Test
    fun `a single day applies alone`() {
        assertEquals(d(5) to d(5), RangePick().tap(d(5)).result())
        assertNull(RangePick().result())
    }

    @Test
    fun `a third tap starts a new range`() {
        assertEquals(RangePick(d(7), null), RangePick().tap(d(1)).tap(d(3)).tap(d(7)))
    }
}
