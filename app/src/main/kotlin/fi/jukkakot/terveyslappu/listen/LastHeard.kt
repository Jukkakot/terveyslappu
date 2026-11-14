package fi.jukkakot.terveyslappu.listen

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** When the trial last heard the scale and how strong the signal was. */
data class LastHeard(val at: Instant, val rssi: Int)

/** The Asetukset line (background-weigh-in "Last heard"). Pure. */
object LastHeardText {
    private val finnish = Locale.forLanguageTag("fi")
    private val time = DateTimeFormatter.ofPattern("H.mm", finnish)
    private val day = DateTimeFormatter.ofPattern("d.M.", finnish)

    fun text(heard: LastHeard?, now: Instant, zone: ZoneId): String {
        if (heard == null) return "Ei vielä kuultu"
        val at = heard.at.atZone(zone)
        val today = now.atZone(zone).toLocalDate()
        val whenText = when (at.toLocalDate()) {
            today -> "tänään ${time.format(at)}"
            today.minusDays(1) -> "eilen ${time.format(at)}"
            else -> "${day.format(at)} klo ${time.format(at)}"
        }
        return "Kuultu viimeksi $whenText, signaali ${heard.rssi}"
    }
}
