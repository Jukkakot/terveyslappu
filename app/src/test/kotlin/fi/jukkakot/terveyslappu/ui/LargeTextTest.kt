package fi.jukkakot.terveyslappu.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.backup.BackupStatus
import fi.jukkakot.terveyslappu.weight.weightEntry
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.Instant

/** ui rule 20: each screen keeps its actions on screen at 360 dp and 200 % text. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w360dp-h780dp", fontScale = 2.0f)
class LargeTextTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val weights = (0..14).map {
        weightEntry("$it", Instant.now().minusSeconds(it * 43_200L + 60), null, 80.0 + it / 10.0, "me", "me")
    }

    private fun show(screen: Screen) {
        compose.setContent {
            TerveysTheme {
                MainScreen(MainUiState(access = Access.GRANTED, weights = weights, screen = screen), "0.1.1-test", MainActions())
            }
        }
    }

    /** weight-chart "Chart axes": at 360 dp and font scale 1.3 the date labels neither overlap nor get cut. */
    @Test
    @Config(fontScale = 1.3f)
    @org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
    fun `Askeleet's date labels do not overlap at 360 dp and font scale 1_3`() {
        val today = java.time.LocalDate.now()
        val steps = fi.jukkakot.terveyslappu.steps.StepsUiState(
            access = fi.jukkakot.terveyslappu.steps.StepsAccess.GRANTED,
            days = (0L..400L).map { fi.jukkakot.terveyslappu.steps.StepsDay(today.minusDays(it), 6_000L + it * 137) },
        )
        var shown by androidx.compose.runtime.mutableStateOf(fi.jukkakot.terveyslappu.chart.Period.WEEK)
        compose.setContent {
            TerveysTheme {
                MainScreen(MainUiState(access = Access.GRANTED, weights = weights, screen = Screen.HISTORY, historyOnSteps = true, period = shown), "0.1.1-test", MainActions(), steps = steps)
            }
        }
        for (period in listOf(fi.jukkakot.terveyslappu.chart.Period.WEEK, fi.jukkakot.terveyslappu.chart.Period.MONTH, fi.jukkakot.terveyslappu.chart.Period.QUARTER, fi.jukkakot.terveyslappu.chart.Period.YEAR)) {
            shown = period
            compose.waitForIdle()
            val chart = compose.activity.chartViews().single()
            val rects = dateLabelRects(chart)
            if (period == fi.jukkakot.terveyslappu.chart.Period.WEEK) {
                kotlin.test.assertEquals(7, rects.size, "one weekday label per day")
            } else {
                kotlin.test.assertTrue(rects.size in 3..5, "$period: ${rects.size} labels")
            }
            for (r in rects) kotlin.test.assertTrue(r.left >= 0f && r.right <= chart.width, "$period: $r cut at an edge of ${chart.width}")
            rects.zipWithNext().forEach { (a, b) -> kotlin.test.assertTrue(b.left > a.right, "$period: $a overlaps $b") }
            // Text follows the font size: 11 sp at 1.3.
            kotlin.test.assertEquals(11f * 1.3f, chart.xAxis.textSize, 0.1f)
        }
    }

    @Test
    fun `home keeps the card, the weigh-in button and settings visible`() {
        show(Screen.HOME)
        compose.onNodeWithTag("weighIn").assertIsDisplayed()
        compose.onNodeWithTag("latestCard").assertIsDisplayed()
        compose.onNodeWithTag("change", useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithTag("settings").assertIsDisplayed()
    }

    @Test
    fun `Historia keeps back, add and the list visible`() {
        show(Screen.HISTORY)
        compose.onNodeWithTag("back").assertIsDisplayed()
        compose.onNodeWithTag("addWeight").assertIsDisplayed()
        compose.onNodeWithTag("weights").assertIsDisplayed()
    }

    @Test
    fun `Asetukset reaches every row and its sub-screens`() {
        var page by androidx.compose.runtime.mutableStateOf(SettingsPage.LIST)
        compose.setContent {
            TerveysTheme {
                SettingsScreen(page, true, ThemeMode.SYSTEM, fi.jukkakot.terveyslappu.profile.OwnerProfile(birthYear = 1990), false, BackupStatus(), "0.1.1-test", MainActions(), SnackbarHostState())
            }
        }
        for (tag in listOf("rowProfile", "rowListen", "rowData", "rowFood", "rowSteps", "rowAppearance", "rowSupport")) {
            compose.onNodeWithTag(tag).performScrollTo().assertIsDisplayed()
        }
        page = SettingsPage.APPEARANCE
        compose.onNodeWithTag("theme-SYSTEM").assertIsDisplayed()
        page = SettingsPage.PROFILE
        compose.onNodeWithTag("gender-NONE").performScrollTo().assertIsDisplayed()
        page = SettingsPage.DATA
        compose.onNodeWithTag("backupEnable").performScrollTo().assertIsDisplayed()
        page = SettingsPage.SUPPORT
        compose.onNodeWithTag("shareLog").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `the Oma calendar keeps OK and the history notice reachable`() {
        val days = mapOf(java.time.LocalDate.of(2026, 11, 3) to 100.0)
        compose.setContent {
            TerveysTheme { RangeCalendarContent(null, days, true, {}, {}, {}, java.time.LocalDate.of(2026, 11, 9)) }
        }
        compose.onNodeWithTag("dayKg-2026-11-03", useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithTag("calendarOk").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("historyGrant").assertIsDisplayed()
    }
}
