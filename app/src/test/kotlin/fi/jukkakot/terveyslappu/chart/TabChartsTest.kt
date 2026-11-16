package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.burn.BurnDay
import fi.jukkakot.terveyslappu.burn.BurnSeries
import fi.jukkakot.terveyslappu.food.Amount
import fi.jukkakot.terveyslappu.food.AmountUnit
import fi.jukkakot.terveyslappu.food.EnergySeries
import fi.jukkakot.terveyslappu.food.FoodItem
import fi.jukkakot.terveyslappu.steps.StepsDay
import fi.jukkakot.terveyslappu.steps.StepsSeries
import fi.jukkakot.terveyslappu.weight.weightEntry
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The fixed tabs as chart definitions (design 2, weight-chart "Chart axes", "Chart legends"); made-up values. */
class TabChartsTest {
    private val zone = ZoneId.of("Europe/Helsinki")
    private val today = LocalDate.of(2026, 11, 15)
    private val now = today.atTime(21, 0).atZone(zone).toInstant()

    private fun weight(day: LocalDate, kg: Double, hour: Int = 7) = weightEntry("$day-$hour", day.atTime(hour, 5).atZone(zone).toInstant(), null, kg, "me", "me")

    private fun only(spec: ChartSpec) = spec.panels.single()

    @Test
    fun `Paino uses the weight scale with three ticks and a point per weight`() {
        val entries = (0L..29L).filter { it % 3 != 0L }.map { weight(today.minusDays(it), 95.5 + it * 0.05) }
        val data = ChartData.of(Period.MONTH, entries, now, zone)!!
        val panel = only(TabCharts.weight(data, zone, today))
        val scale = WeightScale.of(data.ys)
        assertEquals(scale.min, panel.left!!.min)
        assertEquals(scale.max, panel.left!!.max)
        assertEquals(AxisTicks.line(scale.min, scale.max), panel.left!!.ticks)
        val line = panel.series.single().shape as SeriesShape.Line
        assertEquals(entries.size, line.points.size)
        // No entry on a gap day.
        assertTrue(line.points.none { it.day == today.minusDays(3) })
    }

    @Test
    fun `lone weights keep their dot in a dense chart`() {
        val entries = listOf(weight(today.minusDays(300), 90.0)) + (0L..100L).map { weight(today.minusDays(it), 85.0) }
        val data = ChartData.of(Period.All, entries, now, zone)!!
        val line = only(TabCharts.weight(data, zone, today)).series.single().shape as SeriesShape.Line
        assertEquals(Dots.FitOrLone(setOf(0)), line.dots)
        assertEquals(setOf(0), line.dots.shown(line.points.size, 300f))
    }

    @Test
    fun `BMI bands are clipped to the scale and the legend names BMI`() {
        val perKg = 1 / (1.8 * 1.8)
        val entries = (0L..6L).map { weight(today.minusDays(it), 80.0 + it) }
        val data = ChartData.of(Period.WEEK, entries, now, zone, value = { it.kg * perKg }) { WeightScale.of(it, perKg) }!!
        val spec = TabCharts.bmi(data, zone, today)
        val panel = only(spec)
        assertTrue(panel.bands.isNotEmpty())
        assertTrue(panel.bands.all { it.from >= data.minY && it.to <= data.maxY && it.from < it.to })
        assertEquals(listOf(LegendLabel.BMI), spec.legend.map { it.label })
    }

    private fun food(day: LocalDate, kcal: Double, treat: Boolean = false, pos: Int = 1): FoodItem {
        val t = day.atTime(12, 0).atZone(zone).toInstant()
        return FoodItem("$day-$pos", day, pos, "Ruoka", kcal, Amount(1.0, AmountUnit.KPL), treat, t, zone.rules.getOffset(t), true)
    }

    @Test
    fun `Syoty stacks the treats, its scale is AxisTicks bars and the legend has keskiarvo`() {
        val days = (0L..6L).filter { it != 2L }
        val items = days.flatMap { listOf(food(today.minusDays(it), 1800.0), food(today.minusDays(it), 200.0, treat = true, pos = 2)) }
        val series = EnergySeries.of(items, Period.WEEK, today)
        val spec = TabCharts.energy(series, today)
        val panel = only(spec)
        assertEquals(AxisTicks.bars(2000.0, TabCharts.KCAL_STEP), panel.left!!.ticks)
        val bars = panel.series.single().shape as SeriesShape.StackedBars
        assertEquals(days.size, bars.points.size)
        assertEquals(listOf(1800.0, 200.0), bars.points.first().parts)
        assertEquals(series.stats.averageKcal!!.toDouble(), panel.series.single().average)
        assertEquals(listOf(LegendLabel.FOOD, LegendLabel.TREAT, LegendLabel.AVERAGE), spec.legend.map { it.label })
    }

    @Test
    fun `Askeleet over 90 days is weekly with viikoittain in the legend`() {
        val steps = (0L..200L).map { StepsDay(today.minusDays(it), 5000 + it) }
        val series = StepsSeries.of(steps, Period.Last(180), today)
        val spec = TabCharts.steps(series, today)
        assertTrue(spec.axis.weekly)
        val bars = only(spec).series.single().shape as SeriesShape.Bars
        assertEquals(7.0, bars.widthDays)
        assertEquals(series.bars.size, bars.points.size)
        assertEquals(LegendLabel.WEEKLY, spec.legend.last().label)
        assertEquals(LegendForm.NONE, spec.legend.last().form)
    }

    @Test
    fun `no keskiarvo without an average`() {
        val series = StepsSeries.of(listOf(StepsDay(today, 4000)), Period.WEEK, today)
        val spec = TabCharts.steps(series, today)
        assertEquals(listOf(LegendLabel.STEPS), spec.legend.map { it.label })
    }

    @Test
    fun `Kulutus splits lepo and aktiivinen, kokonais and active-only days in their own sets`() {
        val days = listOfNotNull(
            BurnDay.of(today.minusDays(1), 2300, 450),
            BurnDay.of(today.minusDays(2), 2100, null),
            BurnDay.of(today.minusDays(3), null, 400),
        )
        val series = BurnSeries.of(days, Period.WEEK, today)
        val spec = TabCharts.burn(series, today)
        val panel = only(spec)
        assertEquals(listOf("split", "total", "active"), panel.series.map { it.id })
        val split = panel.series[0].shape as SeriesShape.StackedBars
        assertEquals(listOf(1850.0, 450.0), split.points.single().parts)
        assertEquals(series.stats.averageTotal!!.toDouble(), panel.series[0].average)
        assertEquals(listOf(LegendLabel.REST, LegendLabel.ACTIVE, LegendLabel.TOTAL, LegendLabel.AVERAGE), spec.legend.map { it.label })
        assertEquals(AxisTicks.bars(2300.0, TabCharts.KCAL_STEP).last(), panel.left!!.max)
    }
}
