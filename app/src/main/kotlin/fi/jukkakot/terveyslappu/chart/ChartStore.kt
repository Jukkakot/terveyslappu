package fi.jukkakot.terveyslappu.chart

import android.content.Context
import fi.jukkakot.terveyslappu.log.AppLog
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDate

/**
 * The own charts as one JSON string (custom-charts design 3):
 * `{"v":1,"next":3,"charts":[{"id":1,"name":"…","layout":"stacked","period":{"k":"last","days":30},"series":[{"kind":"weight","style":"line"},…]}]}`.
 * Pure; an unreadable value is null (the store then shows no charts and logs it once).
 */
object ChartCodec {
    const val VERSION = 1

    @Serializable
    private data class StoredPeriod(val k: String, val days: Long? = null, val from: String? = null, val to: String? = null)

    @Serializable
    private data class StoredSeries(val kind: String, val style: String)

    @Serializable
    private data class StoredChart(val id: Int, val name: String, val layout: String, val period: StoredPeriod, val series: List<StoredSeries>)

    @Serializable
    private data class Stored(val v: Int, val next: Int, val charts: List<StoredChart>)

    private val json = Json { ignoreUnknownKeys = true }

    fun encode(list: ChartList): String = json.encodeToString(
        Stored.serializer(),
        Stored(VERSION, list.next, list.charts.map { c -> StoredChart(c.id, c.name, c.layout.name.lowercase(), period(c.period), c.series.map { StoredSeries(it.kind.name.lowercase(), it.style.name.lowercase()) }) }),
    )

    /** Null when [text] cannot be read; an empty value is no charts. */
    fun decode(text: String?): ChartList? {
        if (text.isNullOrBlank()) return ChartList()
        return runCatching {
            val stored = json.decodeFromString(Stored.serializer(), text)
            require(stored.v == VERSION)
            val charts = stored.charts.map { c ->
                val series = c.series.map { ChartSeries(enumOf(it.kind), enumOf(it.style)) }.distinctBy { it.kind }.sortedBy { it.kind }
                require(series.isNotEmpty() && c.name.isNotBlank())
                CustomChart(c.id, c.name, series, enumOf(c.layout), period(c.period))
            }.distinctBy { it.id }
            ChartList(charts, maxOf(stored.next, (charts.maxOfOrNull { it.id } ?: 0) + 1))
        }.getOrNull()
    }

    private inline fun <reified E : Enum<E>> enumOf(name: String): E = enumValues<E>().first { it.name.equals(name, ignoreCase = true) }

    private fun period(p: Period): StoredPeriod = when (p) {
        is Period.Last -> StoredPeriod("last", days = p.days)
        Period.All -> StoredPeriod("all")
        is Period.Custom -> StoredPeriod("custom", from = p.from.toString(), to = p.to.toString())
    }

    private fun period(p: StoredPeriod): Period = when (p.k) {
        "last" -> Period.Last(requireNotNull(p.days).also { require(it > 0) })
        "all" -> Period.All
        "custom" -> {
            val from = LocalDate.parse(p.from)
            val to = LocalDate.parse(p.to)
            require(!to.isBefore(from))
            Period.Custom(from, to)
        }
        else -> error("period")
    }
}

/**
 * The own charts in the `chart` prefs file, key `customCharts` (custom-charts design 3), so Android's
 * transfer carries them with the periods; not in the data or backup file. Singleton in `TerveysApp`.
 */
class ChartStore(context: Context) {
    private val prefs = context.getSharedPreferences("chart", Context.MODE_PRIVATE)

    fun load(): ChartList = ChartCodec.decode(prefs.getString(KEY, null)) ?: run {
        AppLog.w("chart.custom.decode", "failed")
        ChartList()
    }

    fun save(list: ChartList) = prefs.edit().putString(KEY, ChartCodec.encode(list)).apply()

    private companion object {
        const val KEY = "customCharts"
    }
}
