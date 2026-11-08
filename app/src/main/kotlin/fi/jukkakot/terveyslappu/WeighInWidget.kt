package fi.jukkakot.terveyslappu

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import fi.jukkakot.terveyslappu.ui.formatDay
import fi.jukkakot.terveyslappu.ui.formatKg
import java.time.LocalDate

/** Home-screen widget: "Punnitse" and the latest weight with its day; a tap opens the weigh-in. */
class WeighInWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        manager.updateAppWidget(ids, views(context))
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context) ?: return
            val ids = manager.getAppWidgetIds(ComponentName(context, WeighInWidget::class.java))
            if (ids.isNotEmpty()) manager.updateAppWidget(ids, views(context))
        }

        /** The launcher picks the layout by the widget's current size (widget-sizes). */
        private fun views(context: Context): RemoteViews = RemoteViews(
            mapOf(
                SizeF(40f, 40f) to layout(context, R.layout.widget_weigh_in_small, withDay = false),
                SizeF(40f, 100f) to layout(context, R.layout.widget_weigh_in, withDay = true),
            ),
        )

        private fun layout(context: Context, layoutId: Int, withDay: Boolean): RemoteViews {
            val pending = PendingIntent.getActivity(
                context, 1, WeighInActivity.intent(context, "widget"),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            return RemoteViews(context.packageName, layoutId).apply {
                setOnClickPendingIntent(R.id.widget_root, pending)
                val last = LastWeight.load(context)
                setViewVisibility(R.id.widget_weight, if (last == null) View.GONE else View.VISIBLE)
                if (withDay) setViewVisibility(R.id.widget_day, if (last == null) View.GONE else View.VISIBLE)
                if (last != null) {
                    setTextViewText(R.id.widget_weight, context.getString(R.string.widget_weight, formatKg(last.first)))
                    if (withDay) setTextViewText(R.id.widget_day, formatDay(last.second, LocalDate.now()).replaceFirstChar { it.uppercase() })
                }
            }
        }
    }
}
