package fi.jukkakot.terveyslappu.listen

import fi.jukkakot.terveyslappu.scale.KnownScale
import fi.jukkakot.terveyslappu.scale.RawPacket
import fi.jukkakot.terveyslappu.scale.ScalePacketTest.Companion.packet
import org.junit.Test
import java.time.LocalTime
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The background's decision timing (background design 2) with made-up packets (NFR-1). */
class BackgroundDecisionTest {
    private val scale = KnownScale("00:11:22:33:44:55", "Yoda1")
    private val session = ListenSession(0, 4 * 3_600_000L, 1_000_000L, 0, 80, "save")

    private fun heard(atMs: Long, raw: Int, stable: Boolean, key: Int = 0x12C0) =
        RawPacket(atMs, key, packet(raw, if (stable) 1 else 0), -80, "Yoda1", scale.address)

    private fun feed(packets: List<RawPacket>, from: ListenSession = session): Pair<ListenSession, List<HeardEvent.Decided>> {
        var s = from
        val decided = mutableListOf<HeardEvent.Decided>()
        for (p in packets) {
            val (next, events) = HeardPackets.onPacket(s, p, scale)
            s = next
            decided += events.filterIsInstance<HeardEvent.Decided>()
        }
        return s to decided
    }

    @Test
    fun `the first stable packet decides, timed at itself`() {
        val (s, decided) = feed(listOf(heard(100, 0, false), heard(300, 825, true), heard(400, 825, true), heard(500, 826, true)))
        assertEquals(1, decided.size)
        assertEquals(82.5, decided[0].kg)
        assertEquals(session.startedMs + 300, decided[0].atWallMs)
        assertEquals(0x12C0, decided[0].companyId)
        assertEquals(0x12C0, s.decidedKey)
    }

    @Test
    fun `a lone stable packet decides without any later delivery`() {
        val (_, decided) = feed(listOf(heard(1_000, 790, true, key = 0x61C0)))
        assertEquals(79.0, decided.single().kg)
        assertEquals(0x61C0, decided.single().companyId)
    }

    @Test
    fun `unstable and zero packets decide nothing`() {
        val (_, decided) = feed(listOf(heard(100, 0, false), heard(200, 800, false)))
        assertTrue(decided.isEmpty())
    }

    @Test
    fun `a new company id with the same final reading is a new packet for the session, the key rule lives in WeighingKeys`() {
        val (_, decided) = feed(listOf(heard(100, 825, true), heard(5_000, 825, true, key = 0x13C0)))
        assertEquals(listOf(82.5, 82.5), decided.map { it.kg })
    }

    @Test
    fun `out of range stable weights are never decided`() {
        val (_, decided) = feed(listOf(heard(100, 150, true), heard(200, 150, true)))
        assertTrue(decided.isEmpty())
        val (_, events2) = HeardPackets.onPacket(session, heard(100, 150, true), scale)
        assertTrue(events2.any { it is HeardEvent.OutOfRange })
        val (_, events) = HeardPackets.onPacket(session, heard(100, 3_100, true), scale)
        assertTrue(events.any { it is HeardEvent.OutOfRange })
    }
}

class WidgetStateTest {
    private val facts = ListenFacts(
        enabled = true, hasScale = true, permission = true, bluetoothOn = true, powerSave = false,
        listenInPowerSave = true, armed = true, inside = true, nextStart = LocalTime.of(6, 0),
    )

    private fun state(f: ListenFacts) = WidgetStateRule.of(ListenStatusRule.status(f), f.inside)

    @Test
    fun `the widget follows the status`() {
        assertNull(state(facts.copy(enabled = false)))
        assertEquals(WidgetState.ON, state(facts))
        assertEquals(WidgetState.RESTING, state(facts.copy(inside = false)))
        // Outside the window always Lepää, also with Bluetooth off.
        assertEquals(WidgetState.RESTING, state(facts.copy(inside = false, bluetoothOn = false)))
        assertEquals(WidgetState.BLOCKED, state(facts.copy(bluetoothOn = false)))
        assertEquals(WidgetState.BLOCKED, state(facts.copy(healthWrite = false)))
        assertEquals(ListenStatus.HealthMissing, ListenStatusRule.status(facts.copy(healthWrite = false)))
        assertEquals(WidgetState.BLOCKED, state(facts.copy(powerSave = true, listenInPowerSave = false)))
        assertEquals(WidgetState.BLOCKED, state(facts.copy(armed = false)))
        assertEquals(WidgetState.BLOCKED, state(facts.copy(permission = false)))
    }

    @Test
    fun `a stored ON becomes RESTING once the window end has passed`() {
        assertEquals(WidgetState.RESTING, WidgetStateRule.shown(WidgetState.ON, 1_000, 1_000))
        assertEquals(WidgetState.ON, WidgetStateRule.shown(WidgetState.ON, 1_000, 999))
        assertEquals(WidgetState.BLOCKED, WidgetStateRule.shown(WidgetState.BLOCKED, 1_000, 2_000))
        assertNull(WidgetStateRule.shown(null, null, 0))
    }
}
