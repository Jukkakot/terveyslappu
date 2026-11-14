package fi.jukkakot.terveyslappu

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import fi.jukkakot.terveyslappu.log.AppLog

/**
 * Home-screen widget "Lisää annos" (food-widget): Punnitse's round plate with a cutlery icon that
 * opens [FoodAddActivity]. It never shows food, kcal, a count or a state mark, so no food code
 * updates it: it is drawn only when placed and after an app update.
 */
class FoodAddWidget : AppWidgetProvider() {

    /** After an app update some launchers keep the old views until the widget is re-added: redraw. */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            draw(context)
            return
        }
        super.onReceive(context, intent)
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        draw(context)
    }

    private fun draw(context: Context) {
        try {
            val manager = AppWidgetManager.getInstance(context) ?: return
            val ids = manager.getAppWidgetIds(ComponentName(context, FoodAddWidget::class.java))
            if (ids.isEmpty()) return
            val pending = PendingIntent.getActivity(
                context, 2, FoodAddActivity.intent(context, "widget"),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            val views = RemoteViews(context.packageName, R.layout.widget_food_add).apply {
                setOnClickPendingIntent(R.id.widget_root, pending)
            }
            manager.updateAppWidget(ids, views)
        } catch (e: Exception) {
            AppLog.e("widget.food", e, "draw")
        }
    }
}
