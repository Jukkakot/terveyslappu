package fi.jukkakot.terveyslappu.ui

import fi.jukkakot.terveyslappu.derived.shownDelta
import android.app.Application
import android.os.SystemClock
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.WeightRecord
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fi.jukkakot.terveyslappu.TerveysApp
import fi.jukkakot.terveyslappu.auto.RefPair
import fi.jukkakot.terveyslappu.weight.scaleClientId
import fi.jukkakot.terveyslappu.log.AppLog
import fi.jukkakot.terveyslappu.scale.LastWeighInPrefs
import fi.jukkakot.terveyslappu.scale.ScaleScanner
import fi.jukkakot.terveyslappu.scale.WeighIn
import fi.jukkakot.terveyslappu.scale.WeighInState
import fi.jukkakot.terveyslappu.weight.HealthConnectStatus
import fi.jukkakot.terveyslappu.weight.HealthConnectWeights
import kotlinx.coroutines.flow.StateFlow
import fi.jukkakot.terveyslappu.auto.AutoWeights
import fi.jukkakot.terveyslappu.auto.RecentAuto
import fi.jukkakot.terveyslappu.scale.WeightChange
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

/** Android wiring of [WeighIn]: the real scanner, Health Connect and clocks. */
class WeighInViewModel(app: Application) : AndroidViewModel(app) {

    private val weights = HealthConnectWeights(app)

    private val weighIn = WeighIn(
        source = ScaleScanner(app),
        save = { kg, time ->
            val id = weights.writeScale(kg, time, ZoneId.systemDefault().rules.getOffset(time))
            val terveys = app as TerveysApp
            runCatching { terveys.reference.accepted(listOf(RefPair(id, time.toEpochMilli(), kg))) }.onFailure { AppLog.e("auto.store", it, "reference") }
            runCatching { terveys.listen.weightSaved(id, time.toEpochMilli()) }.onFailure { AppLog.e("listen.done", it, "saved") }
            terveys.weightsSignal.emit()
            terveys.backup.request("weighin")
        },
        previous = { before -> weights.latestBefore(before) },
        delete = { _, time ->
            weights.deleteScale(time)
            val terveys = app as TerveysApp
            runCatching { terveys.reference.removed(scaleClientId(time)) }.onFailure { AppLog.e("auto.store", it, "reference") }
            runCatching { terveys.listen.weightRemoved(scaleClientId(time)) }.onFailure { AppLog.e("listen.done", it, "removed") }
            terveys.weightsSignal.emit()
            terveys.backup.request("weighin")
        },
        archive = (app as TerveysApp).rawArchive,
        // A new scale re-plans background listening at once (its filters follow the remembered scale).
        last = LastWeighInPrefs(app) { (app as TerveysApp).listen.replan("scale") },
        // The claim shared with the background save (one weighing, one save).
        keys = (app as TerveysApp).keys,
        elapsedMs = SystemClock::elapsedRealtime,
        now = Instant::now,
    )

    val state: StateFlow<WeighInState> = weighIn.state

    /** Seconds left before the weigh-in gives up (shown in the dialog). */
    val secondsLeft: StateFlow<Int?> = weighIn.secondsLeft

    /** Save and Poista outlive the dialog (rotation, close), so they run in the app-wide scope. */
    private val appScope = (app as TerveysApp).appScope

    /** Access was checked and the scan started once; kept across a recreated dialog. */
    var prepared = false

    /** A stable weight is decided: a recreated dialog must not prepare or scan again. */
    val decided: Boolean get() = weighIn.decidedWeight

    fun discard() = weighIn.discard(appScope)

    /** The recent automatic weight was checked once; Punnitse uudelleen and later prepares skip it. */
    var recentChecked = false

    /** The dialog showed the recent automatic weight (`weighin.end reason=recent|recentdeleted`). */
    var fromRecent = false

    /** When this weigh-in started (the dialog opened or Punnitse uudelleen), for `weighin.end ms=`. */
    var openedMs = SystemClock.elapsedRealtime()

    /** True once `weighin.end` was logged for this weigh-in. */
    var endLogged = false

    /**
     * Shows the recent automatic weight instead of scanning when there is one (everyday-feedback
     * design 4): checked once, capped at [RecentAuto.READ_CAP_MS], skipped without read access.
     * Returns true when shown.
     */
    suspend fun showRecentIfAny(): Boolean {
        if (recentChecked) return false
        recentChecked = true
        val started = SystemClock.elapsedRealtime()
        val read = HealthPermission.getReadPermission(WeightRecord::class)
        val canRead = runCatching { read in weights.grantedPermissions() }.getOrDefault(false)
        if (!canRead) {
            AppLog.i("weighin.recent", "none", "reason" to "noread")
            return false
        }
        val terveys = getApplication<TerveysApp>()
        val now = Instant.now()
        val found = try {
            withTimeoutOrNull(RecentAuto.READ_CAP_MS) {
                val entries = weights.recent(now.minus(RecentAuto.WINDOW), now)
                val marked = runCatching { terveys.marks.ids() }.getOrDefault(emptySet())
                val isAuto = { second: Long -> terveys.rawArchive.get(second)?.fields?.get(AutoWeights.VIA) == AutoWeights.AUTO }
                val entry = RecentAuto.pick(entries, now, isAuto, marked) ?: return@withTimeoutOrNull Result.success(null)
                val change = runCatching { weights.latestBefore(entry.time) }.getOrNull()
                    ?.let { (kg, time) -> WeightChange(shownDelta(kg, entry.kg), time) }
                Result.success(WeighInState.Recent(entry.kg, entry.time, entry.clientRecordId!!, change))
            } ?: Result.failure(java.util.concurrent.TimeoutException("recent"))
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
        val ms = SystemClock.elapsedRealtime() - started
        val recent = found.getOrElse {
            AppLog.w("weighin.recent", "failed", "cause" to (it::class.simpleName ?: "error"), "ms" to ms)
            return false
        }
        if (recent == null) {
            AppLog.i("weighin.recent", "none", "ms" to ms)
            return false
        }
        AppLog.i("weighin.recent", "ok", "ageS" to Duration.between(recent.time, now).seconds, "ms" to ms)
        fromRecent = true
        weighIn.showRecent(recent)
        return true
    }

    /** Poista on the recent automatic weight: the notification's delete path, from the dialog. */
    fun discardRecent() = weighIn.discardRecent(appScope) { recent ->
        getApplication<TerveysApp>().auto.delete(recent.id, recent.time.toEpochMilli(), recent.kg, questioned = false, from = AutoWeights.FROM_DIALOG)
    }

    /** The latest scan's counts for `weighin.end`. */
    val heard: Boolean get() = weighIn.heard
    val ignored: Int get() = weighIn.ignored

    /** True when Health Connect is available and this app may write weight. */
    suspend fun hasHealthAccess(): Boolean {
        if (weights.status() != HealthConnectStatus.AVAILABLE) return false
        val write = HealthPermission.getWritePermission(WeightRecord::class)
        return runCatching { write in weights.grantedPermissions() }
            .onFailure { AppLog.e("weighin.hc", it) }
            .getOrDefault(false)
    }

    fun show(state: WeighInState) = weighIn.show(state)

    fun start() = weighIn.start(viewModelScope, appScope)

    fun retrySave() = weighIn.retrySave(appScope)

    fun stop() = weighIn.stop()

    override fun onCleared() {
        weighIn.stop()
    }
}
