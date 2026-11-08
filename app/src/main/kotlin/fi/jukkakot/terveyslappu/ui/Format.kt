package fi.jukkakot.terveyslappu.ui

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val finnish = Locale.forLanguageTag("fi")
private val dateTimeFormat = DateTimeFormatter.ofPattern("EEE d.M. 'klo' H.mm", finnish)
private val dateFormat = DateTimeFormatter.ofPattern("EEE d.M.yyyy", finnish)
private val timeFormat = DateTimeFormatter.ofPattern("H.mm", finnish)

/** 82.4 → "82,4". */
fun formatKg(kg: Double): String = String.format(finnish, "%.1f", kg)

fun formatDateTime(time: Instant, zone: ZoneId = ZoneId.systemDefault()): String = dateTimeFormat.format(time.atZone(zone))

fun formatDate(time: LocalDateTime): String = dateFormat.format(time)

fun formatTime(time: LocalDateTime): String = timeFormat.format(time)

private val dayFormat = DateTimeFormatter.ofPattern("EEE d.M.", finnish)

/** Change between two weights: −0,3 / +0,3 / ±0,0 (one decimal, real minus sign). */
fun formatChange(deltaKg: Double): String {
    val tenths = Math.round(deltaKg * 10)
    val abs = String.format(finnish, "%.1f", kotlin.math.abs(tenths) / 10.0)
    return when {
        tenths < 0 -> "−$abs"
        tenths > 0 -> "+$abs"
        else -> "±$abs"
    }
}

/** The day of [time] relative to [today]: "tänään", "eilen" or "ti 7.11.". */
fun formatDay(time: Instant, today: LocalDate, zone: ZoneId = ZoneId.systemDefault()): String {
    val day = time.atZone(zone).toLocalDate()
    return when (day) {
        today -> "tänään"
        today.minusDays(1) -> "eilen"
        else -> dayFormat.format(day)
    }
}
