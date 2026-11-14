package fi.jukkakot.terveyslappu.listen

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.PowerManager
import android.os.SystemClock
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import fi.jukkakot.terveyslappu.auto.Plausibility
import fi.jukkakot.terveyslappu.log.AppLog
import fi.jukkakot.terveyslappu.scale.KnownScale
import fi.jukkakot.terveyslappu.scale.LastWeighInStore
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

/**
 * The trial's own preferences file `listen` (design 9): not in data_extraction_rules.xml's
 * allow-list, so never backed up or moved to another phone.
 */
class ListenPrefs(context: Context) : ListenStore {
    private val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    override fun settings() = ListenSettings(
        enabled = prefs.getBoolean(ENABLED, false),
        start = LocalTime.ofSecondOfDay(prefs.getInt(START, ListenWindow.DEFAULT_START.toSecondOfDay() / 60) * 60L),
        end = LocalTime.ofSecondOfDay(prefs.getInt(END, ListenWindow.DEFAULT_END.toSecondOfDay() / 60) * 60L),
        mode = ListenMode.of(prefs.getString(MODE, null)),
        listenInPowerSave = prefs.getBoolean(POWER_SAVE, true),
        // New in phase 2: a trial install has no limit yet and gets the default (migration keeps the rest).
        limit = Plausibility.limit(prefs.getInt(LIMIT, (Plausibility.DEFAULT_LIMIT * 2).toInt()) / 2.0),
    )

    override fun saveSettings(settings: ListenSettings) = prefs.edit(commit = true) {
        putBoolean(ENABLED, settings.enabled)
        putInt(START, settings.start.toSecondOfDay() / 60)
        putInt(END, settings.end.toSecondOfDay() / 60)
        putString(MODE, settings.mode.key)
        putBoolean(POWER_SAVE, settings.listenInPowerSave)
        putInt(LIMIT, (Plausibility.limit(settings.limit) * 2).toInt())
    }

    override fun session(): ListenSession? = prefs.getString(SESSION, null)?.let {
        runCatching { json.decodeFromString<ListenSession>(it) }.onFailure { e -> AppLog.e("listen.session", e, "read") }.getOrNull()
    }

    override fun saveSession(session: ListenSession?) = prefs.edit(commit = true) {
        if (session == null) remove(SESSION) else putString(SESSION, json.encodeToString(session))
    }

    override fun lastHeard(): LastHeard? =
        if (prefs.contains(HEARD_AT)) LastHeard(Instant.ofEpochMilli(prefs.getLong(HEARD_AT, 0)), prefs.getInt(HEARD_RSSI, 0)) else null

    override fun saveLastHeard(heard: LastHeard) = prefs.edit(commit = true) {
        putLong(HEARD_AT, heard.at.toEpochMilli())
        putInt(HEARD_RSSI, heard.rssi)
    }

    override fun done(): WindowDone? {
        val start = prefs.getLong(DONE_START, 0).takeIf { it > 0 } ?: return null
        return WindowDone(start, prefs.getStringSet(DONE_IDS, emptySet()).orEmpty().toSet())
    }

    override fun saveDone(done: WindowDone?) = prefs.edit(commit = true) {
        if (done == null) {
            remove(DONE_START).remove(DONE_IDS)
        } else {
            putLong(DONE_START, done.windowStartMs).putStringSet(DONE_IDS, done.ids)
        }
    }

    companion object {
        const val FILE = "listen"
        private const val DONE_START = "done_window_start"
        private const val DONE_IDS = "done_ids"
        private const val ENABLED = "enabled"
        private const val START = "start_min"
        private const val END = "end_min"
        private const val MODE = "mode"
        private const val POWER_SAVE = "in_power_save"
        /** Hyväksyntäraja in half kilos. */
        private const val LIMIT = "limit_half_kg"
        private const val SESSION = "session"
        private const val HEARD_AT = "heard_at"
        private const val HEARD_RSSI = "heard_rssi"
    }
}

/** Health Connect permissions are platform permissions from Android 14 (minSdk 34): checked without a Health Connect call. */
const val HEALTH_WRITE_WEIGHT = "android.permission.health.WRITE_WEIGHT"

/** The phone's Bluetooth, power and battery state, and the scale remembered by the dialog. */
class AndroidListenDevice(private val context: Context, private val weighIn: LastWeighInStore) : ListenDevice {
    private val adapter: BluetoothAdapter? get() = context.getSystemService(BluetoothManager::class.java)?.adapter
    private val power: PowerManager? get() = context.getSystemService(PowerManager::class.java)

    override fun scale(): KnownScale? = weighIn.scale()
    override fun permission() = context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
    override fun bluetoothOn() = adapter?.isEnabled == true
    override fun powerSave() = power?.isPowerSaveMode == true
    /** Rajoitettu (background restricted) or Optimoitu (not ignoring optimisation): Samsung stops screen-off scans in both. */
    override fun batteryRestricted() = context.getSystemService(ActivityManager::class.java)?.isBackgroundRestricted == true ||
        power?.isIgnoringBatteryOptimizations(context.packageName) == false
    override fun battery() = context.getSystemService(BatteryManager::class.java)?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
    override fun screenOn() = power?.isInteractive == true
    override fun healthWrite() = context.checkSelfPermission(HEALTH_WRITE_WEIGHT) == PackageManager.PERMISSION_GRANTED
    override fun notificationsOn() = NotificationManagerCompat.from(context).areNotificationsEnabled()
    override fun offloadedFiltering() = adapter?.isOffloadedFilteringSupported == true
    override fun offloadedBatching() = adapter?.isOffloadedScanBatchingSupported == true
    override fun nowMs() = System.currentTimeMillis()
    override fun elapsedNanos() = SystemClock.elapsedRealtimeNanos()
    override fun zone(): ZoneId = ZoneId.systemDefault()
}

/**
 * The PendingIntent scan (design 1-3): filters on the remembered address, plus its name when known
 * (OR), never an empty list. Results go to [ListenReceiver] with the explicit, mutable intent the
 * system fills in; stop uses an identical one.
 */
class AndroidListenRadio(private val context: Context) : ListenRadio {
    private val scanner: BluetoothLeScanner?
        get() = context.getSystemService(BluetoothManager::class.java)?.adapter?.takeIf { it.isEnabled }?.bluetoothLeScanner

    private fun intent(): PendingIntent = PendingIntent.getBroadcast(
        context, SCAN_REQUEST,
        Intent(context, ListenReceiver::class.java).setAction(ListenReceiver.ACTION_SCAN),
        PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private fun allowed() = context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission") // allowed() checks BLUETOOTH_SCAN.
    override fun start(scale: KnownScale, mode: ListenMode): Int? {
        if (!allowed()) return NO_PERMISSION
        val le = scanner ?: return NO_SCANNER
        val filters = buildList {
            if (BluetoothAdapter.checkBluetoothAddress(scale.address.uppercase())) {
                add(ScanFilter.Builder().setDeviceAddress(scale.address.uppercase()).build())
            }
            scale.name?.let { add(ScanFilter.Builder().setDeviceName(it).build()) }
        }
        if (filters.isEmpty()) return NO_FILTER
        val settings = ScanSettings.Builder()
            .setScanMode(if (mode == ListenMode.BALANCED) ScanSettings.SCAN_MODE_BALANCED else ScanSettings.SCAN_MODE_LOW_POWER)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setReportDelay(0) // each packet at once: the weight is notified while the user still stands on the scale
            .build()
        return try {
            runCatching { le.stopScan(intent()) }
            val code = le.startScan(filters, settings, intent())
            if (code == 0) null else code
        } catch (e: Exception) {
            AppLog.e("listen.failed", e, "start")
            NO_SCANNER
        }
    }

    @SuppressLint("MissingPermission")
    override fun stop() {
        if (!allowed()) return
        val le = scanner ?: return
        runCatching { le.stopScan(intent()) }.onFailure { AppLog.e("listen.stop", it, "failed") }
    }

    companion object {
        private const val SCAN_REQUEST = 1
        /** Our own codes: Bluetooth off or no LE scanner, permission missing, no usable filter. */
        const val NO_SCANNER = -1
        const val NO_PERMISSION = -2
        const val NO_FILTER = -3
    }
}

/** `setAndAllowWhileIdle`: inexact, fires in Doze, no exact-alarm permission (design 4). */
class AndroidListenAlarm(private val context: Context) : ListenAlarm {
    private val manager: AlarmManager? get() = context.getSystemService(AlarmManager::class.java)

    /** Cancels the tick alarm v1.21 may have left armed (request code 3, action `...listen.TICK`); the tick itself is gone. */
    fun cancelLegacyTick() {
        val pending = PendingIntent.getBroadcast(
            context, LEGACY_TICK_REQUEST,
            Intent(context, ListenReceiver::class.java).setAction(LEGACY_TICK_ACTION),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE,
        ) ?: return
        manager?.cancel(pending)
        pending.cancel()
    }

    private fun intent(action: String?, flags: Int): PendingIntent? = PendingIntent.getBroadcast(
        context, ALARM_REQUEST,
        Intent(context, ListenReceiver::class.java).setAction(ListenReceiver.ACTION_ALARM).apply {
            action?.let { putExtra(ListenReceiver.EXTRA_ACTION, it) }
        },
        PendingIntent.FLAG_IMMUTABLE or flags,
    )

    override fun set(atMs: Long, action: String) {
        val pending = intent(action, PendingIntent.FLAG_UPDATE_CURRENT) ?: return
        manager?.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMs, pending)
    }

    override fun cancel() {
        val pending = intent(null, PendingIntent.FLAG_NO_CREATE) ?: return
        manager?.cancel(pending)
        pending.cancel()
    }

    override fun armed(): Boolean = intent(null, PendingIntent.FLAG_NO_CREATE) != null

    private companion object {
        const val ALARM_REQUEST = 2
        const val LEGACY_TICK_REQUEST = 3
        const val LEGACY_TICK_ACTION = "fi.jukkakot.terveyslappu.listen.TICK"
    }
}
