package fi.jukkakot.terveyslappu.ui

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.food.Amount
import fi.jukkakot.terveyslappu.food.AmountUnit
import fi.jukkakot.terveyslappu.food.FoodItem
import fi.jukkakot.terveyslappu.steps.StepsAccess
import fi.jukkakot.terveyslappu.steps.StepsDay
import fi.jukkakot.terveyslappu.steps.StepsUiState
import fi.jukkakot.terveyslappu.weight.weightEntry
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.assertTrue

/** Made-up front page data (NFR-1) for the grid and edit-mode tests. */
internal object HomeFixtures {
    private val zone: ZoneId = ZoneId.systemDefault()
    val today: LocalDate = LocalDate.now(zone)
    private val now: Instant = Instant.now()

    val weights = listOf(0L, 2L, 5L, 9L, 14L, 20L, 27L).mapIndexed { i, days ->
        weightEntry("w$i", now.minusSeconds(days * 86_400 + 3_600), null, 82.4 + i * 0.2, "me", "me")
    }

    private fun food(uuid: String, day: LocalDate, kcal: Double, treat: Boolean = false): FoodItem {
        val t = day.atTime(12, 0).atZone(zone).toInstant()
        return FoodItem(uuid, day, 1, "Annos", kcal, Amount(1.0, AmountUnit.KPL), treat, t, zone.rules.getOffset(t), true)
    }

    val foodItems = listOf(
        food("f0", today, 1240.0),
        food("f1", today, 230.0, treat = true),
        food("f2", today.minusDays(1), 1900.0),
        food("f3", today.minusDays(2), 2100.0),
        food("f4", today.minusDays(4), 1700.0),
    )
    val food = FoodUiState(access = FoodAccess.GRANTED, items = foodItems)

    /** The highest day 11 400 steps: the big card's scale top is 14 000 (spec Card sizes). */
    val steps = StepsUiState(
        access = StepsAccess.GRANTED,
        days = listOf(StepsDay(today, 6420), StepsDay(today.minusDays(1), 11_400), StepsDay(today.minusDays(3), 7600), StepsDay(today.minusDays(5), 5200)),
    )

    /** The node lies fully inside [container] and has a height (nothing cut off or squeezed away). */
    fun assertInside(node: SemanticsNodeInteraction, container: SemanticsNodeInteraction, what: String) {
        val b = node.getUnclippedBoundsInRoot()
        val c = container.getUnclippedBoundsInRoot()
        val slack = 0.5.dp
        assertTrue(b.bottom - b.top > 1.dp, "$what has no height: $b")
        assertTrue(b.top >= c.top - slack && b.bottom <= c.bottom + slack && b.left >= c.left - slack && b.right <= c.right + slack, "$what $b is outside $c")
    }
}
