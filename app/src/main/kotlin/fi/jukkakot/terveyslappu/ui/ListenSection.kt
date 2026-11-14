package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.auto.Plausibility
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import fi.jukkakot.terveyslappu.listen.ListenMode
import fi.jukkakot.terveyslappu.listen.ListenStatus
import fi.jukkakot.terveyslappu.listen.ListenUi
import fi.jukkakot.terveyslappu.listen.ListenWindow
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** Which system screen a status or warning button opens. */
enum class ListenFix { PERMISSION, HEALTH, BLUETOOTH, POWER_SAVE, BATTERY, NOTIFICATIONS }

internal val clockFormat = DateTimeFormatter.ofPattern("H.mm")

/**
 * "Automaattinen punnitus" (background-weigh-in): the switch, the status with its fix, the battery
 * and notification warnings, the pending queue, the window, the mode, the power-saving choice, the
 * acceptance limit and the last-heard line.
 */
@Composable
internal fun ListenGroup(ui: ListenUi, actions: MainActions, snackbar: SnackbarHostState) {
    val colors = MaterialTheme.colorScheme
    val settings = ui.settings
    val scope = rememberCoroutineScope()
    val sameTime = stringResource(R.string.listen_same_time)
    var picking by remember { mutableStateOf<Boolean?>(null) } // true = start, false = end

    SwitchRow(
        stringResource(R.string.listen_switch),
        if (ui.hasScale) stringResource(R.string.listen_switch_hint) else stringResource(R.string.listen_no_scale),
        checked = settings.enabled,
        enabled = ui.hasScale || settings.enabled,
        tag = "listenSwitch",
        onChange = actions.onListenEnabled,
    )
    ui.status?.let { status ->
        val (text, fix) = statusText(status)
        ListenNotice(text, warn = status.isBlocked(), fix = fix, tag = "listenStatus", actions = actions, symbol = status.symbol())
    }
    if (ui.batteryRestricted) {
        ListenNotice(stringResource(R.string.listen_battery_warning), warn = true, fix = ListenFix.BATTERY, tag = "listenBattery", actions = actions)
    }
    if (ui.notificationsOff) {
        ListenNotice(stringResource(R.string.listen_notifications_warning), warn = true, fix = ListenFix.NOTIFICATIONS, tag = "listenNotifications", actions = actions)
    }
    if (ui.pending > 0) {
        ListenNotice(stringResource(R.string.listen_pending, ui.pending), warn = true, fix = null, tag = "listenPending", actions = actions)
    }
    TimeRow(stringResource(R.string.listen_start), settings.start, "listenStart") { picking = true }
    TimeRow(stringResource(R.string.listen_end), settings.end, "listenEnd") { picking = false }
    ModeChoice(settings.mode) { actions.onListenSettings(settings.copy(mode = it)) }
    SwitchRow(
        stringResource(R.string.listen_power_save),
        null,
        checked = settings.listenInPowerSave,
        enabled = true,
        tag = "listenPowerSave",
        onChange = { actions.onListenSettings(settings.copy(listenInPowerSave = it)) },
    )
    LimitRow(settings.limit) { actions.onListenSettings(settings.copy(limit = it)) }
    Text(
        ui.lastHeard,
        style = MaterialTheme.typography.bodyMedium,
        color = colors.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).testTag("listenHeard"),
    )

    picking?.let { start ->
        TimeDialog(
            initial = if (start) settings.start else settings.end,
            onPick = { time ->
                picking = null
                val next = if (start) settings.copy(start = time) else settings.copy(end = time)
                if (ListenWindow.valid(next.start, next.end)) {
                    actions.onListenSettings(next)
                } else {
                    scope.launch { snackbar.showSnackbar(sameTime) }
                }
            },
            onDismiss = { picking = null },
        )
    }
}

@Composable
internal fun statusText(status: ListenStatus): Pair<String, ListenFix?> = when (status) {
    ListenStatus.NoScale -> stringResource(R.string.listen_status_no_scale) to null
    ListenStatus.PermissionMissing -> stringResource(R.string.listen_status_permission) to ListenFix.PERMISSION
    ListenStatus.HealthMissing -> stringResource(R.string.listen_status_health) to ListenFix.HEALTH
    ListenStatus.BluetoothOff -> stringResource(R.string.listen_status_bluetooth) to ListenFix.BLUETOOTH
    ListenStatus.Stopped -> stringResource(R.string.listen_status_stopped) to null
    ListenStatus.PowerSavePause -> stringResource(R.string.listen_status_power_save) to ListenFix.POWER_SAVE
    ListenStatus.Listening -> stringResource(R.string.listen_status_listening) to null
    is ListenStatus.Resting -> stringResource(R.string.listen_status_resting, clockFormat.format(status.startsAt)) to null
    is ListenStatus.Done -> stringResource(
        if (status.tomorrow) R.string.listen_status_done_tomorrow else R.string.listen_status_done,
        clockFormat.format(status.nextStart),
    ) to null
}

/** The widget's symbol for the status, in its colour (same drawable and colour resources). */
internal class StatusSymbol(val icon: Int, val color: Int, val label: Int)

internal fun ListenStatus.symbol(): StatusSymbol = when (this) {
    ListenStatus.Listening -> StatusSymbol(R.drawable.ic_state_on, R.color.widget_state_on, R.string.widget_state_on)
    is ListenStatus.Done -> StatusSymbol(R.drawable.ic_state_done, R.color.widget_state_done, R.string.widget_state_done)
    is ListenStatus.Resting -> StatusSymbol(R.drawable.ic_state_rest, R.color.widget_state_rest, R.string.widget_state_rest)
    else -> StatusSymbol(R.drawable.ic_state_blocked, R.color.widget_state_blocked, R.string.widget_state_blocked)
}

private fun ListenFix.label(): Int = when (this) {
    ListenFix.PERMISSION -> R.string.listen_fix_permission
    ListenFix.HEALTH -> R.string.listen_fix_health
    ListenFix.NOTIFICATIONS -> R.string.listen_fix_notifications
    ListenFix.BLUETOOTH -> R.string.listen_fix_bluetooth
    ListenFix.POWER_SAVE -> R.string.listen_fix_power_save
    ListenFix.BATTERY -> R.string.listen_fix_battery
}

/** A status or warning line; a blocked reason or warning is in the error colour with an icon (not colour alone). */
@Composable
private fun ListenNotice(text: String, warn: Boolean, fix: ListenFix?, tag: String, actions: MainActions, symbol: StatusSymbol? = null) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(start = 16.dp, end = 8.dp).testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (symbol != null) {
            Icon(painterResource(symbol.icon), contentDescription = stringResource(symbol.label), tint = colorResource(symbol.color), modifier = Modifier.size(20.dp))
        } else if (warn) {
            Icon(painterResource(R.drawable.ic_warning), contentDescription = null, tint = colors.error, modifier = Modifier.size(20.dp))
        }
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (warn) colors.error else colors.onSurface,
            modifier = Modifier.weight(1f),
        )
        fix?.let { TextButton(onClick = { actions.onListenFix(it) }, modifier = Modifier.testTag("$tag-fix")) { Text(stringResource(it.label())) } }
    }
}

@Composable
private fun SwitchRow(label: String, detail: String?, checked: Boolean, enabled: Boolean, tag: String, onChange: (Boolean) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, role = Role.Switch) { onChange(!checked) }
            .heightIn(min = 52.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = if (enabled) colors.onSurface else colors.onSurfaceVariant)
            detail?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant) }
        }
        Switch(checked = checked, onCheckedChange = null, enabled = enabled)
    }
}

@Composable
private fun TimeRow(label: String, time: LocalTime, tag: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).heightIn(min = 52.dp).padding(horizontal = 16.dp, vertical = 8.dp).testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(clockFormat.format(time), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
    }
}

/** Hyväksyntäraja: 0,5-20 kg in 0,5 kg steps with − and + (background-weigh-in "Trial setting"). */
@Composable
private fun LimitRow(limit: Double, onLimit: (Double) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp).testTag("listenLimit"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.listen_limit), style = MaterialTheme.typography.bodyLarge)
            Text(stringResource(R.string.listen_limit_hint), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        }
        val less = stringResource(R.string.listen_limit_less)
        val more = stringResource(R.string.listen_limit_more)
        TextButton(
            onClick = { onLimit(Plausibility.limit(limit - Plausibility.STEP)) },
            enabled = limit > Plausibility.MIN_LIMIT,
            modifier = Modifier.semantics { contentDescription = less }.testTag("listenLimitLess"),
        ) { Text("−", style = MaterialTheme.typography.titleLarge) }
        Text(
            stringResource(R.string.listen_limit_value, formatKg(limit)),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.primary,
            modifier = Modifier.testTag("listenLimitValue"),
        )
        TextButton(
            onClick = { onLimit(Plausibility.limit(limit + Plausibility.STEP)) },
            enabled = limit < Plausibility.MAX_LIMIT,
            modifier = Modifier.semantics { contentDescription = more }.testTag("listenLimitMore"),
        ) { Text("+", style = MaterialTheme.typography.titleLarge) }
    }
}

/** Säästö / Tasapaino as filter chips, like the theme choice. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ModeChoice(mode: ListenMode, onMode: (ListenMode) -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(R.string.listen_mode), style = MaterialTheme.typography.bodyLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ListenMode.entries.forEach { option ->
                val selected = option == mode
                FilterChip(
                    selected = selected,
                    onClick = { onMode(option) },
                    label = { Text(stringResource(if (option == ListenMode.SAVE) R.string.listen_mode_save else R.string.listen_mode_balanced)) },
                    leadingIcon = if (selected) {
                        { Icon(painterResource(R.drawable.ic_check), contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                    } else {
                        null
                    },
                    modifier = Modifier.testTag("listenMode-${option.name}"),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeDialog(initial: LocalTime, onPick: (LocalTime) -> Unit, onDismiss: () -> Unit) {
    val state = rememberTimePickerState(initial.hour, initial.minute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { TimePicker(state) },
        confirmButton = {
            TextButton(onClick = { onPick(LocalTime.of(state.hour, state.minute)) }, modifier = Modifier.testTag("timeOk")) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

