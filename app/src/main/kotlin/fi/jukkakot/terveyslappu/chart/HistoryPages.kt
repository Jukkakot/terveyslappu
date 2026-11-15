package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.derived.Quantity

/** One page of Historia's pager (custom-charts design 11). */
sealed interface HistoryPage {
    data class Fixed(val quantity: Quantity) : HistoryPage
    data object Energy : HistoryPage
    data object Steps : HistoryPage
    data object Burn : HistoryPage
    data class Custom(val id: Int) : HistoryPage
    data object NewChart : HistoryPage
}

/** Historia's pages in tab order: Paino, BMI, Syöty, Askeleet, Kulutus, the own charts, Uusi kaavio. Pure. */
class HistoryPages private constructor(val pages: List<HistoryPage>) {
    val size: Int get() = pages.size

    operator fun get(index: Int): HistoryPage = pages[index.coerceIn(0, pages.lastIndex)]

    fun indexOf(page: HistoryPage): Int = pages.indexOf(page)

    /** The page of chart [id] (after a create, or opened from its card); -1 when it does not exist. */
    fun indexOfChart(id: Int): Int = indexOf(HistoryPage.Custom(id))

    companion object {
        val ENERGY = Quantity.entries.size
        val STEPS = ENERGY + 1
        val BURN = STEPS + 1

        fun of(charts: List<CustomChart>): HistoryPages = HistoryPages(
            Quantity.entries.map { HistoryPage.Fixed(it) } + HistoryPage.Energy + HistoryPage.Steps + HistoryPage.Burn +
                charts.map { HistoryPage.Custom(it.id) } + HistoryPage.NewChart,
        )

        /** After chart [id] of [before] is deleted: the page before its tab. */
        fun afterDelete(before: List<CustomChart>, id: Int): Int = (of(before).indexOfChart(id) - 1).coerceAtLeast(0)
    }
}
