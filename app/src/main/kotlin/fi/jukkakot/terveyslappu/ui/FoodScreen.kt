package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.food.Amount
import fi.jukkakot.terveyslappu.food.AmountUnit
import fi.jukkakot.terveyslappu.food.DaySum
import fi.jukkakot.terveyslappu.food.EditInput
import fi.jukkakot.terveyslappu.food.FoodItem
import fi.jukkakot.terveyslappu.food.Product
import fi.jukkakot.terveyslappu.food.Suggestion
import fi.jukkakot.terveyslappu.weight.HistoryAccess
import java.text.NumberFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/** Everything the food screens can ask for; the activity wires these to [FoodViewModel] and the permission screen. */
class FoodActions(
    /** Ruoka on a day (asks the nutrition permission first when it was never asked). */
    val onOpenDay: (LocalDate) -> Unit = {},
    /** Lisää annos for a day. */
    val onAdd: (LocalDate) -> Unit = {},
    val onCloseSheet: () -> Unit = {},
    val onQuery: (String) -> Unit = {},
    val onPick: (Suggestion) -> Unit = {},
    val onEnter: () -> Unit = {},
    val onUndoAdd: () -> Unit = {},
    val onEdit: (FoodItem) -> Unit = {},
    val onCloseEditor: () -> Unit = {},
    val onSave: (FoodItem, EditInput) -> Unit = { _, _ -> },
    val onDelete: (FoodItem) -> Unit = {},
    val onDay: (LocalDate) -> Unit = {},
    val onStartOrder: () -> Unit = {},
    val onMove: (Int, Int) -> Unit = { _, _ -> },
    val onFinishOrder: () -> Unit = {},
    val onGrant: () -> Unit = {},
    val onSaveProduct: (String, Product) -> Boolean = { _, _ -> true },
    val onRemoveProduct: (String) -> Unit = {},
)

private val finnish = Locale.forLanguageTag("fi")
private val dayFormat = DateTimeFormatter.ofPattern("EEEE d.M.", finnish)
private val clock = DateTimeFormatter.ofPattern("H.mm", finnish)

/** 1760 → "1 760". */
fun formatKcal(kcal: Double): String = NumberFormat.getIntegerInstance(finnish).format(kcal.roundToInt())

fun formatKcal(kcal: Int): String = formatKcal(kcal.toDouble())

/** 200 g, 1 kpl, 1,5 kpl. */
fun formatAmount(amount: Amount): String {
    val n = if (amount.value % 1.0 == 0.0) amount.value.toLong().toString() else String.format(finnish, "%.1f", amount.value)
    return if (amount.unit == AmountUnit.G) "$n g" else "$n kpl"
}

@Composable
internal fun dayTitle(day: LocalDate, today: LocalDate = LocalDate.now()): String = when (day) {
    today -> stringResource(R.string.food_today)
    today.minusDays(1) -> stringResource(R.string.food_yesterday)
    else -> dayFormat.format(day).replaceFirstChar { it.titlecase(finnish) }
}

/** "1 760 kcal · herkkuja 230 · 1 ilman kcal" (food-log "Ruoka day screen"). */
@Composable
internal fun daySumText(sum: DaySum): String = buildList {
    add(stringResource(R.string.food_kcal, formatKcal(sum.total)))
    if (sum.treat > 0) add(stringResource(R.string.food_day_treats, formatKcal(sum.treat)))
    if (sum.withoutKcal > 0) add(stringResource(R.string.food_day_without, sum.withoutKcal))
}.joinToString(" · ")

/** The reason and grant button shown in place of the food log without the nutrition permission. */
@Composable
internal fun FoodPermissionNotice(text: String, onGrant: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(24.dp).testTag("foodPermission"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        TextButton(onClick = onGrant, modifier = Modifier.testTag("foodGrant")) { Text(stringResource(R.string.food_permission_grant)) }
    }
}

/** Ruoka: one day's items in eating order, its sums, earlier days by arrows (food-log "Ruoka day screen"). */
@Composable
internal fun FoodScreen(
    food: FoodUiState,
    actions: FoodActions,
    history: HistoryAccess?,
    onGrantHistory: () -> Unit,
    onBack: () -> Unit,
    snackbar: SnackbarHostState,
) {
    val today = LocalDate.now()
    val ordering = food.ordering
    // Leaving Järjestä any other way also saves the order (review decision).
    DisposableEffect(Unit) { onDispose { actions.onFinishOrder() } }
    Scaffold(
        topBar = {
            SubScreenBar(stringResource(R.string.food_title), onBack = {
                if (ordering != null) actions.onFinishOrder()
                onBack()
            }) {
                if (food.access == FoodAccess.GRANTED && food.items != null) {
                    if (ordering != null) {
                        TextButton(onClick = actions.onFinishOrder, modifier = Modifier.testTag("orderDone")) { Text(stringResource(R.string.food_order_done)) }
                    } else if (food.dayItems(food.day).size > 1) {
                        IconButton(onClick = actions.onStartOrder, modifier = Modifier.testTag("order")) {
                            Icon(painterResource(R.drawable.ic_sort), contentDescription = stringResource(R.string.food_order))
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (food.access == FoodAccess.GRANTED && ordering == null) {
                Box(Modifier.navigationBarsPadding().padding(16.dp)) {
                    OutlinedButton(
                        onClick = { actions.onAdd(food.day) },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("foodAdd"),
                    ) {
                        Icon(painterResource(R.drawable.ic_add), contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.food_add), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                food.access == FoodAccess.CHECKING -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                food.access != FoodAccess.GRANTED -> FoodPermissionNotice(stringResource(R.string.food_permission_reason), actions.onGrant, Modifier.align(Alignment.Center))
                food.items == null && food.loadFailed -> Notice(stringResource(R.string.food_read_failed))
                food.items == null -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                else -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).testTag("foodDay")) {
                    DayBar(food.day, today, enabled = ordering == null, actions.onDay)
                    if (history == HistoryAccess.NOT_GRANTED && food.day < today.minusDays(30)) HistoryNotice(onGrantHistory)
                    val items = ordering ?: food.dayItems(food.day)
                    if (items.isEmpty()) {
                        Text(
                            stringResource(R.string.food_day_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 24.dp).testTag("foodEmpty"),
                        )
                    } else {
                        Text(
                            daySumText(DaySum.of(items)),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 12.dp).testTag("foodSum"),
                        )
                        if (ordering != null) {
                            Text(stringResource(R.string.food_order_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
                            OrderList(ordering, actions.onMove)
                        } else {
                            Column(Modifier.fillMaxWidth().clip(groupShape).background(MaterialTheme.colorScheme.surfaceContainer)) {
                                items.forEachIndexed { i, item ->
                                    if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                    FoodRow(item, ZoneId.systemDefault(), Modifier.clickable { actions.onEdit(item) })
                                }
                            }
                        }
                    }
                    Spacer(Modifier.heightIn(min = 16.dp))
                }
            }
        }
    }
}

@Composable
private fun DayBar(day: LocalDate, today: LocalDate, enabled: Boolean, onDay: (LocalDate) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onDay(day.minusDays(1)) }, enabled = enabled, modifier = Modifier.testTag("prevDay")) {
            Icon(painterResource(R.drawable.ic_chevron_left), contentDescription = stringResource(R.string.food_prev_day))
        }
        Text(dayTitle(day, today), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, modifier = Modifier.weight(1f).testTag("foodDayTitle"))
        // No days after today (food-log "Ruoka day screen").
        IconButton(onClick = { onDay(day.plusDays(1)) }, enabled = enabled && day < today, modifier = Modifier.testTag("nextDay")) {
            Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = stringResource(R.string.food_next_day))
        }
    }
}

/** One item: name with the herkku sign, amount and small time; kcal on the right. */
@Composable
internal fun FoodRow(item: FoodItem, zone: ZoneId, modifier: Modifier = Modifier, trailing: @Composable () -> Unit = {}) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 16.dp, vertical = 8.dp).testTag("food-${item.uuid}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(item.name, style = MaterialTheme.typography.bodyLarge)
                if (item.treat) TreatSign()
            }
            val details = listOfNotNull(
                item.amount?.let(::formatAmount),
                if (item.timed) clock.format(item.time.atZone(zone)) else null,
            ).joinToString(" · ")
            if (details.isNotEmpty()) Text(details, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
        Text(
            item.kcal?.let { stringResource(R.string.food_kcal, formatKcal(it)) } ?: stringResource(R.string.food_no_kcal),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (item.kcal != null) FontWeight.SemiBold else FontWeight.Normal,
            color = if (item.kcal != null) colors.onSurface else colors.onSurfaceVariant,
        )
        trailing()
    }
}

/** The small herkku sign: the treat tone, never red or a warning (food-log "Treats apart"). */
@Composable
internal fun TreatSign() {
    Text(
        stringResource(R.string.food_treat_sign),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onTertiaryContainer,
        modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.tertiaryContainer).padding(horizontal = 6.dp, vertical = 1.dp),
    )
}

/** Järjestä: drag a row by its handle, or move it with the arrows (no gesture without an alternative). */
@Composable
private fun OrderList(items: List<FoodItem>, onMove: (Int, Int) -> Unit) {
    var dragged by remember { mutableIntStateOf(-1) }
    var offset by remember { mutableFloatStateOf(0f) }
    var rowHeight by remember { mutableIntStateOf(1) }
    val zone = ZoneId.systemDefault()
    Column(Modifier.fillMaxWidth().clip(groupShape).background(MaterialTheme.colorScheme.surfaceContainer).testTag("orderList")) {
        items.forEachIndexed { i, item -> key(item.uuid) {
            if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            val lifted = i == dragged
            FoodRow(
                item,
                zone,
                Modifier
                    .onSizeChanged { rowHeight = it.height.coerceAtLeast(1) }
                    .offset { IntOffset(0, if (lifted) offset.roundToInt() else 0) }
                    .background(if (lifted) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainer),
            ) {
                IconButton(onClick = { onMove(i, i - 1) }, enabled = i > 0, modifier = Modifier.size(40.dp)) {
                    Icon(painterResource(R.drawable.ic_arrow_up), contentDescription = stringResource(R.string.food_move_up, item.name))
                }
                IconButton(onClick = { onMove(i, i + 1) }, enabled = i < items.lastIndex, modifier = Modifier.size(40.dp)) {
                    Icon(painterResource(R.drawable.ic_arrow_down), contentDescription = stringResource(R.string.food_move_down, item.name))
                }
                Icon(
                    painterResource(R.drawable.ic_drag_handle),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp).pointerInput(items.size) {
                        detectDragGestures(
                            onDragStart = {
                                dragged = items.indexOfFirst { it.uuid == item.uuid }
                                offset = 0f
                            },
                            onDragEnd = { dragged = -1; offset = 0f },
                            onDragCancel = { dragged = -1; offset = 0f },
                        ) { change, amount ->
                            change.consume()
                            offset += amount.y
                            val half = rowHeight / 2f
                            if (offset > half && dragged in 0 until items.lastIndex) {
                                onMove(dragged, dragged + 1)
                                dragged += 1
                                offset -= rowHeight
                            } else if (offset < -half && dragged > 0) {
                                onMove(dragged, dragged - 1)
                                dragged -= 1
                                offset += rowHeight
                            }
                        }
                    },
                )
            }
        } }
    }
}
