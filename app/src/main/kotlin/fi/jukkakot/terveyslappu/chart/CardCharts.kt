package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.burn.BurnKind
import fi.jukkakot.terveyslappu.burn.HomeBurn
import fi.jukkakot.terveyslappu.home.CardSize
import fi.jukkakot.terveyslappu.steps.HomeSteps
import java.time.LocalDate
import java.time.ZoneId

/**
 * The front page feature cards' charts as one-panel chart definitions (home-cards-on-chart-library
 * design 1, 2), drawn by the same library path as Historia in its card mode. The rules are the
 * cards' own (home-screen "Card sizes", weight-chart "Dots when they fit"), not Historia's:
 * daily bars at every period; small and wide scaled to the highest bar, big to the tab's rounded
 * top with the dashed average; the weight line in the primary tone by time. Own chart cards use
 * `CustomChartSpec.of(..., card = true)`. Pure.
 */
object CardCharts {
    const val KCAL_STEP = 250.0
    const val STEPS_STEP = 1000.0

    /** The weight card's line, its dots, the latest weight's dot and the room kept for it on every side. */
    const val WEIGHT_LINE_DP = 2.5f
    const val WEIGHT_DOT_DP = 2.5f
    const val LAST_DOT_DP = 4f
    const val WEIGHT_INSET_DP = 4f

    /** The feature cards' 1 dp baseline in the bar tone. */
    const val BASELINE_ALPHA = 0.4f

    private const val SECONDS_PER_DAY = 86_400.0

    /** Cards have no marker. */
    val noReadout: (LocalDate) -> ReadoutCard = { ReadoutCard("", emptyList()) }

    /**
     * The weight card: the period's weights as a line by time over the whole period (x = days from
     * [HomeSummary.sparkFrom]), dots when they fit, the latest weight always a bigger dot; no line
     * with one weight. Null when the period has no weight (nothing is drawn).
     */
    fun weight(summary: HomeSummary, zone: ZoneId): ChartSpec? {
        val scale = summary.sparkScale ?: return null
        val spark = summary.spark.takeIf { it.isNotEmpty() } ?: return null
        val t0 = summary.sparkFrom.epochSecond
        val span = (summary.sparkTo.epochSecond - t0).coerceAtLeast(1) / SECONDS_PER_DAY
        val points = spark.map {
            ChartPoint(((it.time.epochSecond - t0) / SECONDS_PER_DAY).coerceIn(0.0, span), it.kg, it.time.atZone(zone).toLocalDate())
        }
        val series = listOfNotNull(
            SeriesSpec("line", ToneRef.WEIGHT, SeriesShape.Line(points, Dots.FitButLast, WEIGHT_LINE_DP, WEIGHT_DOT_DP), look = LineLook(fill = true, ring = false)).takeIf { points.size > 1 },
            SeriesSpec("latest", ToneRef.WEIGHT, SeriesShape.Line(listOf(points.last()), Dots.All, WEIGHT_LINE_DP, LAST_DOT_DP), look = LineLook(fill = false)),
        )
        val panel = PanelSpec(0, AxisSpec(scale.min, scale.max, ValueUnit.KG), series = series, grid = false, insetDp = WEIGHT_INSET_DP)
        val axis = ChartAxis(summary.sparkFrom.atZone(zone).toLocalDate(), summary.sparkTo.atZone(zone).toLocalDate())
        return ChartSpec(axis, listOf(panel), emptyList(), noReadout, xRange = 0.0..span)
    }

    /** The food card: daily kcal bars with the treats stacked on top (Energia's tones). Null without bars. */
    fun food(home: HomeFood, size: CardSize): ChartSpec? {
        if (home.bars.isEmpty()) return null
        val axis = ChartAxis(home.from, home.to)
        val x = ChartX(axis)
        val points = home.bars.map { bar ->
            ChartPoint(x.of(bar.from), bar.total.toDouble(), bar.from, listOf((bar.total - bar.treat).toDouble().coerceAtLeast(0.0), bar.treat.toDouble()))
        }
        val series = SeriesSpec(
            "food",
            ToneRef.FOOD,
            SeriesShape.StackedBars(points, listOf(ToneRef.FOOD, ToneRef.TREAT)),
            average = home.averageKcal?.toDouble()?.takeIf { size == CardSize.BIG },
            today = BarParts.todayOf(axis, home.bars.map { it.from }, home.to),
        )
        return bars(axis, listOf(series), home.bars.maxOf { it.total }.toDouble(), KCAL_STEP, size, ToneRef.FOOD)
    }

    /** The steps card: daily step bars in one tone. Null without bars. */
    fun steps(home: HomeSteps, size: CardSize): ChartSpec? {
        if (home.bars.isEmpty()) return null
        val axis = ChartAxis(home.from, home.to)
        val x = ChartX(axis)
        val points = home.bars.map { ChartPoint(x.of(it.from), it.steps.toDouble(), it.from) }
        val series = SeriesSpec("steps", ToneRef.STEPS, SeriesShape.Bars(points), average = home.averageSteps?.toDouble()?.takeIf { size == CardSize.BIG }, today = BarParts.todayOf(axis, home.bars.map { it.from }, home.to))
        return bars(axis, listOf(series), home.bars.maxOf { it.steps }.toDouble(), STEPS_STEP, size, ToneRef.STEPS)
    }

    /**
     * The Kulutus card (energy-burn "Kulutus card"): lepo in the light tone under aktiivinen in the
     * full tone, a kokonais day in the kokonais tone, an active-only day in the full tone. Null without bars.
     */
    fun burn(home: HomeBurn, size: CardSize): ChartSpec? {
        if (home.bars.isEmpty()) return null
        val axis = ChartAxis(home.from, home.to)
        val x = ChartX(axis)
        val tone = TabReadouts.BURN_TONE
        val split = home.bars.filter { it.kind == BurnKind.SPLIT }.map { bar ->
            val active = (bar.active ?: 0L).toDouble()
            ChartPoint(x.of(bar.from), bar.total.toDouble(), bar.from, listOf((bar.total - active).coerceAtLeast(0.0), active))
        }
        val whole = home.bars.filter { it.kind == BurnKind.TOTAL }.map { ChartPoint(x.of(it.from), it.total.toDouble(), it.from) }
        val activeOnly = home.bars.filter { it.kind == BurnKind.ACTIVE }.map { ChartPoint(x.of(it.from), it.total.toDouble(), it.from) }
        val average = home.averageTotal?.toDouble()?.takeIf { size == CardSize.BIG }
        val day = BarParts.todayOf(axis, home.bars.map { it.from }, home.to)
        val series = listOfNotNull(
            SeriesSpec("split", tone, SeriesShape.StackedBars(split, listOf(ToneRef.REST, tone)), today = day).takeIf { split.isNotEmpty() },
            SeriesSpec("total", tone.alpha(ToneRef.TOTAL_ALPHA), SeriesShape.Bars(whole), today = day).takeIf { whole.isNotEmpty() },
            SeriesSpec("active", tone, SeriesShape.Bars(activeOnly), today = day).takeIf { activeOnly.isNotEmpty() },
        ).mapIndexed { i, s -> if (i == 0) s.copy(average = average) else s }
        return bars(axis, series, home.bars.maxOf { it.total }.toDouble(), KCAL_STEP, size, tone)
    }

    /** The scale top of a feature card: big = the tab's rounded top, else the highest bar (at least 1). */
    fun top(highest: Double, step: Double, size: CardSize): Double =
        if (size == CardSize.BIG) AxisTicks.bars(highest, step).last() else highest.coerceAtLeast(1.0)

    private fun bars(axis: ChartAxis, series: List<SeriesSpec>, highest: Double, step: Double, size: CardSize, tone: ToneRef): ChartSpec {
        val unit = if (step == STEPS_STEP) ValueUnit.STEPS else ValueUnit.KCAL
        val panel = PanelSpec(0, AxisSpec(0.0, top(highest, step, size), unit), series = series, grid = false, baseline = tone.alpha(BASELINE_ALPHA))
        return ChartSpec(axis, listOf(panel), emptyList(), noReadout)
    }
}

/** The top of a card chart's scale (the big cards' scale-top label). */
val ChartSpec.scaleTop: Double get() = drawn.first().left?.max ?: 0.0
