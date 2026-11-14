package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.food.BaseProduct

/** What only the Lisää annos dialog asks for (food-widget design 9, 13). */
class FoodDialogActions(
    /** Salli: the nutrition permission, asked in place. */
    val onGrant: () -> Unit = {},
    /** Denied: Health Connect's app permissions; checked again on return. */
    val onOpenSettings: () -> Unit = {},
    /** The sheet's window gained focus (the clipboard may be read only then). */
    val onFocused: () -> Unit = {},
)

/**
 * The Lisää annos dialog's content (food-widget "Adding from the home screen"): the app's add sheet
 * over the home screen, or its permission and availability states; Muuta's editor replaces the
 * sheet and returns to it.
 */
@Composable
internal fun FoodAddDialog(food: FoodUiState, base: List<BaseProduct>, actions: FoodActions, dialog: FoodDialogActions) {
    val day = food.sheetDay
    if (day != null) {
        FoodSheetFrame(actions.onCloseSheet) {
            val focused = LocalWindowInfo.current.isWindowFocused
            LaunchedEffect(focused) { if (focused) dialog.onFocused() }
            when (food.access) {
                // Checking: the frame only, no keyboard yet.
                FoodAccess.CHECKING -> Box(Modifier.fillMaxWidth().heightIn(min = 160.dp).testTag("sheetChecking"))
                FoodAccess.GRANTED -> AddFoodContent(food, day, base, actions)
                FoodAccess.NEEDS -> SheetNotice(stringResource(R.string.food_permission_reason), stringResource(R.string.food_sheet_allow), dialog.onGrant, "sheetGrant")
                FoodAccess.DENIED -> SheetNotice(stringResource(R.string.food_permission_reason), stringResource(R.string.food_permission_settings), dialog.onOpenSettings, "sheetSettings")
                FoodAccess.UNAVAILABLE -> SheetNotice(stringResource(R.string.hc_unavailable), stringResource(R.string.close), actions.onCloseSheet, "sheetClose")
            }
        }
    }
    food.editing?.let { FoodEditor(it, food.products, actions) }
}

@Composable
private fun SheetNotice(text: String, button: String, onClick: () -> Unit, tag: String) {
    Column(Modifier.fillMaxWidth().testTag("sheetNotice"), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        TextButton(onClick = onClick, modifier = Modifier.heightIn(min = 48.dp).testTag(tag)) { Text(button) }
    }
}
