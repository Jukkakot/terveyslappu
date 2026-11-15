package fi.jukkakot.terveyslappu.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.TerveysApp
import fi.jukkakot.terveyslappu.chart.CustomChart
import fi.jukkakot.terveyslappu.chart.CustomCharts
import fi.jukkakot.terveyslappu.chart.Period
import fi.jukkakot.terveyslappu.home.CardSize
import fi.jukkakot.terveyslappu.home.HomeCard
import fi.jukkakot.terveyslappu.home.HomeLayout
import fi.jukkakot.terveyslappu.home.HomePrefs
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

/** The own charts' actions in the view model (custom-charts tasks 1.2). */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class ChartViewModelTest {
    private val app: TerveysApp = ApplicationProvider.getApplicationContext()
    private val store = ViewModelStore()
    private val model = ViewModelProvider(store, ViewModelProvider.AndroidViewModelFactory(app))[MainViewModel::class.java]

    @After
    fun clear() = store.clear()

    private fun save(name: String): Int = model.onSaveChart(CustomCharts.newDraft(model.state.value.charts).copy(name = name).toChart())

    @Test
    fun `saving creates charts with new ids that a new start loads`() {
        val a = save("Liike")
        val b = save("Ilta")
        assertEquals(listOf(1, 2), listOf(a, b))
        val edited = model.state.value.charts.first().copy(name = "Aamu")
        model.onSaveChart(edited)
        assertEquals(listOf("Aamu", "Ilta"), model.state.value.charts.map { it.name })
        assertEquals(listOf("Aamu", "Ilta"), app.charts.load().charts.map { it.name })
    }

    @Test
    fun `delete removes the card from the saved layout`() {
        val id = save("Liike")
        model.onHomeLayout(HomeLayout.DEFAULT.addChart(CustomChart.cardId(id)))
        model.onDeleteChart(id)
        assertEquals(emptyList(), model.state.value.charts)
        assertEquals(HomeLayout.DEFAULT, model.state.value.homeLayout)
        assertEquals(HomeLayout.DEFAULT, HomePrefs(app).load(setOf(CustomChart.cardId(id))))
    }

    @Test
    fun `deleting the only shown chart card saves the default layout`() {
        val id = save("Liike")
        model.onHomeLayout(HomeLayout(listOf(HomeCard(CustomChart.cardId(id), CardSize.WIDE)), HomeLayout.FIXED))
        model.onDeleteChart(id)
        assertEquals(HomeLayout.DEFAULT, model.state.value.homeLayout)
        assertEquals(listOf(HomeLayout.WEIGHT, HomeLayout.FOOD, HomeLayout.STEPS), HomePrefs(app).load().shown.map { it.id })
    }

    @Test
    fun `a period change is stored and leaves the shared period alone`() {
        val id = save("Liike")
        val shared = model.state.value.period
        model.onChartPeriod(id, Period.QUARTER)
        assertEquals(Period.QUARTER, model.state.value.charts.single().period)
        assertEquals(Period.QUARTER, app.charts.load().charts.single().period)
        assertEquals(shared, model.state.value.period)
    }

    @Test
    fun `a chart card opens Historia on the chart and back clears it`() {
        val id = save("Liike")
        model.openChart(id)
        assertEquals(Screen.HISTORY, model.state.value.screen)
        assertEquals(id, model.state.value.historyChart)
        model.back()
        assertEquals(Screen.HOME, model.state.value.screen)
        assertEquals(null, model.state.value.historyChart)
    }
}
