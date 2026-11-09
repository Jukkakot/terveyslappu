package fi.jukkakot.terveyslappu

import android.app.PendingIntent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import fi.jukkakot.terveyslappu.log.AppLog

/** Quick Settings tile "Punnitse": collapses the panel and opens the weigh-in dialog. */
class WeighInTileService : TileService() {

    override fun onStartListening() {
        qsTile?.apply {
            state = Tile.STATE_INACTIVE
            subtitle = null
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
