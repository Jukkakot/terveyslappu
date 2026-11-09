package fi.jukkakot.terveyslappu.data

import fi.jukkakot.terveyslappu.raw.RawFormat
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * The data file as CSV (data-file "CSV layout"): `#terveyslappu;version=2`, then per data kind
 * `#table;<kind>`, its header and rows. Weights keep the `date;time;weight_kg;source;raw` columns,
 * oldest first, comma decimals, so `HistoryParser` reads them like any history table. Reading
 * lives in `HistoryParser.parse`, which also reads older files and other apps' tables.
 */
object CsvData {
    const val VERSION_LINE = "#${DataFile.APP};version=${DataFile.VERSION}"
    const val TABLE_PREFIX = "#table;"
    const val WEIGHT_HEADER = "date;time;weight_kg;source;raw"

    private val date = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val time = DateTimeFormatter.ofPattern("HH:mm:ss")

    fun write(set: DataSet, fallbackZone: ZoneId): String = buildString {
        append(VERSION_LINE).append('\n')
        if (set.weights.isNotEmpty()) {
            append(TABLE_PREFIX).append(DataFile.WEIGHTS).append('\n')
            append(WEIGHT_HEADER).append('\n')
            for (row in set.weights.sortedBy { it.time }) {
                val local = row.time.atZone(row.zoneOffset ?: fallbackZone)
                append(date.format(local)).append(';')
                append(time.format(local)).append(';')
                append(kg(row.kg)).append(';')
                append(quote(row.source)).append(';')
                append(row.raw?.let { quote(RawFormat.encode(it)) } ?: "").append('\n')
            }
        }
    }

    /** 82.4 → `82,4`, 82.45 → `82,45`, 82.0 → `82`. */
    fun kg(value: Double): String =
        BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString().replace('.', ',')

    private fun quote(text: String): String =
        if (text.any { it == ';' || it == '"' || it == '\n' }) "\"" + text.replace("\"", "\"\"").replace('\n', ' ') + "\"" else text
}
