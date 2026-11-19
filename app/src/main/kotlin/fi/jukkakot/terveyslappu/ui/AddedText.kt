package fi.jukkakot.terveyslappu.ui

import fi.jukkakot.terveyslappu.food.FoodItem
import fi.jukkakot.terveyslappu.food.QUICK_ADD

private const val NBSP = ' '

/**
 * What the add bar names (food-log "Search messages and loading", everyday-feedback design 8):
 * "Rahka 200 g", "Pikalisäys 350 kcal", or the name only without an amount. The amount is written
 * as the rows write it, its number and unit joined by a non-breaking space; a long name may wrap.
 */
fun addedSubject(item: FoodItem): String {
    val amount = item.amount
    val kcal = item.kcal
    val detail = when {
        item.name == QUICK_ADD && kcal != null -> "${formatKcal(kcal)} kcal"
        amount != null -> formatAmount(amount)
        else -> null
    }
    return if (detail == null) item.name else item.name + " " + detail.replace(' ', NBSP)
}
