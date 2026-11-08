package fi.jukkakot.terveyslappu.weight

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Mass
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

enum class HealthConnectStatus { AVAILABLE, NEEDS_UPDATE, UNAVAILABLE }

/** Weight records in Health Connect: the only place this app stores weights (NFR-2). */
class HealthConnectWeights(private val context: Context) {

    val permissions: Set<String> = setOf(
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getWritePermission(WeightRecord::class),
    )

    private val client by lazy { HealthConnectClient.getOrCreate(context) }

    fun status(): HealthConnectStatus = when (HealthConnectClient.getSdkStatus(context)) {
        HealthConnectClient.SDK_AVAILABLE -> HealthConnectStatus.AVAILABLE
        HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> HealthConnectStatus.NEEDS_UPDATE
        else -> HealthConnectStatus.UNAVAILABLE
    }

    suspend fun grantedPermissions(): Set<String> = client.permissionController.getGrantedPermissions()

    /** Writes [kg] at [time] as a manual entry; returns the client record id. */
    suspend fun write(kg: Double, time: Instant, zoneOffset: ZoneOffset): String {
        val clientId = "terveyslappu-${UUID.randomUUID()}"
        val record = WeightRecord(
            time = time,
            zoneOffset = zoneOffset,
            weight = Mass.kilograms(kg),
            metadata = Metadata.manualEntry(clientRecordId = clientId),
        )
        client.insertRecords(listOf(record))
        return clientId
    }

    /** Weights from every app for the [days] before [now], newest first. */
    suspend fun recent(now: Instant, days: Long = 30): List<WeightEntry> {
        val request = ReadRecordsRequest(
            recordType = WeightRecord::class,
            timeRangeFilter = TimeRangeFilter.between(now.minus(Duration.ofDays(days)), now.plusSeconds(60)),
            ascendingOrder = false,
        )
        val ownPackage = context.packageName
        return client.readRecords(request).records.map {
            weightEntry(it.metadata.id, it.time, it.zoneOffset, it.weight.inKilograms, it.metadata.dataOrigin.packageName, ownPackage)
        }.newestFirst()
    }

    /** Deletes one of this app's own records; others' records are never touched. */
    suspend fun delete(entry: WeightEntry) {
        require(entry.own) { "only own records can be deleted" }
        client.deleteRecords(WeightRecord::class, recordIdsList = listOf(entry.id), clientRecordIdsList = emptyList())
    }
}
