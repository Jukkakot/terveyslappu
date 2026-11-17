package fi.jukkakot.terveyslappu.chart

import java.time.LocalDate

/**
 * An own chart's Historia tab as a chart definition (design 2; weight-chart "Stacked layout",
 * "Overlaid layout", "Custom charts"). Allekkain: one panel per shown data with its own scale, a
 * message panel for data without values. Päällekkäin: one plot in fraction units (0 = bottom of a
 * data's scale, 1 = top), the first two drawn data labelled left and right, bars side by side.
 * One drawn data in Päällekkäin is exactly the stacked single panel.
 */
object CustomChartSpec {
    const val OVERLAID_DP = 200

    /** Panel height per 1, 2, 3+ stacked panels. */
    fun panelHeight(count: Int): Int = when (count) {
        1 -> 160
        2 -> 136
        else -> 112
    }

    /** The bars' share of a day's slot. */
    const val BAR_SHARE = 0.7

    fun unit(kind: SeriesKind): ValueUnit = when (kind) {
        SeriesKind.WEIGHT -> ValueUnit.KG
        SeriesKind.ENERGY, SeriesKind.BURN -> ValueUnit.KCAL
        SeriesKind.STEPS -> ValueUnit.STEPS
    }

    /**
     * The data's tone (weight-chart "Data colours in own charts"): its chosen colour, else (Oletus)
     * its quantity colour.
     */
    fun tone(chart: CustomChart, kind: SeriesKind, overlaid: Boolean): ToneRef {
        chart.colourOf(kind)?.let { return ToneRef.Palette(it) }
        return when (kind) {
            SeriesKind.WEIGHT -> ToneRef.WEIGHT
            SeriesKind.ENERGY -> ToneRef.FOOD
            SeriesKind.BURN -> ToneRef.BURN
            SeriesKind.STEPS -> ToneRef.STEPS
        }
    }

    /** Syöty's treat share: the lighter tone of a chosen colour, else the herkku colour. */
    fun treatTone(chart: CustomChart, tone: ToneRef): ToneRef =
        if (chart.colourOf(SeriesKind.ENERGY) != null) tone.alpha(ToneRef.LIGHT_ALPHA) else ToneRef.TREAT

    /** Kulutus's lepo part: the lighter tone of a chosen colour, else the lepo colour. */
    fun restTone(chart: CustomChart, tone: ToneRef): ToneRef =
        if (chart.colourOf(SeriesKind.BURN) != null) tone.alpha(ToneRef.LIGHT_ALPHA) else ToneRef.REST

    /** A card's line width and dot radius (today's card look, home-cards-on-chart-library design 3). */
    const val CARD_LINE_DP = 2f
    const val CARD_DOT_DP = 3f

    /**
     * [access]: whether a kind can be read (kinds without access have no marker row). [card]: the
     * front page card (home-cards-on-chart-library design 1): no marker, the card's line and dots,
     * Päällekkäin bars spread over the whole day (each 70 % of its sub-slot, centred in it).
     */
    fun of(data: CustomChartData, today: LocalDate, card: Boolean = false, access: (SeriesKind) -> Boolean = { true }): ChartSpec {
        val chart = data.chart
        val axis = data.axis
        val overlaid = chart.layout == ChartLayout.OVERLAID && data.drawn.size > 1
        val lines = averageLines(data, today, card)
        val trends = lines.mapNotNull { (k, l) -> l.trend?.let { k to it } }.toMap()
        val readout: (LocalDate) -> ReadoutCard = if (card) {
            CardCharts.noReadout
        } else { day ->
            TabReadouts.custom(ChartReadout.of(day, data, access), today, trends, { trendTone(chart, it, overlaid) }) { tone(chart, it, overlaid) }
        }
        if (data.empty) return ChartSpec(axis, emptyList(), emptyList(), readout, empty = true)
        return if (overlaid) overlaid(data, readout, card, today, lines) else stacked(data, readout, card, today, lines)
    }

    /** A trend's tone: neutral in Allekkain, the data's tone at 60 % in Päällekkäin (period-trend-line design 5). */
    fun trendTone(chart: CustomChart, kind: SeriesKind, overlaid: Boolean): ToneRef =
        if (overlaid) tone(chart, kind, overlaid = true).alpha(SeriesSpec.TREND_ALPHA) else ToneRef.MUTED

    /**
     * Each drawn data's Keskiarvo line (period-trend-line design 4): a card always the flat average;
     * a tab the flat average or, over 31 axis days, the trend of the same daily values (today left
     * out but for Paino), clamped into the data's scale. Empty with Keskiarvo off.
     */
    fun averageLines(data: CustomChartData, today: LocalDate, card: Boolean): Map<SeriesKind, AverageLine> {
        if (!data.chart.showAverage) return emptyMap()
        return data.drawn.associate { kind ->
            val flat = kind.average?.let { AverageLine.Flat(it) } ?: AverageLine.None
            kind.kind to if (card) flat else {
                val values = kind.values.orEmpty().filter { kind.kind == SeriesKind.WEIGHT || it.day != today }.map { it.day to it.value }
                when (val line = AverageLine.of(data.axis.days, values, kind.average)) {
                    is AverageLine.Trend -> AverageLine.Trend(line.segments.map { it.clamped(kind.min, kind.max) })
                    else -> line
                }
            }
        }
    }

    private fun trendPoints(line: AverageLine?, x: ChartX, y: (Double) -> Double = { it }): List<List<ChartPoint>>? =
        TabCharts.trendPoints(line?.trend?.map { it.map(y) }, x)

    private fun stacked(data: CustomChartData, readout: (LocalDate) -> ReadoutCard, card: Boolean, today: LocalDate, lines: Map<SeriesKind, AverageLine>): ChartSpec {
        val chart = data.chart
        val single = chart.layout == ChartLayout.OVERLAID
        val kinds = if (single) data.drawn.ifEmpty { data.kinds.take(1) } else data.kinds
        val height = panelHeight(kinds.size)
        val x = ChartX(data.axis)
        val panels = kinds.map { kind ->
            if (!kind.drawn) return@map PanelSpec(height, null, message = PanelMessage(kind.kind), kind = kind.kind)
            val tone = tone(chart, kind.kind, overlaid = false)
            val values = kind.values.orEmpty()
            val average = lines[kind.kind]?.flat
            val trend = trendPoints(lines[kind.kind], x)
            val series: List<SeriesSpec> = when {
                kind.style == SeriesStyle.LINE -> lineSeries(kind, values, x, card, today, tone, Side.LEFT, average, ToneRef.MUTED, trend) { it }
                kind.kind == SeriesKind.ENERGY -> {
                    val points = values.map { ChartPoint(x.of(it.day), it.value, it.day, listOf((it.value - it.treat).coerceAtLeast(0.0), it.treat)) }
                    listOf(SeriesSpec(kind.kind.name, tone, SeriesShape.StackedBars(points, listOf(tone, treatTone(chart, tone))), average = average, trend = trend))
                }
                kind.kind == SeriesKind.BURN -> {
                    val split = values.filter { it.active != null }.map { ChartPoint(x.of(it.day), it.value, it.day, listOf((it.value - it.active!!).coerceAtLeast(0.0), it.active)) }
                    val whole = values.filter { it.active == null }.map { ChartPoint(x.of(it.day), it.value, it.day) }
                    listOfNotNull(
                        SeriesSpec("${kind.kind.name}-split", tone, SeriesShape.StackedBars(split, listOf(restTone(chart, tone), tone))).takeIf { split.isNotEmpty() },
                        SeriesSpec("${kind.kind.name}-total", tone.alpha(ToneRef.TOTAL_ALPHA), SeriesShape.Bars(whole)).takeIf { whole.isNotEmpty() },
                    ).mapIndexed { i, s -> if (i == 0) s.copy(average = average, trend = trend) else s }
                }
                else -> listOf(SeriesSpec(kind.kind.name, tone, SeriesShape.Bars(values.map { ChartPoint(x.of(it.day), it.value, it.day) }), average = average, trend = trend))
            }
            PanelSpec(height, AxisSpec(kind.min, kind.max, unit(kind.kind)), series = series, kind = kind.kind)
        }
        val legend = data.drawn.filter { it in kinds }.flatMap { stackedLegend(chart, it) } + averageLegend(lines)
        val notDrawn = if (single) data.kinds.filter { !it.drawn && it !in kinds }.map { it.kind } else emptyList()
        return ChartSpec(data.axis, panels, legend, readout, notDrawn = notDrawn)
    }

    /**
     * A line's series. Syöty, Kulutus and Askeleet lines stop before today (today's running total is
     * only part of a day); today, when it has a value, is a separate one-point series "<KIND>.today"
     * (its lone dot) in the same tone and side, without an average. Paino joins today.
     */
    private fun lineSeries(
        kind: KindData, values: List<DayValue>, x: ChartX, card: Boolean, today: LocalDate,
        tone: ToneRef, side: Side, average: Double?, averageTone: ToneRef, trend: List<List<ChartPoint>>?, y: (Double) -> Double,
    ): List<SeriesSpec> {
        if (kind.kind == SeriesKind.WEIGHT) return listOf(SeriesSpec(kind.kind.name, tone, line(kind, values, x, card, y), side, average, averageTone, trend))
        val todayValue = values.filter { it.day == today }
        val before = values.filter { it.day != today }
        // Only today: its series carries the average, so the dashed line still draws.
        if (before.isEmpty() && todayValue.isNotEmpty()) return listOf(SeriesSpec("${kind.kind.name}.today", tone, line(kind, todayValue, x, card, y), side, average, averageTone))
        return listOfNotNull(
            SeriesSpec(kind.kind.name, tone, line(kind, before, x, card, y), side, average, averageTone, trend),
            SeriesSpec("${kind.kind.name}.today", tone, line(kind, todayValue, x, card, y), side, null, averageTone).takeIf { todayValue.isNotEmpty() },
        )
    }

    private fun line(kind: KindData, values: List<DayValue>, x: ChartX, card: Boolean, y: (Double) -> Double = { it }): SeriesShape.Line {
        val points = values.map { ChartPoint(x.of(it.day), y(it.value), it.day) }
        val dots = if (kind.kind == SeriesKind.WEIGHT) Dots.FitOrLast else Dots.None
        return if (card) SeriesShape.Line(points, dots, CARD_LINE_DP, CARD_DOT_DP) else SeriesShape.Line(points, dots)
    }

    private fun label(kind: SeriesKind): LegendLabel = when (kind) {
        SeriesKind.WEIGHT -> LegendLabel.WEIGHT
        SeriesKind.ENERGY -> LegendLabel.FOOD
        SeriesKind.BURN -> LegendLabel.BURN
        SeriesKind.STEPS -> LegendLabel.STEPS
    }

    private fun stackedLegend(chart: CustomChart, kind: KindData): List<LegendItem> {
        val tone = tone(chart, kind.kind, overlaid = false)
        if (kind.style == SeriesStyle.LINE) return listOf(LegendItem(label(kind.kind), tone, LegendForm.LINE))
        return when (kind.kind) {
            SeriesKind.ENERGY -> listOf(LegendItem(LegendLabel.FOOD, tone, LegendForm.SQUARE), LegendItem(LegendLabel.TREAT, treatTone(chart, tone), LegendForm.SQUARE))
            SeriesKind.BURN -> listOfNotNull(
                LegendItem(LegendLabel.REST, restTone(chart, tone), LegendForm.SQUARE),
                LegendItem(LegendLabel.ACTIVE, tone, LegendForm.SQUARE),
                LegendItem(LegendLabel.TOTAL, tone.alpha(ToneRef.TOTAL_ALPHA), LegendForm.SQUARE).takeIf { kind.values.orEmpty().any { it.active == null } },
            )
            else -> listOf(LegendItem(label(kind.kind), tone, LegendForm.SQUARE))
        }
    }

    /** One shared "keskiarvo" entry for the flat lines and one "trendi" for the trends (both when both kinds draw). */
    private fun averageLegend(lines: Map<SeriesKind, AverageLine>): List<LegendItem> = listOfNotNull(
        LegendItem(LegendLabel.AVERAGE, ToneRef.MUTED, LegendForm.DASHED).takeIf { lines.values.any { it is AverageLine.Flat } },
        TabCharts.TREND_LEGEND.takeIf { lines.values.any { it is AverageLine.Trend } },
    )

    /** A card's Päällekkäin bar [slot] of [slots]: the centre of its sub-slot (1/[slots] of the day) from the day's centre. */
    fun subSlotOffset(slot: Int, slots: Int): Double = -0.5 + (slot + 0.5) / slots

    /** The drawn data grouped by unit in data order: the first two groups get the value axes. */
    fun unitGroups(drawn: List<KindData>): List<List<KindData>> = drawn.groupBy { unit(it.kind) }.values.toList()

    private fun overlaid(data: CustomChartData, readout: (LocalDate) -> ReadoutCard, card: Boolean, today: LocalDate, lines: Map<SeriesKind, AverageLine>): ChartSpec {
        val chart = data.chart
        val drawn = data.drawn
        val x = ChartX(data.axis)
        val bars = drawn.filter { it.style == SeriesStyle.BARS }
        val slots = bars.size.coerceAtLeast(1)
        val slotWidth = BAR_SHARE / slots
        val series = drawn.flatMap { kind ->
            val tone = tone(chart, kind.kind, overlaid = true)
            val values = kind.values.orEmpty()
            val average = lines[kind.kind]?.flat?.let { kind.fraction(it).toDouble() }
            val trend = trendPoints(lines[kind.kind], x) { kind.fraction(it).toDouble() }
            val lineTone = if (trend != null) tone.alpha(SeriesSpec.TREND_ALPHA) else tone
            val side = when (unitGroups(drawn).indexOfFirst { kind in it }) {
                0 -> Side.LEFT
                1 -> Side.RIGHT
                else -> Side.HIDDEN
            }
            if (kind.style == SeriesStyle.LINE) return@flatMap lineSeries(kind, values, x, card, today, tone, side, average, lineTone, trend) { kind.fraction(it).toDouble() }
            val shape = run {
                val slot = bars.indexOf(kind)
                // Side by side in the day's slot (review decision 4); dayAt rounds back to the day.
                val offset = if (card) subSlotOffset(slot, slots) else -BAR_SHARE / 2 + (slot + 0.5) * slotWidth
                SeriesShape.Bars(values.map { ChartPoint(x.of(it.day) + offset, kind.fraction(it.value).toDouble(), it.day) }, slots = slots)
            }
            listOf(SeriesSpec(kind.kind.name, tone, shape, side, average, averageTone = lineTone, trend = trend))
        }
        fun axisOf(group: List<KindData>): AxisSpec {
            val kind = group[0]
            val tone = if (group.size > 1) null else tone(chart, kind.kind, overlaid = true)
            return AxisSpec(0.0, 1.0, unit(kind.kind), tone, kind.min, kind.max)
        }
        val groups = unitGroups(drawn)
        val panel = PanelSpec(OVERLAID_DP, axisOf(groups[0]), groups.getOrNull(1)?.let { axisOf(it) }, series, kind = null)
        val legend = drawn.map { kind ->
            LegendItem(label(kind.kind), tone(chart, kind.kind, overlaid = true), if (kind.style == SeriesStyle.LINE) LegendForm.LINE else LegendForm.SQUARE)
        } + averageLegend(lines)
        return ChartSpec(data.axis, listOf(panel), legend, readout, notDrawn = data.kinds.filter { !it.drawn }.map { it.kind })
    }
}
