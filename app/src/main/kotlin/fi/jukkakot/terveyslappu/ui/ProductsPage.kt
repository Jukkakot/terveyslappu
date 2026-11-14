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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.food.Amount
import fi.jukkakot.terveyslappu.food.AmountUnit
import fi.jukkakot.terveyslappu.food.Energy
import fi.jukkakot.terveyslappu.food.FoodNames
import fi.jukkakot.terveyslappu.food.Product

/** Ruokakirjaus in Asetukset (food-log "Ruokakirjaus in Asetukset"): the own products, edit and remove. */
@Composable
internal fun ProductList(products: List<Product>, actions: FoodActions) {
    var editing by remember { mutableStateOf<Product?>(null) }
    val sorted = remember(products) { products.sortedBy { it.key } }
    if (sorted.isEmpty()) {
        Text(
            stringResource(R.string.food_products_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp).testTag("productsEmpty"),
        )
    }
    sorted.forEachIndexed { i, p ->
        if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            Modifier.fillMaxWidth().clickable { editing = p }.heightIn(min = 56.dp).padding(horizontal = 16.dp, vertical = 8.dp).testTag("product-${p.name}"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(p.name, style = MaterialTheme.typography.bodyLarge)
                val line = listOfNotNull(energyText(p.energy), p.lastAmount?.let { stringResource(R.string.food_usual, formatAmount(it)) }).joinToString(", ")
                Text(line, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (p.treat) TreatSign()
        }
    }
    editing?.let { p ->
        ProductEditor(
            p,
            onSave = { new -> if (actions.onSaveProduct(p.key, new)) editing = null },
            onRemove = {
                actions.onRemoveProduct(p.key)
                editing = null
            },
            onDismiss = { editing = null },
        )
    }
}

private fun parse(text: String): Double? = text.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it >= 0 }

private fun show(value: Double?): String = value?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString().replace('.', ',') } ?: ""

/** Name, energy (per kpl or per 100 g), usual amount, Herkku; Poista tuote. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductEditor(product: Product, onSave: (Product) -> Unit, onRemove: () -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(product.name) }
    var unit by remember { mutableStateOf(product.energy?.per ?: product.lastAmount?.unit ?: AmountUnit.KPL) }
    var kcal by remember { mutableStateOf(show(product.energy?.kcal)) }
    var usual by remember { mutableStateOf(show(product.lastAmount?.value)) }
    var treat by remember { mutableStateOf(product.treat) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.food_product_title)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()).testTag("productEditor"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.food_edit_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth().testTag("productName"))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(AmountUnit.KPL to R.string.food_edit_kcal_kpl, AmountUnit.G to R.string.food_edit_kcal_g).forEach { (u, label) ->
                        FilterChip(selected = unit == u, onClick = { unit = u }, label = { Text(stringResource(label)) })
                    }
                }
                OutlinedTextField(
                    kcal, { kcal = it },
                    label = { Text(stringResource(R.string.food_product_energy) + " (" + stringResource(if (unit == AmountUnit.KPL) R.string.food_edit_kcal_kpl else R.string.food_edit_kcal_g) + ")") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("productKcal"),
                )
                OutlinedTextField(
                    usual, { usual = it },
                    label = { Text(stringResource(R.string.food_product_usual) + " (" + stringResource(if (unit == AmountUnit.KPL) R.string.food_unit_kpl else R.string.food_unit_g) + ")") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.food_treat), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Switch(checked = treat, onCheckedChange = { treat = it })
                }
                Text(stringResource(R.string.food_product_remove_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        Product(
                            name = FoodNames.clean(name).ifEmpty { product.name },
                            treat = treat,
                            energy = parse(kcal)?.let { Energy(it, unit) },
                            lastAmount = parse(usual)?.takeIf { it > 0 }?.let { Amount(it, unit) },
                        ),
                    )
                },
                modifier = Modifier.testTag("productSave"),
            ) { Text(stringResource(R.string.food_save)) }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onRemove, modifier = Modifier.testTag("productRemove")) { Text(stringResource(R.string.food_product_remove)) }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
            }
        },
    )
}
