package fi.jukkakot.terveyslappu.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.DpRect
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.home.CardSize
import fi.jukkakot.terveyslappu.home.HomeEditSignal
import fi.jukkakot.terveyslappu.home.HomeLayout
import fi.jukkakot.terveyslappu.home.HomeLayout.Companion.FOOD
import fi.jukkakot.terveyslappu.home.HomeLayout.Companion.STEPS
import fi.jukkakot.terveyslappu.home.HomeLayout.Companion.WEIGHT
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The front page's edit mode (home-screen "Card edit mode", "Hidden cards"); made-up data. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w411dp-h891dp")
class HomeEditTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private var state by mutableStateOf(MainUiState(access = Access.GRANTED, weights = HomeFixtures.weights))
    private var food by mutableStateOf(HomeFixtures.food)
    private var saved: HomeLayout? = null
    private var adds = 0
    private var weighIns = 0
    private var stepsOpened = 0
    private var screens = 0

    @Composable
    private fun Screen() {
        TerveysTheme {
            MainScreen(
                state,
                "0.1.1-test",
                MainActions(
                    onHomeLayout = { saved = it; state = state.copy(homeLayout = it) },
                    onWeighIn = { weighIns++ },
                    onOpenEntry = { screens++ },
                    onScreen = { screens++ },
                ),
                food = food,
                foodActions = FoodActions(onAdd = { adds++ }),
                steps = HomeFixtures.steps,
                stepsActions = StepsActions(onOpen = { stepsOpened++ }),
            )
        }
    }

    private fun show() = compose.setContent { Screen() }

    private fun node(tag: String) = compose.onNodeWithTag(tag, useUnmergedTree = true)
    private fun bounds(tag: String): DpRect = node(tag).getUnclippedBoundsInRoot()
    private fun editing(): Boolean = compose.onAllNodes(androidx.compose.ui.test.hasTestTag("editTitle"), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()

    /** The shown cards in reading order (row by row, left to right). */
    private fun order(): List<String> {
        compose.waitForIdle()
        return listOf(WEIGHT, FOOD, STEPS)
            .filter { compose.onAllNodes(androidx.compose.ui.test.hasTestTag("homeSlot-$it"), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
            .sortedWith(compareBy({ bounds("homeSlot-$it").top.value }, { bounds("homeSlot-$it").left.value }))
    }

    private fun enter() {
        node("homeSlot-food").performTouchInput { longClick() }
        assertTrue(editing(), "edit mode on")
    }

    private fun menu(id: String, item: String) {
        node("editMenu-$id").performClick()
        node(item).performClick()
    }

    /** A press held past the long-press timeout on [from], then moved without lifting to [to]'s centre, then released. */
    private fun holdAndDrag(from: String, to: String) {
        val start = bounds("homeSlot-$from")
        val target = bounds("homeSlot-$to")
        val density = compose.density
        // Reorderable moves a card once the dragged card covers the other card's centre.
        val dx = with(density) { ((target.left + target.right - start.left - start.right) / 2).toPx() }
        val dy = with(density) { ((target.top + target.bottom - start.top - start.bottom) / 2).toPx() }
        node("homeSlot-$from").performTouchInput {
            down(center)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + 200)
            val steps = 20
            repeat(steps) {
                moveBy(Offset(dx / steps, dy / steps))
                advanceEventTime(16)
            }
            advanceEventTime(200)
            up()
        }
        compose.waitForIdle()
    }

    @Test
    fun `long-press on the food title starts edit mode, replaces the menu and Ruoka does not open`() {
        show()
        node("appMenu").assertExists()
        node("foodToday").performTouchInput { longClick() }
        assertTrue(editing())
        assertEquals(0, adds)
        node("editCancel").assertIsEnabled()
        node("editDone").assertIsEnabled()
        node("appMenu").assertDoesNotExist()
        node("help").assertDoesNotExist()
    }

    @Test
    fun `a long-press that then moves drags the card`() {
        show()
        holdAndDrag(STEPS, WEIGHT)
        assertTrue(editing(), "edit mode stays on")
        assertEquals(listOf(STEPS, WEIGHT, FOOD), order())
    }

    @Test
    fun `in edit mode a long-press drag on the card body reorders and a tap does nothing`() {
        show()
        enter()
        holdAndDrag(FOOD, WEIGHT)
        assertEquals(listOf(FOOD, WEIGHT, STEPS), order())
        node("homeSlot-steps").performClick()
        assertEquals(0, stepsOpened)
    }

    @Test
    fun `drag from the handle`() {
        show()
        enter()
        val start = bounds("editHandle-steps")
        val target = bounds("homeSlot-weight")
        val density = compose.density
        node("editHandle-steps").performTouchInput {
            down(center)
            val dy = with(density) { (target.top - start.top).toPx() }
            val dx = with(density) { (target.left - start.left).toPx() }
            repeat(20) {
                moveBy(Offset(dx / 20, dy / 20))
                advanceEventTime(16)
            }
            advanceEventTime(200)
            up()
        }
        assertEquals(listOf(STEPS, WEIGHT, FOOD), order())
    }

    @Test
    fun `back discards a move`() {
        show()
        enter()
        menu(STEPS, "editEarlier-steps")
        assertEquals(listOf(WEIGHT, STEPS, FOOD), order())
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        assertTrue(!editing())
        assertEquals(listOf(WEIGHT, FOOD, STEPS), order())
        assertNull(saved)
    }

    @Test
    fun `Peruuta discards and Valmis saves a move`() {
        show()
        enter()
        menu(FOOD, "editSize-food-wide")
        node("editCancel").performClick()
        assertNull(saved)
        enter()
        menu(STEPS, "editEarlier-steps")
        node("editDone").performClick()
        assertTrue(!editing())
        assertEquals(listOf(WEIGHT, STEPS, FOOD), saved?.shown?.map { it.id })
        assertEquals(listOf(WEIGHT, STEPS, FOOD), order())
    }

    @Test
    fun `Valmis with no change saves nothing`() {
        show()
        enter()
        node("editDone").performClick()
        assertNull(saved)
        assertTrue(!editing())
    }

    @Test
    fun `rotation keeps the draft`() {
        val restore = StateRestorationTester(compose)
        restore.setContent { Screen() }
        enter()
        menu(STEPS, "editEarlier-steps")
        restore.emulateSavedInstanceStateRestore()
        assertTrue(editing())
        assertEquals(listOf(WEIGHT, STEPS, FOOD), order())
    }

    @Test
    fun `a food-sheet intent while editing ends edit mode and drops the draft`() {
        show()
        enter()
        menu(STEPS, "editEarlier-steps")
        food = food.copy(sheetDay = HomeFixtures.today)
        compose.waitForIdle()
        food = food.copy(sheetDay = null)
        assertTrue(!editing())
        assertEquals(listOf(WEIGHT, FOOD, STEPS), order())
        enter()
        menu(STEPS, "editEarlier-steps")
        HomeEditSignal.outsideIntent("test")
        compose.waitForIdle()
        assertTrue(!editing(), "the widget's or weigh-in's intent ends it too")
        assertEquals(listOf(WEIGHT, FOOD, STEPS), order())
    }

    @Test
    fun `losing Health Connect access ends edit mode`() {
        show()
        enter()
        state = state.copy(access = Access.DENIED)
        compose.waitForIdle()
        state = state.copy(access = Access.GRANTED)
        assertTrue(!editing())
    }

    @Test
    fun `in edit mode a tap on the steps card or the weight card opens nothing`() {
        show()
        enter()
        node("homeSlot-steps").performClick()
        node("homeSlot-weight").performClick()
        assertEquals(0, stepsOpened)
        assertEquals(0, weighIns)
        assertEquals(0, screens)
    }

    @Test
    fun `Pieni on the weight card puts food beside it`() {
        show()
        enter()
        menu(WEIGHT, "editSize-weight-small")
        assertEquals(bounds("homeSlot-weight").top, bounds("homeSlot-food").top)
        assertTrue(bounds("homeSlot-weight").right <= bounds("homeSlot-food").left)
    }

    @Test
    fun `Siirrä aiemmaksi on steps gives weight, steps, food and the first card cannot move earlier`() {
        show()
        enter()
        menu(STEPS, "editEarlier-steps")
        assertEquals(listOf(WEIGHT, STEPS, FOOD), order())
        node("editMenu-weight").performClick()
        node("editEarlier-weight").assertIsNotEnabled()
        node("editLater-weight").assertIsEnabled()
    }

    @Test
    fun `hide steps, Valmis, steps gone`() {
        show()
        enter()
        node("editAdd").assertIsEnabled() // always: it lists the charts and Uusi kaavio (home-screen "Chart cards")
        menu(STEPS, "editHide-steps")
        node("editDone").performClick()
        assertEquals(listOf(WEIGHT, FOOD), order())
        assertEquals(listOf(HomeLayout.BURN, STEPS), saved?.hidden?.map { it.id })
    }

    @Test
    fun `Lisää kortti brings steps back last at its size`() {
        state = state.copy(homeLayout = HomeLayout.DEFAULT.resize(STEPS, CardSize.WIDE).hide(STEPS))
        show()
        enter()
        node("editAdd").performClick()
        node("editAddItem-steps").performClick()
        node("editAddItem-steps").assertDoesNotExist()
        assertEquals(listOf(WEIGHT, FOOD, STEPS), order())
        assertTrue(bounds("homeSlot-steps").right - bounds("homeSlot-steps").left > (bounds("homeSlot-food").right - bounds("homeSlot-food").left) * 1.5f, "wide as before")
    }

    @Test
    fun `the last shown card cannot be hidden`() {
        state = state.copy(homeLayout = HomeLayout.DEFAULT.hide(WEIGHT).hide(FOOD))
        show()
        node("homeSlot-steps").performTouchInput { longClick() }
        node("editMenu-steps").performClick()
        node("editHide-steps").assertIsNotEnabled()
    }

    @Test
    fun `Palauta oletus then Valmis gives the default layout`() {
        state = state.copy(homeLayout = HomeLayout.DEFAULT.move(2, 0).resize(FOOD, CardSize.BIG))
        show()
        enter()
        node("homeGrid").performScrollToNode(androidx.compose.ui.test.hasTestTag("editReset"))
        node("editReset").assertIsEnabled()
        node("editReset").performClick()
        node("editReset").assertIsNotEnabled()
        node("editDone").performClick()
        assertEquals(HomeLayout.DEFAULT, saved)
        assertEquals(listOf(WEIGHT, FOOD, STEPS), order())
    }

    @Test
    fun `TalkBack reads the food card's place and size and offers its actions`() {
        show()
        val before = node("homeSlot-food").fetchSemanticsNode().config[SemanticsActions.CustomActions].map { it.label }
        assertEquals(listOf("Muokkaa etusivua"), before)
        enter()
        val config = node("homeSlot-food").fetchSemanticsNode().config
        assertEquals(listOf("Syöty, 2/3, pieni"), config[SemanticsProperties.ContentDescription])
        val labels = config[SemanticsActions.CustomActions].map { it.label }
        assertTrue("Siirrä aiemmaksi" in labels && "Siirrä myöhemmäksi" in labels && "Leveä" in labels && "Iso" in labels && "Piilota" in labels, labels.toString())
        assertTrue("Pieni" !in labels, "the current size is not offered")
    }

}
