package fi.jukkakot.terveyslappu.food

import fi.jukkakot.terveyslappu.auto.JsonFileValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

/** The own products (`products.json` in app storage, carried by Android backup and the data file). */
class ProductStore(file: File? = null) : JsonFileValue<List<Product>>(file, ListSerializer(Product.serializer()), emptyList()) {
    fun all(): List<Product> = get()

    fun find(name: String): Product? = FoodNames.key(name).let { k -> get().firstOrNull { it.key == k } }

    /** Adds or replaces the product with [product]'s name; [QUICK_ADD] is never a product. */
    fun put(product: Product) {
        if (product.name == QUICK_ADD) return
        update { old -> old.filter { it.key != product.key } + product }
    }

    /** Replaces [old]'s entry (a rename keeps its place); false when the new name is another product's. */
    fun replace(oldKey: String, product: Product): Boolean {
        val clash = get().any { it.key == product.key && it.key != oldKey }
        if (clash) return false
        update { list ->
            val i = list.indexOfFirst { it.key == oldKey }
            if (i < 0) list + product else list.toMutableList().also { it[i] = product }
        }
        return true
    }

    /** Kumoa of an AI import: each product back as it was ([previous] null = it did not exist). */
    fun restore(previous: Map<String, Product?>) {
        update { old -> old.filter { it.key !in previous } + previous.values.filterNotNull() }
    }

    fun remove(key: String) {
        update { old -> old.filter { it.key != key } }
    }

    /** Import: adds the products whose name is not yet an own product (the phone's product wins); returns how many. */
    fun addMissing(products: Collection<Product>): Int {
        var added = 0
        update { old ->
            val keys = old.map { it.key }.toMutableSet()
            old + products.filter { it.name != QUICK_ADD && keys.add(it.key) }.also { added = it.size }
        }
        return added
    }
}

/** The shipped base products (design 3): generic foods from open sources and generic estimates. */
object BaseProducts {
    @Serializable
    private data class Row(
        val name: String,
        val aliases: List<String> = emptyList(),
        val per: String,
        val kcal: Double,
        val portion: Double,
        val treat: Boolean = false,
    )

    private val json = Json { ignoreUnknownKeys = true }

    fun parse(text: String): List<BaseProduct> = json.decodeFromString(ListSerializer(Row.serializer()), text).map { row ->
        val unit = when (row.per) {
            "100g" -> AmountUnit.G
            "kpl" -> AmountUnit.KPL
            else -> error("unknown per ${row.per} for ${row.name}")
        }
        BaseProduct(row.name, row.aliases, Energy(row.kcal, unit), Amount(row.portion, unit), row.treat)
    }
}
