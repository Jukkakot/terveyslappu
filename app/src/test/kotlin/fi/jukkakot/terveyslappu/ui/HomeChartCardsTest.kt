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
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Own charts on the front page (home-screen "Chart cards"); made-up data. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w411dp-h891dp")
class HomeChartCardsTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private var list = ChartList()
    private var state by mutableStateOf(MainUiState(access = Access.GRANTED, weights = HomeFixtures.weights))
    private var saved: HomeLayout? = null
    private var opened: Int? = null

    private fun add(name: String, vararg kinds: SeriesKind = SeriesKind.entries.toTypedArray(), layout: ChartLayout = ChartLayout.STACKED): CustomChart {
        val (next, stored) = CustomCharts.add(list, CustomChart(0, name, kinds.map { ChartSeries(it, CustomCharts.defaultStyle(it)) }, layout, Period.MONTH))
        list = next
        state = state.copy(charts = list.charts)
        return stored
    }

    private fun show() = compose.setContent {
        TerveysTheme {
            MainScreen(
                state,
                "0.1.1-test",
                MainActions(
                    onHomeLayout = { saved = it; state = state.copy(homeLayout = it) },
                    onOpenChart = { opened = it; state = state.copy(screen = Screen.HISTORY, historyChart = it) },
                    onChartShown = { state = state.copy(historyChart = null) },
                    onBack = { state = state.back() },
                    onSaveChart = { chart ->
                        val (next, stored) = CustomCharts.add(list, chart)
                        list = next
                        state = state.copy(charts = list.charts)
                        stored.id
                    },
                ),
                food = HomeFixtures.food,
                steps = HomeFixtures.steps,
            )
        }
    }

    private fun node(tag: String) = compose.onNodeWithTag(tag, useUnmergedTree = true)
    private fun exists(tag: String) = compose.onAllNodes(hasTestTag(tag), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()

    private fun enter() {
        node("homeSlot-food").performTouchInput { longClick() }
        compose.waitForIdle()
    }

    private fun openAdd() {
        node("homeGrid").performScrollToNode(hasTestTag("editAdd"))
        node("editAdd").performClick()
    }

    @Test
    fun `add Liike from Lisaa kortti then Valmis shows it last and wide`() {
        val liike = add("Liike")
        add("Ilta")
        show()
        enter()
        openAdd()
        // Charts in tab order, then Uusi kaavio.
        val tops = listOf("editAddItem-chart:1", "editAddItem-chart:2", "editAddNewChart").map { node(it).getUnclippedBoundsInRoot().top.value }
        assertEquals(tops.sorted(), tops)
        node("editAddItem-${liike.cardId}").performClick()
        node("editDone").performClick()
        assertEquals(HomeCard(liike.cardId, CardSize.WIDE), saved!!.shown.last())
        assertTrue(exists("chartCard-1"))
    }

    @Test
    fun `new chart from the front page has a Historia tab even after Peruuta`() {
        show()
        enter()
        openAdd()
        node("editAddNewChart").performClick()
        node("chartSave").performClick()
        compose.waitForIdle()
        assertEquals(listOf("Kaavio 1"), list.charts.map { it.name })
        assertTrue(exists("homeSlot-chart:1"), "the card is in the edit draft")
        node("editCancel").performClick()
        compose.waitForIdle()
        assertEquals(false, exists("homeSlot-chart:1"))
        assertEquals(1, state.charts.size, "the chart stays")
    }

    @Test
    fun `Piilota on a chart card, then it is in Lisaa kortti and returns wide`() {
        val liike = add("Liike")
        state = state.copy(homeLayout = HomeLayout.DEFAULT.addChart(liike.cardId).resize(liike.cardId, CardSize.SMALL))
        show()
        enter()
        node("homeGrid").performScrollToNode(hasTestTag("editMenu-${liike.cardId}"))
        node("editMenu-${liike.cardId}").performClick()
        node("editHide-${liike.cardId}").performClick()
        node("editDone").performClick()
        assertEquals(false, saved!!.contains(liike.cardId))
        enter()
        openAdd()
        node("editAddItem-${liike.cardId}").performClick()
        node("editDone").performClick()
        assertEquals(HomeCard(liike.cardId, CardSize.WIDE), saved!!.shown.last())
    }

    @Test
    fun `Palauta oletus clears chart cards`() {
        val liike = add("Liike")
        state = state.copy(homeLayout = HomeLayout.DEFAULT.addChart(liike.cardId))
        show()
        enter()
        node("homeGrid").performScrollToNode(hasTestTag("editReset"))
        node("editReset").performClick()
        node("editDone").performClick()
        assertEquals(HomeLayout.DEFAULT, saved)
        assertEquals(1, state.charts.size)
    }

    @Test
    fun `a tap opens Historia on that tab and back returns home`() {
        val liike = add("Liike", SeriesKind.STEPS)
        add("Ilta")
        state = state.copy(homeLayout = HomeLayout.DEFAULT.addChart(liike.cardId))
        show()
        node("homeGrid").performScrollToNode(hasTestTag("chartCard-1"))
        node("chartCard-1").performClick()
        compose.waitForIdle()
        assertEquals(1, opened)
        node("page-chart-1").let { assertTrue(exists("page-chart-1")) }
        state = state.back()
        compose.waitForIdle()
        assertTrue(exists("chartCard-1"))
    }

    @Test
    fun `the chart card's label follows the tab's period`() {
        val liike = add("Liike")
        list = CustomCharts.update(list, liike.copy(period = Period.QUARTER))
        state = state.copy(charts = list.charts, homeLayout = HomeLayout.DEFAULT.addChart(liike.cardId))
        show()
        node("homeGrid").performScrollToNode(hasTestTag("chartCardPeriod"))
        node("chartCardPeriod").assertTextEquals("3 kk")
        state = state.copy(charts = list.charts.map { it.copy(period = Period.Custom(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 15))) })
        compose.waitForIdle()
        node("chartCardPeriod").assertTextEquals("${formatShortDay(LocalDate.of(2026, 9, 1))}–${formatShortDay(LocalDate.of(2026, 10, 15))}")
    }

    @Test
    fun `home reads steps from the earlier of the steps card and a 1 v chart card`() {
        val today = LocalDate.of(2026, 11, 15)
        val year = CustomChart(1, "Vuosi", listOf(ChartSeries(SeriesKind.STEPS, CustomCharts.defaultStyle(SeriesKind.STEPS))), ChartLayout.STACKED, Period.YEAR)
        assertEquals(today.minusDays(364), CustomCharts.homeStepsFrom(7, listOf("weight", year.cardId), listOf(year), today))
        assertEquals(today.minusDays(6), CustomCharts.homeStepsFrom(7, listOf("weight"), listOf(year), today), "not on the front page")
        assertEquals(null, CustomCharts.homeStepsFrom(7, listOf(year.cardId), listOf(year.copy(period = Period.All)), today))
    }
}

/** A chart card at every size inside its grid cell (home-card-layout's size matrix, extended). */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w360dp-h760dp", fontScale = 1.3f)
class ChartCardSizesTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val chart = CustomChart(1, "Paino, ruoka ja askeleet", SeriesKind.entries.map { ChartSeries(it, CustomCharts.defaultStyle(it)) }, ChartLayout.STACKED, Period.MONTH)
    private var cell by mutableStateOf(HomeCard(chart.cardId, CardSize.SMALL))
    private var layout by mutableStateOf(ChartLayout.STACKED)

    private fun node(tag: String) = compose.onNodeWithTag(tag, useUnmergedTree = true)
    private fun exists(tag: String) = compose.onAllNodes(hasTestTag(tag), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()

    private fun cells() {
        compose.setContent {
            TerveysTheme {
                val state = MainUiState(access = Access.GRANTED, weights = HomeFixtures.weights, charts = listOf(chart.copy(layout = layout)))
                val unit = rowUnit()
                val width = if (cell.size == CardSize.SMALL) 160.dp else 328.dp
                Box(Modifier.width(width).height(cardHeight(cell.size, unit)).testTag("cell")) {
                    HomeCardView(cell, HomeCardsData(state, MainActions(), HomeFixtures.food, FoodActions(), HomeFixtures.steps, StepsActions()), Modifier.fillMaxSize())
                }
            }
        }
    }

    @Test
    fun `size matrix at 360 dp and font scale 1_3`() {
        cells()
        for (l in ChartLayout.entries) for (size in CardSize.entries) {
            layout = l
            cell = HomeCard(chart.cardId, size)
            compose.waitForIdle()
            for (tag in listOf("chartCardTitle", "chartCardChart", "chartCardPeriod")) assertInside(node(tag), node("cell"), "$l/$size: $tag")
            assertEquals(size != CardSize.SMALL, exists("chartDates"), "$l/$size dates")
            assertEquals(size == CardSize.BIG, exists("scaleTop-WEIGHT"), "$l/$size scale top")
        }
    }

    @Test
    fun `small card shows the name, the chart and the period, no dates`() {
        cells()
        node("chartCardPeriod").assertTextEquals("1 kk")
        assertTrue(exists("plot-WEIGHT"))
        assertEquals(false, exists("chartDates"))
    }

    @Test
    fun `big card shows the scale tops`() {
        cell = HomeCard(chart.cardId, CardSize.BIG)
        cells()
        node("scaleTop-STEPS").assertTextEquals(compose.activity.getString(fi.jukkakot.terveyslappu.R.string.steps_value, formatSteps(14_000)))
        assertTrue(exists("chartLegend"))
    }
}
