package fi.jukkakot.terveyslappu.ui

import android.os.Looper
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.TerveysApp
import fi.jukkakot.terveyslappu.food.Amount
import fi.jukkakot.terveyslappu.food.AmountUnit
import fi.jukkakot.terveyslappu.food.EditInput
import fi.jukkakot.terveyslappu.food.EditMode
import fi.jukkakot.terveyslappu.food.FoodItem
import kotlinx.coroutines.CompletableDeferred
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The food view model's add, editor-return and sheet-line rules (food-widget), against a fake Health Connect. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class FoodViewModelTest {
    private lateinit var app: TerveysApp
    private val records = FakeFoodRecords()
    private val store = ViewModelStore()
    private lateinit var model: FoodViewModel
    private val zone = ZoneId.systemDefault()
    private val today = LocalDate.now(zone)

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        app.foodRecords = records
        model = ViewModelProvider(store, ViewModelProvider.AndroidViewModelFactory(app))[FoodViewModel::class.java]
    }

    @After
    fun tearDown() = store.clear()

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    private val state get() = model.state.value

    private fun ready() {
        model.refresh()
        idle()
        model.openSheet(today)
    }

    private fun type(text: String) {
        model.onQuery(text)
        model.addTyped()
        idle()
    }

    private fun earlier(name: String, pos: Int): FoodItem {
        val t = today.atTime(6, 0).atZone(zone).toInstant()
        return FoodItem("e$pos", today, pos, name, 100.0, Amount(1.0, AmountUnit.KPL), false, t, zone.rules.getOffset(t), true)
    }

    @Test
    fun `typed items are written in order and the line shows the latest`() {
        ready()
        type("kahvi")
        type("350")
        assertEquals(listOf("Kahvi", "Pikalisäys"), records.items.sortedBy { it.pos }.map { it.name })
        assertEquals("Pikalisäys", state.lastAdded?.name)
        assertNotNull(app.products.find("Kahvi"))
    }

    @Test
    fun `Tallenna in the editor returns to the sheet with the edited item`() {
        ready()
        type("Rahka")
        val added = assertNotNull(state.lastAdded)
        model.openEditor(added)
        assertNull(state.sheetDay)
        model.saveEdit(added, EditInput("Rahka", EditMode.G, 200.0, 65.0, false))
        idle()
        assertEquals(today, state.sheetDay)
        assertNull(state.editing)
        assertEquals(Amount(200.0, AmountUnit.G), state.lastAdded?.amount)
        assertEquals(130.0, records.items.single().kcal)
    }

    @Test
    fun `Peruuta returns to the sheet with the same line`() {
        ready()
        type("Rahka")
        val added = assertNotNull(state.lastAdded)
        model.openEditor(added)
        model.closeEditor()
        assertEquals(today, state.sheetDay)
        assertEquals(added, state.lastAdded)
    }

    @Test
    fun `Poista returns to the sheet with a Kumoa line and no item line`() {
        ready()
        type("Rahka")
        model.openEditor(assertNotNull(state.lastAdded))
        model.delete(state.editing!!)
        idle()
        assertEquals(today, state.sheetDay)
        assertNull(state.lastAdded)
        assertIs<SheetLine.Deleted>(state.line)
        assertTrue(records.items.isEmpty())
        model.undoDelete()
        idle()
        assertEquals(listOf("Rahka"), records.items.map { it.name })
    }

    @Test
    fun `closing the sheet drops a pending Kumoa`() {
        ready()
        type("Rahka")
        model.openEditor(state.lastAdded!!)
        model.delete(state.editing!!)
        model.closeSheet()
        assertNull(state.line)
    }

    @Test
    fun `the editor opened from the list returns to the list`() {
        records.stored["x"] = earlier("Puuro", 1).copy(recordId = "rec:x")
        model.refresh()
        idle()
        model.openEditor(state.items!!.single())
        model.closeEditor()
        assertNull(state.sheetDay)
        assertNull(state.editing)
    }

    @Test
    fun `an add before the day is read lands at the end once it is read`() {
        records.stored["x"] = earlier("Puuro", 1).copy(recordId = "rec:x")
        val gate = CompletableDeferred<Unit>()
        records.readGate = gate
        model.refresh()
        idle()
        model.openSheet(today)
        assertNull(state.items)
        model.onQuery("kahvi")
        model.addTyped()
        model.onQuery("Pulla")
        model.addTyped()
        idle()
        gate.complete(Unit)
        idle()
        assertEquals(listOf("Puuro" to 1, "Kahvi" to 2, "Pulla" to 3), records.items.sortedBy { it.pos }.map { it.name to it.pos })
    }

    @Test
    fun `after a failed read the add reads again and appends at the end`() {
        records.stored["x"] = earlier("Puuro", 1).copy(recordId = "rec:x")
        records.failReads = 1
        ready()
        assertTrue(state.loadFailed)
        type("kahvi")
        assertEquals(2, records.items.single { it.name == "Kahvi" }.pos)
    }

    @Test
    fun `a failed write shows the line and keeps the sheet open`() {
        ready()
        records.failInsert = true
        type("kahvi")
        assertEquals(SheetLine.Failed, state.line)
        assertEquals(today, state.sheetDay)
    }

    @Test
    fun `closing at once keeps the write`() {
        ready()
        model.onQuery("kahvi")
        model.addTyped()
        model.closeSheet()
        store.clear()
        idle()
        assertEquals(listOf("Kahvi"), records.items.map { it.name })
    }

    @Test
    fun `the sheet keeps the day it was opened with`() {
        model.refresh()
        idle()
        val yesterday = today.minusDays(1)
        model.openSheet(yesterday)
        type("kahvi")
        assertEquals(yesterday, records.items.single().day)
    }
}
