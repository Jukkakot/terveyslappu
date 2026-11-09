package fi.jukkakot.terveyslappu.scale

/**
 * One advertisement as received: ms since the scan started, company id, payload, signal, name and
 * the advertiser's Bluetooth [address] (null in old logs).
 */
data class RawPacket(
    val atMs: Long,
    val companyId: Int,
    val data: ByteArray,
    val rssi: Int,
    val name: String?,
    val address: String? = null,
) {
    override fun equals(other: Any?): Boolean =
        other is RawPacket && atMs == other.atMs && companyId == other.companyId && data.contentEquals(other.data) &&
            rssi == other.rssi && name == other.name && address == other.address

    override fun hashCode(): Int = (atMs * 31 + companyId).toInt() * 31 + data.contentHashCode()
}

/**
 * The `scale.packet` log line (NFR-7): `... D scale.packet t=412 key=12c0 data=0339... rssi=-61
 * name=Yoda1 addr=AA:BB:CC:DD:EE:FF`
 * (`addr` missing in older logs). [fields] writes it, [parse] reads it back from a shared log for replay tests.
 */
object PacketLog {
    const val EVENT = "scale.packet"

    fun fields(packet: RawPacket): Array<Pair<String, Any?>> = arrayOf(
        "t" to packet.atMs,
        "key" to "%04x".format(packet.companyId and 0xFFFF),
        "data" to packet.data.toHex(),
        "rssi" to packet.rssi,
        "name" to (packet.name?.ifEmpty { null }?.replace(' ', '_') ?: "-"),
        "addr" to (packet.address ?: "-"),
    )

    /** The packet of a log line, or null when the line is not a `scale.packet` line. */
    fun parse(line: String): RawPacket? {
        val parts = line.trim().split(' ')
        if (parts.size < 3 || parts[2] != EVENT) return null
        val values = parts.drop(3).mapNotNull { part ->
            val eq = part.indexOf('=')
            if (eq > 0) part.substring(0, eq) to part.substring(eq + 1) else null
        }.toMap()
        return RawPacket(
            atMs = values["t"]?.toLongOrNull() ?: return null,
            companyId = values["key"]?.toIntOrNull(16) ?: return null,
            data = values["data"]?.hexToBytes() ?: return null,
            rssi = values["rssi"]?.toIntOrNull() ?: 0,
            name = values["name"]?.takeIf { it != "-" },
            address = values["addr"]?.takeIf { it != "-" },
        )
    }

    /** Runs the weigh-in decision over a log's packet lines, as the phone did. */
    fun replay(lines: List<String>, decision: StableWeight = StableWeight()): StableResult? {
        for (packet in lines.mapNotNull(::parse)) {
            if (!ScalePacket.isScale(packet.name, packet.companyId)) continue
            decision.onTick(packet.atMs)?.let { return it }
            val reading = ScalePacket.decode(packet.data) ?: continue
            decision.onReading(reading, packet.atMs, packet)?.let { return it }
        }
        return decision.result
    }
}
