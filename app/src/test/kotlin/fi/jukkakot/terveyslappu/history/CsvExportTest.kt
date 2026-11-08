package fi.jukkakot.terveyslappu.history

import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.test.assertEquals

class CsvExportTest {
    private val zone = ZoneId.of("Europe/Helsinki")

    @Test
    fun `row format with the record offset`() {
        val row = ExportRow(Instant.parse("2024-03-01T05:12:00Z"), ZoneOffset.ofHours(2), 82.4, "Terveyslappu")
        assertEquals("${CsvExport.HEADER}\n2024-03-01;07:12:00;82,4;Terveyslappu\n", CsvExport.write(listOf(row), zone))
    }

    @Test
    fun `kilograms keep up to two decimals`() {
        assertEquals("82,45", CsvExport.kg(82.45))
        assertEquals("82", CsvExport.kg(82.0))
    }

    @Test
    fun `source with a semicolon is quoted`() {
        val row = ExportRow(Instant.parse("2024-03-01T05:12:00Z"), null, 80.0, "A;B")
        assertEquals("2024-03-01;07:12:00;80;\"A;B\"", CsvExport.write(listOf(row), zone).lines()[1])
    }

    @Test
    fun `an export reads back to the same weights`() {
        val rows = (0 until 40).map {
            ExportRow(Instant.parse("2024-01-01T05:00:00Z").plusSeconds(it * 86_400L + it * 61L), null, 80.0 + it / 10.0, if (it % 2 == 0) "Terveyslappu" else "Garmin Connect")
        }
        val parsed = HistoryParser.parse(CsvExport.write(rows.shuffled(), zone), zone, Instant.parse("2026-11-08T00:00:00Z"))
        assertEquals(rows.map { it.time to it.kg }, parsed.weights.map { it.time to it.kg })
        assertEquals(emptyList(), parsed.skipped)
    }
}
