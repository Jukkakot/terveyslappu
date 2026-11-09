package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.chart.ChartData
import fi.jukkakot.terveyslappu.chart.Period
import java.time.LocalDate

internal fun Period.label(): Int = when (this) {
    Period.WEEK -> R.string.period_week
    Period.MONTH -> R.string.period_month
    Period.QUARTER -> R.string.period_quarter
    Period.YEAR -> R.string.period_year
    else -> R.string.period_all
}

private fun Period.tag(): String = when (this) {
    is Period.Last -> "period-$days"
    Period.All -> "period-all"
    is Period.Custom -> "period-custom"
}

/**
 * The period chips; Oma opens the calendar with each weighed day's latest weight ([dayWeights]) and
 * then shows the picked dates. [historyMissing] explains in the calendar why old days are empty.
 */
@Composable
fun PeriodChips(
    period: Period,
    onPeriod: (Period) -> Unit,
    dayWeights: Map<LocalDate, Double> = emptyMap(),
    historyMissing: Boolean = false,
    onGrantHistory: () -> Unit = {},
) {
    var pickRange by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Period.PRESETS.forEach { preset ->
            FilterChip(
                selected = period == preset,
                onClick = { onPeriod(preset) },
                label = { Text(stringResource(preset.label())) },
                modifier = Modifier.testTag(preset.tag()),
            )
        }
        val custom = period as? Period.Custom
        FilterChip(
            selected = custom != null,
            onClick = { pickRange = true },
            label = { Text(custom?.let { "${formatShortDay(it.from)}–${formatShortDay(it.to)}" } ?: stringResource(R.string.period_custom)) },
            modifier = Modifier.testTag("period-custom"),
        )
    }
    if (pickRange) {
        RangeCalendarDialog(
            initial = period as? Period.Custom,
            dayWeights = dayWeights,
            historyMissing = historyMissing,
            onGrantHistory = onGrantHistory,
            onPick = {
                pickRange = false
                onPeriod(it)
            },
            onDismiss = { pickRange = false },
        )
    }
}

/** Shown when the period reaches further back than the app may read. */
@Composable
fun HistoryNotice(onGrant: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            stringResource(R.string.history_needed),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onGrant, modifier = Modifier.testTag("historyGrant")) { Text(stringResource(R.string.history_grant)) }
    }
}

/** The chart, or the empty-period text in the same space. */
@Composable
fun ChartArea(data: ChartData?) {
    if (data == null) {
        Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.period_empty), style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        WeightChart(data, stringResource(R.string.chart_description, data.entries.size))
    }
}
