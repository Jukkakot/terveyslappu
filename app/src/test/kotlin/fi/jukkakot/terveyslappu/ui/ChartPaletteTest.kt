package fi.jukkakot.terveyslappu.ui

import fi.jukkakot.terveyslappu.chart.SeriesColour
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** energy-burn review decision 1: every full shade at least 3:1 against the card surface. */
class ChartPaletteTest {
    @Test
    fun `every colour has both shades with enough contrast`() {
        assertEquals(SeriesColour.entries.toSet(), ChartPalette.shades.keys)
        for ((colour, shades) in ChartPalette.shades) {
            val light = ChartPalette.contrast(shades.light, ChartPalette.LIGHT_SURFACE)
            val dark = ChartPalette.contrast(shades.dark, ChartPalette.DARK_SURFACE)
            assertTrue(light >= 3.0, "$colour light $light")
            assertTrue(dark >= 3.0, "$colour dark $dark")
        }
    }
}
