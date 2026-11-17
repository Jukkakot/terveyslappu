package fi.jukkakot.terveyslappu.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import fi.jukkakot.terveyslappu.TerveysApp

/** What [TerveysTheme] chose: dark or light and which colours (read by [isDarkTheme], [cardColour]). */
internal data class AppLook(val dark: Boolean, val colours: ColourChoice)

internal val LocalAppLook = staticCompositionLocalOf<AppLook?> { null }

/** The Terveyslappu palette (visual-identity design 1): MaterialKolor's scheme from the mint seed with the AC neutrals. */
internal object BrandColours {
    val SEED = Color(0xFF2E7D6B)

    val DARK_BACKGROUND = Color(0xFF0E1013)
    val DARK_CARD = Color(0xFF16191E)
    val DARK_RAISED = Color(0xFF1E2229)
    val DARK_HIGH = Color(0xFF272C34)
    val DARK_HIGHEST = Color(0xFF30363F)
    val DARK_PRIMARY = Color(0xFF4DF0B8)
    val DARK_ON_PRIMARY = Color(0xFF00382A)

    val LIGHT_BACKGROUND = Color(0xFFF4F6F5)
    val LIGHT_CARD = Color(0xFFFFFFFF)
    val LIGHT_RAISED = Color(0xFFEEF1F0)
    val LIGHT_HIGH = Color(0xFFE3E8E6)
    val LIGHT_HIGHEST = Color(0xFFDCE2DF)

    fun scheme(dark: Boolean): ColorScheme {
        val base = dynamicColorScheme(seedColor = SEED, isDark = dark, isAmoled = false, style = PaletteStyle.TonalSpot)
        return if (dark) {
            base.copy(
                primary = DARK_PRIMARY,
                onPrimary = DARK_ON_PRIMARY,
                background = DARK_BACKGROUND,
                surface = DARK_BACKGROUND,
                surfaceDim = DARK_BACKGROUND,
                surfaceContainerLowest = DARK_BACKGROUND,
                surfaceContainerLow = DARK_CARD,
                surfaceContainer = DARK_RAISED,
                surfaceContainerHigh = DARK_HIGH,
                surfaceContainerHighest = DARK_HIGHEST,
                surfaceBright = DARK_HIGH,
                surfaceVariant = DARK_HIGH,
            )
        } else {
            base.copy(
                background = LIGHT_BACKGROUND,
                surface = LIGHT_BACKGROUND,
                surfaceDim = LIGHT_HIGH,
                surfaceContainerLowest = LIGHT_CARD,
                surfaceContainerLow = LIGHT_CARD,
                surfaceContainer = LIGHT_RAISED,
                surfaceContainerHigh = LIGHT_HIGH,
                surfaceContainerHighest = LIGHT_HIGHEST,
                surfaceBright = LIGHT_CARD,
            )
        }
    }
}

/**
 * Material 3, light or dark by the theme choice (Teema, default: follow the system), in the app's
 * own palette or the phone's colours (Värit, default Terveyslappu). Every activity uses it, so a
 * change applies everywhere. [mode] and [colours] override the remembered choices (tests, the tour).
 */
@Composable
fun TerveysTheme(mode: ThemeMode? = null, colours: ColourChoice? = null, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val appearance = (context.applicationContext as? TerveysApp)?.appearance
    val mode = mode ?: appearance?.mode?.collectAsState()?.value ?: ThemeMode.SYSTEM
    val choice = colours ?: appearance?.colours?.collectAsState()?.value ?: ColourChoice.BRAND
    val dark = mode.isDark(isSystemInDarkTheme())
    val colors = remember(dark, choice) {
        when (choice) {
            ColourChoice.BRAND -> BrandColours.scheme(dark)
            ColourChoice.PHONE -> if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            // Edge-to-edge bars follow the system by default; make their icons follow the chosen theme.
            context.findActivity()?.window?.let { window ->
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
            }
        }
    }
    CompositionLocalProvider(LocalAppLook provides AppLook(dark, choice)) {
        MaterialTheme(colorScheme = colors, content = content)
    }
}

/**
 * A card's surface (app-appearance "Card look"): Terveyslappu's one-step-lighter card tone; with
 * Puhelimen värit the container one step above the background, so cards never vanish into it.
 */
@Composable
@ReadOnlyComposable
internal fun cardColour(): Color =
    if (LocalAppLook.current?.colours == ColourChoice.PHONE) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerLow

/** A raised part on a card (the hero figure's base, the periods' track): one step above [cardColour]. */
@Composable
@ReadOnlyComposable
internal fun raisedColour(): Color =
    if (LocalAppLook.current?.colours == ColourChoice.PHONE) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer

/** The "?" weight's warning tint (amber family): container, outline and text, readable in both themes. */
data class CheckColors(val container: Color, val outline: Color, val content: Color)

/** Picked by the theme's darkness ([isDarkTheme]), so it follows the theme choice. */
@Composable
fun checkColors(): CheckColors =
    if (isDarkTheme()) {
        CheckColors(container = Color(0xFF3A2E0A), outline = Color(0xFFE2B53E), content = Color(0xFFFFE6A6))
    } else {
        CheckColors(container = Color(0xFFFFF1CC), outline = Color(0xFF9A6B00), content = Color(0xFF3D2B00))
    }

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
