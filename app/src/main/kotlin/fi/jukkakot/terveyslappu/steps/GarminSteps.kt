package fi.jukkakot.terveyslappu.steps

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.DecodeSequenceMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeToSequence
import kotlinx.serialization.json.doubleOrNull
import java.io.FilterInputStream
import java.io.InputStream
import java.time.LocalDate
import java.util.zip.ZipInputStream
import kotlin.math.roundToLong

/**
 * One imported Garmin day (energy-burn design 6): its steps (null: none) and its active and resting
 * kcal (both null: none), as Garmin's daily summary gives them; nothing is computed.
 */
data class GarminDay(val day: LocalDate, val steps: Long? = null, val active: Long? = null, val rest: Long? = null) {
    val hasKcal: Boolean get() = active != null && rest != null
    val total: Long? get() = if (active != null && rest != null) active + rest else null
}

/** One read of a Garmin export: days with steps or kcal (oldest first) and the number of UDS files read. */
data class GarminRead(val days: List<GarminDay>, val entries: Int) {
    val stepDays: Int get() = days.count { it.steps != null }
    val kcalDays: Int get() = days.count { it.hasKcal }
}

/** The days with steps of an imported history (steps "Imported Garmin days in the totals"). */
fun List<GarminDay>.steps(): List<StepsDay> = mapNotNull { d -> d.steps?.let { StepsDay(d.day, it) } }

/** The Asetukset row's figures (steps "Imported history in Asetukset"): [count] = days with steps or kcal. */
data class GarminSummary(val first: LocalDate, val last: LocalDate, val count: Int, val kcal: Boolean = true) {
    companion object {
        fun of(days: List<GarminDay>): GarminSummary? =
            if (days.isEmpty()) null else GarminSummary(days.minOf { it.day }, days.maxOf { it.day }, days.size, days.any { it.hasKcal })
    }
}

/**
 * Garmin's account data export (steps "Import Garmin step history", design 8, 9): a ZIP streamed
 * entry by entry; only `…/DI_CONNECT/DI-Connect-Aggregator/UDSFile_*.json` is parsed, each as a
 * JSON array decoded one daily summary at a time, so memory stays bounded whatever the ZIP's size.
 * Reads `calendarDate`, `totalSteps`, `activeKilocalories` and `bmrKilocalories` only (energy-burn
 * design 6: `totalKilocalories` = active + bmr is not read; a day's kcal are kept only when active > 0
 * or the day has steps, so BMR-only days before the watch drop out). Pure.
 */
object GarminStepsReader {
    private val UDS = Regex("""(^|.*/)DI_CONNECT/DI-Connect-Aggregator/UDSFile_[^/]*\.json""")
    private val ZIP_MAGIC = byteArrayOf(0x50, 0x4B, 0x03, 0x04)
    private val json = Json { ignoreUnknownKeys = true }

    /** A ZIP starts with `PK\x03\x04`. */
    fun isZip(head: ByteArray): Boolean = head.size >= 4 && (0 until 4).all { head[it] == ZIP_MAGIC[it] }

    fun isUds(name: String): Boolean = UDS.matches(name)

    /** Throws on a corrupt ZIP or JSON; days on or after [today] are left out; duplicates keep the larger count and the larger kcal total. */
    @OptIn(ExperimentalSerializationApi::class)
    fun read(input: InputStream, today: LocalDate): GarminRead {
        val best = HashMap<LocalDate, GarminDay>()
        var entries = 0
        val zip = ZipInputStream(input)
        while (true) {
            val entry = zip.nextEntry ?: break
            if (entry.isDirectory || !isUds(entry.name)) continue
            entries++
            val sequence = json.decodeToSequence<JsonElement>(KeepOpen(zip), DecodeSequenceMode.ARRAY_WRAPPED)
            for (element in sequence) {
                val new = day(element) ?: continue
                if (new.day >= today) continue
                best[new.day] = best[new.day]?.let { combine(it, new) } ?: new
            }
        }
        return GarminRead(best.values.sortedBy { it.day }, entries)
    }

    private fun combine(a: GarminDay, b: GarminDay): GarminDay {
        val steps = listOfNotNull(a.steps, b.steps).maxOrNull()
        val kcal = listOf(a, b).filter { it.hasKcal }.maxByOrNull { it.total!! }
        return GarminDay(a.day, steps, kcal?.active, kcal?.rest)
    }

    private fun day(element: JsonElement): GarminDay? {
        val obj = element as? JsonObject ?: return null
        val date = (obj["calendarDate"] as? JsonPrimitive)?.takeIf { it.isString }?.content ?: return null
        val day = runCatching { LocalDate.parse(date) }.getOrNull() ?: return null
        val steps = number(obj, "totalSteps")?.toLong()?.takeIf { it >= 1 }
        val active = number(obj, "activeKilocalories")
        val bmr = number(obj, "bmrKilocalories")
        if (active == null || bmr == null || !(active > 0 || steps != null)) {
            return steps?.let { GarminDay(day, it) }
        }
        return GarminDay(day, steps, active.coerceAtLeast(0.0).roundToLong(), bmr.coerceAtLeast(0.0).roundToLong())
    }

    private fun number(obj: JsonObject, key: String): Double? =
        (obj[key] as? JsonPrimitive)?.takeIf { !it.isString }?.doubleOrNull

    /** The JSON decoder must not close the ZIP stream at an entry's end. */
    private class KeepOpen(input: InputStream) : FilterInputStream(input) {
        override fun close() {}
    }
}

/**
 * Imported days in the totals (steps "Imported Garmin days in the totals", design 1, 2): used only
 * when the source read is Garmin Connect's package set or has no origin filter; per day the larger
 * of Health Connect and the import, never today or later. Pure.
 */
object GarminMerge {
    const val GARMIN = "com.garmin.android.apps.connectmobile"

    fun uses(source: StepsSourceState): Boolean = source.packages == null || GARMIN in source.packages

    fun merge(hc: List<StepsDay>, imported: List<StepsDay>, source: StepsSourceState, today: LocalDate): List<StepsDay> {
        if (imported.isEmpty() || !uses(source)) return hc
        val days = HashMap<LocalDate, Long>()
        for (d in imported) if (d.day < today && d.steps > 0) days[d.day] = d.steps
        for (d in hc) days[d.day] = maxOf(d.steps, days[d.day] ?: 0L)
        return days.entries.map { StepsDay(it.key, it.value) }.sortedBy { it.day }
    }
}
