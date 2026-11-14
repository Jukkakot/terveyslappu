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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.backup.BackupStatus
import fi.jukkakot.terveyslappu.data.DataFormat
import fi.jukkakot.terveyslappu.profile.OwnerProfile
import java.time.Instant

/**
 * Asetukset (home-screen spec): Ulkoasu, Omat tiedot (owner-profile), Painot, Varmuuskopio and Tuki. Painot and Varmuuskopio need
 * Health Connect and are shown only with access; Jaa loki is always reachable.
 */
@Composable
internal fun SettingsScreen(
    granted: Boolean,
    themeMode: ThemeMode,
    profile: OwnerProfile,
    heightInHealthConnect: Boolean,
    backup: BackupStatus,
    versionName: String,
    actions: MainActions,
    snackbar: SnackbarHostState,
    profileFocus: Boolean = false,
) {
    val profileView = remember { BringIntoViewRequester() }
    LaunchedEffect(profileFocus) { if (profileFocus) profileView.bringIntoView() }
    Scaffold(
        topBar = { SubScreenBar(stringResource(R.string.settings_title), onBack = { actions.onScreen(Screen.HOME) }) },
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
                .testTag("settingsList"),
        ) {
            Section(R.string.settings_appearance) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.settings_theme), style = MaterialTheme.typography.bodyLarge)
                    ThemeChoice(themeMode, actions.onTheme)
                }
            }
            Column(Modifier.bringIntoViewRequester(profileView)) {
                Section(R.string.profile_section) { OwnerFields(profile, heightInHealthConnect, actions.onProfile) }
            }
            if (granted) {
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
            Section(R.string.settings_support) {
                SettingsRow(
                    R.drawable.ic_share,
                    stringResource(R.string.menu_share_log),
                    actions.onShareLog,
                    "shareLog",
                    trailing = stringResource(R.string.settings_version, versionName),
                )
                SettingsRow(R.drawable.ic_help, stringResource(R.string.menu_help), { actions.onScreen(Screen.HELP) }, "help")
            }
        }
    }
}

@Composable
private fun Section(title: Int, content: @Composable ColumnScope.() -> Unit) {
    Text(
        stringResource(title),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 8.dp),
    )
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
        status.failed -> stringResource(R.string.backup_failed, formatDateTime(status.lastFailedAt!!))
        status.lastOkAt == null -> stringResource(R.string.backup_never)
        else -> stringResource(if (warn) R.string.backup_stale else R.string.backup_ok, formatDateTime(status.lastOkAt), status.lastCount)
    }
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
