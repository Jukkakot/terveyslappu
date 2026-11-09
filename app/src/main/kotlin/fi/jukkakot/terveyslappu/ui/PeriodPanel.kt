package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.chart.ChartData
import fi.jukkakot.terveyslappu.chart.Period
import fi.jukkakot.terveyslappu.chart.PeriodStats
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private fun Period.label(): Int = when (this) {
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

/** The period chips; Oma opens a date-range picker and then shows the picked dates. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodChips(period: Period, onPeriod: (Period) -> Unit) {
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
        val custom = period as? Period.Custom
        fun LocalDate.millis() = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        fun Long.date() = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
        val state = rememberDateRangePickerState(
            initialSelectedStartDateMillis = custom?.from?.millis(),
            initialSelectedEndDateMillis = custom?.to?.millis(),
        )
        DatePickerDialog(
            onDismissRequest = { pickRange = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val from = state.selectedStartDateMillis?.date()
                        if (from != null) onPeriod(Period.Custom(from, state.selectedEndDateMillis?.date() ?: from))
                        pickRange = false
                    },
                    enabled = state.selectedStartDateMillis != null,
                ) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { pickRange = false }) { Text(stringResource(R.string.cancel)) } },
        ) { DateRangePicker(state = state, modifier = Modifier.weight(1f)) }
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

/** Two compact lines: latest and change, then average, lowest, highest and count. */
@Composable
fun StatsRow(stats: PeriodStats) {
    val first = buildList {
        add(stringResource(R.string.stats_latest, formatKg(stats.latest)))
        stats.change?.let { add(stringResource(R.string.stats_change, formatChange(it))) }
        stats.perWeek?.let { add(stringResource(R.string.stats_rate, formatRate(it))) }
    }.joinToString(" · ")
    val second = stringResource(R.string.stats_spread, formatKg(stats.average), formatKg(stats.min), formatKg(stats.max)) +
        " · " + pluralStringResource(R.plurals.stats_count, stats.count, stats.count)
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).testTag("stats")) {
        Text(first, style = MaterialTheme.typography.bodyMedium)
        Text(second, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
