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
    ): AiImportPlan {
        var n = 0
        return AiImport.plan(items, products, day, dayItems, own, emptyList(), now, zone) { "u${n++}" }
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

    @Test
    fun `a known product is not changed by an item and a known treat stays a treat`() {
        val own = listOf(Product("Rahka", energy = Energy.per100g(65.0)), Product("Keksi", treat = true, energy = Energy.perPiece(50.0)))
        val r = plan(listOf(AiItem("rahka", Amount(200.0, AmountUnit.G), 150.0, false), AiItem("Keksi", Amount(2.0, AmountUnit.KPL), 90.0, false)), own = own)
        assertTrue(r.products.isEmpty())
        assertEquals(150.0, r.items[0].kcal)
        assertEquals("Rahka", r.items[0].name)
        assertTrue(r.items[1].treat)
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
