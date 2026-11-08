package fi.jukkakot.terveyslappu.scale

import kotlin.math.roundToInt

enum class ScaleUnit { KG, JIN, LB, ST_LB }

/**
 * One decoded reading of the Prego P814B (`Yoda1`, OKOK "C0" layout): weight in [kg] (converted
 * from the unit the scale shows), the scale's own [stable] flag and [impedanceOhm] (0 = none).
 * Layout from openspec/context/product.md: bytes 0-1 weight big-endian, 2-3 impedance /10,
 * byte 6 attributes (bit 0 stable, bits 1-2 decimals, bits 3-4 unit).
 */
data class ScaleReading(val kg: Double, val stable: Boolean, val unit: ScaleUnit, val impedanceOhm: Double)

object ScalePacket {
    private const val MIN_LENGTH = 13
    private const val IDX_ATTRIB = 6
    private const val LB_IN_KG = 0.45359237
    private const val STONE_IN_KG = 6.35029318

    /** True when an advertisement looks like the scale: Yoda name (or none) and a 0xC0 company id. */
    fun isScale(name: String?, companyId: Int): Boolean =
        (companyId and 0xFF) == 0xC0 && (name.isNullOrEmpty() || name.startsWith("Yoda", ignoreCase = true))

    /** Decodes [data] (manufacturer data after the company id), or null when it is too short. */
    fun decode(data: ByteArray): ScaleReading? {
        if (data.size < MIN_LENGTH) return null
        val attrib = data[IDX_ATTRIB].toInt() and 0xFF
        val stable = attrib and 0x01 != 0
        val divider = when ((attrib shr 1) and 0x3) {
            1 -> 1.0
            2 -> 100.0
            else -> 10.0
        }
        val unit = ScaleUnit.entries[(attrib shr 3) and 0x3]
        val raw = u16(data[0], data[1])
        val kg = when (unit) {
            ScaleUnit.KG -> raw / divider
            ScaleUnit.JIN -> raw / divider / 2
            ScaleUnit.LB -> raw / divider * LB_IN_KG
            ScaleUnit.ST_LB -> (data[0].toInt() and 0xFF) * STONE_IN_KG + (data[1].toInt() and 0xFF) / divider * LB_IN_KG
        }
        return ScaleReading(round2(kg), stable, unit, u16(data[2], data[3]) / 10.0)
    }

    private fun u16(msb: Byte, lsb: Byte): Int = ((msb.toInt() and 0xFF) shl 8) or (lsb.toInt() and 0xFF)

    private fun round2(value: Double): Double = (value * 100).roundToInt() / 100.0
}

/** Lower-case hex without separators, as the log writes bytes. */
fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it.toInt() and 0xFF) }

fun String.hexToBytes(): ByteArray {
    require(length % 2 == 0) { "odd hex length" }
    return ByteArray(length / 2) { substring(it * 2, it * 2 + 2).toInt(16).toByte() }
}
