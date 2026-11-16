package fi.jukkakot.terveyslappu.ui

import fi.jukkakot.terveyslappu.burn.BurnDay
import fi.jukkakot.terveyslappu.burn.BurnSeries
import fi.jukkakot.terveyslappu.chart.ChartData
import fi.jukkakot.terveyslappu.chart.ChartLayout
import fi.jukkakot.terveyslappu.chart.ChartSeries
import fi.jukkakot.terveyslappu.chart.CustomChart
import fi.jukkakot.terveyslappu.chart.CustomChartData
import fi.jukkakot.terveyslappu.chart.Period
import fi.jukkakot.terveyslappu.chart.SeriesKind
import fi.jukkakot.terveyslappu.chart.SeriesStyle
import fi.jukkakot.terveyslappu.chart.WeightScale
import fi.jukkakot.terveyslappu.food.Amount
import fi.jukkakot.terveyslappu.food.AmountUnit
import fi.jukkakot.terveyslappu.food.EnergySeries
import fi.jukkakot.terveyslappu.food.FoodItem
import fi.jukkakot.terveyslappu.steps.StepsDay
import fi.jukkakot.terveyslappu.steps.StepsSeries
import fi.jukkakot.terveyslappu.weight.WeightEntry
import fi.jukkakot.terveyslappu.weight.weightEntry
import java.time.LocalDate
import java.time.ZoneId

/** Made-up chart data for the Historia chart tests and renders (NFR-1: never real values). */
object ChartFixtures {
    val zone: ZoneId = ZoneId.systemDefault()
    val today: LocalDate = LocalDate.now(zone)
    private val now = today.atTime(21, 0).atZone(zone).toInstant()

    private fun wobble(i: Int) = ((i * 37) % 11 - 5) / 20.0

    /** About one weight a day over the last 30 days, a few skipped, at 7.05. */
    val weights: List<WeightEntry> = (0 until 30).filter { it !in setOf(3, 4, 11, 17, 18, 25) }.map { back ->
        val day = today.minusDays(back.toLong())
        weightEntry("w$back", day.atTime(7, 5).atZone(zone).toInstant(), null, 82.0 + back * 0.08 + wobble(back), "me", "me")
    }

    val weightMonth: ChartData get() = ChartData.of(Period.MONTH, weights, now, zone)!!

    /** BMI at 180 cm of the same weights. */
    val bmiMonth: ChartData
        get() {
            val perKg = 1 / (1.8 * 1.8)
            return ChartData.of(Period.MONTH, weights, now, zone, value = { it.kg * perKg }) { WeightScale.of(it, perKg) }!!
        }

    private fun food(day: LocalDate, kcal: Double, treat: Boolean = false, pos: Int = 1): FoodItem {
        val t = day.atTime(12, 0).atZone(zone).toInstant()
        return FoodItem("$day-$pos", day, pos, "Annos", kcal, Amount(1.0, AmountUnit.KPL), treat, t, zone.rules.getOffset(t), true)
    }

    val foodItems: List<FoodItem> = (0 until 200).filter { it % 9 != 4 }.flatMap { back ->
        val day = today.minusDays(back.toLong())
        listOf(food(day, 1500.0 + (back * 53) % 700), food(day, (back * 31 % 4) * 90.0, treat = true, pos = 2))
    }

    val energyWeek: EnergySeries get() = EnergySeries.of(foodItems, Period.WEEK, today)

    val stepDays: List<StepsDay> = (0 until 200).filter { it % 7 != 3 }.map { back -> StepsDay(today.minusDays(back.toLong()), 4000L + (back * 977L) % 7000) }

    val stepsWeek: StepsSeries get() = StepsSeries.of(stepDays, Period.WEEK, today)
    val stepsHalfYear: StepsSeries get() = StepsSeries.of(stepDays, Period.Last(180), today)

    /** Split days, a kokonais day and an active-only day. */
    val burnDays: List<BurnDay> = (0 until 60).mapNotNull { back ->
        val day = today.minusDays(back.toLong())
        when (back % 10) {
            4 -> BurnDay.of(day, 2300, null)
            7 -> BurnDay.of(day, null, 520)
            9 -> null
            else -> BurnDay.of(day, 2200L + (back * 61L) % 700, 300L + (back * 47L) % 500)
        }
    }

    val burnTwoWeeks: BurnSeries get() = BurnSeries.of(burnDays, Period.Last(14), today)

    fun chart(layout: ChartLayout, vararg series: Pair<SeriesKind, SeriesStyle>, average: Boolean = false) =
        CustomChart(1, "Liike", series.map { ChartSeries(it.first, it.second) }, layout, Period.MONTH, showAverage = average)

    fun data(chart: CustomChart, weights: Boolean = true, food: Boolean = true, steps: Boolean = true, burn: Boolean = true): CustomChartData =
        CustomChartData.of(
            chart, today, zone,
            if (weights) this.weights else emptyList(),
            if (food) foodItems else emptyList(),
            if (steps) stepDays else null,
            burnDays = if (burn) burnDays else null,
        )

    val three = arrayOf(SeriesKind.WEIGHT to SeriesStyle.LINE, SeriesKind.ENERGY to SeriesStyle.BARS, SeriesKind.STEPS to SeriesStyle.BARS)
}
