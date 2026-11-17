package fi.jukkakot.terveyslappu.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import fi.jukkakot.terveyslappu.chart.QuantityTone
import fi.jukkakot.terveyslappu.chart.SeriesKind

/**
 * One fixed colour per quantity (app-appearance "Quantity colours"), the same in both colour
 * choices: paino/BMI mint, Syöty amber (herkku lighter), Askeleet blue, Kulutus coral (lepo lighter).
 * Every main shade reaches 3:1 against the card surfaces and the 60 % past-day shade 2:1
 * (`QuantityColoursTest`); herkku and lepo only need to be told apart from their main part.
 */
internal object QuantityColours {
    data class Shades(val light: Color, val dark: Color)

    val shades: Map<QuantityTone, Shades> = mapOf(
        QuantityTone.WEIGHT to Shades(Color(0xFF1F6B5A), Color(0xFF4DF0B8)),
        QuantityTone.FOOD to Shades(Color(0xFF96590A), Color(0xFFE9A23B)),
        QuantityTone.TREAT to Shades(Color(0xFFE3A33A), Color(0xFFF9DFAE)),
        QuantityTone.STEPS to Shades(Color(0xFF3D6FC0), Color(0xFF8AB6F9)),
        QuantityTone.BURN to Shades(Color(0xFFB5442E), Color(0xFFFF8A70)),
        QuantityTone.REST to Shades(Color(0xFFE9A08F), Color(0xFFFFD3C7)),
    )

    /** The main parts (the lighter parts only need to be told apart from these). */
    val main = listOf(QuantityTone.WEIGHT, QuantityTone.FOOD, QuantityTone.STEPS, QuantityTone.BURN)

    /** The past days' bars on a tab or card when today has a bar (app-appearance "Chart look"). */
    const val PAST_ALPHA = 0.6f

    fun of(tone: QuantityTone, dark: Boolean): Color = shades.getValue(tone).let { if (dark) it.dark else it.light }

    /** An own chart's Oletus colour for a data kind. */
    fun of(kind: SeriesKind): QuantityTone = when (kind) {
        SeriesKind.WEIGHT -> QuantityTone.WEIGHT
        SeriesKind.ENERGY -> QuantityTone.FOOD
        SeriesKind.STEPS -> QuantityTone.STEPS
        SeriesKind.BURN -> QuantityTone.BURN
    }
}

/**
 * Whether the current theme is dark: from [TerveysTheme]'s look, else (a plain MaterialTheme in a
 * test) by the surface's lightness. The one place that answers it for colour picks.
 */
@Composable
@ReadOnlyComposable
internal fun isDarkTheme(): Boolean = LocalAppLook.current?.dark ?: (MaterialTheme.colorScheme.surface.luminance() < 0.5f)

/** [tone]'s shade for the current theme. */
@Composable
@ReadOnlyComposable
internal fun quantityColour(tone: QuantityTone): Color = QuantityColours.of(tone, isDarkTheme())

/** The treats' share as text ("herkkuja 230 kcal"): the Syöty amber, readable as text in both themes. */
@Composable
@ReadOnlyComposable
internal fun treatTextColour(): Color = quantityColour(QuantityTone.FOOD)
