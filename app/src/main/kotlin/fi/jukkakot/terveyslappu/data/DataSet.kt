package fi.jukkakot.terveyslappu.data

import fi.jukkakot.terveyslappu.raw.RawEntry
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/** One exported weight: when (with the record's own offset when known), kg, the writing app, its kept raw data and its "?" mark. */
data class ExportRow(
    val time: Instant,
    val zoneOffset: ZoneOffset?,
    val kg: Double,
    val source: String,
    val raw: RawEntry? = null,
    val check: Boolean = false,
)

/**
 * Everything the data file carries (data-file "Data kinds"): one field per data kind. A new kind
 * gets a field here and a part in [CsvData] and [JsonData]; export, import and backup follow.
 */
data class DataSet(
    val weights: List<ExportRow>,
    val foods: List<fi.jukkakot.terveyslappu.food.FoodItem> = emptyList(),
    val products: List<fi.jukkakot.terveyslappu.food.Product> = emptyList(),
)

/** The data file's formats (Vie tiedot asks which one; the backup uses CSV). */
enum class DataFormat(val extension: String, val mimeType: String) {
    CSV("csv", "text/csv"),
    JSON("json", "application/json"),
    ;

    fun write(set: DataSet, fallbackZone: ZoneId): String = when (this) {
        CSV -> CsvData.write(set, fallbackZone)
        JSON -> JsonData.write(set, fallbackZone)
    }

    companion object {
        /** JSON when the text's first non-blank character is `{`, else CSV (or another history table). */
        fun detect(text: String): DataFormat = if (text.trimStart('\uFEFF', ' ', '\t', '\r', '\n').startsWith("{")) JSON else CSV
    }
}

/** Shared by both formats. */
object DataFile {
    const val APP = "terveyslappu"
    const val VERSION = 2
    const val WEIGHTS = "weights"

    private val stamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS")

    /** `terveyslappu-export-20261109-071205-123.csv`: local time to the millisecond, so names never collide. */
    fun exportName(format: DataFormat, at: LocalDateTime): String = "$APP-export-${stamp.format(at)}.${format.extension}"
}
