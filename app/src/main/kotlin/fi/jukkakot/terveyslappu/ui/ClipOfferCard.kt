package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.food.ClipLine
import fi.jukkakot.terveyslappu.food.ClipOffer
import java.time.LocalDate

/**
 * The clipboard card (food-log "Clipboard offer"), in the add sheet, on Ruoka and on the front
 * page: "Leikepöydällä" with the total, the item lines, Lisää kaikki (to [day]), Muokkaa and ✕.
 */
@Composable
internal fun ClipOfferCard(offer: ClipOffer, day: LocalDate, actions: FoodActions, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val today = LocalDate.now()
    Column(
        modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(colors.surfaceContainerHigh)
            .padding(start = 16.dp, end = 4.dp, bottom = 12.dp).testTag("clipOffer"),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.ai_clip_title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            offer.kcal?.let { Text(stringResource(R.string.food_kcal, formatKcal(it)), style = MaterialTheme.typography.titleMedium, modifier = Modifier.testTag("clipTotal")) }
            IconButton(onClick = actions.onClipSkip, modifier = Modifier.size(48.dp).testTag("clipSkip")) {
                Icon(painterResource(R.drawable.ic_close), contentDescription = stringResource(R.string.ai_clip_skip))
            }
        }
        Column(Modifier.padding(end = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            offer.lines.forEach { ClipLineRow(it) }
            if (offer.more > 0) Text(stringResource(R.string.ai_clip_more, offer.more), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            if (offer.products > 0) Text(stringResource(R.string.ai_clip_more_products, offer.products), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        }
        Row(Modifier.padding(top = 12.dp, end = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val label = when {
                offer.productsOnly -> stringResource(R.string.ai_clip_save_products)
                day != today -> stringResource(R.string.ai_clip_add_all_day, dayTitle(day, today).lowercaseFirst())
                else -> stringResource(R.string.ai_clip_add_all)
            }
            Button(onClick = { actions.onClipAddAll(day) }, modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("clipAddAll")) {
                Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            OutlinedButton(onClick = actions.onClipOpen, modifier = Modifier.heightIn(min = 48.dp).testTag("clipOpen")) {
                Text(stringResource(R.string.ai_clip_edit))
            }
        }
    }
}

/** "Rahka · 200 g · herkku sign · 130 kcal", or a product's "Lohikeitto · 380 kcal / kpl"; long names end in "…". */
@Composable
private fun ClipLineRow(line: ClipLine) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val name = listOfNotNull(line.name, line.amount?.let(::formatAmount)).joinToString(" · ")
        Text(name, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        if (line.treat) TreatSign()
        val right = when {
            line.energy != null -> energyText(line.energy)
            line.kcal != null -> stringResource(R.string.food_kcal, formatKcal(line.kcal))
            else -> stringResource(R.string.food_no_kcal)
        }
        Text(right, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}

/** "Tänään" → "tänään" for use inside a line. */
internal fun String.lowercaseFirst(): String = replaceFirstChar { it.lowercase() }
