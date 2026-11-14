package fi.jukkakot.terveyslappu.data

import fi.jukkakot.terveyslappu.food.Amount
import fi.jukkakot.terveyslappu.food.AmountUnit
import fi.jukkakot.terveyslappu.food.Energy
import fi.jukkakot.terveyslappu.food.FoodItem
import fi.jukkakot.terveyslappu.food.Product
import fi.jukkakot.terveyslappu.history.Table
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.put
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

/** The foods and products read from a data file (data-file "Food parts"). */
data class FoodParts(val foods: List<FoodItem> = emptyList(), val products: List<Product> = emptyList())

/**
 * The data file's `foods` and `products` parts in both formats (data-file "Food parts"). A food
 * carries its day, logging time when known, position, name, kcal, amount and unit, `treat` for a
 * Herkku item and its uuid; a product its name, category, energy and last used amount.
 */
object FoodData {
    const val FOODS = "foods"
    const val PRODUCTS = "products"
    const val FOOD_HEADER = "date;time;pos;name;kcal;amount;unit;treat;uuid"
    const val PRODUCT_HEADER = "name;category;per;kcal;amount;unit"
    private const val TREAT = "herkku"
    private const val FOOD = "ruoka"

    private val date = DateTimeFormatter.ISO_LOCAL_DATE
    private val time = DateTimeFormatter.ofPattern("HH:mm:ssxxx")

    /** Oldest first: by day, then position, then time. */
    fun sorted(foods: Collection<FoodItem>): List<FoodItem> =
        foods.sortedWith(compareBy<FoodItem> { it.day }.thenBy { it.pos == null }.thenBy { it.pos ?: 0 }.thenBy { it.time })

    fun csv(set: DataSet): String = buildString {
        if (set.foods.isNotEmpty()) {
            append(CsvData.TABLE_PREFIX).append(FOODS).append('\n').append(FOOD_HEADER).append('\n')
            for (f in sorted(set.foods)) {
                append(date.format(f.day)).append(';')
                if (f.timed) append(time.format(f.time.atOffset(f.zoneOffset)))
                append(';').append(f.pos ?: "").append(';')
                append(quote(f.name)).append(';')
                append(f.kcal?.let(::num) ?: "").append(';')
                append(f.amount?.let { num(it.value) } ?: "").append(';')
                append(f.amount?.let { unit(it.unit) } ?: "").append(';')
                append(if (f.treat) "1" else "").append(';')
                append(f.uuid).append('\n')
            }
        }
        if (set.products.isNotEmpty()) {
            append(CsvData.TABLE_PREFIX).append(PRODUCTS).append('\n').append(PRODUCT_HEADER).append('\n')
            for (p in set.products.sortedBy { it.key }) {
                append(quote(p.name)).append(';')
                append(if (p.treat) TREAT else FOOD).append(';')
                append(p.energy?.let { per(it.per) } ?: "").append(';')
                append(p.energy?.let { num(it.kcal) } ?: "").append(';')
                append(p.lastAmount?.let { num(it.value) } ?: "").append(';')
                append(p.lastAmount?.let { unit(it.unit) } ?: "").append('\n')
            }
        }
    }

    /** Reads the `foods` and `products` tables of a CSV data file; other lines are ignored. */
    fun readCsv(text: String, zone: ZoneId): FoodParts {
        val foods = mutableListOf<FoodItem>()
        val products = mutableListOf<Product>()
        var kind: String? = null
        var header: List<String>? = null
        for (raw in text.removePrefix("\uFEFF").lines()) {
            val line = raw.trimEnd('\r')
            if (line.isBlank()) continue
            if (line.startsWith("#")) {
                kind = line.trim().takeIf { it.startsWith(CsvData.TABLE_PREFIX) }?.removePrefix(CsvData.TABLE_PREFIX)?.trim()
                header = null
                continue
            }
            if (kind != FOODS && kind != PRODUCTS) continue
            val fields = Table.split(line, ';')
            if (header == null) {
                header = fields.map { it.trim().lowercase() }
                continue
            }
            val cols = header
            fun col(name: String) = cols.indexOf(name).takeIf { it >= 0 }?.let { fields.getOrNull(it)?.trim() }?.takeIf { it.isNotEmpty() }
            if (kind == FOODS) {
                runCatching {
                    food(
                        day = LocalDate.parse(col("date")),
                        time = col("time")?.let { OffsetTime.parse(it) },
                        pos = col("pos")?.toIntOrNull(),
                        name = col("name") ?: return@runCatching null,
                        kcal = col("kcal")?.let(::parse),
                        amount = amount(col("amount")?.let(::parse), col("unit")),
                        treat = col("treat") == "1",
                        uuid = col("uuid"),
                        zone = zone,
                    )
                }.getOrNull()?.let(foods::add)
            } else {
                val name = col("name") ?: continue
                products += Product(
                    name = name,
                    treat = col("category") == TREAT,
                    energy = energy(col("kcal")?.let(::parse), col("per")),
                    lastAmount = amount(col("amount")?.let(::parse), col("unit")),
                )
            }
        }
        return FoodParts(foods, products)
    }

    fun JsonObjectBuilder.putJson(set: DataSet, fallbackZone: ZoneId) {
        if (set.foods.isNotEmpty()) {
            put(
                FOODS,
                buildJsonArray {
                    for (f in sorted(set.foods)) {
                        add(
                            buildJsonObject {
                                put("day", date.format(f.day))
                                if (f.timed) put("time", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(f.time.atOffset(f.zoneOffset)))
                                f.pos?.let { put("pos", it) }
                                put("name", f.name)
                                f.kcal?.let { put("kcal", round(it)) }
                                f.amount?.let {
                                    put("amount", round(it.value))
                                    put("unit", unit(it.unit))
                                }
                                if (f.treat) put("treat", true)
                                put("uuid", f.uuid)
                            },
                        )
                    }
                },
            )
        }
        if (set.products.isNotEmpty()) {
            put(
                PRODUCTS,
                buildJsonArray {
                    for (p in set.products.sortedBy { it.key }) {
                        add(
                            buildJsonObject {
                                put("name", p.name)
                                put("category", if (p.treat) TREAT else FOOD)
                                p.energy?.let {
                                    put("kcal", round(it.kcal))
                                    put("per", per(it.per))
                                }
                                p.lastAmount?.let {
                                    put("amount", round(it.value))
                                    put("unit", unit(it.unit))
                                }
                            },
                        )
                    }
                },
            )
        }
    }

    fun readJson(root: JsonObject, zone: ZoneId): FoodParts {
        val foods = (root[FOODS] as? JsonArray).orEmpty().mapNotNull { el ->
            val o = el as? JsonObject ?: return@mapNotNull null
            runCatching {
                val time = o.str("time")?.let { OffsetDateTime.parse(it) }
                food(
                    day = o.str("day")?.let { LocalDate.parse(it) } ?: time!!.toLocalDate(),
                    time = time?.toOffsetTime(),
                    pos = (o["pos"] as? JsonPrimitive)?.intOrNull,
                    name = o.str("name") ?: return@runCatching null,
                    kcal = o.num("kcal"),
                    amount = amount(o.num("amount"), o.str("unit")),
                    treat = (o["treat"] as? JsonPrimitive)?.booleanOrNull == true,
                    uuid = o.str("uuid"),
                    zone = zone,
                )
            }.getOrNull()
        }
        val products = (root[PRODUCTS] as? JsonArray).orEmpty().mapNotNull { el ->
            val o = el as? JsonObject ?: return@mapNotNull null
            val name = o.str("name") ?: return@mapNotNull null
            Product(name, o.str("category") == TREAT, energy(o.num("kcal"), o.str("per")), amount(o.num("amount"), o.str("unit")))
        }
        return FoodParts(foods, products)
    }

    private fun food(
        day: LocalDate,
        time: OffsetTime?,
        pos: Int?,
        name: String,
        kcal: Double?,
        amount: Amount?,
        treat: Boolean,
        uuid: String?,
        zone: ZoneId,
    ): FoodItem {
        val at = time?.atDate(day)?.toInstant()
            ?: day.atTime(LocalTime.NOON).atZone(zone).toInstant().plusSeconds((pos ?: 0).toLong())
        return FoodItem(
            uuid = uuid ?: UUID.randomUUID().toString(),
            day = day,
            pos = pos?.takeIf { it > 0 },
            name = name,
            kcal = kcal?.takeIf { it >= 0 },
            amount = amount,
            treat = treat,
            time = at,
            zoneOffset = time?.offset ?: zone.rules.getOffset(at),
            timed = time != null,
        )
    }

    private fun amount(value: Double?, unit: String?): Amount? {
        val u = when (unit) {
            "g" -> AmountUnit.G
            "kpl" -> AmountUnit.KPL
            else -> return null
        }
        return value?.takeIf { it > 0 }?.let { Amount(it, u) }
    }

    private fun energy(kcal: Double?, per: String?): Energy? {
        val u = when (per) {
            "100g" -> AmountUnit.G
            "kpl" -> AmountUnit.KPL
            else -> return null
        }
        return kcal?.takeIf { it >= 0 }?.let { Energy(it, u) }
    }

    private fun unit(u: AmountUnit) = if (u == AmountUnit.G) "g" else "kpl"

    private fun per(u: AmountUnit) = if (u == AmountUnit.G) "100g" else "kpl"

    private fun round(v: Double): Double = BigDecimal.valueOf(v).setScale(1, RoundingMode.HALF_UP).stripTrailingZeros().toDouble()

    /** Comma decimals like the weights: 130 → `130`, 1.5 → `1,5`. */
    private fun num(v: Double): String = BigDecimal.valueOf(v).setScale(1, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString().replace('.', ',')

    private fun parse(text: String): Double? = text.replace(',', '.').toDoubleOrNull()

    private fun quote(text: String): String =
        if (text.any { it == ';' || it == '"' || it == '\n' }) "\"" + text.replace("\"", "\"\"").replace('\n', ' ') + "\"" else text

    private fun JsonObject.str(key: String): String? = (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.content

    private fun JsonObject.num(key: String): Double? = (this[key] as? JsonPrimitive)?.doubleOrNull

    private fun JsonArray?.orEmpty(): List<JsonElement> = this ?: emptyList()
}
