package fi.jukkakot.terveyslappu.chart

import java.text.BreakIterator

/** The data an own chart can show, in their drawing and listing order (weight-chart "Custom charts"). */
enum class SeriesKind { WEIGHT, ENERGY, STEPS }

/** Pylväät or Viiva. */
enum class SeriesStyle { BARS, LINE }

/** Allekkain (one panel per data) or Päällekkäin (one plot). */
enum class ChartLayout { STACKED, OVERLAID }

data class ChartSeries(val kind: SeriesKind, val style: SeriesStyle)

/**
 * One own chart (custom-charts design 1): shown as its own Historia tab and, when added, as a front
 * page card `chart:<id>`. [series] is non-empty, sorted by kind, at most one per kind.
 */
data class CustomChart(
    val id: Int,
    val name: String,
    val series: List<ChartSeries>,
    val layout: ChartLayout,
    val period: Period,
) {
    val cardId: String get() = cardId(id)

    fun has(kind: SeriesKind): Boolean = series.any { it.kind == kind }

    fun styleOf(kind: SeriesKind): SeriesStyle? = series.firstOrNull { it.kind == kind }?.style

    companion object {
        const val CARD_PREFIX = "chart:"

        fun cardId(id: Int): String = "$CARD_PREFIX$id"

        /** The chart id of a front page card id, or null for a fixed card. */
        fun idOf(cardId: String): Int? = cardId.removePrefix(CARD_PREFIX).takeIf { it != cardId }?.toIntOrNull()
    }
}

/**
 * The editor's working copy: every kind keeps a style, also while switched off (spec "Chart
 * editor": a switched-off data keeps its style while the editor is open).
 */
data class ChartDraft(
    /** 0 for a new chart. */
    val id: Int,
    val name: String,
    val on: Set<SeriesKind>,
    val styles: Map<SeriesKind, SeriesStyle>,
    val layout: ChartLayout,
    val period: Period,
) {
    val isNew: Boolean get() = id == 0

    fun styleOf(kind: SeriesKind): SeriesStyle = styles[kind] ?: CustomCharts.defaultStyle(kind)

    /** The chart this draft saves as (name trimmed); [id] is given by [CustomCharts.add] for a new one. */
    fun toChart(id: Int = this.id): CustomChart = CustomChart(
        id = id,
        name = name.trim(),
        series = SeriesKind.entries.filter { it in on }.map { ChartSeries(it, styleOf(it)) },
        layout = layout,
        period = period,
    )

    companion object {
        fun of(chart: CustomChart) = ChartDraft(
            id = chart.id,
            name = chart.name,
            on = chart.series.map { it.kind }.toSet(),
            styles = SeriesKind.entries.associateWith { chart.styleOf(it) ?: CustomCharts.defaultStyle(it) },
            layout = chart.layout,
            period = chart.period,
        )
    }
}

/** The stored charts in tab order and the id counter (ids are never reused). */
data class ChartList(val charts: List<CustomChart> = emptyList(), val next: Int = 1)

/** Pure rules of own charts (custom-charts design 1). */
object CustomCharts {
    const val MAX_NAME = 24
    val DEFAULT_PERIOD: Period = Period.MONTH

    fun defaultStyle(kind: SeriesKind): SeriesStyle = if (kind == SeriesKind.WEIGHT) SeriesStyle.LINE else SeriesStyle.BARS

    /** "Kaavio n": the lowest n that no existing name equals (trimmed, ignoring case). */
    fun defaultName(existing: List<CustomChart>): String {
        val taken = existing.map { it.name.trim().lowercase() }.toSet()
        var n = 1
        while ("kaavio $n" in taken) n++
        return "Kaavio $n"
    }

    /** A new chart: all three data on, Paino as a line, the others as bars, Allekkain, 1 kk. */
    fun newDraft(existing: List<CustomChart>): ChartDraft = ChartDraft(
        id = 0,
        name = defaultName(existing),
        on = SeriesKind.entries.toSet(),
        styles = SeriesKind.entries.associateWith(::defaultStyle),
        layout = ChartLayout.STACKED,
        period = DEFAULT_PERIOD,
    )

    /** Grapheme clusters, so an emoji counts as one. */
    fun length(text: String): Int {
        val it = BreakIterator.getCharacterInstance()
        it.setText(text)
        var count = 0
        while (it.next() != BreakIterator.DONE) count++
        return count
    }

    /** The first [max] grapheme clusters of [text] (the name field's cap). */
    fun cap(text: String, max: Int = MAX_NAME): String {
        val breaks = BreakIterator.getCharacterInstance()
        breaks.setText(text)
        var end = 0
        repeat(max) {
            val next = breaks.next()
            if (next == BreakIterator.DONE) return text
            end = next
        }
        return text.substring(0, end)
    }

    /** Trimmed, 1 to [MAX_NAME] grapheme clusters; duplicates allowed. */
    fun validName(name: String): Boolean = name.trim().let { it.isNotEmpty() && length(it) <= MAX_NAME }

    /** Switches [kind] on or off; the last one that is on stays on. */
    fun toggle(draft: ChartDraft, kind: SeriesKind): ChartDraft = when {
        kind !in draft.on -> draft.copy(on = draft.on + kind)
        draft.on.size == 1 -> draft
        else -> draft.copy(on = draft.on - kind)
    }

    fun setStyle(draft: ChartDraft, kind: SeriesKind, style: SeriesStyle): ChartDraft = draft.copy(styles = draft.styles + (kind to style))

    fun setPeriod(chart: CustomChart, period: Period): CustomChart = chart.copy(period = period)

    /** Adds [chart] last with the next id; returns the new list and the stored chart. */
    fun add(list: ChartList, chart: CustomChart): Pair<ChartList, CustomChart> {
        val stored = chart.copy(id = list.next, name = chart.name.trim())
        return ChartList(list.charts + stored, list.next + 1) to stored
    }

    fun update(list: ChartList, chart: CustomChart): ChartList =
        list.copy(charts = list.charts.map { if (it.id == chart.id) chart.copy(name = chart.name.trim()) else it })

    fun delete(list: ChartList, id: Int): ChartList = list.copy(charts = list.charts.filterNot { it.id == id })
}
