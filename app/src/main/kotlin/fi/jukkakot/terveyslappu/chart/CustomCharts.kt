package fi.jukkakot.terveyslappu.chart

import java.text.BreakIterator

/** The data an own chart can show, in their drawing and listing order (weight-chart "Custom charts"): Paino, Syöty, Kulutus, Askeleet. */
enum class SeriesKind { WEIGHT, ENERGY, BURN, STEPS }

/**
 * A data's fixed colour in an own chart (weight-chart "Data colours in own charts"); null on a
 * [ChartSeries] = Oletus (the quantity colour). Shades live in `ui/ChartPalette`.
 */
enum class SeriesColour { SININEN, VIHREA, ORANSSI, PUNAINEN, VIOLETTI, TURKOOSI, KELTAINEN, HARMAA }

/** Pylväät or Viiva. */
enum class SeriesStyle { BARS, LINE }

/** Allekkain (one panel per data) or Päällekkäin (one plot). */
enum class ChartLayout { STACKED, OVERLAID }

data class ChartSeries(val kind: SeriesKind, val style: SeriesStyle, val colour: SeriesColour? = null)

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
    /** Keskiarvo: a flat dashed line per drawn data at its period average (default off). */
    val showAverage: Boolean = false,
) {
    val cardId: String get() = cardId(id)

    fun has(kind: SeriesKind): Boolean = series.any { it.kind == kind }

    fun styleOf(kind: SeriesKind): SeriesStyle? = series.firstOrNull { it.kind == kind }?.style

    fun colourOf(kind: SeriesKind): SeriesColour? = series.firstOrNull { it.kind == kind }?.colour

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
    val showAverage: Boolean = false,
    /** Every kind keeps its colour (null = Oletus), also while off. */
    val colours: Map<SeriesKind, SeriesColour?> = emptyMap(),
) {
    val isNew: Boolean get() = id == 0

    // Paino is always a line: bars of a scale that does not start at zero mislead (weight-line-only).
    fun styleOf(kind: SeriesKind): SeriesStyle =
        if (kind == SeriesKind.WEIGHT) SeriesStyle.LINE else styles[kind] ?: CustomCharts.defaultStyle(kind)

    fun colourOf(kind: SeriesKind): SeriesColour? = colours[kind]

    /** The chart this draft saves as (name trimmed); [id] is given by [CustomCharts.add] for a new one. */
    fun toChart(id: Int = this.id): CustomChart = CustomChart(
        id = id,
        name = name.trim(),
        series = SeriesKind.entries.filter { it in on }.map { ChartSeries(it, styleOf(it), colourOf(it)) },
        layout = layout,
        period = period,
        showAverage = showAverage,
    )

    companion object {
        fun of(chart: CustomChart) = ChartDraft(
            id = chart.id,
            name = chart.name,
            on = chart.series.map { it.kind }.toSet(),
            styles = SeriesKind.entries.associateWith { chart.styleOf(it) ?: CustomCharts.defaultStyle(it) },
            layout = chart.layout,
            period = chart.period,
            showAverage = chart.showAverage,
            colours = chart.series.associate { it.kind to it.colour },
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

    /** A new chart: every data but Kulutus on, Paino as a line, the others as bars, Allekkain, 1 kk, Oletus colours. */
    fun newDraft(existing: List<CustomChart>): ChartDraft = ChartDraft(
        id = 0,
        name = defaultName(existing),
        on = SeriesKind.entries.toSet() - SeriesKind.BURN,
        styles = SeriesKind.entries.associateWith(::defaultStyle),
        layout = ChartLayout.STACKED,
        period = DEFAULT_PERIOD,
        showAverage = false,
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

    fun setStyle(draft: ChartDraft, kind: SeriesKind, style: SeriesStyle): ChartDraft = if (kind == SeriesKind.WEIGHT) draft else draft.copy(styles = draft.styles + (kind to style))

    fun setColour(draft: ChartDraft, kind: SeriesKind, colour: SeriesColour?): ChartDraft = draft.copy(colours = draft.colours + (kind to colour))

    fun setShowAverage(draft: ChartDraft, on: Boolean): ChartDraft = draft.copy(showAverage = on)

    fun setPeriod(chart: CustomChart, period: Period): CustomChart = chart.copy(period = period)

    /** Adds [chart] last with the next id; returns the new list and the stored chart. */
    fun add(list: ChartList, chart: CustomChart): Pair<ChartList, CustomChart> {
        val stored = chart.copy(id = list.next, name = chart.name.trim())
        return ChartList(list.charts + stored, list.next + 1) to stored
    }

    fun update(list: ChartList, chart: CustomChart): ChartList =
        list.copy(charts = list.charts.map { if (it.id == chart.id) chart.copy(name = chart.name.trim()) else it })

    /**
     * The first day the front page reads steps from (custom-charts design 13): the steps card's start
     * or, earlier, the start of a shown chart card with steps; null (everything) for such a card on Kaikki.
     */
    fun homeStepsFrom(stepsDays: Long, shownCardIds: List<String>, charts: List<CustomChart>, today: java.time.LocalDate): java.time.LocalDate? =
        homeFrom(SeriesKind.STEPS, stepsDays, shownCardIds, charts, today)

    /** Whether the front page shows Kulutus: its card ([burnCard]) or a shown chart card with it. */
    fun homeShowsBurn(burnCard: Boolean, shownCardIds: List<String>, charts: List<CustomChart>): Boolean =
        burnCard || charts.any { it.cardId in shownCardIds && it.has(SeriesKind.BURN) }

    /** The same as [homeStepsFrom] for Kulutus: [burnDays] null when its card is hidden. */
    fun homeBurnFrom(burnDays: Long?, shownCardIds: List<String>, charts: List<CustomChart>, today: java.time.LocalDate): java.time.LocalDate? =
        homeFrom(SeriesKind.BURN, burnDays, shownCardIds, charts, today)

    private fun homeFrom(kind: SeriesKind, cardDays: Long?, shownCardIds: List<String>, charts: List<CustomChart>, today: java.time.LocalDate): java.time.LocalDate? {
        val card = cardDays?.let { today.minusDays(it - 1) }
        val starts = charts.filter { it.cardId in shownCardIds && it.has(kind) }.map { chart ->
            if (chart.period == Period.All) return null
            AxisTicks.periodDates(chart.period, today, null).first
        }
        return (starts + listOfNotNull(card)).minOrNull() ?: today
    }

    /** The period in the log (`last30`, `all`, `custom`): never dates (design 17). */
    fun logPeriod(period: Period): String = when (period) {
        is Period.Last -> "last${period.days}"
        Period.All -> "all"
        is Period.Custom -> "custom"
    }

    fun delete(list: ChartList, id: Int): ChartList = list.copy(charts = list.charts.filterNot { it.id == id })

    /** Kumoa of a delete: [chart] back at tab [index] (clamped); the id counter stays, so ids are never reused. */
    fun restore(list: ChartList, chart: CustomChart, index: Int): ChartList {
        if (list.charts.any { it.id == chart.id }) return list
        val at = index.coerceIn(0, list.charts.size)
        return list.copy(charts = list.charts.toMutableList().also { it.add(at, chart) })
    }
}
