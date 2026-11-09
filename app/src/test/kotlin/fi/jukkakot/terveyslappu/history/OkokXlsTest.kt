package fi.jukkakot.terveyslappu.history

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** A made-up OKOK-style workbook written with POI (NFR-1: invented person, dates and weights). */
class OkokXlsTest {
    private val zone = ZoneId.of("Europe/Helsinki")
    private val now = Instant.parse("2026-11-08T12:00:00Z")

    private val header = listOf("Date", "Time", "Weight(Kg)", "BMI", "Body Fat Percentage", "Muscle Weight(Kg)")

    private fun workbook(rows: List<List<Any>>): ByteArray {
        HSSFWorkbook().use { book ->
            val sheet = book.createSheet("Sheet1")
            rows.forEachIndexed { r, cells ->
                val row = sheet.createRow(r)
                cells.forEachIndexed { c, value ->
                    val cell = row.createCell(c)
                    when (value) {
                        is Number -> cell.setCellValue(value.toDouble())
                        else -> cell.setCellValue(value.toString())
                    }
                }
            }
            return ByteArrayOutputStream().also { book.write(it) }.toByteArray()
        }
    }

    private fun okok(vararg data: List<Any>) = workbook(
        listOf(
            listOf("Testi Henkilö"),
            listOf("Gender: Male", "Age: 40", "Height: 180cm"),
            listOf("2024-11-20 ~ 2025-01-05(47Day)"),
            header,
        ) + data,
    )

    private fun parse(bytes: ByteArray): Pair<ParsedHistory, SheetText> {
        assertTrue(XlsTable.isXls(bytes))
        val sheet = OkokLayout.toText(XlsTable.read(bytes))
        return HistoryParser.parse(sheet.text, zone, now) to sheet
    }

    @Test
    fun `years come from the range line across a new year`() {
        val (parsed, sheet) = parse(
            okok(
                listOf("01-05", "07:00", "80.1", "24.10", "18.50", "35.0"),
                listOf("01-02", "07:10", "80.4", "24.20", "18.60", "35.1"),
                listOf("12-30", "07:20", "80.9", "24.30", "18.70", "35.2"),
                listOf("11-20", "07:30", "81.2", "24.40", "18.80", "35.3"),
            ),
        )
        assertTrue(sheet.okok)
        assertEquals(
            listOf(LocalDate.of(2024, 11, 20), LocalDate.of(2024, 12, 30), LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 5)),
            parsed.weights.map { it.time.atZone(zone).toLocalDate() },
        )
    }

    @Test
    fun `weight and time as text`() {
        val (parsed, _) = parse(okok(listOf("01-05", "07:12", "82.4", "24.10", "18.50", "35.0")))
        val w = parsed.weights.single()
        assertEquals(82.4, w.kg)
        assertEquals(Instant.parse("2025-01-05T05:12:00Z"), w.time)
    }

    @Test
    fun `every other column and the profile are kept as written`() {
        val (parsed, sheet) = parse(okok(listOf("01-05", "07:12", "82.4", "24.10", "18.50", "35.0")))
        val raw = parsed.weights.single().raw!!
        assertEquals(OkokLayout.SOURCE, raw.source)
        assertEquals(parsed.weights.single().time.epochSecond, raw.second)
        assertEquals(
            linkedMapOf(
                "BMI" to "24.10",
                "Body Fat Percentage" to "18.50",
                "Muscle Weight(Kg)" to "35.0",
                "profile1" to "Testi Henkilö",
                "Gender" to "Male",
                "Age" to "40",
                "Height" to "180cm",
            ),
            raw.fields,
        )
        assertEquals(3, sheet.keptColumns)
    }

    @Test
    fun `skip reasons point at the sheet row`() {
        val (parsed, _) = parse(okok(listOf("01-05", "07:12", "82.4"), listOf("01-04", "07:12", "82.6"), listOf("01-03", "07:12", "82.5"), listOf("01-02", "07:12", "abc")))
        assertEquals(listOf(8), parsed.skipped.map { it.line }, parsed.skipped.toString())
    }

    @Test
    fun `a plain sheet without the OKOK title rows reads as a table`() {
        val bytes = workbook(listOf(listOf("Date", "Weight"), listOf("2024-03-01", 82.4), listOf("2024-03-02", 82.1)))
        val (parsed, sheet) = parse(bytes)
        assertFalse(sheet.okok)
        assertEquals(listOf(82.4, 82.1), parsed.weights.map { it.kg })
    }

    @Test
    fun `a text file is not taken for xls`() {
        assertFalse(XlsTable.isXls("Date;Weight\n2024-03-01;82,4\n".toByteArray()))
    }

    @Test
    fun `a damaged xls fails to read`() {
        val bytes = okok(listOf("01-05", "07:12", "82.4")).copyOf(600)
        assertTrue(XlsTable.isXls(bytes))
        assertFailsWith<Exception> { XlsTable.read(bytes) }
    }
}
