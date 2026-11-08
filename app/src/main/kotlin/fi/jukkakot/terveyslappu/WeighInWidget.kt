package fi.jukkakot.terveyslappu

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
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

        private fun views(context: Context): RemoteViews {
            val pending = PendingIntent.getActivity(
                context, 1, WeighInActivity.intent(context, "widget"),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            return RemoteViews(context.packageName, R.layout.widget_weigh_in).apply {
                setOnClickPendingIntent(R.id.widget_root, pending)
                val last = LastWeight.load(context)
                if (last == null) {
                    setViewVisibility(R.id.widget_weight, View.GONE)
                    setViewVisibility(R.id.widget_day, View.GONE)
                } else {
                    setViewVisibility(R.id.widget_weight, View.VISIBLE)
                    setViewVisibility(R.id.widget_day, View.VISIBLE)
                    setTextViewText(R.id.widget_weight, context.getString(R.string.widget_weight, formatKg(last.first)))
                    setTextViewText(R.id.widget_day, formatDay(last.second, LocalDate.now()).replaceFirstChar { it.uppercase() })
                }
            }
        }
    }
}
