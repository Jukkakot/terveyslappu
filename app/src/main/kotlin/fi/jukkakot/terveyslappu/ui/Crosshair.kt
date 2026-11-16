package fi.jukkakot.terveyslappu.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.chart.ChartAxis
import fi.jukkakot.terveyslappu.chart.pick
import fi.jukkakot.terveyslappu.chart.shows
import java.time.LocalDate

/**
 * What stays of the hand-made crosshair once the charts moved to MPAndroidChart
 * (charts-on-mpandroidchart design 7): the removal rule on reload and the TalkBack actions. The
 * chosen day lives with the caller (HistoryScreen); on weekly bars it is the week's first day
 * inside the axis ([ChartAxis.pick]).
 */

/** A data reload keeps the choice; it is removed only when its day is no longer on the [axis]. */
@Composable
internal fun ClearOffAxis(axis: ChartAxis, crosshair: LocalDate?, onClear: () -> Unit) {
    val current by rememberUpdatedState(crosshair)
    val clear by rememberUpdatedState(onClear)
    LaunchedEffect(axis) { if (current?.let { !axis.shows(it) } == true) clear() }
}

/**
 * TalkBack's "Edellinen päivä" and "Seuraava päivä" on a chart; the first use starts from today
 * (clamped into the axis). On weekly bars a step is one week.
 */
@Composable
internal fun Modifier.crosshairActions(axis: ChartAxis, crosshair: LocalDate?, onDay: (LocalDate) -> Unit): Modifier {
    val previous = stringResource(R.string.chart_previous_day)
    val next = stringResource(R.string.chart_next_day)
    val current by rememberUpdatedState(crosshair)
    val pick by rememberUpdatedState(onDay)
    return semantics {
        fun step(days: Long): Boolean {
            val step = if (axis.weekly) days * 7 else days
            val start = current ?: LocalDate.now().clampTo(axis.from, axis.to)
            val day = if (current == null) start else start.plusDays(step).clampTo(axis.from, axis.to)
            pick(axis.pick(day))
            return true
        }
        customActions = listOf(CustomAccessibilityAction(previous) { step(-1) }, CustomAccessibilityAction(next) { step(1) })
    }
}

private fun LocalDate.clampTo(from: LocalDate, to: LocalDate): LocalDate = if (this < from) from else if (this > to) to else this
