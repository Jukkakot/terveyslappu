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
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.burn.BurnUiState
import fi.jukkakot.terveyslappu.home.CardSize
import fi.jukkakot.terveyslappu.home.HomeCard
import fi.jukkakot.terveyslappu.home.HomeLayout
import fi.jukkakot.terveyslappu.steps.StepsAccess
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Smoke tests of the Kulutus card and tab (energy-burn); made-up kcal only (NFR-1). */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w360dp-h780dp")
class BurnScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private var grants = 0
    private var sourceOpened = 0

    private val withCard = HomeLayout.DEFAULT.show(HomeLayout.BURN)

    private fun show(burn: BurnUiState, start: MainUiState = MainUiState(access = Access.GRANTED, weights = HomeFixtures.weights, homeLayout = withCard)) {
        compose.setContent {
            var state by remember { mutableStateOf(start) }
            TerveysTheme {
                MainScreen(
                    state,
                    "0.1.1-test",
                    MainActions(onBack = { state = state.back() }, onBurnShown = { state = state.copy(historyOnBurn = false) }),
                    food = HomeFixtures.food,
                    steps = HomeFixtures.steps,
                    burn = burn,
                    burnActions = BurnActions(
                        onGrant = { grants++ },
                        onOpen = { state = state.copy(screen = Screen.HISTORY, historyOnBurn = true) },
                        onOpenSource = { sourceOpened++ },
                    ),
                )
            }
        }
    }

    @Test
    fun `the card opens Kulutus after Askeleet with bars, stats and the day list`() {
        show(HomeFixtures.burn)
        compose.onNodeWithTag("burnCard").performClick()
        compose.onNodeWithTag("tab-BURN").assertIsSelected()
        assertTrue(compose.onNodeWithTag("tab-STEPS").getBoundsInRoot().right <= compose.onNodeWithTag("tab-BURN").getBoundsInRoot().left)
        compose.onNodeWithTag("burnChart").assertExists()
        compose.onNodeWithTag("burnStats").assertExists()
        compose.onNodeWithTag("burnDay-${HomeFixtures.today}").assertExists()
        compose.onNodeWithTag("burnSource").performClick()
        assertEquals(1, sourceOpened)
    }

    @Test
    fun `without the permissions the card asks`() {
        show(BurnUiState(access = StepsAccess.NEEDS))
        compose.onNodeWithTag("burnCard").performClick()
        assertEquals(1, grants)
    }

    @Test
    fun `without the permissions the tab asks`() {
        grants = 1
        show(BurnUiState(access = StepsAccess.NEEDS), MainUiState(access = Access.GRANTED, weights = HomeFixtures.weights, screen = Screen.HISTORY, historyOnBurn = true))
        compose.onNodeWithTag("burnGrant").performClick()
        assertEquals(2, grants)
    }

    @Test
    fun `no data in the period reads the empty line`() {
        show(BurnUiState(access = StepsAccess.GRANTED, days = emptyList()), MainUiState(access = Access.GRANTED, weights = HomeFixtures.weights, screen = Screen.HISTORY, historyOnBurn = true))
        compose.onNodeWithTag("burnEmpty").assertExists()
        compose.onNodeWithTag("burnChart").assertDoesNotExist()
    }

    @Test
    fun `a small Kulutus card pairs with steps`() {
        show(HomeFixtures.burn)
        val steps = compose.onNodeWithTag("homeSlot-steps", useUnmergedTree = true).getBoundsInRoot()
        val burn = compose.onNodeWithTag("homeSlot-burn", useUnmergedTree = true).getBoundsInRoot()
        assertTrue(burn.top >= steps.bottom, "Kulutus comes after steps (food and steps share the row)")
        assertEquals(HomeCard(HomeLayout.BURN, CardSize.SMALL), withCard.shown.last())
    }
}
