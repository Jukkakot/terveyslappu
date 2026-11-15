package fi.jukkakot.terveyslappu.chart

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ChartCodecTest {
    private fun chart(id: Int, period: Period) = CustomCharts.newDraft(emptyList()).copy(name = "Kaavio $id", period = period).toChart(id)

    @Test
    fun `round trip with all three period kinds`() {
        val list = ChartList(
            listOf(
                chart(1, Period.Last(30)),
                chart(2, Period.All).copy(layout = ChartLayout.OVERLAID, series = listOf(ChartSeries(SeriesKind.STEPS, SeriesStyle.LINE))),
                chart(4, Period.Custom(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 15))),
            ),
            next = 5,
        )
        val text = ChartCodec.encode(list)
        assertEquals(list, ChartCodec.decode(text))
        assertEquals(true, text.startsWith("""{"v":1,"next":5,"charts":[{"id":1,"name":"Kaavio 1","layout":"stacked","period":{"k":"last","days":30}"""), text)
    }

    @Test
    fun `colours and Kulutus round trip, an old chart reads as Oletus`() {
        val series = listOf(
            ChartSeries(SeriesKind.ENERGY, SeriesStyle.BARS, SeriesColour.VIHREA),
            ChartSeries(SeriesKind.BURN, SeriesStyle.LINE),
            ChartSeries(SeriesKind.STEPS, SeriesStyle.BARS, SeriesColour.PUNAINEN),
        )
        val list = ChartList(listOf(chart(1, Period.MONTH).copy(series = series)), next = 2)
        val text = ChartCodec.encode(list)
        assertEquals(list, ChartCodec.decode(text))
        assertEquals(true, """{"kind":"burn","style":"line"}""" in text && """"colour":"punainen"""" in text, text)
        val old = """{"v":1,"next":2,"charts":[{"id":1,"name":"A","layout":"stacked","period":{"k":"all"},"series":[{"kind":"steps","style":"bars"},{"kind":"weight","style":"line"}]}]}"""
        val read = ChartCodec.decode(old)!!.charts.single()
        assertEquals(listOf(SeriesKind.WEIGHT, SeriesKind.STEPS), read.series.map { it.kind })
        assertEquals(listOf(null, null), read.series.map { it.colour })
        val unknown = old.replace("""{"kind":"steps","style":"bars"}""", """{"kind":"steps","style":"bars","colour":"pinkki"}""")
        assertNull(ChartCodec.decode(unknown)!!.charts.single().colourOf(SeriesKind.STEPS))
    }

    @Test
    fun `nothing stored is no charts, a bad value is null`() {
        assertEquals(ChartList(), ChartCodec.decode(null))
        assertEquals(ChartList(), ChartCodec.decode(""))
        for (bad in listOf("{nope", """{"v":2,"next":1,"charts":[]}""", """{"v":1,"next":2,"charts":[{"id":1,"name":"A","layout":"x","period":{"k":"all"},"series":[{"kind":"weight","style":"line"}]}]}""")) {
            assertNull(ChartCodec.decode(bad), bad)
        }
    }

    @Test
    fun `log period never has dates`() {
        assertEquals("last30", CustomCharts.logPeriod(Period.MONTH))
        assertEquals("all", CustomCharts.logPeriod(Period.All))
        assertEquals("custom", CustomCharts.logPeriod(Period.Custom(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2))))
    }

    @Test
    fun `the average flag round trips and old JSON reads as off`() {
        val on = chart(1, Period.MONTH).copy(showAverage = true)
        val list = ChartList(listOf(on, chart(2, Period.MONTH)), next = 3)
        val text = ChartCodec.encode(list)
        assertEquals(list, ChartCodec.decode(text))
        assertEquals(true, ChartCodec.decode(text)!!.charts[0].showAverage)
        assertEquals(false, ChartCodec.decode(text)!!.charts[1].showAverage)
        val old = """{"v":1,"next":2,"charts":[{"id":1,"name":"A","layout":"stacked","period":{"k":"all"},"series":[{"kind":"weight","style":"line"}]}]}"""
        assertEquals(false, ChartCodec.decode(old)!!.charts.single().showAverage)
    }

    @Test
    fun `a draft and an undo keep the average flag`() {
        val chart = chart(1, Period.MONTH).copy(showAverage = true)
        assertEquals(true, ChartDraft.of(chart).showAverage)
        assertEquals(chart, ChartDraft.of(chart).toChart())
        val list = CustomCharts.restore(ChartList(emptyList(), 2), chart, 0)
        assertEquals(true, list.charts.single().showAverage)
        assertEquals(false, CustomCharts.newDraft(emptyList()).showAverage)
        assertEquals(true, CustomCharts.setShowAverage(CustomCharts.newDraft(emptyList()), true).showAverage)
    }
}
