package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.burn.BurnDay
import fi.jukkakot.terveyslappu.food.Amount
import fi.jukkakot.terveyslappu.food.AmountUnit
import fi.jukkakot.terveyslappu.food.FoodItem
import fi.jukkakot.terveyslappu.steps.StepsDay
import fi.jukkakot.terveyslappu.weight.weightEntry
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** An own chart as a chart definition (weight-chart "Stacked layout", "Overlaid layout", "Kulutus in own charts"); made-up values. */
class CustomChartSpecTest {
    private val zone = ZoneId.of("Europe/Helsinki")
    private val today = LocalDate.of(2026, 11, 15)

    private val weights = (0L..20L step 2).map { weightEntry("w$it", today.minusDays(it).atTime(7, 0).atZone(zone).toInstant(), null, 82.0 + it * 0.1, "me", "me") }
    private val food = (0L..10L).map { back ->
        val day = today.minusDays(back)
        val t = day.atTime(12, 0).atZone(zone).toInstant()
        FoodItem("f$back", day, 1, "Ruoka", 1900.0, Amount(1.0, AmountUnit.KPL), false, t, zone.rules.getOffset(t), true)
    }
    private val steps = (0L..9L).map { StepsDay(today.minusDays(it), 6000 + it * 100) }
    private val burn = listOfNotNull(BurnDay.of(today.minusDays(1), 2300, 450), BurnDay.of(today.minusDays(2), 2200, null))

    private fun chart(layout: ChartLayout, vararg series: Pair<SeriesKind, SeriesStyle>, average: Boolean = false) =
        CustomChart(1, "Oma", series.map { ChartSeries(it.first, it.second) }, layout, Period.MONTH, average)

    private fun spec(chart: CustomChart, weights: Boolean = true, food: Boolean = true, steps: Boolean = true, burn: Boolean = true) =
        CustomChartSpec.of(
            CustomChartData.of(chart, today, zone, if (weights) this.weights else null, if (food) this.food else null, if (steps) this.steps else null, burnDays = if (burn) this.burn else null),
            today,
        ) { true }

    private val three = arrayOf(SeriesKind.WEIGHT to SeriesStyle.LINE, SeriesKind.ENERGY to SeriesStyle.BARS, SeriesKind.STEPS to SeriesStyle.BARS)

    @Test
    fun `Allekkain has a panel per data in kind order with 112, 136 and 160 dp`() {
        val s = spec(chart(ChartLayout.STACKED, *three))
        assertEquals(listOf(SeriesKind.WEIGHT, SeriesKind.ENERGY, SeriesKind.STEPS), s.panels.map { it.kind })
        assertTrue(s.panels.all { it.heightDp == 112 })
        assertEquals(listOf(136, 136), spec(chart(ChartLayout.STACKED, SeriesKind.WEIGHT to SeriesStyle.LINE, SeriesKind.STEPS to SeriesStyle.BARS)).panels.map { it.heightDp })
        assertEquals(listOf(160), spec(chart(ChartLayout.STACKED, SeriesKind.STEPS to SeriesStyle.BARS)).panels.map { it.heightDp })
        assertTrue(s.panels.all { it.right == null })
        assertTrue(s.notDrawn.isEmpty())
    }

    @Test
    fun `a data without values is a message panel`() {
        val s = spec(chart(ChartLayout.STACKED, *three), steps = false)
        val panel = s.panels.single { it.kind == SeriesKind.STEPS }
        assertEquals(PanelMessage(SeriesKind.STEPS), panel.message)
        assertNull(panel.left)
        assertEquals(2, s.drawn.size)
    }

    @Test
    fun `Paallekkain is one plot in fractions labelled by the first two drawn data`() {
        val s = spec(chart(ChartLayout.OVERLAID, *three, average = true))
        val panel = s.panels.single()
        assertEquals(CustomChartSpec.OVERLAID_DP, panel.heightDp)
        assertEquals(ValueUnit.KG, panel.left!!.unit)
        assertEquals(ValueUnit.KCAL, panel.right!!.unit)
        assertEquals(0.0 to 1.0, panel.left!!.min to panel.left!!.max)
        assertEquals(listOf(Side.LEFT, Side.RIGHT, Side.HIDDEN), panel.series.map { it.side })
        // Every value is a fraction of its own scale; the third data still gets its average.
        assertTrue(panel.series.flatMap { it.shape.all }.all { it.y in 0.0..1.0 })
        assertTrue(panel.series[2].average != null)
        // Bars side by side: energy in slot 0, steps in slot 1 of the same day.
        val steps = (panel.series[2].shape as SeriesShape.Bars).points.first()
        val energy = (panel.series[1].shape as SeriesShape.Bars).points.first { it.day == steps.day }
        assertTrue(energy.x < steps.x)
        assertEquals(energy.day, s.dayAt(energy.x))
        assertEquals(steps.day, s.dayAt(steps.x))
        assertEquals(ToneRef.STEPS, panel.series[2].tone)
    }

    @Test
    fun `axes go to what is drawn and the rest is named under the plot`() {
        val s = spec(chart(ChartLayout.OVERLAID, *three), weights = false)
        val panel = s.panels.single()
        assertEquals(ValueUnit.KCAL, panel.left!!.unit)
        assertEquals(ValueUnit.STEPS, panel.right!!.unit)
        assertEquals(listOf(SeriesKind.WEIGHT), s.notDrawn)
    }

    @Test
    fun `one drawn data in Paallekkain is the stacked single panel`() {
        val s = spec(chart(ChartLayout.OVERLAID, *three), weights = false, food = false)
        assertEquals(listOf(SeriesKind.STEPS), s.panels.map { it.kind })
        assertEquals(160, s.panels.single().heightDp)
        assertEquals(listOf(SeriesKind.WEIGHT, SeriesKind.ENERGY), s.notDrawn)
    }

    @Test
    fun `Kulutus in Allekkain splits lepo and aktiivinen, a kokonais day in its own set`() {
        val s = spec(chart(ChartLayout.STACKED, SeriesKind.BURN to SeriesStyle.BARS))
        val series = s.panels.single().series
        assertEquals(2, series.size)
        assertEquals(listOf(1850.0, 450.0), (series[0].shape as SeriesShape.StackedBars).points.single().parts)
        assertEquals(LegendLabel.TOTAL, s.legend.last().label)
    }

    @Test
    fun `the marker rows follow the shown data with access`() {
        val data = CustomChartData.of(chart(ChartLayout.STACKED, *three), today, zone, weights, food, steps)
        val s = CustomChartSpec.of(data, today) { it != SeriesKind.STEPS }
        val card = s.readout(today.minusDays(1))
        assertEquals(listOf("paino", "syöty"), card.rows.map { it.name })
        assertEquals("ei painoa", card.rows[0].value)
    }

    @Test
    fun `nothing at all is empty`() {
        val data = CustomChartData.of(chart(ChartLayout.STACKED, *three).copy(period = Period.All), today, zone, emptyList(), emptyList(), emptyList())
        assertTrue(CustomChartSpec.of(data, today) { true }.empty)
    }

    private fun overlaid(vararg k: SeriesKind, burn: Boolean = true, weights: Boolean = true) =
        spec(chart(ChartLayout.OVERLAID, *k.map { it to if (it == SeriesKind.BURN) SeriesStyle.LINE else SeriesStyle.BARS }.toTypedArray()), burn = burn, weights = weights).panels.single()

    @Test
    fun `eaten and burned share the right axis in the neutral tone`() {
        val p = overlaid(SeriesKind.WEIGHT, SeriesKind.ENERGY, SeriesKind.BURN)
        assertEquals(ValueUnit.KG, p.left!!.unit)
        assertEquals(ValueUnit.KCAL, p.right!!.unit)
        assertNull(p.right!!.tone)
        assertEquals(listOf(Side.LEFT, Side.RIGHT, Side.RIGHT), p.series.map { it.side })
    }

    @Test
    fun `only eaten and burned has one axis on the left`() {
        val p = overlaid(SeriesKind.ENERGY, SeriesKind.BURN)
        assertEquals(ValueUnit.KCAL, p.left!!.unit)
        assertNull(p.left!!.tone)
        assertNull(p.right)
    }

    @Test
    fun `eaten, burned and steps`() {
        val p = overlaid(SeriesKind.ENERGY, SeriesKind.BURN, SeriesKind.STEPS)
        assertEquals(ValueUnit.KCAL, p.left!!.unit)
        assertEquals(ValueUnit.STEPS, p.right!!.unit)
        assertEquals(listOf(Side.LEFT, Side.LEFT, Side.RIGHT), p.series.map { it.side })
    }

    @Test
    fun `shared scale frees the second axis`() {
        val p = overlaid(SeriesKind.WEIGHT, SeriesKind.ENERGY, SeriesKind.BURN, SeriesKind.STEPS)
        assertEquals(listOf(Side.LEFT, Side.RIGHT, Side.RIGHT, Side.HIDDEN), p.series.map { it.side })
    }

    @Test
    fun `burned without permission leaves eaten's own right axis`() {
        val p = overlaid(SeriesKind.WEIGHT, SeriesKind.ENERGY, SeriesKind.BURN, burn = false)
        assertEquals(ValueUnit.KCAL, p.right!!.unit)
        assertEquals(ToneRef.FOOD, p.right!!.tone)
    }

    @Test
    fun `a running line stops before today and today is a separate one-point series`() {
        val s = spec(chart(ChartLayout.STACKED, SeriesKind.STEPS to SeriesStyle.LINE, SeriesKind.WEIGHT to SeriesStyle.LINE))
        val steps = s.panels.single { it.kind == SeriesKind.STEPS }.series
        assertEquals(listOf("STEPS", "STEPS.today"), steps.map { it.id })
        val line = (steps[0].shape as SeriesShape.Line).points
        assertEquals(9, line.size)
        assertTrue(line.none { it.day == today })
        val dot = (steps[1].shape as SeriesShape.Line).points.single()
        assertEquals(today, dot.day)
        assertEquals(steps[0].tone, steps[1].tone)
        assertEquals(steps[0].side, steps[1].side)
        assertEquals(1, (steps[1].shape as SeriesShape.Line).dots.shown(1, 300f).size)
    }

    @Test
    fun `only today is a lone dot with no line`() {
        val data = CustomChartData.of(chart(ChartLayout.STACKED, SeriesKind.STEPS to SeriesStyle.LINE, average = true), today, zone, null, null, listOf(StepsDay(today, 3000)), burnDays = null)
        val series = CustomChartSpec.of(data, today) { true }.panels.single().series
        assertEquals(listOf("STEPS.today"), series.map { it.id })
    }

    @Test
    fun `overlaid lines also leave today out and the dot shares the side`() {
        val s = spec(chart(ChartLayout.OVERLAID, SeriesKind.ENERGY to SeriesStyle.LINE, SeriesKind.STEPS to SeriesStyle.LINE, average = true))
        val series = s.panels.single().series
        assertEquals(listOf("ENERGY", "ENERGY.today", "STEPS", "STEPS.today"), series.map { it.id })
        assertEquals(listOf(Side.LEFT, Side.LEFT, Side.RIGHT, Side.RIGHT), series.map { it.side })
        assertTrue(series.filter { it.id.endsWith(".today") }.all { it.average == null })
        assertTrue(series.filter { !it.id.endsWith(".today") }.all { s0 -> (s0.shape as SeriesShape.Line).points.none { it.day == today } })
    }

    @Test
    fun `the weight line still joins today`() {
        val todayWeight = weightEntry("t", today.atTime(7, 0).atZone(zone).toInstant(), null, 82.0, "me", "me")
        val data = CustomChartData.of(chart(ChartLayout.STACKED, SeriesKind.WEIGHT to SeriesStyle.LINE), today, zone, listOf(todayWeight) + weights, null, null, burnDays = null)
        val series = CustomChartSpec.of(data, today) { true }.panels.single().series
        assertEquals(listOf("WEIGHT"), series.map { it.id })
        assertTrue((series.single().shape as SeriesShape.Line).points.any { it.day == today })
    }

    @Test
    fun `bars are unchanged and today stays in them`() {
        val s = spec(chart(ChartLayout.STACKED, SeriesKind.STEPS to SeriesStyle.BARS))
        val series = s.panels.single().series
        assertEquals(listOf("STEPS"), series.map { it.id })
        assertTrue((series.single().shape as SeriesShape.Bars).points.any { it.day == today })
    }
}
