package fi.jukkakot.terveyslappu.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.chart.Period
import org.junit.Rule
import org.junit.Test
import fi.jukkakot.terveyslappu.ui.StatsLayout.Key
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** weight-chart "Statistics layout": the per-tab mapping. */
class StatsLayoutTest {
    @Test
    fun `each tab's hero and row`() {
        assertEquals(StatsLayout.Layout(Key.CHANGE, Key.RATE, listOf(Key.AVERAGE, Key.MIN, Key.MAX, Key.COUNT)), StatsLayout.of(StatsLayout.Tab.WEIGHT))
        assertEquals(StatsLayout.Layout(Key.LATEST, Key.CHANGE, listOf(Key.AVERAGE, Key.MIN, Key.MAX, Key.COUNT)), StatsLayout.of(StatsLayout.Tab.BMI))
        assertEquals(StatsLayout.Layout(Key.AVERAGE, null, listOf(Key.TREATS, Key.DAYS)), StatsLayout.of(StatsLayout.Tab.FOOD))
        assertEquals(StatsLayout.Layout(Key.AVERAGE, null, listOf(Key.DAYS)), StatsLayout.of(StatsLayout.Tab.STEPS))
        assertEquals(StatsLayout.Layout(Key.AVERAGE, null, listOf(Key.ACTIVE_AVERAGE, Key.DAYS)), StatsLayout.of(StatsLayout.Tab.BURN))
    }
}

/** weight-chart "One quantity at a time" and "Periods": tabs first, five tabs and six periods fit 360 dp. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w360dp-h780dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HistoryHeaderTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private fun show() {
        compose.setContent {
            TerveysTheme {
                MainScreen(MainUiState(access = Access.GRANTED, weights = ChartFixtures.weights, screen = Screen.HISTORY, period = Period.MONTH), "0.0.0-test", MainActions())
            }
        }
        compose.waitForIdle()
    }

    private fun bounds(tag: String) = compose.onNodeWithTag(tag, useUnmergedTree = true).getUnclippedBoundsInRoot()

    @Test
    fun `the five tabs are fully visible above the periods`() {
        show()
        val tabs = listOf("tab-WEIGHT", "tab-BMI", "tab-ENERGY", "tab-STEPS", "tab-BURN").map(::bounds)
        assertTrue(tabs.all { it.left >= 0.dp && it.right <= 360.dp }, "$tabs")
        assertTrue(tabs.first().bottom <= bounds("periods").top, "tabs above the periods")
        sixPeriods()
    }

    @Test
    @Config(fontScale = 1.3f)
    fun `the six periods fit 360 dp at 130 percent text`() {
        show()
        sixPeriods()
    }

    private fun sixPeriods() {
        val tags = listOf("period-7", "period-30", "period-90", "period-365", "period-all", "period-custom")
        val segments = tags.map(::bounds)
        assertTrue(segments.all { it.left >= 0.dp && it.right <= 360.dp && it.right - it.left > 30.dp }, "$segments")
        assertTrue(segments.zipWithNext().all { (a, b) -> a.right <= b.left + 0.5.dp }, "one row, in order")
    }
}
