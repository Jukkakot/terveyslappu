package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.chart.Period
import fi.jukkakot.terveyslappu.chart.RangePick
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val finnish = Locale.forLanguageTag("fi")
private val monthTitle = DateTimeFormatter.ofPattern("LLLL yyyy", finnish)
private val dayName = DateTimeFormatter.ofPattern("d. MMMM yyyy", finnish)

/** The Oma calendar in a dialog (weight-chart "Days with weights in the calendar"). */
@Composable
fun RangeCalendarDialog(
    initial: Period.Custom?,
    dayWeights: Map<LocalDate, Double>,
    historyMissing: Boolean,
    onGrantHistory: () -> Unit,
    onPick: (Period.Custom) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
        ) {
            RangeCalendarContent(initial, dayWeights, historyMissing, onGrantHistory, onPick, onDismiss)
        }
    }
}

/** The calendar without the dialog window, so tests can drive it. */
@Composable
internal fun RangeCalendarContent(
    initial: Period.Custom?,
    dayWeights: Map<LocalDate, Double>,
    historyMissing: Boolean,
    onGrantHistory: () -> Unit,
    onPick: (Period.Custom) -> Unit,
    onDismiss: () -> Unit,
    today: LocalDate = LocalDate.now(),
) {
    var pick by remember { mutableStateOf(initial?.let { RangePick(it.from, it.to) } ?: RangePick()) }
    val thisMonth = YearMonth.from(today)
    // From the oldest weight's month, at least 12 months back, to this month.
    val oldest = dayWeights.keys.minOrNull()?.let { YearMonth.from(it) }
    val startMonth = listOfNotNull(oldest, thisMonth.minusMonths(12)).min()
    val opening = initial?.to?.let { YearMonth.from(it) }?.coerceIn(startMonth, thisMonth) ?: thisMonth
    val state = rememberCalendarState(startMonth = startMonth, endMonth = thisMonth, firstVisibleMonth = opening, firstDayOfWeek = DayOfWeek.MONDAY)
    val scope = rememberCoroutineScope()
    val shown = state.firstVisibleMonth.yearMonth
    // Scrolls when large text makes it taller than the screen, so OK stays reachable.
    Column(Modifier.verticalScroll(rememberScrollState()).padding(top = 16.dp, start = 12.dp, end = 12.dp, bottom = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { scope.launch { state.animateScrollToMonth(shown.minusMonths(1)) } },
                enabled = shown > startMonth,
                modifier = Modifier.testTag("prevMonth"),
            ) {
                Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = stringResource(R.string.calendar_prev), modifier = Modifier.rotate(180f))
            }
            Text(
                monthTitle.format(shown).replaceFirstChar { it.titlecase(finnish) },
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f).testTag("monthTitle"),
            )
            IconButton(
                onClick = { scope.launch { state.animateScrollToMonth(shown.plusMonths(1)) } },
                enabled = shown < thisMonth,
                modifier = Modifier.testTag("nextMonth"),
            ) {
                Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = stringResource(R.string.calendar_next))
            }
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            DayOfWeek.entries.forEach {
                Text(
                    it.getDisplayName(TextStyle.SHORT_STANDALONE, finnish),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        HorizontalCalendar(
            state = state,
            dayContent = { day -> DayCell(day, dayWeights[day.date], pick, today) { pick = pick.tap(day.date) } },
        )
        if (historyMissing) HistoryNotice(onGrantHistory)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("calendarCancel")) { Text(stringResource(R.string.cancel)) }
            TextButton(
                onClick = { pick.result()?.let { (from, to) -> onPick(Period.Custom(from, to)) } },
                enabled = pick.start != null,
                modifier = Modifier.testTag("calendarOk"),
            ) { Text(stringResource(R.string.ok)) }
        }
    }
}

/** One day: its number and, when weighed, the day's latest weight; the range in the theme colours. */
@Composable
private fun DayCell(day: CalendarDay, kg: Double?, pick: RangePick, today: LocalDate, onTap: () -> Unit) {
    if (day.position != DayPosition.MonthDate) {
        Box(Modifier.fillMaxWidth().height(52.dp))
        return
    }
    val colors = MaterialTheme.colorScheme
    val date = day.date
    val future = date.isAfter(today)
    val end = date == pick.start || date == pick.end
    val between = !end && pick.contains(date)
    val text = when {
        end -> colors.onPrimary
        between -> colors.onPrimaryContainer
        else -> colors.onSurface
    }
    val label = buildString {
        append(dayName.format(date))
        kg?.let { append(", ${formatKg(it)} kg") }
    }
    // The weight stays small at any text size so "100,0" fits a seventh of the width.
    val small = with(LocalDensity.current) { 10.dp.toSp() }
    Box(
        Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(vertical = 2.dp)
            .semantics {
                contentDescription = label
                selected = end || between
                if (future) disabled()
            }
            .testTag("day-$date"),
        contentAlignment = Alignment.Center,
    ) {
        if (between) Box(Modifier.fillMaxWidth().height(48.dp).background(colors.primaryContainer))
        // The band runs on behind the start and end circles, so the range reads as one piece.
        val range = pick.end != null && pick.start != pick.end
        if (range && end) {
            Row(Modifier.fillMaxWidth().height(48.dp)) {
                val left = date == pick.end
                Box(Modifier.weight(1f).fillMaxHeight().then(if (left) Modifier.background(colors.primaryContainer) else Modifier))
                Box(Modifier.weight(1f).fillMaxHeight().then(if (!left) Modifier.background(colors.primaryContainer) else Modifier))
            }
        }
        Column(
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .then(if (end) Modifier.background(colors.primary) else Modifier)
                .then(if (date == today && !end) Modifier.border(1.dp, colors.primary, CircleShape) else Modifier)
                .then(if (future) Modifier else Modifier.clickable(onClick = onTap)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                "${date.dayOfMonth}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (kg != null) FontWeight.SemiBold else null,
                color = if (future) colors.onSurface.copy(alpha = 0.38f) else text,
            )
            if (kg != null) {
                Text(
                    formatKg(kg),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = small, lineHeight = small),
                    color = if (end) colors.onPrimary else if (between) colors.onPrimaryContainer else colors.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier.testTag("dayKg-$date"),
                )
            }
        }
    }
}
