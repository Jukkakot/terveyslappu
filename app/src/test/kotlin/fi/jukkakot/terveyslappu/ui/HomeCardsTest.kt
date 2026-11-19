package fi.jukkakot.terveyslappu.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.home.CardSize
import fi.jukkakot.terveyslappu.home.HomeCard
import fi.jukkakot.terveyslappu.home.HomeLayout
import fi.jukkakot.terveyslappu.ui.HomeFixtures.assertInside
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The front page grid and the cards at their sizes (home-screen "Card grid", "Card sizes"); made-up data. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w360dp-h760dp")
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
class HomeCardsTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private var burnOpened = 0

    private fun data(state: MainUiState) =
        HomeCardsData(state, MainActions(), HomeFixtures.food, FoodActions(), HomeFixtures.steps, StepsActions(), HomeFixtures.burn, BurnActions(onOpen = { burnOpened++ }))

    private fun home(layout: HomeLayout = HomeLayout.DEFAULT, questioned: Boolean = false) {
        val state = MainUiState(
            access = Access.GRANTED,
            weights = HomeFixtures.weights,
            homeLayout = layout,
            questioned = if (questioned) setOf(HomeFixtures.weights.first().id) else emptySet(),
        )
        compose.setContent {
            TerveysTheme {
                MainScreen(state, "0.1.1-test", MainActions(), food = HomeFixtures.food, steps = HomeFixtures.steps)
            }
        }
    }

    private fun node(tag: String) = compose.onNodeWithTag(tag, useUnmergedTree = true)
    private fun bounds(tag: String) = node(tag).getUnclippedBoundsInRoot()

    @Test
    fun `the default layout shows weight above food and steps side by side`() {
        home()
        val weight = bounds("homeSlot-weight")
        val food = bounds("homeSlot-food")
        val steps = bounds("homeSlot-steps")
        assertTrue(weight.bottom <= food.top, "weight above food")
        assertEquals(food.top, steps.top, "food and steps share a row")
        assertEquals(food.bottom, steps.bottom, "same row, same height")
        assertTrue(food.right <= steps.left, "food left, steps right")
    }

    @Test
    fun `lone small card keeps an empty right half and no later card fills it`() {
        home(HomeLayout(listOf(HomeCard(HomeLayout.FOOD, CardSize.SMALL), HomeCard(HomeLayout.WEIGHT, CardSize.WIDE), HomeCard(HomeLayout.STEPS, CardSize.SMALL))))
        val food = bounds("homeSlot-food")
        val weight = bounds("homeSlot-weight")
        val steps = bounds("homeSlot-steps")
        assertEquals(food.left, steps.left, "food and steps at the same x")
        assertTrue(food.bottom <= weight.top, "weight below food")
        assertTrue(weight.bottom <= steps.top, "steps below weight")
        assertTrue(weight.right - weight.left > (food.right - food.left) * 2, "weight spans the width")
    }

    @Test
    fun `a big card is two rows and the gap high`() {
        home(HomeLayout.DEFAULT.resize(HomeLayout.WEIGHT, CardSize.BIG))
        val weight = bounds("homeSlot-weight")
        val food = bounds("homeSlot-food")
        assertEquals((food.bottom - food.top) * 2 + 8.dp, weight.bottom - weight.top)
    }

    @Test
    fun `fits without scrolling with the check notice`() {
        home(questioned = true)
        val screen = compose.onRoot().getUnclippedBoundsInRoot()
        for (tag in listOf("checkNotice", "latestCard", "foodCard", "foodPeriod", "stepsCard", "stepsPeriod", "stepsAverage")) {
            node(tag).assertIsDisplayed()
            assertTrue(bounds(tag).bottom <= screen.bottom, "$tag ends at ${bounds(tag).bottom}, screen at ${screen.bottom}")
        }
    }

    @Test
    @Config(fontScale = 1.3f)
    fun `default layout at 130 percent text keeps the food and steps averages`() {
        home()
        for (tag in listOf("foodAverage", "stepsAverage")) node(tag).assertIsDisplayed()
        assertInside(node("foodAverage"), node("homeSlot-food"), "foodAverage")
        assertInside(node("stepsAverage"), node("homeSlot-steps"), "stepsAverage")
    }


    private fun scrollTo(tag: String) {
        compose.onNodeWithTag("homeGrid").performScrollToNode(androidx.compose.ui.test.hasTestTag(tag))
    }

    @Test
    @Config(fontScale = 2f)
    fun `at 200 percent text every card takes a full row and keeps its parts inside`() {
        home()
        val keys = mapOf(
            "weight" to listOf("latestKg", "homePeriod", "sparkRange"),
            "food" to listOf("foodTodayValue", "foodPeriod", "foodAverage"),
            "steps" to listOf("stepsTodayValue", "stepsPeriod", "stepsAverage"),
        )
        for ((id, tags) in keys) {
            scrollTo("homeSlot-$id")
            val slot = bounds("homeSlot-$id")
            assertEquals(328.dp, slot.right - slot.left, "$id is a full-width card")
            for (tag in tags) assertInside(node(tag), node("homeSlot-$id"), "$id: $tag")
        }
    }

    @Test
    @Config(fontScale = 1.3f)
    fun `at exactly 130 percent food and steps still share a row`() {
        home()
        val food = bounds("homeSlot-food")
        val steps = bounds("homeSlot-steps")
        assertEquals(food.top, steps.top, "food and steps share a row")
        assertTrue(food.right <= steps.left, "food left, steps right")
    }

    @Test
    fun `a small steps card's average is two whole lines beside or under the period label`() {
        cell = HomeCard(HomeLayout.STEPS, CardSize.SMALL)
        cells()
        val box = bounds("stepsAverage")
        assertTrue(box.bottom - box.top < 36.dp, "two lines, not letters stacked: $box")
        assertTrue(box.right - box.left > 60.dp, "the value keeps its width: $box")
        assertInside(node("stepsAverage"), node("cell"), "stepsAverage")
    }

    @Test
    @Config(fontScale = 1.3f)
    fun `at 130 percent a small card's average sits under the period label`() {
        cell = HomeCard(HomeLayout.STEPS, CardSize.SMALL)
        cells()
        assertTrue(bounds("stepsAverage").top >= bounds("stepsPeriod").bottom - 4.dp, "under the label")
        assertInside(node("stepsAverage"), node("cell"), "stepsAverage")
    }

    @Test
    @Config(fontScale = 2f)
    fun `a wide steps card's long average stays whole at 200 percent`() {
        cell = HomeCard(HomeLayout.STEPS, CardSize.WIDE)
        cells()
        val long = node("stepsAverage").fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.Text].first().text
        assertTrue(long.startsWith(compose.activity.getString(R.string.home_average_word)), long)
        val box = node("stepsAverage").getUnclippedBoundsInRoot()
        assertInside(node("stepsAverage"), node("cell"), "stepsAverage")
        assertTrue(box.bottom - box.top < 40.dp, "one line: $box")
    }

    @Test
    fun `the food card opens Ruoka from any part and asks for the permission when it can`() {
        var days = 0
        var grants = 0
        val actions = FoodActions(onOpenDay = { days++ }, onGrant = { grants++ })
        fun show(food: FoodUiState) {
            compose.setContent {
                TerveysTheme {
                    Box(Modifier.width(160.dp).height(cardHeight(CardSize.SMALL, rowUnit()))) {
                        HomeCardView(HomeCard(HomeLayout.FOOD, CardSize.SMALL), HomeCardsData(MainUiState(access = Access.GRANTED, weights = HomeFixtures.weights), MainActions(), food, actions, HomeFixtures.steps, StepsActions()), Modifier.fillMaxSize())
                    }
                }
            }
        }
        show(HomeFixtures.food)
        for (tag in listOf("foodToday", "foodBars", "foodAverage")) node(tag).performClick()
        assertEquals(3, days)
        assertEquals(0, grants)
    }

    @Test
    fun `the food card asks when the permission can be asked, opens Ruoka when it was denied`() {
        var days = 0
        var grants = 0
        var food by mutableStateOf(FoodUiState(access = FoodAccess.NEEDS))
        compose.setContent {
            TerveysTheme {
                Box(Modifier.width(160.dp).height(cardHeight(CardSize.SMALL, rowUnit()))) {
                    HomeCardView(HomeCard(HomeLayout.FOOD, CardSize.SMALL), HomeCardsData(MainUiState(access = Access.GRANTED, weights = HomeFixtures.weights), MainActions(), food, FoodActions(onOpenDay = { days++ }, onGrant = { grants++ }), HomeFixtures.steps, StepsActions()), Modifier.fillMaxSize())
                }
            }
        }
        node("foodCard").performClick()
        assertEquals(1, grants)
        assertEquals(0, days)
        food = FoodUiState(access = FoodAccess.DENIED)
        compose.waitForIdle()
        node("foodCard").performClick()
        assertEquals(1, days)
    }

    private var cell by mutableStateOf(HomeCard(HomeLayout.WEIGHT, CardSize.WIDE))

    /** One card at its grid size on a 360 dp screen (16 dp sides, 8 dp gap). */
    private fun cells() {
        val state = MainUiState(access = Access.GRANTED, weights = HomeFixtures.weights)
        compose.setContent {
            TerveysTheme {
                val unit = rowUnit()
                val width = if (cell.size == CardSize.SMALL) 160.dp else 328.dp
                Box(Modifier.width(width).height(cardHeight(cell.size, unit)).testTag("cell")) {
                    HomeCardView(cell, data(state), Modifier.fillMaxSize())
                }
            }
        }
    }

    @Test
    fun `small weight card shows the number and the chip in its half`() {
        cell = HomeCard(HomeLayout.WEIGHT, CardSize.SMALL)
        cells()
        for (tag in listOf("latestKg", "homePeriod")) assertInside(node(tag), node("cell"), tag)
        node("sparkRange").assertDoesNotExist()
    }

    @Test
    fun `wide food card has the long average and no button`() {
        cell = HomeCard(HomeLayout.FOOD, CardSize.WIDE)
        cells()
        val average = node("foodAverage")
        val text = compose.activity.getString(R.string.home_food_average_long, formatKcal(1900))
        average.assertTextEquals(text)
        node("homeFoodAdd").assertDoesNotExist()
    }

    @Test
    fun `big steps card shows the scale top`() {
        cell = HomeCard(HomeLayout.STEPS, CardSize.BIG)
        cells()
        node("stepsScaleTop").assertTextEquals(compose.activity.getString(R.string.steps_value, formatSteps(14_000)))
        cell = HomeCard(HomeLayout.STEPS, CardSize.WIDE)
        compose.waitForIdle()
        node("stepsScaleTop").assertDoesNotExist()
    }

    @Test
    fun `Kulutus card shows today, aktiivinen, the two-line average and opens Kulutus`() {
        cell = HomeCard(HomeLayout.BURN, CardSize.SMALL)
        cells()
        node("burnTodayValue").assertTextEquals(formatKcal(900))
        node("burnTodayActive").assertTextEquals(compose.activity.getString(R.string.burn_active_line, formatKcal(100)))
        node("burnAverage").assertContentDescriptionEquals(compose.activity.getString(R.string.home_burn_average_long, formatKcal(2250)))
        node("burnScaleTop").assertDoesNotExist()
        node("burnCard").performClick()
        assertEquals(1, burnOpened)
        cell = HomeCard(HomeLayout.BURN, CardSize.BIG)
        compose.waitForIdle()
        node("burnScaleTop").assertTextEquals(compose.activity.getString(R.string.food_kcal, formatKcal(3000)))
    }

    @Test
    fun `the default layout has no Kulutus card`() {
        home()
        node("homeSlot-burn").assertDoesNotExist()
        node("homeSlot-steps").assertIsDisplayed()
    }

    /**
     * home-card-layout design 2 / task 2.4: every card at every size at 360 dp and 130 % text keeps its
     * key parts inside the card. ROW_UNIT is the smallest value that passes.
     */
    @Test
    @Config(fontScale = 1.3f)
    fun `size matrix at 360 dp and font scale 1_3`() {
        val keys = mapOf(
            HomeLayout.WEIGHT to listOf("latestKg", "homePeriod"),
            HomeLayout.FOOD to listOf("foodTodayValue", "foodPeriod", "foodAverage"),
            HomeLayout.STEPS to listOf("stepsTodayValue", "stepsPeriod", "stepsAverage"),
            HomeLayout.BURN to listOf("burnTodayValue", "burnPeriod", "burnAverage"),
        )
        cells()
        for ((id, tags) in keys) for (size in CardSize.entries) {
            cell = HomeCard(id, size)
            compose.waitForIdle()
            val extra = when {
                size != CardSize.BIG -> emptyList()
                id == HomeLayout.WEIGHT -> listOf("sparkScaleTop", "sparkScaleBottom")
                id == HomeLayout.FOOD -> listOf("foodScaleTop")
                id == HomeLayout.BURN -> listOf("burnScaleTop")
                else -> listOf("stepsScaleTop")
            }
            for (tag in tags + extra) assertInside(node(tag), node("cell"), "$id/$size: $tag")
        }
    }
}
