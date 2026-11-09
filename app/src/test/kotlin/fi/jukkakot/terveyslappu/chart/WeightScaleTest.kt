package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.weight.weightEntry
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

/** Made-up weights only (NFR-1). */
class WeightScaleTest {
    @Test
    fun `steady weight gets the minimum span and looks flat`() {
        assertEquals(KgRange(95.0, 100.0), WeightScale.of(listOf(97.76, 97.84)))
    }

    @Test
    fun `a real change widens the scale`() {
        assertEquals(KgRange(91.0, 100.0), WeightScale.of(listOf(92.3, 95.0, 98.6)))
    }

    @Test
    fun `single weight and whole kilos stay at least four kilos`() {
        val single = WeightScale.of(listOf(80.0))
        assertEquals(KgRange(78.0, 82.0), single)
        assertEquals(4.0, single.span)
    }

    @Test
    fun `home sparkline spans the whole default week with the shared scale`() {
        val now = Instant.parse("2026-11-09T07:05:00Z")
        val weights = listOf(
            weightEntry("a", now.minusSeconds(86_400), null, 97.84, "me", "me"),
            weightEntry("b", now, null, 97.76, "me", "me"),
        )
        val summary = HomeSummary.of(weights, now)!!
        assertEquals(now.minus(java.time.Duration.ofDays(7)), summary.sparkFrom)
        assertEquals(now, summary.sparkTo)
        assertEquals(KgRange(95.0, 100.0), summary.sparkScale)
    }
}
