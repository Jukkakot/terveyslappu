package fi.jukkakot.terveyslappu.ui

import android.os.Looper
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.TerveysApp
import fi.jukkakot.terveyslappu.food.AddSearchTarget
import fi.jukkakot.terveyslappu.food.Amount
import fi.jukkakot.terveyslappu.food.AmountUnit
import fi.jukkakot.terveyslappu.food.EditInput
import fi.jukkakot.terveyslappu.food.EditMode
import fi.jukkakot.terveyslappu.food.Energy
import fi.jukkakot.terveyslappu.food.FoodItem
import fi.jukkakot.terveyslappu.food.Product
import fi.jukkakot.terveyslappu.food.Suggestion
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
        model.openSearch(today)
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
    fun `Tallenna on Ruoka edits an item added in the search and leaves no search open`() {
        ready()
        type("Rahka")
        val added = assertNotNull(state.lastAdded)
        model.leaveSearch()
        model.openEditor(added)
        assertNull(state.searchDay)
        model.saveEdit(added, EditInput("Rahka", EditMode.G, 200.0, 65.0, false))
        idle()
        assertNull(state.searchDay)
        assertNull(state.editing)
        assertEquals(130.0, records.items.single().kcal)
        // An edit keeps the highlight.
        assertEquals(setOf(added.uuid), state.highlight)
    }

    @Test
    fun `Poista on Ruoka drops the annos from the highlight and the count`() {
        ready()
        type("Rahka")
        val added = assertNotNull(state.lastAdded)
        model.leaveSearch()
        model.delete(added)
        idle()
        assertTrue(records.items.isEmpty())
        assertTrue(state.highlight.isEmpty())
    }

    @Test
    fun `adds are counted from the tap and Kumoa takes the latest back out`() {
        ready()
        type("Rahka")
        type("kahvi")
        assertEquals(2, state.addedIds.size)
        model.undoAdd()
        idle()
        assertEquals(1, state.addedIds.size)
        assertEquals(listOf("Rahka"), records.items.map { it.name })
    }

    @Test
    fun `an add is counted before the day has been read`() {
        val gate = CompletableDeferred<Unit>()
        records.readGate = gate
        model.refresh()
        idle()
        model.openSearch(today)
        model.onQuery("kahvi")
        model.addTyped()
        assertEquals(1, state.addedIds.size)
        gate.complete(Unit)
        idle()
        assertEquals(1, state.addedIds.size)
    }

    @Test
    fun `a failed write no longer counts as added and back adds nothing`() {
        ready()
        records.failInsert = true
        type("kahvi")
        assertTrue(state.addedIds.isEmpty())
        assertEquals(AddSearchTarget.Origin, model.leaveSearch())
        assertNull(state.searchDay)
        assertTrue(state.highlight.isEmpty())
    }

    @Test
    fun `leaving with adds shows that day on Ruoka with them highlighted and drops the bar`() {
        model.refresh()
        idle()
        val yesterday = today.minusDays(1)
        model.openSearch(yesterday)
        type("kahvi")
        type("pulla")
        val ids = records.items.sortedBy { it.pos }.map { it.uuid }
        val target = assertIs<AddSearchTarget.Ruoka>(model.leaveSearch())
        assertEquals(yesterday, target.day)
        assertEquals(ids, target.uuids)
        assertEquals(ids.toSet(), state.highlight)
        assertEquals(yesterday, state.day)
        assertNull(state.searchDay)
        assertNull(state.line)
    }

    @Test
    fun `leaving without adds returns to the origin and keeps Ruoka as it was`() {
        ready()
        assertEquals(AddSearchTarget.Origin, model.leaveSearch())
        assertEquals(today, state.day)
        assertTrue(state.highlight.isEmpty())
    }

    @Test
    fun `the highlight ends with a new search, a day change or leaving Ruoka`() {
        ready()
        type("Rahka")
        model.leaveSearch()
        assertEquals(1, state.highlight.size)
        model.setDay(today.minusDays(1))
        assertTrue(state.highlight.isEmpty())
        model.openSearch(today)
        type("kahvi")
        model.leaveSearch()
        model.openSearch(today)
        assertTrue(state.highlight.isEmpty())
        model.leaveSearch()
        model.showRuoka(today, setOf("x"))
        model.clearHighlight()
        assertTrue(state.highlight.isEmpty())
    }

    @Test
    fun `the widget exit shows Ruoka with the highlight and reads the day after the writes`() {
        records.stored["x"] = earlier("Puuro", 1).copy(recordId = "rec:x")
        model.refresh()
        idle()
        model.openSearch(today)
        model.showRuoka(today, setOf("x"))
        idle()
        assertNull(state.searchDay)
        assertEquals(setOf("x"), state.highlight)
        assertEquals(1, state.items?.size)
    }

    @Test
    fun `the empty field ranking is fixed while the search is open`() {
        app.products.put(Product("Testipuuro", false, Energy(100.0, AmountUnit.KPL), Amount(1.0, AmountUnit.KPL)))
        app.products.put(Product("Testikahvi", false, Energy(5.0, AmountUnit.KPL), Amount(1.0, AmountUnit.KPL)))
        ready()
        val before = assertNotNull(state.picks).map { it.name }
        type("Testikahvi")
        type("Testikahvi")
        assertEquals(before, state.picks?.map { it.name })
    }

    @Test
    fun `an edited add from the draft editor counts as added and Peruuta does not`() {
        app.products.put(Product("Testikastike", false, Energy(140.0, AmountUnit.G), Amount(250.0, AmountUnit.G)))
        ready()
        val pick = Suggestion.Own(app.products.find("Testikastike")!!)
        model.editDraft(pick)
        model.closeEditor()
        assertTrue(state.addedIds.isEmpty())
        model.editDraft(pick)
        model.addDraft(EditInput("Testikastike", EditMode.G, 350.0, 140.0, false))
        idle()
        assertEquals(1, state.addedIds.size)
    }

    @Test
    fun `Lisaa kaikki counts the imported annokset as added and Kumoa removes them`() {
        ready()
        check(100)
        model.addAllFromClip(clipJson, today)
        idle()
        assertEquals(2, state.addedIds.size)
        model.undoImport()
        idle()
        assertTrue(state.addedIds.isEmpty())
    }

    @Test
    fun `the editor opened from the list returns to the list`() {
        records.stored["x"] = earlier("Puuro", 1).copy(recordId = "rec:x")
        model.refresh()
        idle()
        model.openEditor(state.items!!.single())
        model.closeEditor()
        assertNull(state.searchDay)
        assertNull(state.editing)
    }

    @Test
    fun `an add before the day is read lands at the end once it is read`() {
        records.stored["x"] = earlier("Puuro", 1).copy(recordId = "rec:x")
        val gate = CompletableDeferred<Unit>()
        records.readGate = gate
        model.refresh()
        idle()
        model.openSearch(today)
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
        assertEquals(today, state.searchDay)
    }

    @Test
    fun `closing at once keeps the write`() {
        ready()
        model.onQuery("kahvi")
        model.addTyped()
        model.closeSearch()
        store.clear()
        idle()
        assertEquals(listOf("Kahvi"), records.items.map { it.name })
    }

    @Test
    fun `the sheet keeps the day it was opened with`() {
        model.refresh()
        idle()
        val yesterday = today.minusDays(1)
        model.openSearch(yesterday)
        type("kahvi")
        assertEquals(yesterday, records.items.single().day)
    }

    // Clipboard offer (food-sheet-clip-and-suggestions); made-up foods only.
    private val clipJson = "Arvio:\n```json\n{\"items\":[{\"name\":\"Testipuuro\",\"amount\":300,\"unit\":\"g\",\"kcal\":210},{\"name\":\"Testikahvi\",\"kcal\":5}]}\n```"

    private fun check(time: Long, text: String = clipJson): Int {
        var reads = 0
        model.checkClip(true, time) { reads++; text }
        return reads
    }

    @Test
    fun `the offer survives a second check without a second read`() {
        assertEquals(1, check(100))
        assertEquals(2, state.clipOffer?.count)
        assertEquals(215, state.clipOffer?.kcal)
        assertEquals(0, check(100))
        assertNotNull(state.clipOffer)
    }

    @Test
    fun `other text and a clip without text offer nothing`() {
        check(100, "https://example.com/a")
        assertNull(state.clipOffer)
        check(200)
        assertNotNull(state.clipOffer)
        model.checkClip(false, 300) { error("no read") }
        assertNull(state.clipOffer)
    }

    @Test
    fun `Ohita ends the offer, also when the same json is copied again`() {
        check(100)
        model.skipClipOffer()
        assertNull(state.clipOffer)
        check(100)
        assertNull(state.clipOffer)
        check(200, "Toinen viesti " + clipJson)
        assertNull(state.clipOffer)
    }

    @Test
    fun `tapping the offer opens the preview from the clipboard text and saving ends the offer`() {
        ready()
        check(100)
        model.openClipOffer(clipJson)
        val preview = assertNotNull(state.ai)
        assertEquals(2, preview.items.size)
        assertNotNull(preview.sourceKey)
        model.addAi()
        idle()
        assertNull(state.clipOffer)
        check(100)
        assertNull(state.clipOffer)
        assertEquals(2, records.items.size)
    }

    @Test
    fun `Peruuta on the preview keeps the offer`() {
        check(100)
        model.openClipOffer(clipJson)
        model.cancelAi()
        check(100)
        assertNotNull(state.clipOffer)
    }

    @Test
    fun `content saved from a share is not offered afterwards`() {
        ready()
        model.openAi(clipJson, "share")
        model.addAi()
        idle()
        check(100)
        assertNull(state.clipOffer)
    }

    @Test
    fun `the copied import instructions are never offered`() {
        val prompt = app.assets.open("ai-food-prompt.txt").bufferedReader().use { it.readText() }
        model.markTextDone(prompt)
        check(100, prompt)
        assertNull(state.clipOffer)
    }

    // Edit before adding.
    private val kastike = Product("Testikastike", false, Energy(140.0, AmountUnit.G), Amount(250.0, AmountUnit.G))

    @Test
    fun `edit before adding adds the edited item and the product remembers the amount`() {
        app.products.put(kastike)
        ready()
        model.editDraft(Suggestion.Own(kastike))
        assertNotNull(state.editing)
        assertNotNull(state.draft)
        assertNull(state.searchDay)
        model.addDraft(EditInput("Testikastike", EditMode.G, 350.0, 140.0, false))
        idle()
        val item = records.items.single()
        assertEquals(350.0, item.amount?.value)
        assertEquals(490.0, item.kcal)
        assertEquals(today, state.searchDay)
        assertNull(state.editing)
        assertNull(state.draft)
        assertEquals(item.uuid, assertIs<SheetLine.Added>(state.line).item.uuid)
        assertEquals(350.0, app.products.find("Testikastike")?.usualAmount()?.value)
    }

    @Test
    fun `Peruuta in the draft editor adds nothing and returns to the field`() {
        app.products.put(kastike)
        ready()
        model.editDraft(Suggestion.Own(kastike))
        model.closeEditor()
        idle()
        assertTrue(records.items.isEmpty())
        assertEquals(today, state.searchDay)
        assertNull(state.draft)
        assertNull(state.line)
    }

    // Lisää kaikki and the product delete (food-sheet-layout).
    @Test
    fun `Lisaa kaikki adds the card's items to the sheet's day and ends the offer`() {
        ready()
        check(100)
        model.addAllFromClip(clipJson, today)
        idle()
        assertEquals(listOf("Testipuuro", "Testikahvi"), records.items.sortedBy { it.pos }.map { it.name })
        assertTrue(records.items.all { it.day == today })
        assertNull(state.clipOffer)
        assertIs<SheetLine.Imported>(state.line)
        check(100)
        assertNull(state.clipOffer)
    }

    @Test
    fun `a changed clipboard saves nothing and refreshes the card`() {
        ready()
        check(100)
        model.addAllFromClip("""{"items":[{"name":"Testileipä","kcal":90}]}""", today)
        idle()
        assertTrue(records.items.isEmpty())
        assertEquals(1, state.clipOffer?.count)
        assertEquals(SheetLine.ClipChanged, state.line)
        model.addAllFromClip("https://example.com/a", today)
        assertNull(state.clipOffer)
    }

    @Test
    fun `Kumoa of Lisaa kaikki removes the items and the offer stays gone`() {
        ready()
        check(100)
        model.addAllFromClip(clipJson, today)
        idle()
        model.undoImport()
        idle()
        assertTrue(records.items.isEmpty())
        assertNull(app.products.find("Testipuuro"))
        check(100)
        assertNull(state.clipOffer)
    }

    @Test
    fun `a failed Lisaa kaikki keeps the offer and the products`() {
        ready()
        check(100)
        records.failInsert = true
        model.addAllFromClip(clipJson, today)
        idle()
        assertNotNull(state.clipOffer)
        assertEquals(SheetLine.Failed, state.line)
        assertNull(app.products.find("Testipuuro"))
    }

    @Test
    fun `the delete button removes a product and Kumoa brings it back`() {
        val pallo = Product("Testipallo", true, Energy(150.0, AmountUnit.KPL), Amount(1.0, AmountUnit.KPL))
        app.products.put(pallo)
        ready()
        model.deleteProduct(pallo)
        assertNull(app.products.find("Testipallo"))
        assertEquals(SheetLine.ProductRemoved(pallo), state.line)
        model.undoDeleteProduct()
        assertEquals(pallo, app.products.find("Testipallo"))
        assertNull(state.line)
    }
}
