package fi.jukkakot.terveyslappu.ui

import fi.jukkakot.terveyslappu.data.DataFormat
import fi.jukkakot.terveyslappu.weight.HistoryAccess
import org.junit.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class MainUiStateTest {
    private val t0 = Instant.parse("2026-11-08T07:00:00Z")

    @Test
    fun `every load stamps the time the charts take their now from`() {
        val first = MainUiState(loadFailed = true)
        assertNull(first.loadedAt)
        val a = first.loaded(emptyList(), HistoryAccess.GRANTED, t0)
        assertEquals(t0, a.loadedAt)
        assertFalse(a.loadFailed)
        val b = a.loaded(emptyList(), HistoryAccess.NOT_GRANTED, t0.plusSeconds(3600))
        assertEquals(t0.plusSeconds(3600), b.loadedAt)
        assertEquals(HistoryAccess.NOT_GRANTED, b.history)
    }

    @Test
    fun `every export format has its own pending action`() {
        for (format in DataFormat.entries) assertEquals(format, AfterHistory.export(format).format)
    }
}

class SettingsBackRuleTest {
    private val settings = MainUiState(screen = Screen.SETTINGS)

    @Test
    fun `list goes home, help and history go home`() {
        assertEquals(Screen.HOME, settings.back().screen)
        assertEquals(Screen.HOME, MainUiState(screen = Screen.HELP).back().screen)
        assertEquals(Screen.HOME, MainUiState(screen = Screen.HISTORY).back().screen)
    }

    @Test
    fun `Historia opened from Ruoka's Viikko returns to Ruoka, and a day there does not loop`() {
        val week = MainUiState(screen = Screen.HISTORY, historyOnEnergy = true, historyOrigin = Screen.FOOD)
        assertEquals(Screen.FOOD, week.back().screen)
        val day = week.openFood()
        assertEquals(Screen.FOOD, day.screen)
        assertEquals(Screen.HOME, day.back().screen)
    }

    @Test
    fun `Ruoka after the add search stays on Ruoka from Ruoka, and goes back to where Ruoka came from`() {
        val fromHome = MainUiState().openFoodAfterSearch()
        assertEquals(Screen.FOOD, fromHome.screen)
        assertEquals(Screen.HOME, fromHome.back().screen)
        // From Ruoka the origin is unchanged (Historia stays its origin).
        val onRuoka = MainUiState(screen = Screen.FOOD, foodOrigin = Screen.HISTORY, historyPage = 4)
        assertEquals(Screen.FOOD, onRuoka.openFoodAfterSearch().screen)
        assertEquals(Screen.HISTORY, onRuoka.openFoodAfterSearch().foodOrigin)
    }

    @Test
    fun `Ruoka after the add search from Historia returns to the tab that was open`() {
        val history = MainUiState(screen = Screen.HISTORY, historyPage = 1)
        val ruoka = history.openFoodAfterSearch()
        assertEquals(Screen.FOOD, ruoka.screen)
        val back = ruoka.back()
        assertEquals(Screen.HISTORY, back.screen)
        assertEquals(1, back.historyStartPage)
        assertFalse(back.historyOnEnergy)
        // Ruoka opened from the Energia tab as before still returns to Energia.
        val plain = history.openFood().back()
        assertEquals(true, plain.historyOnEnergy)
        assertNull(plain.historyStartPage)
    }

    @Test
    fun `a sub-screen opened from the list returns to the list`() {
        val back = MainUiState().withSettings(SettingsPage.DATA, Screen.SETTINGS).back()
        assertEquals(Screen.SETTINGS, back.screen)
        assertEquals(SettingsPage.LIST, back.settingsPage)
        assertEquals(Screen.HOME, back.back().screen)
    }

    @Test
    fun `the steps source opened from Askeleet returns to Askeleet`() {
        val opened = MainUiState(screen = Screen.HISTORY).withStepsSource()
        assertEquals(Screen.SETTINGS, opened.screen)
        assertEquals(SettingsPage.STEPS, opened.settingsPage)
        val back = opened.back()
        assertEquals(Screen.HISTORY, back.screen)
        assertEquals(true, back.historyOnSteps)
        val fromList = MainUiState().withSettings(SettingsPage.STEPS, Screen.SETTINGS).back()
        assertEquals(SettingsPage.LIST, fromList.settingsPage)
    }

    @Test
    fun `a deep-linked sub-screen returns to its origin`() {
        for (from in listOf(Screen.HOME, Screen.HISTORY)) {
            val back = MainUiState(screen = from).withSettings(SettingsPage.PROFILE, from).back()
            assertEquals(from, back.screen)
            assertEquals(SettingsPage.LIST, back.settingsPage)
        }
    }
}
