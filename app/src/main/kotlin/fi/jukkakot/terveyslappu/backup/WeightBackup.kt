package fi.jukkakot.terveyslappu.backup

import fi.jukkakot.terveyslappu.log.AppLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

/** The backup's persisted state (weight-backup spec). [uri] null = the backup is off. */
data class BackupStatus(
    val uri: String? = null,
    val lastOkAt: Instant? = null,
    val lastCount: Int = 0,
    val lastFailedAt: Instant? = null,
    val running: Boolean = false,
) {
    val enabled: Boolean get() = uri != null

    /** The latest attempt failed. */
    val failed: Boolean get() = lastFailedAt != null && (lastOkAt == null || lastFailedAt.isAfter(lastOkAt))

    /** A backup should run on app open: on, and no success yet on [now]'s calendar day in [zone]. */
    fun isDue(now: Instant, zone: ZoneId): Boolean =
        enabled && (lastOkAt == null || lastOkAt.atZone(zone).toLocalDate() != now.atZone(zone).toLocalDate())

    /** The last success is old enough to warn about. */
    fun isStale(now: Instant): Boolean = enabled && lastOkAt != null && Duration.between(lastOkAt, now) >= STALE_AFTER

    /** On and failed or stale: the home screen shows its notice. */
    fun needsAttention(now: Instant): Boolean = enabled && (failed || isStale(now))

    companion object {
        val STALE_AFTER: Duration = Duration.ofDays(3)
    }
}

/** Where the status is kept between app starts (SharedPreferences on the phone, a fake in tests). */
interface BackupStore {
    fun load(): BackupStatus
    fun save(status: BackupStatus)
}

/**
 * Rewrites the backup file after changes and when due (design decisions 1, 3, 4). Runs never
 * overlap: a request during a run makes one more run afterwards, so the file always ends up with
 * the latest weights. [write] writes all weights to the file and returns how many.
 */
class WeightBackup(
    private val store: BackupStore,
    private val write: suspend (uri: String) -> Int,
    private val now: () -> Instant,
    private val scope: CoroutineScope,
) {
    private val _status = MutableStateFlow(store.load().copy(running = false))
    val status: StateFlow<BackupStatus> = _status

    private val mutex = Mutex()

    @Volatile
    private var again = false

    fun enable(uri: String) {
        AppLog.i("backup.enable")
        set(BackupStatus(uri = uri))
        request("enable")
    }

    fun disable() {
        AppLog.i("backup.disable")
        set(BackupStatus())
    }

    /** Runs a backup in the background when the backup is on. */
    fun request(reason: String) {
        if (!_status.value.enabled) return
        scope.launch { run(reason) }
    }

    fun requestIfDue() {
        if (_status.value.isDue(now(), ZoneId.systemDefault())) request("due")
    }

    suspend fun run(reason: String) {
        if (!mutex.tryLock()) {
            again = true
            return
        }
        try {
            do {
                again = false
                once(reason)
            } while (again)
        } finally {
            mutex.unlock()
        }
    }

    private suspend fun once(reason: String) {
        val uri = _status.value.uri ?: return
        _status.update { it.copy(running = true) }
        val started = System.nanoTime()
        try {
            val count = write(uri)
            AppLog.i("backup.run", "ok", "reason" to reason, "count" to count, "ms" to (System.nanoTime() - started) / 1_000_000)
            // A disable or file change during the run wins over this result.
            if (_status.value.uri == uri) set(_status.value.copy(lastOkAt = now(), lastCount = count, running = false))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLog.e("backup.run", e, "failed", "reason" to reason)
            if (_status.value.uri == uri) set(_status.value.copy(lastFailedAt = now(), running = false))
        }
    }

    private fun set(status: BackupStatus) {
        _status.value = status
        store.save(status.copy(running = false))
    }
}
