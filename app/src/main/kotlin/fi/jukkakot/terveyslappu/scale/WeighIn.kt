package fi.jukkakot.terveyslappu.scale

import fi.jukkakot.terveyslappu.derived.shownDelta
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
import kotlin.math.abs

/** Difference to the latest earlier weight and that weight's time (weigh-in-feedback). */
data class WeightChange(val deltaKg: Double, val previousTime: Instant)

/**
 * The dialog's "Vaaka toistaa äskeistä punnitusta" line (everyday-feedback design 1): [last] is the
 * remembered weight when the ignored packet repeats it (the brackets with kg and time), else null.
 */
data class RepeatLine(val last: LastWeight?)

/** Why a weigh-in ended without a saved weight (scale-weigh-in "Weigh-in dialog"). */
sealed interface EndReason {
    val key: String

    data object NotHeard : EndReason {
        override val key = "notheard"
    }

    data object Unsettled : EndReason {
        override val key = "unsettled"
    }

    data class Repeat(val line: RepeatLine) : EndReason {
        override val key = "repeat"
    }
}

/** What the weigh-in dialog shows. */
sealed interface WeighInState {
    /** Checking access (Health Connect, nearby devices, Bluetooth) before scanning. */
    data object Preparing : WeighInState
    data object NoHealthAccess : WeighInState
    data object PermissionDenied : WeighInState
    data object BluetoothOff : WeighInState
    /** Scanning, no scale packet yet: "step on the scale", or the repeat line while only repeats are heard. */
    data class Scanning(val repeat: RepeatLine? = null) : WeighInState
    /** A changing weight; [repeat] while its stable reading is ignored as a repeat. */
    data class Live(val kg: Double, val stable: Boolean, val repeat: RepeatLine? = null) : WeighInState
    data class Saving(val kg: Double) : WeighInState
    /** [deleteFailed]: Poista could not delete this weight. */
    data class Saved(val kg: Double, val change: WeightChange? = null, val deleteFailed: Boolean = false) : WeighInState
    data class SaveFailed(val kg: Double) : WeighInState
    /**
     * Nothing decided within the time limit: the end view. [heard] tells whether a reading other
     * than an ignored repeat arrived, [ignored] how many packets were ignored as repeats.
     */
    data class NotFound(val reason: EndReason, val heard: Boolean = false, val ignored: Int = 0) : WeighInState
    /**
     * The dialog opened right after an automatic save (scale-weigh-in "Open on a recent automatic
     * weight"): that weight instead of a scan. [id] is its client record id.
     */
    data class Recent(
        val kg: Double,
        val time: Instant,
        val id: String,
        val change: WeightChange? = null,
        val deleteFailed: Boolean = false,
    ) : WeighInState
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
    private val keys: WeighingKeys = WeighingKeys(last, now),
) {
    private val openedMs = elapsedMs()
    private val _state = MutableStateFlow<WeighInState>(WeighInState.Preparing)
    val state: StateFlow<WeighInState> = _state

    /** Seconds left before the weigh-in gives up, while scanning; null otherwise (the dialog shows it). */
    private val _secondsLeft = MutableStateFlow<Int?>(null)
    val secondsLeft: StateFlow<Int?> = _secondsLeft

    /** The scan; cancelled when the dialog stops or restarts. */
    private var job: Job? = null
    /** Save, retry or Poista: runs in its own scope so closing or recreating the dialog does not cut it. */
    private var work: Job? = null
    private var decided: Pair<Double, Instant>? = null
    private var decidedRaw: RawEntry? = null
    private var decidedKey: Int? = null
    private var decidedScale: KnownScale? = null
    private var saved: WeighInState.Saved? = null

    /** True once a stable weight is decided: the weigh-in then only saves, shows and maybe discards it. */
    val decidedWeight: Boolean get() = decided != null

    /** The latest scan's counts for `weighin.end`: a reading other than an ignored repeat was heard, packets ignored. */
    @Volatile var heard: Boolean = false
        private set
    @Volatile var ignored: Int = 0
        private set

    /** Shows an access state; ignored once a weight is decided (a recreated dialog keeps the result). */
    fun show(state: WeighInState) {
        if (decided != null) return
        _state.value = state
    }

    /**
     * Starts scanning (again); a running scan is replaced. The decided weight is saved in
     * [saveScope], which outlives the dialog. Ignored once a weight is decided, so a restart never
     * weighs or saves again.
     */
    fun start(scope: CoroutineScope, saveScope: CoroutineScope = scope) {
        if (decided != null) return
        job?.cancel()
        job = scope.launch { scan(saveScope) }
    }

    /** Deletes the saved weight and its raw data (Poista); a failed delete stays on the result. */
    fun discard(scope: CoroutineScope) {
        val result = saved ?: return
        val weight = decided ?: return
        if (work?.isActive == true) return
        work = scope.launch {
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
            remember(deleted = true)
            _state.value = WeighInState.Discarded
        }
    }

    /** Shows the recent automatic weight instead of scanning; ignored once a weight is decided. */
    fun showRecent(recent: WeighInState.Recent) = show(recent)

    /**
     * Poista on the recent automatic weight: [delete] removes it (the notification's path) and
     * returns false when that failed; a failed delete stays on the view saying so.
     */
    fun discardRecent(scope: CoroutineScope, delete: suspend (WeighInState.Recent) -> Boolean) {
        val recent = _state.value as? WeighInState.Recent ?: return
        if (work?.isActive == true) return
        work = scope.launch {
            val ok = try {
                delete(recent)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLog.e("weighin.recent", e, "delete failed")
                false
            }
            _state.value = if (ok) WeighInState.Discarded else recent.copy(deleteFailed = true)
        }
    }

    /** Records the decided weigh-in's company id so its lingering broadcast is not saved again. */
    private fun remember(deleted: Boolean = false) {
        val key = decidedKey ?: return
        try {
            keys.touch(key)
            decided?.let { keys.remember(it.first, decidedScale?.address, deleted) }
        } catch (e: Exception) {
            AppLog.e("weighin.last", e, "save")
        }
    }

    /** A saved weigh-in's scale becomes the one background listening follows (scale-weigh-in). */
    private fun rememberScale() {
        val scale = decidedScale ?: return
        try {
            last.saveScale(scale)
        } catch (e: Exception) {
            AppLog.e("weighin.last", e, "scale")
        }
    }

    /** The previous weigh-in's company id while its broadcast may still linger, else null. */
    private fun ignoredKey(): Int? = try {
        keys.lingering()
    } catch (e: Exception) {
        AppLog.e("weighin.last", e, "load")
        null
    }

    private suspend fun scan(saveScope: CoroutineScope) {
        decided = null
        decidedRaw = null
        decidedKey = null
        decidedScale = null
        val ignoredKey = ignoredKey()
        heard = false
        ignored = 0
        val seen = Seen()
        run {
            val scanStartMs = elapsedMs()
            val scanStartWall = now()
            _state.value = WeighInState.Scanning()
            val result = try {
                withTimeoutOrNull(timeoutMs) {
                    coroutineScope {
                        val ticker = launch {
                            var left = (timeoutMs / 1000).toInt()
                            while (left >= 0) {
                                _secondsLeft.value = left
                                delay(1000)
                                left--
                            }
                        }
                        try {
                            decide(scanStartMs, ignoredKey, seen) { reading ->
                                heard = true
                                _state.value = WeighInState.Live(reading.kg, reading.stable, seen.repeatFor(reading.kg))
                            }
                        } finally {
                            ticker.cancel()
                        }
                    }
                }
            } catch (e: ScanFailedException) {
                _secondsLeft.value = null
                AppLog.e("weighin.scan", e, "failed", "code" to e.code)
                _state.value = WeighInState.ScanFailed(e.code)
                return
            }
            _secondsLeft.value = null
            if (result == null) {
                val reason = seen.reason(heard)
                AppLog.w("weighin.timeout", "", "heard" to heard, "ignored" to ignored, "reason" to reason.key, "ms" to timeoutMs)
                _state.value = WeighInState.NotFound(reason, heard, ignored)
                return
            }
            AppLog.i("scale.stable", "", "kg" to result.kg, "t" to result.atMs)
            val time = scanStartWall.plusMillis(result.atMs)
            decided = result.kg to time
            decidedKey = result.packet?.companyId
            decidedScale = result.packet?.let { p -> p.address?.let { KnownScale(it, p.name?.ifEmpty { null }) } }
            decidedRaw = result.packet?.let { RawEntry(time.epochSecond, SOURCE, ScalePacket.rawFields(it.companyId, it.data)) }
            work = saveScope.launch { saveDecided() }
        }
    }

    /** Saves the decided weight again after a failed save. */
    fun retrySave(scope: CoroutineScope) {
        if (decided == null || _state.value !is WeighInState.SaveFailed || work?.isActive == true) return
        work = scope.launch { saveDecided() }
    }

    /** Stops scanning; a save or Poista already under way finishes. */
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
                previous(time)?.let { (prevKg, prevTime) -> WeightChange(shownDelta(prevKg, kg), prevTime) }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLog.e("weighin.previous", e, "failed")
                null
            }
            remember()
            rememberScale()
            saved = WeighInState.Saved(kg, change).also { _state.value = it }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLog.e("weighin.save", e, "failed", "kg" to kg, "ms" to elapsedMs() - started)
            _state.value = WeighInState.SaveFailed(kg)
        }
    }

    /**
     * What the scan ignored as repeats (everyday-feedback design 1): the newest ignored packet's
     * line and kg, and whether the newest stable reading heard was an ignored one.
     */
    private class Seen {
        var line: RepeatLine? = null
        var ignoredKg: Double? = null
        var newestStableIgnored = false

        /** The repeat line under a live weight while that weight is the ignored one. */
        fun repeatFor(kg: Double): RepeatLine? =
            line?.takeIf { ignoredKg?.let { abs(it - kg) <= WeighingKeys.SAME_KG + 1e-9 } == true }

        fun reason(heard: Boolean): EndReason {
            val repeat = line
            return when {
                repeat != null && (!heard || newestStableIgnored) -> EndReason.Repeat(repeat)
                heard -> EndReason.Unsettled
                else -> EndReason.NotHeard
            }
        }
    }

    /** An ignored packet: counted, and the dialog shows the repeat line (under the live weight when one shows). */
    private fun onIgnored(seen: Seen, reading: ScaleReading?, address: String?) {
        ignored++
        val last = if (reading?.stable == true) lastWeight()?.takeIf { it.repeats(reading.kg, address) } else null
        val line = RepeatLine(last)
        seen.line = line
        if (reading?.stable == true) {
            seen.newestStableIgnored = true
            seen.ignoredKg = reading.kg
        }
        when (val current = _state.value) {
            is WeighInState.Scanning -> _state.value = WeighInState.Scanning(line)
            is WeighInState.Live -> if (reading?.stable == true) _state.value = current.copy(repeat = line)
            else -> Unit
        }
    }

    private fun LastWeight.repeats(kg: Double, address: String?): Boolean {
        val sameScale = this.address == null || address == null || this.address.equals(address, ignoreCase = true)
        return sameScale && abs(this.kg - kg) <= WeighingKeys.SAME_KG + 1e-9
    }

    private fun lastWeight(): LastWeight? = try {
        keys.lastWeight()
    } catch (e: Exception) {
        AppLog.e("weighin.last", e, "load")
        null
    }

    private suspend fun decide(scanStartMs: Long, ignoredKey: Int?, seen: Seen, onReading: (ScaleReading) -> Unit): StableResult = coroutineScope {
        var ignoreLogged = false
        var decision = StableWeight()
        val done = CompletableDeferred<StableResult>()
        val ticker = launch {
            while (true) {
                delay(tickMs)
                decision.onTick(elapsedMs() - scanStartMs)?.let { result ->
                    if (claim(result)) done.complete(result) else decision = StableWeight()
                }
            }
        }
        val collector = launch {
            source.packets().collect { packet ->
                if (!ScalePacket.isScale(packet.name, packet.companyId)) return@collect
                if (packet.companyId == ignoredKey || takenElsewhere(packet.companyId)) {
                    if (!ignoreLogged) AppLog.i("scale.ignore", "", "key" to "%04x".format(packet.companyId and 0xFFFF))
                    ignoreLogged = true
                    onIgnored(seen, ScalePacket.decode(packet.data), packet.address)
                    return@collect
                }
                val reading = ScalePacket.decode(packet.data) ?: return@collect
                if (reading.stable && sameWeight(reading.kg, packet.address)) {
                    AppLog.i("scale.ignore", "", "reason" to "same", "kg" to reading.kg)
                    onIgnored(seen, reading, packet.address)
                    return@collect
                }
                if (reading.stable) seen.newestStableIgnored = false
                onReading(reading)
                decision.onReading(reading, packet.atMs, packet)?.let { result ->
                    // Claimed atomically before saving: the background may have saved this weighing first.
                    if (claim(result)) done.complete(result) else decision = StableWeight()
                }
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

    /** True when the weighing [key] was claimed (saved or discarded) by the background or an earlier weigh-in. */
    private fun takenElsewhere(key: Int): Boolean = try {
        keys.isTaken(key)
    } catch (e: Exception) {
        AppLog.e("weighin.last", e, "load")
        false
    }

    private fun sameWeight(kg: Double, address: String?): Boolean = try {
        keys.sameWeight(kg, address)
    } catch (e: Exception) {
        AppLog.e("weighin.last", e, "same")
        false
    }

    private fun claim(result: StableResult): Boolean {
        val key = result.packet?.companyId ?: return true
        val ok = try {
            keys.claim(key)
        } catch (e: Exception) {
            AppLog.e("weighin.last", e, "claim")
            true
        }
        if (!ok) AppLog.i("scale.ignore", "", "key" to "%04x".format(key and 0xFFFF), "claimed" to true)
        return ok
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
        const val TIMEOUT_MS = 30_000L
        const val TICK_MS = 250L
        /** The scale repeats its last stable packet for at least ten seconds after a weighing. */
        const val IGNORE_MS = 120_000L
    }
}
