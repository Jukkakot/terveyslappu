package fi.jukkakot.terveyslappu.scale

import android.content.Context
import java.time.Instant

/** The user's scale as heard in the deciding packet of the latest saved weigh-in (background-weigh-in). */
data class KnownScale(val address: String, val name: String?)

/**
 * The company id of the last saved or discarded weigh-in and when it ended. The scale's company id
 * is a per-weighing counter (e.g. 5fc0, 60c0, 61c0) and the scale keeps repeating the last stable
 * packet for a while, so the next weigh-in ignores that id for a short time. A saved weigh-in also
 * remembers the scale ([saveScale]) so background listening knows which scale is the user's.
 */
interface LastWeighInStore {
    fun load(): Pair<Int, Instant>?
    fun save(key: Int, at: Instant)
    fun scale(): KnownScale?
    fun saveScale(scale: KnownScale)
}

class MemoryLastWeighIn : LastWeighInStore {
    private var value: Pair<Int, Instant>? = null
    private var known: KnownScale? = null
    override fun load() = value
    override fun save(key: Int, at: Instant) {
        value = key to at
    }
    override fun scale() = known
    override fun saveScale(scale: KnownScale) {
        known = scale
    }
}

/** App-private preferences, so reopening the widget right after Poista still knows the id. */
class LastWeighInPrefs(context: Context) : LastWeighInStore {
    private val prefs = context.getSharedPreferences("weighin", Context.MODE_PRIVATE)

    override fun load(): Pair<Int, Instant>? {
        if (!prefs.contains(KEY)) return null
        return prefs.getInt(KEY, 0) to Instant.ofEpochMilli(prefs.getLong(AT, 0))
    }

    override fun save(key: Int, at: Instant) {
        prefs.edit().putInt(KEY, key).putLong(AT, at.toEpochMilli()).apply()
    }

    override fun scale(): KnownScale? = prefs.getString(ADDRESS, null)?.let { KnownScale(it, prefs.getString(NAME, null)) }

    override fun saveScale(scale: KnownScale) {
        prefs.edit().putString(ADDRESS, scale.address).putString(NAME, scale.name).apply()
    }

    private companion object {
        const val KEY = "last_key"
        const val AT = "last_at"
        const val ADDRESS = "scale_address"
        const val NAME = "scale_name"
    }
}
