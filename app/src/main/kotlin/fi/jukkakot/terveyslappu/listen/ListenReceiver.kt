package fi.jukkakot.terveyslappu.listen

import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.util.size
import fi.jukkakot.terveyslappu.TerveysApp
import fi.jukkakot.terveyslappu.WeighInWidget
import fi.jukkakot.terveyslappu.auto.AutoWeights
import fi.jukkakot.terveyslappu.log.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** The trial's scan results and alarms (not exported: only our own PendingIntents reach it). */
class ListenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as TerveysApp
        val listen = app.listen
        val reason = when (intent.action) {
            ACTION_ALARM -> {
                val action = intent.getStringExtra(EXTRA_ACTION) ?: ListenController.ACTION_CHECK
                listen.replan(action)
                action
            }
            ACTION_SCAN -> {
                val error = intent.getIntExtra(BluetoothLeScanner.EXTRA_ERROR_CODE, 0)
                if (error != 0) listen.onScanError(error)
                val results = intent.getParcelableArrayListExtra(BluetoothLeScanner.EXTRA_LIST_SCAN_RESULT, ScanResult::class.java)
                if (!results.isNullOrEmpty()) listen.onResults(results.flatMap(::heard))
                AutoWeights.WEIGHIN
            }
            else -> return
        }
        flush(app, reason)
    }

    /**
     * Writes what waits in the pending queue (a weight decided just now, or an earlier failed one)
     * within the receiver's time budget (design 1, 11), then redraws the widget.
     */
    private fun flush(app: TerveysApp, reason: String) {
        if (app.pendingWeights.count() == 0) {
            if (reason != AutoWeights.WEIGHIN) WeighInWidget.update(app)
            return
        }
        val result = goAsync()
        app.appScope.launch(Dispatchers.IO) {
            try {
                app.auto.flush(reason)
            } catch (e: Exception) {
                AppLog.e("auto.save", e, "flush")
            } finally {
                WeighInWidget.update(app)
                result.finish()
            }
        }
    }

    private fun heard(result: ScanResult): List<HeardResult> {
        val record = result.scanRecord ?: return emptyList()
        val data = record.manufacturerSpecificData ?: return emptyList()
        return (0 until data.size).map { i ->
            HeardResult(data.keyAt(i), data.valueAt(i), result.rssi, record.deviceName, result.device?.address, result.timestampNanos)
        }
    }

    companion object {
        const val ACTION_ALARM = "fi.jukkakot.terveyslappu.listen.ALARM"
        const val ACTION_SCAN = "fi.jukkakot.terveyslappu.listen.SCAN"
        const val EXTRA_ACTION = "action"
    }
}

/** Re-arms the trial after a phone restart or an app update, without opening the app (design 5). */
class ListenBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reason = when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> "boot"
            Intent.ACTION_MY_PACKAGE_REPLACED -> "update"
            else -> return
        }
        val app = context.applicationContext as TerveysApp
        app.listen.replan(reason)
        WeighInWidget.update(app)
    }
}
