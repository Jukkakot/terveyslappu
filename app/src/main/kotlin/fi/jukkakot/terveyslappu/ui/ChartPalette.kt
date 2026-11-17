package fi.jukkakot.terveyslappu.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import fi.jukkakot.terveyslappu.chart.SeriesColour
import kotlin.math.max
import kotlin.math.min

/**
 * The own charts' fixed data colours (weight-chart "Data colours in own charts", energy-burn review
 * decision 1): a light-theme and a dark-theme shade per colour, each at least 3:1 against the card
 * surface ([LIGHT_SURFACE], [DARK_SURFACE]; `ChartPaletteTest`). The lighter tone (treats, lepo) is
 * the shade at [LIGHT_ALPHA]; a kokonais Kulutus day (no split) at [TOTAL_ALPHA].
 */
internal object ChartPalette {
    data class Shades(val light: Color, val dark: Color)

    val shades: Map<SeriesColour, Shades> = mapOf(
        SeriesColour.SININEN to Shades(Color(0xFF1E66D0), Color(0xFF8AB4F8)),
        SeriesColour.VIHREA to Shades(Color(0xFF2E7D32), Color(0xFF81C995)),
        SeriesColour.ORANSSI to Shades(Color(0xFFB85C00), Color(0xFFFCAD70)),
        SeriesColour.PUNAINEN to Shades(Color(0xFFC62828), Color(0xFFF28B82)),
        SeriesColour.VIOLETTI to Shades(Color(0xFF7B1FA2), Color(0xFFD7AEFB)),
        SeriesColour.TURKOOSI to Shades(Color(0xFF00838F), Color(0xFF78D9EC)),
        SeriesColour.KELTAINEN to Shades(Color(0xFF8A6A00), Color(0xFFFDD663)),
        SeriesColour.HARMAA to Shades(Color(0xFF5F6368), Color(0xFFBDC1C6)),
    )

    const val LIGHT_ALPHA = 0.45f
    const val TOTAL_ALPHA = 0.7f

    val LIGHT_SURFACE = Color(0xFFF3EDF7)
    val DARK_SURFACE = Color(0xFF211F26)

    /** WCAG contrast ratio of two opaque colours. */
    fun contrast(a: Color, b: Color): Double {
        val la = a.luminance().toDouble()
        val lb = b.luminance().toDouble()
        return (max(la, lb) + 0.05) / (min(la, lb) + 0.05)
    }
}

/** [colour]'s shade for the current theme ([isDarkTheme]). */
@Composable
internal fun paletteColour(colour: SeriesColour): Color {
    val shades = ChartPalette.shades.getValue(colour)
    return if (isDarkTheme()) shades.dark else shades.light
}

/** The Kulutus colour (coral) for the current theme. */
@Composable
internal fun burnColour(): Color = quantityColour(fi.jukkakot.terveyslappu.chart.QuantityTone.BURN)
