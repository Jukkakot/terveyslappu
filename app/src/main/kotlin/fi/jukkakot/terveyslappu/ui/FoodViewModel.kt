package fi.jukkakot.terveyslappu.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.TerveysApp
import fi.jukkakot.terveyslappu.food.AddInput
import fi.jukkakot.terveyslappu.food.AddSearchExit
import fi.jukkakot.terveyslappu.food.AddSearchTarget
import fi.jukkakot.terveyslappu.food.Suggestions
import fi.jukkakot.terveyslappu.food.AiFailure
import fi.jukkakot.terveyslappu.food.AiItem
import fi.jukkakot.terveyslappu.food.AiParsed
import fi.jukkakot.terveyslappu.food.AiProduct
import fi.jukkakot.terveyslappu.food.AiImport
import fi.jukkakot.terveyslappu.food.AiImportPlan
import fi.jukkakot.terveyslappu.food.AiPaste
import fi.jukkakot.terveyslappu.food.ClipOffer
import fi.jukkakot.terveyslappu.food.ClipOffers
import fi.jukkakot.terveyslappu.food.ClipRead
import fi.jukkakot.terveyslappu.food.ClipStep
import fi.jukkakot.terveyslappu.food.AiResult
import fi.jukkakot.terveyslappu.food.EditInput
import fi.jukkakot.terveyslappu.food.Energy
import fi.jukkakot.terveyslappu.food.Recalc
import fi.jukkakot.terveyslappu.food.FoodDay
import fi.jukkakot.terveyslappu.food.FoodEdits
import fi.jukkakot.terveyslappu.food.FoodItem
import fi.jukkakot.terveyslappu.food.FoodPlan
import fi.jukkakot.terveyslappu.food.FoodRecords
import fi.jukkakot.terveyslappu.food.HealthConnectFoods
import fi.jukkakot.terveyslappu.food.Product
import fi.jukkakot.terveyslappu.food.Suggestion
import fi.jukkakot.terveyslappu.food.isKnownName
import fi.jukkakot.terveyslappu.food.resolveTyped
import fi.jukkakot.terveyslappu.log.AppLog
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

/** Whether the food log may read and write nutrition records (design 8). */
enum class FoodAccess { CHECKING, UNAVAILABLE, NEEDS, DENIED, GRANTED }

/** The add search's current bar (food-add-search design 3): the latest add, an import, a removed ruoka, a failed write ... */
sealed interface SheetLine {
    /** "<name> lisätty · Kumoa". */
    data class Added(val item: FoodItem) : SheetLine

    /** "Tallennus epäonnistui". */
    data object Failed : SheetLine

    /** A plain notice (no action), e.g. the import text had no usable JSON. */
    data class Message(val text: Int) : SheetLine

    /** "<N> annosta lisätty · Kumoa" (or "<N> tuotetta tallennettu · Kumoa") after an import in an open sheet. */
    data class Imported(val plan: AiImportPlan) : SheetLine

    /** "<name> poistettu · Kumoa" after a row's delete button. */
    data class ProductRemoved(val product: Product) : SheetLine

    /** "Leikepöytä muuttui": the card's content left the clipboard before the tap; nothing saved. */
    data object ClipChanged : SheetLine

    /** Ohita on the clipboard card inside the sheet: "Ohitettu · Kumoa". */
    data class ClipSkipped(val offer: ClipOffer) : SheetLine

    /** "Päivitetty <N> kirjausta · Kumoa" after a product's energy correction (product-energy-fix). */
    data class EnergyUpdated(val undo: EnergyUndo) : SheetLine

    /** "Päivitettiin <X>/<N> kirjausta · Yritä uudelleen": writing the recalculated items failed part way. */
    data class EnergyFailed(val done: Int, val of: Int, val retry: EnergyRetry) : SheetLine

    /** "Aiempia kirjauksia ei voitu lukea": the correction was saved from now on only. */
    data object EnergyReadFailed : SheetLine
}

/** The correction dialog (food-log "Confirming a correction"): N items on D days, their kcal sums, M kept. */
data class EnergyAsk(val name: String, val count: Int, val days: Int, val oldSum: Int, val newSum: Int, val kept: Int)

/** The dialog's answer: Päivitä kaikki, Vain jatkossa, Peruuta. */
enum class EnergyChoice { ALL, FUTURE, CANCEL }

/**
 * What Kumoa of a correction restores (design 7): the product's [old] energy if it still holds [new],
 * the edited item as it was ([editedBefore]) or the added item gone ([addedUuid]), and the
 * recalculated items whose kcal is still amount × new.
 */
class EnergyUndo(
    val productKey: String,
    val old: Energy,
    val new: Energy,
    val editedBefore: FoodItem?,
    val addedUuid: String?,
    val recalc: Recalc,
) {
    val count: Int get() = recalc.new.size
}

/** Yritä uudelleen (design 6): the recalculation from the items as they are now, for these product keys. */
class EnergyRetry(val keys: Set<String>, val except: String?, val undo: EnergyUndo)

data class FoodUiState(
    val access: FoodAccess = FoodAccess.CHECKING,
    /** Every own item, null until read. */
    val items: List<FoodItem>? = null,
    val products: List<Product> = emptyList(),
    val loadFailed: Boolean = false,
    /** The Ruoka screen's day. */
    val day: LocalDate = LocalDate.now(),
    /** The add search adds to this day; null = closed. */
    val searchDay: LocalDate? = null,
    val query: String = "",
    /** The sheet's line, until the next add or the sheet closes. */
    val line: SheetLine? = null,
    /** The item editor is open on this item. */
    val editing: FoodItem? = null,
    /** The editor was opened from the add sheet on this day: leaving it reopens the sheet (food-widget design 4). */
    val editReturn: LocalDate? = null,
    /** The editor is a draft of a new item of this suggestion (edit before adding): Lisää / Peruuta. */
    val draft: Suggestion? = null,
    /** Järjestä: the working order of the day. */
    val ordering: List<FoodItem>? = null,
    /** The AI import preview, null = closed. */
    val ai: AiPreview? = null,
    /** The clipboard line (food-ai-import), null = none. */
    val clipOffer: ClipOffer? = null,
    /** The keyboard comes up when the search shows its field: set on open, and on return from an editor only if it was up before. */
    val keyboardBack: Boolean = false,
    /** The uuids counted as added in this search, in order (queued adds minus failed writes, Kumoa and deletes). */
    val addedIds: Set<String> = emptySet(),
    /** The annokset shown highlighted on Ruoka after leaving the search (the items' own client ids). */
    val highlight: Set<String> = emptySet(),
    /** The empty-field ranking, fixed while the search is open; null until the day has been read. */
    val picks: List<Product>? = null,
    /** The correction dialog over the editor, null = none. */
    val energyAsk: EnergyAsk? = null,
    /** Counts Ruokakirjaus product saves that finished after a correction check: the product editor closes on each. */
    val productSaves: Int = 0,
    /** Valmiit ruuat (FoodPrefs): the base products are in use. */
    val baseOn: Boolean = true,
    /** The import preview's plan (the sum and the product change lines), null = no preview. */
    val aiPlan: AiImportPlan? = null,
) {
    /** The latest add (the Muuta · Kumoa line). */
    val lastAdded: FoodItem? get() = (line as? SheetLine.Added)?.item

    fun dayItems(day: LocalDate): List<FoodItem> = FoodDay.ordered(items.orEmpty().filter { it.day == day })
}

/**
 * The food log's state and writes (food-log); Health Connect holds the log, app storage the
 * products. Used by the main screen and the Lisää annos dialog; writes run in the app scope so a
 * closed screen never cancels one (food-widget design 6).
 */
class FoodViewModel(app: Application) : AndroidViewModel(app) {
    private val terveys = app as TerveysApp
    val foods: FoodRecords get() = terveys.foodRecords
    val permissions: Set<String> = HealthConnectFoods.PERMISSIONS
    private val store get() = terveys.products
    /** Empty while Valmiit ruuat is off: then no base product is suggested, resolved or used by an import. */
    val base: List<fi.jukkakot.terveyslappu.food.BaseProduct> get() = if (terveys.foodPrefs.baseProducts.value) terveys.baseProducts else emptyList()

    private val _state = MutableStateFlow(FoodUiState())
    val state: StateFlow<FoodUiState> = _state

    private val _messages = MutableSharedFlow<UiMessage>(extraBufferCapacity = 4)
    val messages: SharedFlow<UiMessage> = _messages

    /** Writes run one at a time (app-wide), so an undo finds the record its add wrote. */
    private val writes get() = terveys.foodWrites

    /** Where adds come from, for the log: app, widget or shortcut. */
    var source: String = "app"
        private set

    /** The dialog's writes tell the app's screens (food-widget design 14). */
    private var notifyApp = false

    /** The dialog opened its sheet once; a recreated dialog keeps its state (design 11). */
    private var dialogStarted = false

    var permissionAsked = false
        private set

    init {
        // An import (MainViewModel) or the Lisää annos dialog wrote foods or products.
        viewModelScope.launch {
            terveys.foodPrefs.baseProducts.collect { on ->
                _state.update { it.copy(baseOn = on) }
                if (_state.value.ai != null) replan()
            }
        }
        viewModelScope.launch {
            terveys.foodSignal.changes.collect {
                if (notifyApp) return@collect
                if (_state.value.access == FoodAccess.GRANTED) load() else refresh()
            }
        }
    }

    /**
     * The Lisää annos activity (food-add-search design 5): adds go to today's search, writes tell the
     * app. A second tap leaves an open search of the same date as it is; a new date starts afresh.
     */
    fun startDialog(from: String) {
        source = from
        notifyApp = true
        val today = LocalDate.now()
        if (dialogStarted && _state.value.searchDay == today) return
        dialogStarted = true
        openSearch(today)
    }

    /** On every resume: the permission may have changed in Health Connect's settings. */
    fun refresh() {
        viewModelScope.launch {
            val granted = runCatching { foods.granted() }.onFailure { AppLog.e("hc.permission.nutrition", it, "check") }.getOrNull()
            val access = when {
                granted == null -> FoodAccess.UNAVAILABLE
                granted -> FoodAccess.GRANTED
                permissionAsked -> FoodAccess.DENIED
                else -> FoodAccess.NEEDS
            }
            _state.update { it.copy(access = access, products = store.all(), day = it.day) }
            if (access == FoodAccess.GRANTED) load()
        }
    }

    fun onPermissionRequested() {
        AppLog.i("hc.permission.nutrition", "request")
    }

    fun onPermissionResult(granted: Set<String>) {
        permissionAsked = true
        val ok = granted.containsAll(permissions)
        AppLog.i("hc.permission.nutrition", if (ok) "granted" else "denied")
        refresh()
    }

    suspend fun load() {
        val started = System.nanoTime()
        runCatching { foods.own(Instant.now()) }
            .onSuccess { items ->
                AppLog.i("food.read", "ok", "count" to items.size, "ms" to msSince(started))
                _state.update { it.copy(items = items, products = store.all(), loadFailed = false) }
                fixPicks()
            }
            .onFailure { e ->
                AppLog.e("food.read", e, "failed", "ms" to msSince(started))
                _state.update { it.copy(loadFailed = true, products = store.all()) }
            }
    }

    /** Changing the day clears the highlight (food-log "Highlight on Ruoka"). */
    fun setDay(day: LocalDate) = _state.update { it.copy(day = minOf(day, LocalDate.now()), ordering = null, highlight = emptySet()) }

    /** Leaving Ruoka (the front page, Historia, Asetukset) clears the highlight. */
    fun clearHighlight() = _state.update { if (it.highlight.isEmpty()) it else it.copy(highlight = emptySet()) }

    /** When the search opened (`food.search.end ms=`) and how it is being left (`via=back|done`). */
    private var searchOpenedMs = 0L
    private var leaveVia = VIA_BACK

    /** The added strip's Valmis: the next leaving is logged as `via=done`. */
    fun markSearchDone() {
        leaveVia = VIA_DONE
    }

    /** A new search: nothing counted as added, no highlight, the keyboard up once the field shows (design 3, 6). */
    fun openSearch(day: LocalDate) {
        searchOpenedMs = android.os.SystemClock.elapsedRealtime()
        leaveVia = VIA_BACK
        _state.update {
            it.copy(searchDay = day, query = "", line = null, editReturn = null, keyboardBack = true, addedIds = emptySet(), highlight = emptySet(), picks = null)
        }
        fixPicks()
    }

    /** The empty-field ranking is computed once, as soon as the day has been read, and then kept (design 3). */
    private fun fixPicks() = _state.update { s ->
        val items = s.items
        if (s.searchDay == null || s.picks != null || items == null) s
        else s.copy(picks = Suggestions.atEmpty(items, s.products, ZonedDateTime.now()))
    }

    /** Remembers whether the keyboard was up as an editor replaces the search; cleared once the search has used it. */
    fun setKeyboardBack(up: Boolean) = _state.update { it.copy(keyboardBack = up) }

    /** Closes the search without the exit rule (import hand-over): drops the bar, a pending Kumoa included. */
    fun closeSearch() = _state.update { it.copy(searchDay = null, query = "", line = null, editReturn = null, keyboardBack = false, addedIds = emptySet(), picks = null) }

    /**
     * Back arrow and system back (food-log "Leaving the search"): the search closes and the bar goes.
     * With annokset counted as added, Ruoka's day becomes the search's day and they are highlighted;
     * the caller then shows Ruoka. Returns the target.
     */
    fun leaveSearch(): AddSearchTarget {
        val s = _state.value
        val day = s.searchDay ?: return AddSearchTarget.Origin
        val target = AddSearchExit.target(day, s.addedIds)
        AppLog.i(
            "food.search.end", "",
            "added" to s.addedIds.size,
            "via" to leaveVia,
            "ms" to android.os.SystemClock.elapsedRealtime() - searchOpenedMs,
        )
        leaveVia = VIA_BACK
        _state.update {
            val left = it.copy(searchDay = null, query = "", line = null, editReturn = null, keyboardBack = false, addedIds = emptySet(), picks = null)
            if (target is AddSearchTarget.Ruoka) left.copy(day = minOf(day, LocalDate.now()), ordering = null, highlight = target.uuids.toSet()) else left
        }
        return target
    }

    /**
     * The widget activity's exit with adds: the app shows Ruoka on [day] with [uuids] highlighted. Any
     * search or editor open in the app closes; the day is read only after the pending writes (the
     * activity's, queued earlier under the same lock) have finished.
     */
    fun showRuoka(day: LocalDate, uuids: Set<String>) {
        _state.update {
            it.copy(
                searchDay = null, query = "", line = null, editing = null, draft = null, editReturn = null, keyboardBack = false,
                addedIds = emptySet(), picks = null, day = minOf(day, LocalDate.now()), ordering = null, highlight = uuids,
            )
        }
        // Waits for the writes queued before this (the activity's adds), then reads the day.
        terveys.appScope.launch {
            writes.withLock { }
            refresh()
        }
    }

    /** The bar [line] ended without its action (timeout, swipe): the search no longer holds it. */
    fun clearLine(line: SheetLine) = _state.update { if (it.line == line) it.copy(line = null) else it }

    /** Drops an add from the count (failed write, Kumoa, delete) and from the highlight. */
    private fun uncount(vararg uuids: String) = _state.update { it.copy(addedIds = it.addedIds - uuids.toSet(), highlight = it.highlight - uuids.toSet()) }

    fun onQuery(text: String) = _state.update { it.copy(query = text) }

    /** Tapping a suggestion adds it at once. */
    fun addSuggestion(pick: Suggestion) {
        add(pick, null, "suggestion")
        _state.update { it.copy(query = "") }
    }

    /**
     * Enter: a bare number as Pikalisäys, a known name with its product's kcal; a new name opens the
     * new-annos editor instead, since its details come next (Peruuta keeps the typed text).
     */
    fun addTyped() {
        when (val input = AddInput.parse(_state.value.query) ?: return) {
            is AddInput.Quick -> add(null, input.kcal, "quick")
            is AddInput.Name -> {
                val own = store.all()
                val pick = resolveTyped(input.name, own, base)
                if (!isKnownName(input.name, own, base)) return editDraft(pick)
                add(pick, null, "typed")
            }
        }
        _state.update { it.copy(query = "") }
    }

    /**
     * Adds at the end of the sheet's day. Before the day has been read (or after a failed read) the
     * add waits for a read in the write queue, so it still lands at the end (food-widget design 8).
     */
    private fun add(pick: Suggestion?, quickKcal: Int?, kind: String) {
        val day = _state.value.searchDay ?: LocalDate.now()
        val now = Instant.now()
        val uuid = UUID.randomUUID().toString()
        val fields = arrayOf<Pair<String, Any?>>("from" to source, "kind" to kind)
        // Counted as added from the moment the add is queued (food-log "Leaving the search").
        _state.update { if (it.searchDay != null) it.copy(addedIds = it.addedIds + uuid) else it }
        terveys.appScope.launch {
            writes.withLock {
                if (_state.value.items == null) load()
                if (_state.value.items == null) {
                    AppLog.w("food.add", "failed", *fields, "reason" to "read")
                    uncount(uuid)
                    failed()
                    return@withLock
                }
                val (item, product) = FoodEdits.add(day, _state.value.dayItems(day), pick, quickKcal, now, ZoneId.systemDefault(), uuid)
                // The product is learnt even when the write fails (review decision).
                product?.let { p -> if (store.find(p.name) == null) store.put(p) }
                _state.update { it.copy(items = it.items.orEmpty() + item, line = SheetLine.Added(item), products = store.all()) }
                if (!runWrite("food.add", *fields, "kcal" to (item.kcal != null), "past" to !item.timed) { foods.insert(listOf(item)) }) uncount(uuid)
            }
        }
    }

    /** Kumoa of the latest add: the annos no longer counts as added. */
    fun undoAdd() {
        val added = _state.value.lastAdded ?: return
        _state.update { it.copy(line = null) }
        uncount(added.uuid)
        terveys.appScope.launch {
            writes.withLock {
                val record = _state.value.items.orEmpty().firstOrNull { it.uuid == added.uuid }?.recordId
                runCatching { if (record != null) foods.delete(listOf(record)) }
                    .onSuccess {
                        AppLog.i("food.delete", "ok", "undo" to "add")
                        changed()
                    }
                    .onFailure { AppLog.e("food.delete", it, "failed", "undo" to "add") }
                load()
            }
        }
    }

    /** AI import: reads [text] (shared or pasted); a text with no usable JSON says why and adds nothing. */
    fun openAi(text: String, from: String) {
        when (val r = AiPaste.parse(text)) {
            is AiResult.Ok -> {
                val p = r.parsed
                AppLog.i("food.import", "ok", "from" to from, "count" to p.items.size, "products" to p.products.size, "skipped" to p.skipped)
                // Any open sheet or editor closes (food-widget design 5).
                _state.update {
                    it.copy(ai = AiPreview(p.items, p.products, p.skipped, LocalDate.now(), ClipOffers.key(p)), searchDay = null, query = "", line = null, editing = null, draft = null, editReturn = null)
                }
                replan()
            }
            is AiResult.Failed -> {
                AppLog.i("food.import", "failed", "from" to from, "reason" to r.reason.name.lowercase())
                val text = if (r.reason == AiFailure.NO_JSON) R.string.ai_no_json else R.string.ai_empty
                if (_state.value.searchDay != null) _state.update { it.copy(line = SheetLine.Message(text)) } else _messages.tryEmit(UiMessage(text))
            }
        }
    }

    private val clipPrefs = app.getSharedPreferences("foodclip", android.content.Context.MODE_PRIVATE)

    private fun doneKeys(): List<String> =
        runCatching { clipJson.decodeFromString(doneSerializer, clipPrefs.getString("done", null) ?: "[]") }.getOrDefault(emptyList())

    private fun markDone(key: String) {
        clipPrefs.edit().putString("done", clipJson.encodeToString(doneSerializer, ClipOffers.markDone(doneKeys(), key))).apply()
        _state.update { s -> if (s.clipOffer?.key == key) s.copy(clipOffer = null) else s }
    }

    /**
     * The last read clip: its time and the parsed content (JSON in `foodclip`), the card's lines
     * planned against the current products on every check. A cache of an older version without the
     * parsed content counts as no cache (read again).
     */
    private fun cachedRead(): ClipRead? {
        if (!clipPrefs.contains("readTime")) return null
        val parsed = clipPrefs.getString("readParsed", null)
        if (parsed == null && clipPrefs.contains("readKey")) return null
        val offer = parsed?.let { runCatching { clipJson.decodeFromString(AiParsed.serializer(), it) }.getOrNull() ?: return null }
            ?.let { ClipOffers.offer(it, store.all(), base) }
        return ClipRead(clipPrefs.getLong("readTime", 0L), offer)
    }

    private fun cacheRead(time: Long, parsed: AiParsed?) {
        val edit = clipPrefs.edit().putLong("readTime", time).remove("readKey").remove("readCount").remove("readKcal").remove("readProducts")
        if (parsed == null) edit.remove("readParsed") else edit.putString("readParsed", clipJson.encodeToString(AiParsed.serializer(), parsed))
        edit.apply()
    }

    /**
     * The clipboard check on open and resume (design 3): [hasText] and [clipTime] from the clip's
     * description (no paste toast); [read] reads the text only when the clip changed since the last read.
     */
    fun checkClip(hasText: Boolean, clipTime: Long, read: () -> String) {
        // Before this change the app kept the last offered clip's time: older clips count as answered.
        val legacy = clipPrefs.getLong("offered", -1L)
        if (legacy >= 0) {
            clipPrefs.edit().remove("offered").apply()
            if (clipTime in 1..legacy && cachedRead() == null) cacheRead(clipTime, null)
        }
        val done = doneKeys()
        val offer: ClipOffer?
        val didRead: Boolean
        val cached = cachedRead()
        var json = hasText && cached?.offer != null
        when (val step = ClipOffers.step(hasText, clipTime, cached, done)) {
            is ClipStep.Known -> {
                didRead = false
                offer = step.offer
            }
            ClipStep.Read -> {
                didRead = true
                val parsed = ClipOffers.parsed(read())
                cacheRead(clipTime, parsed)
                json = parsed != null
                offer = ClipOffers.shown(parsed?.let { ClipOffers.offer(it, store.all(), base) }, done)
            }
        }
        AppLog.i("food.import.offer", "check", "read" to didRead, "json" to json, "done" to (json && offer == null))
        val before = _state.value.clipOffer
        _state.update { it.copy(clipOffer = offer) }
        if (offer != null && offer != before) AppLog.i("food.import.offer", "shown", "count" to offer.count)
    }

    /** Tapping the offer: logged; the caller reads the clip text and opens the preview (design 4). */
    fun clipOpened() {
        _state.value.clipOffer?.let { AppLog.i("food.import.offer", "open", "count" to it.count) }
    }

    /** Muokkaa in the app: [text] is the clipboard read now; the preview opens when it still holds the card's content. */
    fun openClipOffer(text: String) {
        if (!clipMatches(text)) return
        clipOpened()
        openAi(text, "paste")
    }

    /**
     * Whether [text] (the clipboard read at the tap) still holds the card's content (design 1). If
     * not, the card is refreshed (or goes away), "Leikepöytä muuttui" says so and nothing is saved.
     */
    fun clipMatches(text: String): Boolean {
        val offer = _state.value.clipOffer ?: return false
        val parsed = ClipOffers.parsed(text)
        if (parsed != null && ClipOffers.key(parsed) == offer.key) return true
        val found = ClipOffers.shown(parsed?.let { ClipOffers.offer(it, store.all(), base) }, doneKeys())
        AppLog.i("food.import.offer", "changed", "json" to (parsed != null))
        _state.update { it.copy(clipOffer = found, line = if (it.searchDay != null) SheetLine.ClipChanged else it.line) }
        if (_state.value.searchDay == null) _messages.tryEmit(UiMessage(R.string.ai_clip_changed))
        return false
    }

    /**
     * Lisää kaikki: the clipboard [text] read at the tap is saved as the preview's Lisää would, to
     * [day] (the sheet's, Ruoka's or today), without the preview; only when it is still the card's content.
     */
    fun addAllFromClip(text: String, day: LocalDate) {
        val offer = _state.value.clipOffer ?: return
        if (!clipMatches(text)) return
        val parsed = ClipOffers.parsed(text) ?: return
        AppLog.i("food.import.offer", "addall", "count" to offer.count, "products" to parsed.products.size)
        saveImport(parsed.items, parsed.products, minOf(day, LocalDate.now()), offer.key)
    }

    /** Ohita: that content is not offered again. */
    fun skipClipOffer() {
        val offer = _state.value.clipOffer ?: return
        AppLog.i("food.import.offer", "skip", "count" to offer.count)
        markDone(offer.key)
        if (_state.value.searchDay != null) {
            _state.update { it.copy(line = SheetLine.ClipSkipped(offer)) }
        } else {
            _messages.tryEmit(UiMessage(R.string.ai_clip_skipped, action = UiAction(R.string.undo) { unskip(offer) }))
        }
    }

    /** The sheet line's Kumoa of Ohita. */
    fun undoClipSkip() {
        val offer = (_state.value.line as? SheetLine.ClipSkipped)?.offer ?: return
        _state.update { it.copy(line = null) }
        unskip(offer)
    }

    /** Kumoa of Ohita: the content counts as never skipped and the card shows it again. */
    private fun unskip(offer: ClipOffer) {
        clipPrefs.edit().putString("done", clipJson.encodeToString(doneSerializer, ClipOffers.unmarkDone(doneKeys(), offer.key))).apply()
        AppLog.i("food.import.offer", "unskip", "count" to offer.count)
        _state.update { it.copy(clipOffer = offer) }
    }

    /** Kopioi tuontiohje: the example JSON in the copied text is never offered. */
    fun markTextDone(text: String) {
        (AiPaste.parse(text) as? AiResult.Ok)?.let { markDone(ClipOffers.key(it.parsed)) }
    }

    fun setAi(preview: AiPreview) {
        _state.update { it.copy(ai = preview.copy(day = minOf(preview.day, LocalDate.now()))) }
        replan()
    }

    fun cancelAi() = _state.update { it.copy(ai = null, aiPlan = null) }

    /** The preview's dry-run plan (the sum and the product change lines), as the save would run it. */
    private fun replan() = _state.update { s ->
        val preview = s.ai ?: return@update s.copy(aiPlan = null)
        val plan = AiImport.plan(
            preview.items, preview.products, preview.day, s.dayItems(preview.day), store.all(), base, Instant.now(), ZoneId.systemDefault(),
        ) { "" }
        s.copy(aiPlan = plan)
    }

    /** The Valmiit ruuat switch in Ruokakirjaus. */
    fun setBaseProducts(on: Boolean) {
        terveys.foodPrefs.setBaseProducts(on)
        AppLog.i("food.base", "set", "on" to on)
    }

    /** Lisää of the preview: items at the end of the day, products kept; Kumoa takes both back. */
    fun addAi() {
        val preview = _state.value.ai ?: return
        _state.update { it.copy(ai = null, aiPlan = null) }
        saveImport(preview.items, preview.products, preview.day, preview.sourceKey)
    }

    /**
     * An import's save (the preview's Lisää and the card's Lisää kaikki): items at the end of [day];
     * products are kept and [key] marked done only after Health Connect took the items (review
     * decision), so a failed write leaves the offer and the products as they were.
     */
    private fun saveImport(items: List<AiItem>, products: List<AiProduct>, day: LocalDate, key: String?) {
        terveys.appScope.launch {
            writes.withLock {
                if (items.isNotEmpty() && _state.value.items == null) load()
                if (items.isNotEmpty() && _state.value.items == null) {
                    AppLog.w("food.import.save", "failed", "reason" to "read")
                    failed()
                    return@withLock
                }
                val plan = AiImport.plan(
                    items, products, day, _state.value.dayItems(day), store.all(), base, Instant.now(), ZoneId.systemDefault(),
                ) { UUID.randomUUID().toString() }
                val started = System.nanoTime()
                // In the search the imported annokset count as added (food-log "Clipboard offer").
                val counted = plan.items.map { it.uuid }.toTypedArray()
                _state.update { if (it.searchDay != null) it.copy(addedIds = it.addedIds + counted) else it }
                val fields = arrayOf<Pair<String, Any?>>("count" to plan.items.size, "products" to plan.products.size, "changed" to plan.changes.size)
                val written = plan.items.isEmpty() || runCatching { foods.insert(plan.items) }
                    .onFailure { AppLog.e("food.import.save", it, "failed", *fields, "ms" to msSince(started)) }
                    .isSuccess
                if (!written) {
                    uncount(*counted)
                    failed()
                    load()
                    return@withLock
                }
                AppLog.i("food.import.save", "ok", *fields, "ms" to msSince(started))
                plan.products.forEach { store.put(it) }
                key?.let { markDone(it) }
                terveys.backup.request("food")
                changed()
                _state.update { it.copy(items = it.items.orEmpty() + plan.items, products = store.all()) }
                imported(plan)
                if (plan.items.isNotEmpty()) load()
            }
        }
    }

    /** The import's Kumoa: the sheet's line while a sheet is open, else the snackbar. */
    private fun imported(plan: AiImportPlan) {
        if (_state.value.searchDay != null) {
            _state.update { it.copy(line = SheetLine.Imported(plan)) }
            return
        }
        val undo = UiAction(R.string.undo) { undoAi(plan) }
        _messages.tryEmit(
            if (plan.items.isEmpty()) UiMessage(R.string.ai_products_saved, listOf(plan.products.size), undo)
            else UiMessage(R.string.ai_added, listOf(plan.items.size), undo),
        )
    }

    /** The sheet line's Kumoa of an import. */
    fun undoImport() {
        val plan = (_state.value.line as? SheetLine.Imported)?.plan ?: return
        _state.update { it.copy(line = null) }
        undoAi(plan)
    }

    /** A row's delete button (food-log "Removing a product from the sheet"): logged items keep their values. */
    fun deleteProduct(product: Product) {
        store.remove(product.key)
        AppLog.i("food.product.delete", "ok", "from" to source)
        _state.update { it.copy(products = store.all(), line = if (it.searchDay != null) SheetLine.ProductRemoved(product) else it.line) }
    }

    /** The sheet line's Kumoa of a product delete: the product back as it was. */
    fun undoDeleteProduct() {
        val product = (_state.value.line as? SheetLine.ProductRemoved)?.product ?: return
        store.restore(mapOf(product.key to product))
        AppLog.i("food.product.delete", "undo", "from" to source)
        _state.update { it.copy(products = store.all(), line = null) }
    }

    private fun undoAi(plan: AiImportPlan) {
        uncount(*plan.items.map { it.uuid }.toTypedArray())
        terveys.appScope.launch {
            writes.withLock {
                val records = _state.value.items.orEmpty().filter { i -> plan.items.any { it.uuid == i.uuid } }.mapNotNull { it.recordId }
                runCatching { if (records.isNotEmpty()) foods.delete(records) }
                    .onSuccess { AppLog.i("food.import.undo", "ok", "count" to records.size) }
                    .onFailure { AppLog.e("food.import.undo", it, "failed") }
                store.restore(plan.before)
                _state.update { it.copy(products = store.all()) }
                terveys.backup.request("food")
                load()
            }
        }
    }

    /** Muuta: from the sheet's line the editor replaces the sheet and leaving it reopens the sheet (design 4). */
    fun openEditor(item: FoodItem) = _state.update { it.copy(editing = item, draft = null, editReturn = it.searchDay, searchDay = null) }

    /** A row's edit button: the editor opens on the item a tap would add; nothing is written yet. */
    fun editDraft(pick: Suggestion) {
        val s = _state.value
        val day = s.searchDay ?: LocalDate.now()
        val (item, _) = FoodEdits.add(day, s.dayItems(day), pick, null, Instant.now(), ZoneId.systemDefault(), "draft")
        _state.update { it.copy(editing = item, draft = pick, editReturn = it.searchDay, searchDay = null) }
    }

    /** Lisää of the draft editor: the item as edited at the end of the day; back to the field with the "lisätty · Kumoa" bar. */
    fun addDraft(input: EditInput) {
        val current = _state.value
        val pick = current.draft ?: return
        val day = current.editReturn ?: current.searchDay ?: LocalDate.now()
        val now = Instant.now()
        val uuid = UUID.randomUUID().toString()
        val preview = FoodEdits.addEdited(day, current.items.orEmpty(), pick, input, store.all(), base, now, ZoneId.systemDefault(), uuid).others.product
        correction(preview)?.let { (p, before) ->
            correct(Correction(SaveKind.DRAFT, p.name, p.key, p.key, before, p.energy!!, uuid, input = input, pick = pick, day = day, now = now, uuid = uuid))
            return
        }
        _state.update { s -> leaveEditor(s, s.line).let { left -> left.copy(query = "", addedIds = if (left.searchDay != null) left.addedIds + uuid else left.addedIds) } }
        val fields = arrayOf<Pair<String, Any?>>("from" to source, "kind" to "edited")
        terveys.appScope.launch {
            writes.withLock {
                if (_state.value.items == null) load()
                if (_state.value.items == null) {
                    AppLog.w("food.add", "failed", *fields, "reason" to "read")
                    uncount(uuid)
                    failed()
                    return@withLock
                }
                val plan = FoodEdits.addEdited(day, _state.value.items.orEmpty(), pick, input, store.all(), base, now, ZoneId.systemDefault(), uuid)
                val item = plan.item
                val others = plan.others
                // As after Tallenna: the product keeps the edited values (also when the write fails).
                others.product?.let { store.put(it) }
                _state.update { s ->
                    val byUuid = others.new.associateBy { it.uuid }
                    s.copy(items = s.items.orEmpty().map { byUuid[it.uuid] ?: it } + item, line = SheetLine.Added(item), products = store.all())
                }
                val old = withRecordIds(others.old)
                val ok = runWrite("food.add", *fields, "kcal" to (item.kcal != null), "past" to !item.timed) {
                    foods.insert(listOf(item))
                    if (others.new.isNotEmpty()) foods.replace(old, others.new)
                }
                if (!ok) uncount(uuid)
            }
        }
    }

    /** Peruuta: back to the sheet (its line as it was) when the editor came from there. */
    fun closeEditor() = _state.update { leaveEditor(it, it.line) }

    fun saveEdit(item: FoodItem, input: EditInput) {
        val current = _state.value
        val plan = FoodEdits.edit(item, input, current.items.orEmpty(), store.all(), base)
        correction(plan.product)?.let { (p, before) ->
            val known = withRecordIds(listOf(item)).single()
            correct(Correction(SaveKind.EDIT, p.name, p.key, p.key, before, p.energy!!, item.uuid, item = known, input = input))
            return
        }
        val edited = plan.new.firstOrNull { it.uuid == item.uuid } ?: item
        // Only the line's own item moves the line; editing another item leaves it as it was.
        _state.update { s -> leaveEditor(s, s.line) }
        apply(plan, "food.edit", oldKey = null)
    }

    /** Poista on Ruoka (row menu or editor): at once, Ruoka's Kumoa bar brings the item back without its highlight. */
    fun delete(item: FoodItem) {
        // The record id before the item leaves the list (a just-added item has none of its own).
        val known = withRecordIds(listOf(item)).single()
        _state.update { leaveEditor(it, it.line).copy(items = it.items?.filter { i -> i.uuid != item.uuid }, addedIds = it.addedIds - item.uuid, highlight = it.highlight - item.uuid) }
        _messages.tryEmit(UiMessage(R.string.food_deleted, listOf(item.name), UiAction(R.string.undo) { restore(item) }))
        write("food.delete") { (known.recordId ?: withRecordIds(listOf(item)).single().recordId)?.let { foods.delete(listOf(it)) } }
    }

    /** The editor closes; it returns to the search with [line] when it came from there, else the list stays. */
    private fun leaveEditor(s: FoodUiState, line: SheetLine?): FoodUiState {
        val back = s.editReturn
        return s.copy(editing = null, draft = null, editReturn = null, searchDay = back ?: s.searchDay, line = if (back != null) line else s.line)
    }

    /** Undo of a delete: the old record id back, so its place and values return. */
    private fun restore(item: FoodItem) {
        write("food.add", "from" to source, "kind" to "undo") { foods.insert(listOf(item)) }
    }

    fun startOrder() = _state.update { it.copy(ordering = it.dayItems(it.day)) }

    fun move(from: Int, to: Int) = _state.update { s ->
        val list = s.ordering?.toMutableList() ?: return@update s
        if (from !in list.indices || to !in list.indices) return@update s
        list.add(to, list.removeAt(from))
        s.copy(ordering = list)
    }

    /** Valmis, or any other exit from Järjestä: positions 1..n, only the moved records rewritten. */
    fun finishOrder() {
        val order = _state.value.ordering ?: return
        val changed = FoodDay.renumbered(order)
        _state.update { s ->
            val byUuid = changed.associateBy { it.uuid }
            s.copy(ordering = null, items = s.items?.map { byUuid[it.uuid] ?: it })
        }
        if (changed.isEmpty()) return
        val old = order.filter { o -> changed.any { it.uuid == o.uuid } }
        write("food.order", "moved" to changed.size) { foods.replace(withRecordIds(old), changed) }
    }

    /** Ruokakirjaus: a product changed; a name of another product is refused. */
    fun saveProduct(oldKey: String, product: Product): Boolean {
        val clash = store.all().any { it.key == product.key && it.key != oldKey }
        if (clash) {
            _messages.tryEmit(UiMessage(R.string.food_product_exists, listOf(product.name)))
            return false
        }
        val before = store.all().firstOrNull { it.key == oldKey }?.energy
        if (FoodEdits.isCorrection(before, product.energy)) {
            // The editor stays open until the check (and the dialog) decide; productSaves closes it.
            correct(Correction(SaveKind.PRODUCT, product.name, oldKey, product.key, before!!, product.energy!!, null, oldKey = oldKey, product = product))
            return false
        }
        apply(FoodEdits.product(oldKey, product, _state.value.items.orEmpty()), "food.edit", oldKey)
        return true
    }

    /** Where a correction is saved from (design 5). */
    private enum class SaveKind { EDIT, DRAFT, PRODUCT }

    /**
     * A correction waiting for the read and the dialog: [key] the items' product key before the save,
     * [productKey] after it; the rest rebuilds the save's own plan from fresh items.
     */
    private class Correction(
        val kind: SaveKind,
        val name: String,
        val key: String,
        val productKey: String,
        val old: Energy,
        val new: Energy,
        val except: String?,
        val item: FoodItem? = null,
        val input: EditInput? = null,
        val pick: Suggestion? = null,
        val day: LocalDate? = null,
        val now: Instant? = null,
        val uuid: String? = null,
        val oldKey: String? = null,
        val product: Product? = null,
    )

    private var checking = false
    private var pending: Pair<Correction, Recalc>? = null

    /** The planned product and its energy before, when the save corrects an own product's energy (design 1). */
    private fun correction(product: Product?): Pair<Product, Energy>? {
        val p = product ?: return null
        val before = store.find(p.name)?.energy ?: return null
        return if (FoodEdits.isCorrection(before, p.energy)) p to before else null
    }

    /**
     * A correction (design 4–5): reads the items fresh under the write lock; with other items to
     * change the dialog asks, otherwise (or when the read fails) it saves as today.
     */
    private fun correct(c: Correction) {
        if (checking || pending != null) return
        checking = true
        terveys.appScope.launch {
            writes.withLock {
                val started = System.nanoTime()
                val fresh = runCatching { foods.own(Instant.now()) }
                    .onFailure { AppLog.e("food.read", it, "failed", "ms" to msSince(started)) }
                    .getOrNull()
                checking = false
                if (fresh == null) {
                    AppLog.i("food.product.energy", "", "choice" to "future", "reason" to "read")
                    saveCorrection(c, _state.value.items.orEmpty(), null)
                    energyBar(SheetLine.EnergyReadFailed, UiMessage(R.string.food_energy_read_failed))
                    return@withLock
                }
                _state.update { it.copy(items = fresh, loadFailed = false) }
                val r = FoodEdits.recalc(c.key, c.old, c.new, fresh, c.except)
                if (r.new.isEmpty()) {
                    saveCorrection(c, fresh, null)
                    return@withLock
                }
                pending = c to r
                _state.update { it.copy(energyAsk = EnergyAsk(c.name, r.new.size, r.days, r.oldSum, r.newSum, r.kept)) }
            }
        }
    }

    /** The dialog's answer: Peruuta leaves the editor open with nothing written. */
    fun chooseEnergy(choice: EnergyChoice) {
        val (c, r) = pending ?: return
        pending = null
        _state.update { it.copy(energyAsk = null) }
        AppLog.i("food.product.energy", "", "choice" to choice.name.lowercase(), "items" to r.new.size, "kept" to r.kept, "days" to r.days)
        if (choice == EnergyChoice.CANCEL) return
        terveys.appScope.launch {
            writes.withLock { saveCorrection(c, _state.value.items.orEmpty(), r.takeIf { choice == EnergyChoice.ALL }) }
        }
    }

    /**
     * Saves [c] as today (product, rename, Herkku, fills) from [items], with [recalc]'s items on top
     * when given (Päivitä kaikki). Runs under the write lock. The editor closes first.
     */
    private suspend fun saveCorrection(c: Correction, items: List<FoodItem>, recalc: Recalc?) {
        val zone = ZoneId.systemDefault()
        var added: FoodItem? = null
        val plan = when (c.kind) {
            SaveKind.EDIT -> FoodEdits.edit(c.item!!, c.input!!, items, store.all(), base)
            SaveKind.DRAFT -> FoodEdits.addEdited(c.day!!, items, c.pick!!, c.input!!, store.all(), base, c.now!!, zone, c.uuid!!).let {
                added = it.item
                it.others
            }
            SaveKind.PRODUCT -> FoodEdits.product(c.oldKey!!, c.product!!, items)
        }
        plan.product?.let { p -> if (c.kind == SaveKind.PRODUCT) store.replace(c.oldKey!!, p) else store.put(p) }
        val merged = FoodEdits.merge(plan, recalc, items)
        val edited = c.item?.let { i -> merged.new.firstOrNull { it.uuid == i.uuid } ?: i }
        _state.update { s ->
            val byUuid = merged.new.associateBy { it.uuid }
            val left = when (c.kind) {
                SaveKind.EDIT -> leaveEditor(s, s.line)
                SaveKind.DRAFT -> leaveEditor(s, s.line).let { left ->
                    left.copy(query = "", line = SheetLine.Added(added!!), addedIds = if (left.searchDay != null) left.addedIds + added!!.uuid else left.addedIds)
                }
                SaveKind.PRODUCT -> s.copy(productSaves = s.productSaves + 1)
            }
            left.copy(items = s.items.orEmpty().map { byUuid[it.uuid] ?: it } + listOfNotNull(added), products = store.all())
        }
        val recalced = recalc?.new?.map { it.uuid }?.toSet().orEmpty()
        val old = withRecordIds(merged.old)
        val ownOld = old.filter { it.uuid !in recalced }
        val ownNew = merged.new.filter { it.uuid !in recalced }
        val event = if (c.kind == SaveKind.DRAFT) "food.add" else "food.edit"
        val fields = if (c.kind == SaveKind.DRAFT) arrayOf<Pair<String, Any?>>("from" to source, "kind" to "edited") else arrayOf("items" to ownNew.size)
        val started = System.nanoTime()
        val wrote = runCatching {
            added?.let { foods.insert(listOf(it)) }
            if (ownNew.isNotEmpty()) foods.replace(ownOld, ownNew)
        }
            .onSuccess { if (added != null || ownNew.isNotEmpty()) AppLog.i(event, "ok", *fields, "ms" to msSince(started)) }
            .onFailure { AppLog.e(event, it, "failed", *fields, "ms" to msSince(started)) }
            .isSuccess
        if (!wrote) {
            added?.let { uncount(it.uuid) }
            failed()
            load()
            return
        }
        if (recalc != null && recalc.new.isNotEmpty()) {
            val undo = EnergyUndo(c.productKey, c.old, c.new, c.item.takeIf { c.kind == SaveKind.EDIT }, added?.uuid, recalc)
            val retry = EnergyRetry(setOf(c.key, c.productKey), c.except, undo)
            writeRecalc(old.filter { it.uuid in recalced }, merged.new.filter { it.uuid in recalced }, retry)
        }
        terveys.backup.request("food")
        changed()
        load()
    }

    /** Writes recalculated items in chunks (design 6) and shows the bar: Päivitetty · Kumoa, or X/N · Yritä uudelleen. */
    private suspend fun writeRecalc(old: List<FoodItem>, new: List<FoodItem>, retry: EnergyRetry) {
        val started = System.nanoTime()
        val oldBy = old.associateBy { it.uuid }
        var done = 0
        var error: Throwable? = null
        for (chunk in new.chunked(RECALC_CHUNK)) {
            error = runCatching { foods.replace(chunk.mapNotNull { oldBy[it.uuid] }, chunk) }.exceptionOrNull()
            if (error != null) break
            done += chunk.size
        }
        if (error == null) {
            AppLog.i("food.product.energy", "ok", "done" to done, "of" to new.size, "ms" to msSince(started))
            val undo = retry.undo
            energyBar(
                SheetLine.EnergyUpdated(undo),
                UiMessage(R.plurals.food_energy_updated, listOf(undo.count), UiAction(R.string.undo) { undoEnergy(undo) }, count = undo.count),
            )
        } else {
            AppLog.e("food.product.energy", error, "failed", "done" to done, "of" to new.size, "cause" to error.javaClass.simpleName)
            energyBar(
                SheetLine.EnergyFailed(done, new.size, retry),
                UiMessage(R.plurals.food_energy_partial, listOf(done, new.size), UiAction(R.string.food_energy_retry) { retryEnergy(retry) }, count = new.size),
            )
        }
    }

    /** The bar where the edit was made: the sheet's line while a sheet is open, else the app bar. */
    private fun energyBar(line: SheetLine, message: UiMessage) {
        if (_state.value.searchDay != null) _state.update { it.copy(line = line) } else _messages.tryEmit(message)
    }

    /** The sheet line's Kumoa of a correction. */
    fun undoEnergyLine() {
        val undo = (_state.value.line as? SheetLine.EnergyUpdated)?.undo ?: return
        _state.update { it.copy(line = null) }
        undoEnergy(undo)
    }

    /** The sheet line's Yritä uudelleen. */
    fun retryEnergyLine() {
        val retry = (_state.value.line as? SheetLine.EnergyFailed)?.retry ?: return
        _state.update { it.copy(line = null) }
        retryEnergy(retry)
    }

    /** Yritä uudelleen (design 6): recalculates from the items as they are now; updated items no longer match and are skipped. */
    private fun retryEnergy(retry: EnergyRetry) {
        terveys.appScope.launch {
            writes.withLock {
                val fresh = runCatching { foods.own(Instant.now()) }.onFailure { AppLog.e("food.read", it, "failed") }.getOrNull()
                if (fresh == null) {
                    failed()
                    return@withLock
                }
                val u = retry.undo
                val plans = retry.keys.map { FoodEdits.recalc(it, u.old, u.new, fresh, retry.except) }
                val old = plans.flatMap { it.old }.distinctBy { it.uuid }
                val new = plans.flatMap { it.new }.distinctBy { it.uuid }
                writeRecalc(old, new, retry)
                terveys.backup.request("food")
                changed()
                load()
            }
        }
    }

    /** Kumoa of a correction (design 7), against the items read now. */
    private fun undoEnergy(u: EnergyUndo) {
        terveys.appScope.launch {
            writes.withLock {
                val fresh = runCatching { foods.own(Instant.now()) }.onFailure { AppLog.e("food.read", it, "failed") }.getOrNull()
                if (fresh == null) {
                    failed()
                    return@withLock
                }
                store.all().firstOrNull { it.key == u.productKey }?.takeIf { it.energy == u.new }?.let { store.put(it.copy(energy = u.old)) }
                val back = FoodEdits.undoRecalc(u.recalc, fresh)
                val byUuid = fresh.associateBy { it.uuid }
                val old = back.old.toMutableList()
                val new = back.new.toMutableList()
                u.editedBefore?.let { b -> byUuid[b.uuid]?.takeIf { it.copy(recordId = null) != b.copy(recordId = null) }?.let { cur -> old += cur; new += b } }
                val drop = u.addedUuid?.let { byUuid[it]?.recordId }
                runCatching {
                    if (drop != null) foods.delete(listOf(drop))
                    val oldBy = old.associateBy { it.uuid }
                    new.chunked(RECALC_CHUNK).forEach { chunk -> foods.replace(chunk.mapNotNull { oldBy[it.uuid] }, chunk) }
                }
                    .onSuccess {
                        AppLog.i("food.product.energy", "undo", "items" to back.new.size)
                        terveys.backup.request("food")
                        changed()
                    }
                    .onFailure {
                        AppLog.e("food.product.energy", it, "undo failed", "items" to back.new.size)
                        failed()
                    }
                _state.update { it.copy(products = store.all()) }
                load()
            }
        }
    }

    /** Poista tuote under Asetukset → Ruokakirjaus: at once, "<name> poistettu · Kumoa" brings it back as it was. */
    fun removeProduct(key: String) {
        val product = store.all().firstOrNull { it.key == key } ?: return
        store.remove(key)
        AppLog.i("food.product.delete", "ok", "from" to "settings")
        _state.update { it.copy(products = store.all()) }
        _messages.tryEmit(
            UiMessage(R.string.food_deleted, listOf(product.name), UiAction(R.string.undo) {
                store.restore(mapOf(product.key to product))
                AppLog.i("food.product.delete", "undo", "from" to "settings")
                _state.update { it.copy(products = store.all()) }
            }),
        )
    }

    private fun apply(plan: FoodPlan, event: String, oldKey: String?) {
        plan.product?.let { p -> if (oldKey != null) store.replace(oldKey, p) else store.put(p) }
        _state.update { s ->
            val byUuid = plan.new.associateBy { it.uuid }
            s.copy(items = s.items?.map { byUuid[it.uuid] ?: it }, products = store.all())
        }
        if (plan.new.isEmpty()) return
        write(event, "items" to plan.new.size) { foods.replace(withRecordIds(plan.old), plan.new) }
    }

    /**
     * The record ids as last read: an edit or move made right after an add, before its write was
     * read back, still deletes the right record (writes run in order, each followed by a read).
     */
    private fun withRecordIds(old: List<FoodItem>): List<FoodItem> {
        val read = _state.value.items.orEmpty().associateBy { it.uuid }
        return old.map { o -> o.copy(recordId = o.recordId ?: read[o.uuid]?.recordId) }
    }

    /** Runs a write in the app scope, one at a time, then reads the log again. */
    private fun write(event: String, vararg fields: Pair<String, Any?>, block: suspend () -> Unit) = terveys.appScope.launch {
        writes.withLock { runWrite(event, *fields, block = block) }
    }

    /** A write under the lock; a failure says so (the sheet's line when the sheet is open) and reads the day back. */
    private suspend fun runWrite(event: String, vararg fields: Pair<String, Any?>, block: suspend () -> Unit): Boolean {
        val started = System.nanoTime()
        val ok = runCatching { block() }
            .onSuccess {
                AppLog.i(event, "ok", *fields, "ms" to msSince(started))
                terveys.backup.request("food")
                changed()
            }
            .onFailure { e ->
                AppLog.e(event, e, "failed", *fields, "ms" to msSince(started))
                failed()
            }
            .isSuccess
        load()
        return ok
    }

    /** A failed write: the bar of an open search (which stays open), else the snackbar. */
    private fun failed() {
        val inSearch = _state.value.searchDay != null
        _state.update { it.copy(line = if (inSearch) SheetLine.Failed else null) }
        if (!inSearch) _messages.tryEmit(UiMessage(R.string.food_write_failed))
    }

    /** The dialog's writes reach the app's screens (food-widget design 14). */
    private fun changed() {
        if (notifyApp) terveys.foodSignal.emit()
    }

    private fun msSince(startNanos: Long): Long = (System.nanoTime() - startNanos) / 1_000_000
}

/** Recalculated items are written this many records at a time (design 6). */
private const val RECALC_CHUNK = 500

private val clipJson = Json
private val doneSerializer = ListSerializer(String.serializer())

/** How the add search was left, for `food.search.end via=`. */
private const val VIA_BACK = "back"
private const val VIA_DONE = "done"
