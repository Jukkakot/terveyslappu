package fi.jukkakot.terveyslappu.home

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.TerveysApp
import fi.jukkakot.terveyslappu.ui.MainViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

/** home-screen "Card grid" scenario Remembered: Valmis saves, a new start loads the same layout. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class HomePrefsTest {
    private val app: TerveysApp = ApplicationProvider.getApplicationContext()

    private fun model(store: ViewModelStore) = ViewModelProvider(store, ViewModelProvider.AndroidViewModelFactory(app))[MainViewModel::class.java]

    @Test
    fun `a saved layout is loaded again by a new view model`() {
        val layout = HomeLayout.DEFAULT.resize(HomeLayout.STEPS, CardSize.BIG).move(2, 0)
        val first = ViewModelStore()
        assertEquals(HomeLayout.DEFAULT, model(first).state.value.homeLayout)
        model(first).onHomeLayout(layout)
        assertEquals(layout, model(first).state.value.homeLayout)
        first.clear()
        val second = ViewModelStore()
        assertEquals(layout, model(second).state.value.homeLayout)
        second.clear()
        assertEquals(layout, HomePrefs(app).load())
    }
}
