package fi.jukkakot.terveyslappu.chart

import android.content.Context

/** The last chosen chart period, kept between app starts (app-private preferences). */
class PeriodPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("chart", Context.MODE_PRIVATE)

    fun load(): Period = Period.decode(prefs.getString(KEY, null))

    fun save(period: Period) = prefs.edit().putString(KEY, period.encode()).apply()

    private companion object {
        const val KEY = "period"
    }
}
