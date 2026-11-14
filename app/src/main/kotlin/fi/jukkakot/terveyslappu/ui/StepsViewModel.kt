package fi.jukkakot.terveyslappu.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fi.jukkakot.terveyslappu.log.AppLog
import fi.jukkakot.terveyslappu.steps.HealthConnectSteps
import fi.jukkakot.terveyslappu.steps.StepsAccess
import fi.jukkakot.terveyslappu.steps.StepsReader
import fi.jukkakot.terveyslappu.steps.StepsRules
import fi.jukkakot.terveyslappu.steps.StepsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate

/**
 * Steps read from Health Connect (steps design 5): the access state and the days of the widest
 * range shown so far, re-read in full on every resume and whenever the home screen or Askeleet is
 * shown (late watch data; no live updates). Nothing is stored.
 */
class StepsViewModel(app: Application) : AndroidViewModel(app) {
    val steps = HealthConnectSteps(app)

    private val reader = StepsReader(steps, log = { ok, error, fields ->
        if (ok) AppLog.i("steps.read", "ok", *fields.toTypedArray()) else AppLog.e("steps.read", error, "failed", *fields.toTypedArray())
    })

    private val _state = MutableStateFlow(StepsUiState())
    val state: StateFlow<StepsUiState> = _state

    /** Remembered only while the app runs (steps "Denied steps permission"). */
    var permissionAsked = false
        private set

    /** The widest start asked so far; [all] = Kaikki. Nothing asked yet: no read. */
    private var widest: LocalDate? = null
    private var all = false

    private val reads = Mutex()

    /** A screen shows steps from [from] (null: Kaikki); widens the read range and reads. */
    fun show(from: LocalDate?) {
        if (from == null) all = true else widest = widest?.let { minOf(it, from) } ?: from
        refresh()
    }

    /** On every resume: the permission may have changed; reads the range shown so far. */
    fun refresh() {
        viewModelScope.launch {
            val granted = runCatching { steps.granted() }.onFailure { AppLog.e("hc.permission.steps", it, "check") }.getOrNull()
            val access = StepsRules.access(granted, permissionAsked)
            _state.update { it.copy(access = access) }
            if (access == StepsAccess.GRANTED && (all || widest != null)) load()
        }
    }

    fun onPermissionRequested() {
        AppLog.i("hc.permission.steps", "request")
    }

    fun onPermissionResult(granted: Set<String>) {
        permissionAsked = true
        AppLog.i("hc.permission.steps", if (granted.containsAll(steps.permissions)) "granted" else "denied")
        refresh()
    }

    private suspend fun load() = reads.withLock {
        val load = reader.read(if (all) null else widest, LocalDate.now())
        _state.update { it.after(load) }
    }
}
