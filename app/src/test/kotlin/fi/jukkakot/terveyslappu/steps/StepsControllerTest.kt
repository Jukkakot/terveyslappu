package fi.jukkakot.terveyslappu.steps

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Made-up step counts only (NFR-1). */
@OptIn(ExperimentalCoroutinesApi::class)
class StepsControllerTest {
    private val garmin = "com.garmin.android.apps.connectmobile"
    private var day = LocalDate.of(2026, 11, 15)
    private var now = Instant.parse("2026-11-15T08:00:00Z")

    private fun key(packages: Set<String>?) = packages?.sorted()?.joinToString(",") ?: "all"

    private inner class Fake : StepsSource {
        val calls = mutableListOf<String>()
        var totals: Map<String, Long> = mapOf(garmin to 240_000, "phone" to 90_000)
        var checkFails = false
        var dailyFails = false
        /** daily() waits for this gate of its source key when set. */
        val gates = mutableMapOf<String, CompletableDeferred<Unit>>()
        val data = mapOf(
            "all" to listOf(StepsDay(day, 9_111)),
            garmin to listOf(StepsDay(day, 7_222)),
            "android" to listOf(StepsDay(day, 3_333)),
        )

        override suspend fun granted() = true
        override suspend fun firstDay(packages: Set<String>?): LocalDate = day.minusDays(20)
        override suspend fun daily(from: LocalDate, to: LocalDate, packages: Set<String>?): List<StepsDay> {
            val k = key(packages)
            calls += "daily:$k"
            gates[k]?.await()
            if (dailyFails) throw IllegalStateException("refused")
            return data[k].orEmpty()
        }

        override suspend fun check(today: LocalDate, candidates: List<StepsApp>): StepsCheck {
            calls += "check"
            if (checkFails) throw IllegalStateException("quota")
            return StepsCheck(totals.map { (id, t) -> StepsAppTotal(StepsApp(id, if (id == "phone") setOf("android") else setOf(id)), t, 10, t / 10) }, 3, 1)
        }

        override suspend fun phonePackages() = setOf("android")
    }

    private val lines = mutableListOf<String>()
    private val logger = StepsLogger { _, event, message, _, fields -> lines += "$event $message ${fields.joinToString(" ") { "${it.first}=${it.second}" }}" }

    private fun TestScope.controller(source: Fake, store: StepsStore = MemoryStepsStore()) =
        StepsController(source, store, backgroundScope, logger, now = { now }, today = { day })

    @Test
    fun `first start checks once, then reads the pick`() = runTest {
        val source = Fake()
        val c = controller(source)
        c.show(day.minusDays(6))
        runCurrent()
        assertEquals(listOf("check", "daily:$garmin"), source.calls)
        assertEquals(StepsSourceLabel.Auto(garmin), c.state.value.source.label)
        assertEquals(7_222, c.state.value.days!!.single().steps)
        source.calls.clear()
        c.refresh()
        runCurrent()
        assertEquals(listOf("daily:$garmin"), source.calls)
    }

    @Test
    fun `the next day reads first and checks after, re-reading when the pick changed`() = runTest {
        val source = Fake()
        val store = MemoryStepsStore().apply { autoPick = garmin; autoPickDay = day.minusDays(1) }
        source.totals = mapOf(garmin to 100_000, "phone" to 150_000)
        val c = controller(source, store)
        c.show(day.minusDays(6))
        runCurrent()
        assertEquals(listOf("daily:$garmin", "check", "daily:android"), source.calls)
        assertEquals("phone", store.autoPick)
        assertEquals(day, store.autoPickDay)
        assertTrue(lines.any { it.startsWith("steps.source switch") && "from=$garmin" in it && "to=phone" in it })
    }

    @Test
    fun `a failed check keeps the earlier pick and is retried after an hour`() = runTest {
        val source = Fake().apply { checkFails = true }
        val store = MemoryStepsStore().apply { autoPick = garmin; autoPickDay = day.minusDays(1) }
        val c = controller(source, store)
        c.show(day.minusDays(6))
        runCurrent()
        assertEquals(listOf("daily:$garmin", "check"), source.calls)
        assertEquals(garmin, store.autoPick)
        assertEquals(day.minusDays(1), store.autoPickDay)
        assertTrue(c.sources.value.failed)
        source.calls.clear()
        now = now.plus(Duration.ofMinutes(30))
        c.refresh()
        runCurrent()
        assertEquals(listOf("daily:$garmin"), source.calls)
        now = now.plus(Duration.ofMinutes(45))
        source.checkFails = false
        c.refresh()
        runCurrent()
        assertTrue("check" in source.calls)
        assertTrue(lines.any { it.startsWith("steps.sources ok") && "reason=retry" in it })
    }

    @Test
    fun `the check runs under a chosen app and Kaikki and leaves the source alone`() = runTest {
        for (choice in listOf(StepsChoice.App("phone"), StepsChoice.All)) {
            val source = Fake()
            val store = MemoryStepsStore().apply { this.choice = choice }
            val c = controller(source, store)
            c.show(day.minusDays(6))
            runCurrent()
            assertEquals("check", source.calls[1])
            assertEquals(2, source.calls.size, "no re-read: the source did not change")
            assertEquals(choice, store.choice)
            assertEquals(garmin, store.autoPick)
        }
    }

    @Test
    fun `a chosen app's recent steps are unknown until today's check`() = runTest {
        val source = Fake().apply { totals = mapOf("phone" to 90_000) }
        val store = MemoryStepsStore().apply { choice = StepsChoice.App(garmin); autoPickDay = day }
        val c = controller(source, store)
        c.show(day.minusDays(6))
        runCurrent()
        assertEquals(StepsSourceLabel.App(garmin, null), c.state.value.source.label)
        c.subScreenShown()
        runCurrent()
        assertEquals(StepsSourceLabel.App(garmin, true), c.state.value.source.label)
        assertEquals(listOf("phone", garmin), c.sources.value.rows.map { it.id })
    }

    @Test
    fun `the sub-screen checks on entry but not twice within a minute`() = runTest {
        val source = Fake()
        val store = MemoryStepsStore().apply { autoPick = garmin; autoPickDay = day }
        val c = controller(source, store)
        c.refresh()
        runCurrent()
        c.subScreenShown()
        runCurrent()
        c.subScreenShown()
        runCurrent()
        assertEquals(listOf("check"), source.calls)
        now = now.plusSeconds(61)
        c.subScreenShown()
        runCurrent()
        assertEquals(listOf("check", "check"), source.calls)
    }

    @Test
    fun `setChoice cancels a running read and drops its result`() = runTest {
        val source = Fake()
        val store = MemoryStepsStore().apply { autoPick = garmin; autoPickDay = day }
        source.gates[garmin] = CompletableDeferred()
        val c = controller(source, store)
        c.show(day.minusDays(6))
        runCurrent()
        assertEquals(listOf("daily:$garmin"), source.calls)
        c.setChoice(StepsChoice.App("phone"))
        source.gates.getValue(garmin).complete(Unit)
        runCurrent()
        assertEquals(3_333, c.state.value.days!!.single().steps)
        assertEquals(StepsChoice.App("phone"), store.choice)
    }

    @Test
    fun `a quick double change shows only the last source`() = runTest {
        val source = Fake()
        val store = MemoryStepsStore().apply { autoPick = garmin; autoPickDay = day }
        val c = controller(source, store)
        c.show(day.minusDays(6))
        runCurrent()
        source.gates["android"] = CompletableDeferred()
        c.setChoice(StepsChoice.App("phone"))
        runCurrent()
        c.setChoice(StepsChoice.App(garmin))
        source.gates.getValue("android").complete(Unit)
        runCurrent()
        assertEquals(7_222, c.state.value.days!!.single().steps)
        assertEquals(StepsSourceLabel.App(garmin, null), c.state.value.source.label)
    }

    @Test
    fun `a failed read after a change is the failure state, not the old numbers`() = runTest {
        val source = Fake()
        val store = MemoryStepsStore().apply { autoPick = garmin; autoPickDay = day }
        val c = controller(source, store)
        c.show(day.minusDays(6))
        runCurrent()
        assertEquals(7_222, c.state.value.days!!.single().steps)
        source.dailyFails = true
        c.setChoice(StepsChoice.App("phone"))
        runCurrent()
        assertNull(c.state.value.days)
        assertTrue(c.state.value.loadFailed)
        assertFalse(c.state.value.stale)
    }

    @Test
    fun `nothing in 30 days reads all apps`() = runTest {
        val source = Fake().apply { totals = emptyMap() }
        val c = controller(source)
        c.show(day.minusDays(6))
        runCurrent()
        assertEquals(listOf("check", "daily:all"), source.calls)
        assertEquals(StepsSourceLabel.Auto(null), c.state.value.source.label)
    }

    @Test
    fun `the log holds no step numbers`() = runTest {
        val source = Fake()
        val c = controller(source)
        c.show(day.minusDays(6))
        runCurrent()
        c.setChoice(StepsChoice.App("phone"))
        runCurrent()
        assertTrue(lines.any { it.startsWith("steps.read ok") && "source=$garmin" in it })
        assertTrue(lines.any { it.startsWith("steps.read ok") && "source=phone" in it })
        assertTrue(lines.any { it.startsWith("steps.source ") && "choice=app" in it && "reason=user" in it })
        for (number in listOf("240000", "90000", "24000", "9000", "7222", "3333", "9111")) {
            assertFalse(lines.any { line -> line.split(' ', '=').any { it == number } }, "$number in ${lines.joinToString("\n")}")
        }
    }

    @Test
    fun `the Garmin import merges into the read and re-merges without a new read`() = runTest {
        val source = Fake()
        var imported = listOf(GarminDay(day.minusDays(400), 6_111))
        val store = MemoryStepsStore().apply { autoPick = garmin; autoPickDay = day }
        val c = StepsController(source, store, backgroundScope, logger, now = { now }, today = { day }, garmin = { imported })
        assertEquals(1, c.sources.value.garmin!!.count)
        c.show(null)
        runCurrent()
        assertEquals(listOf(day.minusDays(400), day), c.state.value.days!!.map { it.day })
        assertTrue(lines.any { it.startsWith("steps.read ok") && "days=1 " in it }, "steps.read counts Health Connect days only")
        source.calls.clear()
        imported = emptyList()
        c.garminChanged()
        assertEquals(listOf(day), c.state.value.days!!.map { it.day })
        assertNull(c.sources.value.garmin)
        assertTrue(source.calls.isEmpty())
        imported = listOf(GarminDay(day.minusDays(400), 6_111))
        c.setChoice(StepsChoice.App("phone"))
        runCurrent()
        c.garminChanged()
        assertEquals(listOf(day), c.state.value.days!!.map { it.day })
    }
}
