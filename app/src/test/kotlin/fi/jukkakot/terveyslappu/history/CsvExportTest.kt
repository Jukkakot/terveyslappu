package fi.jukkakot.terveyslappu.history

import fi.jukkakot.terveyslappu.raw.RawEntry
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
        assertEquals("${CsvExport.HEADER}\n2024-03-01;07:12:00;82,4;Terveyslappu;\n", CsvExport.write(listOf(row), zone))
    }

    private val scaleRaw = RawEntry(
        Instant.parse("2024-03-01T05:12:00Z").epochSecond, "scale",
        linkedMapOf("packet" to "0339", "key" to "12c0", "weight_raw" to "825", "impedance" to "5123"),
    )

    @Test
    fun `scale row carries its raw data`() {
        val row = ExportRow(Instant.parse("2024-03-01T05:12:00Z"), ZoneOffset.ofHours(2), 82.5, "Terveyslappu", scaleRaw)
        assertEquals(
            "2024-03-01;07:12:00;82,5;Terveyslappu;source=scale|packet=0339|key=12c0|weight_raw=825|impedance=5123",
            CsvExport.write(listOf(row), zone).lines()[1],
        )
    }

    @Test
    fun `raw data reads back with the weight`() {
        val odd = RawEntry(scaleRaw.second, "okok", linkedMapOf("note" to "a|b;c\\d=e", "bmi" to "24,1"))
        val rows = listOf(ExportRow(Instant.parse("2024-03-01T05:12:00Z"), null, 82.5, "Terveyslappu", odd))
        val parsed = HistoryParser.parse(CsvExport.write(rows, zone), zone, Instant.parse("2026-11-08T00:00:00Z"))
        assertEquals(listOf(odd), parsed.weights.map { it.raw })
    }

    @Test
    fun `older export without the raw column still imports`() {
        val text = "date;time;weight_kg;source\n2024-03-01;07:12:00;82,4;Terveyslappu\n"
        val parsed = HistoryParser.parse(text, zone, Instant.parse("2026-11-08T00:00:00Z"))
        assertEquals(listOf(82.4), parsed.weights.map { it.kg })
        assertEquals(listOf(null), parsed.weights.map { it.raw })
    }

    @Test
    fun `kilograms keep up to two decimals`() {
        assertEquals("82,45", CsvExport.kg(82.45))
        assertEquals("82", CsvExport.kg(82.0))
    }

    @Test
    fun `source with a semicolon is quoted`() {
        val row = ExportRow(Instant.parse("2024-03-01T05:12:00Z"), null, 80.0, "A;B")
        assertEquals("2024-03-01;07:12:00;80;\"A;B\";", CsvExport.write(listOf(row), zone).lines()[1])
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
