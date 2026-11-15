package fi.jukkakot.terveyslappu.steps

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.DecodeSequenceMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeToSequence
import kotlinx.serialization.json.longOrNull
import java.io.FilterInputStream
import java.io.InputStream
import java.time.LocalDate
import java.util.zip.ZipInputStream

/** One read of a Garmin export: days with steps (oldest first) and the number of UDS files read. */
data class GarminRead(val days: List<StepsDay>, val entries: Int)

/** The Asetukset row's figures (steps "Imported history in Asetukset"). */
data class GarminSummary(val first: LocalDate, val last: LocalDate, val count: Int) {
    companion object {
        fun of(days: List<StepsDay>): GarminSummary? =
            if (days.isEmpty()) null else GarminSummary(days.minOf { it.day }, days.maxOf { it.day }, days.size)
    }
}

/**
 * Garmin's account data export (steps "Import Garmin step history", design 8, 9): a ZIP streamed
 * entry by entry; only `…/DI_CONNECT/DI-Connect-Aggregator/UDSFile_*.json` is parsed, each as a
 * JSON array decoded one daily summary at a time, so memory stays bounded whatever the ZIP's size.
 * Reads `calendarDate` and `totalSteps` only. Pure.
 */
object GarminStepsReader {
    private val UDS = Regex("""(^|.*/)DI_CONNECT/DI-Connect-Aggregator/UDSFile_[^/]*\.json""")
    private val ZIP_MAGIC = byteArrayOf(0x50, 0x4B, 0x03, 0x04)
    private val json = Json { ignoreUnknownKeys = true }

    /** A ZIP starts with `PK\x03\x04`. */
    fun isZip(head: ByteArray): Boolean = head.size >= 4 && (0 until 4).all { head[it] == ZIP_MAGIC[it] }

    fun isUds(name: String): Boolean = UDS.matches(name)

    /** Throws on a corrupt ZIP or JSON; days on or after [today] are left out, duplicates keep the larger count. */
    @OptIn(ExperimentalSerializationApi::class)
    fun read(input: InputStream, today: LocalDate): GarminRead {
        val best = HashMap<LocalDate, Long>()
        var entries = 0
        val zip = ZipInputStream(input)
        while (true) {
            val entry = zip.nextEntry ?: break
            if (entry.isDirectory || !isUds(entry.name)) continue
            entries++
            val sequence = json.decodeToSequence<JsonElement>(KeepOpen(zip), DecodeSequenceMode.ARRAY_WRAPPED)
            for (element in sequence) {
                val (day, steps) = day(element) ?: continue
                if (day >= today) continue
                val old = best[day]
                if (old == null || steps > old) best[day] = steps
            }
        }
        return GarminRead(best.entries.map { StepsDay(it.key, it.value) }.sortedBy { it.day }, entries)
    }

    private fun day(element: JsonElement): Pair<LocalDate, Long>? {
        val obj = element as? JsonObject ?: return null
        val date = (obj["calendarDate"] as? JsonPrimitive)?.takeIf { it.isString }?.content ?: return null
        val day = runCatching { LocalDate.parse(date) }.getOrNull() ?: return null
        val steps = (obj["totalSteps"] as? JsonPrimitive)?.takeIf { !it.isString }?.longOrNull ?: return null
        return if (steps < 1) null else day to steps
    }

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
