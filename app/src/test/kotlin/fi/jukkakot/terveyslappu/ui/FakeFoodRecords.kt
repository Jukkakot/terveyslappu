package fi.jukkakot.terveyslappu.ui

import fi.jukkakot.terveyslappu.food.FoodItem
import fi.jukkakot.terveyslappu.food.FoodRecordId
import fi.jukkakot.terveyslappu.food.FoodRecords
import kotlinx.coroutines.CompletableDeferred
import java.time.Instant

/** An in-memory Health Connect for the food log: upsert by client id, as Health Connect does. Made-up foods only. */
class FakeFoodRecords : FoodRecords {
    val stored = linkedMapOf<String, FoodItem>()
    var permitted = true
    var failInsert = false

    /** Reads fail while this is above zero (each failed read counts it down). */
    var failReads = 0

    /** When set, the next read waits for it. */
    var readGate: CompletableDeferred<Unit>? = null

    val items: List<FoodItem> get() = stored.values.toList()

    override suspend fun granted() = permitted

    override suspend fun own(now: Instant): List<FoodItem> {
        readGate?.let {
            readGate = null
            it.await()
        }
        if (failReads > 0) {
            failReads--
            error("read refused")
        }
        return items
    }

    override suspend fun insert(items: List<FoodItem>) {
        if (failInsert) error("write refused")
        items.forEach { val id = FoodRecordId.encode(it); stored[id] = it.copy(recordId = "rec:$id") }
    }

    override suspend fun delete(recordIds: List<String>) {
        stored.values.removeAll { it.recordId in recordIds }
    }
}
