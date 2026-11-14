package fi.jukkakot.terveyslappu.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.chart.Period
import fi.jukkakot.terveyslappu.food.Amount
import fi.jukkakot.terveyslappu.food.AmountUnit
import fi.jukkakot.terveyslappu.food.FoodItem
import fi.jukkakot.terveyslappu.steps.StepsAccess
import fi.jukkakot.terveyslappu.steps.StepsDay
import fi.jukkakot.terveyslappu.steps.StepsUiState
import fi.jukkakot.terveyslappu.weight.weightEntry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Smoke tests of the steps card and the Askeleet tab; made-up numbers only (NFR-1). */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w360dp-h780dp")
class StepsScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val zone = ZoneId.systemDefault()
    private val today = LocalDate.now(zone)
    private val now = Instant.now()

    private val weights = listOf(
        weightEntry("1", now.minusSeconds(3_600), null, 82.4, "me", "me"),
        weightEntry("2", now.minusSeconds(3 * 86_400), null, 82.9, "me", "me"),
    )
    private val days = listOf(StepsDay(today, 6420), StepsDay(today.minusDays(1), 8000), StepsDay(today.minusDays(3), 7600))
    private val granted = StepsUiState(access = StepsAccess.GRANTED, days = days)

    private val foodToday = run {
        val t = today.atTime(8, 0).atZone(zone).toInstant()
        listOf(FoodItem("a", today, 1, "Rahka", 130.0, Amount(1.0, AmountUnit.KPL), false, t, zone.rules.getOffset(t), true))
    }

    private var grants = 0
    private var shownFrom = mutableListOf<LocalDate?>()

    private fun show(start: MainUiState, steps: StepsUiState, food: FoodUiState = FoodUiState(access = FoodAccess.GRANTED, items = foodToday)) {
        compose.setContent {
            var state by remember { mutableStateOf(start) }
            TerveysTheme {
                MainScreen(
                    state,
                    "0.1.1-test",
                    MainActions(
                        onBack = { state = state.back() },
                        onStepsShown = { state = state.copy(historyOnSteps = false) },
                        onEnergyShown = { state = state.copy(historyOnEnergy = false) },
                    ),
                    food = food,
                    steps = steps,
                    stepsActions = StepsActions(
                        onGrant = { grants++ },
                        onOpen = { state = state.copy(screen = Screen.HISTORY, historyOnSteps = true) },
                        onShown = { shownFrom += it },
                    ),
                )
            }
        }
    }

    @Test
    fun `steps card shows today's steps and opens Askeleet with bars and the day list`() {
        show(MainUiState(access = Access.GRANTED, weights = weights), granted)
        compose.onNodeWithTag("stepsTodayValue", useUnmergedTree = true).assertExists()
        compose.onNodeWithTag("homeFoodAdd").assertExists()
        compose.onNodeWithTag("stepsCard").performClick()
        compose.onNodeWithTag("tab-STEPS").assertIsSelected()
        compose.onNodeWithTag("stepsChart").assertExists()
        compose.onNodeWithTag("stepsStats").assertExists()
        compose.onNodeWithTag("stepsDay-$today").assertExists()
        assertTrue(shownFrom.isNotEmpty())
        compose.onNodeWithTag("back").performClick()
        compose.onNodeWithTag("stepsCard").assertExists()
    }

    @Test
    fun `without the permission the card asks and the food card is unchanged`() {
        show(MainUiState(access = Access.GRANTED, weights = weights), StepsUiState(access = StepsAccess.NEEDS))
        compose.onNodeWithTag("stepsTodayValue", useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithTag("stepsCard").performClick()
        assertEquals(1, grants)
        compose.onNodeWithTag("homeFoodAdd").assertExists()
        compose.onNodeWithTag("stepsBars").assertDoesNotExist()
    }

    @Test
    fun `unavailable card has no tap action`() {
        show(MainUiState(access = Access.GRANTED, weights = weights), StepsUiState(access = StepsAccess.UNAVAILABLE))
        compose.onNodeWithTag("stepsCard").performClick()
        compose.onNodeWithTag("stepsCard").assertExists()
        assertEquals(0, grants)
    }

    @Test
    fun `the front page fits at 360 dp with both cards and + Annos`() {
        val questioned = weights.first().id
        show(MainUiState(access = Access.GRANTED, weights = weights, questioned = setOf(questioned)), granted)
        compose.onNodeWithTag("checkNotice").assertExists()
        val bottom = compose.onRoot().getBoundsInRoot().bottom
        for (tag in listOf("checkNotice", "latestCard", "weighIn", "foodCard", "stepsCard", "homeFoodAdd", "stepsPeriod", "foodPeriod")) {
            val bounds = compose.onNodeWithTag(tag).getBoundsInRoot()
            assertTrue(bounds.bottom <= bottom, "$tag ends at ${bounds.bottom}, screen at $bottom")
            assertTrue(bounds.right - bounds.left > 0.dp, tag)
        }
        // Both halves end on the same line (the clickable steps card's node includes its 8 dp padding, the food card's does not).
        val diff = compose.onNodeWithTag("stepsCard").getBoundsInRoot().bottom - compose.onNodeWithTag("foodCard").getBoundsInRoot().bottom
        assertTrue(diff in 0.dp..8.dp, "bottoms differ by $diff")
        val topDiff = compose.onNodeWithTag("foodCard").getBoundsInRoot().top - compose.onNodeWithTag("stepsCard").getBoundsInRoot().top
        assertEquals(diff, topDiff)
    }

    @Test
    fun `a swipe from Energia reaches Askeleet`() {
        show(MainUiState(access = Access.GRANTED, weights = weights, screen = Screen.HISTORY, historyOnEnergy = true, period = Period.WEEK), granted)
        compose.onNodeWithTag("tab-ENERGY").assertIsSelected()
        compose.onNodeWithTag("page-ENERGY").performTouchInput { swipeLeft() }
        compose.onNodeWithTag("tab-STEPS").assertIsSelected()
        compose.onNodeWithTag("stepsChart").assertExists()
    }

    @Test
    fun `Askeleet without the permission gives the reason, after a denial also Health Connect`() {
        show(MainUiState(access = Access.GRANTED, weights = weights, screen = Screen.HISTORY, historyOnSteps = true), StepsUiState(access = StepsAccess.NEEDS))
        compose.onNodeWithTag("stepsReason").assertExists()
        compose.onNodeWithTag("stepsGrant").performClick()
        assertEquals(1, grants)
        compose.onNodeWithTag("stepsSettings").assertDoesNotExist()
    }

    @Test
    fun `denied Askeleet offers ask again and the settings`() {
        show(MainUiState(access = Access.GRANTED, weights = weights, screen = Screen.HISTORY, historyOnSteps = true), StepsUiState(access = StepsAccess.DENIED))
        compose.onNodeWithTag("stepsAskAgain").assertExists()
        compose.onNodeWithTag("stepsSettings").assertExists()
    }

    @Test
    fun `no step days shows one line instead of a chart`() {
        show(MainUiState(access = Access.GRANTED, weights = weights, screen = Screen.HISTORY, historyOnSteps = true), StepsUiState(access = StepsAccess.GRANTED, days = emptyList()))
        compose.onNodeWithTag("stepsEmpty").assertExists()
        compose.onNodeWithTag("stepsChart").assertDoesNotExist()
    }
}
