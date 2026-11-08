package fi.jukkakot.terveyslappu

import android.content.ActivityNotFoundException
import android.content.Intent
import android.health.connect.HealthConnectManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import fi.jukkakot.terveyslappu.weight.HISTORY_PERMISSION
import fi.jukkakot.terveyslappu.weight.HistoryAccess
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.net.toUri
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fi.jukkakot.terveyslappu.log.AppLog
import fi.jukkakot.terveyslappu.log.shareLogIntent
import fi.jukkakot.terveyslappu.ui.MainActions
import fi.jukkakot.terveyslappu.ui.MainScreen
import fi.jukkakot.terveyslappu.ui.MainViewModel
import fi.jukkakot.terveyslappu.ui.TerveysTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TerveysTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                val snackbar = remember { SnackbarHostState() }
                val permissionLauncher = rememberLauncherForActivityResult(
                    PermissionController.createRequestPermissionResultContract(),
                ) { granted -> viewModel.onPermissionResult(granted) }
                val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
                    AppLog.i("export.pick", if (uri == null) "cancelled" else "picked")
                    if (uri != null) viewModel.export(uri)
                }
                val historyLauncher = rememberLauncherForActivityResult(
                    PermissionController.createRequestPermissionResultContract(),
                ) { granted ->
                    AppLog.i("export.history", if (HISTORY_PERMISSION in granted) "granted" else "denied")
                    launchSafely("export.pick") { exportLauncher.launch(exportFileName()) }
                }
                val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                    AppLog.i("import.pick", if (uri == null) "cancelled" else "picked")
                    if (uri != null) viewModel.onImportFile(uri)
                }

                LifecycleResumeEffect(Unit) {
                    viewModel.refreshAccess()
                    onPauseOrDispose {}
                }
                LaunchedEffect(Unit) {
                    viewModel.messages.collect { message ->
                        snackbar.showSnackbar(getString(message.text, *message.args.toTypedArray()))
                    }
                }

                MainScreen(
                    state = state,
                    versionName = BuildConfig.VERSION_NAME,
                    snackbar = snackbar,
                    actions = MainActions(
                        onWeightText = viewModel::onWeightText,
                        onPickedTime = viewModel::onPickedTime,
                        onSave = viewModel::save,
                        onDelete = viewModel::delete,
                        onRequestPermission = {
                            AppLog.i("hc.permission.request")
                            permissionLauncher.launch(viewModel.weights.permissions)
                        },
                        onOpenSettings = { open(Intent(HealthConnectManager.ACTION_MANAGE_HEALTH_PERMISSIONS).putExtra(Intent.EXTRA_PACKAGE_NAME, packageName)) },
                        onOpenStore = { open(Intent(Intent.ACTION_VIEW, STORE_URL.toUri())) },
                        onShareLog = ::shareLog,
                        onWeighIn = { open(WeighInActivity.intent(this, "main")) },
                        onImportHistory = {
                            try {
                                importLauncher.launch(IMPORT_TYPES)
                            } catch (e: ActivityNotFoundException) {
                                AppLog.e("import.pick", e, "no file picker")
                                Toast.makeText(this, R.string.open_failed, Toast.LENGTH_LONG).show()
                            }
                        },
                        onExport = {
                            lifecycleScope.launch {
                                val history = viewModel.historyAccess()
                                if (history == HistoryAccess.NOT_GRANTED && !viewModel.historyAsked) {
                                    viewModel.historyAsked = true
                                    launchSafely("export.history") { historyLauncher.launch(setOf(HISTORY_PERMISSION)) }
                                } else {
                                    launchSafely("export.pick") { exportLauncher.launch(exportFileName()) }
                                }
                            }
                        },
                        onConfirmImport = viewModel::confirmImport,
                        onDismissImport = viewModel::dismissImport,
                    ),
                )
            }
        }
    }

    private fun shareLog() {
        AppLog.i("log.share")
        AppLog.flush()
        val files = AppLog.logFile?.files().orEmpty()
        if (files.isEmpty()) {
            AppLog.w("log.share", "no log file")
            Toast.makeText(this, R.string.log_empty, Toast.LENGTH_LONG).show()
        } else {
            open(shareLogIntent(this, files))
        }
    }

    /** Starts a picker or permission screen; a missing one is logged and shown (NFR-9). */
    private fun launchSafely(event: String, launch: () -> Unit) {
        try {
            launch()
        } catch (e: ActivityNotFoundException) {
            AppLog.e(event, e, "no activity")
            Toast.makeText(this, R.string.open_failed, Toast.LENGTH_LONG).show()
        }
    }

    private fun exportFileName() = "terveyslappu-painot-${LocalDate.now()}.csv"

    private fun open(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            AppLog.e("intent.open", e, "", "action" to intent.action)
            Toast.makeText(this, R.string.open_failed, Toast.LENGTH_LONG).show()
        }
    }

    private companion object {
        /** Text tables; some file managers label CSV files as octet-stream. */
        val IMPORT_TYPES = arrayOf("text/*", "application/csv", "text/comma-separated-values", "application/octet-stream")
        const val STORE_URL = "market://details?id=com.google.android.apps.healthdata&url=healthconnect%3A%2F%2Fonboarding"
    }
}
