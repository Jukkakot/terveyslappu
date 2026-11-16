package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.burn.BurnDay
import fi.jukkakot.terveyslappu.burn.HomeBurn
import fi.jukkakot.terveyslappu.home.CardSize
import fi.jukkakot.terveyslappu.steps.HomeSteps
import fi.jukkakot.terveyslappu.steps.StepsDay
import fi.jukkakot.terveyslappu.weight.weightEntry
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The front page cards as chart definitions (home-cards-on-chart-library design 1, 2); made-up values. */
class CardChartsTest {
    private val zone = ZoneId.of("Europe/Helsinki")
    private val today = LocalDate.of(2026, 11, 15)
    private val now = today.atTime(21, 0).atZone(zone).toInstant()

    private fun weight(id: String, day: LocalDate, hour: Int, kg: Double) = weightEntry(id, day.atTime(hour, 0).atZone(zone).toInstant(), null, kg, "me", "me")

    private fun line(spec: ChartSpec, id: String) = spec.panels.single().series.firstOrNull { it.id == id }?.shape as SeriesShape.Line?

    @Test
    fun `weight x is time over the whole period with two weighings a day`() {
        val weights = listOf(weight("a", today.minusDays(2), 7, 82.4), weight("b", today.minusDays(2), 19, 82.9), weight("c", today, 7, 82.0))
        val summary = HomeSummary.of(weights, now, 7)!!
        val spec = CardCharts.weight(summary, zone)!!
        val points = line(spec, "line")!!.points
        assertEquals(3, points.size)
        assertEquals(0.5, points[1].x - points[0].x, 1e-9)
        assertEquals(0.0, spec.xMin)
        assertEquals(7.0, spec.xMax, 1e-9)
        // The period starts 7 days before now (21.00): the first weight is 4 days and 10 hours in.
        assertEquals(4 + 10 / 24.0, points[0].x, 1e-9)
        val panel = spec.panels.single()
        assertEquals(summary.sparkScale!!.min, panel.left!!.min)
        assertEquals(summary.sparkScale!!.max, panel.left!!.max)
        assertEquals(CardCharts.WEIGHT_INSET_DP, panel.insetDp)
        assertTrue(panel.series.all { it.tone == ToneRef.PRIMARY }, "the card's weight is primary, not Historia's secondary")
    }

    @Test
    fun `a single weight is just the big dot`() {
        val spec = CardCharts.weight(HomeSummary.of(listOf(weight("a", today.minusDays(1), 7, 82.0)), now, 7)!!, zone)!!
        assertNull(line(spec, "line"))
        val latest = line(spec, "latest")!!
        assertEquals(1, latest.points.size)
        assertEquals(CardCharts.LAST_DOT_DP, latest.dotDp)
        assertEquals(setOf(0), latest.dots.shown(1, 300f))
    }

    @Test
    fun `dots when they fit, the latest always big`() {
        val weights = (0L until 40L).map { weight("w$it", today.minusDays(it % 30), if (it < 30) 7 else 19, 82.0 + it * 0.01) }
        val spec = CardCharts.weight(HomeSummary.of(weights, now, 30)!!, zone)!!
        val main = line(spec, "line")!!
        assertEquals(CardCharts.WEIGHT_LINE_DP, main.widthDp)
        assertEquals(CardCharts.WEIGHT_DOT_DP, main.dotDp)
        val count = main.points.size
        assertEquals((0 until count - 1).toSet(), main.dots.shown(count, 600f), "fit: every dot but the last")
        assertEquals(emptySet(), main.dots.shown(count, 120f), "no fit: none")
        assertEquals(main.points.last(), line(spec, "latest")!!.points.single())
    }

    @Test
    fun `no weight in the period draws nothing`() {
        assertNull(CardCharts.weight(HomeSummary.of(listOf(weight("a", today.minusDays(40), 7, 82.0)), now, 7)!!, zone))
    }

    private val steps = (0L until 90L).filter { it % 5 != 2L }.map { StepsDay(today.minusDays(it), 4000 + it * 50) }

    @Test
    fun `bars are daily at 90 days`() {
        val home = HomeSteps.of(steps, 90, today)
        val spec = CardCharts.steps(home, CardSize.SMALL)!!
        val bars = spec.panels.single().series.single().shape as SeriesShape.Bars
        assertEquals(steps.size, bars.points.size)
        assertEquals(1.0, bars.widthDays)
        val x = ChartX(spec.axis)
        bars.points.forEach { assertEquals(x.of(it.day), it.x) }
        assertEquals(-0.5, spec.xMin)
        assertEquals(89.5, spec.xMax)
    }

    @Test
    fun `small and wide scale to the highest bar, big to the tab's top with the average`() {
        val home = HomeSteps.of(steps, 30, today)
        val highest = home.bars.maxOf { it.steps }.toDouble()
        for (size in listOf(CardSize.SMALL, CardSize.WIDE)) {
            val panel = CardCharts.steps(home, size)!!.panels.single()
            assertEquals(highest, panel.left!!.max)
            assertEquals(0.0, panel.left!!.min)
            assertNull(panel.series.single().average)
            assertTrue(!panel.grid)
            assertEquals(ToneRef.PRIMARY.alpha(CardCharts.BASELINE_ALPHA), panel.baseline)
        }
        val big = CardCharts.steps(home, CardSize.BIG)!!.panels.single()
        assertEquals(AxisTicks.bars(highest, CardCharts.STEPS_STEP).last(), big.left!!.max)
        assertEquals(home.averageSteps!!.toDouble(), big.series.single().average)
    }

    @Test
    fun `Kulutus splits lepo and aktiivinen, a kokonais day in its own tone`() {
        val days = listOfNotNull(BurnDay.of(today, 900, 100), BurnDay.of(today.minusDays(1), 2300, 450), BurnDay.of(today.minusDays(2), 2200, null))
        val spec = CardCharts.burn(HomeBurn.of(days, 7, today), CardSize.BIG)!!
        val series = spec.panels.single().series
        assertEquals(listOf("split", "total"), series.map { it.id })
        val split = series[0].shape as SeriesShape.StackedBars
        assertEquals(listOf(1850.0, 450.0), split.points.first { it.day == today.minusDays(1) }.parts)
        assertEquals(TabReadouts.BURN_TONE.alpha(ToneRef.TOTAL_ALPHA), series[1].tone)
        assertEquals(3000.0, spec.panels.single().left!!.max)
        assertTrue(series[0].average != null)
    }

    @Test
    fun `Paallekkain card bars spread over the whole day`() {
        val chart = CustomChart(1, "Oma", listOf(SeriesKind.ENERGY, SeriesKind.STEPS, SeriesKind.BURN).map { ChartSeries(it, SeriesStyle.BARS) }, ChartLayout.OVERLAID, Period.MONTH)
        val food = (0L..5L).map { back ->
            val day = today.minusDays(back)
            val t = day.atTime(12, 0).atZone(zone).toInstant()
            fi.jukkakot.terveyslappu.food.FoodItem("f$back", day, 1, "Ruoka", 1900.0, fi.jukkakot.terveyslappu.food.Amount(1.0, fi.jukkakot.terveyslappu.food.AmountUnit.KPL), false, t, zone.rules.getOffset(t), true)
        }
        val burn = listOfNotNull(BurnDay.of(today.minusDays(1), 2300, 450))
        val data = CustomChartData.of(chart, today, zone, null, food, steps, burnDays = burn)
        val card = CustomChartSpec.of(data, today, card = true)
        val x = ChartX(card.axis)
        val day = today.minusDays(1)
        val offsets = card.panels.single().series.map { s -> s.shape.all.first { it.day == day }.x - x.of(day) }
        assertEquals(3, offsets.size)
        listOf(-1 / 3.0, 0.0, 1 / 3.0).zip(offsets).forEach { (want, got) -> assertEquals(want, got, 1e-9) }
        assertTrue(card.panels.single().series.all { (it.shape as SeriesShape.Bars).slots == 3 })
        // Historia keeps its touching bars inside 70 % of the day.
        val tab = CustomChartSpec.of(data, today)
        val first = tab.panels.single().series[0].shape.all.first { it.day == day }.x - x.of(day)
        assertEquals(-0.35 + 0.7 / 6, first, 1e-9)
        assertEquals(ReadoutCard("", emptyList()), card.readout(day))
    }

    @Test
    fun `own chart card lines are 2 dp with 3 dp dots`() {
        val chart = CustomChart(1, "Oma", listOf(ChartSeries(SeriesKind.WEIGHT, SeriesStyle.LINE)), ChartLayout.STACKED, Period.MONTH)
        val weights = (0L..10L).map { weight("w$it", today.minusDays(it), 7, 82.0) }
        val spec = CustomChartSpec.of(CustomChartData.of(chart, today, zone, weights, null, null), today, card = true)
        val shape = spec.panels.single().series.single().shape as SeriesShape.Line
        assertEquals(CustomChartSpec.CARD_LINE_DP, shape.widthDp)
        assertEquals(CustomChartSpec.CARD_DOT_DP, shape.dotDp)
    }
}
