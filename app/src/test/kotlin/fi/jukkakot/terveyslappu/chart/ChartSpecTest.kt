package fi.jukkakot.terveyslappu.chart

import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** The chart x mapping and picking rules (charts-on-mpandroidchart design 2, review decisions 4, 5, 11). */
class ChartSpecTest {
    private val zone = ZoneId.of("Europe/Helsinki")
    private val from = LocalDate.of(2026, 11, 2)
    private val month = ChartX(ChartAxis(from, from.plusDays(29)))

    @Test
    fun `a day sits at its index, the axis runs half a day past both ends`() {
        assertEquals(0.0, month.of(from))
        assertEquals(29.0, month.of(from.plusDays(29)))
        assertEquals(-0.5, month.min)
        assertEquals(29.5, month.max)
    }

    @Test
    fun `a weight at 7_05 lies inside its day's slot`() {
        val x = month.of(from.plusDays(3).atTime(7, 5).atZone(zone).toInstant(), zone)
        assertEquals(3 - 0.5 + (7 * 3600 + 5 * 60) / 86_400.0, x, 1e-9)
        assertEquals(from.plusDays(3), month.dayAt(x))
    }

    @Test
    fun `dayAt round trips every day and clamps outside the axis`() {
        for (i in 0L until 30L) assertEquals(from.plusDays(i), month.dayAt(month.of(from.plusDays(i))))
        assertEquals(from, month.dayAt(-3.0))
        assertEquals(from.plusDays(29), month.dayAt(99.0))
        // Just before midnight still belongs to the day.
        assertEquals(from.plusDays(4), month.dayAt(4.49))
    }

    @Test
    fun `a weekly bar sits at the centre of its days inside the axis and keys to its first day there`() {
        // 2026-11-04 is a Wednesday: the first week has Ke..Su inside the axis.
        val axis = ChartAxis(LocalDate.of(2026, 11, 4), LocalDate.of(2027, 2, 28), weekly = true)
        val x = ChartX(axis)
        assertEquals((0.0 + 4.0) / 2, x.ofWeek(LocalDate.of(2026, 11, 2)))
        assertEquals((5.0 + 11.0) / 2, x.ofWeek(LocalDate.of(2026, 11, 9)))
        assertEquals(LocalDate.of(2026, 11, 4), x.dayAt(x.ofWeek(LocalDate.of(2026, 11, 2))))
        assertEquals(LocalDate.of(2026, 11, 9), x.dayAt(x.ofWeek(LocalDate.of(2026, 11, 9))))
        assertEquals(LocalDate.of(2026, 11, 9), axis.pick(LocalDate.of(2026, 11, 13)))
    }

    private fun barPanel(vararg days: Long) = PanelSpec(
        160, AxisSpec(0.0, 10.0, ValueUnit.STEPS),
        series = listOf(SeriesSpec("s", ToneRef.PRIMARY, SeriesShape.Bars(days.map { ChartPoint(it.toDouble(), 5.0, from.plusDays(it)) }))),
    )

    @Test
    fun `a tap on a bar's slot picks its day, an empty day picks nothing`() {
        val panel = barPanel(2, 3, 7)
        assertEquals(from.plusDays(3), panel.pick(3.4, 0.8, month))
        assertNull(panel.pick(5.0, 0.8, month))
    }

    @Test
    fun `a line picks the nearest point within reach`() {
        val points = listOf(8L, 11L).map { ChartPoint(month.of(from.plusDays(it)), 82.0, from.plusDays(it)) }
        val panel = PanelSpec(160, AxisSpec(80.0, 84.0, ValueUnit.KG), series = listOf(SeriesSpec("w", ToneRef.PRIMARY, SeriesShape.Line(points, Dots.All))))
        assertEquals(from.plusDays(8), panel.pick(9.0, 2.5, month))
        assertNull(panel.pick(9.5, 1.0, month))
    }

    @Test
    fun `side by side bars map back to their day`() {
        val offset = -0.35 + 1.5 * 0.35
        val panel = PanelSpec(
            200, AxisSpec(0.0, 1.0, ValueUnit.KCAL),
            series = listOf(SeriesSpec("s", ToneRef.PRIMARY, SeriesShape.Bars(listOf(ChartPoint(4 + offset, 0.5, from.plusDays(4))), slots = 2))),
        )
        assertEquals(from.plusDays(4), month.dayAt(4 + offset))
        assertEquals(from.plusDays(4), panel.pick(3.7, 0.5, month))
    }

    @Test
    fun `the chosen day's points are the exact entries, the day's last weight`() {
        val day = from.plusDays(5)
        val early = ChartPoint(month.of(day.atTime(7, 0).atZone(zone).toInstant(), zone), 82.0, day)
        val late = ChartPoint(month.of(day.atTime(20, 0).atZone(zone).toInstant(), zone), 82.4, day)
        val panel = PanelSpec(160, AxisSpec(80.0, 84.0, ValueUnit.KG), series = listOf(SeriesSpec("w", ToneRef.PRIMARY, SeriesShape.Line(listOf(early, late), Dots.All))))
        assertEquals(mapOf(0 to late), panel.pointsAt(day, month.axis))
        assertEquals(emptyMap(), panel.pointsAt(day.plusDays(1), month.axis))
    }

    @Test
    fun `dots - all when they fit, else the lone ones or the last`() {
        assertEquals(setOf(0, 1, 2), Dots.FitOrLone(setOf(1)).shown(3, 300f))
        assertEquals(setOf(1), Dots.FitOrLone(setOf(1)).shown(100, 300f))
        assertEquals(setOf(99), Dots.FitOrLast.shown(100, 300f))
        assertEquals(setOf(0), Dots.None.shown(1, 300f))
        assertEquals(emptySet(), Dots.None.shown(5, 300f))
    }

    @Test
    fun `an overlaid axis labels the data's own scale`() {
        val axis = AxisSpec(0.0, 1.0, ValueUnit.KCAL, valueMin = 0.0, valueMax = 3000.0)
        assertEquals(listOf(0.0, 0.5, 1.0), axis.ticks)
        assertEquals(1500.0, axis.value(0.5))
    }
}
