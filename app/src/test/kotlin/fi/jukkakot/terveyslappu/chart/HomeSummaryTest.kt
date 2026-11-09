package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.weight.weightEntry
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HomeSummaryTest {
    private val now = Instant.parse("2026-11-09T07:05:00Z")
    private fun weight(id: String, daysAgo: Long, kg: Double) =
        weightEntry(id, now.minusSeconds(daysAgo * 86_400), null, kg, "me", "me")

    @Test
    fun `no weights gives no summary`() {
        assertNull(HomeSummary.of(emptyList(), now))
    }

    @Test
    fun `latest, change from the one before and the three latest, in any input order`() {
        val summary = HomeSummary.of(listOf(weight("b", 1, 82.9), weight("c", 2, 82.5), weight("a", 0, 82.4), weight("d", 3, 82.7)), now)!!
        assertEquals("a", summary.latest.id)
        assertEquals(-0.5, summary.change!!, 1e-9)
        assertEquals(listOf("a", "b", "c"), summary.recent.map { it.id })
    }

    @Test
    fun `sparkline holds the last 30 days oldest first`() {
        val summary = HomeSummary.of(listOf(weight("a", 0, 82.4), weight("b", 29, 84.2), weight("old", 31, 85.0)), now, 30)!!
        assertEquals(listOf("b", "a"), summary.spark.map { it.id })
        assertEquals(-1.8, summary.sparkChange!!, 1e-9)
    }

    @Test
    fun `a single weight has no change and an old one no sparkline`() {
        val summary = HomeSummary.of(listOf(weight("a", 40, 82.4)), now)!!
        assertNull(summary.change)
        assertEquals(emptyList(), summary.spark)
        assertNull(summary.sparkChange)
    }

    @Test
    fun `home period defaults to a week and only takes its choices`() {
        val weights = listOf(weight("a", 0, 82.4), weight("b", 6, 82.0), weight("c", 20, 83.0))
        assertEquals(listOf("b", "a"), HomeSummary.of(weights, now)!!.spark.map { it.id })
        assertEquals(listOf("c", "b", "a"), HomeSummary.of(weights, now, 30)!!.spark.map { it.id })
        assertEquals(7, HomeSummary.homeDays(14))
        assertEquals(90, HomeSummary.homeDays(90))
    }
}
