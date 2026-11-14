package fi.jukkakot.terveyslappu.auto

import fi.jukkakot.terveyslappu.listen.HeardEvent
import fi.jukkakot.terveyslappu.raw.MemoryRawStore
import fi.jukkakot.terveyslappu.scale.MemoryLastWeighIn
import fi.jukkakot.terveyslappu.scale.ScalePacketTest.Companion.packet
import fi.jukkakot.terveyslappu.scale.WeighingKeys
import fi.jukkakot.terveyslappu.weight.scaleClientId
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The automatic save with fake Health Connect and notifications; made-up weights (NFR-1). */
class AutoWeightsTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val zone = ZoneId.of("Europe/Helsinki")
    private val morning = Instant.parse("2026-11-14T05:12:00Z")

    private class FakeWriter : AutoWriter {
        val saved = mutableMapOf<String, Double>()
        var fail = 0
        var slowMs = 0L
        var failDelete = false
        override suspend fun write(weight: PendingWeight): String {
            if (slowMs > 0) delay(slowMs)
            if (fail > 0) {
                fail--
                throw IllegalStateException("refused")
            }
            val id = AutoWeights.clientId(weight)
            saved[id] = weight.kg
            return id
        }
        override suspend fun delete(clientRecordId: String) {
            check(!failDelete) { "delete refused" }
            saved.remove(clientRecordId)
        }
    }

    private class FakeNotifier : AutoNotifier {
        val posted = mutableListOf<AutoNote>()
        val cancelled = mutableListOf<String>()
        override fun post(note: AutoNote): Boolean {
            posted += note
            return true
        }
        override fun cancel(id: String) {
            cancelled += id
        }
    }

    private val writer = FakeWriter()
    private val notifier = FakeNotifier()
    private val keys = WeighingKeys(MemoryLastWeighIn()) { morning }
    private val queue = PendingStore()
    private val marks = MarkStore()
    private val reference = ReferenceStore()
    private val flags = FlagStore()
    private val raw = MemoryRawStore()
    private var dialog = false
    private var changed = 0

    private fun auto(virtualMs: () -> Long = { 0 }) = AutoWeights(
        keys, queue, marks, reference, flags, raw, writer, notifier,
        limit = { 5.0 },
        changed = { changed++ },
        dialogShown = { dialog },
        elapsedMs = virtualMs,
        zone = { zone },
    )

    private fun decided(kg: Double, at: Instant = morning, key: Int = 0x61C0) =
        HeardEvent.Decided(kg, at.toEpochMilli(), key, packet((kg * 10).toInt(), 1))

    @Test
    fun `a decided weight is saved once with its raw data, notified and signalled`() = runTest {
        reference.seed(listOf(RefPair("r", morning.minusSeconds(86_400).toEpochMilli(), 82.0)))
        val auto = auto()
        auto.decided(decided(82.4))
        // The scale repeats the same weighing: the claimed key is not saved again.
        auto.decided(decided(82.4, morning.plusSeconds(3)))
        assertEquals(1, auto.flush(AutoWeights.WEIGHIN))
        val id = scaleClientId(morning)
        assertEquals(mapOf(id to 82.4), writer.saved)
        assertEquals("auto", raw.get(morning.epochSecond)?.fields?.get("via"))
        assertTrue(marks.all().isEmpty())
        assertEquals(82.4, (reference.reference(morning.plusSeconds(60).toEpochMilli()) as Reference.Weight).kg)
        assertEquals(NoteText("82,4 kg tallennettu", "+0,4 kg edellisestä"), notifier.posted.single().text)
        assertTrue(flags.backupDue)
        assertEquals(1, changed)
        assertEquals(0, queue.count())
    }

    @Test
    fun `a re-broadcast with a new company id after a delete is not saved`() = runTest {
        val auto = auto()
        auto.decided(decided(82.4))
        auto.flush(AutoWeights.WEIGHIN)
        val id = scaleClientId(morning)
        auto.delete(id, morning.toEpochMilli(), 82.4, false)
        auto.decided(decided(82.4, morning.plusSeconds(20), key = 0x62C0))
        assertEquals(0, auto.pending())
        assertTrue(writer.saved.isEmpty())
    }

    @Test
    fun `a far weight and an unknown reference are marked`() = runTest {
        val auto = auto()
        auto.decided(decided(82.5))
        auto.flush(AutoWeights.WEIGHIN)
        assertEquals(listOf(scaleClientId(morning)), marks.all().map { it.id })
        assertTrue(notifier.posted.single().questioned)
        assertEquals("82,5 kg tallennettu", notifier.posted.single().text.title)
        reference.seed(listOf(RefPair("r", 0, 82.0)))
        auto.decided(decided(61.3, morning.plusSeconds(600), key = 0x62C0))
        auto.flush(AutoWeights.WEIGHIN)
        assertEquals(2, marks.all().size)
        assertEquals("61,3 kg – tarkista", notifier.posted.last().text.title)
    }

    @Test
    fun `nothing is saved while the dialog is in front`() = runTest {
        dialog = true
        val auto = auto()
        auto.decided(decided(82.5))
        assertEquals(0, auto.flush(AutoWeights.WEIGHIN))
        assertTrue(writer.saved.isEmpty())
        // The key was not claimed, so the dialog can save the weighing.
        assertTrue(keys.claim(0x61C0))
    }

    @Test
    fun `a failed write is queued and retried later as a late save, once`() = runTest {
        reference.seed(emptyList())
        writer.fail = 1
        val auto = auto()
        auto.decided(decided(82.5))
        assertEquals(0, auto.flush(AutoWeights.WEIGHIN))
        assertEquals(1, auto.pending())
        assertTrue(notifier.posted.isEmpty())
        // The key stays claimed: the scale's repeat is not queued twice.
        auto.decided(decided(82.5, morning.plusSeconds(2)))
        assertEquals(1, auto.pending())
        assertEquals(1, auto.flush("app"))
        assertEquals(0, auto.pending())
        assertEquals(1, writer.saved.size)
        assertEquals("82,5 kg tallennettu klo 7.12", notifier.posted.single().text.title)
        assertEquals(0, auto.flush("app"))
    }

    @Test
    fun `the time budget gives up and queues a slow write`() = runTest {
        writer.slowMs = 20_000
        val auto = auto { testScheduler.currentTime }
        auto.decided(decided(82.5))
        val result = async { auto.flush(AutoWeights.WEIGHIN) }
        assertEquals(0, result.await())
        assertTrue(testScheduler.currentTime in AutoWeights.BUDGET_MS until 9_000)
        assertEquals(1, queue.all().single().attempts)
        writer.slowMs = 0
        assertEquals(1, auto.flush("check"))
    }

    @Test
    fun `accept takes the chain and closes the notifications`() = runTest {
        val auto = auto()
        val day = 86_400_000L
        val base = morning.toEpochMilli()
        marks.add(listOf(Mark("a", base, 85.0), Mark("b", base + day, 84.2), Mark("c", base + 2 * day, 83.9), Mark("d", base + 3 * day, 61.3)))
        reference.seed(listOf(RefPair("old", 0, 92.0)))
        assertEquals(listOf("a", "b", "c"), auto.accept("a", AutoWeights.FROM_NOTIFICATION))
        assertEquals(listOf("d"), marks.all().map { it.id })
        assertEquals(listOf("a", "b", "c"), notifier.cancelled)
        assertEquals(83.9, (reference.reference(base + 3 * day) as Reference.Weight).kg)
        assertTrue(flags.backupDue)
        assertEquals(emptyList(), auto.accept("zzz", AutoWeights.FROM_HISTORIA))
    }

    @Test
    fun `delete removes the weight, raw data and mark, a failure re-posts`() = runTest {
        val auto = auto()
        auto.decided(decided(61.3))
        auto.flush(AutoWeights.WEIGHIN)
        val id = scaleClientId(morning)
        writer.failDelete = true
        assertFalse(auto.delete(id, morning.toEpochMilli(), 61.3, true))
        assertEquals(AutoText.DELETE_FAILED, notifier.posted.last().text.text)
        assertEquals(1, marks.all().size)
        writer.failDelete = false
        assertTrue(auto.delete(id, morning.toEpochMilli(), 61.3, true))
        assertTrue(writer.saved.isEmpty())
        assertNull(raw.get(morning.epochSecond))
        assertTrue(marks.all().isEmpty())
        assertEquals(listOf(id), notifier.cancelled)
    }

    @Test
    fun `stores keep their content in files`() {
        val marks = MarkStore(folder.root.resolve("marks.json"))
        // Two own weights in the same second: only the marked id is questioned.
        marks.add(listOf(Mark("terveyslappu-scale-1", 1_000, 61.3)))
        assertEquals(setOf("terveyslappu-scale-1"), MarkStore(folder.root.resolve("marks.json")).ids())
        assertEquals(listOf(Mark("terveyslappu-scale-1", 1_000, 61.3)), marks.remove(listOf("terveyslappu-scale-1")))
        marks.add(listOf(Mark("x", 1, 60.0), Mark("y", 2, 61.0)))
        assertEquals(1, marks.retain(setOf("y")))
        assertEquals(setOf("y"), marks.ids())

        val ref = ReferenceStore(folder.root.resolve("ref.json"))
        assertEquals(Reference.Unknown, ref.reference(10))
        ref.seed(listOf(RefPair("a", 1, 82.0)))
        assertEquals(Reference.Weight(82.0, 1), ReferenceStore(folder.root.resolve("ref.json")).reference(10))

        val queue = PendingStore(folder.root.resolve("q.json"))
        val w = PendingWeight(82.5, 5_000, 7200, 0x61C0, "00")
        queue.add(w)
        queue.add(w.copy(kg = 82.6, timeMs = 5_400))
        assertEquals(1, queue.count())
        queue.failed(w)
        assertEquals(1, PendingStore(folder.root.resolve("q.json")).all().single().attempts)
        queue.remove(w)
        assertEquals(0, queue.count())

        val flags = FlagStore(folder.root.resolve("f.json"))
        flags.backupDue = true
        assertTrue(FlagStore(folder.root.resolve("f.json")).backupDue)
        assertTrue(flags.takeBackupDue())
        assertFalse(FlagStore(folder.root.resolve("f.json")).backupDue)
    }

    @Test
    fun `a broken store file reads as empty`() {
        val file = folder.root.resolve("marks.json").apply { writeText("{not json") }
        assertTrue(MarkStore(file).all().isEmpty())
    }

    @Test
    fun `two simultaneous claimers, exactly one wins`() {
        val store = MemoryLastWeighIn()
        val pool = Executors.newFixedThreadPool(8)
        val start = CountDownLatch(1)
        val wins = AtomicInteger()
        val done = CountDownLatch(8)
        repeat(8) {
            pool.execute {
                start.await()
                if (WeighingKeys(store) { morning }.claim(0x61C0)) wins.incrementAndGet()
                done.countDown()
            }
        }
        start.countDown()
        done.await()
        pool.shutdown()
        assertEquals(1, wins.get())
        assertTrue(WeighingKeys(store) { morning.plusSeconds(180) }.claim(0x61C0))
    }

    @Test
    fun `the weights-changed signal reaches a collector`() = runTest {
        val signal = WeightsSignal()
        val got = async { kotlinx.coroutines.withTimeout(1_000) { signal.changes.first() } }
        testScheduler.runCurrent()
        signal.emit()
        assertEquals(Unit, got.await())
    }
}
