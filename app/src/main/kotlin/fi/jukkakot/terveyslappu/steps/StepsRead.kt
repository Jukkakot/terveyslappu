package fi.jukkakot.terveyslappu.steps

import java.time.LocalDate

/** Whether steps may be read (steps "Steps permission", design 5). */
enum class StepsAccess { CHECKING, UNAVAILABLE, NEEDS, DENIED, GRANTED }

/** Where steps come from: Health Connect in the app, a fake in tests. Read only (steps design 1). */
interface StepsSource {
    /** Whether the read permission is granted; throws when Health Connect cannot be used. */
    suspend fun granted(): Boolean

    /** The first day with a readable steps record of [packages] (null: any app). */
    suspend fun firstDay(packages: Set<String>?): LocalDate?

    /** Daily totals of [packages] (null: Health Connect's merge) from [from] to [to]; a day without data is absent. */
    suspend fun daily(from: LocalDate, to: LocalDate, packages: Set<String>?): List<StepsDay>

    /** The 30-day source check (steps-source-and-axes design 2); throws when any read fails. */
    suspend fun check(today: LocalDate, candidates: List<StepsApp>): StepsCheck

    /** `android` plus the phone's synthetic package name when the system tells it. */
    suspend fun phonePackages(): Set<String>
}

/** A source check's result: every app's 30-day figures, and the record count and pages read (for the log). */
data class StepsCheck(val apps: List<StepsAppTotal>, val records: Int = 0, val pages: Int = 0)

object StepsRules {
    /** The access state from the permission check ([granted] null: the check failed). */
    fun access(granted: Boolean?, asked: Boolean): StepsAccess = when {
        granted == null -> StepsAccess.UNAVAILABLE
        granted -> StepsAccess.GRANTED
        asked -> StepsAccess.DENIED
        else -> StepsAccess.NEEDS
    }

    /** The daily read starts at the later of the wanted start (null: Kaikki) and the first readable day. */
    fun readStart(wanted: LocalDate?, firstDay: LocalDate?, today: LocalDate): LocalDate? {
        val first = firstDay ?: return null
        return minOf(today, if (wanted == null) first else maxOf(wanted, first))
    }

    /** Request ranges of at most [CHUNK_DAYS] days from [from] to [to]. */
    fun chunks(from: LocalDate, to: LocalDate): List<Pair<LocalDate, LocalDate>> = buildList {
        var start = from
        while (start <= to) {
            val end = minOf(to, start.plusDays(CHUNK_DAYS - 1))
            add(start to end)
            start = end.plusDays(1)
        }
    }

    const val CHUNK_DAYS = 366L
}

/** The result of one read. */
sealed interface StepsLoad {
    /** [from] = the first day read (null: no steps at all). */
    data class Ok(val days: List<StepsDay>, val from: LocalDate?) : StepsLoad

    data class Failed(val cause: Throwable) : StepsLoad
}

/** The steps state the screens show (design 5). */
data class StepsUiState(
    val access: StepsAccess = StepsAccess.CHECKING,
    /** Days with steps from [readFrom] to today; null until read. */
    val days: List<StepsDay>? = null,
    /** A read failed before anything was read: the screens say so instead of numbers. */
    val loadFailed: Boolean = false,
    /** The last read failed but earlier numbers are shown ("ei päivitetty"). */
    val stale: Boolean = false,
    /** The source in use (steps-source-and-axes design 5) and its readable name (null: all apps). */
    val source: StepsSourceState = StepsSourceRules.resolve(StepsChoice.Auto, null, null, false),
    val sourceName: String? = null,
) {
    fun after(load: StepsLoad): StepsUiState = when (load) {
        is StepsLoad.Ok -> copy(days = load.days, loadFailed = false, stale = false)
        is StepsLoad.Failed -> if (days == null) copy(loadFailed = true, stale = false) else copy(stale = true)
    }
}

/**
 * One read of the shown range (steps "Old steps need the history permission", "Refresh and failed
 * reads", "Steps in the app log"): the first readable day, then the daily totals in chunks. Whether
 * Health Connect throws or clamps for a start past the readable history is not known (task 2.1b):
 * a failed chunk read is retried once from the day after the first readable day (its midnight can
 * lie a few hours before the first readable record), and the log says which happened
 * (`fallback=none|later-start`) and which source was read (`source=all|phone|<package>`; one source
 * for the whole range, no per-day fallback to another app). Logs counts and timings only, never step counts.
 */
class StepsReader(
    private val source: StepsSource,
    private val log: (ok: Boolean, error: Throwable?, fields: List<Pair<String, Any?>>) -> Unit,
    private val nanos: () -> Long = System::nanoTime,
) {
    suspend fun read(wanted: LocalDate?, today: LocalDate, packages: Set<String>? = null, sourceName: String = "all"): StepsLoad {
        val started = nanos()
        fun ms() = (nanos() - started) / 1_000_000
        return try {
            val first = source.firstDay(packages)
            val start = StepsRules.readStart(wanted, first, today)
            if (start == null) {
                log(true, null, listOf("days" to 0, "first" to "none", "source" to sourceName, "ms" to ms()))
                return StepsLoad.Ok(emptyList(), null)
            }
            var fallback = "none"
            var from: LocalDate = start
            val days = try {
                readAll(start, today, packages)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException || first == null || start != first || start >= today) throw e
                fallback = "later-start"
                from = start.plusDays(1)
                log(false, e, listOf("cause" to e.javaClass.simpleName, "retry" to true, "ms" to ms()))
                readAll(from, today, packages)
            }
            log(true, null, listOf("days" to days.size, "back" to java.time.temporal.ChronoUnit.DAYS.between(from, today), "fallback" to fallback, "source" to sourceName, "ms" to ms()))
            StepsLoad.Ok(days, from)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            log(false, e, listOf("cause" to e.javaClass.simpleName, "source" to sourceName, "ms" to ms()))
            StepsLoad.Failed(e)
        }
    }

    private suspend fun readAll(from: LocalDate, to: LocalDate, packages: Set<String>?): List<StepsDay> =
        StepsRules.chunks(from, to).flatMap { (a, b) -> source.daily(a, b, packages) }.filter { it.steps > 0 }.sortedBy { it.day }
}
