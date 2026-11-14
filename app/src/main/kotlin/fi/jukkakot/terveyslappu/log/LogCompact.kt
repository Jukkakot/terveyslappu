package fi.jukkakot.terveyslappu.log

import fi.jukkakot.terveyslappu.scale.PacketLog
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** The text "Kopioi loki" puts on the clipboard (app-log spec): compact, last 24 h, capped. */
object LogCompact {
    const val MAX_CHARS = 40_000
    private val window: Duration = Duration.ofHours(24)
    private val copiedFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")

    class Result(val text: String, val lines: Int)

    /** [lines] oldest first (older file, then newer). [lines] in the result counts log lines kept, header excluded. */
    fun build(lines: List<String>, version: String, commit: String, now: Instant, zone: ZoneId): Result {
        val header = "terveyslappu v$version commit=$commit copied=${copiedFormat.format(now.atZone(zone))}"
        val recent = lines.filter { it.isNotEmpty() && isRecent(it, now) }
        val compact = collapsePackets(recent)

        // Keep the newest lines that fit; the notice line is reserved room when something is dropped.
        var total = header.length + 1
        var keep = 0
        for (line in compact.asReversed()) {
            if (total + line.length + 1 > MAX_CHARS) break
            total += line.length + 1
            keep++
        }
        val dropped = compact.size - keep
        val kept = compact.takeLast(keep)
        if (dropped == 0) return Result((listOf(header) + kept).joinToString("\n"), kept.size)

        val notice = "... $dropped older lines left out"
        var room = MAX_CHARS - header.length - notice.length - 2
        var fit = 0
        for (line in kept.asReversed()) {
            if (line.length + 1 > room) break
            room -= line.length + 1
            fit++
        }
        val tail = kept.takeLast(fit)
        return Result((listOf(header, "... ${compact.size - fit} older lines left out") + tail).joinToString("\n"), tail.size)
    }

    private fun isRecent(line: String, now: Instant): Boolean {
        val time = runCatching { OffsetDateTime.parse(line.substringBefore(' ')).toInstant() }.getOrNull() ?: return true
        return !time.isBefore(now.minus(window))
    }

    /** Runs of `scale.packet` lines with equal key and data become the first line plus ` xN rssi=min..max`. */
    internal fun collapsePackets(lines: List<String>): List<String> {
        val out = ArrayList<String>()
        var i = 0
        while (i < lines.size) {
            val first = packetOf(lines[i])
            if (first == null) {
                out += lines[i++]
                continue
            }
            var j = i + 1
            val rssi = mutableListOf<Int>()
            first.rssi?.let { rssi += it }
            while (j < lines.size) {
                val next = packetOf(lines[j]) ?: break
                if (next.key != first.key || next.data != first.data) break
                next.rssi?.let { rssi += it }
                j++
            }
            val count = j - i
            out += if (count == 1 || rssi.isEmpty()) {
                lines[i] + if (count > 1) " x$count" else ""
            } else {
                "${lines[i]} x$count rssi=${rssi.min()}..${rssi.max()}"
            }
            i = j
        }
        return out
    }

    private class Packet(val key: String?, val data: String?, val rssi: Int?)

    private fun packetOf(line: String): Packet? {
        val parts = line.split(' ')
        if (parts.size < 3 || parts[2] != PacketLog.EVENT) return null
        fun field(name: String) = parts.drop(3).firstOrNull { it.startsWith("$name=") }?.substringAfter('=')
        return Packet(field("key"), field("data"), field("rssi")?.toIntOrNull())
    }
}
