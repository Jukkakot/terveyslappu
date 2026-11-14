package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.food.EditInput
import fi.jukkakot.terveyslappu.food.EditMode
import fi.jukkakot.terveyslappu.food.Energy
import fi.jukkakot.terveyslappu.food.AmountUnit
import fi.jukkakot.terveyslappu.food.FoodEdits
import fi.jukkakot.terveyslappu.food.FoodItem
import fi.jukkakot.terveyslappu.food.FoodNames
import fi.jukkakot.terveyslappu.food.Product
import fi.jukkakot.terveyslappu.food.Suggestion
import fi.jukkakot.terveyslappu.food.Suggestions
import fi.jukkakot.terveyslappu.food.BaseProduct
import java.time.LocalDate
import java.time.ZonedDateTime

/** "140 kcal / 100 g" or "25 kcal / kpl". */
@Composable
internal fun energyText(energy: Energy?): String = when {
    energy == null -> stringResource(R.string.food_energy_unknown)
    energy.per == AmountUnit.KPL -> stringResource(R.string.food_per_kpl, formatKcal(energy.kcal))
    else -> stringResource(R.string.food_per_100g, formatKcal(energy.kcal))
}

/**
 * The add field (food-log "Adding an item"): keyboard up, suggestion buttons at the empty field,
 * up to five matches while typing, one tap adds, enter adds the typed text; the field stays open.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun AddFoodSheet(food: FoodUiState, base: List<BaseProduct>, actions: FoodActions) {
    val day = food.sheetDay ?: return
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focus = remember { FocusRequester() }
    ModalBottomSheet(onDismissRequest = actions.onCloseSheet, sheetState = sheet, modifier = Modifier.testTag("addSheet")) {
        Column(
            Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val today = LocalDate.now()
            if (day != today) {
                Text(stringResource(R.string.food_add_for_day, dayTitle(day, today)), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            OutlinedTextField(
                value = food.query,
                onValueChange = actions.onQuery,
                label = { Text(stringResource(R.string.food_add_field)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { actions.onEnter() }),
                modifier = Modifier.fillMaxWidth().focusRequester(focus).testTag("addField"),
            )
            LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
            food.lastAdded?.let { LastAddedLine(it, actions) }
            val items = food.items.orEmpty()
            if (FoodNames.normalize(food.query).isEmpty()) {
                val picks = remember(items, food.products) { Suggestions.atEmpty(items, food.products, ZonedDateTime.now()) }
                if (picks.isEmpty()) {
                    Text(stringResource(R.string.food_add_empty_hint), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.testTag("addHint"))
                } else {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.testTag("addSuggestions")) {
                        picks.forEach { p ->
                            SuggestionChip(onClick = { actions.onPick(Suggestion.Own(p)) }, label = { Text(p.name) }, modifier = Modifier.heightIn(min = 48.dp).testTag("pick-${p.name}"))
                        }
                    }
                }
            } else {
                val matches = remember(food.query, items, food.products) { Suggestions.typing(food.query, food.products, base, items) }
                Column(Modifier.testTag("addMatches")) {
                    matches.forEach { s ->
                        Row(
                            Modifier.fillMaxWidth().clickable { actions.onPick(s) }.heightIn(min = 52.dp).padding(horizontal = 4.dp, vertical = 6.dp).testTag("match-${s.name}"),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(s.name, style = MaterialTheme.typography.bodyLarge)
                                Text(energyText(s.product.energy), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (s.product.treat) TreatSign()
                        }
                    }
                }
            }
        }
    }
}

/** "Rahka 200 g · 130 kcal · Muuta · Kumoa" for the latest add. */
@Composable
private fun LastAddedLine(item: FoodItem, actions: FoodActions) {
    Row(Modifier.fillMaxWidth().testTag("lastAdded"), verticalAlignment = Alignment.CenterVertically) {
        val text = listOfNotNull(
            listOfNotNull(item.name, item.amount?.let(::formatAmount)).joinToString(" "),
            item.kcal?.let { stringResource(R.string.food_kcal, formatKcal(it)) } ?: stringResource(R.string.food_no_kcal),
        ).joinToString(" · ")
        Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        TextButton(onClick = { actions.onEdit(item) }, modifier = Modifier.testTag("lastChange")) { Text(stringResource(R.string.food_change)) }
        TextButton(onClick = actions.onUndoAdd, modifier = Modifier.testTag("lastUndo")) { Text(stringResource(R.string.undo)) }
    }
}

private fun number(text: String): Double? = text.trim().replace(',', '.').replace(" ", "").toDoubleOrNull()?.takeIf { it >= 0 }

private fun numberText(value: Double?): String = value?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString().replace('.', ',') } ?: ""

/** The item editor (design "Editor"): name, amount with its unit choice, kcal, Herkku, Poista. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FoodEditor(item: FoodItem, products: List<Product>, actions: FoodActions) {
    val start = remember(item) { FoodEdits.editStart(item, products.firstOrNull { it.key == item.key }) }
    var name by remember(item) { mutableStateOf(start.name) }
    var mode by remember(item) { mutableStateOf(start.mode) }
    var amount by remember(item) { mutableStateOf(numberText(start.amount)) }
    var kcal by remember(item) { mutableStateOf(numberText(start.kcal)) }
    var treat by remember(item) { mutableStateOf(start.treat) }
    val input = EditInput(name, mode, number(amount), number(kcal), treat)
    AlertDialog(
        onDismissRequest = actions.onCloseEditor,
        title = { Text(stringResource(R.string.food_edit_title)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()).testTag("foodEditor"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.food_edit_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth().testTag("editName"))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(EditMode.KPL to R.string.food_unit_kpl, EditMode.G to R.string.food_unit_g, EditMode.TOTAL to R.string.food_unit_total).forEach { (m, label) ->
                        FilterChip(selected = mode == m, onClick = { mode = m }, label = { Text(stringResource(label)) }, modifier = Modifier.testTag("mode-${m.name}"))
                    }
                }
                if (mode != EditMode.TOTAL) {
                    OutlinedTextField(
                        amount, { amount = it },
                        label = { Text(stringResource(R.string.food_edit_amount) + " (" + stringResource(if (mode == EditMode.KPL) R.string.food_unit_kpl else R.string.food_unit_g) + ")") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("editAmount"),
                    )
                }
                val kcalLabel = when (mode) {
                    EditMode.KPL -> R.string.food_edit_kcal_kpl
                    EditMode.G -> R.string.food_edit_kcal_g
                    EditMode.TOTAL -> R.string.food_edit_kcal_total
                }
                OutlinedTextField(
                    kcal, { kcal = it },
                    label = { Text(stringResource(kcalLabel)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("editKcal"),
                )
                Text(
                    stringResource(R.string.food_edit_result, FoodEdits.kcalOf(input)?.let { stringResource(R.string.food_kcal, formatKcal(it)) } ?: stringResource(R.string.food_no_kcal)),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.testTag("editResult"),
                )
                if (mode != EditMode.TOTAL) Text(stringResource(R.string.food_edit_product_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.food_treat), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Switch(checked = treat, onCheckedChange = { treat = it }, modifier = Modifier.testTag("editTreat"))
                }
            }
        },
        confirmButton = { TextButton(onClick = { actions.onSave(item, input) }, modifier = Modifier.testTag("editSave")) { Text(stringResource(R.string.food_save)) } },
        dismissButton = {
            Row {
                TextButton(onClick = { actions.onDelete(item) }, modifier = Modifier.testTag("editDelete")) { Text(stringResource(R.string.food_delete)) }
                TextButton(onClick = actions.onCloseEditor) { Text(stringResource(R.string.cancel)) }
            }
        },
    )
}
