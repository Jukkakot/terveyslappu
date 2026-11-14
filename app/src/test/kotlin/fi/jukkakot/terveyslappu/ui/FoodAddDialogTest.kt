package fi.jukkakot.terveyslappu.ui

import android.os.Looper
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
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
}
