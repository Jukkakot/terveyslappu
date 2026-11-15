package fi.jukkakot.terveyslappu.burn

import fi.jukkakot.terveyslappu.steps.GarminDay
import fi.jukkakot.terveyslappu.steps.GarminMerge
import fi.jukkakot.terveyslappu.steps.StepsAccess
import fi.jukkakot.terveyslappu.steps.StepsLogger
import fi.jukkakot.terveyslappu.steps.StepsSourceLabel
import fi.jukkakot.terveyslappu.steps.StepsSourceState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Made-up kcal only (NFR-1). */
@OptIn(ExperimentalCoroutinesApi::class)
class BurnControllerTest {
    private val day = LocalDate.of(2026, 11, 15)
    private val garminSource = StepsSourceState(setOf(GarminMerge.GARMIN), "garmin", StepsSourceLabel.App(GarminMerge.GARMIN, false))
    private val phoneSource = StepsSourceState(setOf("android"), "phone", StepsSourceLabel.App("phone", false))

    private inner class Fake : BurnSource {
        var grant = BurnGrant.BOTH
        var unavailable = false
        var fails = false
        val calls = mutableListOf<String>()
        var data: List<BurnRaw> = listOf(
            BurnRaw(day.minusDays(2), 2_300, 450),
            BurnRaw(day.minusDays(1), 2_200, null),
            BurnRaw(day, 300, 400),
        )

        override suspend fun granted(): BurnGrant {
            if (unavailable) throw IllegalStateException("no hc")
            return grant
        }

        override suspend fun firstDay(packages: Set<String>?): LocalDate = day.minusDays(20)

        override suspend fun daily(from: LocalDate, to: LocalDate, packages: Set<String>?): List<BurnRaw> {
            calls += "daily:${packages?.sorted()?.joinToString(",") ?: "all"}"
            if (fails) throw IllegalStateException("refused")
            return data.filter { it.day in from..to }
        }
    }

    private val lines = mutableListOf<String>()
    private val logger = StepsLogger { _, event, message, _, fields -> lines += "$event $message ${fields.joinToString(" ") { "${it.first}=${it.second}" }}" }
    private var source = garminSource
    private var imported: List<GarminDay> = emptyList()

    private fun TestScope.controller(fake: Fake) =
        BurnController(fake, backgroundScope, logger, stepsSource = { source }, today = { day }, garmin = { imported })

    @Test
    fun `reads the steps source, logs counts and the invalid day, never kcal`() = runTest {
        val fake = Fake()
        val c = controller(fake)
        c.show(day.minusDays(6))
        runCurrent()
        assertEquals(StepsAccess.GRANTED, c.state.value.access)
        assertEquals(listOf("daily:${GarminMerge.GARMIN}"), fake.calls)
        assertEquals(listOf(BurnKind.SPLIT, BurnKind.TOTAL, BurnKind.SPLIT), c.state.value.days!!.map { it.kind })
        val line = lines.single { it.startsWith("burn.read ok") }
        assertTrue("days=3 split=2 totalOnly=1 activeOnly=0 imported=0 invalid=1 back=6" in line, line)
        assertFalse("2300" in line || "2 300" in line)
    }

    @Test
    fun `a partial grant counts as not granted, asked then denied`() = runTest {
        val fake = Fake().apply { grant = BurnGrant.TOTAL }
        val c = controller(fake)
        c.show(null)
        runCurrent()
        assertEquals(StepsAccess.NEEDS, c.state.value.access)
        assertTrue(fake.calls.isEmpty())
        c.onPermissionRequested()
        c.onPermissionResult(BurnGrant.TOTAL)
        runCurrent()
        assertEquals(StepsAccess.DENIED, c.state.value.access)
        assertTrue(lines.any { it.startsWith("hc.permission.burn denied") && "granted=total" in it })
        fake.grant = BurnGrant.BOTH
        c.onPermissionResult(BurnGrant.BOTH)
        runCurrent()
        assertEquals(StepsAccess.GRANTED, c.state.value.access)
        assertTrue(c.state.value.days!!.isNotEmpty())
    }

    @Test
    fun `Health Connect unavailable`() = runTest {
        val c = controller(Fake().apply { unavailable = true })
        c.show(null)
        runCurrent()
        assertEquals(StepsAccess.UNAVAILABLE, c.state.value.access)
    }

    @Test
    fun `a failed read before data says so, after data goes stale`() = runTest {
        val fake = Fake().apply { fails = true }
        val c = controller(fake)
        c.show(null)
        runCurrent()
        assertTrue(c.state.value.loadFailed)
        assertNull(c.state.value.days)
        fake.fails = false
        c.refresh()
        runCurrent()
        assertFalse(c.state.value.loadFailed)
        fake.fails = true
        c.refresh()
        runCurrent()
        assertTrue(c.state.value.stale)
        assertTrue(lines.any { it.startsWith("burn.read failed") && "cause=IllegalStateException" in it })
    }

    @Test
    fun `the import merges for Garmin, re-merges on change and drops out for another source`() = runTest {
        imported = listOf(GarminDay(day.minusDays(400), 6_000, 500, 1_800), GarminDay(day.minusDays(1), 6_000, 500, 1_800))
        val fake = Fake()
        val c = controller(fake)
        c.show(null)
        runCurrent()
        assertEquals(day.minusDays(400), c.state.value.days!!.first().day)
        assertEquals(BurnKind.SPLIT, c.state.value.days!!.first { it.day == day.minusDays(1) }.kind)
        assertTrue(lines.any { it.startsWith("burn.read ok") && "imported=2" in it })
        imported = emptyList()
        c.garminChanged()
        assertEquals(day.minusDays(2), c.state.value.days!!.first().day)
        imported = listOf(GarminDay(day.minusDays(400), 6_000, 500, 1_800))
        fake.calls.clear()
        source = phoneSource
        c.sourceChanged()
        runCurrent()
        assertEquals(listOf("daily:android"), fake.calls)
        assertEquals(day.minusDays(2), c.state.value.days!!.first().day)
        assertEquals("phone", c.state.value.source.key)
    }
}
