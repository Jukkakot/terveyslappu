package fi.jukkakot.terveyslappu.food

import org.junit.Test
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Made-up foods only (NFR-1). */
class AiPasteTest {
    private val zone = ZoneId.of("Europe/Helsinki")
    private val today = LocalDate.of(2026, 11, 14)
    private val now = today.atTime(21, 30).atZone(zone).toInstant()

    private fun ok(text: String): AiParsed = assertIs<AiResult.Ok>(AiPaste.parse(text)).parsed

    private fun plan(
        items: List<AiItem>,
        products: List<AiProduct> = emptyList(),
        day: LocalDate = today,
        dayItems: List<FoodItem> = emptyList(),
        own: List<Product> = emptyList(),
        base: List<BaseProduct> = emptyList(),
    ): AiImportPlan {
        var n = 0
        return AiImport.plan(items, products, day, dayItems, own, base, now, zone) { "u${n++}" }
    }

    private fun existing(name: String, pos: Int) =
        FoodItem("e$pos", today, pos, name, 130.0, Amount(200.0, AmountUnit.G), false, now, zone.rules.getOffset(now), true)

    @Test
    fun `code fence with text around it`() {
        val p = ok("Tässä arvio:\n```json\n{\"items\":[{\"name\":\"Kaurapuuro\",\"amount\":300,\"unit\":\"g\",\"kcal\":210}]}\n```\nNäin se meni {hmm}.")
        assertEquals(AiItem("Kaurapuuro", Amount(300.0, AmountUnit.G), 210.0, false), p.items.single())
        assertEquals(0, p.skipped)
    }

    @Test
    fun `bare list, string numbers, decimal comma and unknown keys`() {
        val p = ok("""[{"name":"Mehu","amount":"1,5","unit":"kpl","kcal":"90","date":"2026-01-01","x":1}]""")
        assertEquals(AiItem("Mehu", Amount(1.5, AmountUnit.KPL), 90.0, false), p.items.single())
    }

    @Test
    fun `no JSON and empty JSON fail`() {
        assertEquals(AiResult.Failed(AiFailure.NO_JSON), AiPaste.parse("Pelkkää chat-tekstiä [1] ja {sulkuja}."))
        assertEquals(AiResult.Failed(AiFailure.EMPTY), AiPaste.parse("""{"items":[{"kcal":5}]}"""))
    }

    @Test
    fun `nameless and unusable entries are skipped and counted`() {
        val p = ok("""{"items":[{"name":"Kahvi"},{"kcal":100},{"name":"Vilja","amount":-3},{"name":"Pulla","kcal":"abc"}],"products":[{"name":"Keitto"},{"name":"Salaatti","kcal":50,"per":"muu"}]}""")
        assertEquals(listOf("Kahvi"), p.items.map { it.name })
        assertEquals(5, p.skipped)
        assertTrue(p.products.isEmpty())
    }

    @Test
    fun `amount without unit is grams, annos is a piece`() {
        val p = ok("""{"items":[{"name":"Rahka","amount":200}],"products":[{"name":"Lohikeitto","kcal":380,"per":"annos"}]}""")
        assertEquals(Amount(200.0, AmountUnit.G), p.items.single().amount)
        assertEquals(Energy.perPiece(380.0), p.products.single().energy)
    }

    @Test
    fun `the prompt file example parses`() {
        val p = ok(File("src/main/assets/ai-food-prompt.txt").readText())
        assertEquals(3, p.items.size)
        assertEquals(1, p.products.size)
        assertEquals(0, p.skipped)
        assertTrue(p.items[2].treat)
        assertEquals(Amount(400.0, AmountUnit.G), p.products.single().usual)
    }

    @Test
    fun `items go after the day's items in order, learn energy and add a product`() {
        val r = plan(listOf(AiItem("Kaurapuuro", Amount(300.0, AmountUnit.G), 210.0, false), AiItem("Kahvi", null, null, false)), dayItems = listOf(existing("Rahka", 1)))
        assertEquals(listOf(2, 3), r.items.map { it.pos })
        assertEquals(listOf("Kaurapuuro", "Kahvi"), r.items.map { it.name })
        assertNull(r.items[1].kcal)
        val learnt = r.products.first { it.name == "Kaurapuuro" }
        assertEquals(Energy.per100g(70.0), learnt.energy)
        assertEquals(Amount(300.0, AmountUnit.G), learnt.lastAmount)
    }

    @Test
    fun `a piece item learns kcal per piece`() {
        val r = plan(listOf(AiItem("Pulla", Amount(2.0, AmountUnit.KPL), 500.0, true)))
        assertEquals(Energy.perPiece(250.0), r.products.single().energy)
        assertTrue(r.products.single().treat)
    }

    private val rahka = Product("Rahka", energy = Energy.per100g(65.0), lastAmount = Amount(250.0, AmountUnit.G))
    private val fries = BaseProduct("Ranskalaiset perunat", listOf("Ranskalaiset"), Energy.per100g(290.0), Amount(150.0, AmountUnit.G), true)
    private val oven = BaseProduct("Uuniranskalaiset", emptyList(), Energy.per100g(130.0), Amount(200.0, AmountUnit.G), false)

    @Test
    fun `an item keeps its kcal and updates a known product with a change line`() {
        val r = plan(listOf(AiItem("rahka", Amount(200.0, AmountUnit.G), 160.0, false)), own = listOf(rahka))
        assertEquals(160.0, r.items.single().kcal)
        assertEquals("Rahka", r.items.single().name)
        val p = r.products.single()
        assertEquals(Energy.per100g(80.0), p.energy)
        assertEquals(Amount(200.0, AmountUnit.G), p.lastAmount)
        assertEquals(listOf("Rahka: 65 → 80 kcal / 100 g"), r.changes.map { it.text() })
        assertEquals(rahka, r.before["rahka"])
    }

    @Test
    fun `same energy gives no line but the usual amount is saved`() {
        val r = plan(listOf(AiItem("Rahka", Amount(200.0, AmountUnit.G), 130.0, false)), own = listOf(rahka))
        assertTrue(r.changes.isEmpty())
        assertEquals(Amount(200.0, AmountUnit.G), r.products.single().lastAmount)
        assertEquals(Energy.per100g(65.0), r.products.single().energy)
    }

    @Test
    fun `a unit change reads both units and a new product gets no line`() {
        val bun = Product("Pulla", treat = false, energy = Energy.perPiece(250.0))
        val r = plan(listOf(AiItem("Pulla", Amount(100.0, AmountUnit.G), 380.0, false), AiItem("Uusi", Amount(100.0, AmountUnit.G), 50.0, false)), own = listOf(bun))
        assertEquals(listOf("Pulla: 250 kcal / kpl → 380 kcal / 100 g"), r.changes.map { it.text() })
    }

    @Test
    fun `the last item of a product wins`() {
        val r = plan(listOf(AiItem("Rahka", Amount(100.0, AmountUnit.G), 70.0, false), AiItem("Rahka", Amount(200.0, AmountUnit.G), 160.0, false)), own = listOf(rahka))
        assertEquals(Energy.per100g(80.0), r.products.single().energy)
        assertEquals(listOf(70.0, 160.0), r.items.map { it.kcal })
    }

    @Test
    fun `filled with the import's energy`() {
        val r = plan(listOf(AiItem("Rahka", Amount(100.0, AmountUnit.G), null, false), AiItem("Rahka", Amount(200.0, AmountUnit.G), 160.0, false)), own = listOf(rahka))
        assertEquals(listOf(80.0, 160.0), r.items.map { it.kcal })
    }

    @Test
    fun `the product list beats the items`() {
        val r = plan(
            listOf(AiItem("Lohikeitto", Amount(400.0, AmountUnit.G), 320.0, false)),
            listOf(AiProduct("Lohikeitto", Energy.perPiece(380.0), null, false)),
        )
        assertEquals(320.0, r.items.single().kcal)
        assertEquals(Energy.perPiece(380.0), r.products.single().energy)
    }

    @Test
    fun `a base alias does not rename or make a treat`() {
        val r = plan(listOf(AiItem("Ranskalaiset", Amount(200.0, AmountUnit.G), 300.0, false)), base = listOf(fries))
        val item = r.items.single()
        assertEquals("Ranskalaiset", item.name)
        assertFalse(item.treat)
        assertEquals(300.0, item.kcal)
        val p = r.products.single()
        assertEquals("Ranskalaiset", p.name)
        assertEquals(Energy.per100g(150.0), p.energy)
        assertFalse(p.treat)
    }

    @Test
    fun `a base product fills a missing kcal and becomes an own product under the JSON name`() {
        val r = plan(listOf(AiItem("uuniranskalaiset", Amount(200.0, AmountUnit.G), null, false)), base = listOf(oven))
        assertEquals(260.0, r.items.single().kcal)
        assertEquals("uuniranskalaiset", r.products.single().name)
        assertEquals(Energy.per100g(130.0), r.products.single().energy)
        assertTrue(r.changes.isEmpty())
    }

    @Test
    fun `base off means no fill`() {
        val r = plan(listOf(AiItem("Uuniranskalaiset", Amount(200.0, AmountUnit.G), null, false)))
        assertNull(r.items.single().kcal)
        assertNull(r.products.single().energy)
    }

    @Test
    fun `a unit mismatch logs without kcal and keeps its amount`() {
        val r = plan(listOf(AiItem("Rahka", Amount(1.0, AmountUnit.KPL), null, false)), own = listOf(rahka))
        assertNull(r.items.single().kcal)
        assertEquals(Amount(1.0, AmountUnit.KPL), r.items.single().amount)
    }

    @Test
    fun `an item without amount takes the usual amount`() {
        val r = plan(listOf(AiItem("Rahka", null, null, false)), own = listOf(rahka))
        assertEquals(Amount(250.0, AmountUnit.G), r.items.single().amount)
        assertEquals(162.5, r.items.single().kcal)
    }

    @Test
    fun `the category follows the JSON and shows as a line`() {
        val candy = Product("Lakritsi", treat = true, energy = Energy.per100g(350.0))
        val r = plan(listOf(AiItem("Lakritsi", Amount(50.0, AmountUnit.G), 130.0, false)), own = listOf(candy))
        assertFalse(r.items.single().treat)
        assertFalse(r.products.single().treat)
        assertEquals(listOf("Lakritsi: 350 → 260 kcal / 100 g, ei herkku"), r.changes.map { it.text() })
        val onlyCategory = plan(listOf(AiItem("Lakritsi", null, null, false)), own = listOf(candy))
        assertEquals(listOf("Lakritsi: ei herkku"), onlyCategory.changes.map { it.text() })
    }

    @Test
    fun `the product list's own change shows a line`() {
        val r = plan(emptyList(), listOf(AiProduct("Rahka", Energy.per100g(80.0), null, false)), own = listOf(rahka))
        assertEquals(listOf("Rahka: 65 → 80 kcal / 100 g"), r.changes.map { it.text() })
    }


    @Test
    fun `the product list replaces and creates`() {
        val own = listOf(Product("Lohikeitto", energy = Energy.perPiece(300.0), lastAmount = Amount.ONE_PIECE))
        val r = plan(emptyList(), listOf(AiProduct("Lohikeitto", Energy.perPiece(380.0), null, false), AiProduct("Uusi", Energy.per100g(80.0), Amount(400.0, AmountUnit.G), false)), own = own)
        assertEquals(Energy.perPiece(380.0), r.products.first { it.name == "Lohikeitto" }.energy)
        assertEquals(Amount.ONE_PIECE, r.products.first { it.name == "Lohikeitto" }.lastAmount)
        assertEquals(Amount(400.0, AmountUnit.G), r.products.first { it.name == "Uusi" }.lastAmount)
        assertNull(r.before["uusi"])
        assertEquals(Energy.perPiece(300.0), r.before["lohikeitto"]?.energy)
    }

    @Test
    fun `Pikalisäys is never a product`() {
        val r = plan(listOf(AiItem(QUICK_ADD, null, 100.0, false)))
        assertTrue(r.products.isEmpty())
        assertEquals(QUICK_ADD, r.items.single().name)
    }

    @Test
    fun `undo restores the products as they were`() {
        val store = ProductStore()
        val own = listOf(Product("Lohikeitto", energy = Energy.perPiece(300.0)))
        own.forEach { store.put(it) }
        val r = plan(listOf(AiItem("Kaurapuuro", Amount(300.0, AmountUnit.G), 210.0, false)), listOf(AiProduct("Lohikeitto", Energy.perPiece(380.0), null, false)), own = own)
        r.products.forEach { store.put(it) }
        assertEquals(2, store.all().size)
        store.restore(r.before)
        assertEquals(own, store.all())
    }

    @Test
    fun `existing items are not part of the plan and keep their positions`() {
        val old = listOf(existing("Rahka", 1))
        val r = plan(listOf(AiItem("Kaurapuuro", null, 100.0, false)), dayItems = old)
        assertTrue(r.items.none { it.uuid == old[0].uuid })
        assertEquals(2, r.items.single().pos)
    }

    @Test
    fun `a past day gets untimed items`() {
        val r = plan(listOf(AiItem("Kahvi", null, null, false)), day = today.minusDays(1))
        assertFalse(r.items.single().timed)
        assertEquals(today.minusDays(1), r.items.single().day)
    }
}
