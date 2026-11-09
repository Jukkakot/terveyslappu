package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.weight.WeightEntry
import java.time.Duration

/**
 * Each weight's change from the weight just before it in time, by entry id (Historia list rows).
 * The oldest weight has none, nor a weight more than [ChartData.MAX_GAP_DAYS] after the one before
 * it (the same pause that breaks the chart's line). Works in any input order.
 */
fun changesFromPrevious(entries: List<WeightEntry>): Map<String, Double> =
    entries.sortedBy { it.time }
        .zipWithNext { before, after ->
            val days = Duration.between(before.time, after.time).seconds / 86_400.0
            if (days > ChartData.MAX_GAP_DAYS) null else after.id to after.kg - before.kg
        }
        .filterNotNull()
        .toMap()
