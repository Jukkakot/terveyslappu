package fi.jukkakot.terveyslappu.history

import fi.jukkakot.terveyslappu.raw.RawEntry
import fi.jukkakot.terveyslappu.raw.RawFormat

/** A sheet turned into text for [HistoryParser]; [okok] tells whether the OKOK layout was found. */
data class SheetText(val text: String, val okok: Boolean, val keptColumns: Int = 0, val profileCells: Int = 0)

/**
 * Turns a spreadsheet's rows into the semicolon text [HistoryParser] reads, one text line per sheet
 * row so skip reasons point at the sheet's own row numbers.
 *
 * The OKOK International export (history-import "OKOK Excel layout") has title rows (name, profile,
 * the covered range `YYYY-MM-DD ~ YYYY-MM-DD(nDay)`), a header `Date;Time;Weight(Kg);BMI;...` and
 * rows newest first with `MM-DD` dates. Its dates get their year from the range's end year, dropping
 * by one whenever the month-day goes up from one row to the next. The weight's columns stay as they
 * are; every other column and every title cell except the range go, as written, to a `raw` column
 * with source `okok`.
 */
object OkokLayout {
    const val SOURCE = "okok"

    private val range = Regex("""(\d{4})-(\d{1,2})-(\d{1,2})\s*~\s*(\d{4})-(\d{1,2})-(\d{1,2})""")
    private val monthDay = Regex("""^(\d{1,2})[-/.](\d{1,2})$""")
    private val labelled = Regex("""^([^:：]+)[:：]\s*(.*)$""")

    fun toText(rows: List<List<String>>): SheetText {
        val headerIndex = rows.indexOfFirst { cells -> cells.any { it.equals("date", ignoreCase = true) } && cells.any { "weight" in it.lowercase() } }
        val rangeIndex = rows.take(headerIndex.coerceAtLeast(0)).indexOfFirst { row -> row.any { range.containsMatchIn(it) } }
        if (headerIndex < 0 || rangeIndex < 0) return SheetText(rows.joinToString("\n") { line(it) }, okok = false)

        val endYear = rows[rangeIndex].firstNotNullOf { range.find(it) }.groupValues[4].toInt()
        val header = rows[headerIndex]
        val dateCol = header.indexOfFirst { it.equals("date", ignoreCase = true) }
        val timeCol = header.indexOfFirst { it.equals("time", ignoreCase = true) }.takeIf { it >= 0 }
        val weightCol = header.indexOfFirst { "weight" in it.lowercase() }
        val ownCols = setOfNotNull(dateCol, timeCol, weightCol)
        val profile = profile(rows.take(headerIndex).filterIndexed { i, _ -> i != rangeIndex })

        val out = mutableListOf<String>()
        repeat(headerIndex) { out += "" }
        out += line(listOfNotNull(header[dateCol], timeCol?.let { header[it] }, header[weightCol], "raw"))
        var year = endYear
        var previous: Int? = null
        for (cells in rows.drop(headerIndex + 1)) {
            if (cells.all { it.isEmpty() }) {
                out += ""
                continue
            }
            val date = cells.getOrElse(dateCol) { "" }
            val md = monthDay.matchEntire(date)
            val datedText = if (md == null) {
                date
            } else {
                val (month, day) = md.destructured
                val key = month.toInt() * 100 + day.toInt()
                if (previous != null && key > previous) year--
                previous = key
                "%04d-%02d-%02d".format(year, month.toInt(), day.toInt())
            }
            val fields = LinkedHashMap<String, String>()
            header.forEachIndexed { i, name -> if (i !in ownCols && cells.getOrElse(i) { "" }.isNotEmpty()) fields[name.ifEmpty { "col${i + 1}" }] = cells[i] }
            fields.putAll(profile)
            val raw = RawFormat.encode(RawEntry(0, SOURCE, fields))
            out += line(listOfNotNull(datedText, timeCol?.let { cells.getOrElse(it) { "" } }, cells.getOrElse(weightCol) { "" }, raw))
        }
        return SheetText(out.joinToString("\n"), okok = true, keptColumns = header.size - ownCols.size, profileCells = profile.size)
    }

    /** Title cells as written: `Label: value` under its label, others as `profile1`, `profile2` ... */
    private fun profile(titleRows: List<List<String>>): Map<String, String> {
        val fields = LinkedHashMap<String, String>()
        var n = 0
        for (cell in titleRows.flatten().filter { it.isNotEmpty() }) {
            val m = labelled.matchEntire(cell)
            if (m != null && m.groupValues[2].isNotEmpty()) fields[m.groupValues[1].trim()] = m.groupValues[2].trim() else fields["profile${++n}"] = cell
        }
        return fields
    }

    private fun line(cells: List<String>) = cells.joinToString(";") { quote(it) }

    private fun quote(text: String): String =
        if (text.any { it == ';' || it == '"' || it == '\n' }) "\"" + text.replace("\"", "\"\"").replace('\n', ' ') + "\"" else text
}
