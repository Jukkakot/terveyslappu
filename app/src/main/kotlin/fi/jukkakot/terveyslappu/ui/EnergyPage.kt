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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.chart.AxisTicks
import fi.jukkakot.terveyslappu.chart.ChartAxis
import fi.jukkakot.terveyslappu.chart.TabCharts
import fi.jukkakot.terveyslappu.food.EnergyDay
import fi.jukkakot.terveyslappu.food.EnergySeries
import java.time.LocalDate
import kotlin.math.max

/**
 * The Energia tab (weight-chart "Energia chart", "Energia statistics"): kcal bars with the treats'
 * share stacked, the 7-day average line, and under them the period's weights on the same days.
 */
@Composable
internal fun EnergyPage(series: EnergySeries, crosshair: LocalDate? = null, onDay: (LocalDate?) -> Unit = {}) {
    Column(Modifier.fillMaxWidth().testTag("page-ENERGY")) {
        Box(
            Modifier
                .padding(start = 16.dp, end = 16.dp, top = 12.dp)
                .clip(groupShape)
                .background(cardColour())
                .padding(12.dp),
        ) {
            if (series.bars.isEmpty()) {
                Text(
                    stringResource(R.string.energy_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth().height(180.dp).padding(16.dp).testTag("energyEmpty"),
                )
            } else {
                EnergyChart(series, crosshair, onDay)
            }
        }
        if (series.bars.isNotEmpty()) EnergyStatsTiles(series)
    }
}

@Composable
private fun EnergyChart(series: EnergySeries, crosshair: LocalDate?, onDay: (LocalDate?) -> Unit) {
    val today = LocalDate.now()
    val spec = remember(series, today) { TabCharts.energy(series, today) }
    // The marker card on a tap or drag (weight-chart "Crosshair on every Historia chart"); a day opens on Ruoka from the day list.
    Column(Modifier.fillMaxWidth()) {
        RangeLine(series.from, series.to, "energyRange")
        HistoryChart(spec, crosshair, onDay, stringResource(R.string.energy_chart_description, series.bars.size), tag = "energyChart", plotTag = { _, _ -> "energyBars" })
    }
}

@Composable
private fun EnergyStatsTiles(series: EnergySeries) {
    val s = series.stats
    val tiles = listOf(
        R.string.energy_stat_average to (s.averageKcal?.let { stringResource(R.string.energy_per_day, formatKcal(it)) } ?: "–"),
        R.string.energy_stat_treats to (s.averageTreat?.let { stringResource(R.string.energy_per_day, formatKcal(it)) } ?: "–"),
        R.string.energy_stat_days to stringResource(R.string.energy_days, s.loggedDays, s.periodDays),
    )
    val treatTone = (s.averageTreat ?: 0) > 0
    Row(Modifier.fillMaxWidth().padding(16.dp).testTag("energyStats"), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        tiles.forEach { (label, value) ->
            Column(
                Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceContainer).padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(stringResource(label), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (label == R.string.energy_stat_treats && treatTone) treatTextColour() else Color.Unspecified,
                )
            }
        }
    }
}

/** One logged day of the Energia list: date, total and treat share; opens Ruoka on it. */
@Composable
internal fun EnergyDayRow(day: EnergyDay, today: LocalDate, depth: Int = 0, onOpenDay: (LocalDate) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onOpenDay(day.day) }.heightIn(min = 56.dp).padding(start = indent(depth), end = 16.dp, top = 8.dp, bottom = 8.dp).testTag("energyDay-${day.day}"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(dayTitle(day.day, today), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (day.treat > 0) {
            Text(stringResource(R.string.food_day_treats, formatKcal(day.treat)), style = MaterialTheme.typography.bodyMedium, color = treatTextColour())
            Spacer(Modifier.size(12.dp))
        }
        Text(stringResource(R.string.food_kcal, formatKcal(day.total)), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}
