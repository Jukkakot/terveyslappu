package fi.jukkakot.terveyslappu.ui

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
            terveys.weightsSignal.emit()
            terveys.backup.request("weighin")
        },
        previous = { before -> weights.latestBefore(before) },
        delete = { _, time ->
            weights.deleteScale(time)
            val terveys = app as TerveysApp
            runCatching { terveys.reference.removed(scaleClientId(time)) }.onFailure { AppLog.e("auto.store", it, "reference") }
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
