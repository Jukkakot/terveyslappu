package fi.jukkakot.terveyslappu.scale

import fi.jukkakot.terveyslappu.raw.MemoryRawStore
import fi.jukkakot.terveyslappu.scale.ScalePacketTest.Companion.packet
import fi.jukkakot.terveyslappu.ui.CLOSE_AFTER_SAVED_MS
import fi.jukkakot.terveyslappu.ui.autoCloseDelayMs
import fi.jukkakot.terveyslappu.ui.closeWhenDue
import fi.jukkakot.terveyslappu.ui.closesOnRequest
import fi.jukkakot.terveyslappu.ui.blocksBackground
import fi.jukkakot.terveyslappu.ui.endReason
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class WeighInTest {

    private val wallStart = Instant.parse("2026-11-08T05:00:00Z")

    /** Emits (ms, raw weight, stable) packets on virtual time, then waits like a quiet scale. */
    private fun source(vararg steps: Triple<Long, Int, Boolean>, key: Int = 0x12C0, address: String? = null) = PacketSource {
        flow {
            var at = 0L
            for ((ms, raw, stable) in steps) {
                delay(ms - at)
                at = ms
                emit(RawPacket(ms, key, packet(raw, if (stable) 1 else 0), -60, "Yoda1", address))
            }
            awaitCancellation()
        }
    }

    private fun TestScope.weighIn(source: PacketSource, save: suspend (Double, Instant) -> Unit) = WeighIn(
        source = source,
        save = save,
        elapsedMs = { testScheduler.currentTime },
        now = { wallStart.plusMillis(testScheduler.currentTime) },
    )

    /** Background coroutines never idle (the ticker), so move virtual time past the 30 s limit. */
    private fun TestScope.settle() {
        testScheduler.advanceTimeBy(WeighIn.TIMEOUT_MS + 5_000)
        runCurrent()
    }

    @Test
    fun `stable weight is saved once with the time of the first stable packet`() = runTest {
        val saved = mutableListOf<Pair<Double, Instant>>()
        val w = weighIn(source(Triple(100, 800, false), Triple(300, 825, true), Triple(500, 825, true))) { kg, t -> saved += kg to t }
        w.start(backgroundScope)
        settle()
        assertEquals(listOf(82.5 to wallStart.plusMillis(300)), saved)
        assertEquals(WeighInState.Saved(82.5), w.state.value)
    }

    private fun TestScope.withPrevious(previous: suspend (Instant) -> Pair<Double, Instant>?) = WeighIn(
        source = source(Triple(100, 825, true), Triple(200, 825, true)),
        save = { _, _ -> },
        elapsedMs = { testScheduler.currentTime },
        now = { wallStart.plusMillis(testScheduler.currentTime) },
        previous = previous,
    )

    @Test
    fun `saved state carries the change from the previous weight`() = runTest {
        val before = wallStart.minusSeconds(86_400)
        val w = withPrevious { 82.8 to before }
        w.start(backgroundScope)
        settle()
        val saved = w.state.value as WeighInState.Saved
        assertEquals(before, saved.change!!.previousTime)
        assertEquals(-0.3, saved.change.deltaKg, 1e-9)
    }

    @Test
    fun `failed previous lookup still shows the saved weight`() = runTest {
        val w = withPrevious { throw IOException("made-up") }
        w.start(backgroundScope)
        settle()
        assertEquals(WeighInState.Saved(82.5, null), w.state.value)
    }

    @Test
    fun `no scale within the time limit`() = runTest {
        val w = weighIn(source()) { _, _ -> error("must not save") }
        w.start(backgroundScope)
        settle()
        assertEquals(WeighInState.NotFound(EndReason.NotHeard), w.state.value)
    }

    @Test
    fun `the countdown shows the seconds left while scanning`() = runTest {
        val w = weighIn(source()) { _, _ -> error("must not save") }
        w.start(backgroundScope)
        testScheduler.advanceTimeBy(2_000)
        runCurrent()
        assertEquals(28, w.secondsLeft.value)
        settle()
        assertEquals(null, w.secondsLeft.value)
    }

    @Test
    fun `scale heard but never stable`() = runTest {
        val w = weighIn(source(Triple(100, 800, false))) { _, _ -> error("must not save") }
        w.start(backgroundScope)
        settle()
        assertEquals(WeighInState.NotFound(EndReason.Unsettled, heard = true), w.state.value)
    }

    @Test
    fun `failed save can be retried`() = runTest {
        var fail = true
        val saved = mutableListOf<Double>()
        val w = weighIn(source(Triple(100, 825, true), Triple(200, 825, true))) { kg, _ ->
            if (fail) throw IOException("made-up failure")
            saved += kg
        }
        w.start(backgroundScope)
        settle()
        assertEquals(WeighInState.SaveFailed(82.5), w.state.value)
        fail = false
        w.retrySave(backgroundScope)
        settle()
        assertEquals(listOf(82.5), saved)
        assertEquals(WeighInState.Saved(82.5), w.state.value)
    }

    @Test
    fun `scan failure is shown at once`() = runTest {
        val w = weighIn(PacketSource { flow { throw ScanFailedException(2) } }) { _, _ -> }
        w.start(backgroundScope)
        settle()
        assertEquals(WeighInState.ScanFailed(2), w.state.value)
    }

    /** Made-up store: saves upsert by time, deletes remove; packets come per scan from [scans]. */
    private class Store {
        val weights = mutableMapOf<Instant, Double>()
        val raw = MemoryRawStore()
        var failDelete = false
    }

    private fun TestScope.storeWeighIn(store: Store, source: PacketSource, last: LastWeighInStore = MemoryLastWeighIn()) = WeighIn(
        source = source,
        save = { kg, t -> store.weights[t] = kg },
        elapsedMs = { testScheduler.currentTime },
        now = { wallStart.plusMillis(testScheduler.currentTime) },
        delete = { _, t ->
            if (store.failDelete) throw IOException("made-up failure")
            store.weights.remove(t)
        },
        archive = store.raw,
        last = last,
    )

    private fun stable(raw: Int, key: Int = 0x12C0) = source(Triple(100, raw, true), Triple(200, raw, true), key = key)

    @Test
    fun `discard deletes the weight and its raw data and closes`() = runTest {
        val store = Store()
        val w = storeWeighIn(store, stable(825))
        w.start(backgroundScope)
        settle()
        assertEquals(1, store.raw.all().size)
        w.discard(backgroundScope)
        runCurrent()
        assertEquals(emptyMap(), store.weights)
        assertEquals(emptyList(), store.raw.all())
        assertEquals(WeighInState.Discarded, w.state.value)
    }

    @Test
    fun `saved weigh-in keeps the deciding packet raw`() = runTest {
        val store = Store()
        val w = storeWeighIn(store, stable(825))
        w.start(backgroundScope)
        settle()
        val entry = store.raw.get(wallStart.plusMillis(100).epochSecond)!!
        assertEquals(WeighIn.SOURCE, entry.source)
        assertEquals("825", entry.fields["weight_raw"])
        assertEquals("12c0", entry.fields["key"])
        assertEquals("0", entry.fields["impedance"])
    }

    @Test
    fun `failed discard stays on the saved weight`() = runTest {
        val store = Store().apply { failDelete = true }
        val w = storeWeighIn(store, stable(825))
        w.start(backgroundScope)
        settle()
        w.discard(backgroundScope)
        runCurrent()
        assertEquals(WeighInState.Saved(82.5, deleteFailed = true), w.state.value)
        assertEquals(1, store.weights.size)
    }

    /** The previous weigh-in (key 61c0) ended [agoMs] before this one opened. */
    private fun TestScope.afterPrevious(store: Store, agoMs: Long, source: PacketSource): WeighIn {
        val last = MemoryLastWeighIn().apply { save(0x61C0, wallStart.minusMillis(agoMs)) }
        return storeWeighIn(store, source, last)
    }

    @Test
    fun `the previous weigh-in's lingering broadcast is ignored`() = runTest {
        val store = Store()
        val w = afterPrevious(store, 10_000, stable(825, key = 0x61C0))
        w.start(backgroundScope)
        settle()
        assertEquals(emptyMap(), store.weights)
        // The weight of that weigh-in is not known here: the repeat line has no brackets.
        assertEquals(WeighInState.NotFound(EndReason.Repeat(RepeatLine(null)), heard = false, ignored = 2), w.state.value)
    }

    /** 82.5 kg was saved (or deleted) five seconds before the dialog opened. */
    private fun rememberedLast(deleted: Boolean = false) = MemoryLastWeighIn().also {
        WeighingKeys(it) { wallStart.minusSeconds(5) }.remember(82.5, deleted = deleted)
    }

    @Test
    fun `only the same weight repeated ends with the repeat line and its save time`() = runTest {
        val store = Store()
        val w = storeWeighIn(store, stable(825, key = 0x62C0), rememberedLast(deleted = true))
        w.start(backgroundScope)
        testScheduler.advanceTimeBy(150)
        runCurrent()
        val scanning = w.state.value as WeighInState.Scanning
        assertEquals(82.5, scanning.repeat?.last?.kg)
        settle()
        val end = w.state.value as WeighInState.NotFound
        val line = (end.reason as EndReason.Repeat).line
        assertEquals(wallStart.minusSeconds(5), line.last?.at)
        assertTrue(line.last!!.deleted)
        assertFalse(end.heard)
        assertEquals(2, end.ignored)
        assertEquals(emptyMap(), store.weights)
    }

    @Test
    fun `a live weight that settles on the repeat ends as a repeat`() = runTest {
        val store = Store()
        val source = source(Triple(100, 800, false), Triple(300, 822, false), Triple(500, 825, true), Triple(700, 825, true), key = 0x62C0)
        val w = storeWeighIn(store, source, rememberedLast())
        w.start(backgroundScope)
        testScheduler.advanceTimeBy(600)
        runCurrent()
        val live = w.state.value as WeighInState.Live
        assertEquals(82.5, live.repeat?.last?.kg)
        settle()
        val end = w.state.value as WeighInState.NotFound
        assertTrue(end.reason is EndReason.Repeat)
        assertTrue(end.heard)
        assertEquals(emptyMap(), store.weights)
    }

    @Test
    fun `a stable reading of another weight after an ignored one is not a repeat`() = runTest {
        val store = Store()
        // The repeat is ignored, then the user steps on and a new weight settles: it is saved.
        val source = source(Triple(100, 825, true), Triple(400, 790, false), Triple(600, 818, true), Triple(800, 818, true), key = 0x62C0)
        val w = storeWeighIn(store, source, rememberedLast())
        w.start(backgroundScope)
        settle()
        assertEquals(listOf(81.8), store.weights.values.toList())
    }

    @Test
    fun `discard remembers the weight as deleted`() = runTest {
        val store = Store()
        val last = MemoryLastWeighIn()
        val w = storeWeighIn(store, stable(825), last)
        w.start(backgroundScope)
        settle()
        assertFalse(last.weight()!!.deleted)
        w.discard(backgroundScope)
        runCurrent()
        assertTrue(last.weight()!!.deleted)
    }

    @Test
    fun `the recent automatic weight is deleted, a failed delete stays on it`() = runTest {
        val w = weighIn(source()) { _, _ -> error("must not save") }
        val recent = WeighInState.Recent(82.5, wallStart.minusSeconds(60), "terveyslappu-scale-1")
        w.showRecent(recent)
        w.discardRecent(backgroundScope) { false }
        runCurrent()
        assertEquals(recent.copy(deleteFailed = true), w.state.value)
        w.discardRecent(backgroundScope) { true }
        runCurrent()
        assertEquals(WeighInState.Discarded, w.state.value)
    }

    @Test
    fun `the same weight with a new company id after a delete is ignored, another weight is not`() = runTest {
        val store = Store()
        val last = MemoryLastWeighIn()
        WeighingKeys(last) { wallStart.minusSeconds(5) }.remember(82.5)
        val w = storeWeighIn(store, stable(825, key = 0x62C0), last)
        w.start(backgroundScope)
        settle()
        assertEquals(emptyMap(), store.weights)
        val other = storeWeighIn(store, stable(818, key = 0x63C0), last)
        other.start(backgroundScope)
        settle()
        assertEquals(listOf(81.8), store.weights.values.toList())
    }

    @Test
    fun `a weighing the background saved meanwhile is not saved again`() = runTest {
        val store = Store()
        val last = MemoryLastWeighIn()
        // The dialog is open (nothing lingering at its start); the background claims 61c0 a moment later.
        val w = storeWeighIn(store, source(Triple(1_000, 825, true), Triple(1_100, 825, true), key = 0x61C0), last)
        w.start(backgroundScope)
        runCurrent()
        assertTrue(WeighingKeys(last) { wallStart.plusMillis(500) }.claim(0x61C0))
        settle()
        assertEquals(emptyMap(), store.weights)
    }

    @Test
    fun `a new weighing is saved right after the previous one`() = runTest {
        val store = Store()
        val w = afterPrevious(store, 10_000, stable(821, key = 0x62C0))
        w.start(backgroundScope)
        settle()
        assertEquals(listOf(82.1), store.weights.values.toList())
    }

    @Test
    fun `the previous weigh-in's id is used again after two minutes`() = runTest {
        val store = Store()
        val w = afterPrevious(store, WeighIn.IGNORE_MS + 1_000, stable(825, key = 0x61C0))
        w.start(backgroundScope)
        settle()
        assertEquals(listOf(82.5), store.weights.values.toList())
    }

    @Test
    fun `saving and discarding remember the weigh-in's id`() = runTest {
        val last = MemoryLastWeighIn()
        val w = storeWeighIn(Store(), stable(825, key = 0x61C0), last)
        w.start(backgroundScope)
        settle()
        assertEquals(0x61C0, last.load()?.first)
    }

    @Test
    fun `stopping before a result saves nothing`() = runTest {
        val w = weighIn(source(Triple(100, 825, true), Triple(5_000, 825, true))) { _, _ -> error("must not save") }
        w.start(backgroundScope)
        testScheduler.advanceTimeBy(150)
        w.stop()
        settle()
        assertEquals(WeighInState.Live(82.5, true), w.state.value)
    }

    @Test
    fun `a saved weigh-in remembers its scale and Poista keeps it`() = runTest {
        val last = MemoryLastWeighIn()
        val store = Store()
        val w = storeWeighIn(store, source(Triple(100, 825, true), Triple(200, 825, true), address = "00:11:22:33:44:55"), last)
        w.start(backgroundScope)
        settle()
        assertEquals(KnownScale("00:11:22:33:44:55", "Yoda1"), last.scale())
        w.discard(backgroundScope)
        runCurrent()
        assertEquals(WeighInState.Discarded, w.state.value)
        assertEquals(KnownScale("00:11:22:33:44:55", "Yoda1"), last.scale())
    }

    /** A Health Connect that takes [ms] to answer a write or delete, counting the calls. */
    private class SlowStore(val ms: Long) {
        val weights = mutableMapOf<Instant, Double>()
        var saves = 0
        var deletes = 0
    }

    private fun TestScope.slowWeighIn(store: SlowStore, last: LastWeighInStore = MemoryLastWeighIn()) = WeighIn(
        source = stable(825, key = 0x61C0),
        // The write lands at once; Health Connect's answer takes a while (a cancel then loses only the answer).
        save = { kg, t ->
            store.saves++
            store.weights[t] = kg
            delay(store.ms)
        },
        elapsedMs = { testScheduler.currentTime },
        now = { wallStart.plusMillis(testScheduler.currentTime) },
        delete = { _, t ->
            delay(store.ms)
            store.deletes++
            store.weights.remove(t)
        },
        last = last,
    )

    /** A scope like the dialog's (viewModelScope), cancelled when the dialog goes. */
    private fun TestScope.dialogScope() = CoroutineScope(backgroundScope.coroutineContext + Job(backgroundScope.coroutineContext[Job]))

    @Test
    fun `a restart while saving neither shows Preparing nor saves again`() = runTest {
        val store = SlowStore(1_000)
        val last = MemoryLastWeighIn()
        val w = slowWeighIn(store, last)
        w.start(backgroundScope)
        testScheduler.advanceTimeBy(500)
        runCurrent()
        assertEquals(WeighInState.Saving(82.5), w.state.value)
        // The recreated dialog prepares again.
        w.show(WeighInState.Preparing)
        w.start(backgroundScope)
        assertEquals(WeighInState.Saving(82.5), w.state.value)
        settle()
        assertEquals(1, store.saves)
        assertEquals(WeighInState.Saved(82.5), w.state.value)
        assertEquals(0x61C0, last.load()?.first)
    }

    @Test
    fun `the save finishes when the dialog's scope is cancelled`() = runTest {
        val store = SlowStore(1_000)
        val last = MemoryLastWeighIn()
        val w = slowWeighIn(store, last)
        val dialog = dialogScope()
        w.start(dialog, saveScope = backgroundScope)
        testScheduler.advanceTimeBy(500)
        runCurrent()
        dialog.cancel()
        w.stop()
        settle()
        assertEquals(1, store.weights.size)
        assertEquals(0x61C0, last.load()?.first)
        assertEquals(WeighInState.Saved(82.5), w.state.value)
    }

    @Test
    fun `Poista finishes after the dialog closes and a second tap is ignored`() = runTest {
        val store = SlowStore(1_000)
        val w = slowWeighIn(store)
        w.start(backgroundScope)
        settle()
        w.discard(backgroundScope)
        w.discard(backgroundScope)
        // The dialog closes: its scan stops, the delete runs on.
        w.stop()
        settle()
        assertEquals(1, store.deletes)
        assertEquals(emptyMap(), store.weights)
        assertEquals(WeighInState.Discarded, w.state.value)
    }

    @Test
    fun `Poista just before the auto-close waits for the delete`() = runTest {
        val store = SlowStore(2_000)
        val w = slowWeighIn(store)
        w.start(backgroundScope)
        testScheduler.advanceTimeBy(5_000)
        runCurrent()
        assertEquals(WeighInState.Saved(82.5), w.state.value)
        val keepOpen = MutableStateFlow(false)
        var closedAt: Long? = null
        backgroundScope.launch {
            combine(w.state, keepOpen) { s, k -> s to k }.collectLatest { (s, k) ->
                closeWhenDue(s, k) { if (closedAt == null) closedAt = testScheduler.currentTime }
            }
        }
        runCurrent()
        val savedAt = testScheduler.currentTime
        testScheduler.advanceTimeBy(CLOSE_AFTER_SAVED_MS - 500)
        runCurrent()
        // Poista, as the activity wires it: keep open, then delete.
        keepOpen.value = true
        w.discard(backgroundScope)
        testScheduler.advanceTimeBy(1_000)
        runCurrent()
        assertNull(closedAt)
        assertEquals(1, store.weights.size)
        testScheduler.advanceTimeBy(1_500)
        runCurrent()
        assertEquals(WeighInState.Discarded, w.state.value)
        assertEquals(emptyMap(), store.weights)
        assertEquals(savedAt + CLOSE_AFTER_SAVED_MS - 500 + 2_000, closedAt)
    }

    @Test
    fun `the end view and the recent weight follow the close rules`() {
        assertNull(autoCloseDelayMs(WeighInState.NotFound(EndReason.NotHeard), keepOpen = false))
        val recent = WeighInState.Recent(82.5, wallStart, "id")
        assertEquals(60_000L, autoCloseDelayMs(recent, keepOpen = false, resultMs = 60_000L))
        assertNull(autoCloseDelayMs(recent, keepOpen = true))
    }

    @Test
    fun `the countdown on Valmis starts from the lengthened time`() = runTest {
        val seconds = mutableListOf<Int>()
        var closed = false
        closeWhenDue(WeighInState.Saved(82.5), keepOpen = false, resultMs = 3_000L, onSeconds = { seconds += it }) { closed = true }
        assertEquals(listOf(3, 2, 1), seconds)
        assertTrue(closed)
    }

    @Test
    fun `every dialog state has an end reason`() {
        val repeat = EndReason.Repeat(RepeatLine(null))
        val cases = mapOf<WeighInState, String>(
            WeighInState.Preparing to "cancelled",
            WeighInState.BluetoothOff to "cancelled",
            WeighInState.Scanning() to "cancelled",
            WeighInState.Live(82.4, stable = false) to "cancelled",
            WeighInState.NoHealthAccess to "failed",
            WeighInState.PermissionDenied to "failed",
            WeighInState.ScanFailed(2) to "failed",
            WeighInState.SaveFailed(82.5) to "failed",
            WeighInState.Saved(82.5, deleteFailed = true) to "failed",
            WeighInState.Saving(82.5) to "saved",
            WeighInState.Saved(82.5) to "saved",
            WeighInState.Discarded to "discarded",
            WeighInState.NotFound(EndReason.NotHeard) to "notheard",
            WeighInState.NotFound(EndReason.Unsettled, heard = true) to "unsettled",
            WeighInState.NotFound(repeat, ignored = 4) to "repeat",
            WeighInState.Recent(82.5, wallStart, "id") to "recent",
            WeighInState.Recent(82.5, wallStart, "id", deleteFailed = true) to "failed",
        )
        cases.forEach { (state, reason) -> assertEquals(reason, endReason(state, fromRecent = false), state.toString()) }
        assertEquals("recentdeleted", endReason(WeighInState.Discarded, fromRecent = true))
    }

    @Test
    fun `the background is blocked only while the dialog could save itself`() {
        assertTrue(blocksBackground(WeighInState.Preparing))
        assertTrue(blocksBackground(WeighInState.Scanning()))
        assertTrue(blocksBackground(WeighInState.Live(82.4, stable = false)))
        assertTrue(blocksBackground(WeighInState.Saving(82.5)))
        assertFalse(blocksBackground(WeighInState.NotFound(EndReason.NotHeard)))
        assertFalse(blocksBackground(WeighInState.Saved(82.5)))
        assertFalse(blocksBackground(WeighInState.Recent(82.5, wallStart, "id")))
        assertFalse(blocksBackground(WeighInState.Discarded))
        assertFalse(blocksBackground(WeighInState.BluetoothOff))
    }

    @Test
    fun `the saved result closes by itself after ten seconds unless kept open`() {
        assertEquals(CLOSE_AFTER_SAVED_MS, autoCloseDelayMs(WeighInState.Saved(82.5), keepOpen = false))
        assertNull(autoCloseDelayMs(WeighInState.Saved(82.5), keepOpen = true))
        assertEquals(0L, autoCloseDelayMs(WeighInState.Discarded, keepOpen = true))
        assertNull(autoCloseDelayMs(WeighInState.Saving(82.5), keepOpen = false))
        assertFalse(closesOnRequest(WeighInState.Saving(82.5)))
        assertTrue(closesOnRequest(WeighInState.Saved(82.5)))
    }
}
