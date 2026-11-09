package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.backup.BackupStatus
import fi.jukkakot.terveyslappu.chart.HomeSummary
import fi.jukkakot.terveyslappu.weight.WeightEntry
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** The simple home screen (mockup B): latest card, three latest weights, Punnitse vaa'alla. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(state: MainUiState, actions: MainActions, snackbar: SnackbarHostState, backup: BackupStatus) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { actions.onScreen(Screen.SETTINGS) }, modifier = Modifier.testTag("settings")) {
                        Icon(painterResource(R.drawable.ic_settings), contentDescription = stringResource(R.string.settings_title))
                    }
                },
            )
        },
        bottomBar = { if (state.access == Access.GRANTED) WeighInBar(actions.onWeighIn) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (state.access) {
                Access.CHECKING -> Notice(stringResource(R.string.hc_checking))
                Access.UNAVAILABLE -> Notice(stringResource(R.string.hc_unavailable), stringResource(R.string.hc_open_store), actions.onOpenStore)
                Access.NEEDS_UPDATE -> Notice(stringResource(R.string.hc_needs_update), stringResource(R.string.hc_open_store), actions.onOpenStore)
                Access.NEEDS_PERMISSION -> Notice(stringResource(R.string.hc_permission_intro), stringResource(R.string.hc_permission_grant), actions.onRequestPermission)
                Access.DENIED -> Notice(stringResource(R.string.hc_permission_denied), stringResource(R.string.hc_open_settings), actions.onOpenSettings)
                Access.GRANTED -> HomeContent(state, actions, backup)
            }
        }
    }
}

@Composable
private fun HomeContent(state: MainUiState, actions: MainActions, backup: BackupStatus) {
    val now = remember(state.weights) { Instant.now() }
    val summary = remember(state.weights) { state.weights?.let { HomeSummary.of(it, now) } }
    val today = LocalDate.now()
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (backup.needsAttention(Instant.now())) BackupNotice(backup.failed) { actions.onScreen(Screen.SETTINGS) }
        LatestCard(state, summary, today) { actions.onScreen(Screen.HISTORY) }
        summary?.let { RecentList(it.recent, today) }
    }
}

private val cardShape = RoundedCornerShape(24.dp)
internal val groupShape = RoundedCornerShape(20.dp)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LatestCard(state: MainUiState, summary: HomeSummary?, today: LocalDate, onOpen: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(colors.surfaceContainer)
            .clickable(onClickLabel = stringResource(R.string.home_open_history), onClick = onOpen)
            .padding(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
            .testTag("latestCard"),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val title = summary?.let { stringResource(R.string.home_latest, formatDayTime(it.latest.time, today)) }
                ?: stringResource(R.string.history_title)
            Text(title, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
            Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
        when {
            summary != null -> LatestValues(summary)
            state.loadFailed -> Text(stringResource(R.string.recent_failed), style = MaterialTheme.typography.bodyLarge)
            state.weights == null -> Box(Modifier.fillMaxWidth().height(96.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            else -> Column(Modifier.padding(vertical = 8.dp).testTag("homeEmpty"), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.home_empty), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.home_empty_hint), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LatestValues(summary: HomeSummary) {
    val colors = MaterialTheme.colorScheme
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(end = 8.dp)) {
            Text(formatKg(summary.latest.kg), style = MaterialTheme.typography.displayMedium, modifier = Modifier.alignByBaseline())
            Spacer(Modifier.width(6.dp))
            Text("kg", style = MaterialTheme.typography.titleLarge, color = colors.onSurfaceVariant, modifier = Modifier.alignByBaseline())
        }
        summary.change?.let { ChangeChip(it, Modifier.align(Alignment.CenterVertically)) }
    }
    if (summary.spark.isNotEmpty()) {
        val first = summary.spark.first()
        val description = stringResource(R.string.home_spark, formatKg(first.kg), formatKg(summary.latest.kg))
        Sparkline(summary, colors.primary, Modifier.padding(top = 8.dp).fillMaxWidth().height(72.dp).semantics { contentDescription = description })
        val zone = ZoneId.systemDefault()
        val small = MaterialTheme.typography.labelSmall
        Row(Modifier.fillMaxWidth().clearAndSetSemantics {}) {
            Text(formatShortDay(summary.sparkFrom.atZone(zone).toLocalDate()), style = small, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text(formatShortDay(summary.sparkTo.atZone(zone).toLocalDate()), style = small, color = colors.onSurfaceVariant)
        }
        summary.sparkChange?.let { change ->
            Row(Modifier.fillMaxWidth().padding(top = 4.dp).clearAndSetSemantics {}) {
                Text(
                    stringResource(R.string.home_spark_range, formatKg(first.kg), formatKg(summary.latest.kg)),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.weight(1f).testTag("sparkRange"),
                )
                ChangeLabel(stringResource(R.string.kg_value, formatChange(change)), shownDirection(change, 1), style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
            }
        }
    }
}

/** Change from the weight before: arrow and signed number, never colour alone. */
@Composable
private fun ChangeChip(change: Double, modifier: Modifier = Modifier) {
    val label = stringResource(R.string.home_change, formatChange(change))
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = modifier.testTag("change").clearAndSetSemantics { contentDescription = label },
    ) {
        ChangeLabel(
            stringResource(R.string.kg_value, formatChange(change)),
            shownDirection(change, 1),
            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

/** The last 30 days as a line by time, no axes; the latest weight is a dot at the right end. */
@Composable
private fun Sparkline(summary: HomeSummary, color: Color, modifier: Modifier) {
    val points = summary.spark
    val scale = summary.sparkScale ?: return
    Canvas(modifier) {
        val dot = 4.dp.toPx()
        val left = dot
        val width = size.width - 2 * dot
        val top = dot
        val height = size.height - 2 * dot
        // The whole 30 days and the shared weight scale (home-screen, weight-chart "Weight scale").
        val t0 = summary.sparkFrom.epochSecond
        val span = (summary.sparkTo.epochSecond - t0).coerceAtLeast(1)
        fun at(entry: WeightEntry): Offset {
            val x = left + width * ((entry.time.epochSecond - t0).toFloat() / span).coerceIn(0f, 1f)
            val y = top + height * ((scale.max - entry.kg) / scale.span).toFloat()
            return Offset(x, y)
        }
        if (points.size > 1) {
            val path = Path()
            points.forEachIndexed { i, entry ->
                val p = at(entry)
                if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
            }
            drawPath(path, color, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
        drawCircle(color, radius = dot, center = at(points.last()))
    }
}

@Composable
private fun RecentList(recent: List<WeightEntry>, today: LocalDate) {
    Column(Modifier.fillMaxWidth().clip(groupShape).background(MaterialTheme.colorScheme.surfaceContainer).testTag("recent")) {
        recent.forEach { entry ->
            Row(
                Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(formatRowTime(entry.time, today, ZoneId.systemDefault()), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Text(stringResource(R.string.kg_value, formatKg(entry.kg)), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/** One notice when the backup failed or is stale; opens Asetukset (weight-backup "Visible status"). */
@Composable
private fun BackupNotice(failed: Boolean, onOpen: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.errorContainer)
            .clickable(onClickLabel = stringResource(R.string.settings_title), onClick = onOpen)
            .heightIn(min = 48.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("backupNotice"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(painterResource(R.drawable.ic_warning), contentDescription = null, tint = colors.onErrorContainer, modifier = Modifier.size(20.dp))
        Text(
            stringResource(if (failed) R.string.home_backup_failed else R.string.home_backup_stale),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onErrorContainer,
            modifier = Modifier.weight(1f),
        )
        Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = colors.onErrorContainer, modifier = Modifier.size(20.dp))
    }
}

/** The only filled action on the home screen, low for the thumb. */
@Composable
private fun WeighInBar(onWeighIn: () -> Unit) {
    Box(Modifier.navigationBarsPadding().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)) {
        Button(
            onClick = onWeighIn,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).testTag("weighIn"),
        ) {
            Icon(painterResource(R.drawable.ic_scale), contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Text(stringResource(R.string.weighin_main_button), style = MaterialTheme.typography.titleMedium)
        }
    }
}
