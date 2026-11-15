package fi.jukkakot.terveyslappu.home

import android.content.Context

/**
 * The front page layout, kept between app starts in the `chart` prefs file next to the card periods
 * (home-card-layout design 7), so Android's transfer carries it; not in the data or backup file.
 */
class HomePrefs(context: Context, private val codec: HomeLayoutCodec = HomeLayoutCodec()) {
    private val prefs = context.getSharedPreferences("chart", Context.MODE_PRIVATE)

    fun load(): HomeLayout = codec.decode(prefs.getString(KEY, null))

    fun save(layout: HomeLayout) = prefs.edit().putString(KEY, codec.encode(layout)).apply()

    private companion object {
        const val KEY = "homeLayout"
    }
}
