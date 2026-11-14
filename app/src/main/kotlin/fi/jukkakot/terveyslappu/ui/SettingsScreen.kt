package fi.jukkakot.terveyslappu.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.backup.BackupStatus
import fi.jukkakot.terveyslappu.profile.Gender
import fi.jukkakot.terveyslappu.data.DataFormat
import fi.jukkakot.terveyslappu.listen.ListenStatus
import fi.jukkakot.terveyslappu.listen.ListenUi
import fi.jukkakot.terveyslappu.profile.OwnerProfile
import java.time.Instant

/**
 * Asetukset (settings-list): a list of rows with a state line each; a row opens its sub-screen, which
 * holds what was the section. DATA needs Health Connect access and falls back to the list without it.
 */
@Composable
internal fun SettingsScreen(
    page: SettingsPage,
    granted: Boolean,
    themeMode: ThemeMode,
    profile: OwnerProfile,
    heightInHealthConnect: Boolean,
    backup: BackupStatus,
    versionName: String,
    actions: MainActions,
    snackbar: SnackbarHostState,
    listen: ListenUi = ListenUi(),
    food: FoodUiState = FoodUiState(),
    foodActions: FoodActions = FoodActions(),
) {
    val shown = if (page == SettingsPage.DATA && !granted) SettingsPage.LIST else page
    when (shown) {
        SettingsPage.LIST -> SettingsList(granted, themeMode, profile, backup, versionName, actions, snackbar, listen, food.products.size)
        SettingsPage.FOOD -> SubPage(R.string.settings_food, actions, snackbar, "foodPage") {
            Section(null) { ProductList(food.products, foodActions) }
        }
        SettingsPage.PROFILE -> SubPage(R.string.profile_section, actions, snackbar, "profilePage") {
            Section(null) { OwnerFields(profile, heightInHealthConnect, actions.onProfile) }
        }
        SettingsPage.LISTEN -> SubPage(R.string.listen_section, actions, snackbar, "listenPage") {
            Section(null) { ListenGroup(listen, actions, snackbar) }
        }
        SettingsPage.DATA -> SubPage(R.string.settings_data_backup, actions, snackbar, "dataPage") {
            var choosingFormat by remember { mutableStateOf(false) }
            Section(R.string.settings_data) {
                SettingsRow(R.drawable.ic_import, stringResource(R.string.menu_import), actions.onImportHistory, "import")
                SettingsRow(R.drawable.ic_export, stringResource(R.string.menu_export), { choosingFormat = true }, "export")
            }
            if (choosingFormat) {
                ExportFormatDialog(
                    onPick = {
                        choosingFormat = false
                        actions.onExport(it)
                    },
                    onDismiss = { choosingFormat = false },
                )
            }
            Section(R.string.settings_backup) { BackupGroup(backup, Instant.now(), actions) }
        }
        SettingsPage.APPEARANCE -> SubPage(R.string.settings_appearance, actions, snackbar, "appearancePage") {
            Section(null) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.settings_theme), style = MaterialTheme.typography.bodyLarge)
                    ThemeChoice(themeMode, actions.onTheme)
                }
            }
        }
        SettingsPage.SUPPORT -> SupportPage(versionName, actions, snackbar)
    }
}

/** A sub-screen: its own Scaffold with the shared snackbar host, title = the row name. */
@Composable
private fun SubPage(title: Int, actions: MainActions, snackbar: SnackbarHostState, tag: String, content: @Composable ColumnScope.() -> Unit) {
    Scaffold(
        topBar = { SubScreenBar(stringResource(title), onBack = actions.onBack) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
                .testTag(tag),
            content = content,
        )
    }
}

@Composable
private fun SupportPage(versionName: String, actions: MainActions, snackbar: SnackbarHostState) {
    val scope = rememberCoroutineScope()
    val copiedText = stringResource(R.string.log_copied)
    val logEmpty = stringResource(R.string.log_empty)
    SubPage(R.string.settings_support, actions, snackbar, "supportPage") {
        Section(null) {
            SettingsRow(
                R.drawable.ic_share,
                stringResource(R.string.menu_share_log),
                actions.onShareLog,
                "shareLog",
                trailing = stringResource(R.string.settings_version, versionName),
            )
            SettingsRow(
                R.drawable.ic_copy,
                stringResource(R.string.menu_copy_log),
                {
                    val count = actions.onCopyLog()
                    scope.launch {
                        snackbar.showSnackbar(if (count > 0) copiedText.format(count) else logEmpty)
                    }
                },
                "copyLog",
            )
        }
    }
}

@Composable
private fun SettingsList(
    granted: Boolean,
    themeMode: ThemeMode,
    profile: OwnerProfile,
    backup: BackupStatus,
    versionName: String,
    actions: MainActions,
    snackbar: SnackbarHostState,
    listen: ListenUi,
    productCount: Int = 0,
) {
    val none = stringResource(R.string.settings_none)
    val male = stringResource(R.string.gender_male)
    val female = stringResource(R.string.gender_female)
    val profileLine = SettingsSummary.profile(profile, { if (it == Gender.MALE) male else female }, none)
    Scaffold(
        topBar = { SubScreenBar(stringResource(R.string.settings_title), onBack = actions.onBack) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
                .testTag("settingsList"),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Column(Modifier.fillMaxWidth().clip(groupShape).background(MaterialTheme.colorScheme.surfaceContainer)) {
                PageRow(R.drawable.ic_person, R.string.profile_section, "rowProfile", { actions.onSettingsPage(SettingsPage.PROFILE) }) {
                    StateLine(profileLine)
                }
                PageRow(R.drawable.ic_scale, R.string.listen_section, "rowListen", { actions.onSettingsPage(SettingsPage.LISTEN) }) {
                    ListenState(SettingsSummary.listen(listen), listen.status)
                }
                if (granted) {
                    PageRow(R.drawable.ic_backup, R.string.settings_data_backup, "rowData", { actions.onSettingsPage(SettingsPage.DATA) }) {
                        BackupState(SettingsSummary.backup(backup, Instant.now()))
                    }
                }
                PageRow(R.drawable.ic_restaurant, R.string.settings_food, "rowFood", { actions.onSettingsPage(SettingsPage.FOOD) }) {
                    StateLine(stringResource(R.string.settings_food_line, productCount))
                }
                PageRow(R.drawable.ic_palette, R.string.settings_appearance, "rowAppearance", { actions.onSettingsPage(SettingsPage.APPEARANCE) }) {
                    StateLine(stringResource(R.string.settings_theme_line, stringResource(themeMode.label())))
                }
                PageRow(R.drawable.ic_file, R.string.settings_support, "rowSupport", { actions.onSettingsPage(SettingsPage.SUPPORT) }) {
                    StateLine(stringResource(R.string.settings_support_line, versionName))
                }
            }
        }
    }
}

/** One row of the list: icon, name, state line, decorative chevron; one click target. */
@Composable
private fun PageRow(@DrawableRes icon: Int, name: Int, tag: String, onClick: () -> Unit, state: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 64.dp)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(22.dp))
        Column(Modifier.weight(1f)) {
            Text(stringResource(name), style = MaterialTheme.typography.bodyLarge)
            state()
        }
        Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
    }
}

/** A state line; a warning is in the error colour and starts with a warning icon (not colour alone). */
@Composable
private fun StateLine(text: String, warn: Boolean = false, symbol: StatusSymbol? = null) {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (symbol != null) {
            Icon(painterResource(symbol.icon), contentDescription = null, tint = colorResource(symbol.color), modifier = Modifier.size(16.dp))
        } else if (warn) {
            Icon(painterResource(R.drawable.ic_warning), contentDescription = null, tint = colors.error, modifier = Modifier.size(16.dp))
        }
        Text(text, style = MaterialTheme.typography.bodyMedium, color = if (warn) colors.error else colors.onSurfaceVariant)
    }
}

@Composable
internal fun ListenState(line: ListenLine, status: ListenStatus?) {
    when (line) {
        ListenLine.Off -> StateLine(stringResource(R.string.settings_listen_off))
        is ListenLine.Pending -> StateLine(stringResource(R.string.listen_pending, line.count), warn = true)
        is ListenLine.Blocked -> StateLine(statusText(line.status).first, warn = true)
        ListenLine.Battery -> StateLine(stringResource(R.string.listen_battery_warning), warn = true)
        ListenLine.Notifications -> StateLine(stringResource(R.string.listen_notifications_warning), warn = true)
        is ListenLine.On -> StateLine(
            stringResource(R.string.settings_listen_on, clockFormat.format(line.start), clockFormat.format(line.end)),
            symbol = status?.symbol(),
        )
    }
}

@Composable
private fun BackupState(line: BackupLine) {
    val text = when (line) {
        BackupLine.Off -> stringResource(R.string.settings_backup_off)
        BackupLine.Running -> stringResource(R.string.backup_running)
        BackupLine.Never -> stringResource(R.string.backup_never)
        is BackupLine.Failed -> stringResource(if (line.refused) R.string.backup_refused else R.string.backup_failed, formatDateTime(line.at))
        is BackupLine.Stale -> stringResource(R.string.backup_stale, formatDateTime(line.at), line.count)
        is BackupLine.Ok -> stringResource(R.string.backup_ok, formatDateTime(line.at), line.count)
    }
    StateLine(text, warn = line.warn)
}

@Composable
private fun Section(title: Int?, content: @Composable ColumnScope.() -> Unit) {
    if (title == null) {
        Spacer(Modifier.height(16.dp))
    } else {
        Text(
            stringResource(title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 8.dp),
        )
    }
    Column(Modifier.fillMaxWidth().clip(groupShape).background(MaterialTheme.colorScheme.surfaceContainer), content = content)
}

private fun ThemeMode.label(): Int = when (this) {
    ThemeMode.SYSTEM -> R.string.theme_system
    ThemeMode.LIGHT -> R.string.theme_light
    ThemeMode.DARK -> R.string.theme_dark
}

/**
 * Filter chips like Historia's period choice: a segmented button broke "Järjestelmä" mid-word on a
 * 360 dp phone, chips wrap whole at any text size.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ThemeChoice(mode: ThemeMode, onTheme: (ThemeMode) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ThemeMode.entries.forEach { option ->
            val selected = option == mode
            FilterChip(
                selected = selected,
                onClick = { onTheme(option) },
                label = { Text(stringResource(option.label())) },
                leadingIcon = if (selected) {
                    { Icon(painterResource(R.drawable.ic_check), contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                } else {
                    null
                },
                modifier = Modifier.testTag("theme-${option.name}"),
            )
        }
    }
}

/** Vie tiedot's format choice (weight-export "Export to a chosen file"). */
@Composable
private fun ExportFormatDialog(onPick: (DataFormat) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.export_format_title)) },
        text = {
            Column {
                SettingsRow(R.drawable.ic_export, stringResource(R.string.export_format_csv), { onPick(DataFormat.CSV) }, "format-CSV")
                SettingsRow(R.drawable.ic_export, stringResource(R.string.export_format_json), { onPick(DataFormat.JSON) }, "format-JSON")
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

/** A tappable row: icon, label, optional trailing text; quiet rows use the secondary text colour. */
@Composable
private fun SettingsRow(
    @DrawableRes icon: Int,
    label: String,
    onClick: () -> Unit,
    tag: String,
    trailing: String? = null,
    quiet: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 52.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(22.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (quiet) colors.onSurfaceVariant else Color.Unspecified,
            modifier = Modifier.weight(1f),
        )
        trailing?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant) }
    }
}

/** Backup state and its actions (weight-backup "Visible status", "Manage the backup"). */
@Composable
private fun BackupGroup(status: BackupStatus, now: Instant, actions: MainActions) {
    val colors = MaterialTheme.colorScheme
    val warn = status.needsAttention(now)
    val detail = when {
        !status.enabled -> stringResource(R.string.backup_off_hint)
        status.running -> stringResource(R.string.backup_running)
        status.failed -> stringResource(if (status.refused) R.string.backup_refused else R.string.backup_failed, formatDateTime(status.lastFailedAt!!))
        status.lastOkAt == null -> stringResource(R.string.backup_never)
        else -> stringResource(if (warn) R.string.backup_stale else R.string.backup_ok, formatDateTime(status.lastOkAt), status.lastCount)
    }.let { if (status.enabled && status.recentOnly) it + "\n" + stringResource(R.string.backup_recent_only) else it }
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).testTag("backupStatus"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(if (status.enabled) R.string.backup_on else R.string.backup_off),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(detail, style = MaterialTheme.typography.bodyMedium, color = if (warn) colors.error else colors.onSurfaceVariant)
        }
        if (status.running) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
    }
    if (!status.enabled) {
        SettingsRow(R.drawable.ic_backup, stringResource(R.string.backup_enable), actions.onBackupEnable, "backupEnable")
    } else {
        val runLabel = if (status.failed) R.string.backup_retry else R.string.backup_now
        SettingsRow(R.drawable.ic_backup, stringResource(runLabel), actions.onBackupNow, "backupNow")
        SettingsRow(R.drawable.ic_file, stringResource(R.string.backup_change), actions.onBackupChangeFile, "backupChange")
        SettingsRow(R.drawable.ic_off, stringResource(R.string.backup_disable), actions.onBackupDisable, "backupDisable", quiet = true)
    }
}
