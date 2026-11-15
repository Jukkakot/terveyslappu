package fi.jukkakot.terveyslappu.home

/** A front page card's size (home-screen "Card grid"): small = half width, wide = full width, big = full width and two rows. */
enum class CardSize {
    SMALL,
    WIDE,
    BIG,
    ;

    /** Grid columns taken (of two). */
    val span: Int get() = if (this == SMALL) 1 else 2

    /** Row units of height. */
    val units: Int get() = if (this == BIG) 2 else 1
}

/** One card on the front page: its id (`weight`, `food`, `steps`; `chart:<n>` later) and size. */
data class HomeCard(val id: String, val size: CardSize)

/**
 * The front page layout (home-card-layout design 1, 8): the shown cards in the user's order and the
 * hidden ones with their kept sizes. Pure; every edit returns a new layout and refuses what the
 * spec does not allow (hiding the last shown card).
 */
data class HomeLayout(val shown: List<HomeCard>, val hidden: List<HomeCard> = emptyList()) {

    /** The grid's rows in order (see [rows]). */
    fun rows(): List<List<HomeCard>> = rows(shown)

    val isDefault: Boolean get() = this == DEFAULT

    fun indexOf(id: String): Int = shown.indexOfFirst { it.id == id }

    /** Moves the card at [from] to [to] within [shown] (the drag's onMove). */
    fun move(from: Int, to: Int): HomeLayout {
        if (from !in shown.indices || to !in shown.indices || from == to) return this
        val list = shown.toMutableList()
        list.add(to, list.removeAt(from))
        return copy(shown = list)
    }

    fun moveEarlier(id: String): HomeLayout = indexOf(id).let { if (it > 0) move(it, it - 1) else this }

    fun moveLater(id: String): HomeLayout = indexOf(id).let { if (it >= 0 && it < shown.lastIndex) move(it, it + 1) else this }

    fun resize(id: String, size: CardSize): HomeLayout = copy(shown = shown.map { if (it.id == id) it.copy(size = size) else it })

    /** Whether [id] can be hidden: it is shown and not the only shown card (design 12). */
    fun canHide(id: String): Boolean = shown.size > 1 && indexOf(id) >= 0

    fun hide(id: String): HomeLayout {
        if (!canHide(id)) return this
        val card = shown[indexOf(id)]
        return HomeLayout(shown - card, hidden + card)
    }

    /** Brings a hidden card back, last, at the size it had. */
    fun show(id: String): HomeLayout {
        val card = hidden.firstOrNull { it.id == id } ?: return this
        return HomeLayout(shown + card, hidden - card)
    }

    fun reset(): HomeLayout = DEFAULT

    companion object {
        const val WEIGHT = "weight"
        const val FOOD = "food"
        const val STEPS = "steps"

        /** The fixed cards at their default sizes, in default order. */
        val FIXED: List<HomeCard> = listOf(
            HomeCard(WEIGHT, CardSize.WIDE),
            HomeCard(FOOD, CardSize.SMALL),
            HomeCard(STEPS, CardSize.SMALL),
        )

        val DEFAULT = HomeLayout(FIXED)

        /**
         * The rows of [shown]: two small cards that follow each other share a row, a small card before a
         * wide or big one (or last) is alone with an empty right half, wide and big are alone. No
         * backfilling of a gap by a later card.
         */
        fun rows(shown: List<HomeCard>): List<List<HomeCard>> {
            val rows = mutableListOf<List<HomeCard>>()
            var pending: HomeCard? = null
            for (card in shown) {
                if (card.size == CardSize.SMALL) {
                    pending = if (pending != null) {
                        rows += listOf(pending, card)
                        null
                    } else {
                        card
                    }
                } else {
                    pending?.let { rows += listOf(it) }
                    pending = null
                    rows += listOf(card)
                }
            }
            pending?.let { rows += listOf(it) }
            return rows
        }
    }
}
