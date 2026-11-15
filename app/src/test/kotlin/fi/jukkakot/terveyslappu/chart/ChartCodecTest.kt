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
}
