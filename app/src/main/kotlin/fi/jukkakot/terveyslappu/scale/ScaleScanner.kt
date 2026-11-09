package fi.jukkakot.terveyslappu.scale

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.SystemClock
import androidx.annotation.RequiresPermission
import fi.jukkakot.terveyslappu.log.AppLog
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Where the weigh-in gets scale packets from: the phone's Bluetooth, or a fake/replay in tests. */
fun interface PacketSource {
    /** Packets until the collector stops; fails with [ScanFailedException] when the scan cannot run. */
    fun packets(): Flow<RawPacket>
}

class ScanFailedException(val code: Int) : Exception("BLE scan failed, code $code")

/**
 * Low-latency Bluetooth LE scan while it is collected (design decision 2). No scan filter: the
 * scale's name may only be in the scan response and its company id varies, so packets are picked
 * in code. Every scale-like packet is logged raw (NFR-7); other devices are only counted.
 */
class ScaleScanner(private val context: Context) : PacketSource {

    @SuppressLint("MissingPermission") // WeighInActivity checks BLUETOOTH_SCAN before collecting.
    override fun packets(): Flow<RawPacket> = callbackFlow {
        val scanner = context.getSystemService(BluetoothManager::class.java)?.adapter?.bluetoothLeScanner
            ?: throw ScanFailedException(NO_SCANNER)
        val startNanos = SystemClock.elapsedRealtimeNanos()
        val otherDevices = HashSet<String>()
        var scalePackets = 0

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val atMs = ((result.timestampNanos - startNanos) / 1_000_000).coerceAtLeast(0)
                val name = result.scanRecord?.deviceName
                val data = result.scanRecord?.manufacturerSpecificData
                var matched = false
                if (data != null) {
                    for (i in 0 until data.size()) {
                        val companyId = data.keyAt(i)
                        val looksLikeScale = (companyId and 0xFF) == 0xC0 || name?.startsWith("Yoda", ignoreCase = true) == true
                        if (!looksLikeScale) continue
                        matched = true
                        scalePackets++
                        val packet = RawPacket(atMs, companyId, data.valueAt(i), result.rssi, name, result.device.address)
                        AppLog.d(PacketLog.EVENT, "", *PacketLog.fields(packet))
                        trySend(packet)
                    }
                }
                if (!matched) otherDevices += result.device.address
            }

            override fun onScanFailed(errorCode: Int) {
                AppLog.w("scale.scan", "failed", "code" to errorCode)
                close(ScanFailedException(errorCode))
            }
        }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setReportDelay(0)
            .build()
        AppLog.i("scale.scan", "start")
        startScan(scanner, settings, callback)
        awaitClose {
            runCatching { stopScan(scanner, callback) }.onFailure { AppLog.e("scale.scan", it, "stop failed") }
            AppLog.i(
                "scale.scan", "stop",
                "ms" to (SystemClock.elapsedRealtimeNanos() - startNanos) / 1_000_000,
                "scalePackets" to scalePackets,
                "otherDevices" to otherDevices.size,
            )
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun startScan(scanner: android.bluetooth.le.BluetoothLeScanner, settings: ScanSettings, callback: ScanCallback) =
        scanner.startScan(null, settings, callback)

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun stopScan(scanner: android.bluetooth.le.BluetoothLeScanner, callback: ScanCallback) =
        scanner.stopScan(callback)

    companion object {
        /** Our own code: Bluetooth is off or the phone has no LE scanner. */
        const val NO_SCANNER = -1
    }
}
