package fi.jukkakot.terveyslappu

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews

/** Home-screen widget: one "Punnitse" button that opens the weigh-in dialog. */
class WeighInWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val pending = PendingIntent.getActivity(
            context, 1, WeighInActivity.intent(context, "widget"),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val views = RemoteViews(context.packageName, R.layout.widget_weigh_in).apply {
            setOnClickPendingIntent(R.id.widget_root, pending)
        }
        manager.updateAppWidget(ids, views)
    }
}
