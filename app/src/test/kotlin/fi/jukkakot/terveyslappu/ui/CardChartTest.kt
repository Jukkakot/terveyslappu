package fi.jukkakot.terveyslappu.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.chart.ChartLayout
import fi.jukkakot.terveyslappu.chart.ChartSeries
import fi.jukkakot.terveyslappu.chart.CustomChart
import fi.jukkakot.terveyslappu.chart.Period
import fi.jukkakot.terveyslappu.chart.SeriesKind
import fi.jukkakot.terveyslappu.chart.SeriesStyle
import fi.jukkakot.terveyslappu.home.CardSize
import fi.jukkakot.terveyslappu.home.HomeCard
import fi.jukkakot.terveyslappu.home.HomeLayout
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * The front page cards' library charts under the cards' own gestures (home-cards-on-chart-library
 * design 3, 5, 6): a tap on a chart opens the card's target, a long-press enters edit mode and
 * continues into the drag, a recomposition does not rebind. Made-up data.
 */
@RunWith(AndroidJUnit4::class)
// xhdpi: at Robolectric's default mdpi the fonts squeeze the weight and food charts to no height.
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xhdpi")
class CardChartTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val own = CustomChart(1, "Liike", listOf(ChartSeries(SeriesKind.WEIGHT, SeriesStyle.LINE), ChartSeries(SeriesKind.ENERGY, SeriesStyle.BARS)), ChartLayout.STACKED, Period.MONTH)
    private var state by mutableStateOf(MainUiState(access = Access.GRANTED, weights = HomeFixtures.weights, charts = listOf(own)))
    private val opened = mutableListOf<String>()

    private fun show(layout: HomeLayout = HomeLayout.DEFAULT) {
        state = state.copy(homeLayout = layout)
        compose.setContent {
            TerveysTheme {
                MainScreen(
                    state,
                    "0.1.1-test",
                    MainActions(
                        onHomeLayout = { state = state.copy(homeLayout = it) },
                        onScreen = { opened += "screen:$it" },
                        onOpenEnergy = { opened += "energy" },
                        onOpenChart = { opened += "chart:$it" },
                    ),
                    food = HomeFixtures.food,
                    foodActions = FoodActions(onOpenDay = { opened += "ruoka" }),
                    steps = HomeFixtures.steps,
                    stepsActions = StepsActions(onOpen = { opened += "steps" }),
                    burn = HomeFixtures.burn,
                    burnActions = BurnActions(onOpen = { opened += "burn" }),
                )
            }
        }
        compose.waitForIdle()
    }

    private fun node(tag: String) = compose.onNodeWithTag(tag, useUnmergedTree = true)

    /** A card's chart by the start of its content description (the description is on the chart's view). */
    private fun chart(description: Int): SemanticsNodeInteraction {
        val start = compose.activity.getString(description).substringBefore("%")
        return compose.onNode(hasContentDescription(start, substring = true), useUnmergedTree = true)
    }

    private fun editing() = compose.onAllNodes(hasTestTag("editTitle"), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()

    private fun tap(node: SemanticsNodeInteraction, expected: String) {
        opened.clear()
        node.performClick()
        compose.waitForIdle()
        assertEquals(listOf(expected), opened)
    }

    @Test
    fun `a tap on each feature card's chart opens its target`() {
        show(HomeLayout(listOf(HomeCard(HomeLayout.WEIGHT, CardSize.WIDE), HomeCard(HomeLayout.FOOD, CardSize.SMALL), HomeCard(HomeLayout.STEPS, CardSize.SMALL), HomeCard(HomeLayout.BURN, CardSize.SMALL))))
        tap(node("foodToday"), "ruoka")
        tap(chart(R.string.home_food_bars), "energy")
        tap(chart(R.string.home_steps_bars), "steps")
        tap(chart(R.string.home_burn_bars), "burn")
        tap(chart(R.string.home_spark), "screen:${Screen.HISTORY}")
        assertTrue(compose.activity.chartViews().all { it.card }, "every card chart is in card mode")
        assertEquals(4, compose.activity.chartViews().size)
        assertTrue(compose.activity.chartViews().all { it.height > 0 }, "every tapped chart has a height")
    }

    @Test
    fun `a tap on an own chart card's plot opens its tab, the plots keep their tags`() {
        show(HomeLayout(listOf(HomeCard(own.cardId, CardSize.WIDE))))
        node("plot-WEIGHT").assertExists()
        chart(R.string.custom_chart_description).assertExists()
        tap(node("plot-ENERGY"), "chart:${own.id}")
        tap(node("plot-WEIGHT"), "chart:${own.id}")
    }

    @Test
    fun `a long-press on a chart enters edit mode`() {
        show()
        chart(R.string.home_steps_bars).performTouchInput { longClick() }
        compose.waitForIdle()
        assertTrue(editing(), "edit mode on")
    }

    @Test
    fun `a long-press then drag on a chart moves the card`() {
        show()
        val from = chart(R.string.home_steps_bars).getUnclippedBoundsInRoot()
        val to = node("homeSlot-food").getUnclippedBoundsInRoot()
        val density = compose.density
        val dx = with(density) { ((to.left + to.right - from.left - from.right) / 2).toPx() }
        val dy = with(density) { ((to.top + to.bottom - from.top - from.bottom) / 2).toPx() }
        chart(R.string.home_steps_bars).performTouchInput {
            down(center)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + 200)
            repeat(20) {
                moveBy(Offset(dx / 20, dy / 20))
                advanceEventTime(16)
            }
            advanceEventTime(200)
            up()
        }
        compose.waitForIdle()
        assertTrue(editing(), "edit mode on")
        val steps = node("homeSlot-steps").getUnclippedBoundsInRoot()
        val food = node("homeSlot-food").getUnclippedBoundsInRoot()
        assertTrue(steps.left < food.left, "steps moved before food: $steps, $food")
    }

    @Test
    fun `a recomposition does not rebind the charts`() {
        show()
        val before = compose.activity.chartViews().associateWith { it.data }
        assertEquals(3, before.size)
        // Edit mode recomposes every card (dimmed, strip on top) with the same data.
        node("homeSlot-food").performTouchInput { longClick() }
        compose.waitForIdle()
        assertTrue(editing())
        val after = compose.activity.chartViews()
        assertEquals(before.keys, after.toSet(), "the same views")
        after.forEach { assertSame(before.getValue(it), it.data, "data kept") }
    }
}
