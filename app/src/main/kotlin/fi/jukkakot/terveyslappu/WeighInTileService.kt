package fi.jukkakot.terveyslappu

import android.app.PendingIntent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import fi.jukkakot.terveyslappu.log.AppLog
import fi.jukkakot.terveyslappu.ui.formatDay
import fi.jukkakot.terveyslappu.ui.formatKg
import java.time.LocalDate

/** Quick Settings tile "Punnitse": collapses the panel and opens the weigh-in dialog. */
class WeighInTileService : TileService() {

    override fun onStartListening() {
        qsTile?.apply {
            state = Tile.STATE_INACTIVE
            subtitle = LastWeight.load(this@WeighInTileService)?.let { (kg, time) ->
                getString(R.string.tile_subtitle, formatKg(kg), formatDay(time, LocalDate.now()))
            }
            updateTile()
        }
    }

    override fun onClick() {
        AppLog.i("tile.click")
        val pending = PendingIntent.getActivity(
            this, 0, WeighInActivity.intent(this, "tile"),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        try {
            startActivityAndCollapse(pending)
        } catch (e: RuntimeException) {
            AppLog.e("tile.click", e, "open failed")
        }
    }
}
