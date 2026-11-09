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
    fun `due after a day, stale after three`() {
        val s = BackupStatus(uri = "content://x", lastOkAt = t0)
        assertFalse(s.isDue(t0.plus(Duration.ofHours(23))))
        assertTrue(s.isDue(t0.plus(Duration.ofHours(25))))
        assertFalse(s.isStale(t0.plus(Duration.ofDays(2))))
        assertTrue(s.isStale(t0.plus(Duration.ofDays(4))))
        assertFalse(BackupStatus().isDue(t0))
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
        val backup = WeightBackup(store, { 42 }, { t0 }, backgroundScope)
        backup.enable("content://drive/backup.csv")
        runCurrent()
        assertEquals(BackupStatus("content://drive/backup.csv", lastOkAt = t0, lastCount = 42), store.saved)
    }

    @Test
    fun `requests during a run make exactly one more run`() = runTest {
        val gate = CompletableDeferred<Unit>()
        var runs = 0
        val backup = WeightBackup(MemoryStore(BackupStatus(uri = "u")), {
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

    @Test
    fun `failure is recorded and off writes nothing`() = runTest {
        val store = MemoryStore(BackupStatus(uri = "u", lastOkAt = t0))
        val backup = WeightBackup(store, { throw IOException("made-up") }, { t0.plusSeconds(60) }, backgroundScope)
        backup.run("save")
        assertTrue(backup.status.value.failed)
        backup.disable()
        var wrote = false
        WeightBackup(MemoryStore(), { wrote = true; 1 }, { t0 }, backgroundScope).run("save")
        assertFalse(wrote)
        assertEquals(BackupStatus(), store.saved)
    }
}
