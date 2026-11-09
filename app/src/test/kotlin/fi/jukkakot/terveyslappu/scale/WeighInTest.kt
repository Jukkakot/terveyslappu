package fi.jukkakot.terveyslappu.scale

import fi.jukkakot.terveyslappu.raw.MemoryRawStore
import fi.jukkakot.terveyslappu.scale.ScalePacketTest.Companion.packet
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
class WeighInTest {

    private val wallStart = Instant.parse("2026-11-08T05:00:00Z")

    /** Emits (ms, raw weight, stable) packets on virtual time, then waits like a quiet scale. */
    private fun source(vararg steps: Triple<Long, Int, Boolean>, key: Int = 0x12C0) = PacketSource {
        flow {
            var at = 0L
            for ((ms, raw, stable) in steps) {
                delay(ms - at)
                at = ms
                emit(RawPacket(ms, key, packet(raw, if (stable) 1 else 0), -60, "Yoda1"))
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
        assertEquals(WeighInState.NotFound(heard = false), w.state.value)
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
        assertEquals(WeighInState.NotFound(heard = true), w.state.value)
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
        assertEquals(WeighInState.NotFound(heard = false), w.state.value)
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
}
