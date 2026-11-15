package fi.jukkakot.terveyslappu

import fi.jukkakot.terveyslappu.weight.HEIGHT_WRITE_PERMISSION
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import fi.jukkakot.terveyslappu.ui.Screen
import android.content.ClipData
import android.content.ClipboardManager
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
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import fi.jukkakot.terveyslappu.backup.keepAccess
import fi.jukkakot.terveyslappu.backup.releaseAccess
import fi.jukkakot.terveyslappu.weight.HISTORY_PERMISSION
import fi.jukkakot.terveyslappu.weight.HistoryAccess
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import fi.jukkakot.terveyslappu.log.LogCompact
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
import fi.jukkakot.terveyslappu.ui.FoodAccess
import fi.jukkakot.terveyslappu.ui.FoodActions
import fi.jukkakot.terveyslappu.ui.FoodClipboard
import fi.jukkakot.terveyslappu.ui.FoodViewModel
import fi.jukkakot.terveyslappu.ui.showTimed
import fi.jukkakot.terveyslappu.ui.Access
import fi.jukkakot.terveyslappu.ui.BurnActions
import fi.jukkakot.terveyslappu.ui.StepsActions
import fi.jukkakot.terveyslappu.ui.StepsViewModel
import kotlinx.coroutines.flow.merge
import java.time.LocalDate
import fi.jukkakot.terveyslappu.ui.TerveysTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val foodModel: FoodViewModel by viewModels()
    private val stepsModel: StepsViewModel by viewModels()

    /** What to do once the nutrition permission screen returns granted (food-log design 8). */
    private var afterFoodGrant: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) openFrom(intent)
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
                val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                    // A denial keeps the switch on: weights are still saved, Asetukset warns (background-weigh-in).
                    AppLog.i("auto.notify", if (granted) "granted" else "denied")
                    listenTick++
                }
                val food by foodModel.state.collectAsStateWithLifecycle()
                val foodLauncher = rememberLauncherForActivityResult(
                    PermissionController.createRequestPermissionResultContract(),
                ) { granted ->
                    foodModel.onPermissionResult(granted)
                    val then = afterFoodGrant
                    afterFoodGrant = null
                    if (granted.containsAll(foodModel.permissions)) then?.invoke()
                }
                /** Asks the nutrition permission (or opens Health Connect once it was denied), then [then]. */
                fun grantFood(then: (() -> Unit)? = null) {
                    if (foodModel.state.value.access == FoodAccess.DENIED) {
                        open(Intent(HealthConnectManager.ACTION_MANAGE_HEALTH_PERMISSIONS).putExtra(Intent.EXTRA_PACKAGE_NAME, packageName))
                    } else {
                        afterFoodGrant = then
                        foodModel.onPermissionRequested()
                        launchSafely("hc.permission.nutrition") { foodLauncher.launch(foodModel.permissions) }
                    }
                }
                fun openFoodDay(day: LocalDate) {
                    foodModel.setDay(day)
                    viewModel.openFood()
                    if (foodModel.state.value.access == FoodAccess.NEEDS) grantFood()
                }
                val steps by stepsModel.state.collectAsStateWithLifecycle()
                val stepsSources by stepsModel.sources.collectAsStateWithLifecycle()
                // steps design 8: only READ_STEPS, asked from the steps card or the Askeleet tab.
                val stepsLauncher = rememberLauncherForActivityResult(
                    PermissionController.createRequestPermissionResultContract(),
                ) { granted -> stepsModel.onPermissionResult(granted) }
                fun grantSteps() {
                    stepsModel.onPermissionRequested()
                    launchSafely("hc.permission.steps") { stepsLauncher.launch(stepsModel.steps.permissions) }
                }
                val burn by stepsModel.burn.collectAsStateWithLifecycle()
                // energy-burn design 9: both calories read permissions together, from the Kulutus tab or card.
                val burnLauncher = rememberLauncherForActivityResult(
                    PermissionController.createRequestPermissionResultContract(),
                ) { granted -> stepsModel.onBurnPermissionResult(granted) }
                fun grantBurn() {
                    stepsModel.onBurnPermissionRequested()
                    launchSafely("hc.permission.burn") { burnLauncher.launch(stepsModel.burnSource.permissions) }
                }
                // The front page reads the steps card's range whenever it is shown (steps "Refresh and failed reads").
                val homeShown = state.screen == Screen.HOME && state.access == Access.GRANTED
                // Chart cards with steps widen it to their own period's start (custom-charts design 13).
                val homeStepsFrom = fi.jukkakot.terveyslappu.chart.CustomCharts.homeStepsFrom(
                    state.stepsDays, state.homeLayout.shown.map { it.id }, state.charts, LocalDate.now(),
                )
                LaunchedEffect(homeShown, homeStepsFrom) {
                    if (homeShown) stepsModel.show(homeStepsFrom)
                }
                // Kulutus is read for the front page only while its card or a chart card with it is shown (energy-burn).
                val shownIds = state.homeLayout.shown.map { it.id }
                val burnCard = fi.jukkakot.terveyslappu.home.HomeLayout.BURN in shownIds
                val homeBurn = fi.jukkakot.terveyslappu.chart.CustomCharts.homeShowsBurn(burnCard, shownIds, state.charts)
                val homeBurnFrom = fi.jukkakot.terveyslappu.chart.CustomCharts.homeBurnFrom(
                    state.burnDays.takeIf { burnCard }, shownIds, state.charts, LocalDate.now(),
                )
                LaunchedEffect(homeShown, homeBurn, homeBurnFrom) {
                    if (homeShown && homeBurn) stepsModel.showBurn(homeBurnFrom)
                }
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
                    foodModel.refresh()
                    stepsModel.refresh()
                    listenTick++
                    WeighInWidget.update(this@MainActivity)
                    onPauseOrDispose {}
                }
                // Clipboard offer: only while the front page, Ruoka or Lisää annos is shown, on open and on every resume.
                val clipShown = state.screen == Screen.HOME || state.screen == Screen.FOOD || food.sheetDay != null
                LifecycleResumeEffect(clipShown) {
                    if (clipShown) checkClipOffer()
                    onPauseOrDispose {}
                }
                LaunchedEffect(Unit) {
                    // A new message replaces the shown one at once (app-appearance "Undo messages").
                    merge(viewModel.messages, foodModel.messages).collect { message ->
                        launch {
                            val result = snackbar.showTimed(
                                getString(message.text, *message.args.toTypedArray()),
                                message.action?.let { getString(it.label) },
                            )
                            if (result == SnackbarResult.ActionPerformed) message.action?.run?.invoke()
                        }
                    }
                }
                // No bar over an open sheet (its messages are the sheet's line), none left from another screen.
                LaunchedEffect(state.screen, state.settingsPage) { snackbar.currentSnackbarData?.dismiss() }
                val sheetOpen = food.sheetDay != null
                LaunchedEffect(sheetOpen) { if (sheetOpen) snackbar.currentSnackbarData?.dismiss() }

                MainScreen(
                    state = state,
                    backup = backupStatus,
                    versionName = BuildConfig.VERSION_NAME,
                    snackbar = snackbar,
                    themeMode = themeMode,
                    profile = profile,
                    heightInHealthConnect = heightInHc,
                    listen = listenUi,
                    food = food,
                    baseProducts = (application as TerveysApp).baseProducts,
                    steps = steps,
                    stepsActions = StepsActions(
                        onGrant = ::grantSteps,
                        onOpenSettings = { open(Intent(HealthConnectManager.ACTION_MANAGE_HEALTH_PERMISSIONS).putExtra(Intent.EXTRA_PACKAGE_NAME, packageName)) },
                        onOpen = viewModel::openSteps,
                        onPeriod = viewModel::onStepsPeriod,
                        onShown = stepsModel::show,
                        onOpenSource = viewModel::openStepsSource,
                        onChoice = stepsModel::setChoice,
                        onSourceShown = stepsModel::subScreenShown,
                        onSourceHidden = stepsModel::subScreenHidden,
                        onGarminDelete = viewModel::deleteGarmin,
                    ),
                    stepsSources = stepsSources,
                    burn = burn,
                    burnActions = BurnActions(
                        onGrant = ::grantBurn,
                        onOpenSettings = { open(Intent(HealthConnectManager.ACTION_MANAGE_HEALTH_PERMISSIONS).putExtra(Intent.EXTRA_PACKAGE_NAME, packageName)) },
                        onOpen = viewModel::openBurn,
                        onPeriod = viewModel::onBurnPeriod,
                        onShown = stepsModel::showBurn,
                        onOpenSource = viewModel::openBurnSource,
                    ),
                    foodActions = FoodActions(
                        onOpenDay = ::openFoodDay,
                        onAdd = { day ->
                            if (foodModel.state.value.access == FoodAccess.GRANTED) {
                                foodModel.openSheet(day)
                            } else if (foodModel.state.value.access == FoodAccess.NEEDS) {
                                grantFood { foodModel.openSheet(day) }
                            } else {
                                // Denied: Ruoka explains why and offers the grant (food-log "Permission denied").
                                openFoodDay(day)
                            }
                        },
                        onCloseSheet = foodModel::closeSheet,
                        onQuery = foodModel::onQuery,
                        onPick = foodModel::addSuggestion,
                        onEditPick = foodModel::editDraft,
                        onAddDraft = foodModel::addDraft,
                        onEnter = foodModel::addTyped,
                        onUndoAdd = foodModel::undoAdd,
                        onUndoDelete = foodModel::undoDelete,
                        onEdit = foodModel::openEditor,
                        onCloseEditor = foodModel::closeEditor,
                        onSave = foodModel::saveEdit,
                        onDelete = foodModel::delete,
                        onDay = foodModel::setDay,
                        onStartOrder = foodModel::startOrder,
                        onMove = foodModel::move,
                        onFinishOrder = foodModel::finishOrder,
                        onGrant = { grantFood() },
                        onSaveProduct = foodModel::saveProduct,
                        onRemoveProduct = foodModel::removeProduct,
                        onPasteAi = ::pasteAiFood,
                        onCopyAiPrompt = ::copyAiPrompt,
                        onClipOpen = { foodModel.openClipOffer(FoodClipboard.text(this)) },
                        onClipSkip = foodModel::skipClipOffer,
                        onUndoClipSkip = foodModel::undoClipSkip,
                        onClipAddAll = { day ->
                            val addAll = { foodModel.addAllFromClip(FoodClipboard.text(this), day) }
                            if (foodModel.state.value.access == FoodAccess.GRANTED) addAll() else grantFood(addAll)
                        },
                        onUndoImport = foodModel::undoImport,
                        onDeleteProduct = foodModel::deleteProduct,
                        onUndoDeleteProduct = foodModel::undoDeleteProduct,
                        onAiEdit = foodModel::setAi,
                        onAiCancel = foodModel::cancelAi,
                        onAiAdd = {
                            val addNow = {
                                foodModel.state.value.ai?.day?.let { foodModel.setDay(it) }
                                viewModel.openFood()
                                foodModel.addAi()
                            }
                            if (foodModel.state.value.access == FoodAccess.GRANTED) addNow() else grantFood(addNow)
                        },
                    ),
                    actions = MainActions(
                        onScreen = viewModel::onScreen,
                        onBack = viewModel::back,
                        onSettingsPage = viewModel::openSettingsPage,
                        onOpenBackupSettings = viewModel::openBackupSettings,
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
                        onCopyLog = ::copyLog,
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
                        onFoodPeriod = viewModel::onFoodPeriod,
                        onHomeLayout = viewModel::onHomeLayout,
                        onOpenEnergy = viewModel::openEnergy,
                        onStepsPeriod = viewModel::onStepsPeriod,
                        onOpenSteps = viewModel::openSteps,
                        onStepsShown = viewModel::stepsShown,
                        onBurnShown = viewModel::burnShown,
                        onOpenWeek = viewModel::openWeek,
                        onListenEnabled = { on ->
                            listen.setEnabled(on)
                            WeighInWidget.update(this)
                            if (on && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                launchSafely("auto.notify") { notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
                            }
                        },
                        onListenSettings = { settings ->
                            listen.updateSettings(settings)
                            WeighInWidget.update(this)
                        },
                        onListenFix = { fix ->
                            when (fix) {
                                ListenFix.HEALTH -> launchSafely("hc.permission.request") { permissionLauncher.launch(viewModel.weights.permissions) }
                                ListenFix.NOTIFICATIONS -> open(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName))
                                ListenFix.BLUETOOTH -> open(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                                ListenFix.POWER_SAVE -> open(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS))
                                ListenFix.PERMISSION, ListenFix.BATTERY ->
                                    open(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:$packageName".toUri()))
                            }
                        },
                        onAccept = viewModel::accept,
                        onOpenQuestioned = viewModel::openQuestioned,
                        onTargetShown = viewModel::targetShown,
                        onEnergyShown = viewModel::energyShown,
                        onSaveChart = viewModel::onSaveChart,
                        onDeleteChart = viewModel::onDeleteChart,
                        onChartPeriod = viewModel::onChartPeriod,
                        onOpenChart = viewModel::openChart,
                        onChartShown = viewModel::chartShown,
                        onGrantHistory = {
                            viewModel.afterHistory = AfterHistory.RELOAD
                            launchSafely("export.history") { historyLauncher.launch(setOf(HISTORY_PERMISSION)) }
                        },
                    ),
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Any new intent (widget, shortcut, notification, share) ends the front page's edit mode first (home-card-layout design 4).
        fi.jukkakot.terveyslappu.home.HomeEditSignal.outsideIntent("main")
        openFrom(intent)
    }

    /** A weigh-in notification's tap opens Historia (background-weigh-in). */
    private fun openFrom(intent: Intent?) {
        if (intent?.getStringExtra(EXTRA_SCREEN) == SCREEN_HISTORY) viewModel.onScreen(Screen.HISTORY)
        // food-widget design 5: the Lisää annos dialog's import shows the preview over the front page.
        if (intent?.getStringExtra(EXTRA_SCREEN) == SCREEN_HOME) viewModel.onScreen(Screen.HOME)
        // food-ai-import: text shared from an AI chat (or handed over by the dialog) opens the import preview.
        if (intent?.action == Intent.ACTION_SEND && intent.type?.startsWith("text/") == true) {
            foodModel.openAi(intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty(), intent.getStringExtra(EXTRA_IMPORT_FROM) ?: "share")
        }
    }

    /** Liitä tekoälyltä: the clipboard text goes to the import preview. */
    private fun pasteAiFood() {
        foodModel.closeSheet()
        foodModel.openAi(FoodClipboard.text(this), "paste")
    }

    /** The clipboard offer (food-ai-import): called only while the front page, Ruoka or Lisää annos is shown. */
    private fun checkClipOffer() = FoodClipboard.checkOffer(this, foodModel)

    /** Kopioi ohje tekoälylle: assets/ai-food-prompt.txt as is. */
    private fun copyAiPrompt() = FoodClipboard.copyPrompt(this, foodModel)

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

    /** Puts the compact last-24-h log on the clipboard; returns the log lines copied (app-log spec). */
    private fun copyLog(): Int {
        AppLog.flush()
        val lines = AppLog.logFile?.readLines().orEmpty()
        val result = LogCompact.build(lines, BuildConfig.VERSION_NAME, BuildConfig.GIT_SHA, Instant.now(), ZoneId.systemDefault())
        val clipboard = getSystemService(ClipboardManager::class.java)
        clipboard.setPrimaryClip(ClipData.newPlainText("terveyslappu log", result.text))
        AppLog.i("log.copy", "", "lines" to result.lines, "chars" to result.text.length)
        return result.lines
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

    companion object {
        private const val EXTRA_SCREEN = "screen"
        private const val SCREEN_HISTORY = "history"
        private const val SCREEN_HOME = "home"
        private const val EXTRA_IMPORT_FROM = "importFrom"

        /**
         * The Lisää annos dialog's import (food-widget design 5): the app on the preview over the
         * front page, through the share path; a running main screen is reused.
         */
        fun importIntent(context: android.content.Context, text: String, from: String): Intent = Intent(context, MainActivity::class.java)
            .setAction(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, text)
            .putExtra(EXTRA_SCREEN, SCREEN_HOME)
            .putExtra(EXTRA_IMPORT_FROM, from)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

        /** Opens the app at Historia, reusing a running main screen. */
        fun historyIntent(context: android.content.Context): Intent = Intent(context, MainActivity::class.java)
            .putExtra(EXTRA_SCREEN, SCREEN_HISTORY)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

        /** Text tables (some file managers label CSV files as octet-stream) and Garmin's export ZIP. */
        private const val BACKUP_FILE_NAME = "terveyslappu-varmuuskopio.csv"
        private val IMPORT_TYPES = arrayOf("text/*", "application/csv", "text/comma-separated-values", "application/vnd.ms-excel", "application/json", "application/octet-stream", "application/zip", "application/x-zip-compressed")
        private const val STORE_URL = "market://details?id=com.google.android.apps.healthdata&url=healthconnect%3A%2F%2Fonboarding"
    }
}
