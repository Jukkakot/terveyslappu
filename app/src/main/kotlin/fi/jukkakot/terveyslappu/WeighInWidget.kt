package fi.jukkakot.terveyslappu

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

/** Home-screen widget: a plain "Punnitse" button that opens the weigh-in; it never shows a weight. */
class WeighInWidget : AppWidgetProvider() {

    /** After an app update some launchers keep the old views until the widget is re-added: redraw. */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val manager = AppWidgetManager.getInstance(context) ?: return
            onUpdate(context, manager, manager.getAppWidgetIds(ComponentName(context, WeighInWidget::class.java)))
            return
        }
        super.onReceive(context, intent)
    }

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
