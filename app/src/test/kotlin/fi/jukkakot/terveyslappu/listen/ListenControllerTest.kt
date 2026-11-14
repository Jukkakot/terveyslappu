package fi.jukkakot.terveyslappu.listen

import android.app.AlarmManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.scale.KnownScale
import fi.jukkakot.terveyslappu.scale.ScalePacketTest.Companion.packet
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The controller with the real preferences and AlarmManager; the scan and the phone's state are fakes. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class ListenControllerTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val zone = ZoneId.of("Europe/Helsinki")
    private fun at(text: String): Long = LocalDateTime.parse(text).atZone(zone).toInstant().toEpochMilli()

    private class FakeRadio : ListenRadio {
        var running: ListenMode? = null
        var starts = 0
        override fun start(scale: KnownScale, mode: ListenMode, batch: Boolean): Int? {
            running = mode
            starts++
            return null
        }
        override fun stop() {
            running = null
        }
    }

    private inner class FakeDevice : ListenDevice {
        var now = at("2026-11-14T07:00")
        var elapsed = 1_000_000_000L
        var scale: KnownScale? = KnownScale("00:11:22:33:44:55", "Yoda1")
        var powerSave = false
        override fun scale() = scale
        override fun permission() = true
        override fun bluetoothOn() = true
        override fun powerSave() = powerSave
        override fun batteryRestricted() = false
        override fun battery() = 80
        override fun screenOn() = false
        override fun offloadedFiltering() = true
        override fun offloadedBatching() = false
        override fun nowMs() = now
        override fun elapsedNanos() = elapsed
        override fun zone(): ZoneId = zone
    }

    private val radio = FakeRadio()
    private val device = FakeDevice()
    private lateinit var store: ListenPrefs
    private lateinit var listen: ListenController

    @Before
    fun setUp() {
        context.deleteSharedPreferences(ListenPrefs.FILE)
        store = ListenPrefs(context)
        listen = ListenController(store, device, radio, AndroidListenAlarm(context))
    }

    private fun nextAlarm() = shadowOf(context.getSystemService(AlarmManager::class.java)).nextScheduledAlarm

    @Test
    fun `turning on inside the window starts the scan and arms the hourly check`() {
        listen.setEnabled(true)
        assertEquals(ListenMode.SAVE, radio.running)
        assertNotNull(store.session())
        assertEquals(at("2026-11-14T08:00"), nextAlarm()!!.triggerAtTime)
        assertEquals(ListenStatus.Listening, listen.ui().status)
    }

    @Test
    fun `turning on outside the window schedules the start`() {
        device.now = at("2026-11-14T11:00")
        listen.setEnabled(true)
        assertNull(radio.running)
        assertNull(store.session())
        assertEquals(at("2026-11-15T06:00"), nextAlarm()!!.triggerAtTime)
        assertIs<ListenStatus.Resting>(listen.ui().status)
    }

    @Test
    fun `without a remembered scale the trial cannot be turned on`() {
        device.scale = null
        listen.setEnabled(true)
        assertFalse(store.settings().enabled)
        assertFalse(listen.ui().hasScale)
    }

    @Test
    fun `window end and turning off close the session and stop`() {
        listen.setEnabled(true)
        listen.onResults(listOf(HeardResult(0x12C0, packet(825, 1), -97, "Yoda1", "00:11:22:33:44:55", device.elapsed + 1_000_000)))
        assertEquals(1, store.session()!!.packets)
        assertEquals(-97, store.lastHeard()!!.rssi)
        device.now = at("2026-11-14T10:00")
        listen.replan(ListenController.ACTION_END)
        assertNull(store.session())
        assertNull(radio.running)
        assertEquals(at("2026-11-15T06:00"), nextAlarm()!!.triggerAtTime)
        listen.setEnabled(false)
        assertNull(nextAlarm())
        assertNull(listen.ui().status)
    }

    @Test
    fun `settings change re-plans at once and power saving pauses`() {
        listen.setEnabled(true)
        listen.updateSettings(store.settings().copy(mode = ListenMode.BALANCED))
        assertEquals(ListenMode.BALANCED, radio.running)
        device.powerSave = true
        listen.updateSettings(store.settings().copy(listenInPowerSave = false))
        assertNull(radio.running)
        assertEquals(ListenStatus.PowerSavePause, listen.ui().status)
        // A window that no longer contains now stops with a summary.
        listen.updateSettings(store.settings().copy(start = java.time.LocalTime.of(20, 0), end = java.time.LocalTime.of(22, 0)))
        assertNull(store.session())
        assertEquals(at("2026-11-14T20:00"), nextAlarm()!!.triggerAtTime)
    }

    @Test
    fun `a session left over from a reboot is closed and a new one starts`() {
        listen.setEnabled(true)
        val first = store.session()!!
        device.elapsed = 10L
        listen.replan("boot")
        val second = store.session()!!
        assertTrue(second.startedMs >= first.startedMs)
        assertEquals(10L, second.startElapsedNanos)
        assertEquals(2, radio.starts)
        assertTrue(Instant.ofEpochMilli(second.windowEndMs).isAfter(Instant.ofEpochMilli(device.now)))
    }
}
