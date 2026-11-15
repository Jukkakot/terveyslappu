package fi.jukkakot.terveyslappu.steps

import android.content.Context
import android.content.pm.PackageManager
import fi.jukkakot.terveyslappu.R

/**
 * Readable names of steps sources (steps-source-and-axes design 6): the phone's own steps
 * "Puhelin", else the installed app's label, else the package name (not visible or uninstalled).
 */
class StepsAppNames(private val context: Context) {
    private val cache = mutableMapOf<String, String>()

    fun name(id: String): String = cache.getOrPut(id) {
        if (id == StepsSourceRules.PHONE) return@getOrPut context.getString(R.string.steps_source_phone)
        try {
            val pm = context.packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(id, 0)).toString().ifBlank { id }
        } catch (_: PackageManager.NameNotFoundException) {
            id
        }
    }
}
