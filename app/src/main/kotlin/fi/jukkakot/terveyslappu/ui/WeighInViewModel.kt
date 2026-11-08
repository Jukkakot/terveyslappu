package fi.jukkakot.terveyslappu.ui

import android.app.Application
import android.os.SystemClock
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.WeightRecord
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fi.jukkakot.terveyslappu.log.AppLog
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
        save = { kg, time -> weights.writeScale(kg, time, ZoneId.systemDefault().rules.getOffset(time)) },
        elapsedMs = SystemClock::elapsedRealtime,
        now = Instant::now,
    )

    val state: StateFlow<WeighInState> = weighIn.state

    /** True when Health Connect is available and this app may write weight. */
    suspend fun hasHealthAccess(): Boolean {
        if (weights.status() != HealthConnectStatus.AVAILABLE) return false
        val write = HealthPermission.getWritePermission(WeightRecord::class)
        return runCatching { write in weights.grantedPermissions() }
            .onFailure { AppLog.e("weighin.hc", it) }
            .getOrDefault(false)
    }

    fun show(state: WeighInState) = weighIn.show(state)

    fun start() = weighIn.start(viewModelScope)

    fun retrySave() = weighIn.retrySave(viewModelScope)

    fun stop() = weighIn.stop()

    override fun onCleared() {
        weighIn.stop()
    }
}
