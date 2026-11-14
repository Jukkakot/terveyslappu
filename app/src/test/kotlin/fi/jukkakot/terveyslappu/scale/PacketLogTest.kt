package fi.jukkakot.terveyslappu.scale

import fi.jukkakot.terveyslappu.log.Level
import fi.jukkakot.terveyslappu.log.LogLine
import fi.jukkakot.terveyslappu.scale.ScalePacketTest.Companion.packet
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PacketLogTest {

    private fun line(p: RawPacket) =
        LogLine.format(Instant.EPOCH, ZoneOffset.UTC, Level.DEBUG, PacketLog.EVENT, "", PacketLog.fields(p).toList())

    @Test
    fun `a packet survives the log line`() {
        val p = RawPacket(412, 0x12C0, packet(0x0339, 1), -61, "Yoda1", "AA:BB:CC:DD:EE:FF")
        assertEquals(p, PacketLog.parse(line(p)))
    }

    @Test
    fun `older lines without addr still read`() {
        val p = PacketLog.parse("2026-11-08T07:00:00.500+02:00 D scale.packet t=500 key=12c0 data=0339138b00000100000000000000 rssi=-59 name=Yoda1")!!
        assertEquals(null, p.address)
        assertEquals(500, p.atMs)
    }

    @Test
    fun `nameless packet`() {
        val p = RawPacket(5, 0x00C0, packet(0x0339, 1), -70, null)
        assertEquals(null, PacketLog.parse(line(p))!!.name)
    }

    @Test
    fun `other lines are skipped`() {
        assertNull(PacketLog.parse("2026-11-08T07:00:00.000+02:00 I app.start version=1"))
    }

    /** A made-up weigh-in in log form (weights invented, NFR-1), with another device in between. */
    @Test
    fun `replay of a logged weigh-in reaches the same weight`() {
        val lines = listOf(
            "2026-11-08T07:00:00.000+02:00 I scale.scan start",
            "2026-11-08T07:00:00.100+02:00 D scale.packet t=100 key=12c0 data=02ee000000000000000000000000 rssi=-60 name=Yoda1",
            "2026-11-08T07:00:00.150+02:00 D scale.packet t=150 key=004c data=0102030405060708090a0b0c0d rssi=-80 name=-",
            "2026-11-08T07:00:00.300+02:00 D scale.packet t=300 key=12c0 data=0320000000000000000000000000 rssi=-60 name=Yoda1",
            "2026-11-08T07:00:00.500+02:00 D scale.packet t=500 key=12c0 data=0339138b00000100000000000000 rssi=-59 name=Yoda1",
            "2026-11-08T07:00:00.700+02:00 D scale.packet t=700 key=12c0 data=0339138b00000100000000000000 rssi=-59 name=Yoda1",
        )
        val result = PacketLog.replay(lines)!!
        assertEquals(82.5 to 500L, result.kg to result.atMs)
        // The second stable packet decides; it is the one kept raw.
        assertEquals(700L, result.packet!!.atMs)
    }

    @Test
    fun `replay skips the background trial's packets`() {
        val bg = "2026-11-08T07:00:00.500+02:00 D scale.packet t=500 key=12c0 data=0339138b00000100000000000000 rssi=-95 name=Yoda1 addr=00:11:22:33:44:55 src=bg screen=off"
        assertNull(PacketLog.replay(listOf(bg, bg.replace("t=500", "t=700"))))
    }
}
