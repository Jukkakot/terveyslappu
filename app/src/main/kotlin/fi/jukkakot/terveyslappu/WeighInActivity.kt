package fi.jukkakot.terveyslappu

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import fi.jukkakot.terveyslappu.scale.WeighInForeground
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import fi.jukkakot.terveyslappu.ui.closeWhenDue
import fi.jukkakot.terveyslappu.ui.closesOnRequest
import fi.jukkakot.terveyslappu.ui.blocksBackground
import fi.jukkakot.terveyslappu.ui.endReason
import fi.jukkakot.terveyslappu.a11y.ActionTimeouts
import android.os.SystemClock
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // home-card-layout design 4: the front page's edit mode ends and drops its draft.
        if (savedInstanceState == null) fi.jukkakot.terveyslappu.home.HomeEditSignal.outsideIntent("weighIn")
        AppLog.i("weighin.open", "", "from" to (intent.getStringExtra(EXTRA_FROM) ?: "unknown"))
        // The background saves nothing while the dialog could save the weighing itself (design 10).
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { WeighInForeground.shown = blocksBackground(it) }
            }
        }
        setContent {
            TerveysTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                val secondsLeft by viewModel.secondsLeft.collectAsStateWithLifecycle()
                var keepOpen by rememberSaveable { mutableStateOf(false) }
                var resultLeft by remember { mutableStateOf<Int?>(null) }
                // Discarded at once; the saved result or the recent automatic weight after the
                // accessibility-lengthened ten seconds, counted down on Valmis. The end view stays.
                LaunchedEffect(state, keepOpen) {
                    resultLeft = null
                    closeWhenDue(state, keepOpen, ActionTimeouts.millis(ActionTimeouts.RESULT_MS), onSeconds = { resultLeft = it }) {
                        resultLeft = null
                        finish()
                    }
                }
                // While saving, Back does nothing (the save finishes in the app scope either way).
                BackHandler(enabled = !closesOnRequest(state)) {}
                WeighInDialog(
                    state,
                    secondsLeft = secondsLeft,
                    resultSecondsLeft = resultLeft,
                    actions = WeighInActions(
                        onClose = { if (closesOnRequest(viewModel.state.value)) finish() },
                        onKeepOpen = { if (state is WeighInState.Saved || state is WeighInState.Recent) keepOpen = true },
                        // Poista stops the auto-close so it cannot cut the delete short.
                        onDiscard = {
                            keepOpen = true
                            if (viewModel.state.value is WeighInState.Recent) viewModel.discardRecent() else viewModel.discard()
                        },
                        onWeighAgain = {
                            keepOpen = false
                            weighAgain()
                        },
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
        if (!viewModel.prepared || resume) prepare()
    }

    override fun onStop() {
        super.onStop()
        // The collector stops with the lifecycle; the background may save again.
        WeighInForeground.shown = false
        val state = viewModel.state.value
        if (state is WeighInState.Scanning || state is WeighInState.Live) viewModel.stop()
    }

    override fun onDestroy() {
        // Closed for good, not recreated (rotation): the weigh-in ends here.
        if (isFinishing) logEnd()
        super.onDestroy()
    }

    /** `weighin.end` once per weigh-in (scale-weigh-in "Weigh-in end in the log"). */
    private fun logEnd() {
        if (viewModel.endLogged) return
        viewModel.endLogged = true
        val state = viewModel.state.value
        AppLog.i(
            "weighin.end", "",
            "reason" to endReason(state, viewModel.fromRecent),
            "heard" to viewModel.heard,
            "ignored" to viewModel.ignored,
            "ms" to SystemClock.elapsedRealtime() - viewModel.openedMs,
        )
    }

    /** Punnitse uudelleen: ends this weigh-in in the log and checks access again before a new scan, without the recent weight. */
    private fun weighAgain() {
        logEnd()
        viewModel.endLogged = false
        viewModel.fromRecent = false
        viewModel.recentChecked = true
        viewModel.openedMs = SystemClock.elapsedRealtime()
        AppLog.i("weighin.open", "", "from" to "again")
        prepare()
    }

    private fun prepare() {
        // A recreated dialog (rotation) keeps a decided weight: never prepare or scan it again.
        if (viewModel.decided) return
        viewModel.prepared = true
        viewModel.show(WeighInState.Preparing)
        lifecycleScope.launch {
            when {
                !viewModel.hasHealthAccess() -> viewModel.show(WeighInState.NoHealthAccess)
                // Right after an automatic save the dialog shows that weight instead of scanning (once).
                viewModel.showRecentIfAny() -> Unit
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
        private val NEARBY_PERMISSIONS = arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)

        fun intent(context: android.content.Context, from: String): Intent =
            Intent(context, WeighInActivity::class.java)
                .putExtra(EXTRA_FROM, from)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }
}
