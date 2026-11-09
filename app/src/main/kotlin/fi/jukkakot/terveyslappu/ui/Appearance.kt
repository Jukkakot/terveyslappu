package fi.jukkakot.terveyslappu.ui

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** The theme choice in Asetukset (app-appearance); colours always come from the phone's palette. */
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

/** The remembered theme choice (app-private preferences); the singleton lives in `TerveysApp`. */
class AppearancePrefs(context: Context) {
    private val prefs = context.getSharedPreferences("appearance", Context.MODE_PRIVATE)

    private val _mode = MutableStateFlow(ThemeMode.decode(prefs.getString(KEY, null)))
    val mode: StateFlow<ThemeMode> = _mode

    fun set(mode: ThemeMode) {
        prefs.edit().putString(KEY, mode.name).apply()
        _mode.value = mode
    }

    private companion object {
        const val KEY = "theme"
    }
}
