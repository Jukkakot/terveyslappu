package fi.jukkakot.terveyslappu.history

/** Delimited text tables: separator detection and a quote-aware split. */
object Table {
    /** Candidates in tie-break order: semicolon and tab are rarer inside values than comma. */
    val SEPARATORS = listOf(';', '\t', ',')

    /**
     * The separator that splits the most lines into the same number (≥ 2) of fields. A comma
     * decimal inside unquoted values gives a consistent count too, so consistency, not the raw
     * count, decides.
     */
    fun detectSeparator(lines: List<String>): Char {
        val sample = lines.filter { it.isNotBlank() }.take(SAMPLE)
        var best = ','
        var bestScore = 0
        for (sep in SEPARATORS) {
            val counts = sample.map { split(it, sep).size }.filter { it >= 2 }
            val score = counts.groupingBy { it }.eachCount().maxOfOrNull { it.value } ?: 0
            if (score > bestScore) {
                best = sep
                bestScore = score
            }
        }
        return best
    }

    /** Splits [line] on [sep] outside double quotes; fields are trimmed and unquoted (`""` → `"`). */
    fun split(line: String, sep: Char): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var quoted = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && quoted && i + 1 < line.length && line[i + 1] == '"' -> {
                    current.append('"')
                    i++
                }
                c == '"' -> quoted = !quoted
                c == sep && !quoted -> {
                    fields += current.toString().trim()
                    current.clear()
                }
                else -> current.append(c)
            }
            i++
        }
        fields += current.toString().trim()
        return fields
    }

    private const val SAMPLE = 50
}
