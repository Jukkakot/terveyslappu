package fi.jukkakot.terveyslappu.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.food.FoodInfo
import fi.jukkakot.terveyslappu.food.FoodItem
import fi.jukkakot.terveyslappu.food.InfoLine
import fi.jukkakot.terveyslappu.food.Product
import kotlinx.coroutines.launch

/**
 * A food row's ⓘ button (food-log "Item row buttons"): a Material 3 rich tooltip with the annos's
 * details, no actions. Tap toggles it; back, an outside tap or [closeOnScroll] closes it.
 * Tag `<tag>-info`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ItemInfoButton(item: FoodItem, products: List<Product>, tag: String, closeOnScroll: Boolean = false) {
    val state = rememberTooltipState(isPersistent = true)
    val scope = rememberCoroutineScope()
    val lines = remember(item, products) { FoodInfo.lines(item, FoodInfo.ownProduct(item, products)) }
    LaunchedEffect(closeOnScroll) { if (closeOnScroll && state.isVisible) state.dismiss() }
    if (state.isVisible) BackHandler { scope.launch { state.dismiss() } }
    TooltipBox(
        positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
        tooltip = {
            RichTooltip(title = { Text(item.name) }, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }.testTag("$tag-bubble")) {
                Column {
                    lines.forEach { Text(infoText(it), style = MaterialTheme.typography.bodyMedium) }
                }
            }
        },
        state = state,
        enableUserInput = false,
    ) {
        IconButton(
            onClick = { scope.launch { if (state.isVisible) state.dismiss() else state.show() } },
            modifier = Modifier.size(44.dp).testTag("$tag-info"),
        ) {
            Icon(painterResource(R.drawable.ic_info), contentDescription = stringResource(R.string.food_info, item.name), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun infoText(line: InfoLine): String = when (line) {
    is InfoLine.AmountLine -> formatAmount(line.amount)
    is InfoLine.KcalLine -> line.kcal?.let { stringResource(R.string.food_kcal, formatKcal(it)) } ?: stringResource(R.string.food_no_kcal)
    is InfoLine.EnergyLine -> energyText(line.energy)
    InfoLine.TreatLine -> stringResource(R.string.food_treat)
    is InfoLine.TimeLine -> stringResource(R.string.food_info_time, formatTime(line.time))
}
