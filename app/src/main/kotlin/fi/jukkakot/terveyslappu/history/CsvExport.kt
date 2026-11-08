package fi.jukkakot.terveyslappu.history

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/** One exported weight: when (with the record's own offset when known), kg and the writing app. */
data class ExportRow(val time: Instant, val zoneOffset: ZoneOffset?, val kg: Double, val source: String)

/**
 * The weight-export CSV: `date;time;weight_kg;source`, oldest first, comma decimals (Finnish
 * spreadsheets), in the layout [HistoryParser] reads back.
 */
object CsvExport {
    const val HEADER = "date;time;weight_kg;source"
    private val date = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val time = DateTimeFormatter.ofPattern("HH:mm:ss")

    fun write(rows: List<ExportRow>, fallbackZone: ZoneId): String = buildString {
        append(HEADER).append('\n')
        for (row in rows.sortedBy { it.time }) {
            val local = row.time.atZone(row.zoneOffset ?: fallbackZone)
            append(date.format(local)).append(';')
            append(time.format(local)).append(';')
            append(kg(row.kg)).append(';')
            append(quote(row.source)).append('\n')
        }
    }

    /** 82.4 → `82,4`, 82.45 → `82,45`, 82.0 → `82`. */
    fun kg(value: Double): String =
        BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString().replace('.', ',')

    private fun quote(text: String): String =
        if (text.any { it == ';' || it == '"' || it == '\n' }) "\"" + text.replace("\"", "\"\"").replace('\n', ' ') + "\"" else text
}
