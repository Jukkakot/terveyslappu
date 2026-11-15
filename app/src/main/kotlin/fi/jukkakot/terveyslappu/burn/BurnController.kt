package fi.jukkakot.terveyslappu.burn

import fi.jukkakot.terveyslappu.steps.GarminDay
import fi.jukkakot.terveyslappu.steps.StepsAccess
import fi.jukkakot.terveyslappu.steps.StepsChoice
import fi.jukkakot.terveyslappu.steps.StepsLogger
import fi.jukkakot.terveyslappu.steps.StepsRules
import fi.jukkakot.terveyslappu.steps.StepsSourceRules
import fi.jukkakot.terveyslappu.steps.StepsSourceState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Which of the two read permissions are granted (energy-burn design 9: a partial grant is not granted). */
enum class BurnGrant(val log: String) {
    NONE("none"), TOTAL("total"), ACTIVE("active"), BOTH("both");

    companion object {
        fun of(total: Boolean, active: Boolean): BurnGrant = when {
            total && active -> BOTH
            total -> TOTAL
            active -> ACTIVE
            else -> NONE
        }
    }
}

/** One day as Health Connect gives it (null: no record of that type that day). */
data class BurnRaw(val day: LocalDate, val total: Long?, val active: Long?)

/** Where burned energy comes from: Health Connect in the app, a fake in tests. Read only. */
interface BurnSource {
    /** Throws when Health Connect cannot be used. */
    suspend fun granted(): BurnGrant

    /** The earlier of the first total and first active calories record of [packages] (null: any app). */
    suspend fun firstDay(packages: Set<String>?): LocalDate?

    /** Days from [from] to [to] on which an origin of [packages] (null: any app that wrote one) wrote a record. */
    suspend fun daily(from: LocalDate, to: LocalDate, packages: Set<String>?): List<BurnRaw>
}

/** The Kulutus state the screens show (as [fi.jukkakot.terveyslappu.steps.StepsUiState]). */
data class BurnUiState(
    val access: StepsAccess = StepsAccess.CHECKING,
    /** Days with burned energy (Health Connect merged with the import); null until read. */
    val days: List<BurnDay>? = null,
    val loadFailed: Boolean = false,
    /** The last read failed but earlier numbers are shown ("ei päivitetty"). */
    val stale: Boolean = false,
    /** The steps source in use (design 3) and its readable name (null: all apps). */
    val source: StepsSourceState = StepsSourceRules.resolve(StepsChoice.Auto, null, null, false),
    val sourceName: String? = null,
)

/**
 * Kulutus's access and reads (energy-burn design 3, 8, 9, 13), mirroring the steps controller: the
 * source is the resolved steps source, read in chunks from the first readable day, merged with the
 * imported Garmin kcal. Android-free. Logs day counts and timings only, never kcal.
 */
class BurnController(
    private val source: BurnSource,
    private val scope: CoroutineScope,
    private val log: StepsLogger,
    private val stepsSource: () -> StepsSourceState,
    private val names: (String) -> String = { it },
    private val today: () -> LocalDate = LocalDate::now,
    private val nanos: () -> Long = System::nanoTime,
    private val garmin: () -> List<GarminDay> = { emptyList() },
) {
    private val _state = MutableStateFlow(BurnUiState(source = stepsSource()))
    val state: StateFlow<BurnUiState> = _state

    /** Remembered only while the app runs (as steps). */
    var permissionAsked = false
        private set

    private var widest: LocalDate? = null
    private var all = false
    private var generation = 0
    private val reads = Mutex()
    private val readJobs = SupervisorJob(scope.coroutineContext[Job])

    /** The last Health Connect read and its source, for re-merging the import. */
    private var lastHc: Pair<List<BurnDay>, StepsSourceState>? = null

    fun show(from: LocalDate?) {
        if (from == null) all = true else widest = widest?.let { minOf(it, from) } ?: from
        refresh()
    }

    fun refresh() {
        scope.launch {
            val grant = runCatching { source.granted() }
                .onFailure { log.line(false, "hc.permission.burn", "check", it, emptyList()) }
                .getOrNull()
            val access = StepsRules.access(grant?.let { it == BurnGrant.BOTH }, permissionAsked)
            _state.update { it.copy(access = access) }
            if (access == StepsAccess.GRANTED && (all || widest != null)) read()
        }
    }

    fun onPermissionRequested() {
        log.line(true, "hc.permission.burn", "request", null, emptyList())
    }

    fun onPermissionResult(grant: BurnGrant) {
        permissionAsked = true
        log.line(true, "hc.permission.burn", if (grant == BurnGrant.BOTH) "granted" else "denied", null, listOf("granted" to grant.log))
        refresh()
    }

    /** The steps source changed: a read of the old source is dropped and the range is read again. */
    fun sourceChanged() {
        val src = stepsSource()
        if (src.key == _state.value.source.key && src == _state.value.source) return
        val keyChanged = src.key != _state.value.source.key
        _state.update { it.copy(source = src, sourceName = src.appId?.let(names)) }
        if (!keyChanged) return
        generation++
        readJobs.cancelChildren()
        lastHc = null
        _state.update { it.copy(days = null, loadFailed = false, stale = false) }
        if (_state.value.access == StepsAccess.GRANTED && (all || widest != null)) scope.launch { read() }
    }

    /** The import was replaced, deleted or restored: re-merge onto the last read. */
    fun garminChanged() {
        val (hc, src) = lastHc ?: return
        if (stepsSource().key != src.key) return
        _state.update { it.copy(days = BurnMerge.merge(hc, garmin(), src, today()).days) }
    }

    private suspend fun read() {
        scope.launch(readJobs) {
            reads.withLock {
                val src = stepsSource()
                val gen = generation
                val started = nanos()
                val load = readHc(if (all) null else widest, today(), src)
                val ms = (nanos() - started) / 1_000_000
                if (gen != generation || stepsSource().key != src.key) return@withLock
                when (load) {
                    is Load.Ok -> {
                        lastHc = load.days to src
                        val merged = BurnMerge.merge(load.days, garmin(), src, today())
                        log.line(
                            true, "burn.read", "ok", null,
                            listOf(
                                "days" to load.days.size,
                                "split" to load.days.count { it.kind == BurnKind.SPLIT },
                                "totalOnly" to load.days.count { it.kind == BurnKind.TOTAL },
                                "activeOnly" to load.days.count { it.kind == BurnKind.ACTIVE },
                                "imported" to merged.imported,
                                "invalid" to load.invalid,
                                "back" to (load.from?.let { ChronoUnit.DAYS.between(it, today()) } ?: "none"),
                                "fallback" to load.fallback,
                                "source" to src.logName,
                                "ms" to ms,
                            ),
                        )
                        _state.update { it.copy(days = merged.days, loadFailed = false, stale = false, source = src, sourceName = src.appId?.let(names)) }
                    }
                    is Load.Failed -> {
                        log.line(false, "burn.read", "failed", load.cause, listOf("cause" to load.cause.javaClass.simpleName, "source" to src.logName, "ms" to ms))
                        _state.update {
                            val s = if (it.days == null) it.copy(loadFailed = true, stale = false) else it.copy(stale = true)
                            s.copy(source = src, sourceName = src.appId?.let(names))
                        }
                    }
                }
            }
        }.join()
    }

    private sealed interface Load {
        data class Ok(val days: List<BurnDay>, val from: LocalDate?, val invalid: Int, val fallback: String) : Load
        data class Failed(val cause: Throwable) : Load
    }

    /** As the steps read (design 8): the first readable day, chunks, one retry from the next day. */
    private suspend fun readHc(wanted: LocalDate?, today: LocalDate, src: StepsSourceState): Load = try {
        val packages = src.packages
        val first = source.firstDay(packages)
        val start = StepsRules.readStart(wanted, first, today)
        if (start == null) {
            Load.Ok(emptyList(), null, 0, "none")
        } else {
            var fallback = "none"
            var from: LocalDate = start
            val raw = try {
                readAll(start, today, packages)
            } catch (e: Exception) {
                if (e is CancellationException || first == null || start != first || start >= today) throw e
                fallback = "later-start"
                from = start.plusDays(1)
                log.line(false, "burn.read", "failed", e, listOf("cause" to e.javaClass.simpleName, "retry" to true))
                readAll(from, today, packages)
            }
            val invalid = raw.count { BurnDay.invalid(it.total, it.active) }
            Load.Ok(raw.mapNotNull { BurnDay.of(it.day, it.total, it.active) }.sortedBy { it.day }, from, invalid, fallback)
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Load.Failed(e)
    }

    private suspend fun readAll(from: LocalDate, to: LocalDate, packages: Set<String>?): List<BurnRaw> =
        StepsRules.chunks(from, to).flatMap { (a, b) -> source.daily(a, b, packages) }.distinctBy { it.day }
}
