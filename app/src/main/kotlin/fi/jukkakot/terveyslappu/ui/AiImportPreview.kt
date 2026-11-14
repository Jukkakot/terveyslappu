package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.food.AiItem
import fi.jukkakot.terveyslappu.food.AiProduct
import fi.jukkakot.terveyslappu.food.Amount
import fi.jukkakot.terveyslappu.food.AmountUnit
import fi.jukkakot.terveyslappu.food.Energy
import java.time.LocalDate
import java.time.ZoneOffset

/** The AI import's preview (food-ai-import): what Lisää would write, still editable. */
data class AiPreview(val items: List<AiItem>, val products: List<AiProduct>, val skipped: Int, val day: LocalDate)

private fun number(text: String): Double? = text.trim().replace(',', '.').replace(" ", "").toDoubleOrNull()?.takeIf { it >= 0 }

private fun numberText(value: Double?): String = value?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString().replace('.', ',') } ?: ""

/** Full-screen preview: day, items, products, sum, skipped; Peruuta / Lisää at the bottom. */
@Composable
internal fun AiImportPreview(preview: AiPreview, actions: FoodActions) {
    var editItem by remember { mutableStateOf<Int?>(null) }
    var editProduct by remember { mutableStateOf<Int?>(null) }
    var pickDay by remember { mutableStateOf(false) }
    Dialog(onDismissRequest = actions.onAiCancel, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize().testTag("aiPreview"), color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.ai_title), style = MaterialTheme.typography.titleLarge)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.ai_day), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    AssistChip(onClick = { pickDay = true }, label = { Text(dayTitle(preview.day)) }, modifier = Modifier.heightIn(min = 48.dp).testTag("aiDay"))
                }
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    if (preview.items.isNotEmpty()) {
                        SectionTitle(stringResource(R.string.ai_items))
                        preview.items.forEachIndexed { i, item ->
                            val details = listOf(item.amount?.let(::formatAmount), item.kcal?.let { stringResource(R.string.food_kcal, formatKcal(it)) } ?: stringResource(R.string.food_no_kcal))
                            PreviewRow(item.name, item.treat, details.filterNotNull().joinToString(" · "), stringResource(R.string.ai_remove, item.name), "aiItem-$i", { editItem = i }) {
                                actions.onAiEdit(preview.copy(items = preview.items.filterIndexed { j, _ -> j != i }))
                            }
                        }
                    }
                    if (preview.products.isNotEmpty()) {
                        SectionTitle(stringResource(R.string.ai_products))
                        preview.products.forEachIndexed { i, p ->
                            PreviewRow(p.name, p.treat, energyText(p.energy), stringResource(R.string.ai_remove, p.name), "aiProduct-$i", { editProduct = i }) {
                                actions.onAiEdit(preview.copy(products = preview.products.filterIndexed { j, _ -> j != i }))
                            }
                        }
                    }
                    if (preview.skipped > 0) {
                        Text(
                            stringResource(R.string.ai_skipped, preview.skipped),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp).testTag("aiSkipped"),
                        )
                    }
                }
                val total = preview.items.sumOf { it.kcal ?: 0.0 }
                val without = preview.items.count { it.kcal == null }
                val sum = buildList {
                    add(stringResource(R.string.food_kcal, formatKcal(total)))
                    if (without > 0) add(stringResource(R.string.food_day_without, without))
                }.joinToString(" · ")
                Text(stringResource(R.string.ai_sum, sum), style = MaterialTheme.typography.titleMedium, modifier = Modifier.testTag("aiSum"))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = actions.onAiCancel, modifier = Modifier.heightIn(min = 48.dp).testTag("aiCancel")) { Text(stringResource(R.string.cancel)) }
                    Button(
                        onClick = actions.onAiAdd,
                        enabled = preview.items.isNotEmpty() || preview.products.isNotEmpty(),
                        modifier = Modifier.heightIn(min = 48.dp).testTag("aiAdd"),
                    ) { Text(stringResource(R.string.ai_add)) }
                }
            }
        }
    }
    if (pickDay) AiDayPicker(preview.day, { pickDay = false }) { actions.onAiEdit(preview.copy(day = it)); pickDay = false }
    editItem?.let { i ->
        preview.items.getOrNull(i)?.let { item ->
            AiItemEditor(item, { editItem = null }) { new ->
                actions.onAiEdit(preview.copy(items = preview.items.mapIndexed { j, o -> if (j == i) new else o }))
                editItem = null
            }
        }
    }
    editProduct?.let { i ->
        preview.products.getOrNull(i)?.let { product ->
            AiProductEditor(product, { editProduct = null }) { new ->
                actions.onAiEdit(preview.copy(products = preview.products.mapIndexed { j, o -> if (j == i) new else o }))
                editProduct = null
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp, bottom = 2.dp))
}

/** A row: tap edits; the remove button has its own column at the edge. */
@Composable
private fun PreviewRow(name: String, treat: Boolean, details: String, removeLabel: String, tag: String, onEdit: () -> Unit, onRemove: () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 52.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).clickable(onClick = onEdit).padding(vertical = 6.dp).testTag(tag)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f, fill = false))
                if (treat) TreatSign()
            }
            Text(details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onRemove, modifier = Modifier.testTag("$tag-remove")) {
            Icon(painterResource(R.drawable.ic_delete), contentDescription = removeLabel)
        }
    }
}

/** A single-day calendar: today or earlier. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AiDayPicker(day: LocalDate, onDismiss: () -> Unit, onPick: (LocalDate) -> Unit) {
    val today = LocalDate.now()
    val todayMillis = today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val state = rememberDatePickerState(
        initialSelectedDateMillis = day.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayMillis
            override fun isSelectableYear(year: Int) = year <= today.year
        },
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { state.selectedDateMillis?.let { onPick(java.time.Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) } ?: onDismiss() },
                modifier = Modifier.testTag("aiDayOk"),
            ) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    ) { DatePicker(state) }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AiItemEditor(item: AiItem, onDismiss: () -> Unit, onSave: (AiItem) -> Unit) {
    var name by remember(item) { mutableStateOf(item.name) }
    var amount by remember(item) { mutableStateOf(numberText(item.amount?.value)) }
    var unit by remember(item) { mutableStateOf(item.amount?.unit ?: AmountUnit.G) }
    var kcal by remember(item) { mutableStateOf(numberText(item.kcal)) }
    var treat by remember(item) { mutableStateOf(item.treat) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.food_edit_title)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()).testTag("aiItemEditor"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.food_edit_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth().testTag("aiName"))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(AmountUnit.G to R.string.food_unit_g, AmountUnit.KPL to R.string.food_unit_kpl).forEach { (u, label) ->
                        FilterChip(selected = unit == u, onClick = { unit = u }, label = { Text(stringResource(label)) })
                    }
                }
                OutlinedTextField(
                    amount, { amount = it }, label = { Text(stringResource(R.string.food_edit_amount)) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth().testTag("aiAmount"),
                )
                OutlinedTextField(
                    kcal, { kcal = it }, label = { Text(stringResource(R.string.food_edit_kcal_total)) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth().testTag("aiKcal"),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.food_treat), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Switch(checked = treat, onCheckedChange = { treat = it })
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val clean = name.trim().replace(Regex("\\s+"), " ")
                    val a = number(amount)?.takeIf { it > 0 }?.let { Amount(it, unit) }
                    onSave(AiItem(clean.ifEmpty { item.name }, a, number(kcal), treat))
                },
                modifier = Modifier.testTag("aiItemSave"),
            ) { Text(stringResource(R.string.food_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AiProductEditor(product: AiProduct, onDismiss: () -> Unit, onSave: (AiProduct) -> Unit) {
    var name by remember(product) { mutableStateOf(product.name) }
    var per by remember(product) { mutableStateOf(product.energy.per) }
    var kcal by remember(product) { mutableStateOf(numberText(product.energy.kcal)) }
    var treat by remember(product) { mutableStateOf(product.treat) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.ai_product_edit)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()).testTag("aiProductEditor"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.food_edit_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = per == AmountUnit.G, onClick = { per = AmountUnit.G }, label = { Text(stringResource(R.string.food_edit_kcal_g)) })
                    FilterChip(selected = per == AmountUnit.KPL, onClick = { per = AmountUnit.KPL }, label = { Text(stringResource(R.string.food_edit_kcal_kpl)) })
                }
                OutlinedTextField(
                    kcal, { kcal = it }, label = { Text(stringResource(R.string.food_product_energy)) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth().testTag("aiProductKcal"),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.food_treat), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Switch(checked = treat, onCheckedChange = { treat = it })
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val clean = name.trim().replace(Regex("\\s+"), " ")
                    val k = number(kcal) ?: product.energy.kcal
                    onSave(AiProduct(clean.ifEmpty { product.name }, Energy(k, per), product.usual?.takeIf { it.unit == per }, treat))
                },
                modifier = Modifier.testTag("aiProductSave"),
            ) { Text(stringResource(R.string.food_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
