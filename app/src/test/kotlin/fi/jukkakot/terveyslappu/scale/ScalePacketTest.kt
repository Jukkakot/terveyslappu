package fi.jukkakot.terveyslappu.scale

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Made-up packets in the scale's layout (NFR-1: no real weights). */
class ScalePacketTest {

    @Test
    fun `stable kilograms with one decimal`() {
        val reading = ScalePacket.decode(packet(raw = 0x0339, attrib = 0b0_0001))!!
        assertEquals(82.5, reading.kg)
        assertTrue(reading.stable)
        assertEquals(ScaleUnit.KG, reading.unit)
    }

    @Test
    fun `unstable flag and impedance`() {
        val reading = ScalePacket.decode(packet(raw = 0x0300, attrib = 0, impedance = 5123))!!
        assertFalse(reading.stable)
        assertEquals(76.8, reading.kg)
        assertEquals(512.3, reading.impedanceOhm)
    }

    @Test
    fun `two decimals`() {
        // bits 1-2 = 2 -> divide by 100
        assertEquals(82.45, ScalePacket.decode(packet(raw = 8245, attrib = 0b0_0101))!!.kg)
    }

    @Test
    fun `pounds are converted to kilograms`() {
        // 181.9 lb, unit bits 3-4 = 2
        val reading = ScalePacket.decode(packet(raw = 1819, attrib = 0b1_0001))!!
        assertEquals(ScaleUnit.LB, reading.unit)
        assertEquals(82.51, reading.kg)
    }

    @Test
    fun `jin is half a kilogram`() {
        assertEquals(82.5, ScalePacket.decode(packet(raw = 1650, attrib = 0b0_1001))!!.kg)
    }

    @Test
    fun `stones and pounds`() {
        // 13 st 0.0 lb = 82.55 kg; byte 0 stones, byte 1 pounds*10
        assertEquals(82.55, ScalePacket.decode(packet(raw = (13 shl 8) or 0, attrib = 0b1_1001))!!.kg)
    }

    @Test
    fun `short data is ignored`() {
        assertNull(ScalePacket.decode(ByteArray(8)))
    }

    @Test
    fun `scale is recognised by company id low byte and name`() {
        assertTrue(ScalePacket.isScale("Yoda1", 0x12C0))
        assertTrue(ScalePacket.isScale(null, 0x00C0))
        assertFalse(ScalePacket.isScale("Yoda1", 0x004C))
        assertFalse(ScalePacket.isScale("Galaxy Buds", 0x01C0))
    }

    companion object {
        fun packet(raw: Int, attrib: Int, impedance: Int = 0): ByteArray = ByteArray(13).also {
            it[0] = (raw shr 8).toByte()
            it[1] = raw.toByte()
            it[2] = (impedance shr 8).toByte()
            it[3] = impedance.toByte()
            it[6] = attrib.toByte()
        }
    }
}
