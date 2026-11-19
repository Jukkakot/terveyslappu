package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.chart.QuantityTone
import fi.jukkakot.terveyslappu.chart.ChartData
import fi.jukkakot.terveyslappu.chart.DayGroups
import fi.jukkakot.terveyslappu.chart.DayNode
import fi.jukkakot.terveyslappu.chart.GroupSummaries
import fi.jukkakot.terveyslappu.chart.ListGroups
import fi.jukkakot.terveyslappu.chart.RangePick
import fi.jukkakot.terveyslappu.chart.ListNode
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import fi.jukkakot.terveyslappu.chart.PeriodStats
import fi.jukkakot.terveyslappu.chart.filter
import fi.jukkakot.terveyslappu.chart.needsHistory
import fi.jukkakot.terveyslappu.chart.periodStats
import fi.jukkakot.terveyslappu.weight.HistoryAccess
import fi.jukkakot.terveyslappu.weight.WeightEntry
import java.time.Instant
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TextButton
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import fi.jukkakot.terveyslappu.chart.Period
import fi.jukkakot.terveyslappu.derived.Availability
import fi.jukkakot.terveyslappu.derived.Derived
import fi.jukkakot.terveyslappu.derived.Quantity
import fi.jukkakot.terveyslappu.food.EnergySeries
import fi.jukkakot.terveyslappu.profile.OwnerProfile
import fi.jukkakot.terveyslappu.burn.BurnSeries
import fi.jukkakot.terveyslappu.burn.BurnUiState
import fi.jukkakot.terveyslappu.steps.StepsAccess
import fi.jukkakot.terveyslappu.steps.StepsSeries
import fi.jukkakot.terveyslappu.steps.StepsUiState
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.style.TextOverflow
import fi.jukkakot.terveyslappu.chart.CustomCharts
import fi.jukkakot.terveyslappu.chart.HistoryPage
import fi.jukkakot.terveyslappu.chart.HistoryPages
import fi.jukkakot.terveyslappu.chart.SeriesKind
import fi.jukkakot.terveyslappu.log.AppLog
import java.time.LocalDate
import java.time.ZoneId

/** Historia: period chips, chart, stats tiles and the period's weights; the top bar holds the App menu. */
@Composable
internal fun HistoryScreen(
    state: MainUiState,
    actions: MainActions,
    snackbar: SnackbarHostState,
    profile: OwnerProfile = OwnerProfile(),
    food: FoodUiState = FoodUiState(),
    foodActions: FoodActions = FoodActions(),
    steps: StepsUiState = StepsUiState(),
    stepsActions: StepsActions = StepsActions(),
    burn: BurnUiState = BurnUiState(),
    burnActions: BurnActions = BurnActions(),
) {
    Scaffold(
        topBar = {
            SubScreenBar(stringResource(R.string.history_title), onBack = actions.onBack) {
                AppMenuActions(actions, foodActions, "history")
            }
        },
        snackbarHost = { AppSnackbarHost(snackbar) },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            val all = state.weights
            when {
                state.loadFailed && all == null -> Notice(stringResource(R.string.recent_failed))
                all == null -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                else -> PeriodList(state, all, actions, profile, food, foodActions, steps, stepsActions, burn, burnActions)
            }
        }
    }
}

/** Chips, chart and stats scroll away above the period's weights, so the list keeps room on a small phone. */
@Composable
private fun PeriodList(
    state: MainUiState,
    all: List<WeightEntry>,
    actions: MainActions,
    profile: OwnerProfile,
    food: FoodUiState,
    foodActions: FoodActions,
    steps: StepsUiState,
    stepsActions: StepsActions,
    burn: BurnUiState,
    burnActions: BurnActions,
) {
    val zone = ZoneId.systemDefault()
    val now = remember(state.loadedAt, state.period) { state.loadedAt ?: Instant.now() }
    val today = LocalDate.now(zone)
    // "?" weights stay out of the chart, the stats and the calendar, but are listed whatever the period (weight-entry).
    val accepted = remember(all, state.questioned) { all.filter { it.id !in state.questioned } }
    val shown = remember(accepted, state.period) { state.period.filter(accepted, now, zone) }
    val listed = remember(shown, all, state.questioned) { shown + all.filter { it.id in state.questioned } }
    val tree = remember(listed, state.questioned) { ListGroups.build(listed, zone, questioned = state.questioned) }
    val dayWeights = remember(accepted) { RangePick.dayWeights(accepted, zone) }
    // Every group starts closed; reset with the period, forgotten when Historia is left (weight-entry "Open by default").
    var open by remember(state.period) { mutableStateOf(emptySet<String>()) }
    val rows = remember(tree, open) { ListGroups.flatten(tree, open) }
    var checking by remember { mutableStateOf<WeightEntry?>(null) }
    val listState = rememberLazyListState()
    val headerItems = if (state.history == HistoryAccess.NOT_GRANTED && state.period.needsHistory(now, zone)) 4 else 3
    // The home notice opens Historia at the newest "?" weight: open its groups, then scroll to its row.
    LaunchedEffect(state.historyTarget, tree) {
        val target = state.historyTarget ?: return@LaunchedEffect
        val path = ListGroups.pathTo(tree, target)
        if (path == null) actions.onTargetShown() else open = open + path
    }
    LaunchedEffect(state.historyTarget, rows) {
        val target = state.historyTarget ?: return@LaunchedEffect
        val index = rows.indexOfFirst { (it.node as? ListNode.Weight)?.entry?.id == target }
        if (index >= 0) {
            listState.scrollToItem(headerItems + index)
            actions.onTargetShown()
        }
    }
    checking?.let { entry ->
        CheckDialog(
            entry,
            today,
            onAccept = {
                checking = null
                actions.onAccept(entry)
            },
            onDelete = {
                checking = null
                actions.onDelete(entry)
            },
            onDismiss = { checking = null },
        )
    }
    // Not saved: leaving Historia and coming back starts on Paino (weight-chart "One quantity at a time").
    // Paino, BMI, Syöty (food-log), Askeleet (steps), then Kulutus (energy-burn); back from Ruoka opened from Syöty returns to it.
    // Fixed tabs, the own charts, then Uusi kaavio (custom-charts design 11).
    val pages = remember(state.charts) { HistoryPages.of(state.charts) }
    val pageCount by rememberUpdatedState(pages.size)
    val initialPage = when {
        state.historyStartPage != null -> state.historyStartPage.coerceIn(0, pages.size - 1)
        state.historyChart != null -> pages.indexOfChart(state.historyChart).coerceAtLeast(0)
        state.historyOnSteps -> STEPS_PAGE
        state.historyOnBurn -> BURN_PAGE
        state.historyOnEnergy -> ENERGY_PAGE
        else -> 0
    }
    val pager = rememberPagerState(initialPage = initialPage) { pageCount }
    LaunchedEffect(Unit) {
        if (state.historyStartPage != null) actions.onStartPageShown()
        if (state.historyOnEnergy) actions.onEnergyShown()
        if (state.historyOnSteps) actions.onStepsShown()
        if (state.historyOnBurn) actions.onBurnShown()
        if (state.historyChart != null) actions.onChartShown()
    }
    // The add search opened from here returns to this tab through Ruoka.
    LaunchedEffect(pager.currentPage) { actions.onHistoryPage(pager.currentPage) }
    val page = pages[pager.currentPage]
    val onEnergy = page == HistoryPage.Energy
    val onSteps = page == HistoryPage.Steps
    val onBurn = page == HistoryPage.Burn
    val chart = (page as? HistoryPage.Custom)?.let { p -> state.charts.firstOrNull { it.id == p.id } }
    // The period the chips show: the chart's own on its tab, otherwise the shared one.
    val chipPeriod = chart?.period ?: state.period
    val onChipPeriod: (Period) -> Unit = if (chart != null) { p -> actions.onChartPeriod(chart.id, p) } else actions.onPeriod
    // Entering Askeleet (or changing its period) reads the shown range again (steps "Refresh and failed reads").
    LaunchedEffect(onSteps, state.period) {
        if (onSteps) stepsActions.onShown(if (state.period == Period.All) null else StepsSeries.dates(state.period, today, null).first)
    }
    LaunchedEffect(onBurn, state.period) {
        if (onBurn) burnActions.onShown(if (state.period == Period.All) null else BurnSeries.dates(state.period, today, null).first)
    }
    // An own chart reads what it shows like the fixed tabs, and never asks a permission (design 15).
    LaunchedEffect(chart?.id, chart?.period) {
        val shown = chart ?: return@LaunchedEffect
        AppLog.i("chart.custom", "shown", "id" to shown.id, "period" to CustomCharts.logPeriod(shown.period))
        if (shown.has(SeriesKind.STEPS)) stepsActions.onShown(if (shown.period == Period.All) null else StepsSeries.dates(shown.period, today, null).first)
        if (shown.has(SeriesKind.BURN)) burnActions.onShown(if (shown.period == Period.All) null else BurnSeries.dates(shown.period, today, null).first)
    }
    // The crosshair of the shown chart page (every Historia chart): cleared on a tab or period change and when its day leaves the axis (design 10).
    var crosshair by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    LaunchedEffect(pager.currentPage, chipPeriod) { crosshair = null }
    // The editor: null closed, 0 a new chart, else the chart's id.
    var editor by rememberSaveable(saver = EditorTargetSaver) { mutableStateOf(null) }
    var goTo by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(goTo, pages) {
        val target = goTo ?: return@LaunchedEffect
        if (target < pages.size) {
            pager.animateScrollToPage(target)
            goTo = null
        }
    }
    editor?.let { target ->
        val existing = state.charts.firstOrNull { it.id == target }
        ChartEditor(
            existing = existing,
            charts = state.charts,
            onSave = { saved ->
                editor = null
                crosshair = null
                val id = actions.onSaveChart(saved)
                if (existing == null) goTo = HistoryPages.of(state.charts.filter { it.id != id } + saved.copy(id = id)).indexOfChart(id)
            },
            onDelete = { id ->
                editor = null
                crosshair = null
                goTo = HistoryPages.afterDelete(state.charts, id)
                actions.onDeleteChart(id)
            },
            onDismiss = { editor = null },
        )
    }
    val stepsSeries = remember(steps.days, state.period, today) { StepsSeries.of(steps.days.orEmpty(), state.period, today) }
    val burnSeries = remember(burn.days, state.period, today) { BurnSeries.of(burn.days.orEmpty(), state.period, today) }
    // The nutrition permission is asked when Energia is first opened (review decision).
    LaunchedEffect(onEnergy) { if (onEnergy && food.access == FoodAccess.NEEDS) foodActions.onGrant() }
    val series = remember(food.items, state.period, today) { EnergySeries.of(food.items.orEmpty(), state.period, today) }
    // Day lists group like the weight list: all closed on a period change and a tab switch, each list its own open set.
    var openEnergy by remember(state.period) { mutableStateOf(emptySet<String>()) }
    var openSteps by remember(state.period) { mutableStateOf(emptySet<String>()) }
    var openBurn by remember(state.period) { mutableStateOf(emptySet<String>()) }
    var lastPage by remember { mutableStateOf(pager.currentPage) }
    LaunchedEffect(pager.currentPage) {
        if (lastPage != pager.currentPage) {
            lastPage = pager.currentPage
            open = emptySet()
            openEnergy = emptySet()
            openSteps = emptySet()
            openBurn = emptySet()
        }
    }
    val energyRows = remember(series, openEnergy) { DayGroups.flatten(DayGroups.build(series.days) { it.day }, openEnergy) }
    val stepsRows = remember(stepsSeries, openSteps) { DayGroups.flatten(DayGroups.build(stepsSeries.days) { it.day }, openSteps) }
    val burnRows = remember(burnSeries, openBurn) { DayGroups.flatten(DayGroups.build(burnSeries.days) { it.day }, openBurn) }
    val scope = rememberCoroutineScope()
    LazyColumn(Modifier.fillMaxSize().testTag("weights"), state = listState) {
        item(key = "tabs") {
            // The topmost row (weight-chart "One quantity at a time"): compact tabs so the five fixed
            // ones fit 360 dp; own charts by scrolling.
            PrimaryScrollableTabRow(
                selectedTabIndex = pager.currentPage,
                edgePadding = 8.dp,
                containerColor = Color.Transparent,
                minTabWidth = 0.dp,
            ) {
                Quantity.entries.forEachIndexed { i, quantity ->
                    CompactTab(
                        selected = pager.currentPage == i,
                        onClick = { scope.launch { pager.animateScrollToPage(i) } },
                        text = { Text(stringResource(quantity.title()), maxLines = 1) },
                        modifier = Modifier.testTag("tab-${quantity.name}"),
                    )
                }
                CompactTab(
                    selected = onEnergy,
                    onClick = { scope.launch { pager.animateScrollToPage(ENERGY_PAGE) } },
                    text = { Text(stringResource(R.string.quantity_energy), maxLines = 1) },
                    modifier = Modifier.testTag("tab-ENERGY"),
                )
                CompactTab(
                    selected = onSteps,
                    onClick = { scope.launch { pager.animateScrollToPage(STEPS_PAGE) } },
                    text = { Text(stringResource(R.string.quantity_steps), maxLines = 1) },
                    modifier = Modifier.testTag("tab-STEPS"),
                )
                CompactTab(
                    selected = onBurn,
                    onClick = { scope.launch { pager.animateScrollToPage(BURN_PAGE) } },
                    text = { Text(stringResource(R.string.quantity_burn), maxLines = 1) },
                    modifier = Modifier.testTag("tab-BURN"),
                )
                state.charts.forEach { c ->
                    val index = pages.indexOfChart(c.id)
                    CompactTab(
                        selected = pager.currentPage == index,
                        onClick = { scope.launch { pager.animateScrollToPage(index) } },
                        text = { Text(c.name, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 160.dp)) },
                        modifier = Modifier.testTag("tab-chart-${c.id}"),
                    )
                }
                CompactTab(
                    selected = page == HistoryPage.NewChart,
                    onClick = { scope.launch { pager.animateScrollToPage(pages.size - 1) } },
                    text = { Text(stringResource(R.string.chart_new), maxLines = 1) },
                    modifier = Modifier.testTag("tab-NEW"),
                )
            }
        }
        item(key = "chips") {
            PeriodChips(
                chipPeriod,
                onChipPeriod,
                dayWeights = dayWeights,
                historyMissing = state.history == HistoryAccess.NOT_GRANTED,
                onGrantHistory = actions.onGrantHistory,
            )
        }
        if (state.history == HistoryAccess.NOT_GRANTED && chipPeriod.needsHistory(now, zone)) {
            item(key = "history") { HistoryNotice(actions.onGrantHistory) }
        }
        item(key = "pager") {
            androidx.compose.runtime.CompositionLocalProvider(LocalChartReset provides pager.currentPage) {
            HorizontalPager(pager, verticalAlignment = Alignment.Top) { index ->
                val shownPage = pages[index]
                val here = crosshair.takeIf { index == pager.currentPage }
                val onDay: (LocalDate?) -> Unit = { crosshair = it }
                if (shownPage is HistoryPage.Custom) {
                    val c = state.charts.firstOrNull { it.id == shownPage.id }
                    if (c != null) {
                        val data = rememberChartData(c, state, food, steps, today, zone, burn)
                        val current = index == pager.currentPage
                        LaunchedEffect(data.axis, current) { if (current && crosshair?.let { it !in data.axis } == true) crosshair = null }
                        ChartPage(
                            data,
                            chartStates(food, foodActions, steps, stepsActions, burn, burnActions),
                            access = { chartAccess(it, food, steps, burn) },
                            crosshair = crosshair.takeIf { current },
                            onDay = { crosshair = it },
                            onEdit = { editor = c.id },
                        )
                    }
                } else if (shownPage == HistoryPage.NewChart) {
                    NewChartPage { editor = 0 }
                } else if (index == STEPS_PAGE) {
                    StepsTab(steps, stepsSeries, stepsActions, here, onDay)
                } else if (index == BURN_PAGE) {
                    BurnTab(burn, burnSeries, burnActions, here, onDay)
                } else if (index == ENERGY_PAGE) {
                    when {
                        food.access != FoodAccess.GRANTED -> EnergyReason(foodActions.onGrant)
                        food.items == null -> Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        else -> EnergyPage(series, here, onDay)
                    }
                } else {
                    QuantityPage(Quantity.entries[index], state.period, shown, now, zone, profile, actions.onOpenProfile, here, onDay)
                }
            }
            }
        }
        if (onBurn) {
            val shownRows = if (burn.access == StepsAccess.GRANTED && burn.days != null) burnRows else emptyList()
            items(shownRows, key = { "burn-${it.node.key}" }) { row ->
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                when (val node = row.node) {
                    is DayNode.Row -> BurnDayRow(node.item, today, row.depth)
                    is DayNode.Group -> {
                        val s = GroupSummaries.burn(node.members, today)
                        DayGroupRowItem(
                            node,
                            stringResource(R.string.group_avg_kcal, formatKcal(s.main.toDouble())),
                            s.extra?.let { stringResource(R.string.group_avg_active, formatKcal(it.toDouble())) },
                            s.days, node.key in openBurn, today, row.depth,
                        ) { openBurn = if (node.key in openBurn) openBurn - node.key else openBurn + node.key }
                    }
                }
            }
        } else if (onSteps) {
            val shownRows = if (steps.access == StepsAccess.GRANTED && steps.days != null) stepsRows else emptyList()
            items(shownRows, key = { "steps-${it.node.key}" }) { row ->
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                when (val node = row.node) {
                    is DayNode.Row -> StepsDayRow(node.item, today, row.depth)
                    is DayNode.Group -> {
                        val s = GroupSummaries.steps(node.members, today)
                        DayGroupRowItem(
                            node,
                            stringResource(R.string.group_avg_steps, formatSteps(s.main)),
                            null,
                            s.days, node.key in openSteps, today, row.depth,
                        ) { openSteps = if (node.key in openSteps) openSteps - node.key else openSteps + node.key }
                    }
                }
            }
        } else if (onEnergy) {
            items(if (food.access == FoodAccess.GRANTED) energyRows else emptyList(), key = { "energy-${it.node.key}" }) { row ->
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                when (val node = row.node) {
                    is DayNode.Row -> EnergyDayRow(node.item, today, row.depth) { foodActions.onOpenDay(it) }
                    is DayNode.Group -> {
                        val s = GroupSummaries.energy(node.members, today)
                        DayGroupRowItem(
                            node,
                            stringResource(R.string.group_avg_kcal, formatKcal(s.main.toDouble())),
                            s.extra?.let { stringResource(R.string.group_avg_treats, formatKcal(it.toDouble())) },
                            s.days, node.key in openEnergy, today, row.depth, extraColor = treatTextColour(),
                        ) { openEnergy = if (node.key in openEnergy) openEnergy - node.key else openEnergy + node.key }
                    }
                }
            }
        } else if (page is HistoryPage.Fixed) items(rows, key = { it.node.key }) { row ->
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            when (val node = row.node) {
                is ListNode.Weight -> if (node.questioned) {
                    QuestionedRowItem(node.entry, today, row.depth, onCheck = { checking = node.entry }, onAccept = { actions.onAccept(node.entry) }, onDelete = { actions.onDelete(node.entry) })
                } else {
                    WeightRowItem(node.entry, node.change, today, row.depth, onDelete = { actions.onDelete(node.entry) })
                }
                is ListNode.Group -> GroupRowItem(node, node.key in open, today, row.depth) {
                    open = if (node.key in open) open - node.key else open + node.key
                }
                is ListNode.Gap -> GapRowItem(node, today, row.depth)
            }
        }
    }
}

private val ENERGY_PAGE = HistoryPages.ENERGY
private val STEPS_PAGE = HistoryPages.STEPS
private val BURN_PAGE = HistoryPages.BURN

/** Energia without the nutrition permission: the reason and a button to grant it (weight-chart). */
@Composable
private fun EnergyReason(onGrant: () -> Unit) {
    Box(
        Modifier
            .padding(start = 16.dp, end = 16.dp, top = 12.dp)
            .clip(groupShape)
            .background(cardColour())
            .fillMaxWidth()
            .testTag("energyReason"),
    ) {
        FoodPermissionNotice(stringResource(R.string.energy_reason), onGrant)
    }
}

/**
 * One quantity's chart and stats; a quantity the owner details do not allow shows the reason and a
 * button to Omat tiedot instead (weight-chart "One quantity at a time").
 */
@Composable
private fun QuantityPage(
    quantity: Quantity,
    period: Period,
    shown: List<WeightEntry>,
    now: Instant,
    zone: ZoneId,
    profile: OwnerProfile,
    onOpenProfile: () -> Unit,
    crosshair: LocalDate? = null,
    onDay: (LocalDate?) -> Unit = {},
) {
    val value: (WeightEntry) -> Double? = remember(quantity, profile) { { quantity.valueOf(it, profile) } }
    val data = remember(shown, quantity, profile) { ChartData.of(period, shown, now, zone, value) { quantity.scale(it, profile) } }
    val stats = remember(shown, quantity, profile) { periodStats(shown, value, rate = quantity.hasRate) }
    val state = quantity.availability(profile)
    Column(Modifier.fillMaxWidth().testTag("page-${quantity.name}")) {
        Box(
            Modifier
                .padding(start = 16.dp, end = 16.dp, top = 12.dp)
                .clip(groupShape)
                .background(cardColour())
                .padding(8.dp),
        ) {
            if (state != Availability.Ok) {
                Column(
                    Modifier.fillMaxWidth().height(180.dp).padding(16.dp).testTag("quantityReason"),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(reasonText(quantity.title(), state), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                    TextButton(onClick = onOpenProfile, modifier = Modifier.testTag("openProfile")) { Text(stringResource(R.string.derived_open_profile)) }
                }
            } else if (quantity == Quantity.WEIGHT || data == null) {
                ChartArea(data, crosshair, onDay)
            } else {
                WeightChart(
                    data,
                    stringResource(R.string.chart_description_bmi, data.entries.size),
                    bmi = true,
                    crosshair = crosshair,
                    onDay = onDay,
                    zone = zone,
                )
            }
        }
        if (state == Availability.Ok) stats?.let { StatsTiles(it, quantity, profile, shown.maxOfOrNull { e -> e.time }) }
    }
}

/** A Historia tab with 12 dp sides (weight-chart "One quantity at a time": five fit 360 dp). */
@Composable
private fun CompactTab(selected: Boolean, onClick: () -> Unit, text: @Composable () -> Unit, modifier: Modifier = Modifier) {
    Tab(
        selected = selected,
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        selectedContentColor = MaterialTheme.colorScheme.primary,
        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Box(Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
            androidx.compose.material3.ProvideTextStyle(MaterialTheme.typography.titleSmall) { text() }
        }
    }
}

/**
 * Paino and BMI statistics (weight-chart "Statistics layout"): Paino's hero Muutos with Viikossa at
 * its right, BMI's hero Viimeisin with its class word in its tone and Muutos at its right; the row
 * Keskiarvo, Alin, Ylin, Painoja. A value that cannot be shown reads "—".
 */
@Composable
private fun StatsTiles(stats: PeriodStats, quantity: Quantity = Quantity.WEIGHT, profile: OwnerProfile = OwnerProfile(), latestTime: Instant? = null) {
    val none = StatsLayout.NONE
    val unit = if (quantity == Quantity.WEIGHT) "kg" else null
    fun number(v: Double) = formatKg(v)
    val change = Stat(
        stringResource(R.string.stats_change),
        stats.change?.let { formatChange(it) } ?: none,
        unit.takeIf { stats.change != null },
        stats.change?.let { shownDirection(it, 1) } ?: 0,
    )
    val values = buildMap<StatsLayout.Key, Stat> {
        put(StatsLayout.Key.CHANGE, change)
        put(
            StatsLayout.Key.RATE,
            Stat(stringResource(R.string.stats_rate), stats.perWeek?.let { formatRate(it) } ?: none, stringResource(R.string.unit_kg_per_week).takeIf { stats.perWeek != null }, stats.perWeek?.let { shownDirection(it, 1) } ?: 0),
        )
        val minor = latestTime != null && Derived.minor(profile, latestTime.atZone(ZoneId.systemDefault()).year)
        val latest = if (quantity == Quantity.BMI && !minor) {
            Stat(stringResource(R.string.stats_latest), number(stats.latest), stringResource(Derived.bmiClass(stats.latest).word()), unitColor = toneColor(Derived.tone(stats.latest)))
        } else {
            Stat(stringResource(R.string.stats_latest), number(stats.latest), unit)
        }
        put(StatsLayout.Key.LATEST, latest)
        put(StatsLayout.Key.AVERAGE, Stat(stringResource(R.string.stats_average), quantity.format(stats.average)))
        put(StatsLayout.Key.MIN, Stat(stringResource(R.string.stats_min), quantity.format(stats.min)))
        put(StatsLayout.Key.MAX, Stat(stringResource(R.string.stats_max), quantity.format(stats.max)))
        put(StatsLayout.Key.COUNT, Stat(stringResource(R.string.stats_count), stats.count.toString()))
    }
    val layout = StatsLayout.of(if (quantity == Quantity.WEIGHT) StatsLayout.Tab.WEIGHT else StatsLayout.Tab.BMI)
    TabStats(values.getValue(layout.hero), QuantityTone.WEIGHT, layout.right?.let(values::getValue), layout.row.map(values::getValue), "stats")
}

/**
 * A "?" weight (weight-entry "Questioned row look"): an amber-tinted outlined card with the "?"
 * sign and "tarkista", no change; a tap opens Hyväksy / Poista / Peruuta.
 */
@Composable
private fun QuestionedRowItem(entry: WeightEntry, today: LocalDate, depth: Int, onCheck: () -> Unit, onAccept: () -> Unit, onDelete: () -> Unit) {
    val check = checkColors()
    val shape = RoundedCornerShape(12.dp)
    val description = stringResource(R.string.check_row_description, formatKg(entry.kg))
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = indent(depth) - 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
            .clip(shape)
            .background(check.container)
            .border(1.dp, check.outline, shape)
            .clickable(onClickLabel = stringResource(R.string.check_title), onClick = onCheck)
            .heightIn(min = 56.dp)
            .padding(start = 8.dp)
            .testTag("questioned-${entry.id}"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(formatRowTime(entry.time, today), style = MaterialTheme.typography.bodyLarge, color = check.content)
            Text(stringResource(R.string.check_text), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = check.content)
        }
        Text(
            stringResource(R.string.kg_value, formatKg(entry.kg)) + " " + stringResource(R.string.check_mark),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = check.content,
            modifier = Modifier.semantics { contentDescription = description },
        )
        // ⋮: Hyväksy, then Poista (app-appearance "List rows"); a tap on the row still opens the dialog.
        RowMenu(
            stringResource(R.string.kg_value, formatKg(entry.kg)),
            "weight-${entry.id}",
            onEdit = null,
            onDelete = onDelete,
            extra = listOf(RowMenuItem(stringResource(R.string.check_accept), R.drawable.ic_check, "weight-${entry.id}-accept", onAccept)),
        )
    }
}

/** Hyväksy / Poista / Peruuta for a "?" weight; Hyväksy takes the chained acceptance with it. */
@Composable
private fun CheckDialog(entry: WeightEntry, today: LocalDate, onAccept: () -> Unit, onDelete: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.check_title)) },
        text = { Text(stringResource(R.string.check_body, formatKg(entry.kg), formatDayTime(entry.time, today))) },
        confirmButton = { TextButton(onClick = onAccept, modifier = Modifier.testTag("checkAccept")) { Text(stringResource(R.string.check_accept)) } },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete, modifier = Modifier.testTag("checkDelete")) { Text(stringResource(R.string.check_delete), color = MaterialTheme.colorScheme.error) }
                TextButton(onClick = onDismiss, modifier = Modifier.testTag("checkCancel")) { Text(stringResource(R.string.cancel)) }
            }
        },
    )
}

@Composable
private fun WeightRowItem(entry: WeightEntry, change: Double?, today: LocalDate, depth: Int, onDelete: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(start = indent(depth), end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(formatRowTime(entry.time, today), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        change?.let {
            val direction = shownDirection(it, 1)
            ChangeLabel(
                stringResource(R.string.kg_value, formatChange(it)),
                direction,
                Modifier.padding(end = 12.dp).testTag("rowChange-${entry.id}"),
                style = MaterialTheme.typography.bodyMedium,
                color = changeColor(direction),
            )
        }
        Text(stringResource(R.string.kg_value, formatKg(entry.kg)), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        // ⋮ with a red Poista only (a weight has no editor).
        RowMenu(stringResource(R.string.kg_value, formatKg(entry.kg)), "weight-${entry.id}", onEdit = null, onDelete = onDelete)
    }
}
