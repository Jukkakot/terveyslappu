package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.food.EnergySeries
import fi.jukkakot.terveyslappu.food.FoodItem
import fi.jukkakot.terveyslappu.steps.StepsDay
import fi.jukkakot.terveyslappu.steps.StepsSeries
import fi.jukkakot.terveyslappu.weight.WeightEntry
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

/**
 * The shared time axis of every bar, line and crosshair (custom-charts design 6): one slot per day
 * from [from] to [to], x at the day's centre. [weekly] only for the fixed Energia and Askeleet tabs
 * (ISO weeks from Monday); own charts are always daily.
 */
data class ChartAxis(val from: LocalDate, val to: LocalDate, val weekly: Boolean = false) {
    val days: Int get() = (ChronoUnit.DAYS.between(from, to) + 1).toInt().coerceAtLeast(1)

    fun dayWidth(width: Float): Float = width / days

    /** The centre of [day]'s slot on a plot [width] wide. */
    fun x(day: LocalDate, width: Float): Float = ((ChronoUnit.DAYS.between(from, day) + 0.5) * dayWidth(width)).toFloat()

    /** The left edge of [day]'s slot. */
    fun left(day: LocalDate, width: Float): Float = (ChronoUnit.DAYS.between(from, day) * dayWidth(width).toDouble()).toFloat()

    /** The day under [x] (clamped into the axis). */
    fun dayAt(x: Float, width: Float): LocalDate {
        val index = (x / width * days).toInt().coerceIn(0, days - 1)
        return from.plusDays(index.toLong())
    }

    /** A weekly bar's Monday. */
    fun weekOf(day: LocalDate): LocalDate = day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    operator fun contains(day: LocalDate): Boolean = day in from..to
}

/** One day's own value; [treat] is the treats' share of an energy day. */
data class DayValue(val day: LocalDate, val value: Double, val treat: Double = 0.0)

/**
 * One shown data of an own chart over the axis. [values] is null while the data is not loaded or
 * has no access (the UI tells which); empty when it has nothing in the period. [ticks]: bottom,
 * middle and top of its scale (empty without values).
 */
class KindData(
    val kind: SeriesKind,
    val style: SeriesStyle,
    val values: List<DayValue>?,
    val ticks: List<Double>,
    /** The period average for Keskiarvo (see [CustomChartData.average]); null when nothing to average. */
    val average: Double? = null,
) {
    val drawn: Boolean get() = !values.isNullOrEmpty()

    val min: Double get() = ticks.first()
    val max: Double get() = ticks.last()

    /** Where [v] sits on the scale: 0 = bottom, 1 = top. */
    fun fraction(v: Double): Float = if (ticks.isEmpty() || max == min) 0f else ((v - min) / (max - min)).toFloat()

    fun at(day: LocalDate): DayValue? = values?.firstOrNull { it.day == day }
}

/**
 * What an own chart draws (custom-charts design 7): real daily values only, never averages or weeks.
 * One instance per chart feeds its tab, its card and the readout.
 */
class CustomChartData(val chart: CustomChart, val axis: ChartAxis, val kinds: List<KindData>) {
    /** Kinds with values to draw, in kind order. */
    val drawn: List<KindData> get() = kinds.filter { it.drawn }

    fun of(kind: SeriesKind): KindData? = kinds.firstOrNull { it.kind == kind }

    /** Every shown data has loaded and none has a value in the period. */
    val empty: Boolean get() = kinds.all { it.values != null && it.values.isEmpty() }

    companion object {
        const val KCAL_STEP = 250.0
        const val STEPS_STEP = 1000.0

        /** Each day's last weight not marked "?" (the readout and the weight line or bars). */
        fun lastWeights(weights: List<WeightEntry>, zone: ZoneId, questioned: Set<String> = emptySet()): List<DayValue> = weights
            .filter { it.id !in questioned }
            .groupBy { it.time.atZone(zone).toLocalDate() }
            .map { (day, list) -> DayValue(day, list.maxBy { it.time }.kg) }
            .sortedBy { it.day }

        /**
         * [weights], [foodItems] and [stepDays] are null while not loaded or without access; only the
         * chart's shown kinds are used. Kaikki starts at the first day with data among the shown, loaded
         * data (just today when there is none).
         */
        fun of(
            chart: CustomChart,
            today: LocalDate,
            zone: ZoneId,
            weights: List<WeightEntry>?,
            foodItems: Collection<FoodItem>?,
            stepDays: Collection<StepsDay>?,
            questioned: Set<String> = emptySet(),
        ): CustomChartData {
            val all: Map<SeriesKind, List<DayValue>?> = chart.series.associate { s ->
                s.kind to when (s.kind) {
                    SeriesKind.WEIGHT -> weights?.let { lastWeights(it, zone, questioned) }
                    SeriesKind.ENERGY -> foodItems?.let { items -> EnergySeries.loggedDays(items).map { DayValue(it.day, it.total.toDouble(), it.treat.toDouble()) } }
                    SeriesKind.STEPS -> stepDays?.let { days -> StepsSeries.stepDays(days).map { DayValue(it.day, it.steps.toDouble()) } }
                }
            }
            val first = all.values.mapNotNull { it?.firstOrNull()?.day }.minOrNull()
            val (from, to) = AxisTicks.periodDates(chart.period, today, first)
            val axis = ChartAxis(from, to)
            val kinds = chart.series.map { s ->
                val values = all[s.kind]?.filter { it.day in from..to }
                KindData(s.kind, s.style, values, ticks(s.kind, values), average(s.kind, values, today))
            }
            return CustomChartData(chart, axis, kinds)
        }

        /**
         * The period average of [values] (spec "Custom charts"): Energia per logged day and Askeleet
         * per day with steps, today left out; Paino every day's last weight, today included. Null
         * when there is nothing to average.
         */
        fun average(kind: SeriesKind, values: List<DayValue>?, today: LocalDate): Double? {
            val counted = if (kind == SeriesKind.WEIGHT) values else values?.filter { it.day != today }
            return counted?.takeIf { it.isNotEmpty() }?.map { it.value }?.average()
        }

        private fun ticks(kind: SeriesKind, values: List<DayValue>?): List<Double> {
            if (values.isNullOrEmpty()) return emptyList()
            val top = values.maxOf { it.value }
            return when (kind) {
                SeriesKind.WEIGHT -> WeightScale.of(values.map { it.value }).let { AxisTicks.line(it.min, it.max) }
                SeriesKind.ENERGY -> AxisTicks.bars(top, KCAL_STEP)
                SeriesKind.STEPS -> AxisTicks.bars(top, STEPS_STEP)
            }
        }
    }
}

/** A day's values for the crosshair readout (custom-charts design 10); [values] null = "ei …". */
data class Readout(val day: LocalDate, val values: List<Pair<SeriesKind, DayValue?>>)

object ChartReadout {
    /** Daily only; kinds without access ([access] false) are left out; today unmarked. */
    fun of(day: LocalDate, data: CustomChartData, access: (SeriesKind) -> Boolean): Readout =
        Readout(day, data.kinds.filter { access(it.kind) }.map { it.kind to it.at(day) })
}
