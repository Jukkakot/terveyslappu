package fi.jukkakot.terveyslappu.steps

import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.File
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Made-up step counts only (NFR-1). */
class StepsReadTest {
    private val today = LocalDate.of(2026, 11, 14)

    private class FakeSource(
        val first: LocalDate?,
        val data: List<StepsDay> = emptyList(),
        /** daily() throws for a range starting before this day. */
        val failBefore: LocalDate? = null,
        val failAll: Boolean = false,
    ) : StepsSource {
        val asked = mutableListOf<Pair<LocalDate, LocalDate>>()
        override suspend fun granted() = true
        override suspend fun firstDay() = first
        override suspend fun daily(from: LocalDate, to: LocalDate): List<StepsDay> {
            asked += from to to
            if (failAll) throw IllegalStateException("refused")
            if (failBefore != null && from < failBefore) throw SecurityException("too old")
            return data.filter { it.day in from..to }
        }
    }

    private class Log {
        val lines = mutableListOf<Triple<Boolean, Throwable?, List<Pair<String, Any?>>>>()
        fun add(ok: Boolean, e: Throwable?, fields: List<Pair<String, Any?>>) {
            lines += Triple(ok, e, fields)
        }
    }

    @Test
    fun `access state mapping`() {
        assertEquals(StepsAccess.UNAVAILABLE, StepsRules.access(null, asked = false))
        assertEquals(StepsAccess.GRANTED, StepsRules.access(true, asked = true))
        assertEquals(StepsAccess.NEEDS, StepsRules.access(false, asked = false))
        assertEquals(StepsAccess.DENIED, StepsRules.access(false, asked = true))
    }

    @Test
    fun `read starts at the later of the period start and the first readable day`() {
        val first = today.minusDays(20)
        assertEquals(first, StepsRules.readStart(today.minusDays(89), first, today))
        assertEquals(today.minusDays(6), StepsRules.readStart(today.minusDays(6), first, today))
        assertEquals(first, StepsRules.readStart(null, first, today))
        assertNull(StepsRules.readStart(today.minusDays(6), null, today))
    }

    @Test
    fun `long ranges are read in chunks`() {
        val chunks = StepsRules.chunks(today.minusDays(800), today)
        assertEquals(3, chunks.size)
        assertEquals(today.minusDays(800), chunks.first().first)
        assertEquals(today, chunks.last().second)
        chunks.zipWithNext().forEach { (a, b) -> assertEquals(a.second.plusDays(1), b.first) }
    }

    @Test
    fun `a read returns the days and logs counts without step counts`() = runBlocking {
        val log = Log()
        val source = FakeSource(today.minusDays(20), listOf(StepsDay(today, 4321), StepsDay(today.minusDays(2), 8765), StepsDay(today.minusDays(1), 0)))
        val load = StepsReader(source, log::add).read(today.minusDays(6), today)
        assertIs<StepsLoad.Ok>(load)
        assertEquals(2, load.days.size)
        assertEquals(listOf(today.minusDays(6) to today), source.asked)
        val (ok, _, fields) = log.lines.single()
        assertTrue(ok)
        assertTrue(fields.contains("days" to 2))
        assertFalse(fields.any { it.second.toString() == "4321" || it.second.toString() == "8765" })
    }

    @Test
    fun `no readable steps at all reads nothing`() = runBlocking {
        val source = FakeSource(first = null)
        val load = StepsReader(source, Log()::add).read(null, today)
        assertEquals(StepsLoad.Ok(emptyList(), null), load)
        assertTrue(source.asked.isEmpty())
    }

    @Test
    fun `a failure at the first readable day retries from the day after and says so`() = runBlocking {
        val log = Log()
        val first = today.minusDays(40)
        val source = FakeSource(first, listOf(StepsDay(today.minusDays(3), 5000)), failBefore = first.plusDays(1))
        val load = StepsReader(source, log::add).read(null, today)
        assertIs<StepsLoad.Ok>(load)
        assertEquals(first.plusDays(1), load.from)
        assertEquals(2, log.lines.size)
        assertTrue(log.lines.last().third.contains("fallback" to "later-start"))
    }

    @Test
    fun `a failed read gives Failed and a log line with the cause`() = runBlocking {
        val log = Log()
        val load = StepsReader(FakeSource(today.minusDays(3), failAll = true), log::add).read(today.minusDays(6), today)
        assertIs<StepsLoad.Failed>(load)
        val (ok, error, fields) = log.lines.last()
        assertFalse(ok)
        assertTrue(error is IllegalStateException)
        assertTrue(fields.contains("cause" to "IllegalStateException"))
    }

    @Test
    fun `a first failure shows the message, a later one keeps the numbers as stale`() {
        val failed = StepsLoad.Failed(IllegalStateException())
        val first = StepsUiState(access = StepsAccess.GRANTED).after(failed)
        assertTrue(first.loadFailed)
        assertNull(first.days)
        val shown = StepsUiState(access = StepsAccess.GRANTED).after(StepsLoad.Ok(listOf(StepsDay(today, 100)), today))
        val later = shown.after(failed)
        assertFalse(later.loadFailed)
        assertTrue(later.stale)
        assertEquals(shown.days, later.days)
        assertFalse(later.after(StepsLoad.Ok(emptyList(), today)).stale)
    }

    @Test
    fun `the app never asks to write steps`() {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        assertTrue("android.permission.health.READ_STEPS" in manifest)
        assertFalse("android.permission.health.WRITE_STEPS" in manifest)
    }
}
