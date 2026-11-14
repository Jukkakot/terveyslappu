package fi.jukkakot.terveyslappu.chart

import android.content.Context

/** The last chosen chart period, kept between app starts (app-private preferences). */
class PeriodPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("chart", Context.MODE_PRIVATE)

    fun load(): Period = Period.decode(prefs.getString(KEY, null))

    fun save(period: Period) = prefs.edit().putString(KEY, period.encode()).apply()

    /** The home card's own period in days (home-screen "Home chart period"). */
    fun loadHomeDays(): Long = HomeSummary.homeDays(prefs.getLong(HOME_KEY, HomeSummary.DEFAULT_DAYS))

    fun saveHomeDays(days: Long) = prefs.edit().putLong(HOME_KEY, days).apply()

    /** The food card's own period in days (home-screen "Food row on the home screen"). */
    fun loadFoodDays(): Long = HomeSummary.homeDays(prefs.getLong(FOOD_KEY, HomeSummary.DEFAULT_DAYS))

    fun saveFoodDays(days: Long) = prefs.edit().putLong(FOOD_KEY, days).apply()

    /** The steps card's own period in days (home-screen "Steps card on the home screen"). */
    fun loadStepsDays(): Long = HomeSummary.homeDays(prefs.getLong(STEPS_KEY, HomeSummary.DEFAULT_DAYS))

    fun saveStepsDays(days: Long) = prefs.edit().putLong(STEPS_KEY, days).apply()

    private companion object {
        const val KEY = "period"
        const val HOME_KEY = "homeDays"
        const val FOOD_KEY = "foodDays"
        const val STEPS_KEY = "stepsDays"
    }
}
