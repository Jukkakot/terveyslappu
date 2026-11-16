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
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.chart.CardCharts
import fi.jukkakot.terveyslappu.chart.ChartLayout
import fi.jukkakot.terveyslappu.chart.ChartList
import fi.jukkakot.terveyslappu.chart.ChartSeries
import fi.jukkakot.terveyslappu.chart.CustomChart
import fi.jukkakot.terveyslappu.chart.CustomCharts
import fi.jukkakot.terveyslappu.chart.Period
import fi.jukkakot.terveyslappu.chart.SeriesKind
import fi.jukkakot.terveyslappu.home.CardSize
import fi.jukkakot.terveyslappu.home.HomeCard
import fi.jukkakot.terveyslappu.home.HomeLayout
import fi.jukkakot.terveyslappu.ui.HomeFixtures.assertInside
import fi.jukkakot.terveyslappu.weight.weightEntry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The dashboard front page (home-dashboard-menu): plots at least 56 dp, no card buttons; made-up data. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp")
class HomeDashboardTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private var cell by mutableStateOf(HomeCard(HomeLayout.WEIGHT, CardSize.WIDE))
    private var weights = HomeFixtures.weights
    private var list = ChartList()
    private var charts = emptyList<CustomChart>()

    private fun node(tag: String) = compose.onNodeWithTag(tag, useUnmergedTree = true)
    private fun exists(tag: String) = compose.onAllNodes(hasTestTag(tag), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
    private fun height(tag: String): Dp = node(tag).getUnclippedBoundsInRoot().let { it.bottom - it.top }

    private fun addChart(name: String, layout: ChartLayout, vararg kinds: SeriesKind): CustomChart {
        val (next, stored) = CustomCharts.add(list, CustomChart(0, name, kinds.map { ChartSeries(it, CustomCharts.defaultStyle(it)) }, layout, Period.MONTH))
        list = next
        charts = next.charts
        return stored
    }

    /** One card at its grid size on a 360 dp screen (16 dp sides, 8 dp gap), as HomeCardsTest. */
    private fun cells() {
        compose.setContent {
            TerveysTheme {
                val state = MainUiState(access = Access.GRANTED, weights = weights, charts = charts)
                val unit = rowUnit()
                val width = if (cell.size == CardSize.SMALL) 160.dp else 328.dp
                Box(Modifier.width(width).height(cardHeight(cell.size, unit)).testTag("cell")) {
                    HomeCardView(cell, HomeCardsData(state, MainActions(), HomeFixtures.food, FoodActions(), HomeFixtures.steps, StepsActions(), HomeFixtures.burn, BurnActions()), Modifier.fillMaxSize())
                }
            }
        }
    }

    /** The plot's height: the chart minus its inset, or the own chart's panels together. */
    private fun plot(id: String): Dp = when (id) {
        HomeLayout.WEIGHT -> height("chart-weight") - CardCharts.WEIGHT_INSET_DP.dp * 2
        HomeLayout.FOOD -> height("chart-food")
        HomeLayout.STEPS -> height("chart-steps")
        HomeLayout.BURN -> height("chart-burn")
        else -> listOf("plot-OVERLAID") .plus(SeriesKind.entries.map { "plot-${it.name}" }).filter(::exists).sumOf { height(it).value.toDouble() }.toFloat().dp
    }

    private fun measureAll(scale: Float) {
        val stacked = addChart("Kaikki", ChartLayout.STACKED, *SeriesKind.entries.toTypedArray())
        val overlaid = addChart("Päällekkäin", ChartLayout.OVERLAID, SeriesKind.WEIGHT, SeriesKind.ENERGY, SeriesKind.STEPS)
        cells()
        val shorts = mutableListOf<String>()
        val ids = listOf(HomeLayout.WEIGHT, HomeLayout.FOOD, HomeLayout.STEPS, HomeLayout.BURN, stacked.cardId, overlaid.cardId)
        for (id in ids) for (size in CardSize.entries) {
            cell = HomeCard(id, size)
            compose.waitForIdle()
            val dp = plot(id)
            println("PLOT scale=$scale $id/$size = ${dp.value} dp")
            if (dp < 56.dp) shorts += "$id/$size at $scale: plot $dp"
        }
        assertTrue(shorts.isEmpty(), "under 56 dp: $shorts")
    }

    @Test
    fun `every card chart has a 56 dp plot at every size`() = measureAll(1.0f)

    @Test
    @Config(fontScale = 1.3f)
    fun `every card chart has a 56 dp plot at 130 percent text`() = measureAll(1.3f)

    @Test
    fun `a week's spike shows in the wide weight card`() {
        val now = Instant.now()
        val kgs = listOf(97.3, 97.2, 98.0, 97.2, 97.2, 97.1, 97.0)
        weights = kgs.mapIndexed { i, kg -> weightEntry("s$i", now.minusSeconds((6 - i) * 86_400L + 3_600), null, kg, "me", "me") }
        cell = HomeCard(HomeLayout.WEIGHT, CardSize.WIDE)
        cells()
        val summary = fi.jukkakot.terveyslappu.chart.HomeSummary.of(weights, now, 7, emptySet())!!
        val scale = summary.sparkScale!!
        assertEquals(96.5, scale.min, 0.01)
        assertEquals(98.5, scale.max, 0.01)
        val plot = plot(HomeLayout.WEIGHT)
        // The 56 dp floor itself is the matrix test above.
        val rise = (98.0 - 97.2) / (scale.max - scale.min)
        assertTrue(rise * plot.value >= 0.25f * plot.value, "the spike is a quarter of the plot")
    }

    @Test
    fun `cards hold no action buttons`() {
        cells()
        for (size in CardSize.entries) {
            for (id in listOf(HomeLayout.WEIGHT, HomeLayout.FOOD)) {
                cell = HomeCard(id, size)
                compose.waitForIdle()
                assertTrue(!exists("weighIn") && !exists("homeFoodAdd"), "$id/$size has a button")
            }
        }
    }

    @Test
    fun `small weight card with no weights points to the menu`() {
        weights = emptyList()
        cell = HomeCard(HomeLayout.WEIGHT, CardSize.SMALL)
        cells()
        node("homeEmpty").assertExists()
        assertInside(node("homeEmpty"), node("cell"), "homeEmpty")
    }

    private fun foodFits() {
        cell = HomeCard(HomeLayout.FOOD, CardSize.SMALL)
        cells()
        for (tag in listOf("foodToday", "foodTodayValue", "foodPeriod", "foodAverage", "foodBars")) assertInside(node(tag), node("cell"), tag)
    }

    @Test
    fun `small food card keeps its number and bars`() = foodFits()

    @Test
    @Config(fontScale = 1.3f)
    fun `small food card keeps its number and bars at 130 percent text`() = foodFits()
}
