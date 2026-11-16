package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.chart.SeriesKind.ENERGY
import fi.jukkakot.terveyslappu.chart.SeriesKind.STEPS
import fi.jukkakot.terveyslappu.chart.SeriesKind.WEIGHT
import fi.jukkakot.terveyslappu.chart.SeriesStyle.BARS
import fi.jukkakot.terveyslappu.chart.SeriesStyle.LINE
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CustomChartsTest {
    private fun chart(id: Int, name: String) = CustomCharts.newDraft(emptyList()).copy(name = name).toChart(id)

    @Test
    fun `a new draft has all data but Kulutus, weight as a line, stacked, 1 kk`() {
        val draft = CustomCharts.newDraft(emptyList())
        assertEquals("Kaavio 1", draft.name)
        val chart = draft.toChart(1)
        assertEquals(listOf(ChartSeries(WEIGHT, LINE), ChartSeries(ENERGY, BARS), ChartSeries(STEPS, BARS)), chart.series)
        assertEquals(ChartLayout.STACKED, chart.layout)
        assertEquals(Period.Last(30), chart.period)
        assertEquals(listOf(null, null, null), chart.series.map { it.colour })
    }

    @Test
    fun `a colour is kept per data, also while off, and Kulutus sorts after Syöty`() {
        var draft = CustomCharts.newDraft(emptyList())
        draft = CustomCharts.toggle(draft, SeriesKind.BURN)
        draft = CustomCharts.setColour(draft, SeriesKind.BURN, SeriesColour.TURKOOSI)
        draft = CustomCharts.setColour(draft, STEPS, SeriesColour.PUNAINEN)
        val chart = draft.toChart(1)
        assertEquals(listOf(WEIGHT, ENERGY, SeriesKind.BURN, STEPS), chart.series.map { it.kind })
        assertEquals(SeriesColour.TURKOOSI, chart.colourOf(SeriesKind.BURN))
        val off = CustomCharts.toggle(draft, STEPS)
        assertEquals(SeriesColour.PUNAINEN, off.colourOf(STEPS))
        assertEquals(chart, ChartDraft.of(chart).toChart(1))
    }

    @Test
    fun `default name takes the lowest free n, ignoring case and spaces`() {
        assertEquals("Kaavio 2", CustomCharts.newDraft(listOf(chart(1, "Kaavio 1"))).name)
        assertEquals("Kaavio 2", CustomCharts.newDraft(listOf(chart(1, " kaavio 1 "))).name)
        assertEquals("Kaavio 1", CustomCharts.newDraft(listOf(chart(1, "Ilta"))).name, "free again after a rename")
        assertEquals("Kaavio 2", CustomCharts.newDraft(listOf(chart(1, "Kaavio 1"), chart(2, "Kaavio 3"))).name)
    }

    @Test
    fun `names are 1 to 24 graphemes trimmed, an emoji counts one`() {
        assertFalse(CustomCharts.validName("   "))
        assertTrue(CustomCharts.validName(" Ilta "))
        assertTrue(CustomCharts.validName("😀".repeat(24)))
        assertFalse(CustomCharts.validName("😀".repeat(25)))
        assertTrue(CustomCharts.validName("a".repeat(24)))
        assertFalse(CustomCharts.validName("a".repeat(25)))
        assertEquals("😀".repeat(24), CustomCharts.cap("😀".repeat(30)))
        assertEquals("Ilta", CustomCharts.newDraft(emptyList()).copy(name = "  Ilta ").toChart(1).name)
    }

    @Test
    fun `Paino stays a line whatever is set, other data can be bars`() {
        var draft = CustomCharts.setStyle(CustomCharts.newDraft(emptyList()), WEIGHT, BARS)
        assertEquals(LINE, draft.styleOf(WEIGHT))
        assertEquals(LINE, draft.copy(styles = draft.styles + (WEIGHT to BARS)).toChart(1).series.first { it.kind == WEIGHT }.style)
        draft = CustomCharts.setStyle(draft, ENERGY, LINE)
        assertEquals(BARS, CustomCharts.setStyle(draft, ENERGY, BARS).styleOf(ENERGY))
        assertEquals(BARS, draft.styleOf(STEPS))
    }

    @Test
    fun `the last data stays on and a switched-off kind keeps its style`() {
        var draft = CustomCharts.newDraft(emptyList())
        draft = CustomCharts.setStyle(draft, STEPS, LINE)
        draft = CustomCharts.toggle(draft, STEPS)
        assertFalse(STEPS in draft.on)
        draft = CustomCharts.toggle(draft, STEPS)
        assertEquals(LINE, draft.styleOf(STEPS))
        draft = CustomCharts.toggle(CustomCharts.toggle(draft, WEIGHT), ENERGY)
        assertEquals(setOf(STEPS), draft.on)
        assertEquals(setOf(STEPS), CustomCharts.toggle(draft, STEPS).on)
        assertEquals(listOf(ChartSeries(STEPS, LINE)), draft.toChart(1).series)
    }

    @Test
    fun `add gives ids from next, update and delete keep the others, duplicates allowed`() {
        var list = ChartList()
        val (l1, a) = CustomCharts.add(list, chart(0, "Ilta"))
        val (l2, b) = CustomCharts.add(l1, chart(0, "Ilta"))
        list = l2
        assertEquals(1, a.id)
        assertEquals(2, b.id)
        assertEquals(3, list.next)
        assertEquals(2, list.charts.size)
        list = CustomCharts.update(list, b.copy(name = " Aamu "))
        assertEquals(listOf("Ilta", "Aamu"), list.charts.map { it.name })
        list = CustomCharts.delete(list, 1)
        assertEquals(listOf(2), list.charts.map { it.id })
        val (l3, c) = CustomCharts.add(list, chart(0, "Uusi"))
        assertEquals(3, c.id, "ids are never reused")
        assertEquals(4, l3.next)
        assertEquals(Period.YEAR, CustomCharts.setPeriod(c, Period.YEAR).period)
    }

    @Test
    fun `card ids`() {
        assertEquals("chart:7", CustomChart.cardId(7))
        assertEquals(7, CustomChart.idOf("chart:7"))
        assertEquals(null, CustomChart.idOf("weight"))
    }
}
