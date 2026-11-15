package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.chart.ChartAxis
import fi.jukkakot.terveyslappu.chart.ChartLayout
import fi.jukkakot.terveyslappu.chart.CustomChartData
import fi.jukkakot.terveyslappu.chart.KindData
import fi.jukkakot.terveyslappu.chart.SeriesColour
import fi.jukkakot.terveyslappu.chart.SeriesKind
import fi.jukkakot.terveyslappu.chart.SeriesStyle
import fi.jukkakot.terveyslappu.home.CardSize
import java.time.LocalDate

/** Why a shown data has no values to draw (spec "Chart data without values"); Ready = it has loaded. */
internal sealed interface PanelState {
    data object Ready : PanelState
    data object Loading : PanelState
    data class Failed(val text: String) : PanelState
    /** No permission: the reason of its own tab and its buttons (label to action). */
    data class Gated(val text: String, val buttons: List<Pair<String, () -> Unit>>) : PanelState
}

/** Where a chart is drawn: its Historia tab, or a front page card at a size (custom-charts design 8, 9, 13). */
internal sealed interface ChartMode {
    data object Tab : ChartMode
    data class Card(val size: CardSize) : ChartMode
}

private val ChartMode.card: CardSize? get() = (this as? ChartMode.Card)?.size

/** Tab panel height per 1, 2, 3 stacked panels (design 8), the overlaid plot's height (design 9). */
private fun panelHeight(count: Int): Dp = when (count) {
    1 -> 160.dp
    2 -> 136.dp
    else -> 112.dp
}
private val OVERLAID_PLOT = 200.dp
private val MIN_CARD_PANEL = 24.dp

/**
 * An own chart (custom-charts design 8, 9): stacked panels or one overlaid plot over the chart's own
 * days, with the shared axis rule and legend. On a tab, taps and holds on the drawn plot areas call
 * [onDay] (the crosshair, design 10); a card has no gestures.
 */
@Composable
internal fun CustomChartView(
    data: CustomChartData,
    mode: ChartMode,
    states: Map<SeriesKind, PanelState> = emptyMap(),
    crosshair: LocalDate? = null,
    onDay: (LocalDate) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val description = stringResource(R.string.custom_chart_description, data.chart.name)
    val actions = if (mode == ChartMode.Tab) Modifier.crosshairActions(data.axis, crosshair, onDay) else Modifier
    val a11y = Modifier.semantics { contentDescription = description }.then(actions)
    val fill = mode.card != null
    Column(modifier.fillMaxWidth().then(a11y).testTag("customChart-${data.chart.id}")) {
        if (data.empty) {
            Message(stringResource(R.string.chart_empty), "chartEmpty", Modifier.fillMaxWidth().then(if (fill) Modifier.weight(1f) else Modifier.height(panelHeight(1))))
            return@Column
        }
        if (mode == ChartMode.Tab) RangeLine(data.axis.from, data.axis.to, "chartRange")
        val overlaidLayout = data.chart.layout == ChartLayout.OVERLAID
        if (overlaidLayout && data.drawn.size > 1) {
            Overlaid(data, mode, states, crosshair, onDay)
        } else {
            // One data drawn in Päällekkäin = exactly the stacked single panel; the rest named under it.
            Stacked(data, mode, states, crosshair, onDay, single = overlaidLayout)
        }
    }
}


/**
 * The data's tone (weight-chart "Data colours in own charts"): its chosen colour, else Oletus = weight
 * secondary, Syöty primary, Kulutus oranssi, steps tertiary when overlaid (its tab's primary when stacked).
 */
@Composable
private fun toneOf(kind: SeriesKind, colour: SeriesColour?, overlaid: Boolean): Color {
    if (colour != null) return paletteColour(colour)
    val c = MaterialTheme.colorScheme
    return when (kind) {
        SeriesKind.WEIGHT -> c.secondary
        SeriesKind.ENERGY -> c.primary
        SeriesKind.BURN -> burnColour()
        SeriesKind.STEPS -> if (overlaid) c.tertiary else c.primary
    }
}

@Composable
private fun CustomChartData.toneOf(kind: SeriesKind, overlaid: Boolean): Color = toneOf(kind, chart.colourOf(kind), overlaid)

/** Syöty's treat share: the lighter tone of a chosen colour, else today's tertiary (old charts unchanged). */
@Composable
private fun CustomChartData.treatTone(tone: Color): Color =
    if (chart.colourOf(SeriesKind.ENERGY) != null) tone.copy(alpha = ChartPalette.LIGHT_ALPHA) else MaterialTheme.colorScheme.tertiary

/** "82 kg", "2 500 kcal", "14 000 askelta". */
@Composable
private fun axisLabels(kind: KindData): List<String> = kind.ticks.map { valueText(kind.kind, it) }

@Composable
internal fun valueText(kind: SeriesKind, value: Double): String = when (kind) {
    SeriesKind.WEIGHT -> stringResource(R.string.kg_value, formatAxisKg(value))
    SeriesKind.ENERGY, SeriesKind.BURN -> stringResource(R.string.food_kcal, formatKcal(value))
    SeriesKind.STEPS -> stringResource(R.string.steps_value, formatSteps(value.toLong()))
}

@Composable
private fun emptyText(kind: SeriesKind): String = stringResource(
    when (kind) {
        SeriesKind.WEIGHT -> R.string.chart_empty_weight
        SeriesKind.ENERGY -> R.string.chart_empty_energy
        SeriesKind.BURN -> R.string.chart_empty_burn
        SeriesKind.STEPS -> R.string.chart_empty_steps
    },
)

/**
 * Draws one kind's bars or line into the scope; weight bars rise from the scale's bottom (Q16);
 * Kulutus bars split into lepo and aktiivinen when [burnSplit] (Allekkain), else one tone.
 */
private fun DrawScope.drawKind(kind: KindData, axis: ChartAxis, color: Color, treat: Color?, slot: Int = 0, slots: Int = 1, linesToo: Boolean = true, barsToo: Boolean = true, burnSplit: Boolean = false, chosen: LocalDate? = null) {
    val values = kind.values.orEmpty()
    fun y(v: Double) = size.height * (1 - kind.fraction(v))
    if (kind.style == SeriesStyle.BARS) {
        if (!barsToo) return
        if (kind.kind == SeriesKind.BURN && burnSplit) {
            drawBurnBars(axis, values.map { BurnBarSpec(it.day, 1, it.value, it.active) }, ::y, color, slot, slots, chosen = chosen)
        } else {
            drawBars(axis, values.map { BarSpec(it.day, 1, it.value, it.treat) }, ::y, color, treat, slot, slots, chosen = chosen)
        }
    } else if (linesToo) {
        drawValueLine(axis, values.map { it.day to it.value }, ::y, color, dots = kind.kind == SeriesKind.WEIGHT, chosen = chosen)
    }
}

/** The flat dashed average line of [kind] on its own scale (Keskiarvo on; nothing without an average). */
private fun DrawScope.drawKindAverage(kind: KindData, color: Color) {
    val average = kind.average ?: return
    drawAverageLine(size.height * (1 - kind.fraction(average)), color)
}

@Composable
private fun ColumnScope.Stacked(data: CustomChartData, mode: ChartMode, states: Map<SeriesKind, PanelState>, crosshair: LocalDate?, onDay: (LocalDate) -> Unit, single: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    val card = mode.card
    val labels = card == null
    val labelWidth = if (labels) axisLabelWidth(data.drawn.flatMap { axisLabels(it) }) else 0.dp
    val panels = if (single) data.drawn.ifEmpty { data.kinds.take(1) } else data.kinds
    val height = panelHeight(panels.size)
    panels.forEach { kind ->
        val panel = Modifier.fillMaxWidth().then(if (card != null) Modifier.weight(1f).heightIn(min = MIN_CARD_PANEL) else Modifier.height(height))
        Row(panel.testTag("panel-${kind.kind.name}").padding(top = if (labels) 4.dp else 2.dp)) {
            if (labels) {
                if (kind.drawn) {
                    YAxisLabels(axisLabels(kind), listOf(0f, 0.5f, 1f), labelWidth, if (card == null) height - 4.dp else 0.dp, "axis-${kind.kind.name}")
                } else {
                    Spacer(Modifier.width(labelWidth))
                }
            }
            Box(Modifier.weight(1f).fillMaxHeight()) {
                if (kind.drawn) {
                    val tone = data.toneOf(kind.kind, overlaid = false)
                    val treat = if (kind.kind == SeriesKind.ENERGY && kind.style == SeriesStyle.BARS) data.treatTone(tone) else null
                    val chosen = crosshair.takeIf { mode == ChartMode.Tab }
                    Plot(data.axis, mode, crosshair, onDay, Modifier.fillMaxSize().testTag("plot-${kind.kind.name}")) {
                        drawGuidelines(colors.outlineVariant, kind.ticks.map { size.height * (1 - kind.fraction(it)) })
                        drawKind(kind, data.axis, tone, treat, linesToo = false, burnSplit = true, chosen = chosen)
                        if (data.chart.showAverage) drawKindAverage(kind, colors.onSurfaceVariant)
                        drawKind(kind, data.axis, tone, treat, barsToo = false, chosen = chosen)
                    }
                    if (card == CardSize.BIG) ScaleTopLabel(axisLabels(kind).last(), "scaleTop-${kind.kind.name}")
                } else {
                    PanelMessage(kind, states[kind.kind] ?: PanelState.Ready, compact = card != null)
                }
            }
        }
    }
    DatesAndLegend(data, mode, labelWidth, overlaid = false)
    if (single && card == null) NotDrawnLines(data, states, except = panels)
}

/** The overlaid layout's lines under the plot for data that is not drawn (with the grant button). */
@Composable
private fun NotDrawnLines(data: CustomChartData, states: Map<SeriesKind, PanelState>, except: List<KindData> = emptyList()) {
    data.kinds.filter { !it.drawn && it !in except }.forEach { kind ->
        PanelMessage(kind, states[kind.kind] ?: PanelState.Ready, compact = false, line = true)
    }
}

@Composable
private fun ColumnScope.Overlaid(data: CustomChartData, mode: ChartMode, states: Map<SeriesKind, PanelState>, crosshair: LocalDate?, onDay: (LocalDate) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val card = mode.card
    val drawn = data.drawn
    val left = drawn[0]
    val right = drawn[1]
    val labels = card == null
    val leftLabels = axisLabels(left)
    val rightLabels = axisLabels(right)
    val leftWidth = if (labels) axisLabelWidth(leftLabels) else 0.dp
    val rightWidth = if (labels) axisLabelWidth(rightLabels) else 0.dp
    val tones = drawn.associate { it.kind to data.toneOf(it.kind, overlaid = true) }
    val plot = Modifier.fillMaxWidth().then(if (card != null) Modifier.weight(1f).heightIn(min = MIN_CARD_PANEL) else Modifier.height(OVERLAID_PLOT))
    Row(plot.padding(top = 4.dp).testTag("plot-overlaid")) {
        if (labels) YAxisLabels(leftLabels, listOf(0f, 0.5f, 1f), leftWidth, OVERLAID_PLOT - 4.dp, "axisLeft-${left.kind.name}", tones.getValue(left.kind))
        Box(Modifier.weight(1f).fillMaxHeight()) {
            val bars = drawn.filter { it.style == SeriesStyle.BARS }
            val chosen = crosshair.takeIf { mode == ChartMode.Tab }
            Plot(data.axis, mode, crosshair, onDay, Modifier.fillMaxSize().testTag("plot-OVERLAID")) {
                drawGuidelines(colors.outlineVariant, listOf(0f, 0.5f, 1f).map { size.height * (1 - it) })
                bars.forEachIndexed { i, kind -> drawKind(kind, data.axis, tones.getValue(kind.kind), null, i, bars.size, chosen = chosen) }
                if (data.chart.showAverage) drawn.forEach { drawKindAverage(it, tones.getValue(it.kind)) }
                drawn.filter { it.style == SeriesStyle.LINE }.forEach { drawKind(it, data.axis, tones.getValue(it.kind), null, chosen = chosen) }
            }
            if (card == CardSize.BIG) {
                ScaleTopLabel(leftLabels.last(), "scaleTop-${left.kind.name}", Modifier.align(Alignment.TopStart), tones.getValue(left.kind))
                ScaleTopLabel(rightLabels.last(), "scaleTop-${right.kind.name}", Modifier.align(Alignment.TopEnd), tones.getValue(right.kind))
            }
        }
        if (labels) YAxisLabels(rightLabels, listOf(0f, 0.5f, 1f), rightWidth, OVERLAID_PLOT - 4.dp, "axisRight-${right.kind.name}", tones.getValue(right.kind), end = true)
    }
    DatesAndLegend(data, mode, leftWidth, overlaid = true, endPad = rightWidth)
    // Data that is not drawn is named under the plot (on a card left out silently).
    if (card == null) NotDrawnLines(data, states)
}

/** The 3 dates (tab: 4-5 by fit) under the plot and the legend; a small card has neither, wide and big only first and last day. */
@Composable
private fun DatesAndLegend(data: CustomChartData, mode: ChartMode, start: Dp, overlaid: Boolean, endPad: Dp = 0.dp) {
    val card = mode.card
    if (card == CardSize.SMALL) return
    val colors = MaterialTheme.colorScheme
    if (card == null) {
        Row(Modifier.fillMaxWidth().padding(top = 4.dp, end = endPad)) {
            Spacer(Modifier.width(start))
            DateAxis(data.axis.from, data.axis.to, "chartDates", Modifier.weight(1f))
        }
    } else {
        val small = MaterialTheme.typography.labelSmall
        Row(Modifier.fillMaxWidth().padding(top = 2.dp).testTag("chartDates")) {
            Text(formatShortDay(data.axis.from), style = small, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text(formatShortDay(data.axis.to), style = small, color = colors.onSurfaceVariant)
        }
    }
    val weight = stringResource(R.string.legend_weight)
    val food = stringResource(R.string.energy_legend_food)
    val treat = stringResource(R.string.energy_legend_treat)
    val steps = stringResource(R.string.steps_legend)
    val burn = stringResource(R.string.burn_legend)
    val rest = stringResource(R.string.burn_legend_rest)
    val active = stringResource(R.string.burn_legend_active)
    val whole = stringResource(R.string.burn_legend_total)
    val entries = data.drawn.flatMap { kind ->
        val line = kind.style == SeriesStyle.LINE
        val tone = data.toneOf(kind.kind, overlaid)
        when (kind.kind) {
            SeriesKind.WEIGHT -> listOf(LegendEntry(weight, tone, line))
            SeriesKind.ENERGY -> if (!overlaid && !line) listOf(LegendEntry(food, tone), LegendEntry(treat, data.treatTone(tone))) else listOf(LegendEntry(food, tone, line))
            SeriesKind.BURN -> if (!overlaid && !line) {
                listOfNotNull(
                    LegendEntry(rest, tone.copy(alpha = ChartPalette.LIGHT_ALPHA)),
                    LegendEntry(active, tone),
                    LegendEntry(whole, tone.copy(alpha = ChartPalette.TOTAL_ALPHA)).takeIf { kind.values.orEmpty().any { it.active == null } },
                )
            } else {
                listOf(LegendEntry(burn, tone, line))
            }
            SeriesKind.STEPS -> listOf(LegendEntry(steps, tone, line))
        }
    }
    val average = stringResource(R.string.energy_legend_average)
    val withAverage = if (data.chart.showAverage && data.drawn.any { it.average != null }) {
        entries + LegendEntry(average, colors.onSurfaceVariant, dashed = true)
    } else {
        entries
    }
    ChartLegend(withAverage, "chartLegend")
}

/** A plot area; on a tab its gestures place the crosshair, and the line is drawn over it. */
@Composable
private fun Plot(axis: ChartAxis, mode: ChartMode, crosshair: LocalDate?, onDay: (LocalDate) -> Unit, modifier: Modifier, draw: DrawScope.() -> Unit) {
    val line = MaterialTheme.colorScheme.onSurface
    val gestures = if (mode == ChartMode.Tab) Modifier.crosshairGestures(axis, onDay) else Modifier
    Canvas(modifier.clipToBounds().then(gestures)) {
        draw()
        if (crosshair != null && mode == ChartMode.Tab) drawCrosshair(axis, crosshair, line)
    }
}

/** The top of a card's scale with its unit (big cards). */
@Composable
private fun ScaleTopLabel(text: String, tag: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        maxLines = 1,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f))
            .padding(horizontal = 2.dp)
            .testTag(tag),
    )
}

/**
 * A kind without values: the centred line in its panel (tab), a short line (card), or a line under
 * the overlaid plot ([line]); a missing permission with its grant button, loading a spinner.
 */
@Composable
private fun PanelMessage(kind: KindData, state: PanelState, compact: Boolean, line: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    val style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodyMedium
    val tag = "panelMessage-${kind.kind.name}"
    val box = if (line) Modifier.fillMaxWidth().padding(top = 4.dp) else Modifier.fillMaxSize()
    Box(box.testTag(tag), contentAlignment = if (line) Alignment.CenterStart else Alignment.Center) {
        when (state) {
            PanelState.Loading -> CircularProgressIndicator(Modifier.size(if (compact) 16.dp else 24.dp))
            is PanelState.Failed -> Text(state.text, style = style, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
            is PanelState.Gated -> if (compact) {
                Text(stringResource(R.string.chart_card_permission), style = style, color = colors.onSurfaceVariant)
            } else if (line) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(state.text, style = style, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
                    state.buttons.forEach { (label, onClick) -> TextButton(onClick = onClick, modifier = Modifier.testTag("grant-${kind.kind.name}")) { Text(label) } }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(state.text, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    Row {
                        state.buttons.forEach { (label, onClick) -> TextButton(onClick = onClick, modifier = Modifier.testTag("grant-${kind.kind.name}")) { Text(label) } }
                    }
                }
            }
            PanelState.Ready -> Text(
                if (compact) stringResource(R.string.chart_card_none) else emptyText(kind.kind),
                style = style,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun Message(text: String, tag: String, modifier: Modifier) {
    Box(modifier.testTag(tag), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}
