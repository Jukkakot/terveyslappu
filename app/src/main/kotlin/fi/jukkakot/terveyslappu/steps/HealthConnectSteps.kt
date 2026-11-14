package fi.jukkakot.terveyslappu.steps

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId

/**
 * Steps read from Health Connect (steps design 1, 4, 8): only `READ_STEPS`, never written. Daily
 * totals come from Health Connect's own aggregate with no origin filter, so writers are merged by
 * the user's app priority instead of summed.
 */
class HealthConnectSteps(private val context: Context) : StepsSource {

    val permissions: Set<String> = setOf(HealthPermission.getReadPermission(StepsRecord::class))

    private val client by lazy { HealthConnectClient.getOrCreate(context) }

    override suspend fun granted(): Boolean = client.permissionController.getGrantedPermissions().containsAll(permissions)

    override suspend fun firstDay(): LocalDate? {
        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.before(Instant.now().plusSeconds(86_400)),
                ascendingOrder = true,
                pageSize = 1,
            ),
        )
        val record = response.records.firstOrNull() ?: return null
        val offset = record.startZoneOffset ?: ZoneId.systemDefault().rules.getOffset(record.startTime)
        return record.startTime.atOffset(offset).toLocalDate()
    }

    /** One-day buckets with `LocalDateTime` bounds (an `Instant` range throws for a `Period` slicer). */
    override suspend fun daily(from: LocalDate, to: LocalDate): List<StepsDay> {
        val buckets = client.aggregateGroupByPeriod(
            AggregateGroupByPeriodRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(from.atStartOfDay(), to.plusDays(1).atStartOfDay()),
                timeRangeSlicer = Period.ofDays(1),
            ),
        )
        return buckets.mapNotNull { bucket ->
            bucket.result[StepsRecord.COUNT_TOTAL]?.takeIf { it > 0 }?.let { StepsDay(bucket.startTime.toLocalDate(), it) }
        }
    }
}
