package fi.jukkakot.terveyslappu.ui

import android.app.Application
import android.content.pm.PackageManager
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.log.AppLog
import fi.jukkakot.terveyslappu.weight.HealthConnectStatus
import fi.jukkakot.terveyslappu.weight.HealthConnectWeights
import fi.jukkakot.terveyslappu.weight.WeightEntry
import fi.jukkakot.terveyslappu.weight.WeightInput
import fi.jukkakot.terveyslappu.weight.isInFuture
import fi.jukkakot.terveyslappu.weight.parseWeight
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

enum class Access { CHECKING, UNAVAILABLE, NEEDS_UPDATE, NEEDS_PERMISSION, DENIED, GRANTED }

/** A list row: the entry and the label of the app that wrote it (null when it is this app). */
data class WeightRow(val entry: WeightEntry, val sourceLabel: String?)

data class MainUiState(
    val access: Access = Access.CHECKING,
    val weightText: String = "",
    /** Chosen date and time; null means "now" at the moment of saving. */
    val pickedTime: LocalDateTime? = null,
    val rows: List<WeightRow>? = null,
    val loadFailed: Boolean = false,
    val saving: Boolean = false,
)

data class UiMessage(@param:StringRes val text: Int, val args: List<Any> = emptyList())

class MainViewModel(app: Application) : AndroidViewModel(app) {

    val weights = HealthConnectWeights(app)

    private val _state = MutableStateFlow(MainUiState())
    val state: StateFlow<MainUiState> = _state

    private val _messages = MutableSharedFlow<UiMessage>(extraBufferCapacity = 4)
    val messages: SharedFlow<UiMessage> = _messages

    private var permissionAsked = false

    /** Checks Health Connect and the permissions; called on every resume (settings may have changed). */
    fun refreshAccess() {
        viewModelScope.launch {
            val status = weights.status()
            val access = when (status) {
                HealthConnectStatus.UNAVAILABLE -> Access.UNAVAILABLE
                HealthConnectStatus.NEEDS_UPDATE -> Access.NEEDS_UPDATE
                HealthConnectStatus.AVAILABLE -> {
                    val granted = runCatching { weights.grantedPermissions() }
                        .onFailure { AppLog.e("hc.permissions", it) }
                        .getOrDefault(emptySet())
                    when {
                        granted.containsAll(weights.permissions) -> Access.GRANTED
                        permissionAsked -> Access.DENIED
                        else -> Access.NEEDS_PERMISSION
                    }
                }
            }
            if (access != _state.value.access) AppLog.i("hc.access", access.name, "status" to status.name)
            _state.update { it.copy(access = access) }
            if (access == Access.GRANTED) load()
        }
    }

    fun onPermissionResult(granted: Set<String>) {
        permissionAsked = true
        AppLog.i("hc.permission.result", "", "granted" to granted.sorted().joinToString(","))
        refreshAccess()
    }

    fun onWeightText(text: String) = _state.update { it.copy(weightText = text) }

    fun onPickedTime(time: LocalDateTime?) = _state.update { it.copy(pickedTime = time) }

    fun save() {
        val current = _state.value
        val input = parseWeight(current.weightText) as? WeightInput.Valid ?: return
        val zone = ZoneId.systemDefault()
        val now = Instant.now()
        val time = current.pickedTime?.atZone(zone)?.toInstant() ?: now
        if (isInFuture(time, now)) return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            runCatching { weights.write(input.kg, time, zone.rules.getOffset(time)) }
                .onSuccess { clientId ->
                    AppLog.i("weight.save", "ok", "kg" to input.kg, "time" to time, "clientId" to clientId)
                    _state.update { it.copy(weightText = "", pickedTime = null, saving = false) }
                    _messages.tryEmit(UiMessage(R.string.weight_saved, listOf(formatKg(input.kg))))
                    load()
                }
                .onFailure { error ->
                    AppLog.e("weight.save", error, "failed", "kg" to input.kg)
                    _state.update { it.copy(saving = false) }
                    _messages.tryEmit(UiMessage(R.string.weight_save_failed))
                }
        }
    }

    fun delete(entry: WeightEntry) {
        viewModelScope.launch {
            runCatching { weights.delete(entry) }
                .onSuccess {
                    AppLog.i("weight.delete", "ok", "id" to entry.id)
                    _messages.tryEmit(UiMessage(R.string.delete_done))
                    load()
                }
                .onFailure {
                    AppLog.e("weight.delete", it, "failed", "id" to entry.id)
                    _messages.tryEmit(UiMessage(R.string.delete_failed))
                }
        }
    }

    private suspend fun load() {
        runCatching { weights.recent(Instant.now()) }
            .onSuccess { entries ->
                AppLog.i(
                    "weight.read", "ok",
                    "count" to entries.size,
                    "sources" to entries.groupingBy { it.sourcePackage }.eachCount(),
                )
                val rows = entries.map { WeightRow(it, if (it.own) null else labelOf(it.sourcePackage)) }
                _state.update { it.copy(rows = rows, loadFailed = false) }
            }
            .onFailure { error ->
                AppLog.e("weight.read", error, "failed")
                _state.update { it.copy(loadFailed = true) }
            }
    }

    private fun labelOf(packageName: String): String {
        val pm = getApplication<Application>().packageManager
        return try {
            pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            packageName
        }
    }
}
