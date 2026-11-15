package fi.jukkakot.terveyslappu.steps

import android.annotation.SuppressLint
import android.content.Context
import android.health.connect.DeviceDataSource
import android.health.connect.HealthConnectException
import android.health.connect.HealthConnectManager
import android.os.Build
import android.os.OutcomeReceiver
import android.os.ext.SdkExtensions
import androidx.annotation.RequiresExtension
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import kotlin.coroutines.resume

/**
 * Steps read from Health Connect (steps design 1, 4, 8): only `READ_STEPS`, never written. Daily
 * totals come from Health Connect's own aggregate, filtered to the source's origin packages
 * (steps-source-and-axes design 2, 5); no filter = Health Connect's merge by app priority.
 */
class HealthConnectSteps(private val context: Context) : StepsSource {

    val permissions: Set<String> = setOf(HealthPermission.getReadPermission(StepsRecord::class))

    private val client by lazy { HealthConnectClient.getOrCreate(context) }

    override suspend fun granted(): Boolean = client.permissionController.getGrantedPermissions().containsAll(permissions)

    private fun origins(packages: Set<String>?): Set<DataOrigin> = packages?.map(::DataOrigin)?.toSet() ?: emptySet()

    override suspend fun firstDay(packages: Set<String>?): LocalDate? {
        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.before(Instant.now().plusSeconds(86_400)),
                dataOriginFilter = origins(packages),
                ascendingOrder = true,
                pageSize = 1,
            ),
        )
        val record = response.records.firstOrNull() ?: return null
        val offset = record.startZoneOffset ?: ZoneId.systemDefault().rules.getOffset(record.startTime)
        return record.startTime.atOffset(offset).toLocalDate()
    }

    /** One-day buckets with `LocalDateTime` bounds (an `Instant` range throws for a `Period` slicer). */
    override suspend fun daily(from: LocalDate, to: LocalDate, packages: Set<String>?): List<StepsDay> {
        val buckets = client.aggregateGroupByPeriod(
            AggregateGroupByPeriodRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(from.atStartOfDay(), to.plusDays(1).atStartOfDay()),
                timeRangeSlicer = Period.ofDays(1),
                dataOriginFilter = origins(packages),
            ),
        )
        return buckets.mapNotNull { bucket ->
            bucket.result[StepsRecord.COUNT_TOTAL]?.takeIf { it > 0 }?.let { StepsDay(bucket.startTime.toLocalDate(), it) }
        }
    }

    /**
     * The source check (steps-source-and-axes design 2): origins from at most [MAX_PAGES] newest-first
     * record pages and the unfiltered aggregate, grouped with the [candidates] and the manifest's step
     * apps, then one filtered 30-day read per app. Any exception fails the whole check.
     */
    override suspend fun check(today: LocalDate, candidates: List<StepsApp>): StepsCheck {
        val from = today.minusDays(29)
        val range = TimeRangeFilter.between(from.atStartOfDay(), today.plusDays(1).atStartOfDay())
        val seen = mutableSetOf<String>()
        var pages = 0
        var records = 0
        var token: String? = null
        do {
            val response = client.readRecords(
                ReadRecordsRequest(StepsRecord::class, range, ascendingOrder = false, pageSize = PAGE_SIZE, pageToken = token),
            )
            pages++
            records += response.records.size
            response.records.forEach { seen += it.metadata.dataOrigin.packageName }
            token = response.pageToken
        } while (!token.isNullOrEmpty() && pages < MAX_PAGES)
        client.aggregate(AggregateRequest(setOf(StepsRecord.COUNT_TOTAL), range)).dataOrigins.forEach { seen += it.packageName }

        val phone = candidates.firstOrNull { it.id == StepsSourceRules.PHONE }?.packages.orEmpty()
        val spn = phone.firstOrNull { it != StepsSourceRules.ANDROID }
        val apps = linkedMapOf<String, Set<String>>()
        fun add(app: StepsApp) {
            apps[app.id] = apps[app.id].orEmpty() + app.packages
        }
        candidates.forEach(::add)
        StepsSourceRules.KNOWN_APPS.forEach { add(StepsApp(it, setOf(it))) }
        StepsSourceRules.group(seen, spn).forEach(::add)
        val totals = apps.map { (id, packages) ->
            StepsSourceRules.totals(StepsApp(id, packages), daily(from, today, packages), today)
        }
        return StepsCheck(totals, records, pages)
    }

    /** `android` and the phone's synthetic package name when the system can tell it (design 1). */
    override suspend fun phonePackages(): Set<String> {
        val spn = if (SdkExtensions.getExtensionVersion(Build.VERSION_CODES.UPSIDE_DOWN_CAKE) >= 22) {
            runCatching { withTimeoutOrNull(5_000) { currentDeviceOrigin() } }.getOrNull()
        } else {
            null
        }
        return setOfNotNull(StepsSourceRules.ANDROID, spn?.takeIf { it.isNotBlank() })
    }

    @SuppressLint("MissingPermission")
    @RequiresExtension(extension = Build.VERSION_CODES.UPSIDE_DOWN_CAKE, version = 22)
    private suspend fun currentDeviceOrigin(): String? = suspendCancellableCoroutine { cont ->
        val manager = context.getSystemService(HealthConnectManager::class.java)
        if (manager == null) {
            cont.resume(null)
            return@suspendCancellableCoroutine
        }
        manager.getCurrentDeviceDataSource(
            context.mainExecutor,
            object : OutcomeReceiver<DeviceDataSource, HealthConnectException> {
                override fun onResult(result: DeviceDataSource) {
                    if (cont.isActive) cont.resume(result.deviceDataOrigin.packageName)
                }

                override fun onError(error: HealthConnectException) {
                    if (cont.isActive) cont.resume(null)
                }
            },
        )
    }

    private companion object {
        const val PAGE_SIZE = 1000
        const val MAX_PAGES = 5
    }
}
