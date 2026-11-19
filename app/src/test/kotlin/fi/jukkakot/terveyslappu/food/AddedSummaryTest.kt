package fi.jukkakot.terveyslappu.food

import fi.jukkakot.terveyslappu.ui.addedSubject
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The add search's marks, strip and bar text (everyday-feedback design 7, 8); made-up foods. */
class AddedSummaryTest {
    private val day = LocalDate.of(2026, 11, 19)

    private fun item(uuid: String, name: String, kcal: Double?, amount: Amount? = null) =
        FoodItem(uuid, day, 0, name, kcal, amount, false, Instant.EPOCH, ZoneOffset.UTC, true)

    private val rahka = Product("Rahka", energy = Energy(65.0, AmountUnit.G), lastAmount = Amount(200.0, AmountUnit.G))
    private val kahvi = Product("Kahvi")

    @Test
    fun `repeats count per ruoka and the strip counts every add`() {
        val items = listOf(item("a", "Rahka", 130.0), item("b", "Kahvi", 5.0), item("c", "kahvi", 5.0))
        val s = AddedSummary.of(setOf("a", "b", "c"), items)
        assertEquals(1, s.of(Suggestion.Own(rahka)))
        assertEquals(2, s.of(Suggestion.Own(kahvi)))
        assertEquals(3, s.count)
        assertEquals(140, s.kcal)
    }

    @Test
    fun `Kumoa, a failed write or a delete lowers the count`() {
        val items = listOf(item("a", "Rahka", 130.0), item("b", "Kahvi", 5.0), item("c", "Kahvi", 5.0))
        // The undone "c" is no longer in the added ids.
        val s = AddedSummary.of(setOf("a", "b"), items)
        assertEquals(1, s.of(Suggestion.Own(kahvi)))
        assertEquals(2, s.count)
        assertEquals(AddedSummary.NONE, AddedSummary.of(emptySet(), items))
    }

    @Test
    fun `Pikalisäys and the clipboard's adds count only in the strip`() {
        val items = listOf(item("q", QUICK_ADD, 350.0), item("i", "Testikeitto", 180.0))
        val s = AddedSummary.of(setOf("q", "i"), items)
        assertEquals(2, s.count)
        assertEquals(530, s.kcal)
        assertEquals(0, s.of(Suggestion.Own(rahka)))
    }

    @Test
    fun `annokset without kcal are counted apart, an add not yet read only in the count`() {
        val items = listOf(item("a", "Rahka", 130.0), item("p", "työpaikan pizza", null))
        val s = AddedSummary.of(setOf("a", "p", "queued"), items)
        assertEquals(3, s.count)
        assertEquals(130, s.kcal)
        assertEquals(1, s.withKcal)
        assertEquals(1, s.withoutKcal)
    }

    @Test
    fun `the base row of an alias logged as an own ruoka is marked`() {
        val base = BaseProduct("Maitokahvi", listOf("kahvi maidolla"), Energy(40.0, AmountUnit.KPL), Amount(1.0, AmountUnit.KPL), false)
        val s = AddedSummary.of(setOf("a"), listOf(item("a", "Kahvi maidolla", 40.0)))
        assertEquals(1, s.of(Suggestion.Base(base)))
        assertEquals(1, s.of(Suggestion.Own(Product("Kahvi maidolla"))))
    }

    @Test
    fun `the bar names the amount or the Pikalisäys kcal`() {
        assertTrue(addedSubject(item("a", "Rahka", 130.0, Amount(200.0, AmountUnit.G))).contains("200 g"))
        assertTrue(addedSubject(item("q", QUICK_ADD, 350.0)).contains("350 kcal"))
        assertEquals("työpaikan pizza", addedSubject(item("p", "työpaikan pizza", null)))
    }
}
