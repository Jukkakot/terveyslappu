package fi.jukkakot.terveyslappu.auto

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import fi.jukkakot.terveyslappu.MainActivity
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.TerveysApp
import fi.jukkakot.terveyslappu.log.AppLog
import fi.jukkakot.terveyslappu.weight.HealthConnectWeights
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset

/** The process-wide "weights changed" signal (design 12): the main screen reloads on it. */
class WeightsSignal {
    private val flow = MutableSharedFlow<Unit>(extraBufferCapacity = 8)
    val changes: SharedFlow<Unit> = flow

    fun emit() {
        flow.tryEmit(Unit)
    }
}

/** Writes through the dialog's scale write (autoRecorded, scale device, upsert by the reading's second). */
class HealthConnectAutoWriter(private val weights: HealthConnectWeights) : AutoWriter {
    override suspend fun write(weight: PendingWeight): String =
        weights.writeScale(weight.kg, Instant.ofEpochMilli(weight.timeMs), ZoneOffset.ofTotalSeconds(weight.offsetSeconds))

    override suspend fun delete(clientRecordId: String) = weights.deleteByClientId(clientRecordId)
}

/**
 * The weigh-in notifications (design 8): a silent channel for normal weigh-ins and a
 * default-importance one for "?" weights, under one channel group and one notification group with
 * a summary; none times out; the tap opens Historia; Poista (and Hyväksy for "?") go to
 * [AutoActionReceiver].
 */
class AutoNotifications(private val context: Context) : AutoNotifier {
    private val manager = NotificationManagerCompat.from(context)

    fun channels() {
        val system = context.getSystemService(NotificationManager::class.java) ?: return
        system.createNotificationChannelGroup(NotificationChannelGroup(GROUP_CHANNELS, context.getString(R.string.auto_channel_group)))
        system.createNotificationChannel(
            NotificationChannel(CHANNEL_NORMAL, context.getString(R.string.auto_channel_normal), NotificationManager.IMPORTANCE_LOW).apply {
                group = GROUP_CHANNELS
                setSound(null, null)
                enableVibration(false)
            },
        )
        system.createNotificationChannel(
            NotificationChannel(CHANNEL_CHECK, context.getString(R.string.auto_channel_check), NotificationManager.IMPORTANCE_DEFAULT).apply {
                group = GROUP_CHANNELS
            },
        )
    }

    private fun allowed(): Boolean =
        manager.areNotificationsEnabled() &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    override fun post(note: AutoNote): Boolean {
        channels()
        if (!allowed()) return false
        val open = PendingIntent.getActivity(
            context, note.id.hashCode(), MainActivity.historyIntent(context),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val builder = NotificationCompat.Builder(context, if (note.questioned) CHANNEL_CHECK else CHANNEL_NORMAL)
            .setSmallIcon(R.drawable.ic_scale_white)
            .setContentTitle(note.text.title)
            .setContentText(note.text.text)
            .setContentIntent(open)
            .setAutoCancel(true)
            .setGroup(GROUP)
            .setWhen(note.timeMs)
            .setShowWhen(true)
            .setSilent(!note.questioned)
            .setPriority(if (note.questioned) NotificationCompat.PRIORITY_DEFAULT else NotificationCompat.PRIORITY_LOW)
        if (note.questioned) builder.addAction(0, context.getString(R.string.auto_accept), action(note, AutoActionReceiver.ACTION_ACCEPT))
        builder.addAction(0, context.getString(R.string.auto_delete), action(note, AutoActionReceiver.ACTION_DELETE))
        return try {
            manager.notify(note.id.hashCode(), builder.build())
            manager.notify(SUMMARY_ID, summary())
            true
        } catch (e: SecurityException) {
            AppLog.e("auto.notify", e, "denied")
            false
        }
    }

    private fun summary() = NotificationCompat.Builder(context, CHANNEL_NORMAL)
        .setSmallIcon(R.drawable.ic_scale_white)
        .setContentTitle(context.getString(R.string.auto_summary))
        .setGroup(GROUP)
        .setGroupSummary(true)
        .setSilent(true)
        .setAutoCancel(true)
        .setContentIntent(PendingIntent.getActivity(context, 0, MainActivity.historyIntent(context), PendingIntent.FLAG_IMMUTABLE))
        .build()

    /** Distinct request code per weight and action, explicit non-exported receiver, immutable. */
    private fun action(note: AutoNote, action: String): PendingIntent = PendingIntent.getBroadcast(
        context,
        (note.id + action).hashCode(),
        Intent(context, AutoActionReceiver::class.java)
            .setAction(action)
            .putExtra(AutoActionReceiver.EXTRA_ID, note.id)
            .putExtra(AutoActionReceiver.EXTRA_TIME, note.timeMs)
            .putExtra(AutoActionReceiver.EXTRA_KG, note.kg)
            .putExtra(AutoActionReceiver.EXTRA_QUESTIONED, note.questioned),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    override fun cancel(id: String) {
        manager.cancel(id.hashCode())
        // The summary goes when the last weigh-in notification of the group is gone.
        val system = context.getSystemService(NotificationManager::class.java) ?: return
        if (system.activeNotifications.none { it.id != SUMMARY_ID && it.notification.group == GROUP }) manager.cancel(SUMMARY_ID)
    }

    companion object {
        const val CHANNEL_NORMAL = "weighins"
        const val CHANNEL_CHECK = "weighins_check"
        const val GROUP_CHANNELS = "auto"
        const val GROUP = "fi.jukkakot.terveyslappu.WEIGHINS"
        const val SUMMARY_ID = 1
    }
}

/** Poista and Hyväksy from a weigh-in notification (not exported: only our PendingIntents reach it). */
class AutoActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as TerveysApp
        val id = intent.getStringExtra(EXTRA_ID) ?: return
        val timeMs = intent.getLongExtra(EXTRA_TIME, 0)
        val kg = intent.getDoubleExtra(EXTRA_KG, 0.0)
        val questioned = intent.getBooleanExtra(EXTRA_QUESTIONED, false)
        val result = goAsync()
        app.appScope.launch(Dispatchers.IO) {
            try {
                when (intent.action) {
                    ACTION_DELETE -> app.auto.delete(id, timeMs, kg, questioned)
                    ACTION_ACCEPT -> {
                        val accepted = app.auto.accept(id, AutoWeights.FROM_NOTIFICATION)
                        AppLog.i("auto.action", if (accepted.isEmpty()) "confirm failed" else "confirm ok")
                        // Already accepted elsewhere: the notification still closes.
                        if (accepted.isEmpty()) app.autoNotifier.cancel(id)
                    }
                }
            } catch (e: Exception) {
                AppLog.e("auto.action", e, "failed", "action" to intent.action)
            } finally {
                result.finish()
            }
        }
    }

    companion object {
        const val ACTION_DELETE = "fi.jukkakot.terveyslappu.auto.DELETE"
        const val ACTION_ACCEPT = "fi.jukkakot.terveyslappu.auto.ACCEPT"
        const val EXTRA_ID = "id"
        const val EXTRA_TIME = "time"
        const val EXTRA_KG = "kg"
        const val EXTRA_QUESTIONED = "questioned"
    }
}
