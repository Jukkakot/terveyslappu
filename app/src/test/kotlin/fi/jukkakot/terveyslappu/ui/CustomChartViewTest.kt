package fi.jukkakot.terveyslappu.ui

import androidx.activity.ComponentActivity
import fi.jukkakot.terveyslappu.home.CardSize
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.height
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.chart.ChartLayout
import fi.jukkakot.terveyslappu.chart.CustomChartData
import fi.jukkakot.terveyslappu.chart.CustomChartSpec
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
import kotlin.test.assertEquals

/** An own chart's tab on the library chart (weight-chart "Stacked layout", "Overlaid layout"); made-up data. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w360dp-h780dp")
class CustomChartViewTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val f = ChartFixtures

    private fun show(data: CustomChartData, states: Map<SeriesKind, PanelState> = emptyMap()) {
        val spec = CustomChartSpec.of(data, f.today) { true }
        compose.setContent { TerveysTheme { HistoryChart(spec, null, {}, "own", tag = "customChart-1", states = states) } }
    }

    private fun node(tag: String) = compose.onNodeWithTag(tag, useUnmergedTree = true)
    private fun exists(tag: String) = compose.onAllNodes(hasTestTag(tag), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()

    @Test
    fun `three stacked panels share one Compose legend under the stack`() {
        show(f.data(f.chart(ChartLayout.STACKED, *f.three)))
        for ((kind, _) in f.three) node("plot-${kind.name}").assertIsDisplayed()
        node("chartLegend").assertIsDisplayed()
    }

    @Test
    fun `four stacked panels with Kulutus split into lepo and aktiivinen`() {
        show(f.data(f.chart(ChartLayout.STACKED, WEIGHT to SeriesStyle.LINE, ENERGY to SeriesStyle.BARS, SeriesKind.BURN to SeriesStyle.BARS, STEPS to SeriesStyle.BARS)))
        for (kind in SeriesKind.entries) node("plot-${kind.name}").assertIsDisplayed()
        compose.onNodeWithText("lepo", useUnmergedTree = true).assertExists()
        compose.onNodeWithText("aktiivinen", useUnmergedTree = true).assertExists()
    }

    @Test
    fun `a gated panel shows its reason and its grant button still asks`() {
        var asked = 0
        val gated = PanelState.Gated("Askeleet tarvitsevat luvan", listOf("Anna lupa" to { asked++ }))
        show(f.data(f.chart(ChartLayout.STACKED, *f.three), steps = false), mapOf(STEPS to gated))
        node("plot-WEIGHT").assertIsDisplayed()
        node("plot-ENERGY").assertIsDisplayed()
        node("panelMessage-STEPS").assertIsDisplayed()
        assertEquals(false, exists("plot-STEPS"))
        node("grant-STEPS").performClick()
        assertEquals(1, asked)
    }

    @Test
    fun `overlaid draws one plot, the data without values named under it`() {
        show(f.data(f.chart(ChartLayout.OVERLAID, *f.three), weights = false))
        node("plot-0").assertIsDisplayed()
        node("panelMessage-WEIGHT").assertIsDisplayed()
        assertEquals(false, exists("plot-ENERGY"))
    }

    @Test
    fun `overlaid with one drawn data is the stacked single panel`() {
        show(f.data(f.chart(ChartLayout.OVERLAID, *f.three), weights = false, food = false))
        node("plot-STEPS").assertIsDisplayed()
        assertEquals(false, exists("plot-0"))
    }

    @Test
    fun `Kaikki with nothing reads Ei tietoja`() {
        val data = CustomChartData.of(f.chart(ChartLayout.OVERLAID, *f.three).copy(period = Period.All), f.today, f.zone, emptyList(), emptyList(), emptyList())
        show(data)
        node("chartEmpty").assertIsDisplayed()
    }

    @Test
    fun `big Syoty and Kulutus overlaid card has one shared scale top`() {
        val data = f.data(f.chart(ChartLayout.OVERLAID, SeriesKind.ENERGY to SeriesStyle.BARS, SeriesKind.BURN to SeriesStyle.LINE))
        compose.setContent { TerveysTheme { CustomChartView(data, CardSize.BIG, modifier = androidx.compose.ui.Modifier.height(300.dp)) } }
        node("scaleTop-ENERGY").assertIsDisplayed()
        assertEquals(false, exists("scaleTop-BURN"))
    }
}
