package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.burn.BurnSeries
import fi.jukkakot.terveyslappu.food.EnergySeries
import fi.jukkakot.terveyslappu.steps.StepsSeries
import fi.jukkakot.terveyslappu.ui.formatKcal
import fi.jukkakot.terveyslappu.ui.formatKg
import fi.jukkakot.terveyslappu.ui.formatSteps
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.IsoFields
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/**
 * The marker card texts of Historia's charts (weight-chart "Crosshair on every Historia chart",
 * "Point details", energy-burn "Kulutus tab"): a date head (weekday and date, the year when not
 * [today]'s; on weekly bars the week) and one row per series. Pure.
 */
object TabReadouts {
    private const val SEP = " · "
    private val weekdays = listOf("Ma", "Ti", "Ke", "To", "Pe", "La", "Su")
    private val time = DateTimeFormatter.ofPattern("H.mm", Locale.forLanguageTag("fi"))

    const val NO_WEIGHT = "ei painoa"
    const val NO_FOOD = "ei annoksia"
    const val NO_DATA = "ei tietoa"

    const val WEIGHT = "paino"
    const val BMI = "BMI"
    const val FOOD = "syöty"
    const val TREAT = "herkut"
    const val STEPS = "askeleet"
    const val BURN = "kulutus"

    /** The Kulutus tab's tone (the palette's oranssi). */
    val BURN_TONE: ToneRef = ToneRef.BURN

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

    private fun monday(key: LocalDate) = key.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    const val TREND = "trendi"

    /**
     * The trend at a chosen [key] (weight-chart "Trend line in the marker card"): the day itself; a
     * weekly bar's the week's middle day when it is inside the axis and covered, else the nearest
     * covered day of the week inside the axis; null when none is covered.
     */
    fun trendAt(trend: List<TrendSegment>?, key: LocalDate, axis: ChartAxis): Double? {
        if (trend.isNullOrEmpty()) return null
        if (!axis.weekly) return trend.at(key)
        val mid = monday(key).plusDays(3)
        return (0L..6L).map { monday(key).plusDays(it) }
            .filter { it in axis }
            .sortedBy { kotlin.math.abs(java.time.temporal.ChronoUnit.DAYS.between(mid, it)) }
            .firstNotNullOfOrNull { trend.at(it) }
    }

    /** "8 200 askelta/pv", "2 050 kcal/pv", "81,4 kg", "24,8": a trend value like the keskiarvo rows. */
    fun trendValue(unit: ValueUnit, v: Double): String = when (unit) {
        ValueUnit.KG -> "${formatKg(v)} kg"
        ValueUnit.BMI -> formatKg(v)
        ValueUnit.KCAL -> "${formatKcal(v)} kcal/pv"
        ValueUnit.STEPS -> "${formatSteps(kotlin.math.round(v).toLong())} askelta/pv"
    }

    /** [card] with the trend row ("[name] 81,4 kg") inserted after its row [after]; unchanged without a trend that day. */
    fun withTrend(card: ReadoutCard, trend: List<TrendSegment>?, key: LocalDate, axis: ChartAxis, unit: ValueUnit, name: String = TREND, after: Int = card.rows.size - 1, tone: ToneRef = ToneRef.MUTED): ReadoutCard {
        val v = trendAt(trend, key, axis) ?: return card
        val rows = card.rows.toMutableList().apply { add(after + 1, ReadoutRow(tone, name, trendValue(unit, v))) }
        return card.copy(rows = rows)
    }

    /**
     * Paino and BMI: head "Su 8.11. · 7.05" from the day's last weight in [data] (no "?" weights
     * there) and the row "[name] 82,6 kg", or "[name] ei painoa" under the bare date.
     */
    fun weight(data: ChartData, day: LocalDate, zone: ZoneId, today: LocalDate, name: String = WEIGHT, tone: ToneRef = ToneRef.WEIGHT, label: (Double) -> String): ReadoutCard {
        val last = data.entries.indices
            .filter { data.entries[it].time.atZone(zone).toLocalDate() == day }
            .maxByOrNull { data.entries[it].time }
            ?: return ReadoutCard(day(day, today), listOf(ReadoutRow(tone, name, NO_WEIGHT)))
        val head = day(day, today) + SEP + time.format(data.entries[last].time.atZone(zone))
        return ReadoutCard(head, listOf(ReadoutRow(tone, name, label(data.ys[last]))))
    }

    /** Syöty: "syöty 1 760 kcal" and "herkut 230 kcal", or "syöty ei annoksia"; a week "syöty keskiarvo 1 900 kcal/pv". */
    fun energy(series: EnergySeries, key: LocalDate, today: LocalDate): ReadoutCard {
        val food = ToneRef.FOOD
        if (series.weekly) {
            val bar = series.bars.firstOrNull { it.from == monday(key) }
            return ReadoutCard(week(monday(key), today), listOf(ReadoutRow(food, FOOD, bar?.let { "keskiarvo ${formatKcal(it.total)} kcal/pv" } ?: NO_FOOD)))
        }
        val logged = series.days.firstOrNull { it.day == key } ?: return ReadoutCard(day(key, today), listOf(ReadoutRow(food, FOOD, NO_FOOD)))
        return ReadoutCard(
            day(key, today),
            listOf(ReadoutRow(food, FOOD, "${formatKcal(logged.total)} kcal"), ReadoutRow(ToneRef.TREAT, TREAT, "${formatKcal(logged.treat)} kcal")),
        )
    }

    /** Askeleet: "askeleet 9 202 askelta" or "askeleet ei tietoa"; a week "askeleet keskiarvo 8 200 askelta/pv". */
    fun steps(series: StepsSeries, key: LocalDate, today: LocalDate): ReadoutCard {
        val tone = ToneRef.STEPS
        if (series.weekly) {
            val bar = series.bars.firstOrNull { it.from == monday(key) }
            return ReadoutCard(week(monday(key), today), listOf(ReadoutRow(tone, STEPS, bar?.let { "keskiarvo ${formatSteps(it.steps)} askelta/pv" } ?: NO_DATA)))
        }
        val day = series.days.firstOrNull { it.day == key } ?: return ReadoutCard(day(key, today), listOf(ReadoutRow(tone, STEPS, NO_DATA)))
        return ReadoutCard(day(key, today), listOf(ReadoutRow(tone, STEPS, "${formatSteps(day.steps)} askelta")))
    }

    /**
     * Kulutus (energy-burn "Kulutus tab"): "kulutus 2 946 kcal · aktiivinen 465 kcal · lepo 2 481 kcal", a
     * kokonais day "kulutus 2 300 kcal", an active-only day "kulutus 465 kcal · aktiivinen 465 kcal", or
     * "kulutus ei tietoa"; a weekly bar the same values of its bar.
     */
    fun burn(series: BurnSeries, key: LocalDate, today: LocalDate): ReadoutCard {
        val start = if (series.weekly) monday(key) else key
        val head = if (series.weekly) week(start, today) else day(key, today)
        val bar = series.bars.firstOrNull { it.from == start } ?: return ReadoutCard(head, listOf(ReadoutRow(BURN_TONE, BURN, NO_DATA)))
        val unit = if (series.weekly) "kcal/pv" else "kcal"
        val parts = buildList {
            add("${if (series.weekly) "keskiarvo " else ""}${formatKcal(bar.total.toDouble())} $unit")
            bar.active?.let { add("aktiivinen ${formatKcal(it.toDouble())} $unit") }
            if (bar.active != null) bar.rest?.let { add("lepo ${formatKcal(it.toDouble())} $unit") }
        }
        return ReadoutCard(head, listOf(ReadoutRow(BURN_TONE, BURN, parts.joinToString(SEP))))
    }

    /**
     * An own chart (weight-chart "Chart crosshair"): one row per shown data with access, its own
     * value that day or "ei painoa" / "ei annoksia" / "ei tietoa"; Kulutus "2 300 kcal ·
     * aktiivinen 450 kcal" (no lepo). Today unmarked, no week form.
     */
    fun custom(
        readout: Readout,
        today: LocalDate,
        trends: Map<SeriesKind, List<TrendSegment>> = emptyMap(),
        trendTone: (SeriesKind) -> ToneRef = { ToneRef.MUTED },
        tone: (SeriesKind) -> ToneRef,
    ): ReadoutCard = ReadoutCard(
        day(readout.day, today),
        readout.values.flatMap { (kind, value) ->
            // A data with a trend gets "<data> trendi" right after its own row (period-trend-line design 6).
            val trend = trends[kind]?.at(readout.day)?.let { ReadoutRow(trendTone(kind), "${kindName(kind)} $TREND", trendValue(CustomChartSpec.unit(kind), it)) }
            listOfNotNull(ReadoutRow(tone(kind), kindName(kind), customValue(kind, value)), trend)
        },
    )

    fun kindName(kind: SeriesKind): String = when (kind) {
        SeriesKind.WEIGHT -> WEIGHT
        SeriesKind.ENERGY -> FOOD
        SeriesKind.BURN -> BURN
        SeriesKind.STEPS -> STEPS
    }

    private fun customValue(kind: SeriesKind, value: DayValue?): String {
        if (value == null) {
            return when (kind) {
                SeriesKind.WEIGHT -> NO_WEIGHT
                SeriesKind.ENERGY -> NO_FOOD
                SeriesKind.BURN, SeriesKind.STEPS -> NO_DATA
            }
        }
        return when (kind) {
            SeriesKind.WEIGHT -> "${formatKg(value.value)} kg"
            SeriesKind.ENERGY -> "${formatKcal(value.value)} kcal"
            SeriesKind.BURN -> value.active?.let { "${formatKcal(value.value)} kcal${SEP}aktiivinen ${formatKcal(it)} kcal" } ?: "${formatKcal(value.value)} kcal"
            SeriesKind.STEPS -> "${formatSteps(value.value.toLong())} askelta"
        }
    }
}
