package fi.jukkakot.terveyslappu.history

import fi.jukkakot.terveyslappu.weight.MAX_KG
import fi.jukkakot.terveyslappu.weight.MIN_KG
import java.time.Instant
import java.time.ZoneId
import kotlin.math.roundToInt

/** One weight read from a history file; [line] is 1-based in the file. */
data class ImportedWeight(val time: Instant, val kg: Double, val line: Int)

enum class SkipReason { UNREADABLE_DATE, UNREADABLE_WEIGHT, OUT_OF_RANGE, FUTURE, DUPLICATE }

data class SkippedRow(val line: Int, val reason: SkipReason, val raw: String)

/** What the parser decided about the file (columns are 0-based). Logged for shared files. */
data class Layout(
    val separator: Char,
    val header: String?,
    val dateCol: Int,
    val timeCol: Int?,
    val weightCol: Int,
    val pounds: Boolean,
    val dayFirst: Boolean,
    val ambiguousOrder: Boolean,
)

/** [weights] oldest first; [layout] is null when no date and weight columns were found. */
data class ParsedHistory(val weights: List<ImportedWeight>, val skipped: List<SkippedRow>, val layout: Layout?) {
    fun skippedCounts(): Map<SkipReason, Int> = skipped.groupingBy { it.reason }.eachCount()
}

/**
 * Reads a weight history table (history-import spec). Columns come from header keywords when
 * they hold readable values, otherwise from the values themselves; see design.md decision 1.
 */
object HistoryParser {
    private const val SAMPLE = 50
    private const val BOM = "\uFEFF"
    private const val ENOUGH = 0.6
    private const val LB_IN_KG = 0.45359237

    private val weightWords = listOf("weight", "paino", "体重", "gewicht", "poids")
    private val unitOnly = Regex("""^\(?(kg|lbs?)\)?$""")
    private val dateWords = listOf("date", "päiv", "pvm", "日期", "timestamp", "time stamp", "测量时间", "datum")
    private val timeWords = listOf("time", "aika", "时间", "klo")
    private val number = Regex("""^-?\d+(\.\d+)?$""")

    fun parse(text: String, zone: ZoneId, now: Instant): ParsedHistory {
        val lines = text.removePrefix(BOM).lines()
        val numbered = lines.mapIndexedNotNull { i, l -> if (l.isBlank()) null else (i + 1) to l }
        if (numbered.isEmpty()) return ParsedHistory(emptyList(), emptyList(), null)
        val sep = Table.detectSeparator(numbered.map { it.second })
        val rows = numbered.map { (n, l) -> Triple(n, l, Table.split(l, sep)) }

        val headerIndex = rows.take(5).indexOfFirst { (_, _, f) -> f.any { name -> role(name) != null } }
            .takeIf { it >= 0 }
            ?: if (rows.first().third.none { DateParsing.looksLikeDate(it) || parseNumber(it) != null }) 0 else -1
        val header = rows.getOrNull(headerIndex)?.third
        val data = rows.drop(headerIndex + 1)
        val sample = data.take(SAMPLE).map { it.third }

        fun share(col: Int, test: (String) -> Boolean): Double =
            if (sample.isEmpty()) 0.0 else sample.count { test(it.getOrElse(col) { "" }) }.toDouble() / sample.size
        val width = (sample.maxOfOrNull { it.size } ?: 0).coerceAtLeast(header?.size ?: 0)
        val columns = 0 until width

        val dateCol = header?.indices?.firstOrNull { role(header[it]) == Role.DATE && share(it, DateParsing::looksLikeDate) >= ENOUGH }
            ?: columns.filter { share(it, DateParsing::looksLikeDate) >= ENOUGH }.maxByOrNull { share(it, DateParsing::looksLikeDate) }
            ?: return unreadable(data)
        val timeCol = columns.firstOrNull { it != dateCol && share(it, DateParsing::isTimeOnly) >= ENOUGH }
            ?.takeIf { header == null || role(header.getOrElse(it) { "" }) != Role.WEIGHT }
        val inRange: (String) -> Boolean = { v -> parseNumber(v)?.let { it in MIN_KG..MAX_KG * 2.3 } == true }
        val weightCol = header?.indices?.firstOrNull { it != dateCol && role(header[it]) == Role.WEIGHT && share(it, inRange) >= ENOUGH }
            ?: columns.firstOrNull { it != dateCol && it != timeCol && share(it, inRange) >= ENOUGH }
            ?: return unreadable(data)
        val pounds = header?.getOrNull(weightCol)?.lowercase()?.let { "lb" in it || "pound" in it } == true ||
            sample.count { it.getOrElse(weightCol) { "" }.lowercase().contains("lb") } * 2 > sample.size
        val (dayFirst, ambiguous) = DateParsing.dayOrder(data.map { it.third.getOrElse(dateCol) { "" } })
        val layout = Layout(sep, header?.let { rows[headerIndex].second }, dateCol, timeCol, weightCol, pounds, dayFirst, ambiguous)

        val skipped = mutableListOf<SkippedRow>()
        val bySecond = LinkedHashMap<Long, ImportedWeight>()
        for ((line, raw, fields) in data) {
            val time = DateParsing.parse(fields.getOrElse(dateCol) { "" }, timeCol?.let { fields.getOrNull(it) }, dayFirst, zone)
            if (time == null) {
                skipped += SkippedRow(line, SkipReason.UNREADABLE_DATE, raw)
                continue
            }
            val value = parseNumber(fields.getOrElse(weightCol) { "" })
            if (value == null) {
                skipped += SkippedRow(line, SkipReason.UNREADABLE_WEIGHT, raw)
                continue
            }
            val kg = round2(if (pounds) value * LB_IN_KG else value)
            when {
                kg !in MIN_KG..MAX_KG -> skipped += SkippedRow(line, SkipReason.OUT_OF_RANGE, raw)
                time.isAfter(now) -> skipped += SkippedRow(line, SkipReason.FUTURE, raw)
                else -> {
                    val second = time.epochSecond
                    bySecond.remove(second)?.let { skipped += SkippedRow(it.line, SkipReason.DUPLICATE, lines[it.line - 1]) }
                    bySecond[second] = ImportedWeight(time, kg, line)
                }
            }
        }
        return ParsedHistory(bySecond.values.sortedBy { it.time }, skipped.sortedBy { it.line }, layout)
    }

    /** File bytes to text: UTF-16 when it starts with a UTF-16 byte order mark (Excel "Unicode text"), else UTF-8. */
    fun decode(bytes: ByteArray): String = when {
        bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte() -> String(bytes, 2, bytes.size - 2, Charsets.UTF_16LE)
        bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte() -> String(bytes, 2, bytes.size - 2, Charsets.UTF_16BE)
        else -> String(bytes, Charsets.UTF_8)
    }

    /** Weight value: comma or dot decimal, optional unit suffix. */
    fun parseNumber(text: String): Double? {
        val t = text.trim().lowercase().removeSuffix("lbs").removeSuffix("lb").removeSuffix("kg").trim().replace(',', '.')
        return if (number.matches(t)) t.toDouble() else null
    }

    private enum class Role { DATE, TIME, WEIGHT }

    private fun role(name: String): Role? {
        val n = name.trim().lowercase()
        return when {
            weightWords.any { it in n } || unitOnly.matches(n) -> Role.WEIGHT
            dateWords.any { it in n } -> Role.DATE
            timeWords.any { it in n } -> Role.TIME
            else -> null
        }
    }

    private fun unreadable(data: List<Triple<Int, String, List<String>>>) =
        ParsedHistory(emptyList(), data.map { SkippedRow(it.first, SkipReason.UNREADABLE_DATE, it.second) }, null)

    private fun round2(v: Double) = (v * 100).roundToInt() / 100.0
}
