package fi.jukkakot.terveyslappu.auto

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.view.View
import android.widget.ImageView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.WeighInWidget
import fi.jukkakot.terveyslappu.listen.ListenPrefs
import fi.jukkakot.terveyslappu.listen.ListenSettings
import fi.jukkakot.terveyslappu.raw.MemoryRawStore
import fi.jukkakot.terveyslappu.scale.MemoryLastWeighIn
import fi.jukkakot.terveyslappu.scale.WeighingKeys
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.LocalTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Notifications, their actions and the widget mark on Robolectric; made-up weights (NFR-1). */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class AutoAndroidTest {
    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val system = app.getSystemService(NotificationManager::class.java)
    private val notifications = AutoNotifications(app)

    @Before
    fun setUp() {
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun shown() = shadowOf(system).allNotifications

    @Test
    fun `the old silent channel is removed when the channels are created`() {
        system.createNotificationChannel(NotificationChannel("weighins", "old", NotificationManager.IMPORTANCE_LOW))
        notifications.channels()
        assertEquals(null, system.getNotificationChannel("weighins"))
    }

    @Test
    fun `a questioned weigh-in posts a sounding notification with two actions and no timeout`() {
        val note = AutoNote("terveyslappu-scale-1", 61.3, 1_000, true, NoteText("61,3 kg – tarkista", "Poikkeaa 20,7 kg edellisestä hyväksytystä"))
        assertTrue(notifications.post(note))
        val posted = shadowOf(system).getNotification(note.id.hashCode())
        assertEquals(2, posted.actions.size)
        assertEquals(0L, posted.timeoutAfter)
        assertEquals(AutoNotifications.CHANNEL_CHECK, posted.channelId)
        assertEquals(NotificationManager.IMPORTANCE_DEFAULT, system.getNotificationChannel(AutoNotifications.CHANNEL_CHECK).importance)
        assertEquals(NotificationManager.IMPORTANCE_DEFAULT, system.getNotificationChannel(AutoNotifications.CHANNEL_NORMAL).importance)

        val normal = note.copy(id = "terveyslappu-scale-2", questioned = false, text = NoteText("82,5 kg tallennettu", null))
        notifications.post(normal)
        assertEquals(1, shadowOf(system).getNotification(normal.id.hashCode()).actions.size)
        assertEquals(AutoNotifications.CHANNEL_NORMAL, shadowOf(system).getNotification(normal.id.hashCode()).channelId)
    }

    private class Writer : AutoWriter {
        val ids = mutableSetOf<String>()
        override suspend fun write(weight: PendingWeight) = AutoWeights.clientId(weight).also { ids += it }
        override suspend fun delete(clientRecordId: String) {
            ids -= clientRecordId
        }
    }

    private fun intent(action: String, note: AutoNote) = Intent(app, AutoActionReceiver::class.java).setAction(action)
        .putExtra(AutoActionReceiver.EXTRA_ID, note.id)
        .putExtra(AutoActionReceiver.EXTRA_TIME, note.timeMs)
        .putExtra(AutoActionReceiver.EXTRA_KG, note.kg)
        .putExtra(AutoActionReceiver.EXTRA_QUESTIONED, note.questioned)

    @Test
    fun `delete and accept actions work, each closing its notification`() = runBlocking {
        val writer = Writer()
        val marks = MarkStore()
        val auto = AutoWeights(
            WeighingKeys(MemoryLastWeighIn()), PendingStore(), marks, ReferenceStore(), FlagStore(), MemoryRawStore(), writer, notifications,
            limit = { 5.0 }, changed = {}, dialogShown = { false }, elapsedMs = { 0 },
        )
        val first = PendingWeight(61.3, 1_000_000, 7200, 0x61C0, "00")
        writer.write(first)
        val accept = AutoNote(AutoWeights.clientId(first), 61.3, first.timeMs, true, NoteText("61,3 kg – tarkista", null))
        marks.add(listOf(Mark(accept.id, accept.timeMs, accept.kg)))
        notifications.post(accept)
        AutoActionReceiver.handle(auto, notifications, intent(AutoActionReceiver.ACTION_ACCEPT, accept))
        assertTrue(marks.all().isEmpty())
        assertTrue(shown().none { it.extras.getString("android.title") == "61,3 kg – tarkista" })

        val second = PendingWeight(82.5, 2_000_000, 7200, 0x62C0, "00")
        writer.write(second)
        val delete = AutoNote(AutoWeights.clientId(second), 82.5, second.timeMs, false, NoteText("82,5 kg tallennettu", null))
        notifications.post(delete)
        AutoActionReceiver.handle(auto, notifications, intent(AutoActionReceiver.ACTION_DELETE, delete))
        assertTrue(delete.id !in writer.ids)
        assertTrue(shown().none { it.extras.getString("android.title") == "82,5 kg tallennettu" })
    }

    @Test
    fun `widget shows nothing with the feature off and resting outside the window`() {
        val manager = shadowOf(AppWidgetManager.getInstance(app))
        val id = manager.createWidget(WeighInWidget::class.java, R.layout.widget_weigh_in)
        val prefs = ListenPrefs(app)
        prefs.saveSettings(ListenSettings(enabled = false))
        WeighInWidget.update(app)
        assertEquals(View.GONE, manager.getViewFor(id).findViewById<ImageView>(R.id.widget_state).visibility)

        val now = LocalTime.now()
        prefs.saveSettings(ListenSettings(enabled = true, start = now.plusHours(2).withSecond(0).withNano(0), end = now.plusHours(3).withSecond(0).withNano(0)))
        WeighInWidget.update(app)
        val view = manager.getViewFor(id)
        assertEquals(View.VISIBLE, view.findViewById<ImageView>(R.id.widget_state).visibility)
        assertTrue(view.findViewById<View>(R.id.widget_root).contentDescription.contains(app.getString(R.string.widget_state_rest)))
    }
}
