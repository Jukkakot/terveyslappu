package fi.jukkakot.terveyslappu

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import fi.jukkakot.terveyslappu.log.AppLog
import fi.jukkakot.terveyslappu.scale.WeighInState
import fi.jukkakot.terveyslappu.ui.TerveysTheme
import fi.jukkakot.terveyslappu.ui.WeighInActions
import fi.jukkakot.terveyslappu.ui.WeighInDialog
import fi.jukkakot.terveyslappu.ui.WeighInViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The weigh-in dialog (scale-weigh-in spec), opened by the tile, widget, shortcut and main screen.
 * Runs in its own task over whatever is on screen; checks access in order (Health Connect →
 * nearby devices → Bluetooth) and then scans. Scanning stops when the dialog leaves the screen.
 */
class WeighInActivity : ComponentActivity() {

    private val viewModel: WeighInViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        val granted = result.values.all { it }
        AppLog.i("weighin.permission", if (granted) "granted" else "denied")
        if (granted) prepare() else viewModel.show(WeighInState.PermissionDenied)
    }

    private val bluetoothLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        AppLog.i("weighin.bluetooth", if (bluetoothOn()) "on" else "still off")
        if (bluetoothOn()) prepare() else viewModel.show(WeighInState.BluetoothOff)
    }

    private var prepared = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLog.i("weighin.open", "", "from" to (intent.getStringExtra(EXTRA_FROM) ?: "unknown"))
        setContent {
            TerveysTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                val secondsLeft by viewModel.secondsLeft.collectAsStateWithLifecycle()
                var keepOpen by rememberSaveable { mutableStateOf(false) }
                LaunchedEffect(state, keepOpen) {
                    // Discarded, or no weight within the time limit: the countdown said it would close.
                    if (state is WeighInState.Discarded || state is WeighInState.NotFound) finish()
                    if (state is WeighInState.Saved && !keepOpen) {
                        delay(CLOSE_AFTER_SAVED_MS)
                        finish()
                    }
                }
                WeighInDialog(
                    state,
                    secondsLeft = secondsLeft,
                    actions = WeighInActions(
                        onClose = ::finish,
                        onKeepOpen = { if (state is WeighInState.Saved) keepOpen = true },
                        onDiscard = viewModel::discard,
                        onRetryScan = ::prepare,
                        onRetrySave = viewModel::retrySave,
                        onOpenAppSettings = {
                            open(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:$packageName".toUri()))
                        },
                        onEnableBluetooth = ::requestBluetooth,
                        onOpenApp = {
                            open(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            finish()
                        },
                    ),
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Coming back (e.g. from the app settings): scan again unless a weight was already decided.
        val state = viewModel.state.value
        val resume = state is WeighInState.Scanning || state is WeighInState.Live ||
            state is WeighInState.PermissionDenied || state is WeighInState.BluetoothOff
        if (!prepared || resume) prepare()
    }

    override fun onStop() {
        super.onStop()
        val state = viewModel.state.value
        if (state is WeighInState.Scanning || state is WeighInState.Live) viewModel.stop()
    }

    private fun prepare() {
        prepared = true
        viewModel.show(WeighInState.Preparing)
        lifecycleScope.launch {
            when {
                !viewModel.hasHealthAccess() -> viewModel.show(WeighInState.NoHealthAccess)
                !hasNearbyPermission() -> permissionLauncher.launch(NEARBY_PERMISSIONS)
                !bluetoothOn() -> requestBluetooth()
                else -> viewModel.start()
            }
        }
    }

    private fun hasNearbyPermission() = NEARBY_PERMISSIONS.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }

    private fun bluetoothOn(): Boolean = getSystemService(BluetoothManager::class.java)?.adapter?.isEnabled == true

    private fun requestBluetooth() {
        if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(NEARBY_PERMISSIONS)
            return
        }
        try {
            bluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        } catch (e: ActivityNotFoundException) {
            AppLog.e("weighin.bluetooth", e, "no enable dialog")
            viewModel.show(WeighInState.BluetoothOff)
        }
    }

    private fun open(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            AppLog.e("intent.open", e, "", "action" to intent.action)
            Toast.makeText(this, R.string.open_failed, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        /** Which start opened the dialog, for the log: tile, widget, shortcut, main. */
        const val EXTRA_FROM = "from"
        private const val CLOSE_AFTER_SAVED_MS = 10_000L
        private val NEARBY_PERMISSIONS = arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)

        fun intent(context: android.content.Context, from: String): Intent =
            Intent(context, WeighInActivity::class.java)
                .putExtra(EXTRA_FROM, from)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }
}
