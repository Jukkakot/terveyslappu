package fi.jukkakot.terveyslappu.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.chart.ChartLayout
import fi.jukkakot.terveyslappu.chart.ChartSeries
import fi.jukkakot.terveyslappu.chart.CustomChart
import fi.jukkakot.terveyslappu.chart.CustomChartData
import fi.jukkakot.terveyslappu.chart.Period
import fi.jukkakot.terveyslappu.chart.SeriesKind
import fi.jukkakot.terveyslappu.chart.SeriesKind.ENERGY
import fi.jukkakot.terveyslappu.chart.SeriesKind.STEPS
import fi.jukkakot.terveyslappu.chart.SeriesKind.WEIGHT
import fi.jukkakot.terveyslappu.chart.SeriesStyle
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.ZoneId
import kotlin.test.assertEquals

/** Smoke tests of an own chart's drawing (custom-charts task 2.2); made-up data. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w360dp-h780dp")
class CustomChartViewTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val zone = ZoneId.systemDefault()

    private fun chart(layout: ChartLayout, vararg series: Pair<SeriesKind, SeriesStyle>) =
        CustomChart(1, "Liike", series.map { ChartSeries(it.first, it.second) }, layout, Period.MONTH)

    private fun show(chart: CustomChart, weights: Boolean = true, food: Boolean = true, steps: Boolean = true, states: Map<SeriesKind, PanelState> = emptyMap(), burn: Boolean = true) {
        val data = CustomChartData.of(
            chart, HomeFixtures.today, zone,
            if (weights) HomeFixtures.weights else emptyList(),
            if (food) HomeFixtures.foodItems else emptyList(),
            if (steps) HomeFixtures.steps.days else null,
            burnDays = if (burn) HomeFixtures.burn.days else null,
        )
        compose.setContent { TerveysTheme { CustomChartView(data, ChartMode.Tab, states) } }
    }

    private fun node(tag: String) = compose.onNodeWithTag(tag, useUnmergedTree = true)
    private fun exists(tag: String) = compose.onAllNodes(hasTestTag(tag), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()

    private val all = arrayOf(WEIGHT to SeriesStyle.LINE, ENERGY to SeriesStyle.BARS, STEPS to SeriesStyle.BARS)

    @Test
    fun `three stacked panels`() {
        show(chart(ChartLayout.STACKED, *all))
        for ((kind, _) in all) node("plot-${kind.name}").assertIsDisplayed()
        node("chartDates").assertIsDisplayed()
        node("chartLegend").assertIsDisplayed()
    }

    @Test
    fun `four stacked panels with Kulutus split into lepo and aktiivinen`() {
        show(chart(ChartLayout.STACKED, WEIGHT to SeriesStyle.LINE, ENERGY to SeriesStyle.BARS, SeriesKind.BURN to SeriesStyle.BARS, STEPS to SeriesStyle.BARS))
        for (kind in SeriesKind.entries) node("plot-${kind.name}").assertIsDisplayed()
        compose.onNodeWithText("lepo", useUnmergedTree = true).assertExists()
        compose.onNodeWithText("aktiivinen", useUnmergedTree = true).assertExists()
    }

    @Test
    fun `Kulutus without permission shows its reason in its panel`() {
        val gated = PanelState.Gated("Kulutus vaatii luvan", listOf("Anna lupa" to {}))
        show(chart(ChartLayout.STACKED, SeriesKind.BURN to SeriesStyle.BARS, STEPS to SeriesStyle.BARS), burn = false, states = mapOf(SeriesKind.BURN to gated))
        node("panelMessage-BURN").assertIsDisplayed()
        node("grant-BURN").assertExists()
    }

    @Test
    fun `overlaid weight and energy show both axes and the legend`() {
        show(chart(ChartLayout.OVERLAID, WEIGHT to SeriesStyle.LINE, ENERGY to SeriesStyle.BARS))
        node("plot-OVERLAID").assertIsDisplayed()
        node("axisLeft-WEIGHT-2").assertIsDisplayed()
        node("axisRight-ENERGY-2").assertIsDisplayed()
        assertEquals(2, compose.onAllNodes(hasTestTag("chartLegend-entry"), useUnmergedTree = true).fetchSemanticsNodes().size)
    }

    @Test
    fun `overlaid three have two axes and no scale for steps`() {
        show(chart(ChartLayout.OVERLAID, *all))
        node("axisLeft-WEIGHT-0").assertIsDisplayed()
        node("axisRight-ENERGY-0").assertIsDisplayed()
        assertEquals(false, exists("axisLeft-STEPS-0") || exists("axisRight-STEPS-0") || exists("axis-STEPS-0"))
        assertEquals(3, compose.onAllNodes(hasTestTag("chartLegend-entry"), useUnmergedTree = true).fetchSemanticsNodes().size)
    }

    @Test
    fun `axes go to what is drawn`() {
        show(chart(ChartLayout.OVERLAID, *all), weights = false)
        node("axisLeft-ENERGY-0").assertIsDisplayed()
        node("axisRight-STEPS-0").assertIsDisplayed()
        node("panelMessage-WEIGHT").assertIsDisplayed()
    }

    @Test
    fun `no steps permission in stacked shows the reason at the panel's place`() {
        var asked = 0
        show(chart(ChartLayout.STACKED, *all), steps = false, states = mapOf(STEPS to PanelState.Gated("Askeleet tarvitsevat luvan", listOf("Anna lupa" to { asked++ }))))
        node("plot-WEIGHT").assertIsDisplayed()
        node("plot-ENERGY").assertIsDisplayed()
        node("panelMessage-STEPS").assertIsDisplayed()
        assertEquals(false, exists("plot-STEPS"))
        node("grant-STEPS").performClick()
        assertEquals(1, asked)
    }

    @Test
    fun `overlaid with one drawn data looks like the stacked single panel`() {
        show(chart(ChartLayout.OVERLAID, *all), weights = false, food = false)
        node("plot-STEPS").assertIsDisplayed()
        node("axis-STEPS-0").assertIsDisplayed()
        assertEquals(false, exists("plot-OVERLAID"))
        assertEquals(160f, node("panel-STEPS").fetchSemanticsNode().size.height / compose.density.density, 1f)
    }

    @Test
    fun `Kaikki with nothing reads Ei tietoja`() {
        val data = CustomChartData.of(chart(ChartLayout.OVERLAID, *all).copy(period = Period.All), HomeFixtures.today, zone, emptyList(), emptyList(), emptyList())
        compose.setContent { TerveysTheme { CustomChartView(data, ChartMode.Tab) } }
        node("chartEmpty").assertIsDisplayed()
    }
}
