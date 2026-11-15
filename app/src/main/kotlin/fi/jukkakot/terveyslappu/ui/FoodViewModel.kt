package fi.jukkakot.terveyslappu.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.TerveysApp
import fi.jukkakot.terveyslappu.food.AddInput
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
import fi.jukkakot.terveyslappu.food.FoodDay
import fi.jukkakot.terveyslappu.food.FoodEdits
import fi.jukkakot.terveyslappu.food.FoodItem
import fi.jukkakot.terveyslappu.food.FoodPlan
import fi.jukkakot.terveyslappu.food.FoodRecords
import fi.jukkakot.terveyslappu.food.HealthConnectFoods
import fi.jukkakot.terveyslappu.food.Product
import fi.jukkakot.terveyslappu.food.Suggestion
import fi.jukkakot.terveyslappu.food.resolveName
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
import java.util.UUID

/** Whether the food log may read and write nutrition records (design 8). */
enum class FoodAccess { CHECKING, UNAVAILABLE, NEEDS, DENIED, GRANTED }

/** The line under the add field (food-widget design 7): the latest add, a delete's Kumoa, or a failed write. */
sealed interface SheetLine {
    /** "<name> <amount> · <kcal> · Muuta · Kumoa". */
    data class Added(val item: FoodItem) : SheetLine

    /** "<name> poistettu · Kumoa", after Poista in the editor opened from the sheet. */
    data class Deleted(val item: FoodItem) : SheetLine

    /** "Tallennus epäonnistui". */
    data object Failed : SheetLine

    /** "<N> annosta lisätty · Kumoa" (or "<N> tuotetta tallennettu · Kumoa") after an import in an open sheet. */
    data class Imported(val plan: AiImportPlan) : SheetLine

    /** "<name> poistettu · Kumoa" after a row's delete button. */
    data class ProductRemoved(val product: Product) : SheetLine

    /** "Leikepöytä muuttui": the card's content left the clipboard before the tap; nothing saved. */
    data object ClipChanged : SheetLine

    /** Ohita on the clipboard card inside the sheet: "Ohitettu · Kumoa". */
    data class ClipSkipped(val offer: ClipOffer) : SheetLine
}

data class FoodUiState(
    val access: FoodAccess = FoodAccess.CHECKING,
    /** Every own item, null until read. */
    val items: List<FoodItem>? = null,
    val products: List<Product> = emptyList(),
    val loadFailed: Boolean = false,
    /** The Ruoka screen's day. */
    val day: LocalDate = LocalDate.now(),
    /** The add sheet adds to this day; null = closed. */
    val sheetDay: LocalDate? = null,
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
    /** The keyboard was up when an editor replaced the sheet: it comes back with the sheet (food-log "Adding an item", Keyboard). */
    val sheetKeyboard: Boolean = false,
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
    val base get() = terveys.baseProducts

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
            terveys.foodSignal.changes.collect {
                if (notifyApp) return@collect
                if (_state.value.access == FoodAccess.GRANTED) load() else refresh()
            }
        }
    }

    /** The Lisää annos dialog (food-widget): adds go to today's sheet, writes tell the app. */
    fun startDialog(from: String) {
        source = from
        notifyApp = true
        if (dialogStarted) return
        dialogStarted = true
        openSheet(LocalDate.now())
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
            }
            .onFailure { e ->
                AppLog.e("food.read", e, "failed", "ms" to msSince(started))
                _state.update { it.copy(loadFailed = true, products = store.all()) }
            }
    }

    fun setDay(day: LocalDate) = _state.update { it.copy(day = minOf(day, LocalDate.now()), ordering = null) }

    fun openSheet(day: LocalDate) = _state.update { it.copy(sheetDay = day, query = "", line = null, editReturn = null, sheetKeyboard = false) }

    /** Remembers whether the keyboard was up as an editor replaces the sheet; cleared once the sheet has used it. */
    fun setSheetKeyboard(up: Boolean) = _state.update { it.copy(sheetKeyboard = up) }

    /** Closing drops the line, a pending delete Kumoa included (design 7). */
    fun closeSheet() = _state.update { it.copy(sheetDay = null, query = "", line = null, editReturn = null) }

    fun onQuery(text: String) = _state.update { it.copy(query = text) }

    /** Tapping a suggestion adds it at once. */
    fun addSuggestion(pick: Suggestion) {
        add(pick, null, "suggestion")
        _state.update { it.copy(query = "") }
    }

    /** Enter: exactly the typed text (a bare number as Pikalisäys, a name with its product's kcal). */
    fun addTyped() {
        when (val input = AddInput.parse(_state.value.query) ?: return) {
            is AddInput.Quick -> add(null, input.kcal, "quick")
            is AddInput.Name -> add(resolveName(input.name, store.all(), base), null, "typed")
        }
        _state.update { it.copy(query = "") }
    }

    /**
     * Adds at the end of the sheet's day. Before the day has been read (or after a failed read) the
     * add waits for a read in the write queue, so it still lands at the end (food-widget design 8).
     */
    private fun add(pick: Suggestion?, quickKcal: Int?, kind: String) {
        val day = _state.value.sheetDay ?: LocalDate.now()
        val now = Instant.now()
        val uuid = UUID.randomUUID().toString()
        val fields = arrayOf<Pair<String, Any?>>("from" to source, "kind" to kind)
        terveys.appScope.launch {
            writes.withLock {
                if (_state.value.items == null) load()
                if (_state.value.items == null) {
                    AppLog.w("food.add", "failed", *fields, "reason" to "read")
                    failed()
                    return@withLock
                }
                val (item, product) = FoodEdits.add(day, _state.value.dayItems(day), pick, quickKcal, now, ZoneId.systemDefault(), uuid)
                // The product is learnt even when the write fails (review decision).
                product?.let { p -> if (store.find(p.name) == null) store.put(p) }
                _state.update { it.copy(items = it.items.orEmpty() + item, line = SheetLine.Added(item), products = store.all()) }
                runWrite("food.add", *fields, "kcal" to (item.kcal != null), "past" to !item.timed) { foods.insert(listOf(item)) }
            }
        }
    }

    /** Kumoa of the latest add. */
    fun undoAdd() {
        val added = _state.value.lastAdded ?: return
        _state.update { it.copy(line = null) }
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
                    it.copy(ai = AiPreview(p.items, p.products, p.skipped, LocalDate.now(), ClipOffers.key(p)), sheetDay = null, query = "", line = null, editing = null, draft = null, editReturn = null)
                }
            }
            is AiResult.Failed -> {
                AppLog.i("food.import", "failed", "from" to from, "reason" to r.reason.name.lowercase())
                _messages.tryEmit(UiMessage(if (r.reason == AiFailure.NO_JSON) R.string.ai_no_json else R.string.ai_empty))
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
        _state.update { it.copy(clipOffer = found, line = if (it.sheetDay != null) SheetLine.ClipChanged else it.line) }
        if (_state.value.sheetDay == null) _messages.tryEmit(UiMessage(R.string.ai_clip_changed))
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
        if (_state.value.sheetDay != null) {
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

    fun setAi(preview: AiPreview) = _state.update { it.copy(ai = preview.copy(day = minOf(preview.day, LocalDate.now()))) }

    fun cancelAi() = _state.update { it.copy(ai = null) }

    /** Lisää of the preview: items at the end of the day, products kept; Kumoa takes both back. */
    fun addAi() {
        val preview = _state.value.ai ?: return
        _state.update { it.copy(ai = null) }
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
                val fields = arrayOf<Pair<String, Any?>>("count" to plan.items.size, "products" to plan.products.size)
                val written = plan.items.isEmpty() || runCatching { foods.insert(plan.items) }
                    .onFailure { AppLog.e("food.import.save", it, "failed", *fields, "ms" to msSince(started)) }
                    .isSuccess
                if (!written) {
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
        if (_state.value.sheetDay != null) {
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
        _state.update { it.copy(products = store.all(), line = if (it.sheetDay != null) SheetLine.ProductRemoved(product) else it.line) }
    }

    /** The sheet line's Kumoa of a product delete: the product back as it was. */
    fun undoDeleteProduct() {
        val product = (_state.value.line as? SheetLine.ProductRemoved)?.product ?: return
        store.restore(mapOf(product.key to product))
        AppLog.i("food.product.delete", "undo", "from" to source)
        _state.update { it.copy(products = store.all(), line = null) }
    }

    private fun undoAi(plan: AiImportPlan) {
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
    fun openEditor(item: FoodItem) = _state.update { it.copy(editing = item, draft = null, editReturn = it.sheetDay, sheetDay = null) }

    /** A row's edit button: the editor opens on the item a tap would add; nothing is written yet. */
    fun editDraft(pick: Suggestion) {
        val s = _state.value
        val day = s.sheetDay ?: LocalDate.now()
        val (item, _) = FoodEdits.add(day, s.dayItems(day), pick, null, Instant.now(), ZoneId.systemDefault(), "draft")
        _state.update { it.copy(editing = item, draft = pick, editReturn = it.sheetDay, sheetDay = null) }
    }

    /** Lisää of the draft editor: the item as edited at the end of the day; back to the field with the Muuta · Kumoa line. */
    fun addDraft(input: EditInput) {
        val current = _state.value
        val pick = current.draft ?: return
        val day = current.editReturn ?: current.sheetDay ?: LocalDate.now()
        _state.update { leaveEditor(it, it.line).copy(query = "") }
        val now = Instant.now()
        val uuid = UUID.randomUUID().toString()
        val fields = arrayOf<Pair<String, Any?>>("from" to source, "kind" to "edited")
        terveys.appScope.launch {
            writes.withLock {
                if (_state.value.items == null) load()
                if (_state.value.items == null) {
                    AppLog.w("food.add", "failed", *fields, "reason" to "read")
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
                runWrite("food.add", *fields, "kcal" to (item.kcal != null), "past" to !item.timed) {
                    foods.insert(listOf(item))
                    if (others.new.isNotEmpty()) foods.replace(old, others.new)
                }
            }
        }
    }

    /** Peruuta: back to the sheet (its line as it was) when the editor came from there. */
    fun closeEditor() = _state.update { leaveEditor(it, it.line) }

    fun saveEdit(item: FoodItem, input: EditInput) {
        val current = _state.value
        val plan = FoodEdits.edit(item, input, current.items.orEmpty(), store.all(), base)
        val edited = plan.new.firstOrNull { it.uuid == item.uuid } ?: item
        // Only the line's own item moves the line; editing another item leaves it as it was.
        _state.update { s -> leaveEditor(s, if (s.lastAdded?.uuid == item.uuid) SheetLine.Added(edited) else s.line) }
        apply(plan, "food.edit", oldKey = null)
    }

    fun delete(item: FoodItem) {
        val toSheet = _state.value.editReturn != null
        // The record id before the item leaves the list (the Muuta line's item has none of its own).
        val known = withRecordIds(listOf(item)).single()
        _state.update { leaveEditor(it, SheetLine.Deleted(item)).copy(items = it.items?.filter { i -> i.uuid != item.uuid }) }
        // In the sheet the Kumoa is its line (design 7); elsewhere a snackbar as before.
        if (!toSheet) _messages.tryEmit(UiMessage(R.string.food_deleted, listOf(item.name), UiAction(R.string.undo) { restore(item) }))
        write("food.delete") { (known.recordId ?: withRecordIds(listOf(item)).single().recordId)?.let { foods.delete(listOf(it)) } }
    }

    /** The sheet line's Kumoa of a delete. */
    fun undoDelete() {
        val deleted = (_state.value.line as? SheetLine.Deleted)?.item ?: return
        _state.update { it.copy(line = null) }
        restore(deleted)
    }

    /** The editor closes; it reopens the sheet with [line] when it came from there, else the list stays. */
    private fun leaveEditor(s: FoodUiState, line: SheetLine?): FoodUiState {
        val back = s.editReturn
        return s.copy(editing = null, draft = null, editReturn = null, sheetDay = back ?: s.sheetDay, line = if (back != null) line else s.line)
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
        apply(FoodEdits.product(oldKey, product, _state.value.items.orEmpty()), "food.edit", oldKey)
        return true
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
    private suspend fun runWrite(event: String, vararg fields: Pair<String, Any?>, block: suspend () -> Unit) {
        val started = System.nanoTime()
        runCatching { block() }
            .onSuccess {
                AppLog.i(event, "ok", *fields, "ms" to msSince(started))
                terveys.backup.request("food")
                changed()
            }
            .onFailure { e ->
                AppLog.e(event, e, "failed", *fields, "ms" to msSince(started))
                failed()
            }
        load()
    }

    /** A failed write: the line in an open sheet (which stays open), else the snackbar. */
    private fun failed() {
        val inSheet = _state.value.sheetDay != null
        _state.update { it.copy(line = if (inSheet) SheetLine.Failed else null) }
        if (!inSheet) _messages.tryEmit(UiMessage(R.string.food_write_failed))
    }

    /** The dialog's writes reach the app's screens (food-widget design 14). */
    private fun changed() {
        if (notifyApp) terveys.foodSignal.emit()
    }

    private fun msSince(startNanos: Long): Long = (System.nanoTime() - startNanos) / 1_000_000
}

private val clipJson = Json
private val doneSerializer = ListSerializer(String.serializer())
