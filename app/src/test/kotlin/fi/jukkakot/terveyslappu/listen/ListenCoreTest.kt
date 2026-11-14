package fi.jukkakot.terveyslappu.listen

import fi.jukkakot.terveyslappu.scale.KnownScale
import fi.jukkakot.terveyslappu.scale.RawPacket
import fi.jukkakot.terveyslappu.scale.ScalePacketTest.Companion.packet
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ListenCoreTest {
    private val zone = ZoneId.of("Europe/Helsinki")
    private fun at(text: String): Instant = LocalDateTime.parse(text).atZone(zone).toInstant()
    private fun window(start: String, end: String) = ListenWindow(LocalTime.parse(start), LocalTime.parse(end))

    @Test
    fun `morning window plans the start, the hourly check and the end`() {
        val w = window("06:00", "10:00")
        assertEquals(WindowPlan.Outside(at("2026-11-14T06:00")), w.plan(at("2026-11-14T05:00"), zone))
        assertEquals(
            WindowPlan.Inside(at("2026-11-14T06:00"), at("2026-11-14T10:00"), at("2026-11-14T08:12")),
            w.plan(at("2026-11-14T07:12"), zone),
        )
        // The last check is the end.
        assertEquals(at("2026-11-14T10:00"), (w.plan(at("2026-11-14T09:30"), zone) as WindowPlan.Inside).nextCheck)
        assertEquals(WindowPlan.Outside(at("2026-11-15T06:00")), w.plan(at("2026-11-14T10:00"), zone))
    }

    @Test
    fun `window over midnight runs into the next day`() {
        val w = window("22:00", "02:00")
        val plan = w.plan(at("2026-11-15T01:00"), zone)
        assertIs<WindowPlan.Inside>(plan)
        assertEquals(at("2026-11-14T22:00"), plan.start)
        assertEquals(at("2026-11-15T02:00"), plan.end)
        assertEquals(WindowPlan.Outside(at("2026-11-15T22:00")), w.plan(at("2026-11-15T03:00"), zone))
    }

    @Test
    fun `same start and end is refused`() {
        assertFailsWith<IllegalArgumentException> { window("06:00", "06:00") }
        assertTrue(!ListenWindow.valid(LocalTime.of(7, 0), LocalTime.of(7, 0)))
    }

    @Test
    fun `DST days keep the local times`() {
        // Spring forward 2026-03-29 03:00 -> 04:00 in Helsinki: a 03:30 start moves to 04:30.
        val gap = window("03:30", "06:00")
        assertEquals(WindowPlan.Outside(at("2026-03-29T04:30")), gap.plan(at("2026-03-29T01:00"), zone))
        // Fall back 2026-10-25: the window 06-10 is still four hours, at local time.
        val w = window("06:00", "10:00")
        val plan = w.plan(at("2026-10-25T07:00"), zone) as WindowPlan.Inside
        assertEquals(at("2026-10-25T10:00"), plan.end)
    }

    private val facts = ListenFacts(
        enabled = true, hasScale = true, permission = true, bluetoothOn = true, powerSave = false,
        listenInPowerSave = true, armed = true, inside = true, nextStart = LocalTime.of(6, 0),
    )

    @Test
    fun `status takes the first that applies`() {
        assertNull(ListenStatusRule.status(facts.copy(enabled = false)))
        assertEquals(ListenStatus.Listening, ListenStatusRule.status(facts))
        assertEquals(ListenStatus.Resting(LocalTime.of(6, 0)), ListenStatusRule.status(facts.copy(inside = false)))
        assertEquals(ListenStatus.PermissionMissing, ListenStatusRule.status(facts.copy(permission = false, bluetoothOn = false)))
        assertEquals(ListenStatus.BluetoothOff, ListenStatusRule.status(facts.copy(bluetoothOn = false, powerSave = true, listenInPowerSave = false)))
        assertEquals(ListenStatus.PowerSavePause, ListenStatusRule.status(facts.copy(powerSave = true, listenInPowerSave = false)))
        assertEquals(ListenStatus.Listening, ListenStatusRule.status(facts.copy(powerSave = true)))
        assertEquals(ListenStatus.Stopped, ListenStatusRule.status(facts.copy(armed = false)))
        assertEquals(ListenStatus.NoScale, ListenStatusRule.status(facts.copy(hasScale = false)))
    }

    private val scale = KnownScale("00:11:22:33:44:55", "Yoda1")
    private val session = ListenSession(0, 4 * 3_600_000L, 1_000_000L, 0, 80, "save")

    private fun heard(atMs: Long, raw: Int, stable: Boolean, rssi: Int = -90, key: Int = 0x12C0, addr: String = scale.address) =
        RawPacket(atMs, key, packet(raw, if (stable) 1 else 0), rssi, "Yoda1", addr)

    private fun feed(vararg packets: RawPacket): Pair<ListenSession, List<HeardEvent>> {
        var s = session
        val events = mutableListOf<HeardEvent>()
        for (p in packets) {
            val (next, e) = HeardPackets.onPacket(s, p, scale)
            s = next
            events += e
        }
        return s to events
    }

    @Test
    fun `lingering broadcast is logged once raw and once as a repeat count`() {
        val repeats = (1..20).map { heard(1_000L + it * 100, 825, true, rssi = -90 - it % 3) }
        var (s, events) = feed(heard(1_000, 825, true), *repeats.toTypedArray())
        val (closed, last) = HeardPackets.close(s, s.startedMs + 10_000)
        s = closed
        events = (events + last).filterNot { it is HeardEvent.Decided }
        assertEquals(2, events.size)
        assertIs<HeardEvent.Raw>(events[0])
        assertEquals(HeardEvent.Repeat(20, -92, -90), events[1])
        assertEquals(21, s.packets)
        assertEquals(1, s.distinct)
        assertEquals(listOf(82.5), s.stable)
        assertEquals(session.startedMs + 1_000, s.firstMs)
        assertEquals(session.startedMs + 3_000, s.lastMs)
    }

    @Test
    fun `another address with the same name is not counted`() {
        val (s, events) = feed(heard(1_000, 825, true, addr = "66:77:88:99:AA:BB"))
        assertEquals(0, s.packets)
        assertEquals(listOf<HeardEvent>(HeardEvent.Other("66:77:88:99:AA:BB", "Yoda1", -90)), events)
    }

    @Test
    fun `stable weights follow the dialog rule once per weighing`() {
        val (s, events) = feed(
            heard(100, 800, false),
            heard(300, 825, true),
            heard(400, 826, false),
            heard(500, 825, true),
            heard(600, 825, true, rssi = -80),
            heard(700, 825, true),
            heard(900, 825, true),
            heard(60_000, 819, true, key = 0x13C0),
            heard(60_100, 819, true, key = 0x13C0),
        )
        assertEquals(listOf(82.5, 81.9), s.stable)
        assertEquals(9, s.packets)
        assertEquals(-80, s.rssiMax)
        assertEquals(-90, s.rssiMin)
        assertEquals(HeardEvent.Repeat(3, -90, -80), events.filterIsInstance<HeardEvent.Repeat>().first())
    }

    @Test
    fun `a pending stable reading survives saving and settles at the end`() {
        val (s, _) = feed(heard(100, 790, true))
        val restored = kotlinx.serialization.json.Json.decodeFromString<ListenSession>(kotlinx.serialization.json.Json.encodeToString(s))
        assertEquals(listOf(79.0), HeardPackets.close(restored, restored.startedMs + 5_000).first.stable)
    }

    @Test
    fun `an unfinished window is closed as stale`() {
        assertTrue(SessionRule.continues(session, 3_600_000L, 10))
        // Rebooted: elapsed time went back.
        assertTrue(!SessionRule.continues(session.copy(startElapsedNanos = 50), 3_600_000L, 10))
        assertTrue(!SessionRule.continues(session, session.windowEndMs, 10))
        assertEquals("end", SessionRule.closeReason(session, session.windowEndMs + 60_000))
        assertEquals("stale", SessionRule.closeReason(session, session.windowEndMs + 2 * 3_600_000L))
    }

    @Test
    fun `last heard reads today, yesterday or a date`() {
        val now = at("2026-11-14T09:00")
        assertEquals("Ei vielä kuultu", LastHeardText.text(null, now, zone))
        assertEquals("Kuultu viimeksi tänään 7.12, signaali -97", LastHeardText.text(LastHeard(at("2026-11-14T07:12"), -97), now, zone))
        assertEquals("Kuultu viimeksi eilen 7.12, signaali -97", LastHeardText.text(LastHeard(at("2026-11-13T07:12"), -97), now, zone))
        assertEquals("Kuultu viimeksi 12.11. klo 7.12, signaali -97", LastHeardText.text(LastHeard(at("2026-11-12T07:12"), -97), now, zone))
    }
}
