package fi.jukkakot.terveyslappu.listen

import fi.jukkakot.terveyslappu.scale.KnownScale
import fi.jukkakot.terveyslappu.scale.RawPacket
import fi.jukkakot.terveyslappu.scale.ScalePacket
import fi.jukkakot.terveyslappu.scale.ScaleReading
import fi.jukkakot.terveyslappu.scale.ScaleUnit
import fi.jukkakot.terveyslappu.scale.StableWeight
import fi.jukkakot.terveyslappu.scale.toHex
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
)

/** What a heard packet asks the log for (design 10). */
sealed interface HeardEvent {
    /** A new packet from the scale: logged raw (`scale.packet ... src=bg`). */
    data class Raw(val packet: RawPacket) : HeardEvent
    /** The run of packets identical to the previous one ended: `listen.repeat`. */
    data class Repeat(val count: Int, val rssiMin: Int, val rssiMax: Int) : HeardEvent
    /** A packet from another address (same name filter): `listen.other`, not counted. */
    data class Other(val address: String?, val name: String?, val rssi: Int) : HeardEvent
}

/**
 * Feeds packets into a [ListenSession] (design 6-7): only the remembered address counts; a packet
 * identical (company id + data) to the previous one is only counted, its run logged once when it
 * ends; stable weights come from the dialog's [StableWeight] rule, once per weighing (the company
 * id counts weighings, so the lingering broadcast of a decided one is skipped). Pure.
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
        val atMs = nowMs - s.startedMs
        decision(s).onTick(atMs)?.let { s = s.copy(stable = s.stable + it.kg, pendingKg = null) }
        return s to events
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
        s = if (id == session.lastPacket) {
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
        return stable(s, packet) to events
    }

    private fun stable(s: ListenSession, packet: RawPacket): ListenSession {
        if (packet.companyId == s.decidedKey) return s
        val reading = ScalePacket.decode(packet.data) ?: return s
        val decision = decision(s)
        val result = decision.onReading(reading, packet.atMs, packet)
        if (result != null) return s.copy(stable = s.stable + result.kg, pendingKg = null, decidedKey = packet.companyId)
        val pending = decision.pending
        return s.copy(pendingKg = pending?.kg, pendingAtMs = pending?.atMs ?: 0)
    }

    /** A [StableWeight] in the state the session saved: its pending stable reading fed back in. */
    private fun decision(s: ListenSession): StableWeight = StableWeight().also { d ->
        s.pendingKg?.let { d.onReading(ScaleReading(it, true, ScaleUnit.KG, 0.0), s.pendingAtMs) }
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
