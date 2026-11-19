package fi.jukkakot.terveyslappu.scale

import android.content.Context
import androidx.core.content.edit
import java.time.Instant

/**
 * The weight of the latest saved or deleted weighing, its scale and when it was last heard
 * ([heardAt], slid by the same-weight rule). [at] is when it was saved or deleted (never slid) and
 * [deleted] which of the two; the dialog's repeat line shows them (everyday-feedback design 2).
 */
data class LastWeight(
    val kg: Double,
    val address: String?,
    val heardAt: Instant,
    val at: Instant = heardAt,
    val deleted: Boolean = false,
)

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
    /** The last saved or deleted weight and when its scale was last heard (the same-weight rule). */
    fun weight(): LastWeight?
    fun saveWeight(weight: LastWeight)
    fun scale(): KnownScale?
    fun saveScale(scale: KnownScale)
}

class MemoryLastWeighIn : LastWeighInStore {
    private var value: Pair<Int, Instant>? = null
    private var known: KnownScale? = null
    private var last: LastWeight? = null
    override fun weight() = last
    override fun saveWeight(weight: LastWeight) {
        last = weight
    }
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
class LastWeighInPrefs(context: Context, private val onScale: () -> Unit = {}) : LastWeighInStore {
    private val prefs = context.getSharedPreferences("weighin", Context.MODE_PRIVATE)

    override fun load(): Pair<Int, Instant>? {
        if (!prefs.contains(KEY)) return null
        return prefs.getInt(KEY, 0) to Instant.ofEpochMilli(prefs.getLong(AT, 0))
    }

    override fun save(key: Int, at: Instant) {
        prefs.edit { putInt(KEY, key).putLong(AT, at.toEpochMilli()) }
    }

    override fun weight(): LastWeight? {
        if (!prefs.contains(W_KG)) return null
        val heardAt = Instant.ofEpochMilli(prefs.getLong(W_AT, 0))
        // Prefs from before everyday-feedback have no save time: the last hearing stands in.
        return LastWeight(
            prefs.getString(W_KG, null)?.toDoubleOrNull() ?: return null,
            prefs.getString(W_ADDRESS, null),
            heardAt,
            if (prefs.contains(W_SAVED_AT)) Instant.ofEpochMilli(prefs.getLong(W_SAVED_AT, 0)) else heardAt,
            prefs.getBoolean(W_DELETED, false),
        )
    }

    override fun saveWeight(weight: LastWeight) {
        prefs.edit {
            putString(W_KG, weight.kg.toString())
                .putString(W_ADDRESS, weight.address)
                .putLong(W_AT, weight.heardAt.toEpochMilli())
                .putLong(W_SAVED_AT, weight.at.toEpochMilli())
                .putBoolean(W_DELETED, weight.deleted)
        }
    }

    override fun scale(): KnownScale? = prefs.getString(ADDRESS, null)?.let { KnownScale(it, prefs.getString(NAME, null)) }

    override fun saveScale(scale: KnownScale) {
        val changed = scale != this.scale()
        prefs.edit { putString(ADDRESS, scale.address).putString(NAME, scale.name) }
        if (changed) onScale()
    }

    private companion object {
        const val KEY = "last_key"
        const val AT = "last_at"
        const val ADDRESS = "scale_address"
        const val NAME = "scale_name"
        const val W_KG = "weight_kg"
        const val W_ADDRESS = "weight_address"
        const val W_AT = "weight_at"
        const val W_SAVED_AT = "weight_saved_at"
        const val W_DELETED = "weight_deleted"
    }
}
