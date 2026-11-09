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
private val dayYearFormat = DateTimeFormatter.ofPattern("EEE d.M.yyyy", finnish)

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
        else -> if (day.year == today.year) dayFormat.format(day) else dayYearFormat.format(day)
    }
}

/** "tänään 7.05", "eilen 7.12", "ma 6.11. 7.02" (the home card's time). */
fun formatDayTime(time: Instant, today: LocalDate, zone: ZoneId = ZoneId.systemDefault()): String =
    "${formatDay(time, today, zone)} ${timeFormat.format(time.atZone(zone))}"

/** A list row's time: "Tänään 7.05", "Ma 6.11. 7.02". */
fun formatRowTime(time: Instant, today: LocalDate, zone: ZoneId = ZoneId.systemDefault()): String =
    formatDayTime(time, today, zone).replaceFirstChar { it.titlecase(finnish) }

private val shortDayFormat = DateTimeFormatter.ofPattern("d.M.", finnish)
private val monthYearFormat = DateTimeFormatter.ofPattern("M/yyyy", finnish)
private val pointFormat = DateTimeFormatter.ofPattern("d.M. H.mm", finnish)
private val pointYearFormat = DateTimeFormatter.ofPattern("d.M.yyyy H.mm", finnish)

/** "8.11." */
fun formatShortDay(day: LocalDate): String = shortDayFormat.format(day)

/** A long chart's x label: "9/2022". */
fun formatMonthYear(day: LocalDate): String = monthYearFormat.format(day)

/** A chart dot: "8.11. 7.05 82,6 kg", with the year when not from [today]'s year: "15.9.2024 8.33 97,6 kg". */
fun formatPoint(time: Instant, kg: Double, zone: ZoneId = ZoneId.systemDefault(), today: LocalDate = LocalDate.now(zone)): String {
    val at = time.atZone(zone)
    val format = if (at.year == today.year) pointFormat else pointYearFormat
    return "${format.format(at)} ${formatKg(kg)} kg"
}

/** Direction of a change as shown with [decimals] decimals: −1 down, 1 up, 0 when it shows as ±0. */
fun shownDirection(value: Double, decimals: Int): Int {
    var scale = 1.0
    repeat(decimals) { scale *= 10 }
    return java.lang.Long.signum(Math.round(value * scale))
}

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
