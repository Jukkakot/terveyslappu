package fi.jukkakot.terveyslappu.food

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FoodInfoTest {
    private val t = Instant.parse("2026-01-05T07:10:00Z")

    private fun item(name: String = "Rahka", kcal: Double? = 180.0, amount: Amount? = Amount(200.0, AmountUnit.G), treat: Boolean = false, timed: Boolean = true) =
        FoodItem("u", LocalDate.of(2026, 1, 5), 1, name, kcal, amount, treat, t, ZoneOffset.ofHours(2), timed)

    @Test
    fun allLinesInOrder() {
        val own = Product("rahka", energy = Energy.per100g(90.0))
        val lines = FoodInfo.lines(item(treat = true), own)
        assertEquals(
            listOf(
                InfoLine.AmountLine(Amount(200.0, AmountUnit.G)),
                InfoLine.KcalLine(180.0),
                InfoLine.EnergyLine(Energy.per100g(90.0)),
                InfoLine.TreatLine,
                InfoLine.TimeLine(LocalDateTime.of(2026, 1, 5, 9, 10)),
            ),
            lines,
        )
    }

    @Test
    fun noAmountNoKcalProductMissing() {
        val lines = FoodInfo.lines(item(kcal = null, amount = null), null)
        assertEquals(InfoLine.KcalLine(null), lines.first())
        assertEquals(2, lines.size)
    }

    @Test
    fun productWithoutKaloritAddsNoLine() {
        assertEquals(3, FoodInfo.lines(item(), Product("Rahka")).size)
    }

    @Test
    fun untimedItemHasNoTime() {
        assertEquals(2, FoodInfo.lines(item(timed = false), null).size)
    }

    @Test
    fun ownProductByKey() {
        val products = listOf(Product("Pullo"), Product("Rahka"))
        assertEquals("Rahka", FoodInfo.ownProduct(item(name = " rahka "), products)?.name)
        assertNull(FoodInfo.ownProduct(item(name = "Kala"), products))
    }
}
