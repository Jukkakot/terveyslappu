package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.food.Amount
import fi.jukkakot.terveyslappu.food.AmountUnit
import fi.jukkakot.terveyslappu.food.EnergySeries
import fi.jukkakot.terveyslappu.food.FoodItem
import fi.jukkakot.terveyslappu.steps.StepsDay
import fi.jukkakot.terveyslappu.steps.StepsSeries
import fi.jukkakot.terveyslappu.weight.weightEntry
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The trend line in the chart definitions (weight-chart "Trend line on long periods" and friends); made-up values. */
class TrendChartsTest {
    private val zone = ZoneId.of("Europe/Helsinki")
    private val today = LocalDate.of(2026, 11, 15)
    private val now = today.atTime(21, 0).atZone(zone).toInstant()

    private fun food(day: LocalDate, kcal: Double): FoodItem {
        val t = day.atTime(12, 0).atZone(zone).toInstant()
        return FoodItem("$day", day, 1, "Ruoka", kcal, Amount(1.0, AmountUnit.KPL), false, t, zone.rules.getOffset(t), true)
    }

    private fun foodDays(days: Iterable<Long>) = days.map { food(today.minusDays(it), 1800.0 + 300 * sin(it / 3.0)) }

    private fun weight(back: Long, kg: Double, hour: Int = 7) =
        weightEntry("w$back-$hour", today.minusDays(back).atTime(hour, 5).atZone(zone).toInstant(), null, kg, "me", "me")

    private fun only(spec: ChartSpec) = spec.panels.single().series.first()
    private fun labels(spec: ChartSpec) = spec.legend.map { it.label }

    @Test
    fun `Oma 31 days is flat and 32 days a trend`() {
        val items = foodDays(0L..40L)
        val flat = TabCharts.energy(EnergySeries.of(items, Period.Custom(today.minusDays(30), today), today), today)
        val trend = TabCharts.energy(EnergySeries.of(items, Period.Custom(today.minusDays(31), today), today), today)
        assertEquals(31, flat.axis.days)
        assertNotNull(only(flat).average)
        assertNull(only(flat).trend)
        assertNull(only(trend).average)
        assertNotNull(only(trend).trend)
        assertTrue(LegendLabel.AVERAGE in labels(flat) && LegendLabel.TREND !in labels(flat))
        assertTrue(LegendLabel.TREND in labels(trend) && LegendLabel.AVERAGE !in labels(trend))
    }

    @Test
    fun `a short Kaikki, few values or no long part keep the flat line`() {
        val short = TabCharts.energy(EnergySeries.of(foodDays(0L..20L), Period.All, today), today)
        assertTrue(short.axis.days <= 31)
        assertNull(only(short).trend)
        val few = TabCharts.energy(EnergySeries.of(foodDays(listOf(1L, 5L, 20L, 40L, 60L, 80L)), Period.QUARTER, today), today)
        assertNull(only(few).trend)
        assertNotNull(only(few).average)
        val parts = TabCharts.energy(EnergySeries.of(foodDays((200L..205L) + (20L..27L)), Period.All, today), today)
        assertNull(only(parts).trend)
        assertNotNull(only(parts).average)
    }

    @Test
    fun `a year of steps has a weekly trend, its legend and the week's trend row`() {
        val steps = (1L..365L).map { StepsDay(today.minusDays(it), 6000 + (365 - it) * 8 + (it % 5) * 700) }
        val spec = TabCharts.steps(StepsSeries.of(steps, Period.YEAR, today), today)
        assertEquals(listOf(LegendLabel.STEPS, LegendLabel.TREND, LegendLabel.WEEKLY), labels(spec))
        val series = only(spec)
        val top = spec.panels.single().left!!.max
        assertTrue(series.trend!!.flatten().all { it.y <= top })
        val week = spec.axis.pick(today.minusDays(100))
        val rows = spec.readout(week).rows
        assertEquals(2, rows.size)
        assertEquals(TabReadouts.TREND, rows[1].name)
        assertTrue(rows[1].value.endsWith("askelta/pv"), rows[1].value)
        assertEquals(ToneRef.MUTED, rows[1].tone)
    }

    @Test
    fun `the week's trend row uses the nearest covered day of a week at the axis edge`() {
        val axis = ChartAxis(LocalDate.of(2026, 1, 3), LocalDate.of(2026, 6, 30), weekly = true) // 3.1. is a Saturday
        val trend = listOf(TrendSegment(listOf(LocalDate.of(2026, 1, 3), LocalDate.of(2026, 1, 4)), listOf(1.0, 2.0)))
        assertEquals(1.0, TabReadouts.trendAt(trend, axis.pick(LocalDate.of(2026, 1, 3)), axis)) // Saturday is nearest to Thursday
        val late = listOf(TrendSegment(listOf(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 30)), listOf(1.0, 2.0)))
        assertNull(TabReadouts.trendAt(late, axis.pick(LocalDate.of(2026, 1, 3)), axis))
    }

    @Test
    fun `today's tap on Syoty has no trend row`() {
        val spec = TabCharts.energy(EnergySeries.of(foodDays(0L..100L), Period.Custom(today.minusDays(60), today), today), today)
        assertNotNull(only(spec).trend)
        assertTrue(spec.readout(today).rows.none { it.name == TabReadouts.TREND })
        val rows = spec.readout(today.minusDays(5)).rows
        assertEquals(TabReadouts.TREND, rows[1].name)
        assertTrue(rows[1].value.endsWith("kcal/pv"))
    }

    @Test
    fun `Paino gets a trend at the last weight's time and taps still pick the weight`() {
        val entries = (0L..89L).flatMap { listOf(weight(it, 84.0 - it * -0.03 + (it % 3) * 0.4), weight(it, 84.5 - it * -0.03, hour = 20)) }
        val spec = TabCharts.weight(ChartData.of(Period.QUARTER, entries, now, zone)!!, zone, today)
        val series = only(spec)
        val trend = series.trend!!.single()
        val line = (series.shape as SeriesShape.Line).points
        // The trend's point of a day sits at its last (evening) weight.
        assertEquals(line.last { it.day == today.minusDays(10) }.x, trend.single { it.day == today.minusDays(10) }.x)
        assertEquals(listOf(LegendLabel.WEIGHT, LegendLabel.TREND), labels(spec))
        val a = line[20]
        val b = line[21]
        val panel = spec.panels.single()
        assertEquals(a.day, panel.pick(a.x + (b.x - a.x) * 0.3, 2.0, spec.x))
        val rows = spec.readout(today.minusDays(10)).rows
        assertEquals(TabReadouts.TREND, rows[1].name)
        assertTrue(rows[1].value.endsWith(" kg"))
    }

    @Test
    fun `few weights give no trend and no flat line`() {
        val spec = TabCharts.weight(ChartData.of(Period.QUARTER, (0L..5L).map { weight(it * 10, 82.0) }, now, zone)!!, zone, today)
        assertNull(only(spec).trend)
        assertNull(only(spec).average)
        assertEquals(listOf(LegendLabel.WEIGHT), labels(spec))
    }

    private val ownWeights = (0L..364L).map { weight(it, 82.0 + sin(it / 20.0)) }
    private val fewSteps = (1L..6L).map { StepsDay(today.minusDays(it * 3), 7000) }

    private fun own(layout: ChartLayout, card: Boolean = false, steps: List<StepsDay> = fewSteps): ChartSpec {
        val chart = CustomChart(1, "Oma", listOf(ChartSeries(SeriesKind.WEIGHT, SeriesStyle.LINE), ChartSeries(SeriesKind.STEPS, SeriesStyle.BARS)), layout, Period.YEAR, true)
        return CustomChartSpec.of(CustomChartData.of(chart, today, zone, ownWeights, null, steps, burnDays = null), today, card) { true }
    }

    @Test
    fun `an own chart with both kinds has keskiarvo and trendi`() {
        val spec = own(ChartLayout.STACKED)
        assertTrue(LegendLabel.AVERAGE in labels(spec) && LegendLabel.TREND in labels(spec))
        val weight = spec.panels[0].series.single()
        assertNotNull(weight.trend)
        assertEquals(ToneRef.MUTED, weight.averageTone)
        assertNotNull(spec.panels[1].series.single().average)
        val rows = spec.readout(today.minusDays(3)).rows.map { it.name }
        assertEquals(listOf("paino", "paino trendi", "askeleet"), rows)
    }

    @Test
    fun `overlaid trends are in the data's tone at 60 percent and in fractions`() {
        val steps = (1L..200L).map { StepsDay(today.minusDays(it), 6000 + it * 10) }
        val spec = own(ChartLayout.OVERLAID, steps = steps)
        val series = spec.panels.single().series
        series.forEach { s ->
            assertNotNull(s.trend, s.id)
            assertTrue(s.trend!!.flatten().all { it.y in 0.0..1.0 })
            assertEquals(s.tone.alpha(SeriesSpec.TREND_ALPHA), s.averageTone)
        }
        assertEquals(1, spec.legend.count { it.label == LegendLabel.TREND })
        val rows = spec.readout(today.minusDays(3)).rows.map { it.name }
        assertEquals(listOf("paino", "paino trendi", "askeleet", "askeleet trendi"), rows)
    }

    @Test
    fun `a card spec never carries a trend`() {
        val steps = (1L..200L).map { StepsDay(today.minusDays(it), 6000 + it * 10) }
        listOf(ChartLayout.STACKED, ChartLayout.OVERLAID).forEach { layout ->
            val spec = own(layout, card = true, steps = steps)
            val all = spec.panels.flatMap { it.series }
            assertTrue(all.none { it.trend != null })
            assertTrue(all.any { it.average != null })
        }
    }
}
