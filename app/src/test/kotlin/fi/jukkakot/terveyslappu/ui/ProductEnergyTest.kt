package fi.jukkakot.terveyslappu.ui

import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.TerveysApp
import fi.jukkakot.terveyslappu.food.Amount
import fi.jukkakot.terveyslappu.food.AmountUnit
import fi.jukkakot.terveyslappu.food.EditInput
import fi.jukkakot.terveyslappu.food.EditMode
import fi.jukkakot.terveyslappu.food.Energy
import fi.jukkakot.terveyslappu.food.FoodItem
import fi.jukkakot.terveyslappu.food.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/** product-energy-fix: correcting a product's energy against a fake Health Connect. Made-up foods only (NFR-1). */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w411dp-h891dp")
class ProductEnergyTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private lateinit var app: TerveysApp
    private val records = FakeFoodRecords()
    private val store = ViewModelStore()
    private lateinit var model: FoodViewModel
    private val zone = ZoneId.systemDefault()
    private val today = LocalDate.now(zone)
    private val messages = mutableListOf<UiMessage>()
    private var collector: Job? = null
    private val lakritsi = Product("Lakritsi", energy = Energy.perPiece(10.0), lastAmount = Amount(5.0, AmountUnit.KPL))

    @Before
    fun setUp() {
        app = compose.activity.application as TerveysApp
        app.foodRecords = records
        model = ViewModelProvider(store, ViewModelProvider.AndroidViewModelFactory(app))[FoodViewModel::class.java]
        collector = CoroutineScope(Dispatchers.Main.immediate).launch { model.messages.collect { messages += it } }
        app.products.put(lakritsi)
        // Three days of 5 kpl at 50 kcal and one typed total of 80 kcal.
        runBlocking { (1..3).forEach { d -> records.insert(listOf(item("l$d", today.minusDays(d.toLong()), 50.0, Amount(5.0, AmountUnit.KPL)))) }
            records.insert(listOf(item("own", today.minusDays(1), 80.0, null, pos = 2)))
        }
        model.refresh()
        idle()
    }

    @After
    fun tearDown() {
        collector?.cancel()
        store.clear()
    }

    private fun item(uuid: String, day: LocalDate, kcal: Double?, amount: Amount?, name: String = "Lakritsi", pos: Int = 1): FoodItem {
        val t = day.atTime(12, 0).atZone(zone).toInstant()
        return FoodItem(uuid, day, pos, name, kcal, amount, false, t, zone.rules.getOffset(t), true)
    }

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    private val state get() = model.state.value

    private fun kcal(uuid: String) = records.items.single { it.uuid == uuid }.kcal

    private fun correctInSettings() {
        assertFalse(model.saveProduct(lakritsi.key, lakritsi.copy(energy = Energy.perPiece(12.0))))
        idle()
    }

    @Test
    fun `Paivita kaikki recalculates the earlier items and keeps the typed total`() {
        correctInSettings()
        val ask = assertNotNull(state.energyAsk)
        assertEquals(EnergyAsk("Lakritsi", 3, 3, 150, 180, 1), ask)
        model.chooseEnergy(EnergyChoice.ALL)
        idle()
        assertEquals(listOf(60.0, 60.0, 60.0), listOf("l1", "l2", "l3").map { kcal(it) })
        assertEquals(80.0, kcal("own"))
        assertEquals(12.0, app.products.find("Lakritsi")?.energy?.kcal)
        assertEquals(1, state.productSaves)
        assertEquals(R.plurals.food_energy_updated, messages.last().text)
    }

    @Test
    fun `Vain jatkossa saves the product only`() {
        correctInSettings()
        model.chooseEnergy(EnergyChoice.FUTURE)
        idle()
        assertEquals(50.0, kcal("l1"))
        assertEquals(12.0, app.products.find("Lakritsi")?.energy?.kcal)
        assertEquals(1, state.productSaves)
    }

    @Test
    fun `Peruuta writes nothing and keeps the editor open`() {
        correctInSettings()
        model.chooseEnergy(EnergyChoice.CANCEL)
        idle()
        assertNull(state.energyAsk)
        assertEquals(50.0, kcal("l1"))
        assertEquals(10.0, app.products.find("Lakritsi")?.energy?.kcal)
        assertEquals(0, state.productSaves)
    }

    @Test
    fun `Kumoa restores the product and the items, an item changed again stays`() {
        model.openSheet(today)
        val edited = records.items.single { it.uuid == "l1" }
        model.openEditor(edited)
        model.saveEdit(edited, EditInput("Lakritsi", EditMode.KPL, 5.0, 12.0, false))
        idle()
        assertEquals(2, state.energyAsk?.count)
        assertNotNull(state.editing, "the editor stays open behind the dialog")
        model.chooseEnergy(EnergyChoice.ALL)
        idle()
        assertNull(state.editing)
        assertIs<SheetLine.EnergyUpdated>(state.line)
        assertEquals(listOf(60.0, 60.0, 60.0), listOf("l1", "l2", "l3").map { kcal(it) })
        // Changed again before Kumoa.
        val again = records.items.single { it.uuid == "l3" }
        records.stored.entries.first { it.value.uuid == "l3" }.setValue(again.copy(kcal = 70.0))
        model.undoEnergyLine()
        idle()
        assertEquals(10.0, app.products.find("Lakritsi")?.energy?.kcal)
        assertEquals(listOf(50.0, 50.0, 70.0), listOf("l1", "l2", "l3").map { kcal(it) })
    }

    @Test
    fun `a failed write offers a retry that finishes the update`() {
        model.openSheet(today)
        correctInSettings()
        records.failInsert = true
        model.chooseEnergy(EnergyChoice.ALL)
        idle()
        val line = assertIs<SheetLine.EnergyFailed>(state.line)
        assertEquals(0 to 3, line.done to line.of)
        records.failInsert = false
        model.retryEnergyLine()
        idle()
        assertIs<SheetLine.EnergyUpdated>(state.line)
        assertEquals(listOf(60.0, 60.0, 60.0), listOf("l1", "l2", "l3").map { kcal(it) })
    }

    @Test
    fun `a failed read saves from now on only`() {
        records.failReads = 1
        correctInSettings()
        assertNull(state.energyAsk)
        assertEquals(12.0, app.products.find("Lakritsi")?.energy?.kcal)
        assertEquals(50.0, kcal("l1"))
        assertEquals(R.string.food_energy_read_failed, messages.last().text)
    }

    @Test
    fun `an amount-only save asks nothing`() {
        val edited = records.items.single { it.uuid == "l1" }
        model.openEditor(edited)
        model.saveEdit(edited, EditInput("Lakritsi", EditMode.KPL, 6.0, 10.0, false))
        idle()
        assertNull(state.energyAsk)
        assertNull(state.editing)
        assertEquals(60.0, kcal("l1"))
        assertEquals(50.0, kcal("l2"))
    }

    @Test
    fun `the dialog shows the counts, singular for one`() {
        var choice: EnergyChoice? = null
        compose.setContent { TerveysTheme { EnergyDialog(EnergyAsk("Lakritsi", 1, 1, 50, 60, 0)) { choice = it } } }
        val res = compose.activity.resources
        compose.onNodeWithTag("energyLine").assertTextContains(res.getQuantityString(R.plurals.food_energy_items, 1, 1), substring = true)
        compose.onNodeWithTag("energyKept").assertDoesNotExist()
        compose.onNodeWithTag("energyAll").performClick()
        assertEquals(EnergyChoice.ALL, choice)
    }
}
