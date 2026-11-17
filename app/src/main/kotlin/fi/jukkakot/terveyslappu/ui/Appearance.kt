package fi.jukkakot.terveyslappu.ui

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** The theme choice (Teema) in Asetukset → Ulkoasu (app-appearance "Theme choice"). */
enum class ThemeMode {
    SYSTEM, LIGHT, DARK;

    fun isDark(systemDark: Boolean): Boolean = when (this) {
        SYSTEM -> systemDark
        LIGHT -> false
        DARK -> true
    }

    companion object {
        fun decode(value: String?): ThemeMode = entries.firstOrNull { it.name == value } ?: SYSTEM
    }
}

/** The colour choice (Värit): the app's own palette (default) or the phone's (Material You). */
enum class ColourChoice {
    BRAND, PHONE;

    companion object {
        fun decode(value: String?): ColourChoice = entries.firstOrNull { it.name == value } ?: BRAND
    }
}

/** The remembered theme choice (app-private preferences); the singleton lives in `TerveysApp`. */
class AppearancePrefs(context: Context) {
    private val prefs = context.getSharedPreferences("appearance", Context.MODE_PRIVATE)

    private val _mode = MutableStateFlow(ThemeMode.decode(prefs.getString(KEY, null)))
    val mode: StateFlow<ThemeMode> = _mode

    private val _colours = MutableStateFlow(ColourChoice.decode(prefs.getString(COLOURS, null)))
    val colours: StateFlow<ColourChoice> = _colours

    fun set(mode: ThemeMode) {
        prefs.edit().putString(KEY, mode.name).apply()
        _mode.value = mode
    }

    fun set(colours: ColourChoice) {
        prefs.edit().putString(COLOURS, colours.name).apply()
        _colours.value = colours
    }

    private companion object {
        const val KEY = "theme"
        const val COLOURS = "colours"
    }
}
