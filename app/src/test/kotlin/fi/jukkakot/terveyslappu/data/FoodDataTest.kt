package fi.jukkakot.terveyslappu.data

import fi.jukkakot.terveyslappu.food.Amount
import fi.jukkakot.terveyslappu.food.AmountUnit
import fi.jukkakot.terveyslappu.food.Energy
import fi.jukkakot.terveyslappu.food.FoodItem
import fi.jukkakot.terveyslappu.food.FoodRecords
import fi.jukkakot.terveyslappu.food.Product
import fi.jukkakot.terveyslappu.food.ProductStore
import fi.jukkakot.terveyslappu.food.importFoods
import fi.jukkakot.terveyslappu.history.HistoryParser
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Made-up foods and weights only (NFR-1). */
class FoodDataTest {
    private val zone = ZoneId.of("Europe/Helsinki")
    private val now = Instant.parse("2026-11-14T18:00:00Z")
    private val day = LocalDate.of(2026, 11, 13)

    private val foods = listOf(
        FoodItem("u1", day, 1, "Rahka", 130.0, Amount(200.0, AmountUnit.G), false, Instant.parse("2026-11-13T05:10:00Z"), ZoneOffset.ofHours(2), true),
        FoodItem("u2", day, 2, "Suklaa; tumma", 230.0, Amount(1.5, AmountUnit.KPL), true, Instant.parse("2026-11-13T08:05:00Z"), ZoneOffset.ofHours(2), true),
        FoodItem("u3", day, 3, "Työpaikan pizza", null, null, false, day.atTime(12, 0, 3).atZone(zone).toInstant(), ZoneOffset.ofHours(2), false),
    )
    private val products = listOf(
        Product("Rahka", energy = Energy.per100g(65.0), lastAmount = Amount(200.0, AmountUnit.G)),
        Product("Suklaa; tumma", treat = true, energy = Energy.perPiece(153.3)),
    )
    private val set = DataSet(listOf(ExportRow(Instant.parse("2026-11-13T05:00:00Z"), ZoneOffset.ofHours(2), 82.4, "Terveyslappu")), foods, products)

    private fun readBoth(): List<Pair<String, FoodParts>> = listOf(
        "csv" to HistoryParser.parse(CsvData.write(set, zone), zone, now).also { assertTrue(it.unknownKinds.isEmpty()) }.foods,
        "json" to JsonData.read(JsonData.write(set, zone), now, zone).also { assertTrue(it.unknownKinds.isEmpty()) }.foods,
    )

    @Test
    fun `foods and products round trip in both formats`() {
        for ((name, parts) in readBoth()) {
            assertEquals(foods.map { listOf(it.uuid, it.day, it.pos, it.name, it.kcal, it.amount, it.treat, it.timed) },
                parts.foods.map { listOf(it.uuid, it.day, it.pos, it.name, it.kcal, it.amount, it.treat, it.timed) }, name)
            assertEquals(foods.map { it.time }, parts.foods.map { it.time }, name)
            assertEquals(products, parts.products, name)
        }
    }

    @Test
    fun `a file with weights only changes no foods or products`() {
        val weightsOnly = DataSet(set.weights)
        val csv = HistoryParser.parse(CsvData.write(weightsOnly, zone), zone, now)
        val json = JsonData.read(JsonData.write(weightsOnly, zone), now, zone)
        for (parsed in listOf(csv, json)) {
            assertEquals(1, parsed.weights.size)
            assertEquals(FoodParts(), parsed.foods)
        }
    }

    private class FakeRecords(var permitted: Boolean = true) : FoodRecords {
        val stored = mutableListOf<FoodItem>()
        override suspend fun granted() = permitted
        override suspend fun own(now: Instant) = stored.toList()
        override suspend fun insert(items: List<FoodItem>) {
            stored += items
        }
    }

    @Test
    fun `importing twice writes each item and product once`() = runBlocking {
        val parts = readBoth().last().second
        val records = FakeRecords()
        val store = ProductStore()
        assertEquals(3, importFoods(parts, records, store, now).written)
        val again = importFoods(parts, records, store, now)
        assertEquals(0, again.written)
        assertEquals(0, again.products)
        assertEquals(3, records.stored.size)
        assertEquals(2, store.all().size)
    }

    @Test
    fun `without the permission foods are not written but products are added`() = runBlocking {
        val result = importFoods(FoodParts(foods, products), FakeRecords(permitted = false), ProductStore(), now)
        assertEquals(0, result.written)
        assertEquals(3, result.notWritten)
        assertEquals(2, result.products)
    }
}
