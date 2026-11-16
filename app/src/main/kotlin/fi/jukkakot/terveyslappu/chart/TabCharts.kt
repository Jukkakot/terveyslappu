package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.burn.BurnKind
import fi.jukkakot.terveyslappu.burn.BurnSeries
import fi.jukkakot.terveyslappu.derived.Derived
import fi.jukkakot.terveyslappu.food.EnergySeries
import fi.jukkakot.terveyslappu.steps.StepsSeries
import fi.jukkakot.terveyslappu.ui.formatKg
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.max

/**
 * The fixed Historia tabs as chart definitions (design 2): Paino, BMI, Syöty, Askeleet and Kulutus.
 * Scales, averages and weekly bars come from the existing pure rules ([WeightScale] via
 * [ChartData], [AxisTicks.bars], the series classes); this only places them on the axis.
 */
object TabCharts {
    const val PLOT_DP = 160
    const val KCAL_STEP = 250.0
    const val STEPS_STEP = 1000.0

    private fun weekWidth(weekly: Boolean) = if (weekly) 7.0 else 1.0

    /** Paino: the weights as one line, dots when they fit, else only the lone ones. */
    fun weight(data: ChartData, zone: ZoneId, today: LocalDate): ChartSpec = line(data, zone, today, ValueUnit.KG, LegendLabel.WEIGHT, emptyList()) { d, day ->
        TabReadouts.weight(d, day, zone, today) { "${formatKg(it)} kg" }
    }

    /** BMI: the same line in BMI units over the class bands ([Derived.BANDS], clipped to the scale). */
    fun bmi(data: ChartData, zone: ZoneId, today: LocalDate): ChartSpec {
        val bands = Derived.BANDS.mapNotNull { band ->
            val lo = max(band.from, data.minY)
            val hi = minOf(band.to, data.maxY)
            if (lo < hi) BandSpec(lo, hi, ToneRef.Band(band.tone)) else null
        }
        return line(data, zone, today, ValueUnit.BMI, LegendLabel.BMI, bands) { d, day ->
            TabReadouts.weight(d, day, zone, today, name = TabReadouts.BMI) { formatKg(it) }
        }
    }

    private fun line(
        data: ChartData,
        zone: ZoneId,
        today: LocalDate,
        unit: ValueUnit,
        label: LegendLabel,
        bands: List<BandSpec>,
        readout: (ChartData, LocalDate) -> ReadoutCard,
    ): ChartSpec {
        val axis = ChartAxis(data.from, data.to)
        val x = ChartX(axis)
        val points = data.entries.indices.map { i ->
            ChartPoint(x.of(data.entries[i].time, zone), data.ys[i], data.entries[i].time.atZone(zone).toLocalDate())
        }
        val series = SeriesSpec("line", ToneRef.PRIMARY, SeriesShape.Line(points, Dots.FitOrLone(data.lone)))
        val panel = PanelSpec(PLOT_DP, AxisSpec(data.minY, data.maxY, unit), series = listOf(series), bands = bands)
        return ChartSpec(axis, listOf(panel), listOf(LegendItem(label, ToneRef.PRIMARY, LegendForm.LINE)), readout = { readout(data, it) })
    }

    private fun legendTail(average: Boolean, weekly: Boolean): List<LegendItem> = listOfNotNull(
        LegendItem(LegendLabel.AVERAGE, ToneRef.MUTED, LegendForm.DASHED).takeIf { average },
        LegendItem(LegendLabel.WEEKLY, null, LegendForm.NONE).takeIf { weekly },
    )

    /** Syöty: kcal bars with the treats stacked on top, the average line, weekly bars over 90 days. */
    fun energy(series: EnergySeries, today: LocalDate): ChartSpec {
        val axis = ChartAxis(series.from, series.to, series.weekly)
        val x = ChartX(axis)
        val average = series.stats.averageKcal?.toDouble()
        val top = AxisTicks.bars(max(series.bars.maxOfOrNull { it.total } ?: 0, series.stats.averageKcal ?: 0).toDouble(), KCAL_STEP).last()
        val points = series.bars.map { bar ->
            val at = if (series.weekly) x.ofWeek(bar.from) else x.of(bar.from)
            ChartPoint(at, bar.total.toDouble(), bar.from, listOf((bar.total - bar.treat).toDouble().coerceAtLeast(0.0), bar.treat.toDouble()))
        }
        val bars = SeriesSpec(
            "food",
            ToneRef.PRIMARY,
            SeriesShape.StackedBars(points, listOf(ToneRef.PRIMARY, ToneRef.TERTIARY), weekWidth(series.weekly)),
            average = average,
        )
        val legend = listOf(
            LegendItem(LegendLabel.FOOD, ToneRef.PRIMARY, LegendForm.SQUARE),
            LegendItem(LegendLabel.TREAT, ToneRef.TERTIARY, LegendForm.SQUARE),
        ) + legendTail(average != null, series.weekly)
        val panel = PanelSpec(PLOT_DP, AxisSpec(0.0, top, ValueUnit.KCAL), series = listOf(bars))
        return ChartSpec(axis, listOf(panel), legend, readout = { TabReadouts.energy(series, it, today) })
    }

    /** Askeleet: one bar per day with steps (weekly over 90 days) and the average line. */
    fun steps(series: StepsSeries, today: LocalDate): ChartSpec {
        val axis = ChartAxis(series.from, series.to, series.weekly)
        val x = ChartX(axis)
        val average = series.stats.averageSteps?.toDouble()
        val top = AxisTicks.bars(max(series.bars.maxOfOrNull { it.steps } ?: 0L, series.stats.averageSteps ?: 0L).toDouble(), STEPS_STEP).last()
        val points = series.bars.map { bar ->
            ChartPoint(if (series.weekly) x.ofWeek(bar.from) else x.of(bar.from), bar.steps.toDouble(), bar.from)
        }
        val bars = SeriesSpec("steps", ToneRef.PRIMARY, SeriesShape.Bars(points, weekWidth(series.weekly)), average = average)
        val legend = listOf(LegendItem(LegendLabel.STEPS, ToneRef.PRIMARY, LegendForm.SQUARE)) + legendTail(average != null, series.weekly)
        val panel = PanelSpec(PLOT_DP, AxisSpec(0.0, top, ValueUnit.STEPS), series = listOf(bars))
        return ChartSpec(axis, listOf(panel), legend, readout = { TabReadouts.steps(series, it, today) })
    }

    /**
     * Kulutus (energy-burn "Kulutus tab"): lepo at the bottom in the light tone and aktiivinen on top
     * in the full tone; a kokonais day in the kokonais tone, an active-only day in the full tone.
     */
    fun burn(series: BurnSeries, today: LocalDate): ChartSpec {
        val axis = ChartAxis(series.from, series.to, series.weekly)
        val x = ChartX(axis)
        val tone = TabReadouts.BURN_TONE
        val average = series.stats.averageTotal?.toDouble()
        val top = AxisTicks.bars(max(series.bars.maxOfOrNull { it.total } ?: 0L, series.stats.averageTotal ?: 0L).toDouble(), KCAL_STEP).last()
        fun at(from: LocalDate) = if (series.weekly) x.ofWeek(from) else x.of(from)
        val width = weekWidth(series.weekly)
        val split = series.bars.filter { it.kind == BurnKind.SPLIT }.map { bar ->
            val active = (bar.active ?: 0L).toDouble()
            ChartPoint(at(bar.from), bar.total.toDouble(), bar.from, listOf((bar.total - active).coerceAtLeast(0.0), active))
        }
        val whole = series.bars.filter { it.kind == BurnKind.TOTAL }.map { ChartPoint(at(it.from), it.total.toDouble(), it.from) }
        val activeOnly = series.bars.filter { it.kind == BurnKind.ACTIVE }.map { ChartPoint(at(it.from), it.total.toDouble(), it.from) }
        val shapes = listOfNotNull(
            SeriesSpec("split", tone, SeriesShape.StackedBars(split, listOf(tone.alpha(ToneRef.LIGHT_ALPHA), tone), width)).takeIf { split.isNotEmpty() },
            SeriesSpec("total", tone.alpha(ToneRef.TOTAL_ALPHA), SeriesShape.Bars(whole, width)).takeIf { whole.isNotEmpty() },
            SeriesSpec("active", tone, SeriesShape.Bars(activeOnly, width)).takeIf { activeOnly.isNotEmpty() },
        )
        // One average line for the panel, carried by its first series.
        val withAverage = shapes.mapIndexed { i, s -> if (i == 0) s.copy(average = average) else s }
        val legend = listOfNotNull(
            LegendItem(LegendLabel.REST, tone.alpha(ToneRef.LIGHT_ALPHA), LegendForm.SQUARE),
            LegendItem(LegendLabel.ACTIVE, tone, LegendForm.SQUARE),
            LegendItem(LegendLabel.TOTAL, tone.alpha(ToneRef.TOTAL_ALPHA), LegendForm.SQUARE).takeIf { whole.isNotEmpty() },
        ) + legendTail(average != null, series.weekly)
        val panel = PanelSpec(PLOT_DP, AxisSpec(0.0, top, ValueUnit.KCAL), series = withAverage)
        return ChartSpec(axis, listOf(panel), legend, readout = { TabReadouts.burn(series, it, today) })
    }
}
