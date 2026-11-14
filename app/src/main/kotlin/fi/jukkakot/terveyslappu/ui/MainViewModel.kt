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
import fi.jukkakot.terveyslappu.TerveysApp
import fi.jukkakot.terveyslappu.chart.HomeSummary
import fi.jukkakot.terveyslappu.chart.Period
import fi.jukkakot.terveyslappu.chart.PeriodPrefs
import fi.jukkakot.terveyslappu.chart.encode
import fi.jukkakot.terveyslappu.weight.newestFirst
import fi.jukkakot.terveyslappu.backup.writeExport
import fi.jukkakot.terveyslappu.data.DataFormat
import fi.jukkakot.terveyslappu.data.JsonData
import fi.jukkakot.terveyslappu.history.HistoryParser
import fi.jukkakot.terveyslappu.history.OkokLayout
import fi.jukkakot.terveyslappu.history.SheetText
import fi.jukkakot.terveyslappu.history.XlsTable
import fi.jukkakot.terveyslappu.weight.HistoryAccess
import fi.jukkakot.terveyslappu.history.HistoryWriter
import fi.jukkakot.terveyslappu.history.ParsedHistory
import fi.jukkakot.terveyslappu.history.WriteResult
import fi.jukkakot.terveyslappu.raw.RawEntry
import fi.jukkakot.terveyslappu.auto.Mark
import fi.jukkakot.terveyslappu.auto.RefPair
import fi.jukkakot.terveyslappu.weight.importClientId
import fi.jukkakot.terveyslappu.profile.OwnerProfile
import fi.jukkakot.terveyslappu.weight.HEIGHT_WRITE_PERMISSION
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

enum class Access { CHECKING, UNAVAILABLE, NEEDS_UPDATE, NEEDS_PERMISSION, DENIED, GRANTED }

/** The main activity's three screens (home-screen); back from Historia or Asetukset returns Home. */
enum class Screen { HOME, HISTORY, SETTINGS, HELP }

data class MainUiState(
    val access: Access = Access.CHECKING,
    val screen: Screen = Screen.HOME,
    /** The manual entry dialog (Lisää paino käsin) is open. */
    val entryOpen: Boolean = false,
    /** The last manual save failed; the dialog says so until the weight is edited. */
    val saveFailed: Boolean = false,
    val weightText: String = "",
    /** Chosen date and time; null means "now" at the moment of saving. */
    val pickedTime: LocalDateTime? = null,
    /** Every weight this app wrote, newest first; the period filters it for chart, stats and list. */
    val weights: List<WeightEntry>? = null,
    val period: Period = Period.DEFAULT,
    /** The home card's chart period in days (home-screen "Home chart period"). */
    val homeDays: Long = HomeSummary.DEFAULT_DAYS,
    /** Whether weights older than 30 days can be read; null until checked. */
    val history: HistoryAccess? = null,
    val loadFailed: Boolean = false,
    val saving: Boolean = false,
    val import: ImportState? = null,
    /** Asetukset was opened from a derived value's reason: show Omat tiedot. */
    val profileFocus: Boolean = false,
    /** When [weights] were last read; the charts take their "now" from it so a reload moves them on. */
    val loadedAt: Instant? = null,
    /** Ids of read weights marked "?" (only marks that match a read weight). */
    val questioned: Set<String> = emptySet(),
    /** Historia opens with this weight's row shown (the home notice: the newest "?" weight). */
    val historyTarget: String? = null,
) {
    /** The weights not marked "?": everything that charts, counts or compares uses these. */
    val accepted: List<WeightEntry>? get() = weights?.filter { it.id !in questioned }

    /** The newest weight marked "?", if any. */
    val newestQuestioned: WeightEntry? get() = weights?.filter { it.id in questioned }?.maxByOrNull { it.time }

    /** The state after a successful read at [at]. */
    fun loaded(entries: List<WeightEntry>, history: HistoryAccess, at: Instant) =
        copy(weights = entries, history = history, loadFailed = false, loadedAt = at)
}

/** What the activity does once the history permission screen returns; kept here so recreation does not lose it. */
enum class AfterHistory(val format: DataFormat? = null) {
    RELOAD,
    BACKUP_FILE,
    EXPORT_CSV(DataFormat.CSV),
    EXPORT_JSON(DataFormat.JSON),
    ;

    companion object {
        fun export(format: DataFormat): AfterHistory = entries.first { it.format == format }
    }
}

/** A snackbar action, e.g. Kumoa. */
class UiAction(@param:StringRes val label: Int, val run: () -> Unit)

data class UiMessage(@param:StringRes val text: Int, val args: List<Any> = emptyList(), val action: UiAction? = null)

class MainViewModel(app: Application) : AndroidViewModel(app) {

    val weights = HealthConnectWeights(app)

    /** The app-wide backup (weight-backup); its status is shown on the main screen. */
    val backup = (app as TerveysApp).backup

    private val periodPrefs = PeriodPrefs(app)

    /** The owner details (owner-profile). */
    val profile = (app as TerveysApp).profile

    private val terveys = app as TerveysApp

    /** The first read after the app opens writes the pending queue first, so the list includes it (design 3). */
    private var firstLoad = true

    init {
        // Changes made by the background save or a notification action (design 12).
        viewModelScope.launch {
            terveys.weightsSignal.changes.collect { if (_state.value.access == Access.GRANTED) load() }
        }
    }

    private val _state = MutableStateFlow(MainUiState(period = periodPrefs.load(), homeDays = periodPrefs.loadHomeDays()))
    val state: StateFlow<MainUiState> = _state

    fun onPeriod(period: Period) {
        AppLog.i("chart.period", period.encode())
        periodPrefs.save(period)
        _state.update { it.copy(period = period) }
    }

    fun onHomePeriod(days: Long) {
        val chosen = HomeSummary.homeDays(days)
        AppLog.i("home.period", "", "days" to chosen)
        periodPrefs.saveHomeDays(chosen)
        _state.update { it.copy(homeDays = chosen) }
    }

    /** The history permission screen returned (chart notice): read again with what is now allowed. */
    fun onHistoryResult() {
        viewModelScope.launch { load() }
    }

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
                backup.requestIfDue(changedWhileClosed = runCatching { terveys.autoFlags.takeBackupDue() }.getOrDefault(false))
            }
        }
    }

    fun onPermissionResult(granted: Set<String>) {
        permissionAsked = true
        AppLog.i("hc.permission.result", "", "granted" to granted.sorted().joinToString(","))
        refreshAccess()
    }

    fun onScreen(screen: Screen) = _state.update { it.copy(screen = screen, profileFocus = false, historyTarget = null) }

    /** The home notice: Historia with the newest "?" weight shown (home-screen). */
    fun openQuestioned() = _state.update { it.copy(screen = Screen.HISTORY, profileFocus = false, historyTarget = it.newestQuestioned?.id) }

    /** Historia has scrolled to its target. */
    fun targetShown() = _state.update { it.copy(historyTarget = null) }

    /** Hyväksy on a "?" row: the mark goes with the chained acceptance (weight-entry). */
    fun accept(entry: WeightEntry) {
        val id = entry.clientRecordId ?: return
        viewModelScope.launch {
            withContext(Dispatchers.IO) { runCatching { terveys.auto.accept(id, "historia") }.onFailure { AppLog.e("auto.confirm", it, "failed") } }
            // The weights-changed signal of the acceptance reloads the list.
            backup.request("confirm")
        }
    }

    /** A derived value's reason: Asetukset at Omat tiedot (derived-values). */
    fun openProfile() = _state.update { it.copy(screen = Screen.SETTINGS, profileFocus = true) }

    fun openEntry() = _state.update { it.copy(entryOpen = true, saveFailed = false) }

    /** Cancel: the dialog closes and forgets the typed weight; ignored while saving. */
    fun closeEntry() = _state.update {
        if (it.saving) it else it.copy(entryOpen = false, weightText = "", pickedTime = null, saveFailed = false)
    }

    fun onWeightText(text: String) = _state.update { it.copy(weightText = text, saveFailed = false) }

    fun onPickedTime(time: LocalDateTime?) = _state.update { it.copy(pickedTime = time) }

    fun save() {
        val current = _state.value
        val input = parseWeight(current.weightText) as? WeightInput.Valid ?: return
        val zone = ZoneId.systemDefault()
        val now = Instant.now()
        val time = current.pickedTime?.atZone(zone)?.toInstant() ?: now
        if (isInFuture(time, now)) return
        _state.update { it.copy(saving = true, saveFailed = false) }
        viewModelScope.launch {
            val started = System.nanoTime()
            runCatching { weights.write(input.kg, time, zone.rules.getOffset(time)) }
                .onSuccess { clientId ->
                    AppLog.i("weight.save", "ok", "kg" to input.kg, "time" to time, "clientId" to clientId, "ms" to msSince(started))
                    keepAuto("reference") { terveys.reference.accepted(listOf(RefPair(clientId, time.toEpochMilli(), input.kg))) }
                    _state.update { it.copy(weightText = "", pickedTime = null, saving = false, entryOpen = false) }
                    _messages.tryEmit(UiMessage(R.string.weight_saved, listOf(formatKg(input.kg))))
                    load()
                    backup.request("save")
                }
                .onFailure { error ->
                    AppLog.e("weight.save", error, "failed", "kg" to input.kg, "ms" to msSince(started))
                    _state.update { it.copy(saving = false, saveFailed = true) }
                }
        }
    }

    private val rawArchive get() = getApplication<TerveysApp>().rawArchive

    /** Undo of a delete: writes the weight back with its raw data and its "?" mark (delete-undo). */
    fun restore(entry: WeightEntry, raw: RawEntry? = null, mark: Mark? = null) {
        viewModelScope.launch {
            val started = System.nanoTime()
            runCatching { weights.restore(entry) }
                .onSuccess {
                    AppLog.i("weight.restore", "ok", "kg" to entry.kg, "time" to entry.time, "ms" to msSince(started))
                    raw?.let { keepRaw("restore") { rawArchive.add(it) } }
                    val id = entry.clientRecordId
                    if (mark != null) {
                        keepAuto("mark") { terveys.marks.add(listOf(mark)) }
                    } else if (id != null) {
                        keepAuto("reference") { terveys.reference.accepted(listOf(RefPair(id, entry.time.toEpochMilli(), entry.kg))) }
                    }
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
                    val raw = keepRaw("delete") { rawArchive.remove(entry.time.epochSecond) }
                    val id = entry.clientRecordId
                    val mark = id?.let { keepAuto("mark") { terveys.marks.remove(listOf(it)).firstOrNull() } }
                    if (id != null) {
                        keepAuto("reference") { terveys.reference.removed(id) }
                        keepAuto("notify") { terveys.autoNotifier.cancel(id) }
                    }
                    _messages.tryEmit(UiMessage(R.string.delete_done, action = UiAction(R.string.undo) { restore(entry, raw, mark) }))
                    load()
                    backup.request("delete")
                }
                .onFailure {
                    AppLog.e("weight.delete", it, "failed", "id" to entry.id)
                    _messages.tryEmit(UiMessage(R.string.delete_failed))
                }
        }
    }

    private val historyWriter = HistoryWriter({ batch ->
        weights.writeImported(batch, ZoneId.systemDefault())
        keepRaw("import") { withContext(Dispatchers.IO) { rawArchive.addAll(batch.mapNotNull { it.raw }) } }
    })

    /** Marks and reference trouble is logged and never fails the weight action itself. */
    private inline fun <T> keepAuto(step: String, block: () -> T): T? = try {
        block()
    } catch (e: Exception) {
        AppLog.e("auto.store", e, step)
        null
    }

    /** Imported weights: `check` `?` gives the mark back, the others join the reference list (data-file "Mark read back"). */
    private fun importMarks(weights: List<fi.jukkakot.terveyslappu.history.ImportedWeight>) {
        val (checked, plain) = weights.partition { it.check }
        keepAuto("mark") { terveys.marks.add(checked.map { Mark(importClientId(it.time), it.time.toEpochMilli(), it.kg) }) }
        keepAuto("reference") { terveys.reference.accepted(plain.map { RefPair(importClientId(it.time), it.time.toEpochMilli(), it.kg) }) }
    }

    /** Archive trouble is logged and never fails the weight action itself. */
    private inline fun <T> keepRaw(action: String, block: () -> T): T? = try {
        block()
    } catch (e: Exception) {
        AppLog.e("raw.archive", e, action)
        null
    }

    /** Reads and parses the picked history file, then shows the preview (history-import). */
    fun onImportFile(uri: Uri) {
        _state.update { it.copy(import = ImportState.Reading) }
        viewModelScope.launch {
            val started = System.nanoTime()
            val file = withContext(Dispatchers.IO) {
                runCatching {
                    getApplication<Application>().contentResolver.openInputStream(uri)?.use { input ->
                        val bytes = input.readNBytes(MAX_IMPORT_BYTES + 1)
                        require(bytes.size <= MAX_IMPORT_BYTES) { "file larger than $MAX_IMPORT_BYTES bytes" }
                        bytes
                    } ?: error("no input stream")
                }
            }
            file.onFailure { error ->
                AppLog.e("import.read", error, "failed")
                _state.update { it.copy(import = ImportState.ReadFailed) }
            }.onSuccess { bytes ->
                val parsed = withContext(Dispatchers.Default) { readHistory(bytes) }
                logParsed(parsed.first, bytes.size, msSince(started), parsed.second, parsed.third)
                _state.update { it.copy(import = ImportState.Preview(parsed.first)) }
            }
        }
    }

    /**
     * Parses a picked file: a compound document as `.xls` (its first sheet, OKOK layout when found),
     * text starting with `{` as the JSON data file, anything else as a CSV data file or history
     * table. A damaged `.xls` gives an empty result, logged with the reason. Also returns the format name for the log.
     */
    private fun readHistory(bytes: ByteArray): Triple<ParsedHistory, SheetText?, String> {
        val zone = ZoneId.systemDefault()
        if (!XlsTable.isXls(bytes)) {
            val text = HistoryParser.decode(bytes)
            return when (DataFormat.detect(text)) {
                DataFormat.JSON -> Triple(JsonData.read(text, Instant.now()), null, "json")
                DataFormat.CSV -> Triple(HistoryParser.parse(text, zone, Instant.now()), null, "text")
            }
        }
        val sheet = try {
            OkokLayout.toText(XlsTable.read(bytes))
        } catch (e: Exception) {
            AppLog.e("import.read", e, "xls unreadable", "format" to "xls")
            return Triple(ParsedHistory(emptyList(), emptyList(), null), null, "xls")
        }
        return Triple(HistoryParser.parse(sheet.text, zone, Instant.now()), sheet, "xls")
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
                    importMarks(parsed.weights)
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

    /** Keeps the edited details; true when a new height should also go to Health Connect. */
    fun setProfile(new: OwnerProfile): Boolean {
        val old = profile.profile.value
        profile.set(new)
        return new.heightCm != null && new.heightCm != old.heightCm
    }

    suspend fun hasHeightPermission(): Boolean =
        runCatching { HEIGHT_WRITE_PERMISSION in weights.grantedPermissions() }.getOrDefault(false)

    /** Writes the kept height to Health Connect now; the field notes a failure. */
    fun writeHeight() {
        val cm = profile.profile.value.heightCm ?: return
        viewModelScope.launch {
            val now = Instant.now()
            runCatching { weights.writeHeight(cm, now, ZoneId.systemDefault().rules.getOffset(now)) }
                .onSuccess {
                    AppLog.i("profile.height.hc", "ok")
                    profile.setHeightInHealthConnect(true)
                }
                .onFailure { error ->
                    AppLog.e("profile.height.hc", error, "failed")
                    profile.setHeightInHealthConnect(false)
                }
        }
    }

    fun heightDenied() {
        AppLog.i("profile.height.hc", "denied")
        profile.setHeightInHealthConnect(false)
    }

    /** Whether the export can and may read the whole history; asked once per screen session. */
    var historyAsked = false

    /** The action waiting for the history permission screen (survives activity recreation). */
    var afterHistory: AfterHistory? = null

    suspend fun historyAccess(): HistoryAccess = runCatching { weights.historyAccess() }
        .onFailure { AppLog.e("export.history", it) }
        .getOrDefault(HistoryAccess.UNSUPPORTED)

    /** Writes the data file in [format] to the picked file (weight-export). */
    fun export(uri: Uri, format: DataFormat) {
        viewModelScope.launch {
            val started = System.nanoTime()
            val history = historyAccess()
            val result = runCatching { requireNotNull(writeExport(getApplication(), weights, uri, format)) }
            result.onSuccess { count ->
                AppLog.i("export.write", "ok", "format" to format.extension, "count" to count, "history" to history.name, "ms" to msSince(started))
                val text = if (history == HistoryAccess.GRANTED) R.string.export_done else R.string.export_done_recent
                _messages.tryEmit(UiMessage(text, listOf(count)))
            }.onFailure { error ->
                AppLog.e("export.write", error, "failed", "history" to history.name, "ms" to msSince(started))
                _messages.tryEmit(UiMessage(R.string.export_failed))
            }
        }
    }

    private suspend fun load() {
        if (firstLoad) {
            firstLoad = false
            withContext(Dispatchers.IO) { runCatching { terveys.auto.flush("app") }.onFailure { AppLog.e("auto.retry", it, "failed") } }
        }
        val started = System.nanoTime()
        val history = historyAccess()
        val at = Instant.now()
        runCatching { weights.own(at).newestFirst() }
            .onSuccess { entries ->
                AppLog.i("weight.read", "ok", "ms" to msSince(started), "count" to entries.size, "history" to history.name)
                val questioned = withContext(Dispatchers.IO) { keepAuto("sync") { syncMarks(entries, history, at) } } ?: emptySet()
                _state.update { it.loaded(entries, history, at).copy(questioned = questioned) }
            }
            .onFailure { error ->
                AppLog.e("weight.read", error, "failed", "ms" to msSince(started))
                _state.update { it.copy(loadFailed = true) }
            }
    }

    /**
     * After a foreground read (design 4, 5): orphan marks are dropped, the reference list is seeded
     * from the read accepted weights. Without the history permission only the last 30 days are read,
     * so older marks are kept and an empty read keeps the old reference pairs. Returns the ids of
     * the read weights marked "?".
     */
    private fun syncMarks(entries: List<WeightEntry>, history: HistoryAccess, at: Instant): Set<String> {
        val own = entries.filter { it.own && it.clientRecordId != null }
        val readIds = own.mapNotNull { it.clientRecordId }.toSet()
        val readSince = at.minus(java.time.Duration.ofDays(30)).toEpochMilli()
        val keep = if (history == HistoryAccess.GRANTED) readIds else readIds + terveys.marks.all().filter { it.timeMs < readSince }.map { it.id }
        val dropped = terveys.marks.retain(keep)
        if (dropped > 0) AppLog.i("marks.orphans", "", "dropped" to dropped)
        val marked = terveys.marks.ids()
        val pairs = own.filter { it.clientRecordId !in marked }.map { RefPair(it.clientRecordId!!, it.time.toEpochMilli(), it.kg) }
        terveys.reference.seed(if (pairs.isEmpty() && history != HistoryAccess.GRANTED) terveys.reference.get().pairs else pairs)
        return own.filter { it.clientRecordId in marked }.map { it.id }.toSet()
    }

    private companion object {
        /** History files are small text tables; 10 MB is far above years of daily weights. */
        const val MAX_IMPORT_BYTES = 10 * 1024 * 1024
    }

    private fun msSince(startNanos: Long): Long = (System.nanoTime() - startNanos) / 1_000_000
}
