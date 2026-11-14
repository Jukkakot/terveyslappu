package fi.jukkakot.terveyslappu.data

import fi.jukkakot.terveyslappu.history.HistoryParser
import fi.jukkakot.terveyslappu.history.ParsedHistory
import fi.jukkakot.terveyslappu.history.SkipReason
import fi.jukkakot.terveyslappu.raw.RawEntry
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Made-up weights only (NFR-1). */
class DataFileTest {
    private val zone = ZoneId.of("Europe/Helsinki")
    private val now = Instant.parse("2026-11-09T00:00:00Z")
    private val t = Instant.parse("2024-03-01T05:12:00Z")
    private val scaleRaw = RawEntry(t.epochSecond, "scale", linkedMapOf("packet" to "0339", "key" to "12c0", "weight_raw" to "825"))

    private fun read(format: DataFormat, text: String): ParsedHistory = when (format) {
        DataFormat.CSV -> HistoryParser.parse(text, zone, now)
        DataFormat.JSON -> JsonData.read(text, now)
    }

    @Test
    fun `a questioned weight carries check in both formats and reads back`() {
        val auto = scaleRaw.copy(fields = scaleRaw.fields + ("via" to "auto"))
        val rows = listOf(
            ExportRow(t, ZoneOffset.ofHours(2), 82.0, "Terveyslappu"),
            ExportRow(t.plusSeconds(86_400), ZoneOffset.ofHours(2), 61.3, "Terveyslappu", auto.copy(second = t.plusSeconds(86_400).epochSecond), check = true),
        )
        val csv = CsvData.write(DataSet(rows), zone)
        assertTrue("2024-03-02;07:12:00+02:00;61,3;Terveyslappu;source=scale|packet=0339|key=12c0|weight_raw=825|via=auto;?" in csv.lines())
        val json = JsonData.write(DataSet(rows), zone)
        assertEquals(1, Regex("\"check\": \"\\?\"").findAll(json).count())
        for ((format, text) in listOf(DataFormat.CSV to csv, DataFormat.JSON to json)) {
            val read = read(format, text).weights
            assertEquals(listOf(false, true), read.map { it.check }, format.name)
            assertEquals("auto", read[1].raw?.fields?.get("via"), format.name)
        }
    }

    @Test
    fun `any other check value is no mark and files without the column still import`() {
        val text = "date;time;weight_kg;source;raw;check\n2024-03-01;07:12:00+02:00;82,4;Terveyslappu;;x\n2024-03-02;07:12:00+02:00;82,5;Terveyslappu;;?\n"
        assertEquals(listOf(false, true), HistoryParser.parse(text, zone, now).weights.map { it.check })
        val old = "date;time;weight_kg;source;raw\n2024-03-01;07:12:00+02:00;82,4;Terveyslappu;\n"
        val weights = HistoryParser.parse(old, zone, now).weights
        assertEquals(listOf(82.4), weights.map { it.kg })
        assertEquals(listOf(false), weights.map { it.check })
        val json = "{\"app\":\"terveyslappu\",\"version\":2,\"weights\":[{\"time\":\"2024-03-01T07:12:00+02:00\",\"kg\":82.4,\"source\":\"x\",\"check\":\"ok\"}]}"
        assertEquals(listOf(false), JsonData.read(json, now).weights.map { it.check })
    }

    @Test
    fun `csv layout`() {
        val rows = listOf(
            ExportRow(t, ZoneOffset.ofHours(2), 82.4, "Terveyslappu"),
            ExportRow(t.plusSeconds(86_400), ZoneOffset.ofHours(2), 82.5, "Terveyslappu", scaleRaw.copy(second = t.plusSeconds(86_400).epochSecond)),
        )
        assertEquals(
            listOf(
                "#terveyslappu;version=2",
                "#table;weights",
                "date;time;weight_kg;source;raw;check",
                "2024-03-01;07:12:00+02:00;82,4;Terveyslappu;;",
                "2024-03-02;07:12:00+02:00;82,5;Terveyslappu;source=scale|packet=0339|key=12c0|weight_raw=825;",
                "",
            ),
            CsvData.write(DataSet(rows.reversed()), zone).lines(),
        )
    }

    @Test
    fun `csv keeps the offset, also across zones and the repeated DST hour`() {
        // Finland's clocks go back on 2024-10-27 at 04:00 EEST, so 03:30 happens twice.
        val first = Instant.parse("2024-10-27T00:30:00Z")
        val second = Instant.parse("2024-10-27T01:30:00Z")
        val rows = listOf(
            ExportRow(first, ZoneOffset.ofHours(3), 82.4, "A"),
            ExportRow(second, ZoneOffset.ofHours(2), 82.5, "A"),
            ExportRow(t, null, 82.6, "A"),
        )
        val text = CsvData.write(DataSet(rows), zone)
        assertTrue("2024-10-27;03:30:00+03:00;82,4" in text, text)
        assertTrue("2024-10-27;03:30:00+02:00;82,5" in text, text)
        assertTrue("2024-03-01;07:12:00+02:00;82,6" in text, text)
        for (reader in listOf("America/New_York", "Asia/Tokyo", "Europe/Helsinki")) {
            val parsed = HistoryParser.parse(text, ZoneId.of(reader), now)
            assertEquals(emptyList(), parsed.skipped, reader)
            assertEquals(listOf(t, first, second), parsed.weights.map { it.time }.sorted(), reader)
        }
    }

    @Test
    fun `csv time without an offset is read in the phone zone`() {
        val text = "date;time;weight_kg\n2024-03-01;07:12:00;82,4\n"
        assertEquals(t, HistoryParser.parse(text, zone, now).weights.single().time)
    }

    @Test
    fun `json weight item`() {
        val text = JsonData.write(DataSet(listOf(ExportRow(t, ZoneOffset.ofHours(2), 82.4, "Terveyslappu"))), zone)
        val compact = text.replace(Regex("\\s"), "")
        assertTrue(compact.startsWith("{\"app\":\"terveyslappu\",\"version\":2,\"weights\":["), compact)
        assertTrue("{\"time\":\"2024-03-01T07:12:00+02:00\",\"kg\":82.4,\"source\":\"Terveyslappu\"}" in compact, compact)
    }

    @Test
    fun `both formats read back the same weights and raw data`() {
        val odd = RawEntry(t.epochSecond, "okok", linkedMapOf("note" to "a|b;c\\d=e", "BMI" to "24,1"))
        val rows = (0 until 30).map {
            val time = t.plusSeconds(it * 86_400L + it * 61L)
            ExportRow(time, null, 80.0 + it / 10.0, if (it % 2 == 0) "Terveyslappu" else "A;B", if (it == 0) odd else null)
        }
        for (format in DataFormat.entries) {
            val parsed = read(format, format.write(DataSet(rows.shuffled()), zone))
            assertEquals(rows.map { it.time to it.kg }, parsed.weights.map { it.time to it.kg }, format.name)
            assertEquals(odd, parsed.weights.first().raw, format.name)
            assertEquals(emptyList(), parsed.skipped, format.name)
            assertEquals(emptyList(), parsed.unknownKinds, format.name)
        }
    }

    @Test
    fun `unknown kinds are reported and not read as weights`() {
        val csv = "#terveyslappu;version=3\n#table;steps\ndate;steps\n2024-03-01;9000\n" +
            "#table;weights\ndate;time;weight_kg;source;raw\n2024-03-01;07:12:00;82,4;Testi;\n#goal;kg=75\n"
        val fromCsv = HistoryParser.parse(csv, zone, now)
        assertEquals(listOf(82.4), fromCsv.weights.map { it.kg })
        assertEquals(listOf("steps"), fromCsv.unknownKinds)
        assertEquals(emptyList(), fromCsv.skipped)
        assertEquals(7, fromCsv.weights.single().line)

        val json = """{"app":"terveyslappu","version":3,"steps":[{"date":"2024-03-01","steps":9000}],
            "weights":[{"time":"2024-03-01T07:12:00+02:00","kg":82.4,"source":"Testi","extra":1}]}"""
        val fromJson = JsonData.read(json, now)
        assertEquals(listOf(82.4), fromJson.weights.map { it.kg })
        assertEquals(listOf("steps"), fromJson.unknownKinds)
    }

    @Test
    fun `json rows follow the import rules`() {
        val json = """{"weights":[
            {"time":"2024-03-01T07:12:00+02:00","kg":82.4},
            {"time":"maaliskuu","kg":82.4},
            {"time":"2024-03-02T07:12:00+02:00"},
            {"time":"2024-03-03T07:12:00+02:00","kg":0},
            {"time":"2030-01-01T07:12:00+02:00","kg":80},
            {"time":"2024-03-01T07:12:00+02:00","kg":82.6}]}"""
        val parsed = JsonData.read(json, now)
        assertEquals(listOf(82.6), parsed.weights.map { it.kg })
        assertEquals(
            listOf(SkipReason.DUPLICATE, SkipReason.UNREADABLE_DATE, SkipReason.UNREADABLE_WEIGHT, SkipReason.OUT_OF_RANGE, SkipReason.FUTURE),
            parsed.skipped.map { it.reason },
        )
        assertEquals(emptyList(), JsonData.read("{not json", now).weights)
    }

    @Test
    fun `older export with an owner details line still imports`() {
        val text = "#profile;birth_year=1990|height_cm=180\ndate;time;weight_kg;source;raw\n2024-03-01;07:12:00;82,4;Terveyslappu;\n"
        val parsed = HistoryParser.parse(text, zone, now)
        assertEquals(listOf(82.4), parsed.weights.map { it.kg })
        assertEquals(emptyList(), parsed.skipped)
        val noRaw = HistoryParser.parse("date;time;weight_kg;source\n2024-03-01;07:12:00;82,4;Terveyslappu\n", zone, now)
        assertEquals(listOf(null), noRaw.weights.map { it.raw })
    }

    @Test
    fun `format is detected from the content`() {
        assertEquals(DataFormat.JSON, DataFormat.detect("\uFEFF \n{\"app\":1}"))
        assertEquals(DataFormat.CSV, DataFormat.detect("#terveyslappu;version=2"))
    }

    @Test
    fun `export name has a millisecond timestamp`() {
        val at = LocalDateTime.of(2026, 11, 9, 7, 12, 5, 123_000_000)
        assertEquals("terveyslappu-export-20261109-071205-123.csv", DataFile.exportName(DataFormat.CSV, at))
        assertEquals("terveyslappu-export-20261109-071205-123.json", DataFile.exportName(DataFormat.JSON, at))
    }

    @Test
    fun `kilograms keep up to two decimals`() {
        assertEquals("82,45", CsvData.kg(82.45))
        assertEquals("82", CsvData.kg(82.0))
    }
}
