package fi.jukkakot.terveyslappu.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import fi.jukkakot.terveyslappu.chart.QuantityTone
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** app-appearance "Quantity colours": contrast of every shade (visual-identity design 3). */
class QuantityColoursTest {
    private val lightSurfaces = listOf(BrandColours.LIGHT_CARD, ChartPalette.LIGHT_SURFACE)
    private val darkSurfaces = listOf(BrandColours.DARK_CARD, ChartPalette.DARK_SURFACE)

    @Test
    fun `every main shade is 3 to 1 on the card surfaces and its past-day shade 2 to 1`() {
        assertEquals(QuantityTone.entries.toSet(), QuantityColours.shades.keys)
        for (tone in QuantityColours.main) {
            val s = QuantityColours.shades.getValue(tone)
            lightSurfaces.forEach { assertTrue(ChartPalette.contrast(s.light, it) >= 3.0, "$tone light on $it") }
            darkSurfaces.forEach { assertTrue(ChartPalette.contrast(s.dark, it) >= 3.0, "$tone dark on $it") }
            val pastLight = s.light.copy(alpha = QuantityColours.PAST_ALPHA).compositeOver(BrandColours.LIGHT_CARD)
            val pastDark = s.dark.copy(alpha = QuantityColours.PAST_ALPHA).compositeOver(BrandColours.DARK_CARD)
            assertTrue(ChartPalette.contrast(pastLight, BrandColours.LIGHT_CARD) >= 2.0, "$tone past light")
            assertTrue(ChartPalette.contrast(pastDark, BrandColours.DARK_CARD) >= 2.0, "$tone past dark")
        }
    }

    @Test
    fun `herkku and lepo are told apart from their main part`() {
        for ((lighter, main) in listOf(QuantityTone.TREAT to QuantityTone.FOOD, QuantityTone.REST to QuantityTone.BURN)) {
            for (dark in listOf(false, true)) {
                val ratio = ChartPalette.contrast(QuantityColours.of(lighter, dark), QuantityColours.of(main, dark))
                assertTrue(ratio >= 1.5, "$lighter vs $main dark=$dark: $ratio")
            }
        }
    }

    @Test
    fun `the brand primary reads 3 to 1 on its cards`() {
        for (dark in listOf(false, true)) {
            val scheme = BrandColours.scheme(dark)
            assertTrue(ChartPalette.contrast(scheme.primary, scheme.surfaceContainerLow) >= 3.0, "primary dark=$dark")
            assertTrue(ChartPalette.contrast(scheme.onSurface, scheme.surfaceContainerLow) >= 4.5, "text dark=$dark")
            assertTrue(ChartPalette.contrast(scheme.onSurfaceVariant, scheme.surfaceContainerLow) >= 4.5, "muted text dark=$dark")
            assertEquals(Color(0xFF16191E), BrandColours.scheme(true).surfaceContainerLow)
        }
    }
}
