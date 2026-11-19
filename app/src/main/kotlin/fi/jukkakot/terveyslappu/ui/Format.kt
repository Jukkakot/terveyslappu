package fi.jukkakot.terveyslappu.ui

import fi.jukkakot.terveyslappu.derived.shownTenths
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
fun formatKg(kg: Double): String = formatTenths(shownTenths(kg))

/** Whole tenths as "82,4" (a real minus sign for a negative). */
private fun formatTenths(tenths: Long): String {
    val abs = kotlin.math.abs(tenths)
    return (if (tenths < 0) "−" else "") + "${abs / 10},${abs % 10}"
}

fun formatDateTime(time: Instant, zone: ZoneId = ZoneId.systemDefault()): String = dateTimeFormat.format(time.atZone(zone))

fun formatDate(time: LocalDateTime): String = dateFormat.format(time)

fun formatTime(time: LocalDateTime): String = timeFormat.format(time)

/** "7.12": the local clock time of [time]. */
fun formatClock(time: Instant, zone: ZoneId = ZoneId.systemDefault()): String = timeFormat.format(time.atZone(zone))

private val dayFormat = DateTimeFormatter.ofPattern("EEE d.M.", finnish)
private val dayYearFormat = DateTimeFormatter.ofPattern("EEE d.M.yyyy", finnish)

/** Change between two weights: −0,3 / +0,3 / ±0,0 (one decimal, real minus sign). */
fun formatChange(deltaKg: Double): String {
    val tenths = shownTenths(deltaKg)
    val abs = formatTenths(kotlin.math.abs(tenths))
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
fun formatPoint(time: Instant, kg: Double, zone: ZoneId = ZoneId.systemDefault(), today: LocalDate = LocalDate.now(zone)): String =
    formatPoint(time, "${formatKg(kg)} kg", zone, today)

/** A chart dot with its value already formatted: "8.11. 7.05 BMI 25,3". */
fun formatPoint(time: Instant, value: String, zone: ZoneId = ZoneId.systemDefault(), today: LocalDate = LocalDate.now(zone)): String {
    val at = time.atZone(zone)
    val format = if (at.year == today.year) pointFormat else pointYearFormat
    return "${format.format(at)} $value"
}

/** Direction of a change as shown with [decimals] decimals: −1 down, 1 up, 0 when it shows as ±0. */
fun shownDirection(value: Double, decimals: Int): Int {
    if (decimals == 1) return java.lang.Long.signum(shownTenths(value))
    var scale = 1.0
    repeat(decimals) { scale *= 10 }
    return java.lang.Long.signum(Math.round(value * scale))
}

/** Change per week with one decimal (half away from zero): −0,3 / +0,1 / ±0,0, never "−0,0". */
fun formatRate(kgPerWeek: Double): String = formatChange(kgPerWeek)
