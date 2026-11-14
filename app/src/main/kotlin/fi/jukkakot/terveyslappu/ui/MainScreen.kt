package fi.jukkakot.terveyslappu.ui

import fi.jukkakot.terveyslappu.data.DataFormat

import fi.jukkakot.terveyslappu.profile.OwnerProfile
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.RowScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.backup.BackupStatus
import fi.jukkakot.terveyslappu.chart.Period
import fi.jukkakot.terveyslappu.listen.ListenSettings
import fi.jukkakot.terveyslappu.listen.ListenUi
import fi.jukkakot.terveyslappu.weight.WeightEntry
import java.time.LocalDateTime

/** Everything the screens can ask for; the activity wires these to the view model and intents. */
class MainActions(
    val onScreen: (Screen) -> Unit = {},
    /** The one back rule (MainUiState.back). */
    val onBack: () -> Unit = {},
    val onSettingsPage: (SettingsPage) -> Unit = {},
    val onOpenBackupSettings: () -> Unit = {},
    val onOpenEntry: () -> Unit = {},
    val onCloseEntry: () -> Unit = {},
    val onWeightText: (String) -> Unit = {},
    val onPickedTime: (LocalDateTime?) -> Unit = {},
    val onSave: () -> Unit = {},
    val onDelete: (WeightEntry) -> Unit = {},
    val onRequestPermission: () -> Unit = {},
    val onOpenSettings: () -> Unit = {},
    val onOpenStore: () -> Unit = {},
    val onShareLog: () -> Unit = {},
    /** Copies the compact log to the clipboard; returns the number of log lines copied (0 when there is none). */
    val onCopyLog: () -> Int = { 0 },
    val onWeighIn: () -> Unit = {},
    val onImportHistory: () -> Unit = {},
    val onExport: (DataFormat) -> Unit = {},
    val onBackupEnable: () -> Unit = {},
    val onBackupNow: () -> Unit = {},
    val onBackupChangeFile: () -> Unit = {},
    val onBackupDisable: () -> Unit = {},
    val onConfirmImport: () -> Unit = {},
    val onDismissImport: () -> Unit = {},
    val onPeriod: (Period) -> Unit = {},
    val onHomePeriod: (Long) -> Unit = {},
    val onFoodPeriod: (Long) -> Unit = {},
    /** The food card: Historia on Energia. */
    val onOpenEnergy: () -> Unit = {},
    /** The automatic weigh-in card: its Asetukset page. */
    val onOpenListen: () -> Unit = {},
    val onGrantHistory: () -> Unit = {},
    val onTheme: (ThemeMode) -> Unit = {},
    val onProfile: (OwnerProfile) -> Unit = {},
    val onOpenProfile: () -> Unit = {},
    val onListenEnabled: (Boolean) -> Unit = {},
    val onListenSettings: (ListenSettings) -> Unit = {},
    val onListenFix: (ListenFix) -> Unit = {},
    /** Hyväksy on a "?" row in Historia. */
    val onAccept: (WeightEntry) -> Unit = {},
    /** The home notice: Historia at the newest "?" weight. */
    val onOpenQuestioned: () -> Unit = {},
    /** Historia scrolled to its target row. */
    val onTargetShown: () -> Unit = {},
    /** Historia opened on Energia after Ruoka. */
    val onEnergyShown: () -> Unit = {},
)

/** Home, Historia or Asetukset (home-screen spec); back from the other two returns Home. */
@Composable
fun MainScreen(
    state: MainUiState,
    versionName: String,
    actions: MainActions,
    snackbar: SnackbarHostState = remember { SnackbarHostState() },
    backup: BackupStatus = BackupStatus(),
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    profile: OwnerProfile = OwnerProfile(),
    heightInHealthConnect: Boolean = true,
    listen: ListenUi = ListenUi(),
    food: FoodUiState = FoodUiState(),
    foodActions: FoodActions = FoodActions(),
    baseProducts: List<fi.jukkakot.terveyslappu.food.BaseProduct> = emptyList(),
) {
    val granted = state.access == Access.GRANTED
    // Historia needs Health Connect; if access went away meanwhile, the home screen explains why.
    val screen = if ((state.screen == Screen.HISTORY || state.screen == Screen.FOOD) && !granted) Screen.HOME else state.screen
    BackHandler(enabled = screen != Screen.HOME) { actions.onBack() }
    when (screen) {
        Screen.HOME -> HomeScreen(state, actions, snackbar, backup, profile, food, foodActions, listen)
        Screen.HISTORY -> HistoryScreen(state, actions, snackbar, profile, food, foodActions)
        Screen.SETTINGS -> SettingsScreen(state.settingsPage, granted, themeMode, profile, heightInHealthConnect, backup, versionName, actions, snackbar, listen, food, foodActions)
        Screen.HELP -> HelpScreen(actions)
        Screen.FOOD -> FoodScreen(food, foodActions, state.history, actions.onGrantHistory, actions.onBack, snackbar)
    }
    if (food.sheetDay != null) AddFoodSheet(food, baseProducts, foodActions)
    food.editing?.let { FoodEditor(it, food.products, foodActions) }
    food.ai?.let { AiImportPreview(it, foodActions) }
    state.import?.let { ImportDialog(it, actions.onConfirmImport, actions.onDismissImport) }
}

/** Top bar of Historia and Asetukset: back arrow, title, optional actions. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SubScreenBar(title: String, onBack: () -> Unit, actions: @Composable RowScope.() -> Unit = {}) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBack, modifier = Modifier.testTag("back")) {
                Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.back))
            }
        },
        actions = actions,
    )
}

/** A centred message with an optional button (Health Connect states, read failure). */
@Composable
internal fun Notice(text: String, button: String? = null, onClick: () -> Unit = {}) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        if (button != null) {
            Spacer(Modifier.heightIn(min = 16.dp))
            Button(onClick = onClick) { Text(button) }
        }
    }
}
