package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.backup.BackupStatus
import java.time.Instant

/** One-line backup status above the weight list (weight-backup spec, "Visible status"). */
@Composable
fun BackupCard(status: BackupStatus, now: Instant, onEnable: () -> Unit, onRunNow: () -> Unit) {
    val warn = status.enabled && (status.failed || status.isStale(now))
    val (text, button) = when {
        !status.enabled -> stringResource(R.string.backup_off) to (stringResource(R.string.backup_enable) to onEnable)
        status.running -> stringResource(R.string.backup_running) to null
        status.failed -> stringResource(R.string.backup_failed, formatDateTime(status.lastFailedAt!!)) to
            (stringResource(R.string.backup_retry) to onRunNow)
        status.lastOkAt == null -> stringResource(R.string.backup_never) to (stringResource(R.string.backup_now) to onRunNow)
        else -> stringResource(if (warn) R.string.backup_stale else R.string.backup_ok, formatDateTime(status.lastOkAt), status.lastCount) to
            (if (warn) stringResource(R.string.backup_now) to onRunNow else null)
    }
    Surface(
        color = if (warn) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainer,
        contentColor = if (warn) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().testTag("backupCard"),
    ) {
        Row(
            Modifier.heightIn(min = 48.dp).padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (status.running) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
            Text(text, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f).padding(vertical = 8.dp))
            button?.let { (label, onClick) -> TextButton(onClick = onClick, modifier = Modifier.testTag("backupAction")) { Text(label) } }
        }
    }
}
