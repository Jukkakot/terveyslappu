package fi.jukkakot.terveyslappu.history

import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Dates as weight exports write them (history-import spec, "Date formats"): ISO and `yyyy/M/d`,
 * `d.M.yyyy`, slash or dash `d/M/yyyy` / `M/d/yyyy` (order decided for the whole file), epoch
 * seconds or ms. An optional time follows the date; a zone offset or `Z` is honoured, otherwise
 * the given zone applies.
 */
object DateParsing {

    private const val TIME = """(?:[T ,]+(\d{1,2})[:.](\d{2})(?:[:.](\d{2}))?(?:\.\d+)?\s*(Z|[+-]\d{2}:?\d{2})?)?"""
    private val yearFirst = Regex("""^(\d{4})[-/.](\d{1,2})[-/.](\d{1,2})$TIME$""")
    private val dotted = Regex("""^(\d{1,2})\.(\d{1,2})\.(\d{4})$TIME$""")
    private val slashed = Regex("""^(\d{1,2})[/-](\d{1,2})[/-](\d{4})$TIME$""")
    private val epoch = Regex("""^\d{10}(\d{3})?$""")
    private val timeOnly = Regex("""^(\d{1,2})[:.](\d{2})(?:[:.](\d{2}))?$""")

    /** Whether [text] looks like a date this parser can read (day order not needed for the answer). */
    fun looksLikeDate(text: String): Boolean {
        val t = text.trim()
        return yearFirst.matches(t) || dotted.matches(t) || slashed.matches(t) || epoch.matches(t)
    }

    /** First and second number of a slash/dash date, for deciding the day order of a file. */
    fun slashParts(text: String): Pair<Int, Int>? =
        slashed.matchEntire(text.trim())?.let { it.groupValues[1].toInt() to it.groupValues[2].toInt() }

    /** Day first unless only the second number ever exceeds 12. Returns (dayFirst, ambiguous). */
    fun dayOrder(dates: List<String>): Pair<Boolean, Boolean> {
        val parts = dates.mapNotNull(::slashParts)
        if (parts.isEmpty()) return true to false
        val firstBig = parts.any { it.first > 12 }
        val secondBig = parts.any { it.second > 12 }
        return when {
            firstBig -> true to false
            secondBig -> false to false
            else -> true to true
        }
    }

    /** A valid clock time on its own (`7:12`, `07.12`, `07:12:30`); `82.45` is not one. */
    fun isTimeOnly(text: String): Boolean = parseTime(text) != null

    /** The moment of [date] (with [time] from a separate column when the date has none), or null. */
    fun parse(date: String, time: String?, dayFirst: Boolean, zone: ZoneId): Instant? {
        val t = date.trim()
        if (epoch.matches(t)) {
            val n = t.toLong()
            return if (t.length == 10) Instant.ofEpochSecond(n) else Instant.ofEpochMilli(n)
        }
        val (match, ymd) = yearFirst.matchEntire(t)?.let { it to Triple(1, 2, 3) }
            ?: dotted.matchEntire(t)?.let { it to Triple(3, 2, 1) }
            ?: slashed.matchEntire(t)?.let { it to if (dayFirst) Triple(3, 2, 1) else Triple(3, 1, 2) }
            ?: return null
        val g = match.groupValues
        return try {
            val day = LocalDate.of(g[ymd.first].toInt(), g[ymd.second].toInt(), g[ymd.third].toInt())
            val clock = when {
                g[4].isNotEmpty() -> LocalTime.of(g[4].toInt(), g[5].toInt(), g[6].ifEmpty { "0" }.toInt())
                time != null -> parseTime(time) ?: return null
                else -> LocalTime.MIDNIGHT
            }
            val local = LocalDateTime.of(day, clock)
            val offset = g[7]
            if (offset.isEmpty()) local.atZone(zone).toInstant() else local.toInstant(parseOffset(offset))
        } catch (_: DateTimeException) {
            null
        }
    }

    private fun parseTime(text: String): LocalTime? {
        val m = timeOnly.matchEntire(text.trim()) ?: return null
        return try {
            LocalTime.of(m.groupValues[1].toInt(), m.groupValues[2].toInt(), m.groupValues[3].ifEmpty { "0" }.toInt())
        } catch (_: DateTimeException) {
            null
        }
    }

    private fun parseOffset(text: String): ZoneOffset =
        if (text == "Z") ZoneOffset.UTC else ZoneOffset.of(if (text.length == 5) text.substring(0, 3) + ":" + text.substring(3) else text)
}
