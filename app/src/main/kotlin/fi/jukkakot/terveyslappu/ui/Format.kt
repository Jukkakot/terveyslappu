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

/** "tänään 7.05", "eilen 7.12", "ma 6.11. 7.02" (the home card's time). */
fun formatDayTime(time: Instant, today: LocalDate, zone: ZoneId = ZoneId.systemDefault()): String =
    "${formatDay(time, today, zone)} ${timeFormat.format(time.atZone(zone))}"

/** A list row's time: "Tänään 7.05", "Ma 6.11. 7.02". */
fun formatRowTime(time: Instant, today: LocalDate, zone: ZoneId = ZoneId.systemDefault()): String =
    formatDayTime(time, today, zone).replaceFirstChar { it.titlecase(finnish) }

private val shortDayFormat =DateTimeFormatter.ofPattern("d.M.", finnish)
private val pointFormat = DateTimeFormatter.ofPattern("d.M. H.mm", finnish)

/** "8.11." */
fun formatShortDay(day: LocalDate): String = shortDayFormat.format(day)

/** A chart dot: "8.11. 7.05 82,6 kg". */
fun formatPoint(time: Instant, kg: Double, zone: ZoneId = ZoneId.systemDefault()): String =
    "${pointFormat.format(time.atZone(zone))} ${formatKg(kg)} kg"

/** Change per week with two decimals: −0,25 / +0,10 / ±0,00. */
fun formatRate(kgPerWeek: Double): String {
    val hundredths = Math.round(kgPerWeek * 100)
    val abs = String.format(finnish, "%.2f", kotlin.math.abs(hundredths) / 100.0)
    return when {
        hundredths < 0 -> "−$abs"
        hundredths > 0 -> "+$abs"
        else -> "±$abs"
    }
}
