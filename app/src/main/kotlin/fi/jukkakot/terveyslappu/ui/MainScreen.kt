package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.backup.BackupStatus
import fi.jukkakot.terveyslappu.weight.WeightEntry
import fi.jukkakot.terveyslappu.weight.WeightInput
import fi.jukkakot.terveyslappu.weight.isInFuture
import fi.jukkakot.terveyslappu.weight.parseWeight
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

/** Everything the screen can ask for; the activity wires these to the view model and intents. */
class MainActions(
    val onWeightText: (String) -> Unit = {},
    val onPickedTime: (LocalDateTime?) -> Unit = {},
    val onSave: () -> Unit = {},
    val onDelete: (WeightEntry) -> Unit = {},
    val onRequestPermission: () -> Unit = {},
    val onOpenSettings: () -> Unit = {},
    val onOpenStore: () -> Unit = {},
    val onShareLog: () -> Unit = {},
    val onWeighIn: () -> Unit = {},
    val onImportHistory: () -> Unit = {},
    val onExport: () -> Unit = {},
    val onBackupEnable: () -> Unit = {},
    val onBackupNow: () -> Unit = {},
    val onBackupChangeFile: () -> Unit = {},
    val onBackupDisable: () -> Unit = {},
    val onConfirmImport: () -> Unit = {},
    val onDismissImport: () -> Unit = {},
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    state: MainUiState,
    versionName: String,
    actions: MainActions,
    snackbar: SnackbarHostState = remember { SnackbarHostState() },
    backup: BackupStatus = BackupStatus(),
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = { AppMenu(versionName, actions, granted = state.access == Access.GRANTED, backupOn = backup.enabled) },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize().imePadding()) {
            when (state.access) {
                Access.CHECKING -> Notice(stringResource(R.string.hc_checking))
                Access.UNAVAILABLE -> Notice(stringResource(R.string.hc_unavailable), stringResource(R.string.hc_open_store), actions.onOpenStore)
                Access.NEEDS_UPDATE -> Notice(stringResource(R.string.hc_needs_update), stringResource(R.string.hc_open_store), actions.onOpenStore)
                Access.NEEDS_PERMISSION -> Notice(stringResource(R.string.hc_permission_intro), stringResource(R.string.hc_permission_grant), actions.onRequestPermission)
                Access.DENIED -> Notice(stringResource(R.string.hc_permission_denied), stringResource(R.string.hc_open_settings), actions.onOpenSettings)
                Access.GRANTED -> WeightContent(state, actions, backup)
            }
        }
        state.import?.let { ImportDialog(it, actions.onConfirmImport, actions.onDismissImport) }
    }
}

@Composable
private fun AppMenu(versionName: String, actions: MainActions, granted: Boolean, backupOn: Boolean) {
    val onShareLog = actions.onShareLog
    val onImport = actions.onImportHistory.takeIf { granted }
    val onExport = actions.onExport.takeIf { granted }
    var open by remember { mutableStateOf(false) }
    Box {
        val menuLabel = stringResource(R.string.menu)
        IconButton(onClick = { open = true }, modifier = Modifier.testTag("menu").semantics { contentDescription = menuLabel }) {
            Text("⋮", style = MaterialTheme.typography.titleLarge)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_share_log)) },
                onClick = { open = false; onShareLog() },
            )
            if (onImport != null) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.menu_import)) },
                    onClick = { open = false; onImport() },
                    modifier = Modifier.testTag("menuImport"),
                )
            }
            if (onExport != null) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.menu_export)) },
                    onClick = { open = false; onExport() },
                    modifier = Modifier.testTag("menuExport"),
                )
            }
            if (granted && backupOn) {
                DropdownMenuItem(text = { Text(stringResource(R.string.menu_backup_now)) }, onClick = { open = false; actions.onBackupNow() })
                DropdownMenuItem(text = { Text(stringResource(R.string.menu_backup_change)) }, onClick = { open = false; actions.onBackupChangeFile() })
                DropdownMenuItem(text = { Text(stringResource(R.string.menu_backup_off)) }, onClick = { open = false; actions.onBackupDisable() })
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_version, versionName), style = MaterialTheme.typography.bodySmall) },
                onClick = { open = false },
                enabled = false,
            )
        }
    }
}

@Composable
private fun Notice(text: String, button: String? = null, onClick: () -> Unit = {}) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge)
        if (button != null) {
            Spacer(Modifier.heightIn(min = 16.dp))
            Button(onClick = onClick) { Text(button) }
        }
    }
}

@Composable
private fun WeightContent(state: MainUiState, actions: MainActions, backup: BackupStatus) {
    Column(Modifier.fillMaxSize()) {
        BackupCard(backup, Instant.now(), actions.onBackupEnable, actions.onBackupNow)
        Text(
            stringResource(R.string.recent_title),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
        )
        Box(Modifier.weight(1f).fillMaxWidth()) {
            val rows = state.rows
            when {
                state.loadFailed && rows == null -> Notice(stringResource(R.string.recent_failed))
                rows == null -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                rows.isEmpty() -> Notice(stringResource(R.string.recent_empty))
                else -> LazyColumn(Modifier.fillMaxSize().testTag("weights")) {
                    items(rows, key = { it.entry.id }) { row ->
                        WeightRowItem(row, onLongPress = { if (row.entry.own) actions.onDelete(row.entry) })
                        HorizontalDivider()
                    }
                }
            }
        }
        WeightForm(state, actions)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WeightRowItem(row: WeightRow, onLongPress: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = {}, onLongClick = onLongPress)
            .heightIn(min = 48.dp)
            .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(formatDateTime(row.entry.time), style = MaterialTheme.typography.bodyLarge)
            Text(
                row.sourceLabel ?: stringResource(R.string.recent_this_app),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text("${formatKg(row.entry.kg)} kg", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        if (row.entry.own) {
            // Visible alternative to the long-press (ui rule: no gesture without one).
            val label = stringResource(R.string.delete_weight, formatKg(row.entry.kg))
            IconButton(onClick = onLongPress, modifier = Modifier.testTag("delete-${row.entry.id}")) {
                Icon(painterResource(R.drawable.ic_delete), contentDescription = label, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Spacer(Modifier.width(48.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeightForm(state: MainUiState, actions: MainActions) {
    val zone = ZoneId.systemDefault()
    val input = parseWeight(state.weightText)
    val shownTime = state.pickedTime ?: LocalDateTime.now()
    val future = state.pickedTime != null && isInFuture(state.pickedTime.atZone(zone).toInstant(), Instant.now())
    val canSave = input is WeightInput.Valid && !future && !state.saving
    var pickDate by remember { mutableStateOf(false) }
    var pickTime by remember { mutableStateOf(false) }

    Surface(tonalElevation = 2.dp) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = actions.onWeighIn, modifier = Modifier.fillMaxWidth().testTag("weighIn")) {
                Text(stringResource(R.string.weighin_main_button))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { pickDate = true }, modifier = Modifier.weight(1f)) { Text(formatDate(shownTime)) }
                OutlinedButton(onClick = { pickTime = true }, modifier = Modifier.weight(1f)) { Text(formatTime(shownTime)) }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val error = when {
                    input is WeightInput.Invalid || input is WeightInput.OutOfRange -> stringResource(R.string.weight_range)
                    future -> stringResource(R.string.weight_future)
                    else -> null
                }
                OutlinedTextField(
                    value = state.weightText,
                    onValueChange = actions.onWeightText,
                    label = { Text(stringResource(R.string.weight_label)) },
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { if (canSave) actions.onSave() }),
                    modifier = Modifier.weight(1f).testTag("weightField"),
                )
                FilledTonalButton(onClick = actions.onSave, enabled = canSave, modifier = Modifier.testTag("save")) {
                    if (state.saving) CircularProgressIndicator(Modifier.width(20.dp), strokeWidth = 2.dp) else Text(stringResource(R.string.weight_save))
                }
            }
        }
    }

    if (pickDate) {
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = shownTime.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { pickDate = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        actions.onPickedTime(LocalDateTime.of(date, shownTime.toLocalTime()).withSecond(0).withNano(0))
                    }
                    pickDate = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { pickDate = false }) { Text(stringResource(R.string.cancel)) } },
        ) { DatePicker(state = dateState) }
    }

    if (pickTime) {
        val timeState = rememberTimePickerState(initialHour = shownTime.hour, initialMinute = shownTime.minute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { pickTime = false },
            confirmButton = {
                TextButton(onClick = {
                    val date: LocalDate = shownTime.toLocalDate()
                    actions.onPickedTime(LocalDateTime.of(date, LocalTime.of(timeState.hour, timeState.minute)))
                    pickTime = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { pickTime = false }) { Text(stringResource(R.string.cancel)) } },
            text = { TimePicker(state = timeState) },
        )
    }
}
