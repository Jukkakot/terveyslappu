package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.chart.GroupLevel
import fi.jukkakot.terveyslappu.chart.ListNode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val finnish = Locale.forLanguageTag("fi")
private val monthName = DateTimeFormatter.ofPattern("LLLL", finnish)

/** Rows step in by level so the hierarchy reads without lines. */
internal fun indent(depth: Int): Dp = 20.dp + 12.dp * depth

/** "3.–9.11." or across months "27.10.–2.11.", with the year when not [today]'s. */
internal fun formatDayRange(from: LocalDate, to: LocalDate, today: LocalDate): String {
    val year = if (to.year == today.year) "" else "${to.year}"
    val start = if (from.monthValue == to.monthValue && from.year == to.year) "${from.dayOfMonth}." else formatShortDay(from)
    return "$start–${formatShortDay(to)}$year"
}

/** "2024", "Marraskuu 2025", "Vk 45 · 3.–9.11.". */
internal fun groupName(group: ListNode.Group, today: LocalDate): String {
    val year = if (group.from.year == today.year) "" else " ${group.from.year}"
    return when (group.level) {
        GroupLevel.YEAR -> "${group.from.year}"
        GroupLevel.MONTH -> monthName.format(group.from).replaceFirstChar { it.titlecase(finnish) } + year
        GroupLevel.WEEK -> "Vk ${group.week} · ${formatDayRange(group.from, group.to, today)}"
    }
}

/** A group: name, first → last weight, change with the list's arrow and colours, count; tap opens or closes. */
@Composable
internal fun GroupRowItem(group: ListNode.Group, open: Boolean, today: LocalDate, depth: Int, onToggle: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val name = groupName(group, today)
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = stringResource(if (open) R.string.group_close else R.string.group_open, name), onClick = onToggle)
            .heightIn(min = 56.dp)
            .padding(start = indent(depth), end = 16.dp, top = 6.dp, bottom = 6.dp)
            .testTag("group-${group.key}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(
                if (group.count == 1) stringResource(R.string.group_value_single, formatKg(group.last.kg))
                else stringResource(R.string.group_values, formatKg(group.first.kg), formatKg(group.last.kg), group.count),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
        }
        if (group.count > 1) {
            val direction = shownDirection(group.change, 1)
            ChangeLabel(formatChange(group.change), direction, style = MaterialTheme.typography.bodyMedium, color = changeColor(direction))
        }
        Icon(
            painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(24.dp).rotate(if (open) 270f else 90f),
        )
    }
}

/** A pause over 30 days: a thin row naming the days without weights. */
@Composable
internal fun GapRowItem(gap: ListNode.Gap, today: LocalDate, depth: Int) {
    val from = if (gap.from.year == today.year && gap.to.year == today.year) formatShortDay(gap.from) else "${formatShortDay(gap.from)}${gap.from.year}"
    val to = if (gap.to.year == today.year) formatShortDay(gap.to) else "${formatShortDay(gap.to)}${gap.to.year}"
    Text(
        stringResource(R.string.group_gap, from, to),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(start = indent(depth), end = 16.dp, top = 6.dp, bottom = 6.dp).testTag("gap"),
    )
}
