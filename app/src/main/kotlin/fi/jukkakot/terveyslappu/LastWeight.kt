package fi.jukkakot.terveyslappu

import android.content.ComponentName
import android.content.Context
import android.service.quicksettings.TileService
import androidx.core.content.edit
import fi.jukkakot.terveyslappu.log.AppLog
import java.time.Instant

/**
 * The latest weight the app knows, for the widget and the tile (weigh-in-feedback). Kept in
 * preferences because the widget and tile cannot query Health Connect themselves.
 */
object LastWeight {
    private const val PREFS = "last_weight"
    private const val KG = "kg"
    private const val AT = "at"

    fun load(context: Context): Pair<Double, Instant>? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val at = prefs.getLong(AT, 0)
        if (at == 0L) return null
        return Double.fromBits(prefs.getLong(KG, 0)) to Instant.ofEpochMilli(at)
    }

    /** A weight just saved: kept when it is at least as new as the known one. */
    fun offer(context: Context, kg: Double, time: Instant) {
        val known = load(context)
        if (known == null || !time.isBefore(known.second)) replace(context, kg to time)
    }

    /** The newest weight read from Health Connect (null: none in the list), replacing the known one. */
    fun replace(context: Context, value: Pair<Double, Instant>?) {
        if (value == load(context)) return
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit {
            if (value == null) clear() else putLong(KG, value.first.toBits()).putLong(AT, value.second.toEpochMilli())
        }
        refreshViews(context)
    }

    private fun refreshViews(context: Context) {
        WeighInWidget.updateAll(context)
        runCatching { TileService.requestListeningState(context, ComponentName(context, WeighInTileService::class.java)) }
            .onFailure { AppLog.w("tile.refresh", "failed", "error" to it.toString()) }
    }
}
