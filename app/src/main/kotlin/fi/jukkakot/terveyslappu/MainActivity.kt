package fi.jukkakot.terveyslappu

import fi.jukkakot.terveyslappu.weight.HEIGHT_WRITE_PERMISSION
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
import fi.jukkakot.terveyslappu.data.DataFile
import fi.jukkakot.terveyslappu.data.DataFormat
import java.time.LocalDateTime
import fi.jukkakot.terveyslappu.backup.keepAccess
import fi.jukkakot.terveyslappu.backup.releaseAccess
import fi.jukkakot.terveyslappu.weight.HISTORY_PERMISSION
import fi.jukkakot.terveyslappu.weight.HistoryAccess
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.net.toUri
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fi.jukkakot.terveyslappu.log.AppLog
import fi.jukkakot.terveyslappu.log.shareLogIntent
import android.provider.Settings
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import fi.jukkakot.terveyslappu.ui.ListenFix
import fi.jukkakot.terveyslappu.ui.AfterHistory
import fi.jukkakot.terveyslappu.ui.MainActions
import kotlinx.coroutines.delay
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
                val backupStatus by viewModel.backup.status.collectAsStateWithLifecycle()
                val appearance = (application as TerveysApp).appearance
                val themeMode by appearance.mode.collectAsStateWithLifecycle()
                val profile by viewModel.profile.profile.collectAsStateWithLifecycle()
                val heightInHc by viewModel.profile.heightInHealthConnect.collectAsStateWithLifecycle()
                val heightLauncher = rememberLauncherForActivityResult(
                    PermissionController.createRequestPermissionResultContract(),
                ) { granted -> if (HEIGHT_WRITE_PERMISSION in granted) viewModel.writeHeight() else viewModel.heightDenied() }
                val snackbar = remember { SnackbarHostState() }
                // The listen trial's section: re-read on every change, on resume and every minute (background-weigh-in).
                val listen = (application as TerveysApp).listen
                val listenVersion by listen.changes.collectAsStateWithLifecycle()
                var listenTick by remember { mutableIntStateOf(0) }
                LaunchedEffect(Unit) {
                    while (true) {
                        delay(60_000)
                        listenTick++
                    }
                }
                val listenUi = remember(listenVersion, listenTick) { listen.ui() }
                val permissionLauncher = rememberLauncherForActivityResult(
                    PermissionController.createRequestPermissionResultContract(),
                ) { granted -> viewModel.onPermissionResult(granted) }
                val exportLaunchers = DataFormat.entries.associateWith { format ->
                    rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(format.mimeType)) { uri ->
                        AppLog.i("export.pick", if (uri == null) "cancelled" else "picked", "format" to format.extension)
                        if (uri != null) viewModel.export(uri, format)
                    }
                }
                val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
                    AppLog.i("backup.pick", if (uri == null) "cancelled" else "picked")
                    if (uri != null) {
                        val old = viewModel.backup.status.value.uri
                        if (keepAccess(uri)) {
                            if (old != null && old != uri.toString()) releaseAccess(old)
                            viewModel.backup.enable(uri.toString())
                        } else {
                            Toast.makeText(this, R.string.backup_no_access, Toast.LENGTH_LONG).show()
                        }
                    }
                }
                fun perform(action: AfterHistory) {
                    when {
                        action == AfterHistory.RELOAD -> viewModel.onHistoryResult()
                        action == AfterHistory.BACKUP_FILE -> launchSafely("backup.pick") { backupLauncher.launch(BACKUP_FILE_NAME) }
                        action.format != null -> launchSafely("export.pick") {
                            exportLaunchers.getValue(action.format!!).launch(DataFile.exportName(action.format!!, LocalDateTime.now()))
                        }
                    }
                }
                val historyLauncher = rememberLauncherForActivityResult(
                    PermissionController.createRequestPermissionResultContract(),
                ) { granted ->
                    AppLog.i("export.history", if (HISTORY_PERMISSION in granted) "granted" else "denied")
                    val pending = viewModel.afterHistory
                    viewModel.afterHistory = null
                    if (pending != null) perform(pending)
                }
                /** Asks for the history permission once when it is missing, then does [action] (export, backup). */
                fun withHistory(action: AfterHistory) {
                    lifecycleScope.launch {
                        if (viewModel.historyAccess() == HistoryAccess.NOT_GRANTED && !viewModel.historyAsked) {
                            viewModel.historyAsked = true
                            viewModel.afterHistory = action
                            launchSafely("export.history") { historyLauncher.launch(setOf(HISTORY_PERMISSION)) }
                        } else {
                            perform(action)
                        }
                    }
                }
                val pickBackupFile = { withHistory(AfterHistory.BACKUP_FILE) }
                val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                    AppLog.i("import.pick", if (uri == null) "cancelled" else "picked")
                    if (uri != null) viewModel.onImportFile(uri)
                }

                LifecycleResumeEffect(Unit) {
                    viewModel.refreshAccess()
                    listenTick++
                    onPauseOrDispose {}
                }
                LaunchedEffect(Unit) {
                    viewModel.messages.collect { message ->
                        val result = snackbar.showSnackbar(
                            message = getString(message.text, *message.args.toTypedArray()),
                            actionLabel = message.action?.let { getString(it.label) },
                            duration = if (message.action != null) SnackbarDuration.Long else SnackbarDuration.Short,
                        )
                        if (result == SnackbarResult.ActionPerformed) message.action?.run?.invoke()
                    }
                }

                MainScreen(
                    state = state,
                    backup = backupStatus,
                    versionName = BuildConfig.VERSION_NAME,
                    snackbar = snackbar,
                    themeMode = themeMode,
                    profile = profile,
                    heightInHealthConnect = heightInHc,
                    listen = listenUi,
                    actions = MainActions(
                        onScreen = viewModel::onScreen,
                        onOpenProfile = viewModel::openProfile,
                        onOpenEntry = viewModel::openEntry,
                        onCloseEntry = viewModel::closeEntry,
                        onProfile = { new ->
                            if (viewModel.setProfile(new)) {
                                lifecycleScope.launch {
                                    if (viewModel.hasHeightPermission()) {
                                        viewModel.writeHeight()
                                    } else {
                                        launchSafely("profile.height.hc") { heightLauncher.launch(setOf(HEIGHT_WRITE_PERMISSION)) }
                                    }
                                }
                            }
                        },
                        onTheme = { mode ->
                            AppLog.i("theme.set", mode.name)
                            appearance.set(mode)
                        },
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
                        onExport = { format -> withHistory(AfterHistory.export(format)) },
                        onBackupEnable = pickBackupFile,
                        onBackupChangeFile = pickBackupFile,
                        onBackupNow = { viewModel.backup.request("manual") },
                        onBackupDisable = {
                            viewModel.backup.status.value.uri?.let { releaseAccess(it) }
                            viewModel.backup.disable()
                        },
                        onConfirmImport = viewModel::confirmImport,
                        onDismissImport = viewModel::dismissImport,
                        onPeriod = viewModel::onPeriod,
                        onHomePeriod = viewModel::onHomePeriod,
                        onListenEnabled = listen::setEnabled,
                        onListenSettings = listen::updateSettings,
                        onListenFix = { fix ->
                            open(
                                when (fix) {
                                    ListenFix.BLUETOOTH -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                                    ListenFix.POWER_SAVE -> Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
                                    ListenFix.PERMISSION, ListenFix.BATTERY ->
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:$packageName".toUri())
                                },
                            )
                        },
                        onGrantHistory = {
                            viewModel.afterHistory = AfterHistory.RELOAD
                            launchSafely("export.history") { historyLauncher.launch(setOf(HISTORY_PERMISSION)) }
                        },
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
        const val BACKUP_FILE_NAME = "terveyslappu-varmuuskopio.csv"
        val IMPORT_TYPES = arrayOf("text/*", "application/csv", "text/comma-separated-values", "application/vnd.ms-excel", "application/json", "application/octet-stream")
        const val STORE_URL = "market://details?id=com.google.android.apps.healthdata&url=healthconnect%3A%2F%2Fonboarding"
    }
}
