package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.SnackbarHost
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
import fi.jukkakot.terveyslappu.chart.ChartData
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
import fi.jukkakot.terveyslappu.profile.OwnerProfile
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

/** Historia: period chips, chart, stats tiles and the period's weights; + opens manual entry. */
@Composable
internal fun HistoryScreen(state: MainUiState, actions: MainActions, snackbar: SnackbarHostState, profile: OwnerProfile = OwnerProfile()) {
    Scaffold(
        topBar = {
            SubScreenBar(stringResource(R.string.history_title), onBack = actions.onBack) {
                IconButton(onClick = actions.onOpenEntry, modifier = Modifier.testTag("addWeight")) {
                    Icon(painterResource(R.drawable.ic_add), contentDescription = stringResource(R.string.entry_title))
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            val all = state.weights
            when {
                state.loadFailed && all == null -> Notice(stringResource(R.string.recent_failed))
                all == null -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                else -> PeriodList(state, all, actions, profile)
            }
        }
    }
    if (state.entryOpen) ManualEntryDialog(state, actions)
}

/** Chips, chart and stats scroll away above the period's weights, so the list keeps room on a small phone. */
@Composable
private fun PeriodList(state: MainUiState, all: List<WeightEntry>, actions: MainActions, profile: OwnerProfile) {
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
    val pager = rememberPagerState { Quantity.entries.size }
    val scope = rememberCoroutineScope()
    LazyColumn(Modifier.fillMaxSize().testTag("weights"), state = listState) {
        item(key = "chips") {
            PeriodChips(
                state.period,
                actions.onPeriod,
                dayWeights = dayWeights,
                historyMissing = state.history == HistoryAccess.NOT_GRANTED,
                onGrantHistory = actions.onGrantHistory,
            )
        }
        if (state.history == HistoryAccess.NOT_GRANTED && state.period.needsHistory(now, zone)) {
            item(key = "history") { HistoryNotice(actions.onGrantHistory) }
        }
        item(key = "tabs") {
            PrimaryScrollableTabRow(
                selectedTabIndex = pager.currentPage,
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Quantity.entries.forEachIndexed { i, quantity ->
                    Tab(
                        selected = pager.currentPage == i,
                        onClick = { scope.launch { pager.animateScrollToPage(i) } },
                        text = { Text(stringResource(quantity.title()), maxLines = 1) },
                        modifier = Modifier.testTag("tab-${quantity.name}"),
                    )
                }
            }
        }
        item(key = "pager") {
            HorizontalPager(pager, verticalAlignment = Alignment.Top) { page ->
                QuantityPage(Quantity.entries[page], state.period, shown, now, zone, profile, actions.onOpenProfile)
            }
        }
        items(rows, key = { it.node.key }) { row ->
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            when (val node = row.node) {
                is ListNode.Weight -> if (node.questioned) {
                    QuestionedRowItem(node.entry, today, row.depth, onCheck = { checking = node.entry }, onDelete = { actions.onDelete(node.entry) })
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
                .background(MaterialTheme.colorScheme.surfaceContainer)
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
                ChartArea(data)
            } else {
                                WeightChart(
                    data,
                    stringResource(R.string.chart_description_bmi, data.entries.size),
                    axisLabel = quantity::axis,
                    pointLabel = quantity::point,
                    bands = bmiBands(),
                )
            }
        }
        if (state == Availability.Ok) stats?.let { StatsTiles(it, quantity, profile, shown.maxOfOrNull { e -> e.time }) }
    }
}

/** One stats tile; [direction] puts an arrow on a change (0: none); [valueColor] tints the value. */
private data class Tile(val label: Int, val value: String, val direction: Int = 0, val valueColor: Color = Color.Unspecified)

/** Key numbers as small tiles, three per row; a number that means nothing (one weight) is left out. */
@Composable
private fun StatsTiles(stats: PeriodStats, quantity: Quantity = Quantity.WEIGHT, profile: OwnerProfile = OwnerProfile(), latestTime: Instant? = null) {
    val tiles = buildList {
        if (quantity != Quantity.WEIGHT) {
            // Derived quantities have no rate; the latest takes its place, BMI with its class word in its tone.
            val minor = latestTime != null && Derived.minor(profile, latestTime.atZone(ZoneId.systemDefault()).year)
            if (quantity == Quantity.BMI && !minor) {
                val word = stringResource(Derived.bmiClass(stats.latest).word())
                add(Tile(R.string.stats_latest, "${quantity.format(stats.latest)} $word", valueColor = toneColor(Derived.tone(stats.latest))))
            } else {
                add(Tile(R.string.stats_latest, quantity.format(stats.latest)))
            }
        }
        stats.change?.let { add(Tile(R.string.stats_change, quantity.changeText(it), shownDirection(it, 1))) }
        stats.perWeek?.let { add(Tile(R.string.stats_rate, stringResource(R.string.kg_value, formatRate(it)), shownDirection(it, 2))) }
        add(Tile(R.string.stats_average, quantity.format(stats.average)))
        add(Tile(R.string.stats_min, quantity.format(stats.min)))
        add(Tile(R.string.stats_max, quantity.format(stats.max)))
        add(Tile(R.string.stats_count, stats.count.toString()))
    }
    Column(
        Modifier.fillMaxWidth().padding(16.dp).testTag("stats"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tiles.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (label, value, direction, valueColor) ->
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(stringResource(label), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        ChangeLabel(value, direction, style = MaterialTheme.typography.bodyLarge, color = valueColor, fontWeight = FontWeight.SemiBold)
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/**
 * A "?" weight (weight-entry "Questioned row look"): an amber-tinted outlined card with the "?"
 * sign and "tarkista", no change; a tap opens Hyväksy / Poista / Peruuta.
 */
@Composable
private fun QuestionedRowItem(entry: WeightEntry, today: LocalDate, depth: Int, onCheck: () -> Unit, onDelete: () -> Unit) {
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
            .heightIn(min = 48.dp)
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
        val label = stringResource(R.string.delete_weight, formatKg(entry.kg))
        IconButton(onClick = onDelete, modifier = Modifier.testTag("delete-${entry.id}")) {
            Icon(painterResource(R.drawable.ic_delete), contentDescription = label, tint = check.content)
        }
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
                TextButton(onClick = onDelete, modifier = Modifier.testTag("checkDelete")) { Text(stringResource(R.string.check_delete)) }
                TextButton(onClick = onDismiss, modifier = Modifier.testTag("checkCancel")) { Text(stringResource(R.string.cancel)) }
            }
        },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WeightRowItem(entry: WeightEntry, change: Double?, today: LocalDate, depth: Int, onDelete: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = {}, onLongClick = onDelete)
            .heightIn(min = 48.dp)
            .padding(start = indent(depth), end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(formatRowTime(entry.time, today), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        change?.let {
            val direction = shownDirection(it, 1)
            ChangeLabel(
                formatChange(it),
                direction,
                Modifier.padding(end = 12.dp).testTag("rowChange-${entry.id}"),
                style = MaterialTheme.typography.bodyMedium,
                color = changeColor(direction),
            )
        }
        Text(stringResource(R.string.kg_value, formatKg(entry.kg)), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        // Visible alternative to the long-press (ui rule: no gesture without one).
        val label = stringResource(R.string.delete_weight, formatKg(entry.kg))
        IconButton(onClick = onDelete, modifier = Modifier.testTag("delete-${entry.id}")) {
            Icon(painterResource(R.drawable.ic_delete), contentDescription = label, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
