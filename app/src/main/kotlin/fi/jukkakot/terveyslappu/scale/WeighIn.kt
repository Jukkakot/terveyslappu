package fi.jukkakot.terveyslappu.scale

import fi.jukkakot.terveyslappu.log.AppLog
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
    data class Saved(val kg: Double) : WeighInState
    data class SaveFailed(val kg: Double) : WeighInState
    /** Nothing decided within the time limit; [heard] tells whether any scale packet arrived. */
    data class NotFound(val heard: Boolean) : WeighInState
    data class ScanFailed(val code: Int) : WeighInState
}

/**
 * One weigh-in: scan, decide the stable weight, save it once (scale-weigh-in spec). Android-free
 * so it runs in JVM tests with a fake [source], [save] and virtual time.
 *
 * @param elapsedMs monotonic clock in ms (the ticks are measured with it)
 * @param now wall clock, for the time stamped on the saved weight
 * @param save writes kg at a time to Health Connect
 */
class WeighIn(
    private val source: PacketSource,
    private val save: suspend (kg: Double, time: Instant) -> Unit,
    private val elapsedMs: () -> Long,
    private val now: () -> Instant,
    private val timeoutMs: Long = TIMEOUT_MS,
    private val tickMs: Long = TICK_MS,
) {
    private val openedMs = elapsedMs()
    private val _state = MutableStateFlow<WeighInState>(WeighInState.Preparing)
    val state: StateFlow<WeighInState> = _state

    private var job: Job? = null
    private var decided: Pair<Double, Instant>? = null

    fun show(state: WeighInState) {
        _state.value = state
    }

    /** Starts scanning (again); a running scan is replaced. */
    fun start(scope: CoroutineScope) {
        job?.cancel()
        decided = null
        job = scope.launch {
            val scanStartMs = elapsedMs()
            val scanStartWall = now()
            var heard = false
            _state.value = WeighInState.Scanning
            val result = try {
                withTimeoutOrNull(timeoutMs) {
                    decide(scanStartMs) { reading ->
                        heard = true
                        _state.value = WeighInState.Live(reading.kg, reading.stable)
                    }
                }
            } catch (e: ScanFailedException) {
                AppLog.e("weighin.scan", e, "failed", "code" to e.code)
                _state.value = WeighInState.ScanFailed(e.code)
                return@launch
            }
            if (result == null) {
                AppLog.w("weighin.timeout", "", "heard" to heard, "ms" to timeoutMs)
                _state.value = WeighInState.NotFound(heard)
                return@launch
            }
            AppLog.i("scale.stable", "", "kg" to result.kg, "t" to result.atMs)
            decided = result.kg to scanStartWall.plusMillis(result.atMs)
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
            _state.value = WeighInState.Saved(kg)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLog.e("weighin.save", e, "failed", "kg" to kg, "ms" to elapsedMs() - started)
            _state.value = WeighInState.SaveFailed(kg)
        }
    }

    private suspend fun decide(scanStartMs: Long, onReading: (ScaleReading) -> Unit): StableResult = coroutineScope {
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
                val reading = ScalePacket.decode(packet.data) ?: return@collect
                onReading(reading)
                decision.onReading(reading, packet.atMs)?.let { done.complete(it) }
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

    companion object {
        const val TIMEOUT_MS = 60_000L
        const val TICK_MS = 250L
    }
}
