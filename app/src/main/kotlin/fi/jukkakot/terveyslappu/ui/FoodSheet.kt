package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.food.EditInput
import fi.jukkakot.terveyslappu.food.EnterRow
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
 * The add field (food-log "Adding an item"): keyboard up, suggestion rows at the empty field,
 * up to five matches while typing, one tap adds, enter adds the typed text; the field stays open.
 */
@Composable
internal fun AddFoodSheet(food: FoodUiState, base: List<BaseProduct>, actions: FoodActions) {
    val day = food.sheetDay ?: return
    FoodSheetFrame(actions.onCloseSheet) { AddFoodContent(food, day, base, actions) }
}

/** The add sheet's frame: a bottom sheet with the standard scrim; a tap outside, back or a swipe closes it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FoodSheetFrame(onClose: () -> Unit, content: @Composable () -> Unit) {
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onClose, sheetState = sheet, modifier = Modifier.testTag("addSheet")) {
        Column(
            Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) { content() }
    }
}

/**
 * The sheet's content (food-log "Adding an item"): header with the day and ⋮, the clipboard card,
 * the field, its line, the headed list and the quiet import button. The keyboard rises on open and
 * when the card goes away, never while the card is shown.
 */
@Composable
internal fun AddFoodContent(food: FoodUiState, day: LocalDate, base: List<BaseProduct>, actions: FoodActions) {
    val focus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val today = LocalDate.now()
    SheetHeader(day, today, actions)
    val offer = food.clipOffer
    offer?.let { ClipOfferCard(it, day, actions) }
    OutlinedTextField(
        value = food.query,
        onValueChange = actions.onQuery,
        placeholder = { Text(stringResource(R.string.food_add_field)) },
        leadingIcon = { Icon(painterResource(R.drawable.ic_search), contentDescription = null) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { actions.onEnter() }),
        modifier = Modifier.fillMaxWidth().focusRequester(focus).testTag("addField"),
    )
    val cardShown = offer != null
    LaunchedEffect(cardShown) {
        if (!cardShown) {
            runCatching { focus.requestFocus() }
        } else if (food.query.isEmpty()) {
            // The card came after the keyboard (the dialog reads the clip on focus): put it down.
            focusManager.clearFocus()
            keyboard?.hide()
        }
    }
    food.line?.let { SheetLineRow(it, actions) }
    val items = food.items
    if (FoodNames.normalize(food.query).isEmpty()) {
        val picks = remember(items, food.products) { Suggestions.atEmpty(items.orEmpty(), food.products, ZonedDateTime.now()) }
        when {
            // Until the day is read: a quiet line, no empty hint (food-widget design 8).
            items == null -> Text(
                stringResource(if (food.loadFailed) R.string.food_sheet_read_failed else R.string.food_sheet_loading),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag(if (food.loadFailed) "addReadFailed" else "addLoading"),
            )
            picks.isEmpty() -> Text(stringResource(R.string.food_add_empty_hint), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.testTag("addHint"))
            else -> Column(Modifier.testTag("addSuggestions")) {
                ListHeading(stringResource(R.string.food_list_usual))
                picks.forEach { p -> SuggestionRow(Suggestion.Own(p), "pick", actions) }
            }
        }
    } else {
        val matches = remember(food.query, items, food.products) { Suggestions.typing(food.query, food.products, base, items.orEmpty()) }
        val enter = remember(food.query, matches, food.products) { EnterRow.of(food.query, matches, food.products, base) }
        Column(Modifier.testTag("addMatches")) {
            if (matches.isNotEmpty()) ListHeading(stringResource(R.string.food_list_products))
            matches.forEach { s -> SuggestionRow(s, "match", actions) }
            enter?.let { EnterRowView(it, actions) }
        }
    }
    TextButton(onClick = actions.onPasteAi, modifier = Modifier.heightIn(min = 48.dp).testTag("aiPaste")) { Text(stringResource(R.string.ai_paste)) }
}

/** "Lisää annos · tänään" and the ⋮ menu (Kopioi tuontiohje). */
@Composable
private fun SheetHeader(day: LocalDate, today: LocalDate, actions: FoodActions) {
    var menu by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            stringResource(R.string.food_sheet_title, dayTitle(day, today).lowercaseFirst()),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f).testTag("sheetTitle"),
        )
        Box {
            IconButton(onClick = { menu = true }, modifier = Modifier.size(48.dp).testTag("sheetMenu")) {
                Icon(painterResource(R.drawable.ic_more_vert), contentDescription = stringResource(R.string.food_sheet_more))
            }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.ai_copy_prompt)) },
                    onClick = {
                        menu = false
                        actions.onCopyAiPrompt()
                    },
                    modifier = Modifier.testTag("aiCopyPrompt"),
                )
            }
        }
    }
}

@Composable
private fun ListHeading(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
    )
}

/** The typed list's last row: "Lisää ”kana” uutena" over what enter adds; a tap is enter. */
@Composable
private fun EnterRowView(row: EnterRow, actions: FoodActions) {
    Row(
        Modifier.fillMaxWidth().clickable { actions.onEnter() }.heightIn(min = 56.dp).padding(vertical = 6.dp).testTag("enterRow"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(R.drawable.ic_add), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 12.dp).size(24.dp))
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.food_enter_row, row.text), style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            val detail = when (row) {
                is EnterRow.Quick -> stringResource(R.string.food_enter_quick, formatKcal(row.kcal))
                is EnterRow.Named -> row.product?.let { suggestionDetail(it) } ?: stringResource(R.string.food_no_kcal)
            }
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
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

/**
 * One suggestion (empty-field list and typed matches): name and usual amount · kcal; a tap adds at
 * once. A fixed trailing column: the treat sign, the pencil (editor on a new item first) and, on an
 * own product, the red trash (removes the product).
 */
@Composable
private fun SuggestionRow(s: Suggestion, tag: String, actions: FoodActions) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier.weight(1f).clickable { actions.onPick(s) }.heightIn(min = 56.dp).padding(vertical = 6.dp).testTag("$tag-${s.name}"),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(s.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(suggestionDetail(s.product), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (s.product.treat) TreatSign()
        }
        IconButton(onClick = { actions.onEditPick(s) }, modifier = Modifier.size(48.dp).testTag("edit-${s.name}")) {
            Icon(painterResource(R.drawable.ic_edit), contentDescription = stringResource(R.string.food_edit_before_add))
        }
        if (s is Suggestion.Own) {
            IconButton(onClick = { actions.onDeleteProduct(s.product) }, modifier = Modifier.size(48.dp).testTag("deleteProduct-${s.name}")) {
                Icon(painterResource(R.drawable.ic_delete), contentDescription = stringResource(R.string.food_product_delete, s.name), tint = MaterialTheme.colorScheme.error)
            }
        } else {
            Spacer(Modifier.size(48.dp))
        }
    }
}

/** The line under the field: an add with Muuta · Kumoa, a delete's or import's Kumoa, a failed write or a changed clip. */
@Composable
private fun SheetLineRow(line: SheetLine, actions: FoodActions) {
    when (line) {
        is SheetLine.Added -> Row(Modifier.fillMaxWidth().testTag("lastAdded"), verticalAlignment = Alignment.CenterVertically) {
            val item = line.item
            val text = listOfNotNull(
                listOfNotNull(item.name, item.amount?.let(::formatAmount)).joinToString(" "),
                item.kcal?.let { stringResource(R.string.food_kcal, formatKcal(it)) } ?: stringResource(R.string.food_no_kcal),
            ).joinToString(" · ")
            Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            TextButton(onClick = { actions.onEdit(item) }, modifier = Modifier.testTag("lastChange")) { Text(stringResource(R.string.food_change)) }
            TextButton(onClick = actions.onUndoAdd, modifier = Modifier.testTag("lastUndo")) { Text(stringResource(R.string.undo)) }
        }
        is SheetLine.Deleted -> UndoLine(stringResource(R.string.food_deleted, line.item.name), actions.onUndoDelete, "sheetDeleted", "deletedUndo")
        is SheetLine.ProductRemoved -> UndoLine(stringResource(R.string.food_deleted, line.product.name), actions.onUndoDeleteProduct, "sheetProductRemoved", "productUndo")
        is SheetLine.Imported -> {
            val plan = line.plan
            val text = if (plan.items.isEmpty()) stringResource(R.string.ai_products_saved, plan.products.size) else stringResource(R.string.ai_added, plan.items.size)
            UndoLine(text, actions.onUndoImport, "sheetImported", "importUndo")
        }
        SheetLine.Failed -> NoticeLine(stringResource(R.string.food_sheet_failed), MaterialTheme.colorScheme.error, "sheetFailed")
        SheetLine.ClipChanged -> NoticeLine(stringResource(R.string.ai_clip_changed), MaterialTheme.colorScheme.onSurfaceVariant, "sheetClipChanged")
    }
}

@Composable
private fun UndoLine(text: String, onUndo: () -> Unit, tag: String, undoTag: String) {
    Row(Modifier.fillMaxWidth().testTag(tag), verticalAlignment = Alignment.CenterVertically) {
        Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        TextButton(onClick = onUndo, modifier = Modifier.testTag(undoTag)) { Text(stringResource(R.string.undo)) }
    }
}

@Composable
private fun NoticeLine(text: String, color: androidx.compose.ui.graphics.Color, tag: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = color, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag(tag))
}

private fun number(text: String): Double? = text.trim().replace(',', '.').replace(" ", "").toDoubleOrNull()?.takeIf { it >= 0 }

private fun numberText(value: Double?): String = value?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString().replace('.', ',') } ?: ""

/** The item editor (design "Editor"): name, amount with its unit choice, kcal, Herkku, Poista. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FoodEditor(item: FoodItem, products: List<Product>, actions: FoodActions, draft: Boolean = false) {
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
