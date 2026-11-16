package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.weight.weightEntry
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Made-up weights only (NFR-1). */
class WeightScaleTest {
    @Test
    fun `steady weight gets one kilo`() {
        assertEquals(KgRange(97.5, 98.5), WeightScale.of(listOf(97.76, 97.84)))
    }

    @Test
    fun `a week of small moves fits with half-kilo axis values`() {
        assertEquals(KgRange(96.5, 98.5), WeightScale.of(listOf(97.0, 97.5, 98.0)))
    }

    @Test
    fun `a real change widens the scale`() {
        assertEquals(KgRange(92.0, 99.0), WeightScale.of(listOf(92.3, 95.0, 98.6)))
    }

    @Test
    fun `single weight gets one kilo`() {
        val single = WeightScale.of(listOf(80.0))
        assertEquals(KgRange(79.5, 80.5), single)
        assertEquals(1.0, single.span)
    }

    @Test
    fun `steady bmi is fitted and on half units`() {
        val perKg = 1 / (1.8 * 1.8)
        val r = WeightScale.of(listOf(25.2, 25.3), perKg)
        assertTrue(r.span >= 0.5)
        assertEquals(0.0, (r.min * 2) % 1.0)
        assertEquals(0.0, (r.max * 2) % 1.0)
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
        assertEquals(KgRange(97.5, 98.5), summary.sparkScale)
    }
}
