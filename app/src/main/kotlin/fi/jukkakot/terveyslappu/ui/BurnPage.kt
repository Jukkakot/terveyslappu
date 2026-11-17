package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.burn.BurnBar
import fi.jukkakot.terveyslappu.burn.BurnDay
import fi.jukkakot.terveyslappu.burn.BurnKind
import fi.jukkakot.terveyslappu.burn.BurnSeries
import fi.jukkakot.terveyslappu.burn.BurnUiState
import fi.jukkakot.terveyslappu.chart.AxisTicks
import fi.jukkakot.terveyslappu.chart.ChartAxis
import fi.jukkakot.terveyslappu.chart.TabCharts
import androidx.compose.ui.draw.clipToBounds
import fi.jukkakot.terveyslappu.steps.StepsAccess
import fi.jukkakot.terveyslappu.steps.StepsSourceLabel
import fi.jukkakot.terveyslappu.steps.StepsUiState
import java.time.LocalDate
import kotlin.math.max

/** What the Kulutus card and tab can ask for; the activity wires these (energy-burn design 9). */
class BurnActions(
    /** Asks both calories permissions (first time, or Kysy uudelleen). */
    val onGrant: () -> Unit = {},
    /** Health Connect's permission settings. */
    val onOpenSettings: () -> Unit = {},
    /** Historia on Kulutus (the card). */
    val onOpen: () -> Unit = {},
    /** The card's own period. */
    val onPeriod: (Long) -> Unit = {},
    /** A screen shows Kulutus from this day (null: Kaikki): read it. */
    val onShown: (LocalDate?) -> Unit = {},
    /** The source line or "Vaihda lähde": Asetukset → Askeleet (design 3). */
    val onOpenSource: () -> Unit = {},
)

/**
 * The Kulutus tab (energy-burn "Kulutus tab"): the reason without access, otherwise the two-tone
 * bars, the average line, the source line and the statistics. Burned energy only.
 */
@Composable
internal fun BurnTab(burn: BurnUiState, series: BurnSeries, actions: BurnActions, crosshair: LocalDate? = null, onDay: (LocalDate?) -> Unit = {}) {
    when {
        burn.access != StepsAccess.GRANTED -> BurnReason(burn.access, actions)
        burn.days == null && burn.loadFailed -> Column {
            BurnBox { BurnMessage(stringResource(R.string.burn_read_failed), "burnFailed") }
            BurnSourceLine(burn, actions)
        }
        burn.days == null -> Column {
            Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            BurnSourceLine(burn, actions)
        }
        else -> BurnPage(series, burn, actions, crosshair, onDay)
    }
}

/** The same quiet "Lähde: …" line as Askeleet (design 3); opens Asetukset → Askeleet. */
@Composable
private fun BurnSourceLine(burn: BurnUiState, actions: BurnActions) {
    val colors = MaterialTheme.colorScheme
    val warn = (burn.source.label as? StepsSourceLabel.App)?.noRecent == true
    Box(
        Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(onClick = actions.onOpenSource).padding(horizontal = 16.dp).testTag("burnSource"),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            stepsSourceLineText(StepsUiState(source = burn.source, sourceName = burn.sourceName)),
            style = MaterialTheme.typography.labelMedium,
            color = if (warn) colors.error else colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun BurnBox(content: @Composable () -> Unit) {
    Box(
        Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp).clip(groupShape).background(cardColour()).fillMaxWidth(),
    ) { content() }
}

@Composable
private fun BurnMessage(text: String, tag: String) {
    Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth().padding(24.dp).testTag(tag))
}

/** Kulutus without access: the reason and its buttons, as steps (design 9). */
@Composable
internal fun BurnReason(access: StepsAccess, actions: BurnActions) {
    BurnBox {
        Column(
            Modifier.fillMaxWidth().padding(24.dp).testTag("burnReason"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (access) {
                StepsAccess.CHECKING -> CircularProgressIndicator()
                StepsAccess.UNAVAILABLE -> Text(stringResource(R.string.burn_unavailable), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                StepsAccess.NEEDS -> {
                    Text(stringResource(R.string.burn_reason), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                    TextButton(onClick = actions.onGrant, modifier = Modifier.testTag("burnGrant")) { Text(stringResource(R.string.food_permission_grant)) }
                }
                StepsAccess.DENIED -> {
                    Text(stringResource(R.string.burn_denied_reason), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                    TextButton(onClick = actions.onGrant, modifier = Modifier.testTag("burnAskAgain")) { Text(stringResource(R.string.steps_ask_again)) }
                    TextButton(onClick = actions.onOpenSettings, modifier = Modifier.testTag("burnSettings")) { Text(stringResource(R.string.food_permission_settings)) }
                }
                StepsAccess.GRANTED -> Unit
            }
        }
    }
}

@Composable
private fun BurnPage(series: BurnSeries, burn: BurnUiState, actions: BurnActions, crosshair: LocalDate?, onDay: (LocalDate?) -> Unit) {
    Column(Modifier.fillMaxWidth().testTag("page-BURN")) {
        BurnBox {
            val app = burn.source.appId
            if (series.bars.isEmpty() && app != null) {
                Column(Modifier.fillMaxWidth().padding(24.dp).testTag("burnEmpty"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.burn_empty_app, burn.sourceName ?: app), style = MaterialTheme.typography.bodyLarge)
                    TextButton(onClick = actions.onOpenSource, modifier = Modifier.testTag("burnChangeSource")) { Text(stringResource(R.string.steps_change_source)) }
                }
            } else if (series.bars.isEmpty()) {
                BurnMessage(stringResource(R.string.burn_empty), "burnEmpty")
            } else {
                Box(Modifier.padding(12.dp)) { BurnChart(series, crosshair, onDay) }
            }
        }
        BurnSourceLine(burn, actions)
        if (burn.stale) {
            Text(
                stringResource(R.string.steps_stale),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp).testTag("burnStale"),
            )
        }
        if (series.bars.isNotEmpty()) BurnStatsTiles(series)
    }
}

@Composable
private fun BurnChart(series: BurnSeries, crosshair: LocalDate?, onDay: (LocalDate?) -> Unit) {
    val today = LocalDate.now()
    val spec = remember(series, today) { TabCharts.burn(series, today) }
    // The marker card (energy-burn "Kulutus tab"): a day, or a week on weekly bars.
    Column(Modifier.fillMaxWidth()) {
        RangeLine(series.from, series.to, "burnRange")
        HistoryChart(spec, crosshair, onDay, stringResource(R.string.burn_chart_description, series.bars.size), tag = "burnChart", plotTag = { _, _ -> "burnBars" })
    }
}

@Composable
private fun BurnStatsTiles(series: BurnSeries) {
    val s = series.stats
    val tiles = listOf(
        R.string.energy_stat_average to (s.averageTotal?.let { stringResource(R.string.energy_per_day, formatKcal(it.toDouble())) } ?: "–"),
        R.string.burn_stat_active to (s.averageActive?.let { stringResource(R.string.energy_per_day, formatKcal(it.toDouble())) } ?: "–"),
        R.string.burn_stat_days to stringResource(R.string.energy_days, s.dataDays, s.periodDays),
    )
    Row(Modifier.fillMaxWidth().padding(16.dp).testTag("burnStats"), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        tiles.forEach { (label, value) ->
            Column(
                Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceContainer).padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(stringResource(label), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/** One day of the Kulutus list: date, total kcal and "aktiivinen N kcal"; a row opens nothing. */
@Composable
internal fun BurnDayRow(day: BurnDay, today: LocalDate, depth: Int = 0) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(start = indent(depth), end = 16.dp, top = 8.dp, bottom = 8.dp).testTag("burnDay-${day.day}"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(dayTitle(day.day, today), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text(stringResource(R.string.food_kcal, formatKcal(day.total.toDouble())), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            day.active?.let {
                Text(stringResource(R.string.burn_active_line, formatKcal(it.toDouble())), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
