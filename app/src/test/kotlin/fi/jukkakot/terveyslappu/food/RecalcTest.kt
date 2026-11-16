package fi.jukkakot.terveyslappu.food

import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** product-energy-fix: which earlier items follow a product's corrected energy. Made-up foods only (NFR-1). */
class RecalcTest {
    private val zone = ZoneId.of("Europe/Helsinki")
    private val day = LocalDate.of(2026, 11, 10)

    private fun item(uuid: String, name: String, kcal: Double?, amount: Amount?, d: Int = 0, treat: Boolean = false): FoodItem {
        val date = day.plusDays(d.toLong())
        val time = date.atTime(12, 0).atZone(zone).toInstant()
        return FoodItem(uuid, date, 1, name, kcal, amount, treat, time, zone.rules.getOffset(time), true)
    }

    private fun kpl(n: Double) = Amount(n, AmountUnit.KPL)
    private fun g(n: Double) = Amount(n, AmountUnit.G)
    private val key = FoodNames.key("Lakritsi")

    @Test
    fun `heavier liquorice follows, a typed total stays`() {
        val items = listOf(
            item("a", "Lakritsi", 50.0, kpl(5.0), 0),
            item("b", "Lakritsi", 50.0, kpl(5.0), 1),
            item("c", "Lakritsi", 50.0, kpl(5.0), 2),
            item("d", "Lakritsi", 80.0, null, 2),
        )
        val r = FoodEdits.recalc(key, Energy.perPiece(10.0), Energy.perPiece(12.0), items, null)
        assertEquals(listOf("a", "b", "c"), r.new.map { it.uuid })
        assertEquals(listOf(60.0, 60.0, 60.0), r.new.map { it.kcal })
        assertEquals(1, r.kept)
        assertEquals(3, r.days)
        assertEquals(150, r.oldSum)
        assertEquals(180, r.newSum)
    }

    @Test
    fun `own values stay`() {
        val rahka = FoodNames.key("Rahka")
        val items = listOf(
            item("a", "Rahka", 130.0, g(200.0)),
            item("b", "Rahka", 150.0, g(200.0)),
            item("c", "Rahka", 131.0, g(200.0)),
        )
        val r = FoodEdits.recalc(rahka, Energy.per100g(65.0), Energy.per100g(70.0), items, null)
        assertEquals(listOf("a"), r.new.map { it.uuid })
        assertEquals(140.0, r.new.single().kcal)
        assertEquals(2, r.kept)
    }

    @Test
    fun `decimal energy`() {
        val items = listOf(item("a", "Keksi", 37.5, kpl(3.0)))
        val r = FoodEdits.recalc(FoodNames.key("Keksi"), Energy.perPiece(12.5), Energy.perPiece(15.0), items, null)
        assertEquals(45.0, r.new.single().kcal)
    }

    @Test
    fun `a correction is the same unit with a different kcal`() {
        assertTrue(FoodEdits.isCorrection(Energy.perPiece(10.0), Energy.perPiece(12.0)))
        assertFalse(FoodEdits.isCorrection(Energy.perPiece(10.0), Energy.per100g(10.0)), "unit change")
        assertFalse(FoodEdits.isCorrection(Energy.perPiece(10.0), null), "cleared")
        assertFalse(FoodEdits.isCorrection(null, Energy.perPiece(10.0)), "new energy")
        assertFalse(FoodEdits.isCorrection(Energy.perPiece(10.0), Energy.perPiece(10.0)), "same")
    }

    @Test
    fun `amount, Herkku or a rename alone keep the product's energy`() {
        val product = Product("Lakritsi", energy = Energy.perPiece(10.0), lastAmount = kpl(5.0))
        val edited = item("a", "Lakritsi", 50.0, kpl(5.0))
        val all = listOf(edited, item("b", "Lakritsi", 50.0, kpl(5.0), 1))
        val amountOnly = FoodEdits.edit(edited, EditInput("Lakritsi", EditMode.KPL, 6.0, 10.0, false), all, listOf(product), emptyList())
        assertFalse(FoodEdits.isCorrection(product.energy, amountOnly.product?.energy))
        val treatOnly = FoodEdits.edit(edited, EditInput("Lakritsi", EditMode.KPL, 5.0, 10.0, true), all, listOf(product), emptyList())
        assertFalse(FoodEdits.isCorrection(product.energy, treatOnly.product?.energy))
        val renamed = FoodEdits.product(product.key, product.copy(name = "Salmiakki"), all)
        assertFalse(FoodEdits.isCorrection(product.energy, renamed.product?.energy))
    }

    @Test
    fun `nothing else logged and items without kcal are not in the plan`() {
        val items = listOf(
            item("edited", "Lakritsi", 50.0, kpl(5.0)),
            item("none", "Lakritsi", null, kpl(5.0), 1),
        )
        val r = FoodEdits.recalc(key, Energy.perPiece(10.0), Energy.perPiece(12.0), items, "edited")
        assertTrue(r.new.isEmpty())
        assertEquals(0, r.kept)
    }

    @Test
    fun `another product is untouched`() {
        val items = listOf(item("a", "Lakritsi", 50.0, kpl(5.0)), item("b", "Salmiakki", 50.0, kpl(5.0)))
        val r = FoodEdits.recalc(key, Energy.perPiece(10.0), Energy.perPiece(12.0), items, null)
        assertEquals(listOf("a"), r.new.map { it.uuid })
    }

    @Test
    fun `sums are rounded after summing`() {
        val items = (1..3).map { item("i$it", "Lakritsi", 1.4, kpl(1.0), it) }
        val r = FoodEdits.recalc(key, Energy.perPiece(1.4), Energy.perPiece(1.6), items, null)
        assertEquals(4, r.oldSum, "4.2, not 1+1+1")
        assertEquals(5, r.newSum, "4.8, not 2+2+2")
    }

    @Test
    fun `merge keeps a rename and undo skips an item changed again`() {
        val items = listOf(item("a", "Lakritsi", 50.0, kpl(5.0)), item("b", "Lakritsi", 50.0, kpl(5.0), 1))
        val product = Product("Salmiakki", energy = Energy.perPiece(12.0))
        val plan = FoodEdits.product(key, product, items)
        val r = FoodEdits.recalc(key, Energy.perPiece(10.0), Energy.perPiece(12.0), items, null)
        val merged = FoodEdits.merge(plan, r, items)
        assertEquals(listOf("Salmiakki" to 60.0, "Salmiakki" to 60.0), merged.new.map { it.name to it.kcal })
        val now = listOf(merged.new[0], merged.new[1].copy(kcal = 70.0))
        val undo = FoodEdits.undoRecalc(r, now)
        assertEquals(listOf("a"), undo.new.map { it.uuid })
        assertEquals(50.0, undo.new.single().kcal)
    }
}
