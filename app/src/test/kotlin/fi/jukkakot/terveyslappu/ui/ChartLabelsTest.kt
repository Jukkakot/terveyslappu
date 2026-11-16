package fi.jukkakot.terveyslappu.ui

import fi.jukkakot.terveyslappu.chart.ChartAxis
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The date label formatter and the marker card's placement (design 4, 5). */
class ChartLabelsTest {
    @Test
    fun `a week is labelled by weekday under each day, Su at the end`() {
        val sunday = LocalDate.of(2026, 11, 15)
        val week = ChartAxis(sunday.minusDays(6), sunday)
        assertEquals(listOf("Ma", "Ti", "Ke", "To", "Pe", "La", "Su"), (0..6).map { dateLabelAt(week, it.toFloat(), 7f) })
        val wednesday = LocalDate.of(2026, 11, 18)
        val midweek = ChartAxis(wednesday.minusDays(6), wednesday)
        assertEquals(listOf("To", "Pe", "La", "Su", "Ma", "Ti", "Ke"), (0..6).map { dateLabelAt(midweek, it.toFloat(), 7f) })
    }

    @Test
    fun `day and month, month and year from 365 visible days`() {
        val to = LocalDate.of(2026, 11, 15)
        val year = ChartAxis(to.minusDays(364), to)
        assertEquals("11/2025", dateLabelAt(year, 0f, 365f))
        // Zoomed in under a year: day and month again.
        assertEquals("16.11.", dateLabelAt(year, 0f, 120f))
        val month = ChartAxis(to.minusDays(29), to)
        assertEquals("15.11.", dateLabelAt(month, 29f, 30f))
        // Outside the axis clamps to its ends.
        assertEquals("15.11.", dateLabelAt(month, 40f, 30f))
    }

    @Test
    fun `the marker card stays inside the plot at both ends`() {
        val (min, max) = 40f to 340f
        val width = 120f
        for (anchor in listOf(min, min + 3, 190f, max - 3, max)) {
            val left = markerLeft(anchor, width, 8f, min, max)
            assertTrue(left >= min && left + width <= max, "anchor $anchor: $left")
        }
        // Right of the day when it fits, left of it near the right edge.
        assertEquals(58f, markerLeft(50f, width, 8f, min, max))
        assertEquals(330f - 8f - width, markerLeft(330f, width, 8f, min, max))
    }
}
