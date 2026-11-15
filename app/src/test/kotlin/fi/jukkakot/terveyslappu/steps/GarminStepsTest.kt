package fi.jukkakot.terveyslappu.steps

import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.LocalDate
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Made-up ZIPs, step counts and kcal only (NFR-1). */
class GarminStepsTest {
    private val today = LocalDate.of(2026, 11, 15)
    private val dir = "export-123/DI_CONNECT/DI-Connect-Aggregator/"

    private fun zip(vararg entries: Pair<String, String>): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            for ((name, text) in entries) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(text.toByteArray())
                zip.closeEntry()
            }
        }
        return out.toByteArray()
    }

    private fun summary(date: String, steps: String) =
        """{"userProfilePK":1,"calendarDate":"$date","totalSteps":$steps,"source":2,"wellnessStartTimeLocal":"${date}T00:00:00.0","nested":{"a":[1,2]}}"""

    private fun read(bytes: ByteArray) = GarminStepsReader.read(ByteArrayInputStream(bytes), today)

    @Test
    fun `reads UDS files under a nested folder, larger count wins, bad rows skipped`() {
        val bytes = zip(
            "export-123/DI_CONNECT/DI-Connect-Fitness/sleep.json" to "[${summary("2025-01-01", "99999")}]",
            "${dir}UDSFile_2025-01-01_2025-04-10.json" to "[" + listOf(
                summary("2025-01-02", "5000"),
                summary("2025-01-03", "null"),
                summary("2025-01-04", "0"),
                summary("2025-13-40", "4000"),
                summary("2025-01-05", "6000"),
            ).joinToString(",") + "]",
            "${dir}UDSFile_2025-01-05_2025-04-15.json" to "[${summary("2025-01-05", "6500")},${summary("2026-11-15", "300")},${summary("2026-12-01", "300")}]",
            "${dir}inner.zip" to "PK not read",
        )
        val result = read(bytes)
        assertEquals(2, result.entries)
        assertEquals(listOf(StepsDay(LocalDate.of(2025, 1, 2), 5000), StepsDay(LocalDate.of(2025, 1, 5), 6500)), result.days.steps())
    }

    @Test
    fun `a ZIP without UDS files gives nothing`() {
        val result = read(zip("photos/a.json" to "[]", "DI_CONNECT/DI-Connect-Wellness/UDSFile_x.json" to "[${summary("2025-01-02", "5000")}]"))
        assertEquals(0, result.entries)
        assertTrue(result.days.isEmpty())
    }

    @Test
    fun `UDS at the ZIP root is accepted`() {
        val result = read(zip("DI_CONNECT/DI-Connect-Aggregator/UDSFile_a.json" to "[${summary("2025-01-02", "5000")}]"))
        assertEquals(1, result.days.size)
    }

    @Test
    fun `a corrupt ZIP or JSON throws`() {
        assertFailsWith<Exception> { read(zip("${dir}UDSFile_a.json" to "[{\"calendarDate\":")) }
    }

    @Test
    fun `zip magic`() {
        assertTrue(GarminStepsReader.isZip(zip("a" to "b")))
        assertFalse(GarminStepsReader.isZip("date;kg".toByteArray()))
        assertFalse(GarminStepsReader.isZip(byteArrayOf(0x50)))
    }

    private fun kcal(date: String, steps: String, active: String, bmr: String) =
        """{"calendarDate":"$date","totalSteps":$steps,"activeKilocalories":$active,"bmrKilocalories":$bmr,"totalKilocalories":0.0}"""

    @Test
    fun `kcal are kept next to steps, steps-only and kcal-only days, BMR-only days dropped`() {
        val result = read(
            zip(
                "${dir}UDSFile_a.json" to "[" + listOf(
                    kcal("2025-02-01", "8000", "450.4", "1850.6"),
                    kcal("2025-02-02", "7000", "null", "1850.0"),
                    kcal("2025-02-03", "null", "320.0", "1800.0"),
                    kcal("2025-02-04", "null", "0.0", "1790.0"),
                    kcal("2025-02-05", "0", "\"x\"", "1790.0"),
                ).joinToString(",") + "]",
            ),
        )
        assertEquals(
            listOf(
                GarminDay(LocalDate.of(2025, 2, 1), 8000, 450, 1851),
                GarminDay(LocalDate.of(2025, 2, 2), 7000),
                GarminDay(LocalDate.of(2025, 2, 3), null, 320, 1800),
            ),
            result.days,
        )
        assertEquals(2, result.stepDays)
        assertEquals(2, result.kcalDays)
        assertEquals(2301L, result.days.first().total)
    }

    @Test
    fun `a day twice keeps the larger steps and the larger kcal total independently`() {
        val result = read(
            zip(
                "${dir}UDSFile_a.json" to "[${kcal("2025-02-01", "9000", "100.0", "1800.0")}]",
                "${dir}UDSFile_b.json" to "[${kcal("2025-02-01", "5000", "400.0", "1800.0")}]",
            ),
        )
        assertEquals(listOf(GarminDay(LocalDate.of(2025, 2, 1), 9000, 400, 1800)), result.days)
    }

    @Test
    fun `store replaces whole, deletes and restores`() {
        val file = File.createTempFile("garmin", ".json").apply { delete() }
        try {
            val store = GarminHistoryStore(file)
            assertTrue(store.days.value.isEmpty())
            val days = listOf(GarminDay(LocalDate.of(2025, 1, 5), 6500, 300, 1800), GarminDay(LocalDate.of(2025, 1, 2), 5000))
            store.replace(days)
            assertEquals(days.sortedBy { it.day }, GarminHistoryStore(file).days.value)
            store.replace(listOf(GarminDay(LocalDate.of(2026, 1, 1), 100)))
            assertEquals(1, GarminHistoryStore(file).days.value.size)
            val old = store.delete()
            assertTrue(GarminHistoryStore(file).days.value.isEmpty())
            store.restore(old)
            assertEquals(old, GarminHistoryStore(file).days.value)
            assertEquals(GarminSummary(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 1), 1, kcal = false), GarminSummary.of(old))
            assertTrue(GarminSummary.of(days)!!.kcal)
            assertNull(GarminSummary.of(emptyList()))
        } finally {
            file.delete()
        }
    }

    @Test
    fun `an old steps-only file is read until the next import replaces it`() {
        val dirFile = java.nio.file.Files.createTempDirectory("garmin").toFile()
        try {
            val oldFile = File(dirFile, "garmin-steps.json").apply { writeText("""[{"day":"2025-01-02","steps":5000},{"day":"2025-01-03","steps":4000}]""") }
            val newFile = File(dirFile, "garmin-history.json")
            val store = GarminHistoryStore(newFile, oldFile)
            assertEquals(listOf(GarminDay(LocalDate.of(2025, 1, 2), 5000), GarminDay(LocalDate.of(2025, 1, 3), 4000)), store.days.value)
            assertFalse(GarminSummary.of(store.days.value)!!.kcal)
            store.replace(listOf(GarminDay(LocalDate.of(2025, 2, 1), 8000, 450, 1850)))
            assertFalse(oldFile.exists())
            assertEquals(listOf(GarminDay(LocalDate.of(2025, 2, 1), 8000, 450, 1850)), GarminHistoryStore(newFile, oldFile).days.value)
        } finally {
            dirFile.deleteRecursively()
        }
    }

    // "Imported Garmin days in the totals"

    private val garminSource = StepsSourceState(setOf(GarminMerge.GARMIN), "garmin", StepsSourceLabel.App(GarminMerge.GARMIN, false))
    private val allSource = StepsSourceState(null, "all", StepsSourceLabel.All)
    private val phoneSource = StepsSourceState(setOf("android"), "phone", StepsSourceLabel.App("phone", false))
    private val imported = listOf(StepsDay(LocalDate.of(2023, 7, 18), 8_000), StepsDay(LocalDate.of(2026, 10, 3), 7_400), StepsDay(LocalDate.of(2026, 11, 8), 9_000))
    private val hc = listOf(StepsDay(LocalDate.of(2026, 8, 15), 5_000), StepsDay(LocalDate.of(2026, 11, 8), 4_000), StepsDay(today, 1_200))

    @Test
    fun `before Health Connect the import fills, Kaikki starts at its first day`() {
        val merged = GarminMerge.merge(hc, imported, garminSource, today)
        assertEquals(LocalDate.of(2023, 7, 18), merged.first().day)
        assertEquals(5_000, merged.first { it.day == LocalDate.of(2026, 8, 15) }.steps)
        val series = StepsSeries.of(merged, fi.jukkakot.terveyslappu.chart.Period.All, today)
        assertEquals(LocalDate.of(2023, 7, 18), series.from)
    }

    @Test
    fun `other source ignores the import`() {
        assertEquals(hc, GarminMerge.merge(hc, imported, phoneSource, today))
    }

    @Test
    fun `gap in Health Connect and the larger count on a shared day`() {
        val merged = GarminMerge.merge(hc, imported, allSource, today)
        assertEquals(7_400, merged.first { it.day == LocalDate.of(2026, 10, 3) }.steps)
        assertEquals(9_000, merged.first { it.day == LocalDate.of(2026, 11, 8) }.steps)
    }

    @Test
    fun `today never comes from the import`() {
        val merged = GarminMerge.merge(emptyList(), listOf(StepsDay(today, 5_000), StepsDay(today.plusDays(1), 1)), garminSource, today)
        assertTrue(merged.isEmpty())
        assertEquals(1_200, GarminMerge.merge(hc, listOf(StepsDay(today, 5_000)), garminSource, today).first { it.day == today }.steps)
    }
}
