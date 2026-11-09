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
    private fun source(vararg steps: Triple<Long, Int, Boolean>) = PacketSource {
        flow {
            var at = 0L
            for ((ms, raw, stable) in steps) {
                delay(ms - at)
                at = ms
                emit(RawPacket(ms, 0x12C0, packet(raw, if (stable) 1 else 0), -60, "Yoda1"))
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

    /** Background coroutines never idle (the ticker), so move virtual time past the 60 s limit. */
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

    private fun TestScope.redoWeighIn(store: Store, vararg scans: PacketSource): WeighIn {
        var scan = 0
        return WeighIn(
            source = PacketSource { scans[scan++].packets() },
            save = { kg, t -> store.weights[t] = kg },
            elapsedMs = { testScheduler.currentTime },
            now = { wallStart.plusMillis(testScheduler.currentTime) },
            delete = { _, t ->
                if (store.failDelete) throw IOException("made-up failure")
                store.weights.remove(t)
            },
            archive = store.raw,
        )
    }

    private fun stable(raw: Int) = source(Triple(100, raw, true), Triple(200, raw, true))

    @Test
    fun `weigh again deletes the saved weight and scans, offering undo`() = runTest {
        val store = Store()
        val w = redoWeighIn(store, stable(825), source())
        w.start(backgroundScope)
        settle()
        val first = wallStart.plusMillis(100)
        assertEquals(mapOf(first to 82.5), store.weights)
        w.redo(backgroundScope)
        runCurrent()
        assertEquals(emptyMap(), store.weights)
        assertEquals(WeighInState.Scanning, w.state.value)
        assertEquals(82.5 to first, w.removed.value)
    }

    @Test
    fun `undo writes the deleted weight back and shows it saved`() = runTest {
        val store = Store()
        val w = redoWeighIn(store, stable(825), source())
        w.start(backgroundScope)
        settle()
        w.redo(backgroundScope)
        runCurrent()
        w.undo(backgroundScope)
        runCurrent()
        assertEquals(mapOf(wallStart.plusMillis(100) to 82.5), store.weights)
        assertEquals(WeighInState.Saved(82.5), w.state.value)
        assertEquals(null, w.removed.value)
    }

    @Test
    fun `new weight after weighing again ends the undo`() = runTest {
        val store = Store()
        val w = redoWeighIn(store, stable(825), stable(821))
        w.start(backgroundScope)
        settle()
        w.redo(backgroundScope)
        settle()
        assertEquals(listOf(82.1), store.weights.values.toList())
        assertEquals(WeighInState.Saved(82.1), w.state.value)
        assertEquals(null, w.removed.value)
    }

    @Test
    fun `undo is still offered after the new scan finds nothing`() = runTest {
        val store = Store()
        val w = redoWeighIn(store, stable(825), source())
        w.start(backgroundScope)
        settle()
        w.redo(backgroundScope)
        settle()
        assertEquals(WeighInState.NotFound(heard = false), w.state.value)
        assertEquals(82.5, w.removed.value?.first)
    }

    @Test
    fun `saved weigh-in keeps the deciding packet raw`() = runTest {
        val store = Store()
        val w = redoWeighIn(store, stable(825), source())
        w.start(backgroundScope)
        settle()
        val entry = store.raw.get(wallStart.plusMillis(100).epochSecond)!!
        assertEquals(WeighIn.SOURCE, entry.source)
        assertEquals("825", entry.fields["weight_raw"])
        assertEquals("12c0", entry.fields["key"])
        assertEquals("0", entry.fields["impedance"])
    }

    @Test
    fun `weigh again drops the raw data and undo brings it back`() = runTest {
        val store = Store()
        val w = redoWeighIn(store, stable(825), source())
        w.start(backgroundScope)
        settle()
        val kept = store.raw.all()
        w.redo(backgroundScope)
        runCurrent()
        assertEquals(emptyList(), store.raw.all())
        w.undo(backgroundScope)
        runCurrent()
        assertEquals(kept, store.raw.all())
    }

    @Test
    fun `new weight after weighing again keeps only its own raw data`() = runTest {
        val store = Store()
        val w = redoWeighIn(store, stable(825), stable(821))
        w.start(backgroundScope)
        settle()
        w.redo(backgroundScope)
        settle()
        assertEquals(listOf("821"), store.raw.all().map { it.fields["weight_raw"] })
    }

    @Test
    fun `failed delete stays on the saved weight`() = runTest {
        val store = Store().apply { failDelete = true }
        val w = redoWeighIn(store, stable(825), source())
        w.start(backgroundScope)
        settle()
        w.redo(backgroundScope)
        runCurrent()
        assertEquals(WeighInState.Saved(82.5, deleteFailed = true), w.state.value)
        assertEquals(1, store.weights.size)
        assertEquals(null, w.removed.value)
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
