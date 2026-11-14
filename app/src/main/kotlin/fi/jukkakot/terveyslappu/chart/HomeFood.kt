package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.food.DaySum
import fi.jukkakot.terveyslappu.food.EnergyBar
import fi.jukkakot.terveyslappu.food.EnergySeries
import fi.jukkakot.terveyslappu.food.FoodItem
import java.time.LocalDate

/**
 * What the front page's food card shows (home-screen "Food row on the home screen"): today's sum,
 * the daily bars of the last `days` days ending today and the average per logged day (today left
 * out, as on Energia; the numbers come from [EnergySeries]).
 */
data class HomeFood(
    val todayTotal: Int,
    val todayTreat: Int,
    /** Today has at least one item with kcal. */
    val todayLogged: Boolean,
    val from: LocalDate,
    val to: LocalDate,
    /** One bar per logged day, oldest first. */
    val bars: List<EnergyBar>,
    val averageKcal: Int?,
) {
    companion object {
        fun of(items: Collection<FoodItem>, days: Long, today: LocalDate): HomeFood {
            val series = EnergySeries.of(items, Period.Last(days), today)
            val sum = DaySum.of(items.filter { it.day == today })
            return HomeFood(sum.total, sum.treat, sum.logged, series.from, series.to, series.bars, series.stats.averageKcal)
        }
    }
}
