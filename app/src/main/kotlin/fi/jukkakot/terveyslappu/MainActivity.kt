package fi.jukkakot.terveyslappu

import android.content.ActivityNotFoundException
import android.content.Intent
import android.health.connect.HealthConnectManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
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
