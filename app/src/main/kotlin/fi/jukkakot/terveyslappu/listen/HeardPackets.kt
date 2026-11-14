package fi.jukkakot.terveyslappu.listen

import fi.jukkakot.terveyslappu.scale.KnownScale
import fi.jukkakot.terveyslappu.scale.RawPacket
import fi.jukkakot.terveyslappu.scale.ScalePacket
import fi.jukkakot.terveyslappu.scale.ScaleReading
import fi.jukkakot.terveyslappu.scale.ScaleUnit
import fi.jukkakot.terveyslappu.scale.StableResult
import fi.jukkakot.terveyslappu.scale.StableWeight
import fi.jukkakot.terveyslappu.scale.hexToBytes
import fi.jukkakot.terveyslappu.scale.toHex
import fi.jukkakot.terveyslappu.weight.MAX_KG
import fi.jukkakot.terveyslappu.weight.MIN_KG
import kotlinx.serialization.Serializable

/**
 * One listening window's accumulator (design 6), kept in app-private preferences because the
 * process may die between deliveries. Times: wall ms; [startElapsedNanos] is
 * `elapsedRealtimeNanos` at `listen.start`, against which a scan result's timestamp gives `t=`.
 */
@Serializable
data class ListenSession(
    val windowStartMs: Long,
    val windowEndMs: Long,
    val startedMs: Long,
    val startElapsedNanos: Long,
    val batteryStart: Int,
    val mode: String,
    val lastPacket: String? = null,
    val repeatCount: Int = 0,
    val repeatRssiMin: Int = 0,
    val repeatRssiMax: Int = 0,
    val packets: Int = 0,
    val distinct: Int = 0,
    val rssiMin: Int? = null,
    val rssiMax: Int? = null,
    val firstMs: Long? = null,
    val lastMs: Long? = null,
    val stable: List<Double> = emptyList(),
    val pendingKg: Double? = null,
    val pendingAtMs: Long = 0,
    val decidedKey: Int? = null,
    /** The pending stable reading's packet (company id, data as hex), so a later decision keeps its raw data. */
    val pendingKey: Int? = null,
    val pendingData: String? = null,
)

/** What a heard packet asks the log (and the save) for (design 10). */
sealed interface HeardEvent {
    /** A new packet from the scale: logged raw (`scale.packet ... src=bg`). */
    data class Raw(val packet: RawPacket) : HeardEvent
    /** The run of packets identical to the previous one ended: `listen.repeat`. */
    data class Repeat(val count: Int, val rssiMin: Int, val rssiMax: Int) : HeardEvent
    /** A packet from another address (same name filter): `listen.other`, not counted. */
    data class Other(val address: String?, val name: String?, val rssi: Int) : HeardEvent
    /**
     * A stable weight was decided (background-weigh-in "Save the automatic weight"): [atWallMs] is
     * the deciding reading's time, [companyId] and [data] its packet.
     */
    class Decided(val kg: Double, val atWallMs: Long, val companyId: Int, val data: ByteArray) : HeardEvent
    /** A distinct stable reading outside 20-300 kg: never saved (`auto.skip reason=range`). */
    data class OutOfRange(val kg: Double) : HeardEvent
}

/**
 * Feeds packets into a [ListenSession] (design 6-7): only the remembered address counts; a packet
 * identical (company id + data) to the previous one is only counted, its run logged once when it
 * ends; stable weights come from the dialog's [StableWeight] rule fed with the packets' own
 * timestamps and ticked before every packet as the dialog's ticker would (background design 2),
 * once per weighing (the company id counts weighings, so the lingering broadcast of a decided one
 * is skipped). Pure.
 */
object HeardPackets {

    /** Ends a session: a pending stable reading is settled and a running repeat flushed. */
    fun close(session: ListenSession, nowMs: Long): Pair<ListenSession, List<HeardEvent>> {
        var s = session
        val events = mutableListOf<HeardEvent>()
        if (s.repeatCount > 0) {
            events += HeardEvent.Repeat(s.repeatCount, s.repeatRssiMin, s.repeatRssiMax)
            s = s.copy(repeatCount = 0)
        }
        val (ticked, decided) = tick(s, nowMs - s.startedMs)
        decided?.let { events += it }
        return ticked to events
    }

    /**
     * The dialog's tick at [atMs] (ms since the session start): a pending stable reading quiet for
     * [StableWeight.DEFAULT_QUIET_MS] is decided, still timed at its own packet. Runs before every
     * packet, at the hourly check and at the window end.
     */
    fun tick(s: ListenSession, atMs: Long): Pair<ListenSession, HeardEvent.Decided?> {
        val result = decision(s).onTick(atMs) ?: return s to null
        return decided(s, result)
    }

    fun onPacket(session: ListenSession, packet: RawPacket, scale: KnownScale): Pair<ListenSession, List<HeardEvent>> {
        if (!packet.address.equals(scale.address, ignoreCase = true)) {
            return session to listOf(HeardEvent.Other(packet.address, packet.name, packet.rssi))
        }
        val events = mutableListOf<HeardEvent>()
        val wallMs = session.startedMs + packet.atMs
        val id = "%04x".format(packet.companyId and 0xFFFF) + packet.data.toHex()
        var s = session.copy(
            packets = session.packets + 1,
            rssiMin = minOf(session.rssiMin ?: packet.rssi, packet.rssi),
            rssiMax = maxOf(session.rssiMax ?: packet.rssi, packet.rssi),
            firstMs = session.firstMs ?: wallMs,
            lastMs = wallMs,
        )
        val distinct = id != session.lastPacket
        s = if (!distinct) {
            val first = s.repeatCount == 0
            s.copy(
                repeatCount = s.repeatCount + 1,
                repeatRssiMin = if (first) packet.rssi else minOf(s.repeatRssiMin, packet.rssi),
                repeatRssiMax = if (first) packet.rssi else maxOf(s.repeatRssiMax, packet.rssi),
            )
        } else {
            if (s.repeatCount > 0) events += HeardEvent.Repeat(s.repeatCount, s.repeatRssiMin, s.repeatRssiMax)
            events += HeardEvent.Raw(packet)
            s.copy(lastPacket = id, distinct = s.distinct + 1, repeatCount = 0)
        }
        val (ticked, byTick) = tick(s, packet.atMs)
        byTick?.let { events += it }
        val (next, byPacket) = stable(ticked, packet, distinct)
        byPacket?.let { events += it }
        return next to events
    }

    private fun stable(s: ListenSession, packet: RawPacket, distinct: Boolean): Pair<ListenSession, HeardEvent?> {
        if (packet.companyId == s.decidedKey) return s to null
        val reading = ScalePacket.decode(packet.data) ?: return s to null
        val decision = decision(s)
        val result = decision.onReading(reading, packet.atMs, packet)
        if (result != null) return decided(s, result)
        val pending = decision.pending
        val next = s.copy(
            pendingKg = pending?.kg,
            pendingAtMs = pending?.atMs ?: 0,
            pendingKey = pending?.packet?.companyId,
            pendingData = pending?.packet?.data?.toHex(),
        )
        val outOfRange = reading.stable && reading.kg !in MIN_KG..MAX_KG
        return next to if (outOfRange && distinct) HeardEvent.OutOfRange(reading.kg) else null
    }

    private fun decided(s: ListenSession, result: StableResult): Pair<ListenSession, HeardEvent.Decided?> {
        val packet = result.packet
        val next = s.copy(
            stable = s.stable + result.kg,
            pendingKg = null,
            pendingKey = null,
            pendingData = null,
            decidedKey = packet?.companyId ?: s.decidedKey,
        )
        if (packet == null) return next to null
        return next to HeardEvent.Decided(result.kg, s.startedMs + result.atMs, packet.companyId, packet.data)
    }

    /** A [StableWeight] in the state the session saved: its pending stable reading (and packet) fed back in. */
    private fun decision(s: ListenSession): StableWeight = StableWeight().also { d ->
        s.pendingKg?.let { kg ->
            val packet = s.pendingKey?.let { key -> s.pendingData?.let { RawPacket(s.pendingAtMs, key, it.hexToBytes(), 0, null, null) } }
            d.onReading(ScaleReading(kg, true, ScaleUnit.KG, 0.0), s.pendingAtMs, packet)
        }
    }
}

/** When a saved session belongs to the running window and how an old one is closed (design 6). */
object SessionRule {
    private const val MISSED_END_MS = 3_600_000L

    /** True when [s] is this window's own: its end is still ahead and the phone has not rebooted since. */
    fun continues(s: ListenSession, nowMs: Long, nowElapsedNanos: Long): Boolean =
        s.windowEndMs > nowMs && nowElapsedNanos >= s.startElapsedNanos

    /** Outside the window: `end` when the end passed lately (alarm, settings), `stale` when it was missed. */
    fun closeReason(s: ListenSession, nowMs: Long): String = if (nowMs - s.windowEndMs > MISSED_END_MS) "stale" else "end"
}
