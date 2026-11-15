package fi.jukkakot.terveyslappu.steps

import android.content.Context
import java.time.LocalDate

/** What the steps source keeps (steps-source-and-axes design 3); a map in tests. */
interface StepsStore {
    var choice: StepsChoice

    /** The automatic pick's app id; null = none found. */
    var autoPick: String?

    /** The day of the last successful source check. */
    var autoPickDay: LocalDate?

    /** The phone's origin packages last seen (`android` + synthetic package names). */
    var phonePackages: Set<String>

    /** The last day each app was seen with steps. */
    var seen: Map<String, LocalDate>
}

/** In memory (tests and previews). */
class MemoryStepsStore : StepsStore {
    override var choice: StepsChoice = StepsChoice.Auto
    override var autoPick: String? = null
    override var autoPickDay: LocalDate? = null
    override var phonePackages: Set<String> = emptySet()
    override var seen: Map<String, LocalDate> = emptyMap()
}

/**
 * Prefs file `steps`: only on this phone, left out of Android backup and transfer
 * (`data_extraction_rules.xml`; the phone's package name is device- and app-scoped).
 */
class StepsPrefs(context: Context) : StepsStore {
    private val prefs = context.getSharedPreferences("steps", Context.MODE_PRIVATE)

    override var choice: StepsChoice
        get() = StepsChoice.decode(prefs.getString("choice", null))
        set(value) = prefs.edit().putString("choice", value.encode()).apply()

    override var autoPick: String?
        get() = prefs.getString("autoPick", null)?.takeIf { it.isNotBlank() }
        set(value) = prefs.edit().putString("autoPick", value.orEmpty()).apply()

    override var autoPickDay: LocalDate?
        get() = prefs.getString("autoPickDay", null)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        set(value) = prefs.edit().putString("autoPickDay", value?.toString().orEmpty()).apply()

    override var phonePackages: Set<String>
        get() = prefs.getStringSet("phonePackages", null)?.toSet().orEmpty()
        set(value) = prefs.edit().putStringSet("phonePackages", value).apply()

    override var seen: Map<String, LocalDate>
        get() = prefs.all.mapNotNull { (key, value) ->
            if (!key.startsWith(SEEN)) return@mapNotNull null
            val day = (value as? String)?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return@mapNotNull null
            key.removePrefix(SEEN) to day
        }.toMap()
        set(value) {
            val edit = prefs.edit()
            prefs.all.keys.filter { it.startsWith(SEEN) }.forEach { edit.remove(it) }
            value.forEach { (id, day) -> edit.putString(SEEN + id, day.toString()) }
            edit.apply()
        }

    private companion object {
        const val SEEN = "seen."
    }
}
