package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
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
 * The periods as one row of six equal segments (weight-chart "Periods"): a rounded track with the
 * chosen segment filled in the raised tone and its label in the brand colour; one selectable group
 * for TalkBack. Oma reads "Oma" also when chosen (the dates show in the range line) and opens the
 * calendar on every tap, with each weighed day's latest weight ([dayWeights]); [historyMissing]
 * explains in the calendar why old days are empty.
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
    val phone = LocalAppLook.current?.colours == ColourChoice.PHONE
    val track = if (phone) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer
    val chosen = if (phone) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainerHigh
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(CircleShape)
            .background(track)
            .padding(4.dp)
            .selectableGroup()
            .testTag("periods"),
    ) {
        @Composable
        fun Segment(label: String, selected: Boolean, tag: String, onClick: () -> Unit) {
            Box(
                Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(if (selected) chosen else Color.Transparent)
                    .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
                    .heightIn(min = 44.dp)
                    .padding(horizontal = 2.dp)
                    .testTag(tag),
                contentAlignment = Alignment.Center,
            ) {
                val style = MaterialTheme.typography.labelLarge
                BasicText(
                    label,
                    style = style.copy(
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    ),
                    maxLines = 1,
                    softWrap = false,
                    autoSize = TextAutoSize.StepBased(minFontSize = 9.sp, maxFontSize = style.fontSize, stepSize = 0.5.sp),
                )
            }
        }
        Period.PRESETS.forEach { preset ->
            Segment(stringResource(preset.label()), period == preset, preset.tag()) { onPeriod(preset) }
        }
        Segment(stringResource(R.string.period_custom), period is Period.Custom, "period-custom") { pickRange = true }
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
fun ChartArea(data: ChartData?, crosshair: LocalDate? = null, onDay: (LocalDate?) -> Unit = {}) {
    if (data == null) {
        Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.period_empty), style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        WeightChart(data, stringResource(R.string.chart_description, data.entries.size), crosshair = crosshair, onDay = onDay)
    }
}
