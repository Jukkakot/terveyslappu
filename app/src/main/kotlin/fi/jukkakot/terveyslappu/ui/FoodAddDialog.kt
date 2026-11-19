package fi.jukkakot.terveyslappu.ui

import androidx.compose.runtime.Composable
import fi.jukkakot.terveyslappu.food.BaseProduct

/**
 * The Lisää annos activity's content (food-log "Adding from the home screen"): the add search over
 * the whole screen (or its permission and availability states); Muokkaa's editor opens over it and
 * returns to it.
 */
@Composable
internal fun FoodAddDialog(food: FoodUiState, base: List<BaseProduct>, actions: FoodActions, dialog: FoodDialogActions) {
    AddFoodSearch(food, base, actions, dialog)
    food.editing?.let { FoodEditor(it, food.products + listOfNotNull(food.draft?.product), actions, draft = food.draft != null) }
    food.energyAsk?.let { EnergyDialog(it, actions.onEnergyChoice) }
}
