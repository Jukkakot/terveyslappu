package fi.jukkakot.terveyslappu.derived

import fi.jukkakot.terveyslappu.auto.AutoText
import fi.jukkakot.terveyslappu.chart.HomeSummary
import fi.jukkakot.terveyslappu.chart.changesFromPrevious
import fi.jukkakot.terveyslappu.chart.periodStats
import fi.jukkakot.terveyslappu.ui.formatKg
import fi.jukkakot.terveyslappu.weight.weightEntry
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class ShownChangeTest {
    private val now = Instant.parse("2026-11-09T07:05:00Z")
    private fun weight(id: String, daysAgo: Long, kg: Double) =
        weightEntry(id, now.minusSeconds(daysAgo * 86_400), null, kg, "me", "me")

    @Test
    fun `tenths round like the shown text`() {
        assertEquals(270, shownTenths(27.049999999999997))
        assertEquals("27,0", formatKg(27.049999999999997))
        assertEquals(823, shownTenths(82.34))
        assertEquals(818, shownTenths(81.76))
        assertEquals(-5, shownDeltaTenths(82.34, 81.76))
    }

    @Test
    fun `card change is the difference of the shown weights`() {
        val summary = HomeSummary.of(listOf(weight("a", 0, 81.76), weight("b", 6, 82.34)), now)!!
        assertEquals(-0.5, summary.sparkChange!!, 1e-9)
    }

    @Test
    fun `row change is the difference of the shown weights`() {
        val changes = changesFromPrevious(listOf(weight("a", 0, 81.76), weight("b", 1, 82.04)))
        assertEquals(-0.2, changes["a"]!!, 1e-9)
    }

    @Test
    fun `bmi change uses shown values`() {
        val stats = periodStats(listOf(weight("a", 5, 27.049999999999997), weight("b", 0, 26.94)), { it.kg })!!
        assertEquals(-0.1, stats.change!!, 1e-9)
    }

    @Test
    fun `weekly rate comes from the shown change`() {
        val stats = periodStats(listOf(weight("a", 6, 82.34), weight("b", 0, 81.76)))!!
        assertEquals(-0.5, stats.change!!, 1e-9)
        assertEquals(-0.5 / 6 * 7, stats.perWeek!!, 1e-9)
        val six = periodStats(listOf(weight("a", 6, 82.4), weight("b", 0, 81.8)))!!
        assertEquals("−0,7", fi.jukkakot.terveyslappu.ui.formatRate(six.perWeek!!))
    }

    @Test
    fun `notification change is as shown`() {
        val note = AutoText.of(81.76, false, fi.jukkakot.terveyslappu.auto.Reference.Weight(82.34, 0L), null, java.time.ZoneId.of("Europe/Helsinki"))
        assertEquals("81,8 kg tallennettu", note.title)
        assertEquals("−0,5 kg edellisestä", note.text)
    }
}
