package fi.jukkakot.terveyslappu.ui

import android.app.Application
import android.net.Uri
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import fi.jukkakot.terveyslappu.LastWeight
import fi.jukkakot.terveyslappu.TerveysApp
import fi.jukkakot.terveyslappu.backup.appLabel
import fi.jukkakot.terveyslappu.backup.writeExport
import fi.jukkakot.terveyslappu.history.HistoryParser
import fi.jukkakot.terveyslappu.weight.HistoryAccess
import fi.jukkakot.terveyslappu.history.HistoryWriter
import fi.jukkakot.terveyslappu.history.ParsedHistory
import fi.jukkakot.terveyslappu.history.WriteResult
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
    val import: ImportState? = null,
)

/** A snackbar action, e.g. Kumoa. */
class UiAction(@param:StringRes val label: Int, val run: () -> Unit)

data class UiMessage(@param:StringRes val text: Int, val args: List<Any> = emptyList(), val action: UiAction? = null)

class MainViewModel(app: Application) : AndroidViewModel(app) {

    val weights = HealthConnectWeights(app)

    /** The app-wide backup (weight-backup); its status is shown on the main screen. */
    val backup = (app as TerveysApp).backup

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
            if (access == Access.GRANTED) {
                load()
                backup.requestIfDue()
            }
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
            val started = System.nanoTime()
            runCatching { weights.write(input.kg, time, zone.rules.getOffset(time)) }
                .onSuccess { clientId ->
                    AppLog.i("weight.save", "ok", "kg" to input.kg, "time" to time, "clientId" to clientId, "ms" to msSince(started))
                    _state.update { it.copy(weightText = "", pickedTime = null, saving = false) }
                    _messages.tryEmit(UiMessage(R.string.weight_saved, listOf(formatKg(input.kg))))
                    load()
                    backup.request("save")
                }
                .onFailure { error ->
                    AppLog.e("weight.save", error, "failed", "kg" to input.kg, "ms" to msSince(started))
                    _state.update { it.copy(saving = false) }
                    _messages.tryEmit(UiMessage(R.string.weight_save_failed))
                }
        }
    }

    /** Undo of a delete: writes the weight back (delete-undo). */
    fun restore(entry: WeightEntry) {
        viewModelScope.launch {
            val started = System.nanoTime()
            runCatching { weights.restore(entry) }
                .onSuccess {
                    AppLog.i("weight.restore", "ok", "kg" to entry.kg, "time" to entry.time, "ms" to msSince(started))
                    load()
                    backup.request("restore")
                }
                .onFailure {
                    AppLog.e("weight.restore", it, "failed", "time" to entry.time)
                    _messages.tryEmit(UiMessage(R.string.restore_failed))
                }
        }
    }

    fun delete(entry: WeightEntry) {
        viewModelScope.launch {
            val started = System.nanoTime()
            runCatching { weights.delete(entry) }
                .onSuccess {
                    AppLog.i("weight.delete", "ok", "id" to entry.id, "ms" to msSince(started))
                    _messages.tryEmit(UiMessage(R.string.delete_done, action = UiAction(R.string.undo) { restore(entry) }))
                    load()
                    backup.request("delete")
                }
                .onFailure {
                    AppLog.e("weight.delete", it, "failed", "id" to entry.id)
                    _messages.tryEmit(UiMessage(R.string.delete_failed))
                }
        }
    }

    private val historyWriter = HistoryWriter({ batch -> weights.writeImported(batch, ZoneId.systemDefault()) })

    /** Reads and parses the picked history file, then shows the preview (history-import). */
    fun onImportFile(uri: Uri) {
        _state.update { it.copy(import = ImportState.Reading) }
        viewModelScope.launch {
            val started = System.nanoTime()
            val text = withContext(Dispatchers.IO) {
                runCatching {
                    getApplication<Application>().contentResolver.openInputStream(uri)?.use { input ->
                        val bytes = input.readNBytes(MAX_IMPORT_BYTES + 1)
                        require(bytes.size <= MAX_IMPORT_BYTES) { "file larger than $MAX_IMPORT_BYTES bytes" }
                        HistoryParser.decode(bytes)
                    } ?: error("no input stream")
                }
            }
            text.onFailure { error ->
                AppLog.e("import.read", error, "failed")
                _state.update { it.copy(import = ImportState.ReadFailed) }
            }.onSuccess { content ->
                val parsed = withContext(Dispatchers.Default) { HistoryParser.parse(content, ZoneId.systemDefault(), Instant.now()) }
                logParsed(parsed, content.length, msSince(started))
                _state.update { it.copy(import = ImportState.Preview(parsed)) }
            }
        }
    }

    fun confirmImport() {
        val parsed = when (val current = _state.value.import) {
            is ImportState.Preview -> current.parsed
            is ImportState.Failed -> current.parsed
            else -> return
        }
        val from = (_state.value.import as? ImportState.Failed)?.saved ?: 0
        writeImport(parsed, from)
    }

    fun dismissImport() {
        if (_state.value.import is ImportState.Writing) return
        _state.update { it.copy(import = null) }
    }

    private fun writeImport(parsed: ParsedHistory, from: Int) {
        _state.update { it.copy(import = ImportState.Writing(parsed, from)) }
        viewModelScope.launch {
            val started = System.nanoTime()
            val result = historyWriter.write(parsed.weights, from) { saved ->
                _state.update { it.copy(import = ImportState.Writing(parsed, saved)) }
            }
            when (result) {
                is WriteResult.Done -> {
                    AppLog.i("import.write", "ok", "count" to result.count, "from" to from, "ms" to msSince(started))
                    _state.update { it.copy(import = null) }
                    _messages.tryEmit(UiMessage(R.string.import_done, listOf(result.count)))
                    load()
                    backup.request("import")
                }
                is WriteResult.Failed -> {
                    AppLog.e("import.write", result.error, "failed", "saved" to result.saved, "total" to parsed.weights.size, "ms" to msSince(started))
                    _state.update { it.copy(import = ImportState.Failed(parsed, result.saved)) }
                }
            }
        }
    }

    /** Whether the export can and may read the whole history; asked once per screen session. */
    var historyAsked = false

    suspend fun historyAccess(): HistoryAccess = runCatching { weights.historyAccess() }
        .onFailure { AppLog.e("export.history", it) }
        .getOrDefault(HistoryAccess.UNSUPPORTED)

    /** Writes every readable weight to the picked file (weight-export). */
    fun export(uri: Uri) {
        viewModelScope.launch {
            val started = System.nanoTime()
            val history = historyAccess()
            val result = runCatching { writeExport(getApplication(), weights, uri) }
            result.onSuccess { count ->
                AppLog.i("export.write", "ok", "count" to count, "history" to history.name, "ms" to msSince(started))
                val text = if (history == HistoryAccess.GRANTED) R.string.export_done else R.string.export_done_recent
                _messages.tryEmit(UiMessage(text, listOf(count)))
            }.onFailure { error ->
                AppLog.e("export.write", error, "failed", "history" to history.name, "ms" to msSince(started))
                _messages.tryEmit(UiMessage(R.string.export_failed))
            }
        }
    }

    private suspend fun load() {
        val started = System.nanoTime()
        runCatching { weights.recent(Instant.now()) }
            .onSuccess { entries ->
                AppLog.i(
                    "weight.read", "ok",
                    "ms" to msSince(started),
                    "count" to entries.size,
                    "sources" to entries.groupingBy { it.sourcePackage }.eachCount(),
                )
                val rows = entries.map { WeightRow(it, if (it.own) null else labelOf(it.sourcePackage)) }
                _state.update { it.copy(rows = rows, loadFailed = false) }
                LastWeight.replace(getApplication(), entries.firstOrNull()?.let { it.kg to it.time })
            }
            .onFailure { error ->
                AppLog.e("weight.read", error, "failed", "ms" to msSince(started))
                _state.update { it.copy(loadFailed = true) }
            }
    }

    private companion object {
        /** History files are small text tables; 10 MB is far above years of daily weights. */
        const val MAX_IMPORT_BYTES = 10 * 1024 * 1024
    }

    private fun msSince(startNanos: Long): Long = (System.nanoTime() - startNanos) / 1_000_000

    private fun labelOf(packageName: String): String = getApplication<Application>().appLabel(packageName)
}
