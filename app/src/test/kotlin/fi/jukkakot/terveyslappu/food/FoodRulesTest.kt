package fi.jukkakot.terveyslappu.food

import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Made-up foods only (NFR-1). */
class FoodRulesTest {
    private val zone = ZoneId.of("Europe/Helsinki")
    private val today = LocalDate.of(2026, 11, 14)
    private fun at(day: LocalDate, h: Int, m: Int = 0) = day.atTime(h, m).atZone(zone).toInstant()

    private fun item(name: String, day: LocalDate = today, pos: Int? = 1, kcal: Double? = null, amount: Amount? = null, treat: Boolean = false, h: Int = 12, timed: Boolean = true, uuid: String = "$name-$day-$pos") =
        FoodItem(uuid, day, pos, name, kcal, amount, treat, at(day, h), zone.rules.getOffset(at(day, h)), timed)

    private val base = listOf(
        BaseProduct("Kaurapuuro", listOf("Puuro"), Energy.per100g(60.0), Amount(300.0, AmountUnit.G), false),
        BaseProduct("Jauhelihakastike", emptyList(), Energy.per100g(140.0), Amount(250.0, AmountUnit.G), false),
    )

    @Test
    fun `kcal from per 100 g and per piece, unknown for the wrong unit`() {
        assertEquals(350.0, Energy.per100g(140.0).kcalOf(Amount(250.0, AmountUnit.G)))
        assertEquals(125.0, Energy.perPiece(25.0).kcalOf(Amount(5.0, AmountUnit.KPL)))
        assertNull(Energy.perPiece(25.0).kcalOf(Amount(100.0, AmountUnit.G)))
        assertEquals(Amount.ONE_PIECE, Product("Pulla", energy = Energy.perPiece(250.0)).usualAmount())
        assertNull(Product("Pizza").usualAmount())
    }

    @Test
    fun `names ignore case, spaces and a-umlaut`() {
        assertEquals(FoodNames.key("Työpaikan  pizza "), FoodNames.key("tyopaikan pizza"))
    }

    @Test
    fun `forgiving search`() {
        assertEquals(0, FoodNames.rank("kaurapuu", "Kaurapuuro"))
        assertEquals(1, FoodNames.rank("pizza", "Työpaikan pizza"))
        assertEquals(2, FoodNames.rank("kastike", "Jauhelihakastike"))
        assertNotNull(FoodNames.rank("jaulihak", "Jauhelihakastike"))
        assertNotNull(FoodNames.rank("kaurapiuro", "Kaurapuuro"), "one wrong letter from four letters")
        assertNull(FoodNames.rank("kau", "Rahka"))
        assertNull(FoodNames.rank("xyz", "Rahka"))
        assertNull(FoodNames.rank("rahkx", "Pulla"))
    }

    @Test
    fun `enter adds a bare integer as kcal and anything else as a name`() {
        assertEquals(AddInput.Quick(350), AddInput.parse(" 350 "))
        assertEquals(AddInput.Name("työpaikan pizza"), AddInput.parse("työpaikan  pizza"))
        assertEquals(AddInput.Name("1,5"), AddInput.parse("1,5"))
        assertNull(AddInput.parse("  "))
    }

    @Test
    fun `a typed name is its own product, else an exact base product, else new without kcal`() {
        val own = listOf(Product("Rahka", energy = Energy.per100g(65.0), lastAmount = Amount(200.0, AmountUnit.G)))
        assertEquals(own[0], resolveName("rahka", own, base).product)
        assertEquals("Kaurapuuro", resolveName("puuro", own, base).product.name)
        val new = resolveName("Työpaikan pizza", own, base).product
        assertEquals("Työpaikan pizza", new.name)
        assertNull(new.energy)
        // Fuzzy matches apply only from a suggestion button.
        assertNull(resolveName("kaurapuu", own, base).product.energy)
    }

    @Test
    fun `suggestions at the empty field prefer this time of day, then all logs`() {
        val own = listOf(Product("Rahka"), Product("Pizza"), Product("Pulla"))
        val items = (1..5).map { item("Rahka", today.minusDays(it.toLong()), h = 7, uuid = "r$it") } +
            (1..8).map { item("Pizza", today.minusDays(it.toLong()), h = 18, uuid = "p$it") } +
            listOf(item("Pulla", today.minusDays(40), h = 7, uuid = "old")) +
            listOf(item(QUICK_ADD, today.minusDays(1), h = 7, uuid = "q"))
        val morning = Suggestions.atEmpty(items, own, ZonedDateTime.of(today.atTime(7, 20), zone))
        assertEquals(listOf("Rahka", "Pizza", "Pulla"), morning.map { it.name })
        val evening = Suggestions.atEmpty(items, own, ZonedDateTime.of(today.atTime(19, 0), zone))
        assertEquals("Pizza", evening.first().name)
    }

    @Test
    fun `untimed items do not count for the time of day`() {
        val own = listOf(Product("Rahka"), Product("Lasagne"))
        val items = listOf(item("Lasagne", today.minusDays(1), h = 7, timed = false, uuid = "l1"), item("Lasagne", today.minusDays(2), h = 7, timed = false, uuid = "l2"), item("Rahka", today.minusDays(3), h = 7, uuid = "r"))
        assertEquals("Rahka", Suggestions.atEmpty(items, own, ZonedDateTime.of(today.atTime(7, 0), zone)).first().name)
    }

    @Test
    fun `typing shows own products before base products, at most five`() {
        val own = listOf(Product("Kaurakeksi"))
        val hits = Suggestions.typing("kaura", own, base, emptyList())
        assertEquals(listOf("Kaurakeksi", "Kaurapuuro"), hits.map { it.name })
        assertTrue(hits[1] is Suggestion.Base)
        val many = (1..9).map { Product("Kaura $it") }
        assertEquals(5, Suggestions.typing("kaura", many, base, emptyList()).size)
    }

    @Test
    fun `first use logs a typical portion of a base product`() {
        val hit = Suggestions.typing("kaurapuu", emptyList(), base, emptyList()).single()
        val (logged, product) = FoodEdits.add(today, emptyList(), hit, null, at(today, 7), zone, "u")
        assertEquals(Amount(300.0, AmountUnit.G), logged.amount)
        assertEquals(180.0, logged.kcal)
        assertEquals("Kaurapuuro", product?.name)
    }

    @Test
    fun `fill later fills the product's items without kcal only`() {
        val items = listOf(
            item("Työpaikan pizza", today.minusDays(2), uuid = "mon"),
            item("Työpaikan pizza", today, uuid = "wed"),
            item("Työpaikan pizza", today.minusDays(5), kcal = 700.0, amount = Amount.ONE_PIECE, uuid = "known"),
            item("Rahka", today, uuid = "other"),
        )
        val filled = FoodDay.fill(items, FoodNames.key("työpaikan pizza"), Energy.perPiece(800.0), null)
        assertEquals(setOf("mon", "wed"), filled.map { it.uuid }.toSet())
        assertTrue(filled.all { it.kcal == 800.0 })
    }

    @Test
    fun `edit with per piece fills the other days and the product`() {
        val mon = item("Työpaikan pizza", today.minusDays(2), uuid = "mon")
        val wed = item("Työpaikan pizza", today, uuid = "wed")
        val plan = FoodEdits.edit(wed, EditInput("Työpaikan pizza", EditMode.KPL, 1.0, 800.0, false), listOf(mon, wed), listOf(Product("Työpaikan pizza")), base)
        assertEquals(setOf("mon", "wed"), plan.new.map { it.uuid }.toSet())
        assertTrue(plan.new.all { it.kcal == 800.0 })
        assertEquals(Energy.perPiece(800.0), plan.product?.energy)
    }

    @Test
    fun `repeat with a count keeps the new amount for the next log`() {
        val product = Product("Suklaapala", treat = true, energy = Energy.perPiece(25.0))
        val (logged, _) = FoodEdits.add(today, emptyList(), Suggestion.Own(product), null, at(today, 10), zone, "s")
        val plan = FoodEdits.edit(logged, EditInput("Suklaapala", EditMode.KPL, 5.0, 25.0, true), listOf(logged), listOf(product), base)
        assertEquals(125.0, plan.new.single().kcal)
        assertEquals(Amount(5.0, AmountUnit.KPL), plan.product?.usualAmount())
    }

    @Test
    fun `a total stays on the item and Herkku rewrites the product's other items`() {
        val a = item("Pulla", today.minusDays(1), kcal = 250.0, amount = Amount.ONE_PIECE, uuid = "a")
        val b = item("Pulla", today, uuid = "b")
        val plan = FoodEdits.edit(b, EditInput("Pulla", EditMode.TOTAL, null, 300.0, true), listOf(a, b), listOf(Product("Pulla", energy = Energy.perPiece(250.0))), base)
        val byId = plan.new.associateBy { it.uuid }
        assertEquals(300.0, byId.getValue("b").kcal)
        assertNull(byId.getValue("b").amount)
        assertTrue(byId.getValue("a").treat)
        assertEquals(250.0, byId.getValue("a").kcal)
        assertEquals(Energy.perPiece(250.0), plan.product?.energy)
    }

    @Test
    fun `day sums, order and the earlier-day time`() {
        val items = listOf(
            item("Pizza", pos = 3, uuid = "3"),
            item("Rahka", pos = 1, kcal = 130.0, uuid = "1"),
            item("Suklaa", pos = 2, kcal = 230.0, treat = true, uuid = "2"),
            item("Vieras", pos = null, kcal = 100.0, h = 6, uuid = "x"),
        )
        assertEquals(listOf("1", "2", "3", "x"), FoodDay.ordered(items).map { it.uuid })
        assertEquals(DaySum(total = 460, treat = 230, withoutKcal = 1, count = 4), DaySum.of(items))
        assertEquals(4, FoodDay.nextPos(items))
        val (time, timed) = FoodDay.timeFor(today.minusDays(1), 4, at(today, 9), zone)
        assertFalse(timed)
        assertEquals(today.minusDays(1).atTime(12, 0, 4).atZone(zone).toInstant(), time)
        assertEquals(at(today, 9) to true, FoodDay.timeFor(today, 4, at(today, 9), zone))
    }

    @Test
    fun `reorder renumbers and returns only moved items`() {
        val items = (1..4).map { item("F$it", pos = it, uuid = "$it") }
        val order = listOf(items[0], items[2], items[3], items[1])
        assertEquals(listOf("3" to 2, "4" to 3, "2" to 4), FoodDay.renumbered(order).map { it.uuid to it.pos })
    }

    @Test
    fun `record id round trip and fallback`() {
        val parts = FoodRecordId.Parts(3, Amount(250.0, AmountUnit.G), treat = true, timed = false, uuid = "123e4567-e89b-12d3-a456-426614174000")
        val id = FoodRecordId.encode(parts)
        assertEquals("tl-food-3-g250-t-d-123e4567-e89b-12d3-a456-426614174000", id)
        assertEquals(parts, FoodRecordId.decode(id))
        val piece = FoodRecordId.Parts(1, Amount(1.5, AmountUnit.KPL), false, true, "u")
        assertEquals(piece, FoodRecordId.decode(FoodRecordId.encode(piece)))
        val none = FoodRecordId.Parts(2, null, false, true, "u")
        assertEquals("tl-food-2-n-m-c-u", FoodRecordId.encode(none))
        assertEquals(none, FoodRecordId.decode("tl-food-2-n-m-c-u"))
        for (bad in listOf(null, "terveyslappu-123", "tl-food-0-n-m-c-u", "tl-food-1-x5-m-c-u", "tl-food-1-g-m-c-u", "tl-food-1-n-q-c-u", "tl-food-1-n-m-c-")) {
            assertNull(FoodRecordId.decode(bad), bad)
        }
    }

    @Test
    fun `import skips known uuids and moves colliding positions after the day`() {
        val existing = listOf(item("Rahka", pos = 1, uuid = "a"), item("Pulla", pos = 2, uuid = "b"))
        val file = listOf(item("Rahka", pos = 1, uuid = "a"), item("Kahvi", pos = 2, uuid = "c"), item("Omena", today.minusDays(1), pos = 1, uuid = "d"))
        val written = FoodEdits.import(file, existing)
        assertEquals(listOf("d" to 1, "c" to 3), written.map { it.uuid to it.pos })
        assertTrue(FoodEdits.import(file, existing + written).isEmpty())
    }

    @Test
    fun `store learns names once and refuses a rename onto another product`() {
        val store = ProductStore()
        store.put(Product("Rahka"))
        store.put(Product("rahka ", energy = Energy.per100g(65.0)))
        store.put(Product(QUICK_ADD))
        assertEquals(1, store.all().size)
        store.put(Product("Pulla"))
        assertFalse(store.replace(FoodNames.key("Pulla"), Product("Rahka")))
        assertTrue(store.replace(FoodNames.key("Pulla"), Product("Korvapuusti")))
        assertEquals(listOf("rahka ", "Korvapuusti"), store.all().map { it.name })
        assertEquals(1, store.addMissing(listOf(Product("RAHKA"), Product("Kahvi"))))
    }

    @Test
    fun `product store survives a file round trip`() {
        val file = kotlin.io.path.createTempFile("products", ".json").toFile()
        file.delete()
        ProductStore(file).put(Product("Jauhelihakastike", energy = Energy.per100g(140.0), lastAmount = Amount(250.0, AmountUnit.G)))
        val read = ProductStore(file).all().single()
        assertEquals(Energy.per100g(140.0), read.energy)
        assertEquals(Amount(250.0, AmountUnit.G), read.lastAmount)
        file.delete()
    }

    @Test
    fun `every base product has a name and energy`() {
        val text = java.io.File("src/main/assets/base-products.json").readText()
        val products = BaseProducts.parse(text)
        assertTrue(products.size >= 250, "about 300 base products")
        assertTrue(products.all { it.name.isNotBlank() && it.energy.kcal > 0 && it.portion.value > 0 })
        assertEquals(products.size, products.map { FoodNames.key(it.name) }.toSet().size)
        assertNotNull(products.firstOrNull { it.name == "Kaurapuuro" })
        assertEquals(Energy.per100g(140.0), products.first { it.name == "Jauhelihakastike" }.energy)
    }

    @Test
    fun `instant arithmetic is not shifted by the test clock`() {
        assertEquals(Instant.parse("2026-11-14T05:00:00Z"), at(today, 7))
    }
}
