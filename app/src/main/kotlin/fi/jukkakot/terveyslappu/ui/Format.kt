package fi.jukkakot.terveyslappu.ui

import java.time.Instant
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
