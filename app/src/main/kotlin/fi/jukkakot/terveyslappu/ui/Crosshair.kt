package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.chart.ChartAxis
import java.time.LocalDate

/**
 * The crosshair shared by every Historia chart (weight-chart "Chart crosshair", "Crosshair on every
 * Historia chart"): gestures, TalkBack actions and the readout slot. The chosen day lives with the
 * caller (HistoryScreen), which also applies the removal rules. On weekly bars ([ChartAxis.weekly])
 * the chosen day is the week's Monday.
 */

/** The chosen key for [day]: the day itself, or its week's Monday on weekly bars. */
internal fun ChartAxis.pick(day: LocalDate): LocalDate = if (weekly) weekOf(day) else day

/** Whether a chosen [day] (a Monday on weekly bars) is still on this axis. */
internal fun ChartAxis.shows(day: LocalDate): Boolean = if (weekly) !day.plusDays(6).isBefore(from) && !day.isAfter(to) else day in this

/** A data reload keeps the crosshair; it is removed only when its day is no longer on the [axis]. */
@Composable
internal fun ClearOffAxis(axis: ChartAxis, crosshair: LocalDate?, onClear: () -> Unit) {
    val current by rememberUpdatedState(crosshair)
    val clear by rememberUpdatedState(onClear)
    LaunchedEffect(axis) { if (current?.let { !axis.shows(it) } == true) clear() }
}

/**
 * The crosshair gestures (custom-charts design 10): a tap sets the day; a press held past the
 * long-press timeout without moving places it with a haptic tick and then scrubs, owning both
 * directions until release. A move past touch slop before the timeout is left to the pager and the
 * list (a swipe or fling switches the tab and places nothing).
 */
@Composable
internal fun Modifier.crosshairGestures(axis: ChartAxis, onDay: (LocalDate) -> Unit): Modifier {
    val haptic = LocalHapticFeedback.current
    val pick by rememberUpdatedState(onDay)
    return pointerInput(axis) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val slop = viewConfiguration.touchSlop
            var moved = false
            var released = false
            withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                while (true) {
                    val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                    if (!change.pressed) {
                        released = true
                        break
                    }
                    if ((change.position - down.position).getDistance() > slop) {
                        moved = true
                        break
                    }
                }
            }
            if (moved) return@awaitEachGesture
            fun dayAt(x: Float) = axis.pick(axis.dayAt(x, size.width.toFloat()))
            if (released) {
                pick(dayAt(down.position.x))
                return@awaitEachGesture
            }
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            pick(dayAt(down.position.x))
            while (true) {
                val change = awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull { it.id == down.id } ?: return@awaitEachGesture
                if (change.positionChange() != Offset.Zero) pick(dayAt(change.position.x))
                change.consume()
                if (!change.pressed) return@awaitEachGesture
            }
        }
    }
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

/**
 * The chosen day's values over the chart, or the hint; keeps its height; a tap on the text clears
 * (design 10). [action] (Syöty's "Avaa päivä ›") ends the readout as its own button.
 */
@Composable
internal fun ReadoutSlot(text: String?, onClear: () -> Unit, modifier: Modifier = Modifier, action: Pair<String, () -> Unit>? = null) {
    val colors = MaterialTheme.colorScheme
    Row(modifier.heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
                .then(if (text != null) Modifier.clickable(onClickLabel = stringResource(R.string.chart_clear), onClick = onClear) else Modifier)
                .semantics { liveRegion = LiveRegionMode.Polite }
                .testTag("chartReadout"),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text ?: stringResource(R.string.chart_hint),
                style = if (text != null) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelMedium,
                color = if (text != null) colors.onSurface else colors.onSurfaceVariant,
                modifier = Modifier.testTag(if (text != null) "chartReadoutText" else "chartHint"),
            )
        }
        if (text != null && action != null) {
            TextButton(onClick = action.second, modifier = Modifier.testTag("chartReadoutAction")) { Text(action.first) }
        }
    }
}

/** A fixed tab's readout row over its chart, the full width of the chart box. */
@Composable
internal fun TabReadout(text: String?, onClear: () -> Unit, action: Pair<String, () -> Unit>? = null) =
    ReadoutSlot(text, onClear, Modifier.fillMaxWidth(), action)
