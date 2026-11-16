package fi.jukkakot.terveyslappu.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.chart.CustomChart
import fi.jukkakot.terveyslappu.backup.BackupStatus
import fi.jukkakot.terveyslappu.home.CardSize
import fi.jukkakot.terveyslappu.home.HomeCard
import fi.jukkakot.terveyslappu.home.HomeEditSignal
import fi.jukkakot.terveyslappu.home.HomeLayout
import fi.jukkakot.terveyslappu.home.HomeLayoutCodec
import fi.jukkakot.terveyslappu.log.AppLog
import fi.jukkakot.terveyslappu.steps.StepsUiState
import sh.calvin.reorderable.DragGestureDetector
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import java.time.Instant
import java.time.LocalDate

/**
 * The home screen (home-screen, home-card-layout): the feature cards in the user's grid, and the
 * edit mode that changes it. Edit mode works on a draft kept here (design 4) so the edit bar and the
 * grid share it; it survives rotation (saved with the layout codec), Valmis saves it, Peruuta, back
 * and any outside intent drop it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    state: MainUiState,
    actions: MainActions,
    snackbar: SnackbarHostState,
    backup: BackupStatus,
    food: FoodUiState = FoodUiState(),
    foodActions: FoodActions = FoodActions(),
    steps: StepsUiState = StepsUiState(),
    stepsActions: StepsActions = StepsActions(),
    burn: fi.jukkakot.terveyslappu.burn.BurnUiState = fi.jukkakot.terveyslappu.burn.BurnUiState(),
    burnActions: BurnActions = BurnActions(),
) {
    // Chart cards are kept in the saved draft only while their chart exists (custom-charts design 2).
    val chartIds = remember(state.charts) { state.charts.map { it.cardId }.toSet() }
    val codec = remember(chartIds) { HomeLayoutCodec(allowedExtra = chartIds) }
    val draftState = rememberSaveable(saver = draftSaver(codec)) { mutableStateOf(null) }
    val current by rememberUpdatedState(state)
    val edit = remember(draftState) {
        HomeEditor(
            draftState,
            saved = { current.homeLayout },
            save = { actions.onHomeLayout(it) },
        )
    }
    edit.save = actions.onHomeLayout
    val editing = draftState.value != null
    BackHandler(enabled = editing) { edit.cancel() }
    // Any outside intent ends edit mode first and drops the draft (design 4).
    val outside = state.access != Access.GRANTED || state.screen != Screen.HOME || state.entryOpen || state.import != null ||
        food.sheetDay != null || food.editing != null || food.ai != null
    LaunchedEffect(outside) { if (outside) edit.cancel() }
    LaunchedEffect(edit) { HomeEditSignal.outside.collect { edit.cancel() } }
    Scaffold(
        topBar = {
            if (editing) {
                TopAppBar(
                    title = { Text(stringResource(R.string.home_edit_title), modifier = Modifier.testTag("editTitle")) },
                    navigationIcon = {
                        IconButton(onClick = edit::cancel, modifier = Modifier.testTag("editCancel")) {
                            Icon(painterResource(R.drawable.ic_close), contentDescription = stringResource(R.string.home_edit_cancel))
                        }
                    },
                    actions = {
                        TextButton(onClick = edit::done, modifier = Modifier.testTag("editDone")) { Text(stringResource(R.string.home_edit_done)) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                )
            } else {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    actions = {
                        AppMenuActions(actions, foodActions, "main")
                    },
                )
            }
        },
        snackbarHost = { AppSnackbarHost(snackbar) },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (state.access) {
                Access.CHECKING -> Notice(stringResource(R.string.hc_checking))
                Access.UNAVAILABLE -> Notice(stringResource(R.string.hc_unavailable), stringResource(R.string.hc_open_store), actions.onOpenStore)
                Access.NEEDS_UPDATE -> Notice(stringResource(R.string.hc_needs_update), stringResource(R.string.hc_open_store), actions.onOpenStore)
                Access.NEEDS_PERMISSION -> Notice(stringResource(R.string.hc_permission_intro), stringResource(R.string.hc_permission_grant), actions.onRequestPermission)
                Access.DENIED -> Notice(stringResource(R.string.hc_permission_denied), stringResource(R.string.hc_open_settings), actions.onOpenSettings)
                Access.GRANTED -> HomeGrid(HomeCardsData(state, actions, food, foodActions, steps, stepsActions, burn, burnActions), backup, edit)
            }
        }
    }
}

/** The draft as one codec string in the saved state, so rotation and process death keep it. */
private fun draftSaver(codec: HomeLayoutCodec) = Saver<MutableState<HomeLayout?>, String>(
    save = { it.value?.let(codec::encode) },
    restore = { mutableStateOf(codec.decode(it)) },
)

/** Edit mode's draft and its three exits (design 4, 14). Reads the saved layout through [saved] so stale closures see the current one. */
internal class HomeEditor(
    private val draftState: MutableState<HomeLayout?>,
    private val saved: () -> HomeLayout,
    var save: (HomeLayout) -> Unit,
) {
    val draft: HomeLayout? get() = draftState.value

    fun start() {
        if (draftState.value != null) return
        AppLog.i("home.edit", "open")
        draftState.value = saved()
    }

    fun update(layout: HomeLayout) {
        if (draftState.value != null) draftState.value = layout
    }

    /** Peruuta, back, an outside intent: leave and drop the draft, without asking. */
    fun cancel() {
        val draft = draftState.value ?: return
        AppLog.i("home.edit", "cancel", "changed" to (draft != saved()))
        draftState.value = null
    }

    /** Valmis: save when changed (no layout write or log otherwise), then leave. */
    fun done() {
        val draft = draftState.value ?: return
        if (draft != saved()) save(draft)
        draftState.value = null
    }
}

/** The notices on top (outside edit mode), the cards in a two-column grid, and the edit tools under them while editing. */
@Composable
private fun HomeGrid(data: HomeCardsData, backup: BackupStatus, edit: HomeEditor) {
    val state = data.state
    val draft = edit.draft
    val editing = draft != null
    val layout = draft ?: state.homeLayout
    val gridState = rememberLazyGridState()
    val reorder = rememberReorderableLazyGridState(gridState) { from, to ->
        edit.draft?.let { edit.update(it.moveById(from.key as String, to.key as String)) }
    }
    val unit = rowUnit()
    val context = LocalContext.current
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current
    // The slot's gesture lives across the switch into edit mode, so it must call through these (design 5).
    val announce = remember(edit) {
        { id: String ->
            edit.draft?.let { d -> placeText(context, d, id, data.state.charts)?.let { view.announceForAccessibility(it) } }
            Unit
        }
    }
    val onLongPress = remember(edit) {
        {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            edit.start()
        }
    }
    val full: (androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope.() -> GridItemSpan) = { GridItemSpan(maxLineSpan) }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = Modifier.fillMaxSize().testTag("homeGrid"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(GRID_GAP),
        horizontalArrangement = Arrangement.spacedBy(GRID_GAP),
    ) {
        // Notices stay above the grid and out of the order; hidden while editing (design 11).
        if (!editing) {
            if (backup.needsAttention(Instant.now())) item(key = "backupNotice", span = full) { BackupNotice(backup.failed, data.actions.onOpenBackupSettings) }
            if (state.newestQuestioned != null) item(key = "checkNotice", span = full) { CheckNotice(data.actions.onOpenQuestioned) }
            data.food.clipOffer?.let { offer -> item(key = "clipOffer", span = full) { ClipOfferCard(offer, LocalDate.now(), data.foodActions) } }
        }
        items(layout.shown, key = { it.id }, span = { GridItemSpan(it.size.span) }) { card ->
            ReorderableItem(reorder, key = card.id) { dragging ->
                CardSlot(card, layout, data, unit, editing, dragging, edit, onLongPress, announce)
            }
        }
        if (draft != null) item(key = "editTools", span = full) { EditTools(draft, edit, data.state.charts, data.actions) }
    }
}

/**
 * One card in the grid. Outside edit mode its long-press starts edit mode and goes on into a drag;
 * in edit mode it is drawn dimmed under an outline with the strip (handle, name, menu) on top, its
 * own taps swallowed (design 5, 6).
 */
@Composable
private fun ReorderableCollectionItemScope.CardSlot(
    card: HomeCard,
    layout: HomeLayout,
    data: HomeCardsData,
    unit: Dp,
    editing: Boolean,
    dragging: Boolean,
    edit: HomeEditor,
    onLongPress: () -> Unit,
    announce: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val name = cardLabel(card.id, data.state.charts)
    val index = layout.indexOf(card.id)
    val description = stringResource(R.string.home_edit_card, name, index + 1, layout.shown.size, stringResource(sizeName(card.size)))
    val editLabel = stringResource(R.string.home_edit_title)
    val editActions = editActions(card, layout, edit, announce)
    Box(
        Modifier
            // Always first and never re-keyed, so the long-press that starts edit mode keeps its drag (design 5).
            .draggableHandle(
                onDragStarted = { onLongPress() },
                onDragStopped = { announce(card.id) },
                dragGestureDetector = LongPressEntry,
            )
            .fillMaxWidth()
            .height(cardHeight(card.size, unit))
            .shadow(if (dragging) 8.dp else 0.dp, homeCardShape)
            .semantics {
                if (editing) {
                    contentDescription = description
                    customActions = editActions
                } else {
                    customActions = listOf(CustomAccessibilityAction(editLabel) { onLongPress(); true })
                }
            }
            .testTag("homeSlot-${card.id}"),
    ) {
        val content = if (editing) Modifier.alpha(if (dragging) 1f else 0.55f).clearAndSetSemantics {} else Modifier
        HomeCardView(card, data, Modifier.fillMaxSize().then(content))
        if (editing) {
            // Swallows only taps: drags and long-presses go on to the card's own drag (design 6).
            Box(
                Modifier
                    .matchParentSize()
                    .pointerInput(Unit) { detectTapGestures { } }
                    .border(2.dp, colors.primary, homeCardShape)
                    .testTag("editOverlay-${card.id}"),
            )
            EditStrip(card, name, layout, edit, announce)
        }
    }
}

/** The strip across an edited card's top: drag handle, the card's name, the menu (design 6). */
@Composable
private fun ReorderableCollectionItemScope.EditStrip(card: HomeCard, name: String, layout: HomeLayout, edit: HomeEditor, announce: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    var menu by remember { mutableStateOf(false) }
    Row(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(colors.surfaceContainerHighest.copy(alpha = 0.92f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(48.dp)
                .draggableHandle(onDragStopped = { announce(card.id) })
                .testTag("editHandle-${card.id}"),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(R.drawable.ic_drag_handle), contentDescription = null, tint = colors.onSurfaceVariant)
        }
        Text(name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        Box {
            IconButton(onClick = { menu = true }, modifier = Modifier.testTag("editMenu-${card.id}")) {
                Icon(painterResource(R.drawable.ic_more_vert), contentDescription = stringResource(R.string.home_edit_menu, name))
            }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                val index = layout.indexOf(card.id)
                fun pick(next: HomeLayout, announceAfter: Boolean = true) {
                    menu = false
                    edit.update(next)
                    if (announceAfter) announce(card.id)
                }
                CardSize.entries.forEach { size ->
                    DropdownMenuItem(
                        text = { Text(stringResource(sizeLabel(size))) },
                        trailingIcon = if (size == card.size) {
                            { Icon(painterResource(R.drawable.ic_check), contentDescription = stringResource(R.string.home_edit_current)) }
                        } else {
                            null
                        },
                        onClick = { pick(layout.resize(card.id, size)) },
                        modifier = Modifier.testTag("editSize-${card.id}-${size.name.lowercase()}"),
                    )
                }
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.home_edit_earlier)) },
                    enabled = index > 0,
                    onClick = { pick(layout.moveEarlier(card.id)) },
                    modifier = Modifier.testTag("editEarlier-${card.id}"),
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.home_edit_later)) },
                    enabled = index < layout.shown.lastIndex,
                    onClick = { pick(layout.moveLater(card.id)) },
                    modifier = Modifier.testTag("editLater-${card.id}"),
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.home_edit_hide)) },
                    enabled = layout.canHide(card.id),
                    onClick = { pick(layout.hide(card.id), announceAfter = false) },
                    modifier = Modifier.testTag("editHide-${card.id}"),
                )
            }
        }
    }
}

/** The menu's actions for TalkBack (design 13): only the applicable ones. */
@Composable
private fun editActions(card: HomeCard, layout: HomeLayout, edit: HomeEditor, announce: (String) -> Unit): List<CustomAccessibilityAction> {
    val index = layout.indexOf(card.id)
    val labels = CardSize.entries.associateWith { stringResource(sizeLabel(it)) }
    val earlier = stringResource(R.string.home_edit_earlier)
    val later = stringResource(R.string.home_edit_later)
    val hide = stringResource(R.string.home_edit_hide)
    fun act(label: String, next: () -> HomeLayout, say: Boolean = true) = CustomAccessibilityAction(label) {
        edit.update(next())
        if (say) announce(card.id)
        true
    }
    return buildList {
        if (index > 0) add(act(earlier, { layout.moveEarlier(card.id) }))
        if (index < layout.shown.lastIndex) add(act(later, { layout.moveLater(card.id) }))
        CardSize.entries.filter { it != card.size }.forEach { size -> add(act(labels.getValue(size), { layout.resize(card.id, size) })) }
        if (layout.canHide(card.id)) add(act(hide, { layout.hide(card.id) }, say = false))
    }
}

/** Under the grid in edit mode: Lisää kortti (the hidden cards) and Palauta oletus (design 6). */
@Composable
private fun EditTools(draft: HomeLayout, edit: HomeEditor, charts: List<CustomChart>, actions: MainActions) {
    var open by remember { mutableStateOf(false) }
    // Uusi kaavio from Lisää kortti: the editor over the front page; the edit draft stays (design 14).
    var newChart by rememberSaveable { mutableStateOf(false) }
    if (newChart) {
        ChartEditor(
            existing = null,
            charts = charts,
            onSave = { chart ->
                newChart = false
                val id = actions.onSaveChart(chart)
                edit.draft?.let { edit.update(it.addChart(CustomChart.cardId(id))) }
            },
            onDelete = {},
            onDismiss = { newChart = false },
        )
    }
    val addable = charts.filter { !draft.contains(it.cardId) }
    Row(
        Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            OutlinedButton(
                onClick = { open = true },
                modifier = Modifier.heightIn(min = 48.dp).testTag("editAdd"),
                contentPadding = PaddingValues(start = 12.dp, end = 16.dp),
            ) {
                Icon(painterResource(R.drawable.ic_add), contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.home_edit_add))
            }
            // The menu scrolls by itself once custom-charts makes it long.
            DropdownMenu(expanded = open, onDismissRequest = { open = false }, modifier = Modifier.heightIn(max = 320.dp)) {
                draft.hidden.forEach { card ->
                    DropdownMenuItem(
                        text = { Text(cardLabel(card.id, charts)) },
                        leadingIcon = { Icon(painterResource(cardIcon(card.id)), contentDescription = null) },
                        onClick = {
                            open = false
                            edit.update(draft.show(card.id))
                        },
                        modifier = Modifier.testTag("editAddItem-${card.id}"),
                    )
                }
                // Charts not on the front page, in tab order, then Uusi kaavio (home-screen "Chart cards").
                addable.forEach { chart ->
                    DropdownMenuItem(
                        text = { Text(chart.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        leadingIcon = { Icon(painterResource(R.drawable.ic_chart), contentDescription = null) },
                        onClick = {
                            open = false
                            edit.update(draft.addChart(chart.cardId))
                        },
                        modifier = Modifier.testTag("editAddItem-${chart.cardId}"),
                    )
                }
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.chart_new)) },
                    leadingIcon = { Icon(painterResource(R.drawable.ic_add), contentDescription = null) },
                    onClick = {
                        open = false
                        newChart = true
                    },
                    modifier = Modifier.testTag("editAddNewChart"),
                )
            }
        }
        TextButton(onClick = { edit.update(draft.reset()) }, enabled = !draft.isDefault, modifier = Modifier.heightIn(min = 48.dp).testTag("editReset")) {
            Text(stringResource(R.string.home_edit_reset))
        }
    }
}

/**
 * The cards' gesture (home-card-layout design 5): watches the first down on the Initial pass without
 * consuming; a press held past the long-press timeout without moving past touch slop starts the drag
 * (and, through `onDragStarted`, edit mode with its haptic tick), then consumes every later change of
 * that gesture, so the card's own clickable, button or period label sees a cancelled gesture and
 * does nothing. A move past slop or a release before the timeout leaves the gesture to the card and
 * the page scroll. Reorderable 3.1.0 keeps its drag start internal, but takes a custom detector, so
 * this one detector is both the edit-mode entry and the card's long-press drag: the press that
 * starts edit mode continues into the drag without lifting the finger.
 */
internal object LongPressEntry : DragGestureDetector {
    override suspend fun PointerInputScope.detect(
        onDragStart: (Offset) -> Unit,
        onDragEnd: () -> Unit,
        onDragCancel: () -> Unit,
        onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
    ) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            val slop = viewConfiguration.touchSlop
            val endedEarly = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                var waiting = true
                while (waiting) {
                    val change = awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull { it.id == down.id }
                    waiting = change != null && change.pressed && (change.position - down.position).getDistance() <= slop
                }
                true
            }
            if (endedEarly != null) return@awaitEachGesture
            onDragStart(down.position)
            while (true) {
                val change = awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull { it.id == down.id }
                if (change == null) {
                    onDragCancel()
                    return@awaitEachGesture
                }
                if (!change.pressed) {
                    change.consume()
                    onDragEnd()
                    return@awaitEachGesture
                }
                val delta = change.positionChange()
                if (delta != Offset.Zero) onDrag(change, delta)
                change.consume()
            }
        }
    }
}

/** A card's name: a feature card's, or an own chart's own name. */
@Composable
private fun cardLabel(id: String, charts: List<CustomChart>): String =
    charts.firstOrNull { it.cardId == id }?.name ?: stringResource(cardName(id))

private fun cardName(id: String): Int = when (id) {
    HomeLayout.WEIGHT -> R.string.home_card_weight
    HomeLayout.FOOD -> R.string.home_card_food
    HomeLayout.BURN -> R.string.home_card_burn
    else -> R.string.home_card_steps
}

private fun cardIcon(id: String): Int = when (id) {
    HomeLayout.WEIGHT -> R.drawable.ic_scale
    HomeLayout.FOOD -> R.drawable.ic_restaurant
    HomeLayout.STEPS -> R.drawable.ic_walk
    HomeLayout.BURN -> R.drawable.ic_burn
    else -> R.drawable.ic_chart
}

/** The menu's size names: Pieni, Leveä, Iso. */
private fun sizeLabel(size: CardSize): Int = when (size) {
    CardSize.SMALL -> R.string.home_edit_small
    CardSize.WIDE -> R.string.home_edit_wide
    CardSize.BIG -> R.string.home_edit_big
}

/** The sizes in TalkBack's description: pieni, leveä, iso. */
private fun sizeName(size: CardSize): Int = when (size) {
    CardSize.SMALL -> R.string.home_edit_small_name
    CardSize.WIDE -> R.string.home_edit_wide_name
    CardSize.BIG -> R.string.home_edit_big_name
}

/** "Ruoka, 1/3": the card's new place, announced after a move or a resize (design 13). */
private fun placeText(context: android.content.Context, layout: HomeLayout, id: String, charts: List<CustomChart>): String? {
    val index = layout.indexOf(id).takeIf { it >= 0 } ?: return null
    val name = charts.firstOrNull { it.cardId == id }?.name ?: context.getString(cardName(id))
    return context.getString(R.string.home_edit_place, name, index + 1, layout.shown.size)
}
