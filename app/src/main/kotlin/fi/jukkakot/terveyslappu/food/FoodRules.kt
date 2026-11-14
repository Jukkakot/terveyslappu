package fi.jukkakot.terveyslappu.food

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.abs
import kotlin.math.min

/** Name keys and forgiving matching (kauppalappu's rules, food-log "Forgiving search"). */
object FoodNames {
    private val spaces = Regex("\\s+")

    /** Lower case, ä/å → a, ö → o, single spaces, trimmed. */
    fun normalize(text: String): String = text.trim().lowercase()
        .replace('ä', 'a').replace('å', 'a').replace('ö', 'o')
        .replace(spaces, " ")

    /** The key products are told apart by (case and extra spaces ignored). */
    fun key(name: String): String = normalize(name)

    /** The name as typed, trimmed and with single spaces. */
    fun clean(name: String): String = name.trim().replace(spaces, " ")

    /**
     * How well [query] matches [name], lower is better, null for no match: 0 the name starts with
     * it, 1 a word does, 2 it is inside a word (a compound part, three letters up), 3 its pieces of
     * three letters up start a word and follow in order ("jaulihak" → jauhelihakastike), 4 one wrong,
     * missing or extra letter against a word start (four letters up).
     */
    fun rank(query: String, name: String): Int? {
        val q = normalize(query)
        val n = normalize(name)
        if (q.isEmpty() || n.isEmpty()) return null
        if (n.startsWith(q)) return 0
        val words = n.split(' ', '-', '/').filter { it.isNotEmpty() }
        if (words.any { it.startsWith(q) }) return 1
        if (q.length >= 3 && n.contains(q)) return 2
        if (q.length >= 6 && pieces(q, n, 0, atWordStart = true, left = 3)) return 3
        if (q.length >= 4 && words.any { w -> (q.length - 1..q.length + 1).any { k -> k <= w.length && oneEdit(q, w.substring(0, k)) } }) return 4
        return null
    }

    /** The best rank over a name and its other names. */
    fun rank(query: String, names: List<String>): Int? = names.mapNotNull { rank(query, it) }.minOrNull()

    private fun pieces(q: String, n: String, from: Int, atWordStart: Boolean, left: Int): Boolean {
        if (q.isEmpty()) return true
        if (left == 0) return false
        for (len in q.length downTo 3) {
            val rest = q.length - len
            if (rest in 1..2) continue
            val piece = q.substring(0, len)
            var i = n.indexOf(piece, from)
            while (i >= 0) {
                val ok = !atWordStart || i == 0 || n[i - 1] == ' '
                if (ok && pieces(q.substring(len), n, i + len, atWordStart = false, left = left - 1)) return true
                i = n.indexOf(piece, i + 1)
            }
        }
        return false
    }

    /** At most one substitution, insertion or deletion apart. */
    fun oneEdit(a: String, b: String): Boolean {
        if (abs(a.length - b.length) > 1) return false
        var i = 0
        var j = 0
        var edits = 0
        while (i < a.length && j < b.length) {
            if (a[i] == b[j]) {
                i++
                j++
                continue
            }
            if (++edits > 1) return false
            when {
                a.length > b.length -> i++
                a.length < b.length -> j++
                else -> {
                    i++
                    j++
                }
            }
        }
        return edits + (a.length - i) + (b.length - j) <= 1
    }
}

/** What enter in the add field means (food-log "Adding an item"). */
sealed interface AddInput {
    /** A bare integer: an item [QUICK_ADD] with that many kcal. */
    data class Quick(val kcal: Int) : AddInput

    /** Any other text: an item of that name. */
    data class Name(val name: String) : AddInput

    companion object {
        private val integer = Regex("^\\d{1,6}$")

        /** Null for an empty field (enter does nothing). */
        fun parse(text: String): AddInput? {
            val t = text.trim()
            if (t.isEmpty()) return null
            if (integer.matches(t)) return t.toInt().takeIf { it > 0 }?.let { Quick(it) } ?: Name(t)
            return Name(FoodNames.clean(t))
        }
    }
}

/** A suggestion: an own product, or a base product that becomes one when logged. */
sealed interface Suggestion {
    val name: String
    val product: Product

    data class Own(override val product: Product) : Suggestion {
        override val name get() = product.name
    }

    data class Base(val base: BaseProduct) : Suggestion {
        override val name get() = base.name
        override val product get() = base.toProduct()
    }
}

/** The product a typed name logs as: exact own, exact base, else a new product without kcal. */
fun resolveName(name: String, own: List<Product>, base: List<BaseProduct>): Suggestion {
    val key = FoodNames.key(name)
    own.firstOrNull { it.key == key }?.let { return Suggestion.Own(it) }
    base.firstOrNull { b -> FoodNames.key(b.name) == key || b.aliases.any { FoodNames.key(it) == key } }?.let { return Suggestion.Base(it) }
    return Suggestion.Own(Product(FoodNames.clean(name)))
}

/** The add field's suggestions (food-log "Adding an item", design 4). */
object Suggestions {
    const val EMPTY_LIMIT = 8
    const val TYPING_LIMIT = 5
    private const val WINDOW_DAYS = 30L
    private const val NEAR_MINUTES = 120

    /**
     * At the empty field: own products by distinct days logged within ±2 h of [now]'s clock time in
     * the last 30 days (untimed items left out of this), then by distinct days logged in those 30
     * days, ties by the latest log; the remaining own products follow by their latest log.
     */
    fun atEmpty(items: List<FoodItem>, own: List<Product>, now: ZonedDateTime, limit: Int = EMPTY_LIMIT): List<Product> {
        val since = now.toInstant().minus(Duration.ofDays(WINDOW_DAYS))
        val zone = now.zone
        val clock = now.toLocalTime()
        val byKey = items.filter { it.name != QUICK_ADD }.groupBy { it.key }
        data class Score(val near: Int, val all: Int, val last: Instant?)
        val scores = own.filter { it.name != QUICK_ADD }.associateWith { p ->
            val logs = byKey[p.key].orEmpty()
            val recent = logs.filter { !it.time.isBefore(since) }
            Score(
                near = recent.filter { it.timed && minutesApart(it.time.atZone(zone).toLocalTime(), clock) <= NEAR_MINUTES }.map { it.day }.toSet().size,
                all = recent.map { it.day }.toSet().size,
                last = logs.maxOfOrNull { it.time },
            )
        }
        return scores.entries
            .sortedWith(compareByDescending<Map.Entry<Product, Score>> { it.value.near }.thenByDescending { it.value.all }.thenByDescending { it.value.last ?: Instant.EPOCH })
            .take(limit)
            .map { it.key }
    }

    /** While typing: matching own products first, then base products not yet own; at most five. */
    fun typing(query: String, own: List<Product>, base: List<BaseProduct>, items: List<FoodItem>, limit: Int = TYPING_LIMIT): List<Suggestion> {
        if (FoodNames.normalize(query).isEmpty()) return emptyList()
        val last = items.groupBy { it.key }.mapValues { (_, v) -> v.maxOf { it.time } }
        val ownKeys = own.map { it.key }.toSet()
        val ownHits = own.filter { it.name != QUICK_ADD }.mapNotNull { p -> FoodNames.rank(query, p.name)?.let { p to it } }
            .sortedWith(compareBy<Pair<Product, Int>> { it.second }.thenByDescending { last[it.first.key] ?: Instant.EPOCH })
            .map { Suggestion.Own(it.first) }
        val baseHits = base.filter { FoodNames.key(it.name) !in ownKeys }
            .mapNotNull { b -> FoodNames.rank(query, listOf(b.name) + b.aliases)?.let { b to it } }
            .sortedWith(compareBy<Pair<BaseProduct, Int>> { it.second }.thenBy { it.first.name.length })
            .map { Suggestion.Base(it.first) }
        return (ownHits + baseHits).take(limit)
    }

    private fun minutesApart(a: LocalTime, b: LocalTime): Int {
        val d = abs(a.toSecondOfDay() - b.toSecondOfDay()) / 60
        return min(d, 24 * 60 - d)
    }
}

/** Fill-later, order and time rules of a day's items (design 5, 6 and the review decisions). */
object FoodDay {
    /** Items of one day in eating order: positioned ones by position, unknown-id ones after them by time. */
    fun ordered(items: Collection<FoodItem>): List<FoodItem> =
        items.sortedWith(compareBy<FoodItem> { it.pos == null }.thenBy { it.pos ?: 0 }.thenBy { it.time })

    fun nextPos(dayItems: Collection<FoodItem>): Int = (dayItems.mapNotNull { it.pos }.maxOrNull() ?: 0) + 1

    /** [order] renumbered 1..n; only the items whose position changed. */
    fun renumbered(order: List<FoodItem>): List<FoodItem> =
        order.mapIndexedNotNull { i, item -> if (item.pos == i + 1) null else item.copy(pos = i + 1) }

    /**
     * When a new item on [day] is written: the logging time [now] on its own day (shown), else that
     * day at 12:00 plus [pos] seconds (never shown).
     */
    fun timeFor(day: LocalDate, pos: Int, now: Instant, zone: ZoneId): Pair<Instant, Boolean> =
        if (now.atZone(zone).toLocalDate() == day) {
            now to true
        } else {
            day.atTime(12, 0).atZone(zone).toInstant().plusSeconds(pos.toLong()) to false
        }

    /**
     * Energy given to one item of a product (food-log "Kcal filled in later"): the product's items
     * without kcal get it; an item without an amount takes one piece, or [fallback] for grams.
     * Returns only the changed items; items that have kcal never change.
     */
    fun fill(items: Collection<FoodItem>, productKey: String, energy: Energy, fallback: Amount?): List<FoodItem> =
        items.filter { it.key == productKey && it.kcal == null }.mapNotNull { item ->
            val amount = item.amount?.takeIf { it.unit == energy.per }
                ?: fallback?.takeIf { it.unit == energy.per }
                ?: if (energy.per == AmountUnit.KPL) Amount.ONE_PIECE else null
            amount?.let { a -> energy.kcalOf(a)?.let { item.copy(kcal = it, amount = a) } }
        }
}
