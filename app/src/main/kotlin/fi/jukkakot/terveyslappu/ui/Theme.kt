package fi.jukkakot.terveyslappu.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import fi.jukkakot.terveyslappu.TerveysApp

/**
 * Material 3 with the phone's own colours (Material You), light or dark by the theme choice in
 * Asetukset (default: follow the system). Every activity uses it, so a change applies everywhere.
 */
@Composable
fun TerveysTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val appearance = (context.applicationContext as? TerveysApp)?.appearance
    val mode = appearance?.mode?.collectAsState()?.value ?: ThemeMode.SYSTEM
    val dark = mode.isDark(isSystemInDarkTheme())
    val colors = if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
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
    MaterialTheme(colorScheme = colors, content = content)
}

/** The "?" weight's warning tint (amber family): container, outline and text, readable in both themes. */
data class CheckColors(val container: Color, val outline: Color, val content: Color)

/** Picked by the surface's lightness, like [toneColor], so it follows the theme choice. */
@Composable
fun checkColors(): CheckColors =
    if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) {
        CheckColors(container = Color(0xFF3A2E0A), outline = Color(0xFFE2B53E), content = Color(0xFFFFE6A6))
    } else {
        CheckColors(container = Color(0xFFFFF1CC), outline = Color(0xFF9A6B00), content = Color(0xFF3D2B00))
    }

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
