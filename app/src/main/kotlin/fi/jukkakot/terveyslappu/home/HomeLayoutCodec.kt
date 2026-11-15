package fi.jukkakot.terveyslappu.home

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * The front page layout as one JSON string (home-card-layout design 7):
 * `{"v":1,"shown":[{"id":"weight","size":"wide"},…],"hidden":[…]}`. Pure.
 *
 * [fixed] are the cards every layout holds (with their default sizes, in default order): a fixed id
 * missing from both lists is appended to `shown`. [allowedExtra] ids (`custom-charts`' `chart:<n>`)
 * are kept when listed and never appended; any other id is dropped. An unreadable value, an unknown
 * `v` or an empty `shown` means the default layout ([fixed], nothing hidden).
 */
class HomeLayoutCodec(
    private val fixed: List<HomeCard> = HomeLayout.FIXED,
    private val allowedExtra: Set<String> = emptySet(),
    /** The size of a listed extra card whose stored size is unknown. */
    private val extraSize: CardSize = CardSize.WIDE,
) {
    @Serializable
    private data class Card(val id: String, val size: String)

    @Serializable
    private data class Stored(val v: Int = 0, val shown: List<Card> = emptyList(), val hidden: List<Card> = emptyList())

    private val json = Json { ignoreUnknownKeys = true }

    private val default get() = HomeLayout(fixed)

    fun encode(layout: HomeLayout): String =
        json.encodeToString(Stored.serializer(), Stored(VERSION, layout.shown.map(::card), layout.hidden.map(::card)))

    fun decode(text: String?): HomeLayout {
        if (text.isNullOrBlank()) return default
        val stored = runCatching { json.decodeFromString(Stored.serializer(), text) }.getOrNull() ?: return default
        if (stored.v != VERSION || stored.shown.isEmpty()) return default
        val seen = mutableSetOf<String>()
        fun read(cards: List<Card>): List<HomeCard> = cards.mapNotNull { card ->
            val fallback = defaultSize(card.id) ?: return@mapNotNull null
            if (!seen.add(card.id)) return@mapNotNull null
            HomeCard(card.id, sizeOf(card.size) ?: fallback)
        }
        val shown = read(stored.shown)
        val hidden = read(stored.hidden)
        val missing = fixed.filter { it.id !in seen }
        val all = shown + missing
        if (all.isEmpty()) return default
        return HomeLayout(all, hidden)
    }

    private fun defaultSize(id: String): CardSize? =
        fixed.firstOrNull { it.id == id }?.size ?: extraSize.takeIf { id in allowedExtra }

    private fun card(card: HomeCard) = Card(card.id, card.size.name.lowercase())

    private fun sizeOf(name: String): CardSize? = CardSize.entries.firstOrNull { it.name.equals(name, ignoreCase = true) }

    companion object {
        const val VERSION = 1
    }
}
