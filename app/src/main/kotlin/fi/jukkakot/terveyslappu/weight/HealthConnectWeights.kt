package fi.jukkakot.terveyslappu.weight

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectFeatures
import fi.jukkakot.terveyslappu.history.ImportedWeight
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Mass
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.UUID

enum class HealthConnectStatus { AVAILABLE, NEEDS_UPDATE, UNAVAILABLE }

enum class HistoryAccess { UNSUPPORTED, NOT_GRANTED, GRANTED }

/** Health Connect permission to read records older than 30 days before the first grant. */
const val HISTORY_PERMISSION = HealthPermission.PERMISSION_READ_HEALTH_DATA_HISTORY
private const val PAGE_SIZE = 1000

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

    /**
     * Writes a scale reading [kg] taken at [time] as measured by the Prego scale. The client id comes
     * from the reading's second, so writing the same reading twice keeps one record (an upsert).
     */
    suspend fun writeScale(kg: Double, time: Instant, zoneOffset: ZoneOffset): String {
        val clientId = scaleClientId(time)
        val record = WeightRecord(
            time = time,
            zoneOffset = zoneOffset,
            weight = Mass.kilograms(kg),
            metadata = Metadata.autoRecorded(
                device = Device(type = Device.TYPE_SCALE, manufacturer = "Prego", model = "P814B"),
                clientRecordId = clientId,
            ),
        )
        client.insertRecords(listOf(record))
        return clientId
    }

    /** Writes one batch of imported history weights (history-import), upserting by [importClientId]. */
    suspend fun writeImported(batch: List<ImportedWeight>, zone: ZoneId) {
        val records = batch.map {
            WeightRecord(
                time = it.time,
                zoneOffset = zone.rules.getOffset(it.time),
                weight = Mass.kilograms(it.kg),
                metadata = Metadata.autoRecorded(device = Device(type = Device.TYPE_SCALE), clientRecordId = importClientId(it.time)),
            )
        }
        client.insertRecords(records)
    }

    /** The latest weight from any app before [time] (up to a year back), or null. */
    suspend fun latestBefore(time: Instant): Pair<Double, Instant>? {
        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = WeightRecord::class,
                timeRangeFilter = TimeRangeFilter.between(time.minus(Duration.ofDays(365)), time.minusMillis(1)),
                ascendingOrder = false,
                pageSize = 1,
            ),
        )
        return response.records.firstOrNull()?.let { it.weight.inKilograms to it.time }
    }

    /** Every weight from every app (as far as the read permissions reach), oldest first, paged. */
    suspend fun all(now: Instant): List<WeightEntry> = readAll(now, origins = emptySet())

    /** Every weight this app wrote (scale, manual, imported), oldest first, paged (weight-chart). */
    suspend fun own(now: Instant): List<WeightEntry> = readAll(now, origins = setOf(DataOrigin(context.packageName)))

    private suspend fun readAll(now: Instant, origins: Set<DataOrigin>): List<WeightEntry> {
        val ownPackage = context.packageName
        val entries = mutableListOf<WeightEntry>()
        var pageToken: String? = null
        do {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = WeightRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(Instant.EPOCH, now.plusSeconds(60)),
                    dataOriginFilter = origins,
                    ascendingOrder = true,
                    pageSize = PAGE_SIZE,
                    pageToken = pageToken,
                ),
            )
            response.records.mapTo(entries) {
                it.toEntry(ownPackage)
            }
            pageToken = response.pageToken
        } while (pageToken != null)
        return entries
    }

    /** Whether reading weights older than 30 days is possible, granted or not (weight-export). */
    suspend fun historyAccess(): HistoryAccess {
        val supported = client.features.getFeatureStatus(HealthConnectFeatures.FEATURE_READ_HEALTH_DATA_HISTORY) ==
            HealthConnectFeatures.FEATURE_STATUS_AVAILABLE
        if (!supported) return HistoryAccess.UNSUPPORTED
        return if (HISTORY_PERMISSION in grantedPermissions()) HistoryAccess.GRANTED else HistoryAccess.NOT_GRANTED
    }

    /** Writes a deleted own weight back (undo), with its original client id and origin. */
    suspend fun restore(entry: WeightEntry) {
        require(entry.own) { "only own records can be restored" }
        client.insertRecords(
            listOf(WeightRecord(time = entry.time, zoneOffset = entry.zoneOffset, weight = Mass.kilograms(entry.kg), metadata = restoreMetadata(entry))),
        )
    }

    private fun WeightRecord.toEntry(ownPackage: String) = weightEntry(
        metadata.id, time, zoneOffset, weight.inKilograms, metadata.dataOrigin.packageName, ownPackage,
        clientRecordId = metadata.clientRecordId,
        fromScale = metadata.device?.type == Device.TYPE_SCALE,
    )

    /** Deletes the scale weight this app wrote for the reading at [time] (weigh again). */
    suspend fun deleteScale(time: Instant) {
        client.deleteRecords(WeightRecord::class, recordIdsList = emptyList(), clientRecordIdsList = listOf(scaleClientId(time)))
    }

    /** Deletes one of this app's own records; others' records are never touched. */
    suspend fun delete(entry: WeightEntry) {
        require(entry.own) { "only own records can be deleted" }
        client.deleteRecords(WeightRecord::class, recordIdsList = listOf(entry.id), clientRecordIdsList = emptyList())
    }
}

/** Client record id of a scale reading: one per second of the stable reading. */
fun scaleClientId(time: Instant): String = "terveyslappu-scale-${time.epochSecond}"

/** Client record id of an imported weight: one per second of its time. */
fun importClientId(time: Instant): String = "terveyslappu-import-${time.epochSecond}"

/** Metadata for writing [entry] back: scale weights stay scale measurements, others manual entries. */
fun restoreMetadata(entry: WeightEntry): Metadata {
    val clientId = entry.clientRecordId ?: "terveyslappu-${UUID.randomUUID()}"
    return if (entry.fromScale) {
        Metadata.autoRecorded(device = Device(type = Device.TYPE_SCALE), clientRecordId = clientId)
    } else {
        Metadata.manualEntry(clientRecordId = clientId)
    }
}
