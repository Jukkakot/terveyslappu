package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.chart.AxisTicks
import fi.jukkakot.terveyslappu.chart.ChartDots
import fi.jukkakot.terveyslappu.chart.HomeFood
import fi.jukkakot.terveyslappu.chart.HomeSummary
import fi.jukkakot.terveyslappu.chart.Period
import fi.jukkakot.terveyslappu.home.CardSize
import fi.jukkakot.terveyslappu.home.HomeCard
import fi.jukkakot.terveyslappu.home.HomeLayout
import fi.jukkakot.terveyslappu.steps.HomeSteps
import fi.jukkakot.terveyslappu.steps.StepsAccess
import fi.jukkakot.terveyslappu.steps.StepsUiState
import fi.jukkakot.terveyslappu.weight.WeightEntry
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * One row unit of the front page grid (home-card-layout design 2): small and wide cards are one unit
 * high, big cards two units and the gap. Grows with the text up to 130 %. 248 dp was the starting
 * estimate; the size matrix test (`HomeCardSizesTest`) sets the final value.
 */
internal val ROW_UNIT = 248.dp

/** The grid's gap between cards and rows. */
internal val GRID_GAP = 8.dp

@Composable
internal fun rowUnit(): Dp = ROW_UNIT * LocalDensity.current.fontScale.coerceIn(1f, 1.3f)

internal fun cardHeight(size: CardSize, unit: Dp): Dp = if (size == CardSize.BIG) unit * 2 + GRID_GAP else unit

/** What every front page card reads; one bundle so the grid and the size test draw the same cards. */
internal class HomeCardsData(
    val state: MainUiState,
    val actions: MainActions,
    val food: FoodUiState,
    val foodActions: FoodActions,
    val steps: StepsUiState,
    val stepsActions: StepsActions,
)

/** One front page card at its size, filling [modifier]'s size (home-screen "Card sizes"). */
@Composable
internal fun HomeCardView(card: HomeCard, data: HomeCardsData, modifier: Modifier = Modifier) {
    val state = data.state
    val today = LocalDate.now()
    when (card.id) {
        HomeLayout.WEIGHT -> {
            val now = remember(state.loadedAt) { state.loadedAt ?: Instant.now() }
            val summary = remember(state.weights, state.homeDays, state.questioned) {
                state.weights?.let { HomeSummary.of(it, now, state.homeDays, state.questioned) }
            }
            LatestCard(state, summary, today, card.size, data.actions.onHomePeriod, data.actions.onWeighIn, { data.actions.onScreen(Screen.HISTORY) }, modifier)
        }
        HomeLayout.FOOD -> FoodCard(data.food, state.foodDays, today, card.size, data.actions.onFoodPeriod, data.actions.onOpenEnergy, data.foodActions, modifier)
        HomeLayout.STEPS -> StepsCard(data.steps, state.stepsDays, today, card.size, data.stepsActions, modifier)
    }
}

/**
 * The food card (home-screen "Food row on the home screen", "Card sizes"), built like the weight
 * card: today's kcal, daily bars of its own period, the period's dates and average, outlined
 * + Annos inside. The title and today's number open Ruoka on today; the bars and what is under them
 * open Historia on Energia; the period label keeps its menu. Nothing on it is filled. Today is read
 * at every composition, so a resume after midnight shows the new day.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FoodCard(food: FoodUiState, days: Long, today: LocalDate, size: CardSize, onPeriod: (Long) -> Unit, onOpenEnergy: () -> Unit, actions: FoodActions, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val needsPermission = food.access == FoodAccess.NEEDS || food.access == FoodAccess.DENIED
    val summary = remember(food.items, days, today) { HomeFood.of(food.items.orEmpty(), days, today) }
    // Without the permission a tap asks for it (denied: Ruoka explains and offers the grant).
    val onTop = { if (food.access == FoodAccess.NEEDS) actions.onGrant() else actions.onOpenDay(today) }
    Column(
        modifier
            .clip(cardShape)
            .background(colors.surfaceContainer)
            .padding(8.dp)
            .testTag("foodCard"),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(innerShape)
                .clickable(onClickLabel = stringResource(R.string.home_open_food), onClick = onTop)
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("foodToday"),
        ) {
            CardTitle(R.drawable.ic_restaurant, stringResource(R.string.home_food_title), chevron = true)
            when {
                needsPermission -> Unit
                summary.todayLogged -> FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(formatKcal(summary.todayTotal), style = MaterialTheme.typography.displaySmall, modifier = Modifier.alignByBaseline().testTag("foodTodayValue"))
                        Spacer(Modifier.width(6.dp))
                        Text("kcal", style = MaterialTheme.typography.titleMedium, color = colors.onSurfaceVariant, modifier = Modifier.alignByBaseline())
                    }
                    if (summary.todayTreat > 0) {
                        Text(
                            stringResource(R.string.food_day_treats, formatKcal(summary.todayTreat)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterVertically),
                        )
                    }
                }
                else -> Text(stringResource(R.string.home_food_none), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 4.dp))
            }
        }
        if (needsPermission) {
            // Gated: the same text as before, centred in the free space, with the same tap (design 9).
            GatedBox(Modifier.clip(innerShape).clickable(onClickLabel = stringResource(R.string.home_open_food), onClick = onTop)) {
                Text(stringResource(R.string.food_today_permission), style = MaterialTheme.typography.titleLarge)
            }
            return@Column
        }
        val small = MaterialTheme.typography.labelSmall
        val big = size == CardSize.BIG
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(innerShape)
                .clickable(onClickLabel = stringResource(R.string.home_open_energy), onClick = onOpenEnergy)
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("foodBars"),
        ) {
            Box(Modifier.fillMaxWidth().weight(1f).heightIn(min = MIN_CHART)) {
                if (summary.bars.isNotEmpty()) {
                    val description = stringResource(R.string.home_food_bars, summary.bars.size)
                    val top = if (big) AxisTicks.bars(summary.bars.maxOf { it.total }.toDouble(), KCAL_STEP).last() else null
                    FoodBars(
                        summary, colors.primary, colors.tertiary, top, summary.averageKcal?.toDouble()?.takeIf { big }, colors.onSurfaceVariant,
                        Modifier.fillMaxWidth().fillMaxHeight().semantics { contentDescription = description },
                    )
                    if (top != null) ScaleTop(stringResource(R.string.food_kcal, formatKcal(top)), "foodScaleTop")
                }
            }
            Row(Modifier.fillMaxWidth().clearAndSetSemantics {}) {
                Text(formatShortDay(summary.from), style = small, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
                Text(formatShortDay(summary.to), style = small, color = colors.onSurfaceVariant)
            }
        }
        val average = summary.averageKcal?.let {
            stringResource(if (size == CardSize.SMALL) R.string.home_food_average else R.string.home_food_average_long, formatKcal(it))
        } ?: stringResource(R.string.home_food_no_average)
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            HomePeriodLabel(days, onPeriod, "foodPeriod")
            Text(
                average,
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
                    .clip(innerShape)
                    .clickable(onClickLabel = stringResource(R.string.home_open_energy), onClick = onOpenEnergy)
                    .heightIn(min = 44.dp)
                    .wrapContentHeight(Alignment.CenterVertically)
                    .padding(horizontal = 4.dp)
                    .testTag("foodAverage"),
            )
            // Wide and big: + Annos at the end of the period row (spec Card sizes).
            if (size != CardSize.SMALL) AddButton(today, actions, Modifier)
        }
        if (size == CardSize.SMALL) AddButton(today, actions, Modifier.fillMaxWidth())
    }
}

@Composable
private fun AddButton(today: LocalDate, actions: FoodActions, modifier: Modifier) {
    OutlinedButton(
        onClick = { actions.onAdd(today) },
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(start = 12.dp, end = 16.dp),
        modifier = modifier.heightIn(min = 44.dp).testTag("homeFoodAdd"),
    ) {
        Icon(painterResource(R.drawable.ic_add), contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(4.dp))
        Text(stringResource(R.string.home_food_add))
    }
}

/** A card's icon, title and (when it opens something) chevron. */
@Composable
private fun CardTitle(icon: Int, title: String, chevron: Boolean) {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(painterResource(icon), contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
        if (chevron) Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
    }
}

/** A gated state (permission, unavailable, loading, failed) centred in the card's free space. */
@Composable
private fun ColumnScope.GatedBox(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxWidth().weight(1f).then(modifier).padding(8.dp), contentAlignment = Alignment.Center) { content() }
}

/** The top of a big card's bar scale at the bars' top left (the Energia / Askeleet rule). */
@Composable
private fun ScaleTop(text: String, tag: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f))
            .padding(horizontal = 2.dp)
            .testTag(tag),
    )
}

/** Daily kcal bars of the period without axes, in Energia's tones (food, treats stacked); big: on the scale [top] with a dashed [average]. */
@Composable
private fun FoodBars(summary: HomeFood, food: Color, treat: Color, top: Double?, average: Double?, line: Color, modifier: Modifier) {
    Canvas(modifier) {
        val days = (ChronoUnit.DAYS.between(summary.from, summary.to) + 1).toInt().coerceAtLeast(1)
        val max = (top ?: summary.bars.maxOf { it.total }.toDouble()).coerceAtLeast(1.0).toFloat()
        val dayW = size.width / days
        for (bar in summary.bars) {
            val left = ChronoUnit.DAYS.between(summary.from, bar.from) * dayW + dayW * 0.15f
            val width = maxOf(dayW * 0.7f, 1.dp.toPx())
            val baseTop = size.height * (1 - (bar.total - bar.treat) / max)
            drawRect(food, Offset(left, baseTop), Size(width, size.height - baseTop))
            if (bar.treat > 0) {
                val treatTop = size.height * (1 - bar.total / max)
                drawRect(treat, Offset(left, treatTop), Size(width, baseTop - treatTop))
            }
        }
        drawLine(food.copy(alpha = 0.4f), Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
        if (average != null) averageLine(average.toFloat() / max, line)
    }
}

/** The dashed average line of a big card (design 9): 1 dp, onSurfaceVariant. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.averageLine(fraction: Float, color: Color) {
    val y = size.height * (1 - fraction.coerceIn(0f, 1f))
    val dash = 4.dp.toPx()
    drawLine(color, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash)))
}

/**
 * The steps card (home-screen "Steps card on the home screen", "Card sizes"), the food card's twin
 * without a quick action: today's steps, daily bars of its own period, the period's dates and
 * average. A tap anywhere but the period label opens Askeleet (without the permission: asks for it;
 * after a denial Askeleet explains); unavailable: no tap action. Free space goes to the bars.
 */
@Composable
private fun StepsCard(steps: StepsUiState, days: Long, today: LocalDate, size: CardSize, actions: StepsActions, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val summary = remember(steps.days, days, today) { HomeSteps.of(steps.days.orEmpty(), days, today) }
    val onTap: (() -> Unit)? = when (steps.access) {
        StepsAccess.NEEDS -> actions.onGrant
        StepsAccess.DENIED, StepsAccess.GRANTED -> actions.onOpen
        StepsAccess.UNAVAILABLE, StepsAccess.CHECKING -> null
    }
    val shows = steps.access == StepsAccess.GRANTED && steps.days != null
    Column(
        modifier
            .clip(cardShape)
            .background(colors.surfaceContainer)
            .then(if (onTap != null) Modifier.clickable(onClickLabel = stringResource(R.string.home_open_steps), onClick = onTap) else Modifier)
            .padding(8.dp)
            .testTag("stepsCard"),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp).testTag("stepsToday")) {
            CardTitle(R.drawable.ic_walk, stringResource(R.string.home_steps_title), chevron = onTap != null)
            if (shows) {
                if (summary.today != null) {
                    Text(formatSteps(summary.today), style = MaterialTheme.typography.displaySmall, modifier = Modifier.testTag("stepsTodayValue"))
                } else {
                    Text(stringResource(R.string.steps_no_data), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
        if (!shows) {
            GatedBox {
                val gated = MaterialTheme.typography.titleLarge
                when {
                    steps.access == StepsAccess.UNAVAILABLE -> Text(stringResource(R.string.steps_unavailable), style = gated)
                    steps.access == StepsAccess.NEEDS || steps.access == StepsAccess.DENIED -> Text(stringResource(R.string.steps_today_permission), style = gated)
                    steps.access == StepsAccess.CHECKING || !steps.loadFailed -> CircularProgressIndicator(Modifier.size(24.dp))
                    else -> Text(stringResource(R.string.steps_read_failed), style = gated, modifier = Modifier.testTag("stepsFailed"))
                }
            }
            return@Column
        }
        val small = MaterialTheme.typography.labelSmall
        val big = size == CardSize.BIG
        Column(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 8.dp, vertical = 4.dp).testTag("stepsBars")) {
            Box(Modifier.fillMaxWidth().weight(1f).heightIn(min = MIN_CHART)) {
                if (summary.bars.isNotEmpty()) {
                    val description = stringResource(R.string.home_steps_bars, summary.bars.size)
                    val top = if (big) AxisTicks.bars(summary.bars.maxOf { it.steps }.toDouble(), STEPS_STEP).last() else null
                    StepsBars(
                        summary, colors.primary, top, summary.averageSteps?.toDouble()?.takeIf { big }, colors.onSurfaceVariant,
                        Modifier.fillMaxWidth().fillMaxHeight().semantics { contentDescription = description },
                    )
                    if (top != null) ScaleTop(stringResource(R.string.steps_value, formatSteps(top.toLong())), "stepsScaleTop")
                }
            }
            Row(Modifier.fillMaxWidth().clearAndSetSemantics {}) {
                Text(formatShortDay(summary.from), style = small, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
                Text(formatShortDay(summary.to), style = small, color = colors.onSurfaceVariant)
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            HomePeriodLabel(days, actions.onPeriod, "stepsPeriod")
            Text(
                summary.averageSteps?.let {
                    stringResource(if (size == CardSize.SMALL) R.string.home_steps_average else R.string.home_steps_average_long, formatSteps(it))
                } ?: stringResource(R.string.home_food_no_average),
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
                modifier = Modifier.weight(1f).padding(start = 4.dp).heightIn(min = 44.dp).wrapContentHeight(Alignment.CenterVertically).padding(horizontal = 4.dp).testTag("stepsAverage"),
            )
        }
        if (steps.stale) {
            Text(
                stringResource(R.string.steps_stale),
                style = small,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp).testTag("stepsStale"),
            )
        }
    }
}

/** Daily step bars of the period without axes, in one tone; big: on the scale [top] with a dashed [average]. */
@Composable
private fun StepsBars(summary: HomeSteps, color: Color, top: Double?, average: Double?, line: Color, modifier: Modifier) {
    Canvas(modifier) {
        val days = (ChronoUnit.DAYS.between(summary.from, summary.to) + 1).toInt().coerceAtLeast(1)
        val max = (top ?: summary.bars.maxOf { it.steps }.toDouble()).coerceAtLeast(1.0).toFloat()
        val dayW = size.width / days
        for (bar in summary.bars) {
            val left = ChronoUnit.DAYS.between(summary.from, bar.from) * dayW + dayW * 0.15f
            val width = maxOf(dayW * 0.7f, 1.dp.toPx())
            val barTop = size.height * (1 - bar.steps / max)
            drawRect(color, Offset(left, barTop), Size(width, size.height - barTop))
        }
        drawLine(color.copy(alpha = 0.4f), Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
        if (average != null) averageLine(average.toFloat() / max, line)
    }
}

private val cardShape = RoundedCornerShape(24.dp)
internal val homeCardShape get() = cardShape
/** A tap area inside a card (its ripple keeps inside the card's corners). */
private val innerShape = RoundedCornerShape(16.dp)
/** The least height of a card's chart; the chart takes the height its size leaves free (design 2). */
private val MIN_CHART = 32.dp
/** The bar scale steps of Energia and Askeleet (steps-source-and-axes `AxisTicks.bars`). */
private const val KCAL_STEP = 250.0
private const val STEPS_STEP = 1000.0
internal val groupShape = RoundedCornerShape(20.dp)

/** The weight card: latest weight, change, small chart, and Punnitse, the home screen's only filled action. */
@Composable
private fun LatestCard(
    state: MainUiState,
    summary: HomeSummary?,
    today: LocalDate,
    size: CardSize,
    onHomePeriod: (Long) -> Unit,
    onWeighIn: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val small = size == CardSize.SMALL
    Column(
        modifier
            .clip(cardShape)
            .background(colors.surfaceContainer)
            .clickable(onClickLabel = stringResource(R.string.home_open_history), onClick = onOpen)
            .padding(start = if (small) 12.dp else 16.dp, end = if (small) 12.dp else 16.dp, top = 12.dp, bottom = 12.dp)
            .testTag("latestCard"),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val title = summary?.let { stringResource(R.string.home_latest, formatDayTime(it.latest.time, today)) }
                ?: stringResource(R.string.history_title)
            Text(title, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
        when {
            summary != null -> LatestValues(summary, state.homeDays, size, onHomePeriod)
            state.loadFailed -> GatedBox { Text(stringResource(R.string.recent_failed), style = MaterialTheme.typography.bodyLarge) }
            state.weights == null -> GatedBox { CircularProgressIndicator() }
            else -> GatedBox {
                Column(Modifier.testTag("homeEmpty"), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.home_empty), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.home_empty_hint), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                }
            }
        }
        Button(
            onClick = onWeighIn,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.padding(top = 8.dp).fillMaxWidth().heightIn(min = 52.dp).testTag("weighIn"),
        ) {
            Icon(painterResource(R.drawable.ic_scale), contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Text(stringResource(R.string.home_weigh), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColumnScope.LatestValues(summary: HomeSummary, days: Long, size: CardSize, onHomePeriod: (Long) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val small = size == CardSize.SMALL
    FlowRow(
        Modifier.fillMaxWidth(),
        // Small: the chip goes under the number when it does not fit beside it.
        horizontalArrangement = if (small) Arrangement.spacedBy(8.dp) else Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(end = 8.dp)) {
            val number = if (small) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.displaySmall
            Text(formatKg(summary.latest.kg), style = number, modifier = Modifier.alignByBaseline().testTag("latestKg"))
            Spacer(Modifier.width(6.dp))
            Text("kg", style = MaterialTheme.typography.titleMedium, color = colors.onSurfaceVariant, modifier = Modifier.alignByBaseline())
        }
        summary.change?.let { ChangeChip(it, Modifier.align(Alignment.CenterVertically)) }
    }
    val first = summary.spark.firstOrNull()
    if (first != null) {
        val description = stringResource(R.string.home_spark, formatKg(first.kg), formatKg(summary.latest.kg))
        val chart = Modifier.padding(top = 4.dp).fillMaxWidth().weight(1f).heightIn(min = MIN_CHART)
        val scale = summary.sparkScale
        if (size == CardSize.BIG && scale != null) {
            // Big: the scale's top and bottom with kg at the left edge (spec Card sizes).
            Row(chart) {
                Column(Modifier.fillMaxHeight().padding(end = 4.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.kg_value, formatKg(scale.max)), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant, modifier = Modifier.testTag("sparkScaleTop"))
                    Text(stringResource(R.string.kg_value, formatKg(scale.min)), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant, modifier = Modifier.testTag("sparkScaleBottom"))
                }
                Sparkline(summary, colors.primary, Modifier.weight(1f).fillMaxHeight().semantics { contentDescription = description })
            }
        } else {
            Sparkline(summary, colors.primary, chart.semantics { contentDescription = description })
        }
        val zone = ZoneId.systemDefault()
        val label = MaterialTheme.typography.labelSmall
        Row(Modifier.fillMaxWidth().clearAndSetSemantics {}) {
            Text(formatShortDay(summary.sparkFrom.atZone(zone).toLocalDate()), style = label, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text(formatShortDay(summary.sparkTo.atZone(zone).toLocalDate()), style = label, color = colors.onSurfaceVariant)
        }
    } else {
        Spacer(Modifier.weight(1f))
    }
    Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        HomePeriodLabel(days, onHomePeriod)
        if (small) {
            Spacer(Modifier.weight(1f).width(4.dp))
        } else {
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
        }
        summary.sparkChange?.let { change ->
            ChangeLabel(stringResource(R.string.kg_value, formatChange(change)), shownDirection(change, 1), Modifier.testTag("sparkChange"), style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
        }
    }
}

/** A card's chart period (home-screen "Home chart period"): its own tap opens a menu, not the card's details. */
@Composable
private fun HomePeriodLabel(days: Long, onHomePeriod: (Long) -> Unit, tag: String = "homePeriod") {
    var open by remember { mutableStateOf(false) }
    Box {
        Surface(
            onClick = { open = true },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.heightIn(min = 32.dp).testTag(tag),
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
                    modifier = Modifier.testTag("$tag-$option"),
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

/** The period's weights as a line by time, no axes; the latest weight is a dot at the right end. */
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
        // The whole period and the shared weight scale (home-screen, weight-chart "Weight scale").
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

/** "Punnitus odottaa tarkistusta" in the "?" row's amber tint; opens Historia at the newest "?" weight. */
@Composable
internal fun CheckNotice(onOpen: () -> Unit) {
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
internal fun BackupNotice(failed: Boolean, onOpen: () -> Unit) {
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

/** The clipboard offer line above the cards (food-ai-import). */
@Composable
internal fun ClipOfferNotice(offer: fi.jukkakot.terveyslappu.food.ClipOffer, actions: FoodActions) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceContainer).padding(start = 16.dp, end = 4.dp)) {
        ClipOfferLine(offer, actions)
    }
}
