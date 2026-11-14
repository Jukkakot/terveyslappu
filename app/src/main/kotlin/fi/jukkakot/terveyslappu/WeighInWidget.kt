package fi.jukkakot.terveyslappu

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.edit
import fi.jukkakot.terveyslappu.listen.WidgetState
import fi.jukkakot.terveyslappu.listen.WidgetStateRule
import fi.jukkakot.terveyslappu.log.AppLog

/**
 * Home-screen widget: a "Punnitse" button that opens the weigh-in; it never shows a weight. While
 * automatic weigh-in is on, a small mark shows its state (Päällä, Lepää, Estetty: background-weigh-in
 * "Widget status"), redrawn by [update] at the listen alarms, boot, update, app resume, settings
 * changes, automatic weigh-ins and notification actions (design 10).
 */
class WeighInWidget : AppWidgetProvider() {

    /** After an app update some launchers keep the old views until the widget is re-added: redraw. */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            update(context)
            return
        }
        super.onReceive(context, intent)
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        update(context)
    }

    companion object {
        private const val PREFS = "widget"
        private const val STATE = "state"
        private const val END = "window_end"

        /** Redraws every placed widget with the current state; cheap, so callers need not check. */
        fun update(context: Context) {
            try {
                val manager = AppWidgetManager.getInstance(context) ?: return
                val ids = manager.getAppWidgetIds(ComponentName(context, WeighInWidget::class.java))
                if (ids.isEmpty()) return
                val state = state(context)
                manager.updateAppWidget(ids, views(context, state))
            } catch (e: Exception) {
                AppLog.e("widget.state", e, "update")
            }
        }

        /**
         * The state from the Asetukset status; stored with the window end so a redraw after the end
         * (from the stored value, e.g. when the status cannot be read) shows Lepää.
         */
        private fun state(context: Context): WidgetState? {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val now = System.currentTimeMillis()
            val fresh = runCatching { (context.applicationContext as TerveysApp).listen.ui() }
                .onFailure { AppLog.e("widget.state", it, "status") }
                .getOrNull()
            val state = if (fresh != null) {
                fresh.widget
            } else {
                WidgetStateRule.shown(WidgetState.of(prefs.getString(STATE, null)), prefs.getLong(END, 0).takeIf { it > 0 }, now)
            }
            val old = WidgetState.of(prefs.getString(STATE, null))
            if (fresh != null) {
                prefs.edit {
                    putString(STATE, state?.key)
                    putLong(END, fresh.windowEndMs ?: 0)
                }
            }
            if (state != old) AppLog.i("widget.state", "", "state" to (state?.key ?: "off"))
            return state
        }

        private fun views(context: Context, state: WidgetState?): RemoteViews {
            val pending = PendingIntent.getActivity(
                context, 1, WeighInActivity.intent(context, "widget"),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            return RemoteViews(context.packageName, R.layout.widget_weigh_in).apply {
                setOnClickPendingIntent(R.id.widget_root, pending)
                if (state == null) {
                    setViewVisibility(R.id.widget_state, View.GONE)
                    setContentDescription(R.id.widget_root, context.getString(R.string.weighin_action))
                } else {
                    val (icon, color, label) = when (state) {
                        WidgetState.ON -> Triple(R.drawable.ic_state_on, R.color.widget_state_on, R.string.widget_state_on)
                        WidgetState.DONE -> Triple(R.drawable.ic_state_done, R.color.widget_state_done, R.string.widget_state_done)
                        WidgetState.RESTING -> Triple(R.drawable.ic_state_rest, R.color.widget_state_rest, R.string.widget_state_rest)
                        WidgetState.BLOCKED -> Triple(R.drawable.ic_state_blocked, R.color.widget_state_blocked, R.string.widget_state_blocked)
                    }
                    setViewVisibility(R.id.widget_state, View.VISIBLE)
                    setImageViewResource(R.id.widget_state, icon)
                    setInt(R.id.widget_state, "setColorFilter", context.getColor(color))
                    setContentDescription(
                        R.id.widget_root,
                        context.getString(R.string.widget_state_description, context.getString(R.string.weighin_action), context.getString(label)),
                    )
                }
            }
        }
    }
}
