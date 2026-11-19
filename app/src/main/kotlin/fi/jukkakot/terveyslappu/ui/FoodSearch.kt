package fi.jukkakot.terveyslappu.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.food.BaseProduct
import fi.jukkakot.terveyslappu.food.EnterRow
import fi.jukkakot.terveyslappu.food.FoodNames
import fi.jukkakot.terveyslappu.food.Product
import fi.jukkakot.terveyslappu.food.Suggestion
import fi.jukkakot.terveyslappu.food.Suggestions
import java.time.LocalDate

/** What only the add search asks of its host (the widget's activity, or the app's main screen). */
class FoodDialogActions(
    /** Salli: the nutrition permission, asked in place. */
    val onGrant: () -> Unit = {},
    /** Denied: Health Connect's app permissions; checked again on return. */
    val onOpenSettings: () -> Unit = {},
    /** The search's window gained focus (the clipboard may be read only then). */
    val onFocused: () -> Unit = {},
)

/** The day the search is showing (it stays shown under the editor of "Muokkaa"); null = no search. */
internal val FoodUiState.searchShown: LocalDate? get() = searchDay ?: editReturn

private const val MATCH_LIMIT = 10

/** One bar of the search: text, the action's label (null = no action) and what the action does. */
private class Bar(val text: String, val label: String?, val action: (() -> Unit)?)

/**
 * The add search (food-add-search): a full-screen search of what can be added, with no day list. The
 * top bar (back, field, ✕, ⋮), the clipboard card, "Usein tähän aikaan" or the typed matches, and the
 * app's bar at the bottom for every message. Shared by the app (over its screens) and the widget's
 * activity; the permission states show in its place.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddFoodSearch(food: FoodUiState, base: List<BaseProduct>, actions: FoodActions, dialog: FoodDialogActions) {
    val day = food.searchShown ?: return
    val snackbar = remember { SnackbarHostState() }
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val granted = food.access == FoodAccess.GRANTED
    val windowFocused = LocalWindowInfo.current.isWindowFocused
    LaunchedEffect(windowFocused) { if (windowFocused) dialog.onFocused() }
    BackHandler { actions.onLeaveSearch() }

    // The keyboard comes up once the permission is confirmed; after an editor only if it was up before.
    LaunchedEffect(granted, food.editing == null) {
        if (granted && food.editing == null && food.keyboardBack) {
            actions.onKeyboardBack(false)
            runCatching { focus.requestFocus() }
            keyboard?.show()
        }
    }
    // Every message is a bar; the newest replaces the previous one (design 3).
    val bar = food.line?.let { barOf(it, actions) }
    LaunchedEffect(food.line) {
        val line = food.line ?: return@LaunchedEffect
        val b = bar ?: return@LaunchedEffect
        val result = snackbar.showTimed(b.text, b.label)
        if (result == SnackbarResult.ActionPerformed) b.action?.invoke() else actions.onClearLine(line)
    }
    val readFailed = stringResource(R.string.food_search_read_failed)
    LaunchedEffect(food.loadFailed) {
        if (food.loadFailed) snackbar.showTimed(readFailed, null)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding().testTag("addSearch"),
        topBar = { SearchTopBar(food, granted, focus, actions, dialog) },
        snackbarHost = { AppSnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (food.access) {
                // Checking: the top bar only, no keyboard yet.
                FoodAccess.CHECKING -> Box(Modifier.fillMaxSize().testTag("sheetChecking"))
                FoodAccess.GRANTED -> SearchBody(food, day, base, actions, focus)
                FoodAccess.NEEDS -> SearchNotice(stringResource(R.string.food_permission_reason), stringResource(R.string.food_sheet_allow), dialog.onGrant, "sheetGrant")
                FoodAccess.DENIED -> SearchNotice(stringResource(R.string.food_permission_reason), stringResource(R.string.food_permission_settings), dialog.onOpenSettings, "sheetSettings")
                FoodAccess.UNAVAILABLE -> SearchNotice(stringResource(R.string.hc_unavailable), stringResource(R.string.close), actions.onLeaveSearch, "sheetClose")
            }
        }
    }
}

@Composable
private fun barOf(line: SheetLine, actions: FoodActions): Bar? {
    val undo = stringResource(R.string.undo)
    return when (line) {
        is SheetLine.Added -> Bar(stringResource(R.string.food_added, line.item.name), undo, actions.onUndoAdd)
        is SheetLine.ProductRemoved -> Bar(stringResource(R.string.food_deleted, line.product.name), undo, actions.onUndoDeleteProduct)
        is SheetLine.Imported -> {
            val plan = line.plan
            Bar(if (plan.items.isEmpty()) stringResource(R.string.ai_products_saved, plan.products.size) else stringResource(R.string.ai_added, plan.items.size), undo, actions.onUndoImport)
        }
        SheetLine.Failed -> Bar(stringResource(R.string.food_sheet_failed), null, null)
        is SheetLine.ClipSkipped -> Bar(stringResource(R.string.ai_clip_skipped), undo, actions.onUndoClipSkip)
        SheetLine.ClipChanged -> Bar(stringResource(R.string.ai_clip_changed), null, null)
        is SheetLine.EnergyUpdated -> Bar(pluralStringResource(R.plurals.food_energy_updated, line.undo.count, line.undo.count), undo, actions.onUndoEnergy)
        is SheetLine.EnergyFailed -> Bar(
            pluralStringResource(R.plurals.food_energy_partial, line.of, line.done, line.of), stringResource(R.string.food_energy_retry), actions.onRetryEnergy,
        )
        SheetLine.EnergyReadFailed -> Bar(stringResource(R.string.food_energy_read_failed), null, null)
        is SheetLine.Message -> Bar(stringResource(line.text), null, null)
    }
}

/** Back, the field (with ✕ while it has text) and ⋮; before the permission is confirmed only the back arrow. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(food: FoodUiState, granted: Boolean, focus: FocusRequester, actions: FoodActions, dialog: FoodDialogActions) {
    var menu by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().statusBarsPadding().padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = actions.onLeaveSearch, modifier = Modifier.size(48.dp).testTag("back")) {
            Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.back))
        }
        if (granted) {
            Surface(Modifier.weight(1f), shape = SearchBarDefaults.inputFieldShape, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                SearchBarDefaults.InputField(
                    query = food.query,
                    onQueryChange = actions.onQuery,
                    onSearch = { actions.onEnter() },
                    expanded = true,
                    onExpandedChange = {},
                    placeholder = { Text(stringResource(R.string.food_add_field), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingIcon = { Icon(painterResource(R.drawable.ic_search), contentDescription = null) },
                    trailingIcon = {
                        if (food.query.isNotEmpty()) {
                            IconButton(onClick = { actions.onQuery("") }, modifier = Modifier.testTag("searchClear")) {
                                Icon(painterResource(R.drawable.ic_close), contentDescription = stringResource(R.string.food_search_clear))
                            }
                        }
                    },
                    modifier = Modifier.focusRequester(focus).testTag("addField"),
                )
            }
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
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.ai_paste)) },
                        onClick = {
                            menu = false
                            actions.onPasteAi()
                        },
                        modifier = Modifier.testTag("aiPaste"),
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
/** The list under the top bar: the day line, the clipboard card, then the usual ruuat or the typed matches. */
@Composable
private fun SearchBody(food: FoodUiState, day: LocalDate, base: List<BaseProduct>, actions: FoodActions, focus: FocusRequester) {
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val imeUpNow by rememberUpdatedState(WindowInsets.isImeVisible)
    // An editor replaces the keyboard: remember whether it was up so it comes back with the search.
    val beforeEditor = { actions.onKeyboardBack(imeUpNow) }
    // The date is re-checked on resume and on each add (no ticker): "Lisätään: Eilen" past midnight.
    var today by remember { mutableStateOf(LocalDate.now()) }
    LifecycleResumeEffect(Unit) {
        today = LocalDate.now()
        onPauseOrDispose {}
    }
    LaunchedEffect(food.addedIds) { today = LocalDate.now() }
    val typing = FoodNames.normalize(food.query).isNotEmpty()
    val listState = rememberLazyListState()
    // Scrolling the list by hand puts the keyboard down.
    LaunchedEffect(listState) {
        listState.interactionSource.interactions.collect { if (it is DragInteraction.Start) { focusManager.clearFocus(); keyboard?.hide() } }
    }
    val items = food.items.orEmpty()
    val matches = remember(food.query, items, food.products, base) { if (typing) Suggestions.typing(food.query, food.products, base, items, MATCH_LIMIT) else emptyList() }
    val enter = remember(food.query, matches, food.products, base) { if (typing) EnterRow.of(food.query, matches, food.products, base) else null }
    // A removed ruoka leaves the fixed ranking and returns to its place with Kumoa.
    val picks = food.picks?.let { all -> val keys = food.products.map { it.key }.toSet(); all.filter { it.key in keys } }
    LazyColumn(Modifier.fillMaxSize().testTag("searchList"), state = listState) {
        if (day != today) item(key = "adding") {
            Text(
                stringResource(R.string.food_adding_to, dayTitle(day, today)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).testTag("addingTo"),
            )
        }
        if (!typing) food.clipOffer?.let { o -> item(key = "clip") { ClipOfferCard(o, day, actions, Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp)) } }
        if (typing) {
            items(matches, key = { "m-" + it.name + (it is Suggestion.Own) }) { s -> AddRow(s, "match", actions, beforeEditor) }
            enter?.let { row -> item(key = "enter") { EnterRowView(row, actions) } }
        } else when {
            // Until the day is read: one quiet line (a failed read says so in the bar).
            food.items == null -> if (!food.loadFailed) item(key = "state") { QuietLine(stringResource(R.string.food_sheet_loading), "addLoading") }
            picks.isNullOrEmpty() -> item(key = "state") { QuietLine(stringResource(R.string.food_search_empty), "addEmpty") }
            else -> {
                item(key = "heading") { ListHeading(stringResource(R.string.food_frequent)) }
                items(picks, key = { "p-" + it.key }) { p -> AddRow(Suggestion.Own(p), "pick", actions, beforeEditor) }
            }
        }
    }
}

@Composable
private fun QuietLine(text: String, tag: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).testTag(tag))
}

@Composable
private fun ListHeading(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp).testTag("addHeading"))
}

/** The typed list's last row: "Lisää ”kana” uutena" over what enter adds; a tap is enter. */
@Composable
private fun EnterRowView(row: EnterRow, actions: FoodActions) {
    Row(
        Modifier.fillMaxWidth().clickable { actions.onEnter() }.heightIn(min = 56.dp).padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 6.dp).testTag("enterRow"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlusIcon()
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

@Composable
private fun PlusIcon() {
    Icon(painterResource(R.drawable.ic_add), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 16.dp).size(24.dp))
}

/**
 * One ruoka to add (empty-field rows and typed matches): + icon, name with the herkku sign, usual
 * amount · kcal under it; a tap adds at once. The ⋮ menu: Muokkaa (the editor for a new annos) and,
 * on an own ruoka, Poista. No dividers.
 */
@Composable
private fun AddRow(s: Suggestion, tag: String, actions: FoodActions, beforeEditor: () -> Unit) {
    val addLabel = stringResource(R.string.food_add_row, s.name)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier.weight(1f).clickable(onClickLabel = addLabel) { actions.onPick(s) }.heightIn(min = 56.dp).padding(start = 16.dp, top = 6.dp, bottom = 6.dp).testTag("$tag-${s.name}"),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlusIcon()
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(s.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    if (s.product.treat) TreatSign()
                }
                Text(suggestionDetail(s.product), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        // ⋮: Muokkaa (the editor for a new item) and, on an own product, Poista (removes the product).
        RowMenu(
            s.name,
            "$tag-${s.name}",
            { beforeEditor(); actions.onEditPick(s) },
            if (s is Suggestion.Own) {
                { actions.onDeleteProduct(s.product) }
            } else {
                null
            },
        )
    }
}

/** The permission or availability text with its one button, in place of the rows. */
@Composable
private fun SearchNotice(text: String, button: String, onClick: () -> Unit, tag: String) {
    Column(Modifier.fillMaxWidth().padding(16.dp).testTag("sheetNotice"), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        TextButton(onClick = onClick, modifier = Modifier.heightIn(min = 48.dp).testTag(tag)) { Text(button) }
    }
}
