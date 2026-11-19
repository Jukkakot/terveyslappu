package fi.jukkakot.terveyslappu.a11y

import android.content.Context
import android.view.accessibility.AccessibilityManager

/**
 * Times that end by themselves follow Android's accessibility timeout ("Time to take action",
 * everyday-feedback design 6). [millis] never returns less than the base. The Android source is
 * installed once by the app ([install]); without it (JVM tests) the base is used.
 */
object ActionTimeouts {
    /** Kumoa bars. */
    const val BAR_MS = 4_000L

    /** The weigh-in result's auto-close and the "Paino poistettu" notification. */
    const val RESULT_MS = 10_000L

    @Volatile
    var source: (base: Long) -> Long = { it }

    fun millis(base: Long): Long = runCatching { source(base) }.getOrDefault(base).coerceAtLeast(base)

    fun install(context: Context) {
        val manager = context.applicationContext.getSystemService(AccessibilityManager::class.java) ?: return
        source = { base ->
            manager.getRecommendedTimeoutMillis(
                base.toInt(),
                AccessibilityManager.FLAG_CONTENT_TEXT or AccessibilityManager.FLAG_CONTENT_CONTROLS,
            ).toLong()
        }
    }
}
