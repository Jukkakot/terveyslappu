package fi.jukkakot.terveyslappu.food

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** One eaten item of an AI answer; [kcal] is the item's total. */
data class AiItem(val name: String, val amount: Amount?, val kcal: Double?, val treat: Boolean)

/** One product the AI answer wants kept; [energy] per 100 g or per piece, [usual] the usual amount. */
data class AiProduct(val name: String, val energy: Energy, val usual: Amount?, val treat: Boolean)

/** What was read from a text: the entries and how many were unusable. */
data class AiParsed(val items: List<AiItem>, val products: List<AiProduct>, val skipped: Int)

/** Why a text gave nothing: no JSON block found, or a block without a usable entry. */
enum class AiFailure { NO_JSON, EMPTY }

sealed interface AiResult {
    data class Ok(val parsed: AiParsed) : AiResult
    data class Failed(val reason: AiFailure) : AiResult
}

/** Reads the AI food JSON (assets/ai-food-prompt.txt is the contract) from any text around it. */
object AiPaste {
    fun parse(text: String): AiResult {
        var i = 0
        var found = false
        while (i < text.length) {
            val c = text[i]
            if (c == '{' || c == '[') {
                val end = matchEnd(text, i)
                if (end != null) {
                    val element = runCatching { Json.parseToJsonElement(text.substring(i, end + 1)) }.getOrNull()
                    val parsed = element?.let { read(it) }
                    if (parsed != null) {
                        found = true
                        if (parsed.items.isNotEmpty() || parsed.products.isNotEmpty()) return AiResult.Ok(parsed)
                    }
                }
            }
            i++
        }
        return AiResult.Failed(if (found) AiFailure.EMPTY else AiFailure.NO_JSON)
    }

    /** Index of the bracket closing the one at [start], strings and escapes respected; null if none. */
    private fun matchEnd(text: String, start: Int): Int? {
        var depth = 0
        var inString = false
        var escaped = false
        for (i in start until text.length) {
            val c = text[i]
            if (inString) {
                when {
                    escaped -> escaped = false
                    c == '\\' -> escaped = true
                    c == '"' -> inString = false
                }
                continue
            }
            when (c) {
                '"' -> inString = true
                '{', '[' -> depth++
                '}', ']' -> if (--depth == 0) return i
            }
        }
        return null
    }

    /** Null when the JSON is not the food shape at all. */
    private fun read(element: JsonElement): AiParsed? {
        val itemList: List<JsonElement>
        val productList: List<JsonElement>
        when (element) {
            is JsonObject -> {
                val items = element["items"] as? JsonArray
                val products = element["products"] as? JsonArray
                if (items == null && products == null) return null
                itemList = items.orEmpty()
                productList = products.orEmpty()
            }
            is JsonArray -> {
                if (element.none { it is JsonObject }) return null
                itemList = element
                productList = emptyList()
            }
            else -> return null
        }
        var skipped = 0
        val items = itemList.mapNotNull { e -> item(e).also { if (it == null) skipped++ } }
        val products = productList.mapNotNull { e -> product(e).also { if (it == null) skipped++ } }
        return AiParsed(items, products, skipped)
    }

    private fun item(e: JsonElement): AiItem? {
        val o = e as? JsonObject ?: return null
        val name = name(o) ?: return null
        val rawAmount = o["amount"]?.takeUnless { empty(it) }
        val amount = rawAmount?.let { number(it) ?: return null }
        if (amount != null && amount <= 0) return null
        val kcal = o["kcal"]?.takeUnless { empty(it) }?.let { number(it) ?: return null }
        if (kcal != null && kcal < 0) return null
        val unit = unit(o["unit"]) ?: AmountUnit.G
        return AiItem(name, amount?.let { Amount(it, unit) }, kcal, flag(o["herkku"]))
    }

    private fun product(e: JsonElement): AiProduct? {
        val o = e as? JsonObject ?: return null
        val name = name(o) ?: return null
        val kcal = o["kcal"]?.takeUnless { empty(it) }?.let { number(it) } ?: return null
        if (kcal < 0) return null
        val per = when (text(o["per"])?.lowercase()?.replace(" ", "")) {
            null, "", "100g" -> AmountUnit.G
            "kpl", "annos" -> AmountUnit.KPL
            else -> return null
        }
        val energy = Energy(kcal, per)
        val amount = o["amount"]?.takeUnless { empty(it) }?.let { number(it) }?.takeIf { it > 0 }
        val usual = amount?.let { Amount(it, unit(o["unit"]) ?: per) }?.takeIf { it.unit == per }
        return AiProduct(name, energy, usual, flag(o["herkku"]))
    }

    private fun name(o: JsonObject): String? = text(o["name"])?.let { FoodNames.clean(it) }?.takeIf { it.isNotEmpty() }

    private fun text(e: JsonElement?): String? = (e as? JsonPrimitive)?.content

    private fun empty(e: JsonElement): Boolean = e is JsonPrimitive && (e.content.isBlank() || e.content == "null")

    private fun number(e: JsonElement): Double? {
        val p = e as? JsonPrimitive ?: return null
        return p.content.trim().replace(',', '.').replace(" ", "").toDoubleOrNull()?.takeIf { it.isFinite() }
    }

    private fun unit(e: JsonElement?): AmountUnit? = when (text(e)?.trim()?.lowercase()) {
        "g" -> AmountUnit.G
        "kpl", "annos" -> AmountUnit.KPL
        else -> null
    }

    private fun flag(e: JsonElement?): Boolean = text(e)?.trim()?.lowercase() in setOf("true", "kyllä", "kylla")
}

/** What an import would write: the items, the products to put, and the products as they were (undo). */
data class AiImportPlan(val items: List<FoodItem>, val products: List<Product>, val before: Map<String, Product?>)

/** Planning an AI import with the add and product rules (food-log "Saving an AI import"). Append only. */
object AiImport {
    fun plan(
        items: List<AiItem>,
        products: List<AiProduct>,
        day: LocalDate,
        dayItems: Collection<FoodItem>,
        own: List<Product>,
        base: List<BaseProduct>,
        now: Instant,
        zone: ZoneId,
        newUuid: () -> String,
    ): AiImportPlan {
        val working = linkedMapOf<String, Product>()
        own.forEach { working[it.key] = it }
        val before = linkedMapOf<String, Product?>()
        fun touch(key: String) {
            if (key !in before) before[key] = own.firstOrNull { it.key == key }
        }
        // The chat's explicit list first: it replaces or creates.
        for (p in products) {
            val key = FoodNames.key(p.name)
            if (p.name == QUICK_ADD || key == FoodNames.key(QUICK_ADD)) continue
            touch(key)
            val old = working[key]
            val usual = p.usual ?: old?.lastAmount?.takeIf { it.unit == p.energy.per }
            working[key] = Product(old?.name ?: p.name, p.treat, p.energy, usual)
        }
        val out = mutableListOf<FoodItem>()
        var pos = FoodDay.nextPos(dayItems)
        for (ai in items) {
            val key = FoodNames.key(ai.name)
            val quick = key == FoodNames.key(QUICK_ADD)
            var product: Product? = null
            if (!quick) {
                val known = working.values.toList()
                val resolved = resolveName(ai.name, known, base)
                product = resolved.product
                touch(product.key)
                // A new product (not own, not base) takes the item's category.
                if (resolved is Suggestion.Own && known.none { it.key == product.key }) product = product.copy(treat = ai.treat)
                // An item teaches only a product without energy, from an amount with kcal.
                if (product.energy == null && ai.amount != null && ai.kcal != null) {
                    val per = ai.amount.unit
                    val kcal = if (per == AmountUnit.G) ai.kcal * 100.0 / ai.amount.value else ai.kcal / ai.amount.value
                    product = product.copy(energy = Energy(kcal, per), lastAmount = ai.amount)
                }
                working[product.key] = product
            }
            var amount = ai.amount
            var kcal = ai.kcal
            val energy = product?.energy
            if (kcal == null && energy != null) {
                amount = amount?.takeIf { it.unit == energy.per } ?: product.usualAmount()?.takeIf { it.unit == energy.per }
                kcal = amount?.let { energy.kcalOf(it) }
            }
            val (time, timed) = FoodDay.timeFor(day, pos, now, zone)
            out += FoodItem(
                uuid = newUuid(),
                day = day,
                pos = pos,
                name = product?.name ?: QUICK_ADD,
                kcal = kcal,
                amount = amount,
                treat = (product?.treat ?: false) || ai.treat,
                time = time,
                zoneOffset = zone.rules.getOffset(time),
                timed = timed,
            )
            pos++
        }
        val changed = before.keys.mapNotNull { k -> working[k]?.takeIf { it != before[k] } }
        return AiImportPlan(out, changed, before.filterKeys { k -> changed.any { it.key == k } })
    }
}
