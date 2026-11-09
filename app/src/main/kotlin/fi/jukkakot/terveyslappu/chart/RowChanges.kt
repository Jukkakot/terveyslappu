package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.weight.WeightEntry

/**
 * Each weight's change from the weight just before it in time, by entry id (Historia list rows).
 * The oldest weight has none. Works in any input order.
 */
fun changesFromPrevious(entries: List<WeightEntry>): Map<String, Double> =
    entries.sortedBy { it.time }
        .zipWithNext { before, after -> after.id to after.kg - before.kg }
        .toMap()
