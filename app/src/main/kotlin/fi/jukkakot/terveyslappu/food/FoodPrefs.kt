package fi.jukkakot.terveyslappu.food

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * The food log's settings in the `food` prefs file (food-log "Base products": the switch "Valmiit
 * ruuat"); in Android backup and transfer. One instance per app, shared by every screen.
 */
class FoodPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("food", Context.MODE_PRIVATE)
    private val _baseProducts = MutableStateFlow(prefs.getBoolean(KEY_BASE, true))

    /** Whether the shipped base products are used (suggestions, Enter and the import). On by default. */
    val baseProducts: StateFlow<Boolean> = _baseProducts

    fun setBaseProducts(on: Boolean) {
        prefs.edit().putBoolean(KEY_BASE, on).apply()
        _baseProducts.value = on
    }

    private companion object {
        const val KEY_BASE = "baseProducts"
    }
}
