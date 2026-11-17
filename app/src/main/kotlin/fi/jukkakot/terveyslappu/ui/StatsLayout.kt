package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.chart.QuantityTone

/**
 * Which statistic goes where on each fixed Historia tab (weight-chart "Statistics layout"): one
 * hero figure (with a second figure at its right on Paino and BMI) and one row of the rest.
 */
internal object StatsLayout {
    enum class Key { CHANGE, RATE, LATEST, AVERAGE, MIN, MAX, COUNT, TREATS, DAYS, ACTIVE_AVERAGE }

    enum class Tab { WEIGHT, BMI, FOOD, STEPS, BURN }

    data class Layout(val hero: Key, val right: Key?, val row: List<Key>)

    fun of(tab: Tab): Layout = when (tab) {
        Tab.WEIGHT -> Layout(Key.CHANGE, Key.RATE, listOf(Key.AVERAGE, Key.MIN, Key.MAX, Key.COUNT))
        Tab.BMI -> Layout(Key.LATEST, Key.CHANGE, listOf(Key.AVERAGE, Key.MIN, Key.MAX, Key.COUNT))
        Tab.FOOD -> Layout(Key.AVERAGE, null, listOf(Key.TREATS, Key.DAYS))
        Tab.STEPS -> Layout(Key.AVERAGE, null, listOf(Key.DAYS))
        Tab.BURN -> Layout(Key.AVERAGE, null, listOf(Key.ACTIVE_AVERAGE, Key.DAYS))
    }

    /** A value that cannot be shown. */
    const val NONE = "—"
}

/**
 * One shown statistic: its name, its number and unit, the change arrow ([direction], 0 none) and an
 * optional tone for the unit (BMI's class word).
 */
internal data class Stat(
    val label: String,
    val value: String,
    val unit: String? = null,
    val direction: Int = 0,
    val unitColor: Color = Color.Unspecified,
    val valueColor: Color = Color.Unspecified,
)

/**
 * The hero figure: a card tinted with the tab's quantity colour (14 % dark, 10 % light, never by the
 * value), the muted name over a big number, and on Paino and BMI a second figure at its right.
 */
@Composable
internal fun HeroStat(hero: Stat, tone: QuantityTone, right: Stat? = null, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val tint = quantityColour(tone).copy(alpha = if (isDarkTheme()) 0.14f else 0.10f).compositeOver(cardColour())
    Row(
        modifier.fillMaxWidth().clip(groupShape).background(tint).padding(horizontal = 16.dp, vertical = 12.dp).testTag("heroStat"),
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(Modifier.weight(1f)) {
            Text(hero.label, style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
            BigNumber(
                hero.value,
                hero.unit,
                tag = "heroValue",
                base = MaterialTheme.typography.displaySmall,
                unitColor = hero.unitColor,
                leading = if (hero.direction != 0) {
                    { ArrowIcon(hero.direction, 28.dp, Modifier.align(Alignment.CenterVertically).padding(end = 4.dp)) }
                } else {
                    null
                },
            )
        }
        if (right != null) {
            Column(Modifier.padding(start = 12.dp), horizontalAlignment = Alignment.End) {
                Text(right.label, style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
                ChangeLabel(listOfNotNull(right.value, right.unit).joinToString(" "), right.direction, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, singleLine = true)
            }
        }
    }
}

@Composable
private fun ArrowIcon(direction: Int, size: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier) {
    val icon = if (direction < 0) R.drawable.ic_arrow_down else R.drawable.ic_arrow_up
    Icon(painterResource(icon), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = modifier.size(size).testTag(if (direction < 0) "arrowDown" else "arrowUp"))
}

/**
 * The other statistics in one row: equal columns, each a muted name over its value, thin dividers
 * between them; past four values the row wraps.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun StatRow(stats: List<Stat>, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val box = modifier.fillMaxWidth().clip(groupShape).background(cardColour()).padding(vertical = 12.dp).testTag("statRow")
    @Composable
    fun Item(stat: Stat, cell: Modifier) {
        Column(cell.padding(horizontal = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stat.label, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 1)
            ChangeLabel(
                listOfNotNull(stat.value, stat.unit).joinToString(" "),
                stat.direction,
                style = MaterialTheme.typography.titleSmall,
                color = stat.valueColor,
                fontWeight = FontWeight.SemiBold,
                singleLine = true,
            )
        }
    }
    if (stats.size <= 4) {
        Row(box.height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
            stats.forEachIndexed { i, stat ->
                if (i > 0) VerticalDivider(Modifier.fillMaxHeight().padding(vertical = 2.dp), color = colors.outlineVariant)
                Item(stat, Modifier.weight(1f))
            }
        }
    } else {
        FlowRow(box, maxItemsInEachRow = 4, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            stats.forEach { Item(it, Modifier.weight(1f)) }
        }
    }
}

/** The hero and the row under a tab's chart, 16 dp from the edges. */
@Composable
internal fun TabStats(hero: Stat, tone: QuantityTone, right: Stat?, row: List<Stat>, tag: String) {
    Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp).testTag(tag)) {
        HeroStat(hero, tone, right)
        Spacer(Modifier.height(8.dp).width(0.dp))
        StatRow(row)
    }
}
