package fi.jukkakot.terveyslappu.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.food.Amount
import fi.jukkakot.terveyslappu.food.AmountUnit
import fi.jukkakot.terveyslappu.food.FoodItem
import fi.jukkakot.terveyslappu.food.Product
import fi.jukkakot.terveyslappu.food.Suggestion
import fi.jukkakot.terveyslappu.weight.weightEntry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Smoke tests of the food screens (food-log), made-up foods only (NFR-1). */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w411dp-h891dp")
class FoodScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val zone = ZoneId.systemDefault()
    private val today = LocalDate.now(zone)

    private fun item(uuid: String, day: LocalDate, pos: Int, name: String, kcal: Double?, treat: Boolean = false, hour: Int = 8): FoodItem {
        val t = day.atTime(hour, 0).atZone(zone).toInstant()
        return FoodItem(uuid, day, pos, name, kcal, Amount(1.0, AmountUnit.KPL), treat, t, zone.rules.getOffset(t), true)
    }

    private val items = listOf(
        item("a", today, 1, "Rahka", 130.0),
        item("b", today, 2, "Pullo", 230.0, treat = true, hour = 10),
        item("c", today, 3, "Pizza", null, hour = 12),
        item("d", today.minusDays(1), 1, "Lasagne", 600.0),
    )

    private var picked: Suggestion? = null

    private fun show(start: MainUiState, foodStart: FoodUiState) {
        compose.setContent {
            var state by remember { mutableStateOf(start) }
            var food by remember { mutableStateOf(foodStart) }
            TerveysTheme {
                MainScreen(
                    state,
                    "0.1.1-test",
                    MainActions(
                        onBack = { state = state.back() },
                        onScreen = { state = state.copy(screen = it) },
                    ),
                    food = food,
                    foodActions = FoodActions(
                        onOpenDay = {
                            food = food.copy(day = it)
                            state = state.copy(screen = Screen.FOOD, foodOrigin = state.screen)
                        },
                        onAdd = { food = food.copy(sheetDay = it) },
                        onPick = { picked = it },
                        onCloseSheet = { food = food.copy(sheetDay = null) },
                    ),
                )
            }
        }
    }

    private val granted = FoodUiState(access = FoodAccess.GRANTED, items = items, products = listOf(Product("Rahka"), Product("Pullo", treat = true)), day = today)

    @Test
    fun `front page shows today's total and its button opens the add sheet, where a suggestion adds`() {
        show(MainUiState(access = Access.GRANTED, weights = emptyList()), granted)
        compose.onNodeWithTag("foodToday").assertExists()
        compose.onNodeWithTag("homeFoodAdd").performScrollTo().performClick()
        compose.onNodeWithTag("addField").assertExists()
        compose.onNodeWithTag("pick-Rahka").performClick()
        assertEquals("Rahka", picked?.name)
    }

    @Test
    fun `Ruoka shows the day's items and the total`() {
        show(MainUiState(access = Access.GRANTED, weights = emptyList()), granted)
        compose.onNodeWithTag("foodToday").performScrollTo().performClick()
        compose.onNodeWithTag("food-a").assertExists()
        compose.onNodeWithTag("food-c").assertExists()
        compose.onNodeWithTag("food-d").assertDoesNotExist()
        compose.onNodeWithTag("foodSum").assertExists()
        compose.onNodeWithTag("prevDay").performClick()
        compose.onNodeWithTag("back").performClick()
        compose.onNodeWithTag("foodToday").assertExists()
    }

    @Test
    fun `Energia shows bars and the day list, and a day opens Ruoka`() {
        val weights = listOf(weightEntry("w", today.atTime(7, 0).atZone(zone).toInstant(), null, 82.0, "me", "me"))
        show(MainUiState(access = Access.GRANTED, weights = weights, screen = Screen.HISTORY, historyOnEnergy = true), granted)
        compose.onNodeWithTag("energyChart").assertExists()
        compose.onNodeWithTag("energyDay-${today.minusDays(1)}").performScrollTo().performClick()
        compose.onNodeWithTag("food-d").assertExists()
    }

    @Test
    fun `without the permission Ruoka gives the reason and a grant button`() {
        var asked = false
        compose.setContent {
            TerveysTheme {
                MainScreen(
                    MainUiState(access = Access.GRANTED, weights = emptyList(), screen = Screen.FOOD),
                    "0.1.1-test",
                    MainActions(),
                    food = FoodUiState(access = FoodAccess.DENIED),
                    foodActions = FoodActions(onGrant = { asked = true }),
                )
            }
        }
        compose.onNodeWithTag("foodGrant").performClick()
        assertTrue(asked)
    }
}
