package fi.jukkakot.terveyslappu.listen

import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import fi.jukkakot.terveyslappu.TerveysApp

/** The trial's scan results and alarms (not exported: only our own PendingIntents reach it). */
class ListenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val listen = (context.applicationContext as TerveysApp).listen
        when (intent.action) {
            ACTION_ALARM -> listen.replan(intent.getStringExtra(EXTRA_ACTION) ?: ListenController.ACTION_CHECK)
            ACTION_SCAN -> {
                val error = intent.getIntExtra(BluetoothLeScanner.EXTRA_ERROR_CODE, 0)
                if (error != 0) listen.onScanError(error)
                val results = intent.getParcelableArrayListExtra(BluetoothLeScanner.EXTRA_LIST_SCAN_RESULT, ScanResult::class.java)
                if (!results.isNullOrEmpty()) listen.onResults(results.flatMap(::heard))
            }
        }
    }

    private fun heard(result: ScanResult): List<HeardResult> {
        val record = result.scanRecord ?: return emptyList()
        val data = record.manufacturerSpecificData ?: return emptyList()
        return (0 until data.size()).map { i ->
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
        val listen = (context.applicationContext as TerveysApp).listen
        listen.replan(reason)
    }
}
