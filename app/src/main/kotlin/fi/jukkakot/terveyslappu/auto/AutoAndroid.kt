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
import fi.jukkakot.terveyslappu.a11y.ActionTimeouts
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
 * The weigh-in notifications (design 8): two default-importance channels (normal and "?"
 * weights) under one channel group and one notification group with a summary; the tap opens
 * Historia; Poista paino (and Hyväksy for "?") go to [AutoActionReceiver]. Only "Paino poistettu"
 * times out (everyday-feedback design 5), and the summary with it when it is the last one.
 */
class AutoNotifications(private val context: Context) : AutoNotifier {
    private val manager = NotificationManagerCompat.from(context)

    fun channels() {
        val system = context.getSystemService(NotificationManager::class.java) ?: return
        system.createNotificationChannelGroup(NotificationChannelGroup(GROUP_CHANNELS, context.getString(R.string.auto_channel_group)))
        // The old silent channel cannot be raised by an app: it is removed and replaced (all weigh-ins alert).
        system.deleteNotificationChannel(OLD_CHANNEL_NORMAL)
        system.createNotificationChannel(
            NotificationChannel(CHANNEL_NORMAL, context.getString(R.string.auto_channel_normal), NotificationManager.IMPORTANCE_DEFAULT).apply {
                group = GROUP_CHANNELS
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
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        // Kumoa brings the notification back without a sound (everyday-feedback design 5).
        if (note.silent) builder.setSilent(true).setOnlyAlertOnce(true)
        // No OK next to Poista paino: a swipe dismisses (the ui skill: no no-op beside a destructive action).
        if (note.questioned) {
            builder.addAction(0, context.getString(R.string.auto_accept), action(note, AutoActionReceiver.ACTION_ACCEPT))
        }
        builder.addAction(0, context.getString(R.string.auto_delete), action(note, AutoActionReceiver.ACTION_DELETE))
        return notify(note.id, builder.build(), summaryTimeoutMs = null)
    }

    /** "Paino poistettu" with Kumoa, silent, gone after the accessibility-lengthened ten seconds. */
    override fun postDeleted(deleted: DeletedWeight): Boolean {
        channels()
        if (!allowed()) return false
        val timeout = ActionTimeouts.millis(ActionTimeouts.RESULT_MS)
        val notification = undoBuilder(deleted, context.getString(R.string.auto_deleted))
            .setTimeoutAfter(timeout)
            .build()
        return notify(deleted.id, notification, summaryTimeoutMs = timeout)
    }

    /** "Painoa ei voitu palauttaa" with Kumoa again; stays until dismissed. */
    override fun postRestoreFailed(deleted: DeletedWeight): Boolean {
        channels()
        if (!allowed()) return false
        return notify(deleted.id, undoBuilder(deleted, context.getString(R.string.auto_restore_failed)).build(), summaryTimeoutMs = null)
    }

    private fun undoBuilder(deleted: DeletedWeight, title: String): NotificationCompat.Builder {
        val open = PendingIntent.getActivity(
            context, deleted.id.hashCode(), MainActivity.historyIntent(context),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(context, if (deleted.questioned) CHANNEL_CHECK else CHANNEL_NORMAL)
            .setSmallIcon(R.drawable.ic_scale_white)
            .setContentTitle(title)
            .setContentIntent(open)
            .setAutoCancel(true)
            .setGroup(GROUP)
            .setWhen(deleted.timeMs)
            .setShowWhen(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .addAction(0, context.getString(R.string.undo), undoAction(deleted))
    }

    /**
     * Posts the weight's notification and the group summary. [summaryTimeoutMs]: when the posted
     * one times out and no other weigh-in notification is shown, the summary times out with it.
     */
    private fun notify(id: String, notification: android.app.Notification, summaryTimeoutMs: Long?): Boolean = try {
        manager.notify(id.hashCode(), notification)
        val others = context.getSystemService(NotificationManager::class.java)?.activeNotifications
            ?.any { it.id != SUMMARY_ID && it.id != id.hashCode() && it.notification.group == GROUP } ?: true
        manager.notify(SUMMARY_ID, summary(if (others) null else summaryTimeoutMs))
        true
    } catch (e: SecurityException) {
        AppLog.e("auto.notify", e, "denied")
        false
    }

    private fun summary(timeoutMs: Long?) = NotificationCompat.Builder(context, CHANNEL_NORMAL)
        .setSmallIcon(R.drawable.ic_scale_white)
        .setContentTitle(context.getString(R.string.auto_summary))
        .setGroup(GROUP)
        .setGroupSummary(true)
        .setSilent(true)
        .setAutoCancel(true)
        .apply { if (timeoutMs != null) setTimeoutAfter(timeoutMs) }
        .setContentIntent(PendingIntent.getActivity(context, 0, MainActivity.historyIntent(context), PendingIntent.FLAG_IMMUTABLE))
        .build()

    /** Kumoa: carries everything the restore needs, one request code per weight and action. */
    private fun undoAction(deleted: DeletedWeight): PendingIntent = PendingIntent.getBroadcast(
        context,
        (deleted.id + AutoActionReceiver.ACTION_UNDO).hashCode(),
        AutoActionReceiver.undoIntent(context, deleted),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

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
        const val CHANNEL_NORMAL = "weighins_alert"
        private const val OLD_CHANNEL_NORMAL = "weighins"
        const val CHANNEL_CHECK = "weighins_check"
        const val GROUP_CHANNELS = "auto"
        const val GROUP = "fi.jukkakot.terveyslappu.WEIGHINS"
        const val SUMMARY_ID = 1
    }
}

/** Poista paino, Kumoa and Hyväksy from a weigh-in notification (not exported: only our PendingIntents reach it). */
class AutoActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as TerveysApp
        // A notification action ends the front page's edit mode (home-card-layout design 4).
        fi.jukkakot.terveyslappu.home.HomeEditSignal.outsideIntent("notification")
        val result = goAsync()
        app.appScope.launch(Dispatchers.IO) {
            try {
                handle(app.auto, app.autoNotifier, intent)
            } catch (e: Exception) {
                AppLog.e("auto.action", e, "failed", "action" to intent.action)
            } finally {
                result.finish()
            }
        }
    }

    companion object {
        /** Poista paino, Kumoa or Hyväksy for the weight the intent names. */
        suspend fun handle(auto: AutoWeights, notifier: AutoNotifier, intent: Intent) {
            val id = intent.getStringExtra(EXTRA_ID) ?: return
            val timeMs = intent.getLongExtra(EXTRA_TIME, 0)
            val kg = intent.getDoubleExtra(EXTRA_KG, 0.0)
            val questioned = intent.getBooleanExtra(EXTRA_QUESTIONED, false)
            when (intent.action) {
                ACTION_DELETE -> auto.delete(id, timeMs, kg, questioned)
                ACTION_UNDO -> auto.restore(
                    DeletedWeight(
                        id, kg, timeMs,
                        offsetSeconds = intent.getIntExtra(EXTRA_OFFSET, 0),
                        key = if (intent.hasExtra(EXTRA_KEY)) intent.getIntExtra(EXTRA_KEY, 0) else null,
                        data = intent.getStringExtra(EXTRA_DATA),
                        questioned = questioned,
                    ),
                )
                // OK is gone (everyday-feedback): a notification posted by an older version just closes.
                ACTION_OK -> notifier.cancel(id)
                ACTION_ACCEPT -> {
                    val accepted = auto.accept(id, AutoWeights.FROM_NOTIFICATION)
                    AppLog.i("auto.action", if (accepted.isEmpty()) "confirm failed" else "confirm ok")
                    // Already accepted elsewhere: the notification still closes.
                    if (accepted.isEmpty()) notifier.cancel(id)
                }
            }
        }

        /** The Kumoa intent of a deleted weight (everyday-feedback design 5). */
        fun undoIntent(context: Context, deleted: DeletedWeight): Intent =
            Intent(context, AutoActionReceiver::class.java)
                .setAction(ACTION_UNDO)
                .putExtra(EXTRA_ID, deleted.id)
                .putExtra(EXTRA_TIME, deleted.timeMs)
                .putExtra(EXTRA_KG, deleted.kg)
                .putExtra(EXTRA_QUESTIONED, deleted.questioned)
                .putExtra(EXTRA_OFFSET, deleted.offsetSeconds)
                .putExtra(EXTRA_DATA, deleted.data)
                .apply { deleted.key?.let { putExtra(EXTRA_KEY, it) } }

        const val ACTION_DELETE = "fi.jukkakot.terveyslappu.auto.DELETE"
        const val ACTION_UNDO = "fi.jukkakot.terveyslappu.auto.UNDO"
        const val ACTION_OK = "fi.jukkakot.terveyslappu.auto.OK"
        const val ACTION_ACCEPT = "fi.jukkakot.terveyslappu.auto.ACCEPT"
        const val EXTRA_ID = "id"
        const val EXTRA_TIME = "time"
        const val EXTRA_KG = "kg"
        const val EXTRA_QUESTIONED = "questioned"
        const val EXTRA_OFFSET = "offset"
        const val EXTRA_KEY = "key"
        const val EXTRA_DATA = "data"
    }
}
