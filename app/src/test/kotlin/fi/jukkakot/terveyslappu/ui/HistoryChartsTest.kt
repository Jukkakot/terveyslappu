package fi.jukkakot.terveyslappu.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
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
    fun `two charts give seven tabs in order`() {
        add("Paino ja ruoka")
        add("Liike")
        show()
        val tags = listOf("tab-WEIGHT", "tab-BMI", "tab-ENERGY", "tab-STEPS", "tab-chart-1", "tab-chart-2", "tab-NEW")
        tags.forEach { assertTrue(exists(it), it) }
        val lefts = tags.map { node(it).getUnclippedBoundsInRoot().left.value }
        assertEquals(lefts.sorted(), lefts)
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

    private fun tapPlot(tag: String = "plot-WEIGHT") = node(tag).performTouchInput { click(center) }

    @Test
    fun `a tap shows the readout and a tap on it clears`() {
        add("Liike")
        show()
        openTab("tab-chart-1")
        node("chartHint").assertIsDisplayed()
        tapPlot()
        node("chartReadoutText").assertIsDisplayed()
        node("chartReadout").performClick()
        node("chartHint").assertIsDisplayed()
    }

    @Test
    fun `a hold without moving places the crosshair`() {
        add("Liike")
        show()
        openTab("tab-chart-1")
        node("plot-STEPS").performTouchInput {
            down(center)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + 100)
            up()
        }
        compose.waitForIdle()
        node("chartReadoutText").assertIsDisplayed()
    }

    @Test
    fun `a fling still pages`() {
        add("Liike")
        add("Ilta")
        show()
        openTab("tab-chart-1")
        node("plot-WEIGHT").performTouchInput { swipeLeft() }
        compose.waitForIdle()
        node("page-chart-2").assertIsDisplayed()
        assertEquals(false, exists("chartReadoutText"))
    }

    @Test
    fun `the grant button in a panel still asks and sets no crosshair`() {
        steps = StepsUiState(access = StepsAccess.NEEDS)
        add("Liike")
        show()
        openTab("tab-chart-1")
        node("grant-STEPS").performClick()
        assertEquals(1, stepsGrants)
        assertEquals(false, exists("chartReadoutText"))
        node("panelMessage-STEPS").performTouchInput { click(center) }
        assertEquals(false, exists("chartReadoutText"), "a tap outside the drawn plots places nothing")
    }

    @Test
    fun `Edellinen paiva with no crosshair picks today`() {
        add("Liike")
        show()
        openTab("tab-chart-1")
        val chart = node("customChart-1").fetchSemanticsNode()
        val action = chart.config[SemanticsActions.CustomActions].first { it.label == compose.activity.getString(fi.jukkakot.terveyslappu.R.string.chart_previous_day) }
        compose.runOnIdle { action.action() }
        compose.waitForIdle()
        node("chartReadoutText").assertTextContains(formatShortDay(HomeFixtures.today), substring = true)
        assertTrue(node("chartReadout").fetchSemanticsNode().config.contains(SemanticsProperties.LiveRegion))
    }

    @Test
    fun `the crosshair stays on reload and clears when Kaikki starts later than its day`() {
        list = ChartList()
        val id = add("Liike", SeriesKind.STEPS)
        list = CustomCharts.update(list, list.charts.single().copy(period = Period.All))
        state = state.copy(charts = list.charts)
        show()
        openTab("tab-chart-$id")
        // Kaikki starts at the first step day (5 days back): tap the first slot.
        node("plot-STEPS").performTouchInput { click(centerLeft.copy(x = 2f)) }
        node("chartReadoutText").assertTextContains(formatShortDay(HomeFixtures.today.minusDays(5)), substring = true)
        steps = steps.copy(days = steps.days)
        compose.waitForIdle()
        node("chartReadoutText").assertIsDisplayed()
        steps = steps.copy(days = steps.days!!.filter { it.day >= HomeFixtures.today.minusDays(1) })
        compose.waitForIdle()
        node("chartHint").assertIsDisplayed()
    }

    @Test
    fun `new chart defaults (1 kk) and the last data stays on`() {
        add("Kaavio 1")
        show()
        openTab("tab-NEW")
        node("chartNew").performClick()
        node("chartName").assertTextContains("Kaavio 2")
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
}
