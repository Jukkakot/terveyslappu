package fi.jukkakot.terveyslappu.history

import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Made-up histories (NFR-1). */
class HistoryParserTest {
    private val zone = ZoneId.of("Europe/Helsinki")
    private val now = Instant.parse("2026-11-08T12:00:00Z")
    private fun parse(text: String) = HistoryParser.parse(text, zone, now)

    @Test
    fun `header with date, time, weight and other columns`() {
        val result = parse(
            """
            Date;Time;Weight(kg);BMI;Muscle(kg)
            2024-03-01;07:12;82,4;24,1;35,0
            2024-03-02;07:30;82,1;24,0;35,1
            """.trimIndent(),
        )
        assertEquals(listOf(82.4, 82.1), result.weights.map { it.kg })
        assertEquals(Instant.parse("2024-03-01T05:12:00Z"), result.weights.first().time)
        assertEquals(2, result.layout!!.weightCol)
        assertEquals(1, result.layout.timeCol)
    }

    @Test
    fun `no header, date-time and weight found from values`() {
        val result = parse("01.03.2024 07:12,82.4\n02.03.2024 07:20,82.0\n")
        assertEquals(listOf(82.4, 82.0), result.weights.map { it.kg })
        assertNull(result.layout!!.header)
    }

    @Test
    fun `pounds header converts to kilograms`() {
        val result = parse("date,weight (lb)\n2024-03-01,181.9\n")
        assertEquals(82.51, result.weights.single().kg)
        assertTrue(result.layout!!.pounds)
    }

    @Test
    fun `chinese header and slash dates`() {
        val result = parse("测量时间,体重(kg)\n13/02/2024 07:00,80.5\n14/02/2024 07:00,80.3\n")
        assertEquals(2, result.weights.size)
        assertTrue(result.layout!!.dayFirst)
    }

    @Test
    fun `mixed file - two skipped rows with reasons`() {
        val good = (1..10).joinToString("\n") { "2024-03-%02d 07:00;8%d,0".format(it, it % 10) }
        val result = parse("Date;Weight\n$good\n2024-03-11 07:00;\n2024-03-12 07:00;0\n")
        assertEquals(10, result.weights.size)
        assertEquals(mapOf(SkipReason.UNREADABLE_WEIGHT to 1, SkipReason.OUT_OF_RANGE to 1), result.skippedCounts())
    }

    @Test
    fun `future rows and repeated moments are skipped`() {
        val result = parse("Date;Weight\n2024-03-01 07:00;82,0\n2024-03-01 07:00;82,2\n2030-01-01 07:00;81,0\n")
        assertEquals(listOf(82.2), result.weights.map { it.kg })
        assertEquals(mapOf(SkipReason.DUPLICATE to 1, SkipReason.FUTURE to 1), result.skippedCounts())
    }

    @Test
    fun `nothing usable`() {
        val result = parse("hello world\nfoo bar\n")
        assertTrue(result.weights.isEmpty())
        assertNull(result.layout)
    }

    @Test
    fun `utf-16 file with byte order mark`() {
        val bytes = byteArrayOf(0xFF.toByte(), 0xFE.toByte()) + "Date\tWeight\n2024-03-01\t82,4\n".toByteArray(Charsets.UTF_16LE)
        assertEquals(1, parse(HistoryParser.decode(bytes)).weights.size)
    }

    @Test
    fun `byte order mark and blank lines`() {
        val result = parse("\uFEFFDate,Weight\n\n2024-03-01,82.4\n\n")
        assertEquals(1, result.weights.size)
        assertEquals(3, result.weights.single().line)
    }
}
