package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R

/** An extra row action in a [RowMenu] (a questioned weight's Hyväksy). */
internal class RowMenuItem(val label: String, val icon: Int?, val tag: String, val onClick: () -> Unit)

/**
 * A list row's actions (app-appearance "List rows"): one ⋮ button (44 dp, named "Toiminnot: <name>")
 * opening a menu next to it with Muokkaa (when [onEdit]), the [extra] actions and Poista in the
 * error colour (when [onDelete]). Tags: `<tag>-menu`, `<tag>-edit`, `<tag>-delete` and each extra's own.
 */
@Composable
internal fun RowMenu(name: String, tag: String, onEdit: (() -> Unit)?, onDelete: (() -> Unit)?, extra: List<RowMenuItem> = emptyList()) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }, modifier = Modifier.size(44.dp).testTag("$tag-menu")) {
            Icon(painterResource(R.drawable.ic_more_vert), contentDescription = stringResource(R.string.row_actions, name), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            if (onEdit != null) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.row_edit)) },
                    leadingIcon = { Icon(painterResource(R.drawable.ic_edit), contentDescription = null) },
                    onClick = {
                        open = false
                        onEdit()
                    },
                    modifier = Modifier.testTag("$tag-edit"),
                )
            }
            extra.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.label) },
                    leadingIcon = item.icon?.let { icon -> { Icon(painterResource(icon), contentDescription = null) } },
                    onClick = {
                        open = false
                        item.onClick()
                    },
                    modifier = Modifier.testTag(item.tag),
                )
            }
            if (onDelete != null) {
                val error = MaterialTheme.colorScheme.error
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.row_delete), color = error) },
                    leadingIcon = { Icon(painterResource(R.drawable.ic_delete), contentDescription = null, tint = error) },
                    onClick = {
                        open = false
                        onDelete()
                    },
                    modifier = Modifier.testTag("$tag-delete"),
                )
            }
        }
    }
}
