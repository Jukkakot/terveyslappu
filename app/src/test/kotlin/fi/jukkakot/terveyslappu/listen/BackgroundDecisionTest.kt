package fi.jukkakot.terveyslappu.listen

import fi.jukkakot.terveyslappu.scale.KnownScale
import fi.jukkakot.terveyslappu.scale.RawPacket
import fi.jukkakot.terveyslappu.scale.ScalePacket
import fi.jukkakot.terveyslappu.scale.ScalePacketTest.Companion.packet
import fi.jukkakot.terveyslappu.scale.StableWeight
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

    /** The dialog's rule on the same packets, ticked every 250 ms as its ticker does. */
    private fun dialog(packets: List<RawPacket>): Pair<Double, Long>? {
        val d = StableWeight()
        var t = 0L
        for (p in packets) {
            while (t + 250 <= p.atMs) {
                t += 250
                d.onTick(t)?.let { return it.kg to it.atMs }
            }
            d.onReading(ScalePacket.decode(p.data)!!, p.atMs, p)?.let { return it.kg to it.atMs }
        }
        return null
    }

    @Test
    fun `two equal stable packets decide at once, timed at the first`() {
        val packets = listOf(heard(100, 800, false), heard(300, 825, true), heard(400, 825, true), heard(500, 825, true))
        val (s, decided) = feed(packets)
        assertEquals(1, decided.size)
        assertEquals(82.5, decided[0].kg)
        assertEquals(session.startedMs + 300, decided[0].atWallMs)
        assertEquals(0x12C0, decided[0].companyId)
        assertEquals(dialog(packets), decided[0].kg to decided[0].atWallMs - session.startedMs)
        assertEquals(0x12C0, s.decidedKey)
    }

    @Test
    fun `dialog and background decide the same kg on a noisy stream`() {
        val packets = listOf(
            heard(100, 790, false), heard(200, 812, true), heard(300, 815, false), heard(400, 818, true),
            heard(3_000, 822, false), heard(3_100, 822, true), heard(3_150, 822, true),
        )
        val bg = feed(packets).second.single()
        val (kg, atMs) = dialog(packets)!!
        assertEquals(kg, bg.kg)
        assertEquals(atMs, bg.atWallMs - session.startedMs)
    }

    @Test
    fun `a lone stable packet is decided at the next delivery, the check or the end, timed at itself`() {
        val (pending, none) = feed(listOf(heard(1_000, 790, true)))
        assertTrue(none.isEmpty())
        // Next delivery 5 s later with another reading: the tick before it decides the lone one.
        val (_, next) = feed(listOf(heard(6_000, 500, false, key = 0x13C0)), pending)
        assertEquals(79.0, next.single().kg)
        assertEquals(session.startedMs + 1_000, next.single().atWallMs)
        // The hourly check.
        val (_, byCheck) = HeardPackets.tick(pending, 3_600_000)
        assertEquals(session.startedMs + 1_000, byCheck?.atWallMs)
        // The window end.
        val (_, closing) = HeardPackets.close(pending, pending.startedMs + 3_600_000)
        assertEquals(79.0, closing.filterIsInstance<HeardEvent.Decided>().single().kg)
    }

    @Test
    fun `the pending packet survives the stored session and gives the key`() {
        val (s, _) = feed(listOf(heard(1_000, 790, true, key = 0x61C0)))
        val restored = kotlinx.serialization.json.Json.decodeFromString<ListenSession>(kotlinx.serialization.json.Json.encodeToString(s))
        assertEquals(0x61C0, HeardPackets.tick(restored, 5_000).second?.companyId)
    }

    @Test
    fun `out of range stable weights are never decided`() {
        val (_, decided) = feed(listOf(heard(100, 150, true), heard(200, 150, true)))
        assertTrue(decided.isEmpty())
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
