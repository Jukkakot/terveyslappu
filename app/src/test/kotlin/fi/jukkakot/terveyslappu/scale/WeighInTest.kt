package fi.jukkakot.terveyslappu.scale

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
