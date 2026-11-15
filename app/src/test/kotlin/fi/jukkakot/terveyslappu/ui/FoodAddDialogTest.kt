package fi.jukkakot.terveyslappu.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Looper
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.FoodAddActivity
import fi.jukkakot.terveyslappu.TerveysApp
import androidx.lifecycle.ViewModelProvider
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import fi.jukkakot.terveyslappu.food.Amount
import fi.jukkakot.terveyslappu.food.AmountUnit
import fi.jukkakot.terveyslappu.food.Energy
import fi.jukkakot.terveyslappu.food.Product

/** Smoke test of the Lisää annos dialog (food-widget), against a fake Health Connect; made-up foods only. */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w411dp-h891dp")
class FoodAddDialogTest {
    @get:Rule
    val compose = createEmptyComposeRule()

    private val records = FakeFoodRecords()

    /**
     * The activity's AndroidViewModel factory is a process-wide singleton holding the first test's
     * application; Robolectric gives every test a new one, so forget the old factory.
     */
    @Before
    fun freshViewModelFactory() {
        ViewModelProvider.AndroidViewModelFactory::class.java.getDeclaredField("_instance").apply { isAccessible = true }.set(null, null)
    }

    private fun open(): ActivityScenario<FoodAddActivity> {
        val app = ApplicationProvider.getApplicationContext<TerveysApp>()
        app.foodRecords = records
        val scenario = ActivityScenario.launch<FoodAddActivity>(FoodAddActivity.intent(app, "widget"))
        compose.waitUntilAtLeastOneExists(hasTestTag("addField"), 5_000)
        return scenario
    }

    private fun enter(text: String) {
        compose.onNodeWithTag("addField").performTextInput(text)
        compose.onNodeWithTag("addField").performImeAction()
        compose.waitForIdle()
    }

    @Test
    fun `the dialog adds to today and learns the product`() {
        open().use {
            enter("Tomaattikeitto")
            compose.waitUntilAtLeastOneExists(hasTestTag("lastAdded"), 5_000)
            val item = records.items.single()
            assertEquals("Tomaattikeitto", item.name)
            assertEquals(LocalDate.now(), item.day)
            assertNotNull(ApplicationProvider.getApplicationContext<TerveysApp>().products.find("Tomaattikeitto"))
        }
    }

    @Test
    fun `without the permission the sheet offers Salli instead of the field`() {
        records.permitted = false
        val app = ApplicationProvider.getApplicationContext<TerveysApp>()
        app.foodRecords = records
        ActivityScenario.launch<FoodAddActivity>(FoodAddActivity.intent(app, "widget")).use {
            compose.waitUntilAtLeastOneExists(hasTestTag("sheetGrant"), 5_000)
            compose.onNodeWithTag("addField").assertDoesNotExist()
        }
    }

    @Test
    fun `closing at once keeps the write`() {
        val scenario = open()
        compose.onNodeWithTag("addField").performTextInput("Tomaattikeitto")
        compose.onNodeWithTag("addField").performImeAction()
        scenario.close()
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(listOf("Tomaattikeitto"), records.items.map { it.name })
    }

    // food-sheet-layout; made-up foods only.
    private fun copy(text: String) {
        val app = ApplicationProvider.getApplicationContext<TerveysApp>()
        app.getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("test", text))
    }

    @Test
    fun `the keyboard stays down on open, the field has no focus`() {
        open().use {
            compose.waitForIdle()
            compose.onNodeWithTag("addField").assertIsNotFocused()
            compose.onNodeWithTag("clipOffer").assertDoesNotExist()
        }
    }

    @Test
    fun `the card shows without the keyboard and Lisaa kaikki adds in the sheet`() {
        copy("""Arvio: {"items":[{"name":"Testikeitto","amount":300,"unit":"g","kcal":180},{"name":"Testileipa","kcal":90}]}""")
        open().use {
            compose.waitUntilAtLeastOneExists(hasTestTag("clipOffer"), 5_000)
            compose.onNodeWithTag("addField").assertIsNotFocused()
            compose.onNodeWithTag("clipAddAll").performClick()
            compose.waitUntilAtLeastOneExists(hasTestTag("sheetImported"), 5_000)
            assertEquals(listOf("Testikeitto", "Testileipa"), records.items.sortedBy { it.pos }.map { it.name })
            compose.onNodeWithTag("clipOffer").assertDoesNotExist()
            compose.onNodeWithTag("addField").assertExists()
        }
    }

    @Test
    fun `a chip tap adds the product and it appears in the day list`() {
        val app = ApplicationProvider.getApplicationContext<TerveysApp>()
        app.products.put(Product("Kaurapuuro testi", false, Energy(70.0, AmountUnit.G), Amount(300.0, AmountUnit.G)))
        open().use {
            compose.waitUntilAtLeastOneExists(hasTestTag("chip-Kaurapuuro testi"), 5_000)
            compose.onNodeWithTag("chip-Kaurapuuro testi").performClick()
            compose.waitUntilAtLeastOneExists(hasTestTag("lastAdded"), 5_000)
            shadowOf(Looper.getMainLooper()).idle()
            assertEquals(listOf("Kaurapuuro testi"), records.items.map { it.name })
            compose.onNodeWithTag("sheetTitle").assertTextContains("Tänään · 210 kcal")
            compose.onNodeWithTag("sheetDayList").assertExists()
        }
    }

    @Test
    fun `Tuo ruoka-annokset is in the menu`() {
        open().use {
            compose.onNodeWithTag("sheetMenu").performClick()
            compose.onNodeWithTag("aiPaste").assertExists()
            compose.onNodeWithTag("aiCopyPrompt").assertExists()
        }
    }

    @Test
    fun `the enter row adds what enter adds`() {
        open().use {
            compose.onNodeWithTag("addField").performTextInput("350")
            compose.onNodeWithTag("enterRow").performClick()
            compose.waitUntilAtLeastOneExists(hasTestTag("lastAdded"), 5_000)
            compose.onNodeWithTag("addField").performTextInput("Testimuffinssi")
            compose.onNodeWithTag("enterRow").performClick()
            compose.waitForIdle()
            // The writes run in the app scope on the main looper.
            shadowOf(Looper.getMainLooper()).idle()
            val items = records.items.sortedBy { it.pos }
            assertEquals(listOf("Pikalisäys", "Testimuffinssi"), items.map { it.name })
            assertEquals(350.0, items.first().kcal)
            assertNull(items.last().kcal)
        }
    }

    @Test
    fun `only own products have the delete button`() {
        val app = ApplicationProvider.getApplicationContext<TerveysApp>()
        app.products.put(Product("Kaurapuuro testi", false, Energy(70.0, AmountUnit.G), Amount(300.0, AmountUnit.G)))
        open().use {
            compose.onNodeWithTag("addField").performTextInput("kaurapuu")
            compose.waitForIdle()
            compose.onNodeWithTag("deleteProduct-Kaurapuuro testi").assertExists()
            compose.onNodeWithTag("edit-Kaurapuuro").assertExists()
            compose.onNodeWithTag("deleteProduct-Kaurapuuro").assertDoesNotExist()
            compose.onNodeWithTag("deleteProduct-Kaurapuuro testi").performClick()
            compose.waitUntilAtLeastOneExists(hasTestTag("sheetProductRemoved"), 5_000)
            assertNull(app.products.find("Kaurapuuro testi"))
        }
    }
}
