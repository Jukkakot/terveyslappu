package fi.jukkakot.terveyslappu.burn

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.math.roundToLong
import kotlin.reflect.KClass

/**
 * Burned energy read from Health Connect (energy-burn design 2): `READ_TOTAL_CALORIES_BURNED` and
 * `READ_ACTIVE_CALORIES_BURNED` only, never written. Health Connect fills the total aggregate's empty
 * intervals with active kcal plus a BMR derived from BMR records, or from height and weight (defaults
 * 73 kg, 1,7 m), whatever origin filter is used (knowledge "Steps in Health Connect"). So a raw read
 * first finds the days on which an origin of the source wrote a record (and, without a source app,
 * which origins wrote any); only those days of the filtered daily aggregates are kept, never a day
 * made only of an estimate.
 */
class HealthConnectBurn(private val context: Context) : BurnSource {
    private val total = HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class)
    private val active = HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class)

    val permissions: Set<String> = setOf(total, active)

    private val client by lazy { HealthConnectClient.getOrCreate(context) }

    fun grantOf(granted: Set<String>): BurnGrant = BurnGrant.of(total in granted, active in granted)

    override suspend fun granted(): BurnGrant = grantOf(client.permissionController.getGrantedPermissions())

    private fun origins(packages: Collection<String>?): Set<DataOrigin> = packages?.map(::DataOrigin)?.toSet() ?: emptySet()

    /** The time span and origin of one interval record (the library's interval type is internal). */
    private class Span(val start: Instant, val startOffset: ZoneOffset?, val end: Instant, val endOffset: ZoneOffset?, val origin: String)

    private fun span(record: Record): Span = when (record) {
        is TotalCaloriesBurnedRecord -> Span(record.startTime, record.startZoneOffset, record.endTime, record.endZoneOffset, record.metadata.dataOrigin.packageName)
        is ActiveCaloriesBurnedRecord -> Span(record.startTime, record.startZoneOffset, record.endTime, record.endZoneOffset, record.metadata.dataOrigin.packageName)
        else -> error("unexpected record")
    }

    override suspend fun firstDay(packages: Set<String>?): LocalDate? =
        listOfNotNull(first(TotalCaloriesBurnedRecord::class, packages), first(ActiveCaloriesBurnedRecord::class, packages)).minOrNull()

    private suspend fun <T : Record> first(type: KClass<T>, packages: Set<String>?): LocalDate? {
        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = type,
                timeRangeFilter = TimeRangeFilter.before(Instant.now().plusSeconds(86_400)),
                dataOriginFilter = origins(packages),
                ascendingOrder = true,
                pageSize = 1,
            ),
        )
        return response.records.firstOrNull()?.let { startDay(span(it)) }
    }

    private fun startDay(span: Span): LocalDate {
        val offset = span.startOffset ?: ZoneId.systemDefault().rules.getOffset(span.start)
        return span.start.atOffset(offset).toLocalDate()
    }

    private fun endDay(span: Span): LocalDate {
        val end = span.end.minusNanos(1).coerceAtLeast(span.start)
        val offset = span.endOffset ?: ZoneId.systemDefault().rules.getOffset(end)
        return end.atOffset(offset).toLocalDate()
    }

    /** The days with a record of [type] and the origins that wrote them. */
    private suspend fun <T : Record> recordDays(type: KClass<T>, from: LocalDate, to: LocalDate, packages: Set<String>?): Pair<Set<LocalDate>, Set<String>> {
        val days = HashSet<LocalDate>()
        val seen = HashSet<String>()
        val range = TimeRangeFilter.between(from.atStartOfDay(), to.plusDays(1).atStartOfDay())
        var token: String? = null
        do {
            val response = client.readRecords(ReadRecordsRequest(type, range, origins(packages), pageSize = PAGE_SIZE, pageToken = token))
            for (record in response.records) {
                val span = span(record)
                seen += span.origin
                var day = startDay(span)
                val last = endDay(span)
                while (day <= last && day <= to) {
                    if (day >= from) days += day
                    day = day.plusDays(1)
                }
            }
            token = response.pageToken
        } while (!token.isNullOrEmpty())
        return days to seen
    }

    override suspend fun daily(from: LocalDate, to: LocalDate, packages: Set<String>?): List<BurnRaw> {
        val (totalDays, totalOrigins) = recordDays(TotalCaloriesBurnedRecord::class, from, to, packages)
        val (activeDays, activeOrigins) = recordDays(ActiveCaloriesBurnedRecord::class, from, to, packages)
        if (totalDays.isEmpty() && activeDays.isEmpty()) return emptyList()
        val filter = origins(packages ?: (totalOrigins + activeOrigins))
        val buckets = client.aggregateGroupByPeriod(
            AggregateGroupByPeriodRequest(
                metrics = setOf(TotalCaloriesBurnedRecord.ENERGY_TOTAL, ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(from.atStartOfDay(), to.plusDays(1).atStartOfDay()),
                timeRangeSlicer = Period.ofDays(1),
                dataOriginFilter = filter,
            ),
        )
        return buckets.mapNotNull { bucket ->
            val day = bucket.startTime.toLocalDate()
            val t = bucket.result[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories?.takeIf { day in totalDays }?.roundToLong()
            val a = bucket.result[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories?.takeIf { day in activeDays }?.roundToLong()
            if (t == null && a == null) null else BurnRaw(day, t, a)
        }
    }

    private companion object {
        const val PAGE_SIZE = 5000
    }
}
