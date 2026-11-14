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
    fun `a sub-screen opened from the list returns to the list`() {
        val back = MainUiState().withSettings(SettingsPage.DATA, Screen.SETTINGS).back()
        assertEquals(Screen.SETTINGS, back.screen)
        assertEquals(SettingsPage.LIST, back.settingsPage)
        assertEquals(Screen.HOME, back.back().screen)
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
