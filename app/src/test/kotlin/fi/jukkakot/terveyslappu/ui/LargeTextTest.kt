package fi.jukkakot.terveyslappu.ui

import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.getUnclippedBoundsInRoot
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
    fun `home keeps the card, the help and the menu visible`() {
        show(Screen.HOME)
        compose.onNodeWithTag("help").assertIsDisplayed()
        compose.onNodeWithTag("latestCard").assertIsDisplayed()
        compose.onNodeWithTag("appMenu").assertIsDisplayed()
    }

    @Test
    fun `Historia keeps back, the menu and the list visible`() {
        show(Screen.HISTORY)
        compose.onNodeWithTag("back").assertIsDisplayed()
        compose.onNodeWithTag("appMenu").assertIsDisplayed()
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

    /** food-log "Top bar at large text": back, Viikko and the ⋮ stay on screen beside the title at 200 %. */
    @Test
    @org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
    fun `Ruoka keeps back, Viikko and the more menu visible`() {
        val today = java.time.LocalDate.now()
        val zone = java.time.ZoneId.systemDefault()
        fun annos(uuid: String, pos: Int, name: String): fi.jukkakot.terveyslappu.food.FoodItem {
            val at = today.atTime(8 + pos, 0).atZone(zone).toInstant()
            return fi.jukkakot.terveyslappu.food.FoodItem(uuid, today, pos, name, 130.0, fi.jukkakot.terveyslappu.food.Amount(1.0, fi.jukkakot.terveyslappu.food.AmountUnit.KPL), false, at, zone.rules.getOffset(at), true)
        }
        val food = FoodUiState(access = FoodAccess.GRANTED, items = listOf(annos("a", 1, "Rahka"), annos("b", 2, "Pulla")), day = today)
        compose.setContent {
            TerveysTheme {
                MainScreen(MainUiState(access = Access.GRANTED, weights = weights, screen = Screen.FOOD), "0.1.1-test", MainActions(), food = food)
            }
        }
        for (tag in listOf("back", "foodWeek", "foodMore")) compose.onNodeWithTag(tag).assertIsDisplayed()
        val screen = compose.onRoot().getUnclippedBoundsInRoot()
        for (tag in listOf("back", "foodWeek", "foodMore")) {
            val box = compose.onNodeWithTag(tag).getUnclippedBoundsInRoot()
            kotlin.test.assertTrue(box.left >= 0.dp && box.right <= screen.right, "$tag $box is cut off")
        }
    }

    /** app-appearance "Options on one line each": every theme and colour option sits whole on one row at 200 %. */
    @Test
    @org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
    fun `Ulkoasu shows every option on one line`() {
        compose.setContent {
            TerveysTheme {
                SettingsScreen(SettingsPage.APPEARANCE, true, ThemeMode.SYSTEM, fi.jukkakot.terveyslappu.profile.OwnerProfile(), false, BackupStatus(), "0.1.1-test", MainActions(), SnackbarHostState())
            }
        }
        for (tag in listOf("theme-SYSTEM", "theme-LIGHT", "theme-DARK", "colours-BRAND", "colours-PHONE")) {
            compose.onNodeWithTag(tag).performScrollTo().assertIsDisplayed()
            val box = compose.onNodeWithTag(tag).getUnclippedBoundsInRoot()
            kotlin.test.assertTrue(box.bottom - box.top < 64.dp, "$tag wraps to two lines: $box")
        }
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
