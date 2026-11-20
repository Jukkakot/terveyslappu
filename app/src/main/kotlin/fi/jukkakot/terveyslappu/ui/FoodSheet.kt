package fi.jukkakot.terveyslappu.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import fi.jukkakot.terveyslappu.food.DaySum
import java.time.ZoneId
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.food.EditInput
import fi.jukkakot.terveyslappu.food.EnterRow
import fi.jukkakot.terveyslappu.food.EditMode
import fi.jukkakot.terveyslappu.food.FoodDay
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

/** A suggestion's second line: "200 g · 130 kcal", "140 kcal / 100 g", "200 g · ei kcal" or "ei kcal". */
@Composable
internal fun suggestionDetail(product: Product): String {
    val amount = product.usualAmount()
    val energy = product.energy
    return when {
        amount != null && energy != null -> {
            val kcal = energy.kcalOf(amount)
            formatAmount(amount) + " · " + (kcal?.let { stringResource(R.string.food_kcal, formatKcal(it)) } ?: stringResource(R.string.food_row_no_kcal))
        }
        amount != null -> formatAmount(amount) + " · " + stringResource(R.string.food_row_no_kcal)
        energy != null -> energyText(energy)
        else -> stringResource(R.string.food_row_no_kcal)
    }
}

private fun number(text: String): Double? = text.trim().replace(',', '.').replace(" ", "").toDoubleOrNull()?.takeIf { it >= 0 }

private fun numberText(value: Double?): String = value?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString().replace('.', ',') } ?: ""

/** The item editor (design "Editor"): name, amount with its unit choice, kcal, Herkku, Poista; as a draft Lisää / Peruuta. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FoodEditor(item: FoodItem, products: List<Product>, actions: FoodActions, draft: Boolean = false, items: List<FoodItem>? = null) {
    val start = remember(item) { FoodEdits.editStart(item, products.firstOrNull { it.key == item.key }) }
    var name by remember(item) { mutableStateOf(start.name) }
    var mode by remember(item) { mutableStateOf(start.mode) }
    var amount by remember(item) { mutableStateOf(numberText(start.amount)) }
    var kcal by remember(item) { mutableStateOf(numberText(start.kcal)) }
    var treat by remember(item) { mutableStateOf(start.treat) }
    val input = EditInput(name, mode, number(amount), number(kcal), treat)
    // The same ruoka's earlier amounts in the chosen unit; none while the annokset load or for Yhteensä.
    val recent = remember(items, item, mode) {
        val unit = when (mode) {
            EditMode.KPL -> AmountUnit.KPL
            EditMode.G -> AmountUnit.G
            EditMode.TOTAL -> null
        }
        if (items == null || unit == null) emptyList() else FoodDay.recentAmounts(items, item.key, unit, item.uuid)
    }
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
                    RecentAmountChips(recent, number(amount)) { amount = numberText(it.value) }
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
        confirmButton = {
            // Edit before adding: Lisää / Peruuta instead of Tallenna / Poista.
            if (draft) TextButton(onClick = { actions.onAddDraft(input) }, modifier = Modifier.testTag("editAdd")) { Text(stringResource(R.string.ai_add)) }
            else TextButton(onClick = { actions.onSave(item, input) }, modifier = Modifier.testTag("editSave")) { Text(stringResource(R.string.food_save)) }
        },
        dismissButton = {
            Row {
                if (!draft) TextButton(onClick = { actions.onDelete(item) }, modifier = Modifier.testTag("editDelete")) { Text(stringResource(R.string.food_delete), color = MaterialTheme.colorScheme.error) }
                TextButton(onClick = actions.onCloseEditor, modifier = Modifier.testTag("editCancel")) { Text(stringResource(R.string.cancel)) }
            }
        },
    )
}

/**
 * The editor's recent amounts (food-log "Recent amounts in the editor"): one chip per amount, the
 * one equal to the field ([current]) chosen; a tap fills the field and saves nothing. No row when empty.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RecentAmountChips(recent: List<fi.jukkakot.terveyslappu.food.Amount>, current: Double?, onPick: (fi.jukkakot.terveyslappu.food.Amount) -> Unit) {
    if (recent.isEmpty()) return
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.testTag("recentAmounts")) {
        recent.forEach { chip ->
            FilterChip(
                selected = current == chip.value,
                onClick = { onPick(chip) },
                label = { Text(formatAmount(chip)) },
                modifier = Modifier.testTag("recent-${chip.value}"),
            )
        }
    }
}
