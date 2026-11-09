package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import fi.jukkakot.terveyslappu.chart.changesFromPrevious
import fi.jukkakot.terveyslappu.chart.PeriodStats
import fi.jukkakot.terveyslappu.chart.filter
import fi.jukkakot.terveyslappu.chart.needsHistory
import fi.jukkakot.terveyslappu.chart.periodStats
import fi.jukkakot.terveyslappu.weight.HistoryAccess
import fi.jukkakot.terveyslappu.weight.WeightEntry
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Historia: period chips, chart, stats tiles and the period's weights; + opens manual entry. */
@Composable
internal fun HistoryScreen(state: MainUiState, actions: MainActions, snackbar: SnackbarHostState) {
    Scaffold(
        topBar = {
            SubScreenBar(stringResource(R.string.history_title), onBack = { actions.onScreen(Screen.HOME) }) {
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
                else -> PeriodList(state, all, actions)
            }
        }
    }
    if (state.entryOpen) ManualEntryDialog(state, actions)
}

/** Chips, chart and stats scroll away above the period's weights, so the list keeps room on a small phone. */
@Composable
private fun PeriodList(state: MainUiState, all: List<WeightEntry>, actions: MainActions) {
    val zone = ZoneId.systemDefault()
    val now = remember(all, state.period) { Instant.now() }
    val today = LocalDate.now(zone)
    val shown = remember(all, state.period) { state.period.filter(all, now, zone) }
    val changes = remember(shown) { changesFromPrevious(shown) }
    val data = remember(shown) { ChartData.of(state.period, shown, now, zone) }
    val stats = remember(shown) { periodStats(shown) }
    LazyColumn(Modifier.fillMaxSize().testTag("weights")) {
        item(key = "chips") { PeriodChips(state.period, actions.onPeriod) }
        if (state.history == HistoryAccess.NOT_GRANTED && state.period.needsHistory(now, zone)) {
            item(key = "history") { HistoryNotice(actions.onGrantHistory) }
        }
        item(key = "chart") {
            Box(
                Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp)
                    .clip(groupShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(8.dp),
            ) { ChartArea(data) }
        }
        stats?.let { item(key = "stats") { StatsTiles(it) } }
        items(shown, key = { it.id }) { entry ->
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            WeightRowItem(entry, changes[entry.id], today, onDelete = { actions.onDelete(entry) })
        }
    }
}

/** One stats tile; [direction] puts an arrow on a change (0: none). */
private data class Tile(val label: Int, val value: String, val direction: Int = 0)

/** Key numbers as small tiles, three per row; a number that means nothing (one weight) is left out. */
@Composable
private fun StatsTiles(stats: PeriodStats) {
    val tiles = buildList {
        stats.change?.let { add(Tile(R.string.stats_change, stringResource(R.string.kg_value, formatChange(it)), shownDirection(it, 1))) }
        stats.perWeek?.let { add(Tile(R.string.stats_rate, stringResource(R.string.kg_value, formatRate(it)), shownDirection(it, 2))) }
        add(Tile(R.string.stats_average, formatKg(stats.average)))
        add(Tile(R.string.stats_min, formatKg(stats.min)))
        add(Tile(R.string.stats_max, formatKg(stats.max)))
        add(Tile(R.string.stats_count, stats.count.toString()))
    }
    Column(
        Modifier.fillMaxWidth().padding(16.dp).testTag("stats"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tiles.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (label, value, direction) ->
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(stringResource(label), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        ChangeLabel(value, direction, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WeightRowItem(entry: WeightEntry, change: Double?, today: LocalDate, onDelete: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = {}, onLongClick = onDelete)
            .heightIn(min = 48.dp)
            .padding(start = 20.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
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
