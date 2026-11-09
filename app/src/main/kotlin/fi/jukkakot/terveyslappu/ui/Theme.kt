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

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
