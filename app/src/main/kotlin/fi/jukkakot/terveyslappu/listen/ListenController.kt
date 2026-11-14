package fi.jukkakot.terveyslappu.listen

import fi.jukkakot.terveyslappu.auto.Plausibility
import fi.jukkakot.terveyslappu.log.AppLog
import fi.jukkakot.terveyslappu.scale.KnownScale
import fi.jukkakot.terveyslappu.scale.PacketLog
import fi.jukkakot.terveyslappu.scale.RawPacket
import fi.jukkakot.terveyslappu.scale.StableWeight
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

/** The automatic weigh-in's choices (background-weigh-in "Trial setting"); [limit] is Hyväksyntäraja in kg. */
data class ListenSettings(
    val enabled: Boolean = false,
    val start: LocalTime = ListenWindow.DEFAULT_START,
    val end: LocalTime = ListenWindow.DEFAULT_END,
    val mode: ListenMode = ListenMode.SAVE,
    val listenInPowerSave: Boolean = true,
    val limit: Double = Plausibility.DEFAULT_LIMIT,
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
    /** The window-done record (design 4). */
    fun done(): WindowDone?
    fun saveDone(done: WindowDone?)
}

/**
 * The unmarked weights of this app saved in the window that started at [windowStartMs] (their
 * `clientRecordId`s): a non-empty set means the window's weight is in and listening stops.
 */
data class WindowDone(val windowStartMs: Long, val ids: Set<String>)

/** True when [done] still holds at [now]: until the next window start after its window's start. Pure. */
fun WindowDone?.holds(window: ListenWindow, now: Instant, zone: ZoneId): Boolean =
    this != null && ids.isNotEmpty() && now.isBefore(window.nextStart(Instant.ofEpochMilli(windowStartMs), zone))

/** The phone's state the trial reads. */
interface ListenDevice {
    fun scale(): KnownScale?
    fun permission(): Boolean
    fun bluetoothOn(): Boolean
    fun powerSave(): Boolean
    fun batteryRestricted(): Boolean
    fun battery(): Int
    /** Health Connect weight write access (background-weigh-in "Trial status"). */
    fun healthWrite(): Boolean = true
    /** The app may post notifications (a warning line when not). */
    fun notificationsOn(): Boolean = true
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

/**
 * The alarm that wakes the trial at the window start, the hourly check and the end (design 4),
 * and a second, separate one ([setTick]) that decides a waiting stable reading a few seconds after
 * it. [cancel] cancels both.
 */
interface ListenAlarm {
    fun set(atMs: Long, action: String)
    fun cancel()
    fun armed(): Boolean
    fun setTick(atMs: Long)
    fun cancelTick()
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

/** What the Asetukset section shows; [widget] and [windowEndMs] are what the widget draws. */
data class ListenUi(
    val settings: ListenSettings = ListenSettings(),
    val hasScale: Boolean = false,
    val status: ListenStatus? = null,
    val batteryRestricted: Boolean = false,
    val notificationsOff: Boolean = false,
    /** Weights waiting in the pending queue ("N punnitusta odottaa tallennusta"). */
    val pending: Int = 0,
    val lastHeard: String = LastHeardText.text(null, Instant.EPOCH, ZoneId.systemDefault()),
    val widget: WidgetState? = null,
    val windowEndMs: Long? = null,
)

/** Where a decided background weight goes (the automatic save); called under the controller's lock. */
fun interface DecidedSink {
    fun decided(weight: HeardEvent.Decided)
}

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
    private val sink: DecidedSink = DecidedSink {},
    private val pendingCount: () -> Int = { 0 },
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
            "limit" to next.limit,
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
        val now = Instant.ofEpochMilli(nowMs)
        val plan = settings.window.plan(now, device.zone())
        if (plan is WindowPlan.Inside && store.done().holds(settings.window, now, device.zone())) {
            // The window's weight is in: stop for the rest of the window (design 4).
            radio.stop()
            store.session()?.let {
                AppLog.i("listen.stop", "", "reason" to "done")
                close(it, "done", nowMs)
            }
            arm(settings.window.nextStart(now, device.zone()), ACTION_START)
            changed()
            return
        }
        when (plan) {
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
        // A lone stable reading with nothing after it is decided at the hourly check (design 2).
        if (session != null && reason == ACTION_CHECK) {
            val (ticked, decided) = HeardPackets.tick(session, nowMs - session.startedMs)
            session = ticked
            decided?.let { sink.decided(it.from(scale.address)) }
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
                "late" to lateMinutes(reason, nowMs, plan.start.toEpochMilli()),
                "mode" to settings.mode.key,
                "offloadFilter" to device.offloadedFiltering(),
                "offloadBatch" to device.offloadedBatching(),
            )
        } else {
            session = session.copy(windowEndMs = plan.end.toEpochMilli(), mode = settings.mode.key)
        }
        store.saveSession(session)
        armTick(session)
        if (!device.healthWrite()) {
            radio.stop()
            AppLog.w("listen.stop", "", "reason" to "hc")
        } else if (powerSave && !settings.listenInPowerSave) {
            radio.stop()
            AppLog.i("listen.stop", "", "reason" to "powerSave")
        } else {
            radio.start(scale, settings.mode, device.offloadedBatching())?.let { onScanError(it) }
        }
        arm(plan.nextCheck, if (plan.nextCheck == plan.end) ACTION_END else ACTION_CHECK)
    }

    /** Only an alarm start measures lateness; enable, settings or app inside the window log 0 (trial finding). */
    private fun lateMinutes(reason: String, nowMs: Long, startMs: Long): Long =
        if (reason == ACTION_START || reason == ACTION_CHECK) (nowMs - startMs) / 60_000 else 0

    private fun arm(at: Instant, action: String) {
        alarm.set(at.toEpochMilli(), action)
        AppLog.i("listen.arm", "", "next" to stamp(at.toEpochMilli()), "action" to action)
    }

    fun onScanError(code: Int) {
        AppLog.w("listen.failed", "", "code" to code)
    }

    /**
     * This app saved (or accepted) the weight [id] at [timeMs], not marked "?" (design 4): inside
     * the current window it counts as the window's weight and listening stops.
     */
    @Synchronized
    fun weightSaved(id: String, timeMs: Long) {
        val zone = device.zone()
        val (start, end) = store.settings().window.current(Instant.ofEpochMilli(device.nowMs()), zone) ?: return
        if (timeMs < start.toEpochMilli() || timeMs >= end.toEpochMilli()) return
        val old = store.done()?.takeIf { it.windowStartMs == start.toEpochMilli() }
        val ids = (old?.ids ?: emptySet()) + id
        if (old?.ids == ids) return
        store.saveDone(WindowDone(start.toEpochMilli(), ids))
        replan("done")
    }

    /** A weight of this app was deleted or marked "?": when it was the window's last one, listening resumes. */
    @Synchronized
    fun weightRemoved(id: String) {
        val old = store.done() ?: return
        if (id !in old.ids) return
        val ids = old.ids - id
        store.saveDone(if (ids.isEmpty()) null else old.copy(ids = ids))
        replan("done")
    }

    /**
     * The tick alarm (design 2): decides a stable reading that waited out its quiet time, as the
     * hourly check does, without replanning anything.
     */
    @Synchronized
    fun onTick() {
        val session = store.session() ?: return
        val nowMs = device.nowMs()
        val (ticked, decided) = HeardPackets.tick(session, nowMs - session.startedMs)
        AppLog.i("listen.tick", "", "decided" to (decided != null), "pending" to (ticked.pendingKg != null))
        store.saveSession(ticked)
        decided?.let { sink.decided(it.from(device.scale()?.address)) }
        armTick(ticked)
    }

    /** A stable reading is waiting: wake up just after its quiet time; none waiting: no tick. */
    private fun armTick(session: ListenSession) {
        if (session.pendingKg == null) {
            alarm.cancelTick()
            return
        }
        alarm.setTick(session.startedMs + session.pendingAtMs + StableWeight.DEFAULT_QUIET_MS + TICK_MARGIN_MS)
    }

    /** A delivery from the scan (design 6, 10): logs and accumulates; a late one after the window is dropped. */
    @Synchronized
    fun onResults(results: List<HeardResult>) {
        var session = store.session() ?: run {
            AppLog.i("auto.skip", "", "reason" to "outside", "count" to results.size)
            return
        }
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
        armTick(session)
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
            is HeardEvent.OutOfRange -> AppLog.i("auto.skip", "", "reason" to "range", "kg" to event.kg)
            is HeardEvent.Decided -> {
                AppLog.i("scale.stable", "", "kg" to event.kg, "src" to "bg")
                sink.decided(event.from(device.scale()?.address))
            }
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
        alarm.cancelTick()
    }

    /** The section's state, read fresh (status, warning, last heard). */
    @Synchronized
    fun ui(): ListenUi {
        val settings = store.settings()
        val now = Instant.ofEpochMilli(device.nowMs())
        val zone = device.zone()
        val hasScale = device.scale() != null
        val plan = settings.window.plan(now, zone)
        val nextStart = settings.window.nextStart(now, zone)
        val facts = ListenFacts(
            enabled = settings.enabled,
            hasScale = hasScale,
            permission = device.permission(),
            healthWrite = device.healthWrite(),
            bluetoothOn = device.bluetoothOn(),
            powerSave = device.powerSave(),
            listenInPowerSave = settings.listenInPowerSave,
            armed = alarm.armed(),
            inside = plan is WindowPlan.Inside,
            nextStart = nextStart.atZone(zone).toLocalTime(),
            done = store.done().holds(settings.window, now, zone),
            nextStartTomorrow = nextStart.atZone(zone).toLocalDate().isAfter(now.atZone(zone).toLocalDate()),
        )
        val status = ListenStatusRule.status(facts)
        return ListenUi(
            settings = settings,
            hasScale = hasScale,
            status = status,
            batteryRestricted = settings.enabled && device.batteryRestricted(),
            notificationsOff = settings.enabled && !device.notificationsOn(),
            pending = pendingCount(),
            lastHeard = LastHeardText.text(store.lastHeard(), now, zone),
            widget = WidgetStateRule.of(status, facts.inside),
            windowEndMs = (plan as? WindowPlan.Inside)?.end?.toEpochMilli(),
        )
    }

    private fun stamp(ms: Long): String = stampFormat.format(Instant.ofEpochMilli(ms).atZone(device.zone()))

    companion object {
        const val ACTION_START = "start"
        const val ACTION_CHECK = "check"
        const val ACTION_END = "end"
        /** The tick comes this long after a pending reading's quiet time (inexact alarm, a little slack). */
        const val TICK_MARGIN_MS = 5_000L
        private val clock = DateTimeFormatter.ofPattern("HH:mm")
        private val stampFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    }
}
