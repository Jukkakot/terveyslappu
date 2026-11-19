package fi.jukkakot.terveyslappu.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.food.Amount
import fi.jukkakot.terveyslappu.food.AmountUnit
import fi.jukkakot.terveyslappu.food.FoodItem
import fi.jukkakot.terveyslappu.food.Product
import fi.jukkakot.terveyslappu.food.Suggestion
import fi.jukkakot.terveyslappu.weight.weightEntry
import fi.jukkakot.terveyslappu.food.ClipOffer
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
    private var editPicked: Suggestion? = null
    private var editedItem: String? = null
    private var deletedItem: String? = null

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
                        onOpenEnergy = { state = state.copy(screen = Screen.HISTORY, historyOnEnergy = true) },
                        onFoodPeriod = { state = state.copy(foodDays = it) },
                        onOpenWeek = { state = state.copy(screen = Screen.HISTORY, historyOnEnergy = true, historyOrigin = Screen.FOOD) },
                    ),
                    food = food,
                    foodActions = FoodActions(
                        onOpenDay = {
                            food = food.copy(day = it)
                            state = state.copy(screen = Screen.FOOD, foodOrigin = state.screen)
                        },
                        onAdd = { food = food.copy(sheetDay = it) },
                        onPick = { picked = it },
                        onEditPick = { editPicked = it },
                        onEdit = { editedItem = it.name },
                        onDelete = { deletedItem = it.name },
                        onCloseSheet = { food = food.copy(sheetDay = null) },
                        onClipSkip = { food = food.copy(clipOffer = null) },
                    ),
                )
            }
        }
    }

    private val granted = FoodUiState(access = FoodAccess.GRANTED, items = items, products = listOf(Product("Rahka"), Product("Pullo", treat = true)), day = today)

    @Test
    fun `front page shows today's total and the menu's Lisää annos opens the add sheet, where a suggestion adds`() {
        show(MainUiState(access = Access.GRANTED, weights = emptyList()), granted)
        compose.onNodeWithTag("foodToday").assertExists()
        compose.onNodeWithTag("appMenu").performClick()
        compose.onNodeWithTag("menuFood").performClick()
        compose.onNodeWithTag("addField").assertExists()
        // The sheet shows the day's items, the header total and the chips apart from them.
        compose.onNodeWithTag("food-a").assertExists()
        compose.onNodeWithTag("sheetTitle").assertTextContains("Tänään · 360 kcal")
        compose.onNodeWithTag("chip-Rahka").performClick()
        assertEquals("Rahka", picked?.name)
        compose.onNodeWithTag("chip-Pullo").performTouchInput { longClick() }
        assertEquals("Pullo", editPicked?.name)
    }

    @Test
    fun `typing in the sheet shows Tuotteet instead of the day list and the chips`() {
        show(MainUiState(access = Access.GRANTED, weights = emptyList()), granted.copy(sheetDay = today, query = "rah"))
        compose.onNodeWithTag("addMatches").assertExists()
        compose.onNodeWithTag("addSuggestions").assertDoesNotExist()
        compose.onNodeWithTag("sheetDayList").assertDoesNotExist()
    }

    @Test
    fun `the handle folds and unfolds the add panel`() {
        show(MainUiState(access = Access.GRANTED, weights = emptyList()), granted.copy(sheetDay = today))
        compose.onNodeWithTag("addSuggestions").assertExists()
        compose.onNodeWithTag("addField").assertExists()
        compose.onNodeWithTag("addPanelHandle").performClick()
        compose.waitUntil(3000) { compose.onAllNodesWithTag("addField").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithTag("addSuggestions").assertDoesNotExist()
        compose.onNodeWithTag("addPanelHandle").performClick()
        compose.waitUntil(3000) { compose.onAllNodesWithTag("addField").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("addSuggestions").assertExists()
    }

    @Test
    fun `a food row shows no logging time`() {
        show(MainUiState(access = Access.GRANTED, weights = emptyList()), granted.copy(sheetDay = today))
        compose.onNodeWithTag("food-a").assertExists()
        compose.onAllNodesWithText("8.00", substring = true).assertCountEquals(0)
    }

    @Test
    fun `front page food card shows bars, its own period, and opens Energia`() {
        show(MainUiState(access = Access.GRANTED, weights = emptyList()), granted)
        compose.onNodeWithTag("foodCard").assertExists()
        compose.onNodeWithTag("foodPeriod").performScrollTo().performClick()
        compose.onNodeWithTag("foodPeriod-30").performClick()
        compose.onNodeWithTag("homePeriod").assertDoesNotExist()
        compose.onNodeWithTag("foodBars").performScrollTo().performClick()
        compose.onNodeWithTag("energyChart").assertExists()
        compose.onNodeWithTag("back").performClick()
        compose.onNodeWithTag("foodToday").performScrollTo().performClick()
        compose.onNodeWithTag("foodDay").assertExists()
    }

    @Test
    fun `Viikko on Ruoka opens Energia and back returns to Ruoka`() {
        show(MainUiState(access = Access.GRANTED, weights = emptyList(), screen = Screen.FOOD), granted)
        compose.onNodeWithTag("foodWeek").performClick()
        compose.onNodeWithTag("energyChart").assertExists()
        compose.onNodeWithTag("back").performClick()
        compose.onNodeWithTag("foodDay").assertExists()
    }

    @Test
    fun `front page shows the clipboard offer above the cards and Ohita hides it`() {
        show(MainUiState(access = Access.GRANTED, weights = emptyList()), granted.copy(clipOffer = ClipOffer("k", 4, 850, false)))
        compose.onNodeWithTag("clipOffer").assertExists()
        compose.onNodeWithTag("clipSkip").performClick()
        compose.onNodeWithTag("clipOffer").assertDoesNotExist()
        compose.onNodeWithTag("foodCard").assertExists()
    }

    @Test
    fun `Ruoka rows are one line and the info button shows the amount`() {
        show(MainUiState(access = Access.GRANTED, weights = emptyList(), screen = Screen.FOOD), granted)
        compose.onNodeWithTag("food-a").assertExists()
        assertEquals(0, compose.onAllNodesWithText("1 kpl", useUnmergedTree = true).fetchSemanticsNodes().size)
        compose.onNodeWithTag("food-a-info").performClick()
        compose.waitForIdle()
        assertEquals(1, compose.onAllNodesWithText("1 kpl", useUnmergedTree = true).fetchSemanticsNodes().size)
        assertEquals(null, editedItem)
        compose.onNodeWithTag("food-a-info").performClick()
        compose.waitForIdle()
        assertEquals(0, compose.onAllNodesWithText("1 kpl", useUnmergedTree = true).fetchSemanticsNodes().size)
    }

    @Test
    fun `Ruoka rows' menu edits and deletes`() {
        show(MainUiState(access = Access.GRANTED, weights = emptyList(), screen = Screen.FOOD), granted)
        compose.onNodeWithTag("food-a-menu").performClick()
        compose.onNodeWithTag("food-a-edit").performClick()
        assertEquals("Rahka", editedItem)
        compose.onNodeWithTag("food-b-menu").performClick()
        compose.onNodeWithTag("food-b-delete").performClick()
        assertEquals("Pullo", deletedItem)
    }

    @Test
    fun `Omat ruuat rows' menu edits and deletes`() {
        var removed: String? = null
        compose.setContent {
            TerveysTheme { ProductList(listOf(Product("Rahka")), FoodActions(onRemoveProduct = { removed = it })) }
        }
        compose.onNodeWithTag("product-Rahka-menu").performClick()
        compose.onNodeWithTag("product-Rahka-edit").assertExists()
        compose.onNodeWithTag("product-Rahka-delete").performClick()
        assertEquals("rahka", removed?.lowercase())
    }

    @Test
    fun `Ruokakirjaus starts with the Valmiit ruuat switch`() {
        var set: Boolean? = null
        compose.setContent {
            TerveysTheme { ProductList(emptyList(), FoodActions(onBaseProducts = { set = it }), baseOn = true) }
        }
        compose.onNodeWithTag("baseSwitch").performClick()
        assertEquals(false, set)
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

    @Test
    fun `a tap on a Syoty bar shows the marker card, a list day opens Ruoka and back returns`() {
        show(MainUiState(access = Access.GRANTED, weights = emptyList(), screen = Screen.HISTORY, historyOnEnergy = true), granted)
        compose.waitForIdle()
        val chart = compose.activity.chartViews().single()
        val day = chart.spec!!.panels.single().series.single().shape.all.last().day
        compose.onNodeWithTag("energyBars").performTouchInput { click(androidx.compose.ui.geometry.Offset(chart.pixelX(day), chart.plotMiddle)) }
        compose.waitForIdle()
        compose.onNodeWithTag("chartMarker", useUnmergedTree = true).assertExists()
        compose.onNodeWithTag("chartReadoutAction").assertDoesNotExist()
        compose.onNodeWithTag("energyDay-$day").performScrollTo().performClick()
        compose.onNodeWithTag("food-a").assertExists()
        compose.onNodeWithTag("back").performClick()
        compose.onNodeWithTag("energyChart").assertExists()
    }
}
