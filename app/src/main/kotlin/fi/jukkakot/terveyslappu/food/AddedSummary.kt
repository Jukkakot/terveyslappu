package fi.jukkakot.terveyslappu.food

import kotlin.math.roundToInt

/**
 * What the add search shows of its own adds (food-log "Adding from the search", everyday-feedback
 * design 7): per-ruoka counts for the rows' ✓ / ×N marks and the added strip's numbers. Pure.
 *
 * [count] is every annos counted as added in this search (the session's `addedIds`, also those not
 * yet in [FoodUiState.items] while the day is read); [kcal] the known kcal and [withoutKcal] the
 * count without kcal among those already in the day list; [withKcal] how many have kcal.
 */
data class AddedSummary(
    private val counts: Map<String, Int>,
    val count: Int,
    val kcal: Int,
    val withKcal: Int,
    val withoutKcal: Int,
) {
    /**
     * The mark count of a row: annokset logged as this ruoka (the resolved name, compared by
     * [FoodNames.key]). A Valmiit ruuat row also counts its aliases, so the row of an alias that
     * became an own ruoka under the typed name is marked too.
     */
    fun of(s: Suggestion): Int = when (s) {
        is Suggestion.Own -> counts[FoodNames.key(s.product.name)] ?: 0
        is Suggestion.Base -> (listOf(s.base.name) + s.base.aliases).map(FoodNames::key).distinct().sumOf { counts[it] ?: 0 }
    }

    companion object {
        val NONE = AddedSummary(emptyMap(), 0, 0, 0, 0)

        fun of(addedIds: Set<String>, items: List<FoodItem>?): AddedSummary {
            if (addedIds.isEmpty()) return NONE
            val added = items.orEmpty().filter { it.uuid in addedIds }
            return AddedSummary(
                counts = added.groupingBy { it.key }.eachCount(),
                count = addedIds.size,
                kcal = added.sumOf { it.kcal ?: 0.0 }.roundToInt(),
                withKcal = added.count { it.kcal != null },
                withoutKcal = added.count { it.kcal == null },
            )
        }
    }
}
