package fi.jukkakot.terveyslappu.steps

import fi.jukkakot.terveyslappu.auto.JsonFileValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import java.io.File
import java.time.LocalDate

/**
 * The imported Garmin history (`garmin-history.json` in app storage, in Android backup and
 * transfer, never in the data file; garmin-steps-history design 10, 11, energy-burn design
 * "Structure"): per day steps?, active? and rest? kcal. An older `garmin-steps.json` (steps only) is
 * read while the new file does not exist and removed by the next write. Replaced whole in one
 * atomic write; [delete] returns what was there so Kumoa can [restore] it (kept in memory only).
 */
class GarminHistoryStore(file: File? = null, private val oldFile: File? = null) {
    @Serializable
    private data class Row(val day: String, val steps: Long? = null, val active: Long? = null, val rest: Long? = null)

    private val newFile = file
    private val value = JsonFileValue(file, ListSerializer(Row.serializer()), emptyList())
    private val old = JsonFileValue(oldFile, ListSerializer(Row.serializer()), emptyList())

    private val _days: MutableStateFlow<List<GarminDay>> by lazy { MutableStateFlow(load()) }

    /** Oldest first. */
    val days: StateFlow<List<GarminDay>> get() = _days

    private fun rows(): List<Row> =
        if (newFile?.exists() != true && oldFile?.exists() == true) old.get() else value.get()

    private fun load(): List<GarminDay> = rows()
        .mapNotNull { row ->
            runCatching {
                val kcal = row.active != null && row.rest != null
                GarminDay(LocalDate.parse(row.day), row.steps?.takeIf { it > 0 }, row.active.takeIf { kcal }, row.rest.takeIf { kcal })
            }.getOrNull()
        }
        .filter { it.steps != null || it.hasKcal }
        .sortedBy { it.day }

    @Synchronized
    fun replace(days: List<GarminDay>) {
        value.set(days.sortedBy { it.day }.map { Row(it.day.toString(), it.steps, it.active, it.rest) })
        oldFile?.delete()
        _days.value = load()
    }

    /** Deletes the history; returns it for Kumoa. */
    @Synchronized
    fun delete(): List<GarminDay> {
        val old = _days.value
        replace(emptyList())
        return old
    }

    fun restore(days: List<GarminDay>) = replace(days)
}
