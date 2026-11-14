package fi.jukkakot.terveyslappu.listen

import fi.jukkakot.terveyslappu.log.AppLog
import fi.jukkakot.terveyslappu.scale.KnownScale
import fi.jukkakot.terveyslappu.scale.PacketLog
import fi.jukkakot.terveyslappu.scale.RawPacket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Säästö = the lowest-power scan, Tasapaino = the balanced scan; [key] is what prefs and the log keep. */
enum class ListenMode(val key: String) {
    SAVE("save"),
    BALANCED("balanced"),
    ;

    companion object {
        fun of(key: String?): ListenMode = entries.firstOrNull { it.key == key } ?: SAVE
    }
}

/** The trial's choices (background-weigh-in "Trial setting"). */
data class ListenSettings(
    val enabled: Boolean = false,
    val start: LocalTime = ListenWindow.DEFAULT_START,
    val end: LocalTime = ListenWindow.DEFAULT_END,
    val mode: ListenMode = ListenMode.SAVE,
    val listenInPowerSave: Boolean = true,
) {
    val window: ListenWindow get() = ListenWindow(start, end)
}

/** Where the trial keeps its state (app-private, never backed up). */
interface ListenStore {
    fun settings(): ListenSettings
    fun saveSettings(settings: ListenSettings)
    fun session(): ListenSession?
    fun saveSession(session: ListenSession?)
    fun lastHeard(): LastHeard?
    fun saveLastHeard(heard: LastHeard)
}

/** The phone's state the trial reads. */
interface ListenDevice {
    fun scale(): KnownScale?
    fun permission(): Boolean
    fun bluetoothOn(): Boolean
    fun powerSave(): Boolean
    fun batteryRestricted(): Boolean
    fun battery(): Int
    fun screenOn(): Boolean
    fun offloadedFiltering(): Boolean
    fun offloadedBatching(): Boolean
    fun nowMs(): Long
    fun elapsedNanos(): Long
    fun zone(): ZoneId
}

/** The PendingIntent scan (design 1-3). [start] replaces a running one; returns an error code or null. */
interface ListenRadio {
    fun start(scale: KnownScale, mode: ListenMode, batch: Boolean): Int?
    fun stop()
}

/** The one alarm that wakes the trial at the window start, the hourly check and the end (design 4). */
interface ListenAlarm {
    fun set(atMs: Long, action: String)
    fun cancel()
    fun armed(): Boolean
}

/** One manufacturer-data entry of a delivered scan result. */
data class HeardResult(
    val companyId: Int,
    val data: ByteArray,
    val rssi: Int,
    val name: String?,
    val address: String?,
    val timestampNanos: Long,
)

/** What the Asetukset section shows. */
data class ListenUi(
    val settings: ListenSettings = ListenSettings(),
    val hasScale: Boolean = false,
    val status: ListenStatus? = null,
    val batteryRestricted: Boolean = false,
    val lastHeard: String = LastHeardText.text(null, Instant.EPOCH, ZoneId.systemDefault()),
)

/**
 * The listen-only trial (background-weigh-in, design.md): plans the scan and the alarm from the
 * window, feeds delivered packets to [HeardPackets] and logs what was heard. Android-free; the
 * receivers and Asetukset call it. Every entry point re-reads the store, since the process may
 * have died in between.
 */
class ListenController(
    private val store: ListenStore,
    private val device: ListenDevice,
    private val radio: ListenRadio,
    private val alarm: ListenAlarm,
) {
    private val _changes = MutableStateFlow(0)

    /** Bumped on every change the section shows (settings, status, last heard). */
    val changes: StateFlow<Int> = _changes

    private fun changed() {
        _changes.value++
    }

    @Synchronized
    fun setEnabled(on: Boolean) {
        val settings = store.settings()
        if (on && device.scale() == null) return
        store.saveSettings(settings.copy(enabled = on))
        AppLog.i(if (on) "listen.enable" else "listen.disable")
        replan(if (on) "enable" else "off")
    }

    /** New window, mode or power-saving choice: logged and re-planned at once. */
    @Synchronized
    fun updateSettings(new: ListenSettings) {
        if (!ListenWindow.valid(new.start, new.end)) return
        val old = store.settings()
        val next = new.copy(enabled = old.enabled)
        if (next == old) return
        store.saveSettings(next)
        AppLog.i(
            "listen.settings", "",
            "window" to "${clock.format(next.start)}-${clock.format(next.end)}",
            "mode" to next.mode.key,
            "powerSave" to if (next.listenInPowerSave) "on" else "off",
        )
        replan("settings")
    }

    /** At app start: turned on but nothing armed (force stop, lost alarm) re-arms, logged. */
    @Synchronized
    fun onAppStart() {
        if (!store.settings().enabled || alarm.armed()) return
        AppLog.w("listen.arm", "rearm", "reason" to "app")
        replan("app")
    }

    /**
     * Brings scan and alarm in line with the window (design 4-6). [reason]: the alarm's action
     * (start, check, end), or what asked (enable, off, settings, boot, update, app, scale).
     */
    @Synchronized
    fun replan(reason: String) {
        val settings = store.settings()
        val scale = device.scale()
        val nowMs = device.nowMs()
        if (!settings.enabled || scale == null) {
            radio.stop()
            alarm.cancel()
            store.session()?.let {
                AppLog.i("listen.stop", "", "reason" to "off")
                close(it, "off", nowMs)
            }
            changed()
            return
        }
        when (val plan = settings.window.plan(Instant.ofEpochMilli(nowMs), device.zone())) {
            is WindowPlan.Outside -> {
                radio.stop()
                store.session()?.let {
                    val why = SessionRule.closeReason(it, nowMs)
                    AppLog.i("listen.stop", "", "reason" to why)
                    close(it, why, nowMs)
                }
                arm(plan.nextStart, ACTION_START)
            }
            is WindowPlan.Inside -> inside(plan, settings, scale, reason, nowMs)
        }
        changed()
    }

    private fun inside(plan: WindowPlan.Inside, settings: ListenSettings, scale: KnownScale, reason: String, nowMs: Long) {
        var session = store.session()
        if (session != null && !SessionRule.continues(session, nowMs, device.elapsedNanos())) {
            close(session, "stale", nowMs)
            session = null
        }
        val powerSave = device.powerSave()
        if (reason == ACTION_CHECK) {
            AppLog.i(
                "listen.check", "",
                "bt" to device.bluetoothOn(),
                "powerSave" to powerSave,
                "restricted" to device.batteryRestricted(),
            )
        }
        if (session == null) {
            session = ListenSession(
                windowStartMs = plan.start.toEpochMilli(),
                windowEndMs = plan.end.toEpochMilli(),
                startedMs = nowMs,
                startElapsedNanos = device.elapsedNanos(),
                batteryStart = device.battery(),
                mode = settings.mode.key,
            )
            AppLog.i(
                "listen.start", "",
                "late" to (nowMs - plan.start.toEpochMilli()) / 60_000,
                "mode" to settings.mode.key,
                "offloadFilter" to device.offloadedFiltering(),
                "offloadBatch" to device.offloadedBatching(),
            )
        } else {
            session = session.copy(windowEndMs = plan.end.toEpochMilli(), mode = settings.mode.key)
        }
        store.saveSession(session)
        if (powerSave && !settings.listenInPowerSave) {
            radio.stop()
            AppLog.i("listen.stop", "", "reason" to "powerSave")
        } else {
            radio.start(scale, settings.mode, device.offloadedBatching())?.let { onScanError(it) }
        }
        arm(plan.nextCheck, if (plan.nextCheck == plan.end) ACTION_END else ACTION_CHECK)
    }

    private fun arm(at: Instant, action: String) {
        alarm.set(at.toEpochMilli(), action)
        AppLog.i("listen.arm", "", "next" to stamp(at.toEpochMilli()), "action" to action)
    }

    fun onScanError(code: Int) {
        AppLog.w("listen.failed", "", "code" to code)
    }

    /** A delivery from the scan (design 6, 10): logs and accumulates; a late one after the window is dropped. */
    @Synchronized
    fun onResults(results: List<HeardResult>) {
        var session = store.session() ?: return
        val scale = device.scale() ?: return
        val screen = if (device.screenOn()) "on" else "off"
        val before = session.packets
        var last: RawPacket? = null
        for (r in results) {
            val atMs = ((r.timestampNanos - session.startElapsedNanos) / 1_000_000).coerceAtLeast(0)
            val packet = RawPacket(atMs, r.companyId, r.data, r.rssi, r.name, r.address)
            val (next, events) = HeardPackets.onPacket(session, packet, scale)
            if (next.packets > session.packets) last = packet
            session = next
            events.forEach { log(it, screen) }
        }
        store.saveSession(session)
        val heard = last
        if (session.packets > before && heard != null) {
            store.saveLastHeard(LastHeard(Instant.ofEpochMilli(session.startedMs + heard.atMs), heard.rssi))
            changed()
        }
    }

    private fun log(event: HeardEvent, screen: String) {
        when (event) {
            is HeardEvent.Raw -> AppLog.d(PacketLog.EVENT, "", *PacketLog.fields(event.packet), "src" to "bg", "screen" to screen)
            is HeardEvent.Repeat -> AppLog.d("listen.repeat", "", "count" to event.count, "rssiMin" to event.rssiMin, "rssiMax" to event.rssiMax)
            is HeardEvent.Other -> AppLog.d("listen.other", "", "addr" to (event.address ?: "-"), "name" to (event.name ?: "-"), "rssi" to event.rssi)
        }
    }

    private fun close(session: ListenSession, reason: String, nowMs: Long) {
        val (s, events) = HeardPackets.close(session, nowMs)
        events.forEach { log(it, "-") }
        AppLog.i(
            "listen.summary", "",
            "reason" to reason,
            "mode" to s.mode,
            "packets" to s.packets,
            "distinct" to s.distinct,
            "rssiMax" to (s.rssiMax ?: "-"),
            "rssiMin" to (s.rssiMin ?: "-"),
            "first" to (s.firstMs?.let(::stamp) ?: "-"),
            "last" to (s.lastMs?.let(::stamp) ?: "-"),
            "stable" to s.stable.joinToString(",").ifEmpty { "-" },
            "batteryStart" to s.batteryStart,
            "batteryEnd" to device.battery(),
        )
        store.saveSession(null)
    }

    /** The section's state, read fresh (status, warning, last heard). */
    @Synchronized
    fun ui(): ListenUi {
        val settings = store.settings()
        val now = Instant.ofEpochMilli(device.nowMs())
        val zone = device.zone()
        val hasScale = device.scale() != null
        val plan = settings.window.plan(now, zone)
        val facts = ListenFacts(
            enabled = settings.enabled,
            hasScale = hasScale,
            permission = device.permission(),
            bluetoothOn = device.bluetoothOn(),
            powerSave = device.powerSave(),
            listenInPowerSave = settings.listenInPowerSave,
            armed = alarm.armed(),
            inside = plan is WindowPlan.Inside,
            nextStart = settings.window.nextStart(now, zone).atZone(zone).toLocalTime(),
        )
        return ListenUi(
            settings = settings,
            hasScale = hasScale,
            status = ListenStatusRule.status(facts),
            batteryRestricted = settings.enabled && device.batteryRestricted(),
            lastHeard = LastHeardText.text(store.lastHeard(), now, zone),
        )
    }

    private fun stamp(ms: Long): String = stampFormat.format(Instant.ofEpochMilli(ms).atZone(device.zone()))

    companion object {
        const val ACTION_START = "start"
        const val ACTION_CHECK = "check"
        const val ACTION_END = "end"
        private val clock = DateTimeFormatter.ofPattern("HH:mm")
        private val stampFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    }
}
