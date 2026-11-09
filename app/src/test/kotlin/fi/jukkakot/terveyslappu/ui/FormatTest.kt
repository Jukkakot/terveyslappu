package fi.jukkakot.terveyslappu.ui

import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.assertEquals

class FormatTest {
    private val zone = ZoneId.of("Europe/Helsinki")
    private val today = LocalDate.of(2026, 11, 8)

    @Test
    fun `change has a sign and one decimal`() {
        assertEquals("−0,3", formatChange(82.5 - 82.8))
        assertEquals("+1,2", formatChange(1.2))
        assertEquals("±0,0", formatChange(0.02))
        assertEquals("±0,0", formatChange(-0.04))
    }

    @Test
    fun `chart dot and weekly rate`() {
        assertEquals("8.11. 7.05 82,6 kg", formatPoint(Instant.parse("2026-11-08T05:05:00Z"), 82.6, zone))
        assertEquals("−0,25", formatRate(-0.25))
        assertEquals("±0,00", formatRate(0.001))
    }

    @Test
    fun `day is today, yesterday or a short date`() {
        assertEquals("tänään", formatDay(Instant.parse("2026-11-08T05:00:00Z"), today, zone))
        assertEquals("eilen", formatDay(Instant.parse("2026-11-07T21:59:00Z"), today, zone))
        assertEquals("tänään", formatDay(Instant.parse("2026-11-07T22:01:00Z"), today, zone))
        assertEquals(true, formatDay(Instant.parse("2026-11-03T05:00:00Z"), today, zone).endsWith("3.11."))
    }

    @Test
    fun `card time is lower case, a row starts with a capital`() {
        assertEquals("tänään 7.05", formatDayTime(Instant.parse("2026-11-08T05:05:00Z"), today, zone))
        assertEquals("Eilen 7.12", formatRowTime(Instant.parse("2026-11-07T05:12:00Z"), today, zone))
    }
}
