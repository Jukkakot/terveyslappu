package fi.jukkakot.terveyslappu.food

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.MealType
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.ZoneId
import androidx.health.connect.client.units.Energy as HcEnergy

/** Nutrition records this app writes and reads (food-log "Food in Health Connect", design 5–7). */
class HealthConnectFoods(private val context: Context) : FoodRecords {

    val permissions: Set<String> = setOf(
        HealthPermission.getReadPermission(NutritionRecord::class),
        HealthPermission.getWritePermission(NutritionRecord::class),
    )

    private val client by lazy { HealthConnectClient.getOrCreate(context) }

    override suspend fun granted(): Boolean = client.permissionController.getGrantedPermissions().containsAll(permissions)

    /** Every food item this app wrote, oldest first (as far as the read permissions reach). */
    override suspend fun own(now: Instant): List<FoodItem> {
        val zone = ZoneId.systemDefault()
        val items = mutableListOf<FoodItem>()
        var pageToken: String? = null
        do {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = NutritionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(Instant.EPOCH, now.plusSeconds(86_400)),
                    dataOriginFilter = setOf(DataOrigin(context.packageName)),
                    ascendingOrder = true,
                    pageSize = PAGE_SIZE,
                    pageToken = pageToken,
                ),
            )
            response.records.mapTo(items) { it.toItem(zone) }
            pageToken = response.pageToken
        } while (pageToken != null)
        return items
    }

    /** Writes [items] (new, or the new versions of changed items: same uuid, new client id). */
    override suspend fun insert(items: List<FoodItem>) {
        if (items.isEmpty()) return
        val version = System.currentTimeMillis()
        client.insertRecords(items.map { it.toRecord(version) })
    }

    /**
     * Rewrites changed items (design "Identity is the uuid"): inserts the new versions first, then
     * deletes the old records whose client id changed (an unchanged id was upserted in place).
     */
    suspend fun replace(old: List<FoodItem>, new: List<FoodItem>) {
        insert(new)
        val newIds = new.map { FoodRecordId.encode(it) }.toSet()
        val stale = old.filter { it.recordId != null && (it.pos == null || FoodRecordId.encode(it) !in newIds) }.mapNotNull { it.recordId }
        delete(stale)
    }

    suspend fun delete(recordIds: List<String>) {
        if (recordIds.isEmpty()) return
        client.deleteRecords(NutritionRecord::class, recordIdsList = recordIds, clientRecordIdsList = emptyList())
    }

    private fun FoodItem.toRecord(version: Long) = NutritionRecord(
        startTime = time,
        startZoneOffset = zoneOffset,
        endTime = time.plusSeconds(1),
        endZoneOffset = zoneOffset,
        metadata = Metadata.manualEntry(clientRecordId = FoodRecordId.encode(this), clientRecordVersion = version),
        energy = kcal?.let { HcEnergy.kilocalories(it) },
        name = name,
        mealType = MealType.MEAL_TYPE_UNKNOWN,
    )

    private fun NutritionRecord.toItem(zone: ZoneId): FoodItem {
        val offset = startZoneOffset ?: zone.rules.getOffset(startTime)
        val parts = FoodRecordId.decode(metadata.clientRecordId)
        return FoodItem(
            uuid = parts?.uuid ?: metadata.id,
            day = startTime.atOffset(offset).toLocalDate(),
            pos = parts?.pos,
            name = name ?: "?",
            kcal = energy?.inKilocalories,
            amount = parts?.amount,
            treat = parts?.treat ?: false,
            time = startTime,
            zoneOffset = offset,
            timed = parts?.timed ?: true,
            recordId = metadata.id,
        )
    }

    private companion object {
        const val PAGE_SIZE = 1000
    }
}
