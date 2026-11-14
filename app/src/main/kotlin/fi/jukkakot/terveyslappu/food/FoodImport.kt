package fi.jukkakot.terveyslappu.food

import fi.jukkakot.terveyslappu.data.FoodParts
import java.time.Instant

/** What an import did with the file's food parts (data-file "Food parts"). */
data class FoodImportResult(val written: Int, val products: Int, val notWritten: Int)

/** The Health Connect side of the food log, so import and tests need no Android. */
interface FoodRecords {
    suspend fun granted(): Boolean
    suspend fun own(now: Instant): List<FoodItem>
    suspend fun insert(items: List<FoodItem>)
}

/**
 * Writes the file's foods that are not logged yet (dedup by uuid) and adds the products whose
 * name is not an own product. Without the nutrition permission the foods are not written and
 * counted in [FoodImportResult.notWritten].
 */
suspend fun importFoods(parts: FoodParts, records: FoodRecords, products: ProductStore, now: Instant): FoodImportResult {
    val added = products.addMissing(parts.products)
    if (parts.foods.isEmpty()) return FoodImportResult(0, added, 0)
    if (!records.granted()) return FoodImportResult(0, added, parts.foods.size)
    val missing = FoodEdits.import(parts.foods, records.own(now))
    missing.chunked(BATCH).forEach { records.insert(it) }
    return FoodImportResult(missing.size, added, 0)
}

private const val BATCH = 500
