package fi.jukkakot.terveyslappu.profile

import android.content.Context
import fi.jukkakot.terveyslappu.log.AppLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * The owner details in app-private preferences (owner-profile); the singleton lives in `TerveysApp`.
 * [heightInHealthConnect] is false when the last saved height could not be written there.
 */
class ProfilePrefs(context: Context) {
    private val prefs = context.getSharedPreferences("profile", Context.MODE_PRIVATE)

    private val _profile = MutableStateFlow(load())
    val profile: StateFlow<OwnerProfile> = _profile

    private val _heightInHc = MutableStateFlow(prefs.getBoolean(KEY_HEIGHT_HC, true))
    val heightInHealthConnect: StateFlow<Boolean> = _heightInHc

    /** Keeps [new]; logs which fields changed, never their values (NFR-1). */
    fun set(new: OwnerProfile, event: String = "profile.set") {
        val changed = new.changedFields(_profile.value)
        if (changed.isEmpty()) return
        prefs.edit().apply {
            putOrRemove(KEY_BIRTH_YEAR, new.birthYear)
            putOrRemove(KEY_HEIGHT, new.heightCm)
            if (new.gender == null) remove(KEY_GENDER) else putString(KEY_GENDER, new.gender.name)
        }.apply()
        _profile.value = new
        AppLog.i(event, "", "fields" to changed.joinToString(","))
    }

    fun setHeightInHealthConnect(ok: Boolean) {
        prefs.edit().putBoolean(KEY_HEIGHT_HC, ok).apply()
        _heightInHc.value = ok
    }

    private fun load() = OwnerProfile(
        birthYear = prefs.getInt(KEY_BIRTH_YEAR, 0).takeIf { prefs.contains(KEY_BIRTH_YEAR) },
        heightCm = prefs.getInt(KEY_HEIGHT, 0).takeIf { prefs.contains(KEY_HEIGHT) },
        gender = prefs.getString(KEY_GENDER, null)?.let { name -> Gender.entries.firstOrNull { it.name == name } },
    )

    private fun android.content.SharedPreferences.Editor.putOrRemove(key: String, value: Int?) {
        if (value == null) remove(key) else putInt(key, value)
    }

    private companion object {
        const val KEY_BIRTH_YEAR = "birth_year"
        const val KEY_HEIGHT = "height_cm"
        const val KEY_GENDER = "gender"
        const val KEY_HEIGHT_HC = "height_in_hc"
    }
}
