package fi.jukkakot.terveyslappu.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.TerveysApp
import fi.jukkakot.terveyslappu.food.AddInput
import fi.jukkakot.terveyslappu.food.AiFailure
import fi.jukkakot.terveyslappu.food.AiImport
import fi.jukkakot.terveyslappu.food.AiImportPlan
import fi.jukkakot.terveyslappu.food.AiPaste
import fi.jukkakot.terveyslappu.food.ClipOffer
import fi.jukkakot.terveyslappu.food.ClipOffers
import fi.jukkakot.terveyslappu.food.AiResult
import fi.jukkakot.terveyslappu.food.EditInput
import fi.jukkakot.terveyslappu.food.FoodDay
import fi.jukkakot.terveyslappu.food.FoodEdits
import fi.jukkakot.terveyslappu.food.FoodItem
import fi.jukkakot.terveyslappu.food.FoodPlan
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

/** Whether the food log may read and write nutrition records (design 8). */
enum class FoodAccess { CHECKING, UNAVAILABLE, NEEDS, DENIED, GRANTED }

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
    /** The latest add (the Muuta · Kumoa line), until the next add or the sheet closes. */
    val lastAdded: FoodItem? = null,
    /** The item editor is open on this item. */
    val editing: FoodItem? = null,
    /** Järjestä: the working order of the day. */
    val ordering: List<FoodItem>? = null,
    /** The AI import preview, null = closed. */
    val ai: AiPreview? = null,
    /** The clipboard line (food-ai-import), null = none. */
    val clipOffer: ClipOffer? = null,
) {
    fun dayItems(day: LocalDate): List<FoodItem> = FoodDay.ordered(items.orEmpty().filter { it.day == day })
}

/** The food log's state and writes (food-log); Health Connect holds the log, app storage the products. */
class FoodViewModel(app: Application) : AndroidViewModel(app) {
    private val terveys = app as TerveysApp
    val foods = HealthConnectFoods(app)
    private val store get() = terveys.products
    val base get() = terveys.baseProducts

    private val _state = MutableStateFlow(FoodUiState())
    val state: StateFlow<FoodUiState> = _state

    private val _messages = MutableSharedFlow<UiMessage>(extraBufferCapacity = 4)
    val messages: SharedFlow<UiMessage> = _messages

    /** Writes run one at a time, so an undo finds the record its add wrote. */
    private val writes = Mutex()

    var permissionAsked = false
        private set

    init {
        // An import wrote foods or products (MainViewModel).
        viewModelScope.launch { terveys.foodSignal.changes.collect { if (_state.value.access == FoodAccess.GRANTED) load() else refresh() } }
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
        val ok = granted.containsAll(foods.permissions)
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

    fun openSheet(day: LocalDate) = _state.update { it.copy(sheetDay = day, query = "", lastAdded = null) }

    fun closeSheet() = _state.update { it.copy(sheetDay = null, query = "", lastAdded = null) }

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

    private fun add(pick: Suggestion?, quickKcal: Int?, from: String) {
        val current = _state.value
        val day = current.sheetDay ?: LocalDate.now()
        val zone = ZoneId.systemDefault()
        val (item, product) = FoodEdits.add(day, current.dayItems(day), pick, quickKcal, Instant.now(), zone, UUID.randomUUID().toString())
        // The product is learnt even when the write fails (review decision).
        product?.let { p -> if (store.find(p.name) == null) store.put(p) }
        _state.update { it.copy(items = it.items.orEmpty() + item, lastAdded = item, products = store.all()) }
        write("food.add", "from" to from, "kcal" to (item.kcal != null), "past" to !item.timed) {
            foods.insert(listOf(item))
        }
    }

    /** Kumoa of the latest add. */
    fun undoAdd() {
        val added = _state.value.lastAdded ?: return
        _state.update { it.copy(lastAdded = null) }
        viewModelScope.launch {
            writes.withLock {
                val record = _state.value.items.orEmpty().firstOrNull { it.uuid == added.uuid }?.recordId
                runCatching { if (record != null) foods.delete(listOf(record)) }
                    .onSuccess { AppLog.i("food.delete", "ok", "undo" to "add") }
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
                _state.update { it.copy(ai = AiPreview(p.items, p.products, p.skipped, LocalDate.now()), sheetDay = null, query = "", lastAdded = null) }
            }
            is AiResult.Failed -> {
                AppLog.i("food.import", "failed", "from" to from, "reason" to r.reason.name.lowercase())
                _messages.tryEmit(UiMessage(if (r.reason == AiFailure.NO_JSON) R.string.ai_no_json else R.string.ai_empty))
            }
        }
    }

    private val clipPrefs = app.getSharedPreferences("foodclip", android.content.Context.MODE_PRIVATE)

    /** Timestamp of the clip last offered; the activity asks before it reads any clip text. */
    fun lastOfferedClip(): Long = clipPrefs.getLong("offered", 0L)

    /** The activity read a new clip: it is marked offered whatever it holds; food JSON shows the line. */
    fun offerClip(clipTime: Long, text: String) {
        clipPrefs.edit().putLong("offered", clipTime).apply()
        val offer = ClipOffers.from(text) ?: return
        AppLog.i("food.import.offer", "shown", "count" to offer.count)
        _state.update { it.copy(clipOffer = offer) }
    }

    /** Näytä: the offered text opens the preview. */
    fun openClipOffer() {
        val offer = _state.value.clipOffer ?: return
        AppLog.i("food.import.offer", "open", "count" to offer.count)
        _state.update { it.copy(clipOffer = null) }
        openAi(offer.text, "paste")
    }

    /** Ohita: the line goes away. */
    fun skipClipOffer() {
        val offer = _state.value.clipOffer ?: return
        AppLog.i("food.import.offer", "skip", "count" to offer.count)
        _state.update { it.copy(clipOffer = null) }
    }

    fun setAi(preview: AiPreview) = _state.update { it.copy(ai = preview.copy(day = minOf(preview.day, LocalDate.now()))) }

    fun cancelAi() = _state.update { it.copy(ai = null) }

    /** Lisää of the preview: items at the end of the day, products kept; Kumoa takes both back. */
    fun addAi() {
        val preview = _state.value.ai ?: return
        _state.update { it.copy(ai = null) }
        viewModelScope.launch {
            if (_state.value.items == null) load()
            val current = _state.value
            val plan = AiImport.plan(
                preview.items, preview.products, preview.day, current.dayItems(preview.day), store.all(), base,
                Instant.now(), ZoneId.systemDefault(),
            ) { UUID.randomUUID().toString() }
            plan.products.forEach { store.put(it) }
            _state.update { it.copy(items = it.items.orEmpty() + plan.items, products = store.all()) }
            val undo = UiAction(R.string.undo) { undoAi(plan) }
            _messages.tryEmit(
                if (plan.items.isEmpty()) UiMessage(R.string.ai_products_saved, listOf(plan.products.size), undo)
                else UiMessage(R.string.ai_added, listOf(plan.items.size), undo),
            )
            if (plan.items.isEmpty()) {
                AppLog.i("food.import.save", "ok", "count" to 0, "products" to plan.products.size)
                terveys.backup.request("food")
            } else {
                write("food.import.save", "count" to plan.items.size, "products" to plan.products.size) { foods.insert(plan.items) }
            }
        }
    }

    private fun undoAi(plan: AiImportPlan) {
        viewModelScope.launch {
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

    fun openEditor(item: FoodItem) = _state.update { it.copy(editing = item, sheetDay = null, lastAdded = null) }

    fun closeEditor() = _state.update { it.copy(editing = null) }

    fun saveEdit(item: FoodItem, input: EditInput) {
        val current = _state.value
        val plan = FoodEdits.edit(item, input, current.items.orEmpty(), store.all(), base)
        _state.update { it.copy(editing = null) }
        apply(plan, "food.edit", oldKey = null)
    }

    fun delete(item: FoodItem) {
        _state.update { it.copy(editing = null, items = it.items?.filter { i -> i.uuid != item.uuid }) }
        _messages.tryEmit(UiMessage(R.string.food_deleted, listOf(item.name), UiAction(R.string.undo) { restore(item) }))
        write("food.delete") { withRecordIds(listOf(item)).single().recordId?.let { foods.delete(listOf(it)) } }
    }

    /** Undo of a delete: the old record id back, so its place and values return. */
    private fun restore(item: FoodItem) {
        write("food.add", "from" to "undo") { foods.insert(listOf(item)) }
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

    fun removeProduct(key: String) {
        store.remove(key)
        AppLog.i("food.edit", "product removed")
        _state.update { it.copy(products = store.all()) }
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

    /** Runs a write, then reads the log again; a failure reads the day back and says so. */
    private fun write(event: String, vararg fields: Pair<String, Any?>, block: suspend () -> Unit) = viewModelScope.launch {
        writes.withLock {
            val started = System.nanoTime()
            runCatching { block() }
                .onSuccess {
                    AppLog.i(event, "ok", *fields, "ms" to msSince(started))
                    terveys.backup.request("food")
                }
                .onFailure { e ->
                    AppLog.e(event, e, "failed", *fields, "ms" to msSince(started))
                    _messages.tryEmit(UiMessage(R.string.food_write_failed))
                    _state.update { it.copy(lastAdded = null) }
                }
            load()
        }
    }

    private fun msSince(startNanos: Long): Long = (System.nanoTime() - startNanos) / 1_000_000
}
