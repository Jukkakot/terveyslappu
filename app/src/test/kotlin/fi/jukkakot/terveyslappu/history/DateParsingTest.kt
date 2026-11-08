package fi.jukkakot.terveyslappu.history

import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DateParsingTest {
    private val helsinki = ZoneId.of("Europe/Helsinki")
    private fun at(iso: String) = Instant.parse(iso)

    @Test
    fun `iso with and without time`() {
        assertEquals(at("2024-03-01T05:12:00Z"), DateParsing.parse("2024-03-01 07:12", null, true, helsinki))
        assertEquals(at("2024-03-01T05:12:30Z"), DateParsing.parse("2024-03-01T07:12:30", null, true, helsinki))
        assertEquals(at("2024-02-29T22:00:00Z"), DateParsing.parse("2024-03-01", null, true, helsinki))
        assertEquals(at("2024-03-01T07:12:00Z"), DateParsing.parse("2024-03-01T07:12:00Z", null, true, helsinki))
        assertEquals(at("2024-03-01T05:12:00Z"), DateParsing.parse("2024/3/1 07:12", null, true, helsinki))
    }

    @Test
    fun `finnish dotted date and separate time`() {
        assertEquals(at("2024-03-01T05:12:00Z"), DateParsing.parse("1.3.2024", "7.12", true, helsinki))
        assertEquals(at("2024-03-01T05:12:00Z"), DateParsing.parse("01.03.2024 07:12", null, true, helsinki))
    }

    @Test
    fun `slash order`() {
        assertEquals(at("2024-02-12T22:00:00Z"), DateParsing.parse("13/02/2024", null, true, helsinki))
        assertEquals(at("2024-02-12T22:00:00Z"), DateParsing.parse("02/13/2024", null, false, helsinki))
        assertEquals(true to false, DateParsing.dayOrder(listOf("01/02/2024", "13/02/2024")))
        assertEquals(false to false, DateParsing.dayOrder(listOf("01/02/2024", "02/13/2024")))
        assertEquals(true to true, DateParsing.dayOrder(listOf("01/02/2024", "03/04/2024")))
    }

    @Test
    fun `epoch seconds and millis`() {
        assertEquals(Instant.ofEpochSecond(1709269920), DateParsing.parse("1709269920", null, true, helsinki))
        assertEquals(Instant.ofEpochMilli(1709269920123), DateParsing.parse("1709269920123", null, true, helsinki))
    }

    @Test
    fun `impossible dates and non-dates`() {
        assertNull(DateParsing.parse("2024-02-30", null, true, helsinki))
        assertNull(DateParsing.parse("82,4", null, true, helsinki))
        assertFalse(DateParsing.looksLikeDate("82.4"))
        assertTrue(DateParsing.isTimeOnly("07:12"))
        assertFalse(DateParsing.isTimeOnly("82.45"))
    }
}
