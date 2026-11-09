package fi.jukkakot.terveyslappu.scale

import fi.jukkakot.terveyslappu.log.AppLog
import fi.jukkakot.terveyslappu.raw.MemoryRawStore
import fi.jukkakot.terveyslappu.raw.RawEntry
import fi.jukkakot.terveyslappu.raw.RawStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Instant

/** Difference to the latest earlier weight and that weight's time (weigh-in-feedback). */
data class WeightChange(val deltaKg: Double, val previousTime: Instant)

/** What the weigh-in dialog shows. */
sealed interface WeighInState {
    /** Checking access (Health Connect, nearby devices, Bluetooth) before scanning. */
    data object Preparing : WeighInState
    data object NoHealthAccess : WeighInState
    data object PermissionDenied : WeighInState
    data object BluetoothOff : WeighInState
    /** Scanning, no scale packet yet: "step on the scale". */
    data object Scanning : WeighInState
    data class Live(val kg: Double, val stable: Boolean) : WeighInState
    data class Saving(val kg: Double) : WeighInState
    /** [deleteFailed]: Poista could not delete this weight. */
    data class Saved(val kg: Double, val change: WeightChange? = null, val deleteFailed: Boolean = false) : WeighInState
    data class SaveFailed(val kg: Double) : WeighInState
    /** Nothing decided within the time limit; [heard] tells whether any scale packet arrived. */
    data class NotFound(val heard: Boolean) : WeighInState
    data class ScanFailed(val code: Int) : WeighInState
    /** Poista deleted the saved weight; the dialog closes. */
    data object Discarded : WeighInState
}

/**
 * One weigh-in: scan, decide the stable weight, save it once (scale-weigh-in spec). Android-free
 * so it runs in JVM tests with a fake [source], [save] and virtual time.
 *
 * @param elapsedMs monotonic clock in ms (the ticks are measured with it)
 * @param now wall clock, for the time stamped on the saved weight
 * @param save writes kg at a time to Health Connect
 * @param previous the latest weight before a time (kg, time), for the change shown after saving
 * @param delete removes the weight saved at a time (Poista)
 * @param archive keeps the deciding packet's raw fields with the saved weight (dropped with it)
 * @param last the previous weigh-in's company id, whose lingering broadcast is ignored for [IGNORE_MS]
 */
class WeighIn(
    private val source: PacketSource,
    private val save: suspend (kg: Double, time: Instant) -> Unit,
    private val elapsedMs: () -> Long,
    private val now: () -> Instant,
    private val timeoutMs: Long = TIMEOUT_MS,
    private val tickMs: Long = TICK_MS,
    private val previous: suspend (before: Instant) -> Pair<Double, Instant>? = { null },
    private val delete: suspend (kg: Double, time: Instant) -> Unit = { _, _ -> },
    private val archive: RawStore = MemoryRawStore(),
    private val last: LastWeighInStore = MemoryLastWeighIn(),
) {
    private val openedMs = elapsedMs()
    private val _state = MutableStateFlow<WeighInState>(WeighInState.Preparing)
    val state: StateFlow<WeighInState> = _state

    private var job: Job? = null
    private var decided: Pair<Double, Instant>? = null
    private var decidedRaw: RawEntry? = null
    private var decidedKey: Int? = null
    private var saved: WeighInState.Saved? = null

    fun show(state: WeighInState) {
        _state.value = state
    }

    /** Starts scanning (again); a running scan is replaced. */
    fun start(scope: CoroutineScope) {
        job?.cancel()
        job = scope.launch { scan() }
    }

    /** Deletes the saved weight and its raw data (Poista); a failed delete stays on the result. */
    fun discard(scope: CoroutineScope) {
        val result = saved ?: return
        val weight = decided ?: return
        job?.cancel()
        job = scope.launch {
            try {
                delete(weight.first, weight.second)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLog.e("weighin.discard", e, "failed", "kg" to weight.first)
                _state.value = result.copy(deleteFailed = true)
                return@launch
            }
            AppLog.i("weighin.discard", "ok", "kg" to weight.first, "time" to weight.second)
            saved = null
            keepRaw("remove") { archive.remove(weight.second.epochSecond) }
            remember()
            _state.value = WeighInState.Discarded
        }
    }

    /** Records the decided weigh-in's company id so its lingering broadcast is not saved again. */
    private fun remember() {
        val key = decidedKey ?: return
        try {
            last.save(key, now())
        } catch (e: Exception) {
            AppLog.e("weighin.last", e, "save")
        }
    }

    /** The previous weigh-in's company id while its broadcast may still linger, else null. */
    private fun ignoredKey(): Int? = try {
        last.load()?.takeIf { (_, at) -> at.plusMillis(IGNORE_MS).isAfter(now()) }?.first
    } catch (e: Exception) {
        AppLog.e("weighin.last", e, "load")
        null
    }

    private suspend fun scan() {
        decided = null
        decidedRaw = null
        decidedKey = null
        val ignored = ignoredKey()
        run {
            val scanStartMs = elapsedMs()
            val scanStartWall = now()
            var heard = false
            _state.value = WeighInState.Scanning
            val result = try {
                withTimeoutOrNull(timeoutMs) {
                    decide(scanStartMs, ignored) { reading ->
                        heard = true
                        _state.value = WeighInState.Live(reading.kg, reading.stable)
                    }
                }
            } catch (e: ScanFailedException) {
                AppLog.e("weighin.scan", e, "failed", "code" to e.code)
                _state.value = WeighInState.ScanFailed(e.code)
                return
            }
            if (result == null) {
                AppLog.w("weighin.timeout", "", "heard" to heard, "ms" to timeoutMs)
                _state.value = WeighInState.NotFound(heard)
                return
            }
            AppLog.i("scale.stable", "", "kg" to result.kg, "t" to result.atMs)
            val time = scanStartWall.plusMillis(result.atMs)
            decided = result.kg to time
            decidedKey = result.packet?.companyId
            decidedRaw = result.packet?.let { RawEntry(time.epochSecond, SOURCE, ScalePacket.rawFields(it.companyId, it.data)) }
            saveDecided()
        }
    }

    /** Saves the decided weight again after a failed save. */
    fun retrySave(scope: CoroutineScope) {
        if (decided == null) return
        job = scope.launch { saveDecided() }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private suspend fun saveDecided() {
        val (kg, time) = decided ?: return
        _state.value = WeighInState.Saving(kg)
        val started = elapsedMs()
        try {
            save(kg, time)
            AppLog.i("weighin.save", "ok", "kg" to kg, "time" to time, "ms" to elapsedMs() - started)
            AppLog.i("weighin.total", "", "ms" to elapsedMs() - openedMs)
            decidedRaw?.let { raw -> keepRaw("add") { archive.add(raw) } }
            val change = try {
                previous(time)?.let { (prevKg, prevTime) -> WeightChange(kg - prevKg, prevTime) }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLog.e("weighin.previous", e, "failed")
                null
            }
            remember()
            saved = WeighInState.Saved(kg, change).also { _state.value = it }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLog.e("weighin.save", e, "failed", "kg" to kg, "ms" to elapsedMs() - started)
            _state.value = WeighInState.SaveFailed(kg)
        }
    }

    private suspend fun decide(scanStartMs: Long, ignored: Int?, onReading: (ScaleReading) -> Unit): StableResult = coroutineScope {
        var ignoreLogged = false
        val decision = StableWeight()
        val done = CompletableDeferred<StableResult>()
        val ticker = launch {
            while (true) {
                delay(tickMs)
                decision.onTick(elapsedMs() - scanStartMs)?.let { done.complete(it) }
            }
        }
        val collector = launch {
            source.packets().collect { packet ->
                if (!ScalePacket.isScale(packet.name, packet.companyId)) return@collect
                if (packet.companyId == ignored) {
                    if (!ignoreLogged) AppLog.i("scale.ignore", "", "key" to "%04x".format(packet.companyId and 0xFFFF))
                    ignoreLogged = true
                    return@collect
                }
                val reading = ScalePacket.decode(packet.data) ?: return@collect
                onReading(reading)
                decision.onReading(reading, packet.atMs, packet)?.let { done.complete(it) }
            }
        }
        // A scan error fails the collector, which cancels this scope and surfaces at once (no timeout wait).
        try {
            done.await()
        } finally {
            ticker.cancel()
            collector.cancel()
        }
    }

    /** Archive trouble is logged and never fails the weigh-in: the weight in Health Connect is what counts. */
    private fun <T> keepRaw(action: String, block: () -> T): T? = try {
        block()
    } catch (e: Exception) {
        AppLog.e("weighin.raw", e, action)
        null
    }

    companion object {
        const val SOURCE = "scale"
        const val TIMEOUT_MS = 60_000L
        const val TICK_MS = 250L
        /** The scale repeats its last stable packet for at least ten seconds after a weighing. */
        const val IGNORE_MS = 120_000L
    }
}
