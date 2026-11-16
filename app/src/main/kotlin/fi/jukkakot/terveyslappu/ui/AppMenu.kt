package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import fi.jukkakot.terveyslappu.R
import java.time.LocalDate

/**
 * The top bar actions of the front page and Historia (home-screen "App menu"): the help button and
 * a ⋮ menu with Punnitse, Lisää paino käsin, Lisää annos (always for today), a divider and Asetukset.
 * [source] is the weigh-in's log source (`main` on the front page, `history` on Historia). Edit mode
 * replaces this bar, so an open menu goes with it.
 */
@Composable
internal fun RowScope.AppMenuActions(actions: MainActions, foodActions: FoodActions, source: String) {
    var open by rememberSaveable { mutableStateOf(false) }
    IconButton(onClick = { actions.onScreen(Screen.HELP) }, modifier = Modifier.testTag("help")) {
        Icon(painterResource(R.drawable.ic_help), contentDescription = stringResource(R.string.menu_help))
    }
    IconButton(onClick = { open = true }, modifier = Modifier.testTag("appMenu")) {
        Icon(painterResource(R.drawable.ic_more_vert), contentDescription = stringResource(R.string.app_menu))
    }
    DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
        MenuRow(R.string.home_weigh, R.drawable.ic_scale, "menuWeigh") {
            open = false
            actions.onWeighIn(source)
        }
        MenuRow(R.string.entry_title, R.drawable.ic_add, "menuEntry") {
            open = false
            actions.onOpenEntry()
        }
        MenuRow(R.string.food_add, R.drawable.ic_restaurant, "menuFood") {
            open = false
            foodActions.onAdd(LocalDate.now())
        }
        HorizontalDivider()
        MenuRow(R.string.settings_title, R.drawable.ic_settings, "menuSettings") {
            open = false
            actions.onScreen(Screen.SETTINGS)
        }
    }
}

@Composable
private fun MenuRow(text: Int, icon: Int, tag: String, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(stringResource(text)) },
        leadingIcon = { Icon(painterResource(icon), contentDescription = null) },
        onClick = onClick,
        modifier = Modifier.testTag(tag),
    )
}
