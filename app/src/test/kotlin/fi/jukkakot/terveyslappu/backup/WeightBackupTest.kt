package fi.jukkakot.terveyslappu.backup

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException
import java.time.Duration
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WeightBackupTest {
    private val t0 = Instant.parse("2026-11-08T20:00:00Z")

    private class MemoryStore(var saved: BackupStatus = BackupStatus()) : BackupStore {
        override fun load() = saved
        override fun save(status: BackupStatus) {
            saved = status
        }
    }

    @Test
    fun `due on the first open of a new day, stale after three days`() {
        val zone = java.time.ZoneId.of("Europe/Helsinki")
        // Last success yesterday 23:00 local (21:00Z); same-day opens are not due, next morning is.
        val s = BackupStatus(uri = "content://x", lastOkAt = Instant.parse("2026-11-08T21:00:00Z"))
        assertFalse(s.isDue(Instant.parse("2026-11-08T21:50:00Z"), zone))
        assertTrue(s.isDue(Instant.parse("2026-11-09T05:00:00Z"), zone))
        val today = BackupStatus(uri = "content://x", lastOkAt = Instant.parse("2026-11-09T05:00:00Z"))
        assertFalse(today.isDue(Instant.parse("2026-11-09T10:00:00Z"), zone))
        assertFalse(s.isStale(t0.plus(Duration.ofDays(2))))
        assertTrue(s.isStale(t0.plus(Duration.ofDays(4))))
        assertFalse(BackupStatus().isDue(t0, zone))
    }

    @Test
    fun `failure after success is shown as failed`() {
        assertTrue(BackupStatus(uri = "u", lastOkAt = t0, lastFailedAt = t0.plusSeconds(5)).failed)
        assertFalse(BackupStatus(uri = "u", lastOkAt = t0.plusSeconds(5), lastFailedAt = t0).failed)
    }

    @Test
    fun `home notice on failure or a stale success, never when off`() {
        assertTrue(BackupStatus(uri = "u", lastOkAt = t0, lastFailedAt = t0.plusSeconds(5)).needsAttention(t0.plusSeconds(10)))
        assertTrue(BackupStatus(uri = "u", lastOkAt = t0).needsAttention(t0.plus(Duration.ofDays(4))))
        assertFalse(BackupStatus(uri = "u", lastOkAt = t0).needsAttention(t0.plus(Duration.ofDays(1))))
        assertFalse(BackupStatus(lastFailedAt = t0).needsAttention(t0))
    }

    @Test
    fun `enable writes at once and persists the result`() = runTest {
        val store = MemoryStore()
        val backup = WeightBackup(store, { _, _ -> 42 }, { t0 }, backgroundScope)
        backup.enable("content://drive/backup.csv")
        runCurrent()
        assertEquals(BackupStatus("content://drive/backup.csv", lastOkAt = t0, lastCount = 42), store.saved)
    }

    @Test
    fun `requests during a run make exactly one more run`() = runTest {
        val gate = CompletableDeferred<Unit>()
        var runs = 0
        val backup = WeightBackup(MemoryStore(BackupStatus(uri = "u")), { _, _ ->
            runs++
            if (runs == 1) gate.await()
            runs
        }, { t0 }, backgroundScope)
        val first = backgroundScope.launch { backup.run("save") }
        runCurrent()
        backup.run("save")
        backup.run("delete")
        gate.complete(Unit)
        first.join()
        assertEquals(2, runs)
    }

    /** A fake write that reads [count] weights, asks [accept] like the real one and records whether it wrote. */
    private class FakeWrite(var count: Int) {
        var wrote = 0
        val write: suspend (String, (Int) -> Boolean) -> Int? = { _, accept ->
            if (accept(count)) {
                wrote++
                count
            } else {
                null
            }
        }
    }

    @Test
    fun `an automatic write that halves the count is refused, a manual one is not`() = runTest {
        val fake = FakeWrite(count = 20)
        val store = MemoryStore(BackupStatus(uri = "u", lastOkAt = t0, lastCount = 100))
        val backup = WeightBackup(store, fake.write, { t0.plusSeconds(60) }, backgroundScope, recentOnly = { true })
        backup.run("save")
        assertEquals(0, fake.wrote)
        assertTrue(backup.status.value.failed)
        assertTrue(backup.status.value.refused)
        assertTrue(backup.status.value.recentOnly)
        assertEquals(100, backup.status.value.lastCount)
        assertTrue(store.saved.refused)

        fake.count = 50 // exactly half is fine
        backup.run("save")
        assertEquals(1, fake.wrote)
        assertFalse(backup.status.value.failed)
        assertFalse(backup.status.value.refused)
        assertEquals(50, backup.status.value.lastCount)

        fake.count = 5
        backup.run("manual")
        assertEquals(2, fake.wrote)
        assertEquals(5, backup.status.value.lastCount)
    }

    @Test
    fun `recent only follows the history permission and the first backup is never refused`() = runTest {
        val fake = FakeWrite(count = 3)
        var recent = true
        val backup = WeightBackup(MemoryStore(BackupStatus(uri = "u")), fake.write, { t0 }, backgroundScope, recentOnly = { recent })
        backup.run("enable")
        assertEquals(1, fake.wrote)
        assertTrue(backup.status.value.recentOnly)
        recent = false
        backup.run("save")
        assertFalse(backup.status.value.recentOnly)
    }

    @Test
    fun `failure is recorded and off writes nothing`() = runTest {
        val store = MemoryStore(BackupStatus(uri = "u", lastOkAt = t0))
        val backup = WeightBackup(store, { _, _ -> throw IOException("made-up") }, { t0.plusSeconds(60) }, backgroundScope)
        backup.run("save")
        assertTrue(backup.status.value.failed)
        backup.disable()
        var wrote = false
        WeightBackup(MemoryStore(), { _, _ -> wrote = true; 1 }, { t0 }, backgroundScope).run("save")
        assertFalse(wrote)
        assertEquals(BackupStatus(), store.saved)
    }
}
