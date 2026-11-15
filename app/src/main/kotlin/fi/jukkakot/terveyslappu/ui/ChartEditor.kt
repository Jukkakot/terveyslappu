package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.chart.ChartDraft
import fi.jukkakot.terveyslappu.chart.ChartLayout
import fi.jukkakot.terveyslappu.chart.CustomChart
import fi.jukkakot.terveyslappu.chart.CustomCharts
import fi.jukkakot.terveyslappu.chart.Period
import fi.jukkakot.terveyslappu.chart.SeriesKind
import fi.jukkakot.terveyslappu.chart.SeriesStyle
import fi.jukkakot.terveyslappu.chart.encode

/** Which editor is open: a new chart (id 0) or an existing one; null = closed. Saveable as an Int. */
internal val EditorTargetSaver = Saver<MutableState<Int?>, Int>(save = { it.value ?: -1 }, restore = { mutableStateOf(it.takeIf { v -> v >= 0 }) })

/** The editor's local draft through rotation (design 12). */
private val DraftSaver = Saver<MutableState<ChartDraft>, List<String>>(
    save = { s ->
        val d = s.value
        listOf(d.id.toString(), d.name, d.on.joinToString(",") { it.name }, SeriesKind.entries.joinToString(",") { d.styleOf(it).name }, d.layout.name, d.period.encode(), d.showAverage.toString())
    },
    restore = { l ->
        mutableStateOf(
            ChartDraft(
                id = l[0].toInt(),
                name = l[1],
                on = l[2].split(',').filter { it.isNotEmpty() }.map { SeriesKind.valueOf(it) }.toSet(),
                styles = SeriesKind.entries.zip(l[3].split(',').map { SeriesStyle.valueOf(it) }).toMap(),
                layout = ChartLayout.valueOf(l[4]),
                period = Period.decode(l[5]),
                showAverage = l.getOrNull(6) == "true",
            ),
        )
    },
)

@Composable
private fun kindName(kind: SeriesKind): String = stringResource(
    when (kind) {
        SeriesKind.WEIGHT -> R.string.quantity_weight
        SeriesKind.ENERGY -> R.string.quantity_energy
        SeriesKind.STEPS -> R.string.quantity_steps
    },
)

/**
 * The chart editor (weight-chart "Chart editor", custom-charts design 12): name, the three data rows,
 * Asettelu, Peruuta / Tallenna and, for an existing chart, Poista kaavio (deletes at once; the caller offers Kumoa).
 * Back and an outside tap discard without asking. The period is not here; saving keeps it.
 */
@Composable
internal fun ChartEditor(
    existing: CustomChart?,
    charts: List<CustomChart>,
    onSave: (CustomChart) -> Unit,
    onDelete: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var draft by rememberSaveable(existing?.id, saver = DraftSaver) { mutableStateOf(existing?.let { ChartDraft.of(it) } ?: CustomCharts.newDraft(charts)) }
    val valid = CustomCharts.validName(draft.name)
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth().testTag("chartEditor"),
        ) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(if (draft.isNew) R.string.chart_new else R.string.chart_edit_title), style = MaterialTheme.typography.headlineSmall)
                OutlinedTextField(
                    value = draft.name,
                    onValueChange = { draft = draft.copy(name = CustomCharts.cap(it)) },
                    label = { Text(stringResource(R.string.chart_name)) },
                    singleLine = true,
                    isError = !valid,
                    supportingText = if (!valid) ({ Text(stringResource(R.string.chart_name_missing)) }) else null,
                    modifier = Modifier.fillMaxWidth().testTag("chartName"),
                )
                SeriesKind.entries.forEach { kind ->
                    val on = kind in draft.on
                    Column(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(kindName(kind), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            Switch(checked = on, onCheckedChange = { draft = CustomCharts.toggle(draft, kind) }, modifier = Modifier.testTag("chartOn-${kind.name}"))
                        }
                        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                            SeriesStyle.entries.forEachIndexed { i, style ->
                                SegmentedButton(
                                    selected = draft.styleOf(kind) == style,
                                    onClick = { draft = CustomCharts.setStyle(draft, kind, style) },
                                    enabled = on,
                                    shape = SegmentedButtonDefaults.itemShape(i, SeriesStyle.entries.size),
                                    modifier = Modifier.testTag("chartStyle-${kind.name}-${style.name}"),
                                ) { Text(stringResource(if (style == SeriesStyle.BARS) R.string.chart_style_bars else R.string.chart_style_line)) }
                            }
                        }
                    }
                }
                Text(stringResource(R.string.chart_layout), style = MaterialTheme.typography.titleSmall)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    ChartLayout.entries.forEachIndexed { i, layout ->
                        SegmentedButton(
                            selected = draft.layout == layout,
                            onClick = { draft = draft.copy(layout = layout) },
                            shape = SegmentedButtonDefaults.itemShape(i, ChartLayout.entries.size),
                            modifier = Modifier.testTag("chartLayout-${layout.name}"),
                        ) { Text(stringResource(if (layout == ChartLayout.STACKED) R.string.chart_layout_stacked else R.string.chart_layout_overlaid)) }
                    }
                }
                Text(
                    stringResource(if (draft.layout == ChartLayout.STACKED) R.string.chart_layout_stacked_line else R.string.chart_layout_overlaid_line),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.chart_average), style = MaterialTheme.typography.bodyLarge)
                        Text(stringResource(R.string.chart_average_line), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = draft.showAverage, onCheckedChange = { draft = CustomCharts.setShowAverage(draft, it) }, modifier = Modifier.testTag("chartAverage"))
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    if (existing != null) {
                        TextButton(onClick = { onDelete(existing.id) }, modifier = Modifier.testTag("chartDelete")) {
                            Text(stringResource(R.string.chart_delete), color = MaterialTheme.colorScheme.error)
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onDismiss, modifier = Modifier.testTag("chartCancel")) { Text(stringResource(R.string.cancel)) }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { onSave(draft.toChart()) }, enabled = valid, modifier = Modifier.testTag("chartSave")) { Text(stringResource(R.string.chart_save)) }
                }
            }
        }
    }
}
