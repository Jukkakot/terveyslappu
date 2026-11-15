package fi.jukkakot.terveyslappu.steps

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.time.LocalDate

/** One log line: [ok] false = an error line with [error]. */
fun interface StepsLogger {
    fun line(ok: Boolean, event: String, message: String, error: Throwable?, fields: List<Pair<String, Any?>>)
}

/** One app in the source list (steps-source-and-axes design 7). */
data class StepsSourceRow(val id: String, val name: String, val total: Long, val average: Long?, val lastSeen: LocalDate?) {
    val noRecent: Boolean get() = total <= 0
}

/** The source sub-screen's state. */
data class StepsSourcesUi(
    val choice: StepsChoice = StepsChoice.Auto,
    val source: StepsSourceState = StepsSourceRules.resolve(StepsChoice.Auto, null, null, false),
    /** Readable name of the app in use (null: all apps). */
    val sourceName: String? = null,
    /** The list; empty until a check finished in this run. */
    val rows: List<StepsSourceRow> = emptyList(),
    val checking: Boolean = false,
    /** The last check failed: the list falls back to the failure line. */
    val failed: Boolean = false,
    /** A check finished successfully in this run (the list is known). */
    val checked: Boolean = false,
)

/**
 * The steps state shared by the front page card, Askeleet and Asetukset → Askeleet (steps design 5,
 * steps-source-and-axes design 4, 5, 10): access, the widest range shown, the source in use, the
 * once-a-day source check and the reads. Android-free; [StepsViewModel] wires Health Connect,
 * the prefs and the log. Logs counts, timings and package names only, never step counts.
 */
class StepsController(
    private val source: StepsSource,
    private val store: StepsStore,
    private val scope: CoroutineScope,
    private val log: StepsLogger,
    private val names: (String) -> String = { it },
    private val now: () -> Instant = Instant::now,
    private val today: () -> LocalDate = LocalDate::now,
    private val nanos: () -> Long = System::nanoTime,
) {
    private val reader = StepsReader(source, log = { ok, error, fields -> log.line(ok, "steps.read", if (ok) "ok" else "failed", error, fields) }, nanos)

    private val _state = MutableStateFlow(StepsUiState())
    val state: StateFlow<StepsUiState> = _state

    private val _sources = MutableStateFlow(StepsSourcesUi())
    val sources: StateFlow<StepsSourcesUi> = _sources

    /** Remembered only while the app runs (steps "Denied steps permission"). */
    var permissionAsked = false
        private set

    private var widest: LocalDate? = null
    private var all = false

    private val reads = Mutex()
    private val readJobs = SupervisorJob(scope.coroutineContext[Job])

    /** The latest successful check's totals in this run; the day it succeeded. */
    private var apps: List<StepsAppTotal>? = null
    private var okDay: LocalDate? = null
    private var lastFailureAt: Instant? = null
    private var lastFinishedAt: Instant? = null
    private var checkJob: Deferred<Boolean>? = null
    private var generation = 0
    private var subScreenVisible = false
    private var lastLogged: String? = null

    init {
        publish("start")
    }

    /** The source in use now. */
    fun current(): StepsSourceState =
        StepsSourceRules.resolve(store.choice, store.autoPick, apps, okDay == today(), store.phonePackages)

    /** A screen shows steps from [from] (null: Kaikki); widens the read range and reads. */
    fun show(from: LocalDate?) {
        if (from == null) all = true else widest = widest?.let { minOf(it, from) } ?: from
        refresh()
    }

    /** On every resume: the permission may have changed; reads the range shown so far (a foreground read). */
    fun refresh() {
        scope.launch {
            val granted = runCatching { source.granted() }
                .onFailure { log.line(false, "hc.permission.steps", "check", it, emptyList()) }
                .getOrNull()
            val access = StepsRules.access(granted, permissionAsked)
            val before = _state.value.access
            _state.update { it.copy(access = access) }
            if (access != StepsAccess.GRANTED) return@launch
            if (before != StepsAccess.GRANTED && subScreenVisible) scope.launch { subScreenCheck("granted") }
            if (all || widest != null) load()
        }
    }

    fun onPermissionRequested() {
        log.line(true, "hc.permission.steps", "request", null, emptyList())
    }

    fun onPermissionResult(granted: Boolean) {
        permissionAsked = true
        log.line(true, "hc.permission.steps", if (granted) "granted" else "denied", null, emptyList())
        refresh()
    }

    /** Asetukset → Askeleet was entered (not a resume): check unless the last check finished under a minute ago. */
    fun subScreenShown() {
        subScreenVisible = true
        if (_state.value.access == StepsAccess.GRANTED) scope.launch { subScreenCheck("settings") }
    }

    fun subScreenHidden() {
        subScreenVisible = false
    }

    /** The user picks a source: stored, applied at once; a running read is cancelled and its result dropped. */
    fun setChoice(choice: StepsChoice) {
        if (choice == store.choice) return
        store.choice = choice
        generation++
        readJobs.cancelChildren()
        _state.update { it.copy(days = null, loadFailed = false, stale = false) }
        publish("user")
        if (_state.value.access == StepsAccess.GRANTED && (all || widest != null)) scope.launch { read() }
    }

    private suspend fun subScreenCheck(reason: String) {
        if (checkJob?.isActive != true && !StepsCheckRule.subScreenAllowed(lastFinishedAt, now())) return
        val key = current().key
        val gen = generation
        runCheck(reason)
        if (gen == generation && current().key != key && (all || widest != null)) read()
    }

    private suspend fun load() {
        val due = StepsCheckRule.due(today(), store.autoPickDay, lastFailureAt, now(), foreground = true)
        val retry = lastFailureAt != null
        val first = due && store.choice == StepsChoice.Auto && store.autoPick == null
        if (first) runCheck(if (retry) "retry" else "start")
        read()
        if (due && !first) {
            val key = current().key
            val gen = generation
            runCheck(if (retry) "retry" else "daily")
            if (gen == generation && current().key != key) read()
        }
    }

    /** Reads the range shown from the current source; a result whose source is no longer current is dropped. */
    private suspend fun read() {
        scope.launch(readJobs) {
            reads.withLock {
                val src = current()
                val gen = generation
                val load = reader.read(if (all) null else widest, today(), src.packages, src.logName)
                if (gen == generation && current().key == src.key) _state.update { it.after(load).copy(source = src, sourceName = src.appId?.let(names)) }
            }
        }.join()
    }

    /** At most one check at a time: a request while one runs waits for that one's result. */
    private suspend fun runCheck(reason: String): Boolean {
        checkJob?.takeIf { it.isActive }?.let { return it.await() }
        val job = scope.async { check(reason) }
        checkJob = job
        return job.await()
    }

    private suspend fun check(reason: String): Boolean {
        val started = nanos()
        fun ms() = (nanos() - started) / 1_000_000
        _sources.update { it.copy(checking = true) }
        return try {
            val found = runCatching { source.phonePackages() }.getOrDefault(setOf(StepsSourceRules.ANDROID))
            if (found.size > 1) store.phonePackages = found - StepsSourceRules.ANDROID
            val saved = store.phonePackages
            val candidates = buildList {
                add(StepsApp(StepsSourceRules.PHONE, setOf(StepsSourceRules.ANDROID) + saved))
                listOfNotNull((store.choice as? StepsChoice.App)?.id, store.autoPick)
                    .filter { it != StepsSourceRules.PHONE }
                    .forEach { add(StepsApp(it, StepsSourceRules.packagesOf(it, apps, saved))) }
            }
            val result = source.check(today(), candidates)
            val day = today()
            apps = result.apps
            okDay = day
            store.autoPickDay = day
            result.apps.firstOrNull { it.app.id == StepsSourceRules.PHONE && it.app.packages.size > 1 }?.let { store.phonePackages = it.app.packages - StepsSourceRules.ANDROID }
            val recent = result.apps.filter { it.total > 0 }
            val keep = setOfNotNull((store.choice as? StepsChoice.App)?.id, store.autoPick)
            store.seen = store.seen.filterKeys { it in keep } + recent.associate { it.app.id to day }
            val old = store.autoPick
            val pick = StepsSourceRules.pick(result.apps, old)
            store.autoPick = pick
            if (pick != old) log.line(true, "steps.source", "switch", null, listOf("from" to (old ?: "none"), "to" to (pick ?: "none")))
            lastFailureAt = null
            log.line(true, "steps.sources", "ok", null, listOf("apps" to recent.size, "records" to result.records, "pages" to result.pages, "reason" to reason, "ms" to ms()))
            _sources.update { it.copy(failed = false, checked = true) }
            publish(reason)
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            lastFailureAt = now()
            log.line(false, "steps.sources", "failed", e, listOf("cause" to e.javaClass.simpleName, "reason" to reason, "ms" to ms()))
            _sources.update { it.copy(failed = true) }
            false
        } finally {
            lastFinishedAt = now()
            _sources.update { it.copy(checking = false) }
        }
    }

    /** Pushes the source to both states; logs a change of the choice or the pick. */
    private fun publish(reason: String) {
        val src = current()
        val choice = store.choice
        val pick = store.autoPick
        val logged = "${choice.encode()}|${pick ?: "none"}"
        if (logged != lastLogged) {
            lastLogged = logged
            val kind = when (choice) {
                StepsChoice.Auto -> "auto"
                StepsChoice.All -> "all"
                is StepsChoice.App -> "app"
            }
            val fields = buildList {
                add("choice" to kind)
                if (choice is StepsChoice.App) add("app" to choice.id)
                add("pick" to (pick ?: "none"))
                add("reason" to reason)
            }
            log.line(true, "steps.source", "", null, fields)
        }
        val seen = store.seen
        val rows = apps?.let { list ->
            StepsSourceRules.listed(list, choice).map { StepsSourceRow(it.app.id, names(it.app.id), it.total, it.averageExToday, seen[it.app.id]) }
        }.orEmpty()
        val name = src.appId?.let(names)
        _sources.update { it.copy(choice = choice, source = src, sourceName = name, rows = rows) }
        _state.update { it.copy(source = src, sourceName = name) }
    }
}
