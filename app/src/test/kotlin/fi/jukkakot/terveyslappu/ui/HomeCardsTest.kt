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
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
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
class HomeCardsTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private fun data(state: MainUiState) = HomeCardsData(state, MainActions(), HomeFixtures.food, FoodActions(), HomeFixtures.steps, StepsActions())

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
        for (tag in listOf("checkNotice", "latestCard", "weighIn", "foodCard", "homeFoodAdd", "foodPeriod", "stepsCard", "stepsPeriod", "stepsAverage")) {
            node(tag).assertIsDisplayed()
            assertTrue(bounds(tag).bottom <= screen.bottom, "$tag ends at ${bounds(tag).bottom}, screen at ${screen.bottom}")
        }
    }

    @Test
    @Config(fontScale = 1.3f)
    fun `default layout at 130 percent text keeps Punnitse, + Annos and the steps average`() {
        home()
        for (tag in listOf("weighIn", "homeFoodAdd", "stepsAverage")) node(tag).assertIsDisplayed()
        assertInside(node("weighIn"), node("homeSlot-weight"), "weighIn")
        assertInside(node("homeFoodAdd"), node("homeSlot-food"), "homeFoodAdd")
        assertInside(node("stepsAverage"), node("homeSlot-steps"), "stepsAverage")
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
    fun `small weight card shows the number, the chip and Punnitse in its half`() {
        cell = HomeCard(HomeLayout.WEIGHT, CardSize.SMALL)
        cells()
        for (tag in listOf("latestKg", "change", "weighIn", "homePeriod")) assertInside(node(tag), node("cell"), tag)
        node("sparkRange").assertDoesNotExist()
    }

    @Test
    fun `wide food card has the long average and + Annos on one row`() {
        cell = HomeCard(HomeLayout.FOOD, CardSize.WIDE)
        cells()
        val average = node("foodAverage")
        val text = compose.activity.getString(R.string.home_food_average_long, formatKcal(1900))
        average.assertTextEquals(text)
        val a = average.getUnclippedBoundsInRoot()
        val add = bounds("homeFoodAdd")
        assertTrue(add.top < a.bottom && a.top < add.bottom, "+ Annos on the average's row")
        assertTrue(add.left >= a.right, "+ Annos at the row's end")
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

    /**
     * home-card-layout design 2 / task 2.4: every card at every size at 360 dp and 130 % text keeps its
     * key parts inside the card. ROW_UNIT is the smallest value that passes.
     */
    @Test
    @Config(fontScale = 1.3f)
    fun `size matrix at 360 dp and font scale 1_3`() {
        val keys = mapOf(
            HomeLayout.WEIGHT to listOf("latestKg", "change", "weighIn", "homePeriod"),
            HomeLayout.FOOD to listOf("foodTodayValue", "foodPeriod", "homeFoodAdd", "foodAverage"),
            HomeLayout.STEPS to listOf("stepsTodayValue", "stepsPeriod", "stepsAverage"),
        )
        cells()
        for ((id, tags) in keys) for (size in CardSize.entries) {
            cell = HomeCard(id, size)
            compose.waitForIdle()
            val extra = when {
                size != CardSize.BIG -> emptyList()
                id == HomeLayout.WEIGHT -> listOf("sparkScaleTop", "sparkScaleBottom")
                id == HomeLayout.FOOD -> listOf("foodScaleTop")
                else -> listOf("stepsScaleTop")
            }
            for (tag in tags + extra) assertInside(node(tag), node("cell"), "$id/$size: $tag")
        }
    }
}
