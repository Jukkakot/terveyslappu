package fi.jukkakot.terveyslappu.steps

import fi.jukkakot.terveyslappu.auto.JsonFileValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import java.io.File
import java.time.LocalDate

/**
 * The imported Garmin step history (`garmin-steps.json` in app storage, in Android backup and
 * transfer, never in the data file; garmin-steps-history design 10, 11). Replaced whole in one
 * atomic write; [delete] returns what was there so Kumoa can [restore] it (kept in memory only).
 */
class GarminStepsStore(file: File? = null) {
    @Serializable
    private data class Row(val day: String, val steps: Long)

    private val value = JsonFileValue(file, ListSerializer(Row.serializer()), emptyList())

    private val _days: MutableStateFlow<List<StepsDay>> by lazy { MutableStateFlow(load()) }

    /** Oldest first. */
    val days: StateFlow<List<StepsDay>> get() = _days

    private fun load(): List<StepsDay> = value.get()
        .mapNotNull { row -> runCatching { StepsDay(LocalDate.parse(row.day), row.steps) }.getOrNull() }
        .sortedBy { it.day }

    @Synchronized
    fun replace(days: List<StepsDay>) {
        value.set(days.sortedBy { it.day }.map { Row(it.day.toString(), it.steps) })
        _days.value = load()
    }

    /** Deletes the history; returns it for Kumoa. */
    @Synchronized
    fun delete(): List<StepsDay> {
        val old = _days.value
        replace(emptyList())
        return old
    }

    fun restore(days: List<StepsDay>) = replace(days)
}
