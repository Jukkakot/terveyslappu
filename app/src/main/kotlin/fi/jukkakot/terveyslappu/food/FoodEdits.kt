package fi.jukkakot.terveyslappu.food

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** A change to write: the old versions of the items, their new versions, and the product to keep. */
data class FoodPlan(val old: List<FoodItem>, val new: List<FoodItem>, val product: Product? = null)

/** The editor's unit choice (design "Editor"): pieces, grams, or a total for the item. */
enum class EditMode { KPL, G, TOTAL }

/** What the item editor hands back. [amount] in the [mode]'s unit; [kcal] per piece, per 100 g or the total. */
data class EditInput(val name: String, val mode: EditMode, val amount: Double?, val kcal: Double?, val treat: Boolean)

/** Pure plans for adding, editing, products and import (food-log; the view model writes them). */
object FoodEdits {
    /**
     * A new item at the end of [day]: a [Suggestion] with its usual amount and kcal, or [QUICK_ADD]
     * with [quickKcal]. Returns the item and the product to keep (none for Pikalisäys).
     */
    fun add(day: LocalDate, dayItems: Collection<FoodItem>, pick: Suggestion?, quickKcal: Int?, now: Instant, zone: ZoneId, uuid: String): Pair<FoodItem, Product?> {
        val pos = FoodDay.nextPos(dayItems)
        val (time, timed) = FoodDay.timeFor(day, pos, now, zone)
        val product = pick?.product
        val amount = product?.usualAmount()
        val kcal = quickKcal?.toDouble() ?: amount?.let { a -> product.energy?.kcalOf(a) }
        val item = FoodItem(
            uuid = uuid,
            day = day,
            pos = pos,
            name = product?.name ?: QUICK_ADD,
            kcal = kcal,
            amount = amount,
            treat = product?.treat ?: false,
            time = time,
            zoneOffset = zone.rules.getOffset(time),
            timed = timed,
        )
        return item to product?.takeIf { it.name != QUICK_ADD }
    }

    /** The editor's starting values for [item]. */
    fun editStart(item: FoodItem, product: Product?): EditInput {
        val energy = product?.energy
        return when {
            energy != null && (item.amount == null || item.amount.unit == energy.per) -> EditInput(
                item.name, if (energy.per == AmountUnit.KPL) EditMode.KPL else EditMode.G,
                (item.amount ?: product.usualAmount())?.value, energy.kcal, item.treat,
            )
            item.amount != null -> EditInput(
                item.name, if (item.amount.unit == AmountUnit.KPL) EditMode.KPL else EditMode.G,
                item.amount.value, item.kcal?.let { k -> perUnit(k, item.amount) }, item.treat,
            )
            else -> EditInput(item.name, EditMode.TOTAL, null, item.kcal, item.treat)
        }
    }

    /** kcal of the editor's values, or null when unknown. */
    fun kcalOf(input: EditInput): Double? = when (input.mode) {
        EditMode.TOTAL -> input.kcal
        else -> {
            val unit = if (input.mode == EditMode.KPL) AmountUnit.KPL else AmountUnit.G
            val amount = input.amount?.takeIf { it > 0 }?.let { Amount(it, unit) } ?: if (unit == AmountUnit.KPL) Amount.ONE_PIECE else null
            input.kcal?.let { k -> amount?.let { Energy(k, unit).kcalOf(it) } }
        }
    }

    /**
     * Saving the editor (design "Editor"): per piece and per 100 g go to the product and fill its
     * items without kcal; a total stays on the item; Herkku belongs to the product and rewrites all
     * its items; a new name moves the item to that product (a new one when unknown).
     */
    fun edit(item: FoodItem, input: EditInput, all: Collection<FoodItem>, own: List<Product>, base: List<BaseProduct>): FoodPlan {
        val name = FoodNames.clean(input.name).ifEmpty { item.name }
        val quick = name == QUICK_ADD
        val start = if (quick) null else resolveName(name, own, base).product
        var product = start
        var updated: FoodItem
        val unit = when (input.mode) {
            EditMode.KPL -> AmountUnit.KPL
            EditMode.G -> AmountUnit.G
            EditMode.TOTAL -> null
        }
        if (unit == null) {
            updated = item.copy(name = name, kcal = input.kcal?.takeIf { it >= 0 }, amount = null)
        } else {
            val amount = input.amount?.takeIf { it > 0 }?.let { Amount(it, unit) }
            val given = input.kcal?.takeIf { it >= 0 }?.let { Energy(it, unit) }
            val energy = given ?: product?.energy?.takeIf { it.per == unit }
            val shown = amount ?: if (unit == AmountUnit.KPL && energy != null) Amount.ONE_PIECE else null
            updated = item.copy(name = name, amount = shown, kcal = energy?.let { e -> shown?.let { e.kcalOf(it) } })
            if (product != null) product = product.copy(energy = energy ?: product.energy, lastAmount = amount ?: product.lastAmount)
        }
        if (product != null) product = product.copy(treat = input.treat)
        updated = updated.copy(treat = product?.treat ?: input.treat)
        val changed = linkedMapOf(updated.uuid to updated)
        val others = all.filter { it.uuid != item.uuid }
        if (product != null) {
            if (unit != null && input.kcal != null && product.energy != null) {
                FoodDay.fill(others, product.key, product.energy, updated.amount).forEach { changed[it.uuid] = it }
            }
            if (start?.treat != product.treat || all.any { it.key == product.key && it.treat != product.treat }) {
                others.filter { it.key == product.key && it.treat != product.treat }.forEach { o ->
                    changed[o.uuid] = (changed[o.uuid] ?: o).copy(treat = product.treat)
                }
            }
        }
        return plan(all, changed.values, product)
    }

    /**
     * A product changed in Ruokakirjaus: a rename rewrites its items, a category change their
     * Herkku, an energy fills its items without kcal. [oldKey] is the product's key before.
     */
    fun product(oldKey: String, new: Product, all: Collection<FoodItem>): FoodPlan {
        val changed = all.filter { it.key == oldKey }
            .map { it.copy(name = new.name, treat = new.treat) }
            .associateBy { it.uuid }.toMutableMap()
        val energy = new.energy
        if (energy != null) {
            FoodDay.fill(changed.values, new.key, energy, new.lastAmount).forEach { changed[it.uuid] = it }
        }
        return plan(all, changed.values, new)
    }

    /**
     * Import (data-file "Food parts"): the file's items whose uuid is not yet logged, oldest first;
     * a position already taken on that day goes after the day's items.
     */
    fun import(file: Collection<FoodItem>, existing: Collection<FoodItem>): List<FoodItem> {
        val known = existing.map { it.uuid }.toMutableSet()
        val taken = existing.groupBy { it.day }.mapValues { (_, v) -> v.mapNotNull { it.pos }.toMutableSet() }.toMutableMap()
        val out = mutableListOf<FoodItem>()
        for (item in fiSorted(file)) {
            if (!known.add(item.uuid)) continue
            val used = taken.getOrPut(item.day) { mutableSetOf() }
            val pos = item.pos?.takeIf { it !in used } ?: ((used.maxOrNull() ?: 0) + 1)
            used += pos
            out += item.copy(pos = pos, recordId = null)
        }
        return out
    }

    private fun fiSorted(items: Collection<FoodItem>) =
        items.sortedWith(compareBy<FoodItem> { it.day }.thenBy { it.pos == null }.thenBy { it.pos ?: 0 }.thenBy { it.time })

    private fun plan(all: Collection<FoodItem>, changed: Collection<FoodItem>, product: Product?): FoodPlan {
        val byUuid = all.associateBy { it.uuid }
        val real = changed.filter { it != byUuid[it.uuid] }
        return FoodPlan(real.mapNotNull { byUuid[it.uuid] }, real, product)
    }

    private fun perUnit(kcal: Double, amount: Amount): Double =
        if (amount.unit == AmountUnit.G) kcal * 100.0 / amount.value else kcal / amount.value
}
