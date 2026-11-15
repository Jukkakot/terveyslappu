package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.burn.BurnSeries
import fi.jukkakot.terveyslappu.food.EnergySeries
import fi.jukkakot.terveyslappu.steps.StepsSeries
import fi.jukkakot.terveyslappu.ui.formatKcal
import fi.jukkakot.terveyslappu.ui.formatSteps
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.IsoFields
import java.util.Locale

/**
 * The crosshair readout texts of Historia's charts (weight-chart "Crosshair on every Historia
 * chart"): a date head (weekday and date, the year when not [today]'s; on weekly bars the week)
 * and each tab's values, joined by " · ". Pure.
 */
object TabReadouts {
    private const val SEP = " · "
    private val weekdays = listOf("Ma", "Ti", "Ke", "To", "Pe", "La", "Su")
    private val time = DateTimeFormatter.ofPattern("H.mm", Locale.forLanguageTag("fi"))

    const val NO_WEIGHT = "ei painoa"
    const val NO_FOOD = "ei kirjauksia"
    const val NO_DATA = "ei tietoa"

    /** "Ma 9.11.", "Su 15.9.2024". */
    fun day(day: LocalDate, today: LocalDate): String {
        val year = if (day.year != today.year) "${day.year}" else ""
        return "${weekdays[day.dayOfWeek.value - 1]} ${day.dayOfMonth}.${day.monthValue}.$year"
    }

    /**
     * A weekly bar from [monday]: "Vk 46 · 9.–15.11.", both months when they differ
     * ("Vk 49 · 30.11.–6.12."), the year after the Sunday when it is not [today]'s.
     */
    fun week(monday: LocalDate, today: LocalDate): String {
        val sunday = monday.plusDays(6)
        val start = if (monday.monthValue == sunday.monthValue && monday.year == sunday.year) "${monday.dayOfMonth}." else "${monday.dayOfMonth}.${monday.monthValue}."
        val year = if (sunday.year != today.year) "${sunday.year}" else ""
        return "Vk ${monday.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)}$SEP$start–${sunday.dayOfMonth}.${sunday.monthValue}.$year"
    }

    private fun join(vararg parts: String) = parts.joinToString(SEP)

    /** Paino and BMI: "Su 8.11. · 7.05 · 82,6 kg" from the day's last weight in [data] (no "?" weights there), or "… · ei painoa". */
    fun weight(data: ChartData, day: LocalDate, zone: ZoneId, today: LocalDate, label: (Double) -> String): String {
        val last = data.entries.indices
            .filter { data.entries[it].time.atZone(zone).toLocalDate() == day }
            .maxByOrNull { data.entries[it].time }
            ?: return join(day(day, today), NO_WEIGHT)
        return join(day(day, today), time.format(data.entries[last].time.atZone(zone)), label(data.ys[last]))
    }

    /** Syöty: "Ma 9.11. · 1 760 kcal · herkut 230 kcal" or "… · ei kirjauksia"; a week "Vk 46 · 9.–15.11. · keskim. 1 900 kcal/pv". */
    fun energy(series: EnergySeries, key: LocalDate, today: LocalDate): String {
        if (series.weekly) {
            val bar = series.bars.firstOrNull { it.from == key }
            return join(week(key, today), bar?.let { "keskim. ${formatKcal(it.total)} kcal/pv" } ?: NO_FOOD)
        }
        val logged = series.days.firstOrNull { it.day == key } ?: return join(day(key, today), NO_FOOD)
        return join(day(key, today), "${formatKcal(logged.total)} kcal", "herkut ${formatKcal(logged.treat)} kcal")
    }

    /** Askeleet: "To 12.11. · 9 202 askelta" or "… · ei tietoa"; a week "… · keskim. 8 200 askelta/pv". */
    fun steps(series: StepsSeries, key: LocalDate, today: LocalDate): String {
        if (series.weekly) {
            val bar = series.bars.firstOrNull { it.from == key }
            return join(week(key, today), bar?.let { "keskim. ${formatSteps(it.steps)} askelta/pv" } ?: NO_DATA)
        }
        val day = series.days.firstOrNull { it.day == key } ?: return join(day(key, today), NO_DATA)
        return join(day(key, today), "${formatSteps(day.steps)} askelta")
    }

    /**
     * Kulutus (energy-burn "Kulutus tab"): "Ma 12.11. · 2 300 kcal · aktiivinen 450 · lepo 1 850",
     * a kokonais day "… · 2 300 kcal", or "… · ei tietoa"; a week the same values of its bar.
     */
    fun burn(series: BurnSeries, key: LocalDate, today: LocalDate): String {
        val head = if (series.weekly) week(key, today) else day(key, today)
        val bar = series.bars.firstOrNull { it.from == key } ?: return join(head, NO_DATA)
        val total = "${formatKcal(bar.total.toDouble())} kcal"
        val active = bar.active ?: return join(head, total)
        val rest = bar.rest ?: return join(head, total, "aktiivinen ${formatKcal(active.toDouble())}")
        return join(head, total, "aktiivinen ${formatKcal(active.toDouble())}", "lepo ${formatKcal(rest.toDouble())}")
    }
}
