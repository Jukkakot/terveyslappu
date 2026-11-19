package fi.jukkakot.terveyslappu.ui

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.click
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.chart.ChartLayout
import fi.jukkakot.terveyslappu.chart.ChartList
import fi.jukkakot.terveyslappu.chart.ChartSeries
import fi.jukkakot.terveyslappu.chart.CustomChart
import fi.jukkakot.terveyslappu.chart.CustomCharts
import fi.jukkakot.terveyslappu.chart.Period
import fi.jukkakot.terveyslappu.chart.SeriesKind
import fi.jukkakot.terveyslappu.chart.SeriesStyle
import fi.jukkakot.terveyslappu.steps.StepsAccess
import fi.jukkakot.terveyslappu.steps.StepsUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Own charts in Historia (weight-chart "Custom chart tabs", "Chart crosshair", "Chart editor"); made-up data. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w411dp-h891dp")
class HistoryChartsTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private var list = ChartList()
    private var state by mutableStateOf(MainUiState(access = Access.GRANTED, weights = HomeFixtures.weights, screen = Screen.HISTORY, period = Period.MONTH))
    private var food by mutableStateOf(HomeFixtures.food)
    private var steps by mutableStateOf(HomeFixtures.steps)
    private val stepsShown = mutableListOf<LocalDate?>()
    private var foodGrants = 0
    private var stepsGrants = 0

    private fun add(name: String, vararg kinds: SeriesKind = SeriesKind.entries.toTypedArray(), layout: ChartLayout = ChartLayout.STACKED): Int {
        val chart = CustomChart(0, name, kinds.map { ChartSeries(it, CustomCharts.defaultStyle(it)) }, layout, Period.MONTH)
        val (next, stored) = CustomCharts.add(list, chart)
        list = next
        state = state.copy(charts = list.charts)
        return stored.id
    }

    private fun show() = compose.setContent {
        TerveysTheme {
            MainScreen(
                state,
                "0.1.1-test",
                MainActions(
                    onPeriod = { state = state.copy(period = it) },
                    onBack = { state = state.back() },
                    onScreen = { state = state.copy(screen = it) },
                    onSaveChart = { chart ->
                        if (chart.id == 0) {
                            val (next, stored) = CustomCharts.add(list, chart)
                            list = next
                            state = state.copy(charts = list.charts)
                            stored.id
                        } else {
                            list = CustomCharts.update(list, chart)
                            state = state.copy(charts = list.charts)
                            chart.id
                        }
                    },
                    onDeleteChart = { id -> list = CustomCharts.delete(list, id); state = state.copy(charts = list.charts) },
                    onChartPeriod = { id, p -> list = CustomCharts.update(list, list.charts.first { it.id == id }.copy(period = p)); state = state.copy(charts = list.charts) },
                ),
                food = food,
                foodActions = FoodActions(onGrant = { foodGrants++ }),
                steps = steps,
                stepsActions = StepsActions(onShown = { stepsShown += it }, onGrant = { stepsGrants++ }),
            )
        }
    }

    private fun node(tag: String) = compose.onNodeWithTag(tag, useUnmergedTree = true)
    private fun exists(tag: String) = compose.onAllNodes(hasTestTag(tag), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()

    private fun openTab(tag: String) {
        node(tag).performScrollTo().performClick()
        compose.waitForIdle()
    }

    @Test
    fun `two charts give eight tabs in order`() {
        add("Paino ja ruoka")
        add("Liike")
        show()
        val tags = listOf("tab-WEIGHT", "tab-BMI", "tab-ENERGY", "tab-STEPS", "tab-BURN", "tab-chart-1", "tab-chart-2", "tab-NEW")
        tags.forEach { assertTrue(exists(it), it) }
        val lefts = tags.map { node(it).getUnclippedBoundsInRoot().left.value }
        assertEquals(lefts.sorted(), lefts)
    }

    @Test
    fun `the last tab is a plus icon named Uusi kaavio, at least 48 dp wide`() {
        show()
        val tab = node("tab-NEW")
        compose.onNode(androidx.compose.ui.test.hasContentDescription(compose.activity.getString(R.string.chart_new)) and androidx.compose.ui.test.hasAnyAncestor(androidx.compose.ui.test.hasTestTag("tab-NEW")), useUnmergedTree = true).assertExists()
        tab.assertHasClickAction()
        val box = tab.getUnclippedBoundsInRoot()
        assertTrue(box.right - box.left >= 48.dp && box.bottom - box.top >= 48.dp, "touch target: $box")
        assertTrue(compose.onAllNodes(androidx.compose.ui.test.hasText(compose.activity.getString(R.string.chart_new)) and androidx.compose.ui.test.hasAnyAncestor(androidx.compose.ui.test.hasTestTag("tab-NEW")), useUnmergedTree = true).fetchSemanticsNodes().isEmpty(), "no text on the tab")
        openTab("tab-NEW")
        node("page-newChart").assertIsDisplayed()
    }

    @Test
    fun `no charts yet shows the Uusi kaavio page`() {
        show()
        openTab("tab-NEW")
        node("page-newChart").assertIsDisplayed()
        node("chartNew").assertIsDisplayed()
    }

    @Test
    fun `Historia reopens on Paino`() {
        add("Liike")
        show()
        openTab("tab-chart-1")
        node("page-chart-1").assertIsDisplayed()
        state = state.copy(screen = Screen.HOME)
        compose.waitForIdle()
        state = state.copy(screen = Screen.HISTORY)
        compose.waitForIdle()
        node("page-WEIGHT").assertIsDisplayed()
    }

    @Test
    fun `back clears historyChart`() {
        val s = MainUiState(screen = Screen.HISTORY, historyChart = 3).back()
        assertEquals(null, s.historyChart)
        assertEquals(null, MainUiState(screen = Screen.HISTORY, historyChart = 3).openFood().historyChart)
    }

    @Test
    fun `a card opens its chart's tab`() {
        add("Liike")
        add("Ilta")
        state = state.copy(historyChart = 2)
        show()
        node("page-chart-2").assertIsDisplayed()
    }

    @Test
    fun `changing the chart's period leaves Paino's period alone and back`() {
        add("Liike")
        show()
        openTab("tab-chart-1")
        node("period-90").performClick()
        compose.waitForIdle()
        assertEquals(Period.QUARTER, list.charts.single().period)
        assertEquals(Period.MONTH, state.period)
        openTab("tab-WEIGHT")
        node("period-365").performClick()
        compose.waitForIdle()
        assertEquals(Period.YEAR, state.period)
        assertEquals(Period.QUARTER, list.charts.single().period)
    }

    @Test
    fun `opening a chart with steps reads its start and asks no permission`() {
        food = FoodUiState(access = FoodAccess.NEEDS)
        add("Liike")
        show()
        stepsShown.clear()
        openTab("tab-chart-1")
        assertEquals(HomeFixtures.today.minusDays(29), stepsShown.last())
        assertEquals(0, foodGrants)
        node("panelMessage-ENERGY").assertIsDisplayed()
    }

    @Test
    fun `weights failed shows the failure notice`() {
        add("Liike")
        state = state.copy(weights = null, loadFailed = true, historyChart = 1)
        show()
        assertEquals(false, exists("page-chart-1"))
        compose.onNodeWithTag("weights").assertDoesNotExist()
    }

    private fun views() = compose.activity.chartViews()

    /** Taps [day] in the middle of the plot of the panel [tag] (the view at [index] on screen). */
    private fun tapDay(day: LocalDate, tag: String, index: Int) {
        val view = views()[index]
        node(tag).performTouchInput { click(androidx.compose.ui.geometry.Offset(view.pixelX(day), view.plotMiddle)) }
        compose.waitForIdle()
    }

    @Test
    fun `a tap shows the marker card and tapping the same day again clears it`() {
        add("Liike")
        show()
        openTab("tab-chart-1")
        assertEquals(false, exists("chartMarker"))
        val day = HomeFixtures.today.minusDays(5)
        tapDay(day, "plot-WEIGHT", 0)
        node("chartMarkerHead").assertTextContains(formatShortDay(day), substring = true)
        tapDay(day, "plot-WEIGHT", 0)
        assertEquals(false, exists("chartMarker"))
    }

    @Test
    fun `a tap between weights picks the nearest weight`() {
        add("Liike")
        show()
        openTab("tab-chart-1")
        // Weights 5 and 9 days back: a tap 6 days back chooses 5 days back.
        tapDay(HomeFixtures.today.minusDays(6), "plot-WEIGHT", 0)
        node("chartMarkerHead").assertTextContains(formatShortDay(HomeFixtures.today.minusDays(5)), substring = true)
    }

    @Test
    fun `a sideways drag on the plot scrubs and keeps the tab`() {
        add("Liike")
        add("Ilta")
        show()
        openTab("tab-chart-1")
        val view = views()[0]
        node("plot-WEIGHT").performTouchInput {
            val y = view.plotMiddle
            down(androidx.compose.ui.geometry.Offset(view.pixelX(HomeFixtures.today.minusDays(20)), y))
            for (i in 1..10) moveTo(androidx.compose.ui.geometry.Offset(view.pixelX(HomeFixtures.today.minusDays(20L - i)), y))
            up()
        }
        compose.waitForIdle()
        node("page-chart-1").assertIsDisplayed()
        node("chartMarker").assertIsDisplayed()
    }

    @Test
    fun `the grant button in a panel still asks and a tap outside the plots chooses nothing`() {
        steps = StepsUiState(access = StepsAccess.NEEDS)
        add("Liike")
        show()
        openTab("tab-chart-1")
        node("grant-STEPS").performClick()
        assertEquals(1, stepsGrants)
        assertEquals(false, exists("chartMarker"))
        node("panelMessage-STEPS").performTouchInput { click(center) }
        assertEquals(false, exists("chartMarker"), "a tap outside the drawn plots places nothing")
    }

    @Test
    fun `Edellinen paiva with no choice picks today and announces the card`() {
        add("Liike")
        show()
        openTab("tab-chart-1")
        val chart = node("customChart-1").fetchSemanticsNode()
        val action = chart.config[SemanticsActions.CustomActions].first { it.label == compose.activity.getString(fi.jukkakot.terveyslappu.R.string.chart_previous_day) }
        compose.runOnIdle { action.action() }
        compose.waitForIdle()
        node("chartMarkerHead").assertTextContains(formatShortDay(HomeFixtures.today), substring = true)
        assertTrue(node("customChart-1").announced()!!.contains(formatShortDay(HomeFixtures.today)))
        assertTrue(node("customChart-1").fetchSemanticsNode().config.contains(SemanticsProperties.LiveRegion))
    }

    @Test
    fun `the choice stays on reload and clears when Kaikki starts later than its day`() {
        list = ChartList()
        val id = add("Liike", SeriesKind.STEPS)
        list = CustomCharts.update(list, list.charts.single().copy(period = Period.All))
        state = state.copy(charts = list.charts)
        show()
        openTab("tab-chart-$id")
        // Kaikki starts at the first step day (5 days back).
        val first = HomeFixtures.today.minusDays(5)
        tapDay(first, "plot-STEPS", 0)
        node("chartMarkerHead").assertTextContains(formatShortDay(first), substring = true)
        steps = steps.copy(days = steps.days)
        compose.waitForIdle()
        node("chartMarker").assertIsDisplayed()
        steps = steps.copy(days = steps.days!!.filter { it.day >= HomeFixtures.today.minusDays(1) })
        compose.waitForIdle()
        assertEquals(false, exists("chartMarker"))
    }

    @Test
    fun `a period change clears the choice`() {
        add("Liike")
        show()
        openTab("tab-chart-1")
        tapDay(HomeFixtures.today.minusDays(5), "plot-WEIGHT", 0)
        node("chartMarker").assertIsDisplayed()
        list = CustomCharts.update(list, list.charts.single().copy(period = Period.WEEK))
        state = state.copy(charts = list.charts)
        compose.waitForIdle()
        assertEquals(false, exists("chartMarker"))
    }

    @Test
    fun `zooming one panel shows the same days in the others, a period change shows all again`() {
        add("Liike")
        show()
        openTab("tab-chart-1")
        val (weight, energy) = views().take(2)
        compose.runOnIdle {
            energy.zoom(3f, 1f, energy.viewPortHandler.contentRect.centerX(), 0f)
            energy.gestureListeners.forEach { it.onChartScale(android.view.MotionEvent.obtain(0, 0, android.view.MotionEvent.ACTION_MOVE, 0f, 0f, 0), 3f, 1f) }
        }
        compose.waitForIdle()
        assertEquals(energy.lowestVisibleX, weight.lowestVisibleX, 0.01f)
        assertEquals(energy.highestVisibleX, weight.highestVisibleX, 0.01f)
        assertTrue(weight.visibleXRange < 15f)
        list = CustomCharts.update(list, list.charts.single().copy(period = Period.WEEK))
        state = state.copy(charts = list.charts)
        compose.waitForIdle()
        assertEquals(7f, views()[0].visibleXRange, 0.01f)
    }

    @Test
    fun `stacked panels start and end at the same x`() {
        add("Liike")
        show()
        openTab("tab-chart-1")
        val drawn = views()
        assertTrue(drawn.size >= 3)
        val lefts = drawn.map { it.viewPortHandler.contentLeft }.distinct()
        val rights = drawn.map { it.viewPortHandler.contentRight }.distinct()
        assertEquals(1, lefts.size, "$lefts")
        assertEquals(1, rights.size, "$rights")
    }

    @Test
    fun `Paino and BMI show the library chart and a tap chooses a weight`() {
        compose.setContent {
            TerveysTheme {
                MainScreen(
                    state,
                    "0.1.1-test",
                    MainActions(onPeriod = { state = state.copy(period = it) }),
                    profile = fi.jukkakot.terveyslappu.profile.OwnerProfile(birthYear = 1990, heightCm = 180),
                )
            }
        }
        compose.waitForIdle()
        node("chart").assertIsDisplayed()
        val day = HomeFixtures.today.minusDays(9)
        tapDay(day, "plot-weight", 0)
        node("chartMarkerHead").assertTextContains(formatShortDay(day), substring = true)
        assertTrue(node("chart").announced()!!.contains("paino"))
        openTab("tab-BMI")
        assertEquals(false, exists("chartMarker"), "a tab change clears the choice")
        val bmi = views().first { it.panel?.bands?.isNotEmpty() == true }
        assertTrue(bmi.leftLabels().none { "kg" in it })
    }

    @Test
    fun `a swipe on the statistics switches the tab`() {
        show()
        compose.waitForIdle()
        node("tab-WEIGHT").assertIsDisplayed()
        node("stats").performTouchInput { swipeLeft() }
        compose.waitForIdle()
        assertTrue(node("tab-BMI").fetchSemanticsNode().config[SemanticsProperties.Selected])
    }

    @Test
    fun `new chart defaults (1 kk) and the last data stays on`() {
        add("Kaavio 1")
        show()
        openTab("tab-NEW")
        node("chartNew").performClick()
        node("chartName").assertTextContains("Kaavio 2")
        node("chartStyle-WEIGHT-label").assertExists()
        node("chartStyle-ENERGY-BARS").assertExists()
        compose.onAllNodesWithTag("chartStyle-WEIGHT-BARS").assertCountEquals(0)
        compose.onAllNodesWithTag("chartStyle-WEIGHT-LINE").assertCountEquals(0)
        node("chartOn-WEIGHT").performClick()
        node("chartOn-ENERGY").performClick()
        node("chartOn-STEPS").performClick()
        node("chartStyle-STEPS-LINE").performClick()
        node("chartSave").performClick()
        compose.waitForIdle()
        val made = list.charts.last()
        assertEquals(listOf(ChartSeries(SeriesKind.STEPS, SeriesStyle.LINE)), made.series)
        assertEquals(Period.MONTH, made.period)
        node("page-chart-${made.id}").assertIsDisplayed()
    }

    @Test
    fun `a colour picked in the editor is saved, Kulutus starts off with its dot disabled`() {
        show()
        openTab("tab-NEW")
        node("chartNew").performClick()
        node("chartColour-BURN").assertIsNotEnabled()
        node("chartColour-STEPS").performScrollTo().performClick()
        node("colourPicker").assertIsDisplayed()
        node("colourChoice-PUNAINEN").performClick()
        node("colourPicker").assertDoesNotExist()
        node("chartSave").performScrollTo().performClick()
        compose.waitForIdle()
        val made = list.charts.last()
        assertEquals(listOf(SeriesKind.WEIGHT, SeriesKind.ENERGY, SeriesKind.STEPS), made.series.map { it.kind })
        assertEquals(fi.jukkakot.terveyslappu.chart.SeriesColour.PUNAINEN, made.colourOf(SeriesKind.STEPS))
        assertEquals(null, made.colourOf(SeriesKind.WEIGHT))
    }

    @Test
    fun `blank name disables Tallenna`() {
        show()
        openTab("tab-NEW")
        node("chartNew").performClick()
        node("chartName").performTextReplacement("   ")
        node("chartSave").assertIsNotEnabled()
    }

    @Test
    fun `delete removes the tab at once without asking`() {
        add("Liike")
        add("Ilta")
        show()
        openTab("tab-chart-2")
        node("chartEdit").performClick()
        node("chartDelete").performClick()
        compose.waitForIdle()
        assertEquals(listOf(1), list.charts.map { it.id })
        assertEquals(false, exists("tab-chart-2"))
        node("page-chart-1").assertIsDisplayed()
    }

    @Test
    fun `rename changes the tab text and saving keeps the period`() {
        add("Liike")
        show()
        openTab("tab-chart-1")
        node("period-365").performClick()
        node("chartEdit").performClick()
        node("chartName").performTextReplacement("Ilta")
        node("chartLayout-OVERLAID").performClick()
        node("chartSave").performClick()
        compose.waitForIdle()
        assertEquals("Ilta", list.charts.single().name)
        assertEquals(Period.YEAR, list.charts.single().period)
        assertEquals(ChartLayout.OVERLAID, list.charts.single().layout)
        compose.onAllNodes(hasTestTag("tab-chart-1"), useUnmergedTree = true).fetchSemanticsNodes().single()
        assertTrue(compose.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Text, listOf(androidx.compose.ui.text.AnnotatedString("Ilta"))), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
    }

    @Test
    fun `cancel keeps the chart as it was`() {
        add("Liike")
        show()
        openTab("tab-chart-1")
        node("chartEdit").performClick()
        node("chartLayout-OVERLAID").performClick()
        node("chartCancel").performClick()
        assertEquals(ChartLayout.STACKED, list.charts.single().layout)
    }

    @Test
    fun `the editor's Keskiarvo switch saves the flag`() {
        add("Liike")
        show()
        openTab("tab-chart-1")
        assertEquals(false, list.charts.single().showAverage)
        node("chartEdit").performClick()
        node("chartAverage").performClick()
        node("chartSave").performClick()
        compose.waitForIdle()
        assertEquals(true, list.charts.single().showAverage)
    }
}
