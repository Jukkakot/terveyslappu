package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import fi.jukkakot.terveyslappu.chart.ChartDots
import fi.jukkakot.terveyslappu.chart.HomeSummary
import fi.jukkakot.terveyslappu.chart.Period
import fi.jukkakot.terveyslappu.weight.WeightEntry
import fi.jukkakot.terveyslappu.derived.LatestDerived
import fi.jukkakot.terveyslappu.profile.OwnerProfile
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** The simple home screen (mockup B): latest card, three latest weights, Punnitse vaa'alla. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    state: MainUiState,
    actions: MainActions,
    snackbar: SnackbarHostState,
    backup: BackupStatus,
    profile: OwnerProfile = OwnerProfile(),
    food: FoodUiState = FoodUiState(),
    foodActions: FoodActions = FoodActions(),
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { actions.onScreen(Screen.HELP) }, modifier = Modifier.testTag("help")) {
                        Icon(painterResource(R.drawable.ic_help), contentDescription = stringResource(R.string.menu_help))
                    }
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
                Access.GRANTED -> HomeContent(state, actions, backup, profile, food, foodActions)
            }
        }
    }
}

@Composable
private fun HomeContent(state: MainUiState, actions: MainActions, backup: BackupStatus, profile: OwnerProfile, food: FoodUiState, foodActions: FoodActions) {
    val now = remember(state.loadedAt) { state.loadedAt ?: Instant.now() }
    val summary = remember(state.weights, state.homeDays, state.questioned) {
        state.weights?.let { HomeSummary.of(it, now, state.homeDays, state.questioned) }
    }
    val today = LocalDate.now()
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (backup.needsAttention(Instant.now())) BackupNotice(backup.failed, actions.onOpenBackupSettings)
        if (state.newestQuestioned != null) CheckNotice(actions.onOpenQuestioned)
        LatestCard(state, summary, today, actions.onHomePeriod) { actions.onScreen(Screen.HISTORY) }
        // Recomputed whenever the weights or the owner details change (derived-values).
        val derived = remember(summary?.latest, profile) { summary?.let { LatestDerived.of(it.latest, profile, ZoneId.systemDefault()) } }
        derived?.let { DerivedRow(it, actions.onOpenProfile) }
        summary?.let { RecentList(it.recent, today, state.questioned) }
        FoodRowAndAdd(food, today, foodActions)
    }
}

/**
 * "Syöty tänään" and the outlined Lisää annos (home-screen "Food row on the home screen"); neither
 * is filled, so Punnitse vaa'alla stays the only filled action. Today is read at every composition,
 * so a resume after midnight shows the new day.
 */
@Composable
private fun FoodRowAndAdd(food: FoodUiState, today: LocalDate, actions: FoodActions) {
    val colors = MaterialTheme.colorScheme
    val text = when {
        food.access == FoodAccess.NEEDS || food.access == FoodAccess.DENIED -> stringResource(R.string.food_today_permission)
        else -> food.items?.filter { it.day == today }?.takeIf { it.isNotEmpty() }
            ?.let { stringResource(R.string.food_today_row, formatKcal(fi.jukkakot.terveyslappu.food.DaySum.of(it).total)) }
            ?: stringResource(R.string.food_today_none)
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(groupShape)
            .background(colors.surfaceContainer)
            .clickable(onClickLabel = stringResource(R.string.food_title)) { actions.onOpenDay(today) }
            .heightIn(min = 56.dp)
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .testTag("foodToday"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(painterResource(R.drawable.ic_restaurant), contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
    }
    OutlinedButton(
        onClick = { actions.onAdd(today) },
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("homeFoodAdd"),
    ) {
        Icon(painterResource(R.drawable.ic_add), contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.food_add))
    }
}

private val cardShape = RoundedCornerShape(24.dp)
internal val groupShape = RoundedCornerShape(20.dp)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LatestCard(state: MainUiState, summary: HomeSummary?, today: LocalDate, onHomePeriod: (Long) -> Unit, onOpen: () -> Unit) {
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
            summary != null -> LatestValues(summary, state.homeDays, onHomePeriod)
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
private fun LatestValues(summary: HomeSummary, days: Long, onHomePeriod: (Long) -> Unit) {
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
    val first = summary.spark.firstOrNull()
    if (first != null) {
        val description = stringResource(R.string.home_spark, formatKg(first.kg), formatKg(summary.latest.kg))
        Sparkline(summary, colors.primary, Modifier.padding(top = 8.dp).fillMaxWidth().height(72.dp).semantics { contentDescription = description })
        val zone = ZoneId.systemDefault()
        val small = MaterialTheme.typography.labelSmall
        Row(Modifier.fillMaxWidth().clearAndSetSemantics {}) {
            Text(formatShortDay(summary.sparkFrom.atZone(zone).toLocalDate()), style = small, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text(formatShortDay(summary.sparkTo.atZone(zone).toLocalDate()), style = small, color = colors.onSurfaceVariant)
        }
    }
    Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        HomePeriodLabel(days, onHomePeriod)
        val range = when {
            first == null -> stringResource(R.string.period_empty)
            summary.sparkChange == null -> stringResource(R.string.kg_value, formatKg(first.kg))
            else -> stringResource(R.string.home_spark_range, formatKg(first.kg), formatKg(summary.latest.kg))
        }
        Text(
            range,
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
            modifier = Modifier.weight(1f).padding(start = 8.dp).testTag("sparkRange"),
        )
        summary.sparkChange?.let { change ->
            ChangeLabel(stringResource(R.string.kg_value, formatChange(change)), shownDirection(change, 1), style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
        }
    }
}

/** The home chart's period (home-screen "Home chart period"): its own tap opens a menu, not Historia. */
@Composable
private fun HomePeriodLabel(days: Long, onHomePeriod: (Long) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        Surface(
            onClick = { open = true },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.heightIn(min = 32.dp).testTag("homePeriod"),
        ) {
            Row(Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Period.Last(days).label()), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                Icon(painterResource(R.drawable.ic_arrow_drop_down), contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(18.dp))
            }
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            HomeSummary.HOME_DAYS.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(Period.Last(option).label())) },
                    onClick = {
                        open = false
                        onHomePeriod(option)
                    },
                    modifier = Modifier.testTag("homePeriod-$option"),
                )
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
        // Every weight as a small dot when they fit (weight-chart "Dots when they fit"); the latest always.
        if (ChartDots.fit(points.size, width.toDp().value)) {
            val small = 2.5.dp.toPx()
            points.dropLast(1).forEach { drawCircle(color, radius = small, center = at(it)) }
        }
        drawCircle(color, radius = dot, center = at(points.last()))
    }
}

@Composable
private fun RecentList(recent: List<WeightEntry>, today: LocalDate, questioned: Set<String>) {
    Column(Modifier.fillMaxWidth().clip(groupShape).background(MaterialTheme.colorScheme.surfaceContainer).testTag("recent")) {
        recent.forEach { entry ->
            Row(
                Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(formatRowTime(entry.time, today, ZoneId.systemDefault()), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                val kg = stringResource(R.string.kg_value, formatKg(entry.kg))
                if (entry.id in questioned) {
                    // A "?" weight: its sign, no change (home-screen "Questioned weights on the home screen").
                    val description = stringResource(R.string.check_row_description, formatKg(entry.kg))
                    Text(
                        kg + " " + stringResource(R.string.check_mark),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.semantics { contentDescription = description }.testTag("recentQuestioned"),
                    )
                } else {
                    Text(kg, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/** "Punnitus odottaa tarkistusta" in the "?" row's amber tint; opens Historia at the newest "?" weight. */
@Composable
private fun CheckNotice(onOpen: () -> Unit) {
    val check = checkColors()
    val shape = RoundedCornerShape(16.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(check.container)
            .border(1.dp, check.outline, shape)
            .clickable(onClickLabel = stringResource(R.string.history_title), onClick = onOpen)
            .heightIn(min = 48.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("checkNotice"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(painterResource(R.drawable.ic_warning), contentDescription = null, tint = check.content, modifier = Modifier.size(20.dp))
        Text(stringResource(R.string.home_check_notice), style = MaterialTheme.typography.bodyMedium, color = check.content, modifier = Modifier.weight(1f))
        Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = check.content, modifier = Modifier.size(20.dp))
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
