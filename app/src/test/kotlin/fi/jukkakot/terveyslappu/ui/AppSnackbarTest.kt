package fi.jukkakot.terveyslappu.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import fi.jukkakot.terveyslappu.a11y.ActionTimeouts
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/** The bar's timing (app-appearance "Undo messages"). */
@OptIn(ExperimentalCoroutinesApi::class)
class AppSnackbarTest {
    @Test
    fun `a Kumoa message is gone after the timeout`() = runTest {
        val host = SnackbarHostState()
        val shown = async { host.showTimed("Testi poistettu", "Kumoa", actionMillis = 4_000) }
        runCurrent()
        assertNotNull(host.currentSnackbarData)
        advanceTimeBy(3_900)
        assertNotNull(host.currentSnackbarData)
        advanceTimeBy(200)
        runCurrent()
        assertNull(host.currentSnackbarData)
        assertEquals(SnackbarResult.Dismissed, shown.await())
    }

    @Test
    fun `the accessibility timeout lengthens a Kumoa message`() = runTest {
        val before = ActionTimeouts.source
        ActionTimeouts.source = { 30_000L }
        try {
            val host = SnackbarHostState()
            val shown = async { host.showTimed("Testi poistettu", "Kumoa") }
            runCurrent()
            advanceTimeBy(29_000)
            assertNotNull(host.currentSnackbarData)
            advanceTimeBy(1_500)
            runCurrent()
            assertNull(host.currentSnackbarData)
            assertEquals(SnackbarResult.Dismissed, shown.await())
        } finally {
            ActionTimeouts.source = before
        }
    }

    @Test
    fun `a shorter system timeout never shortens the base`() {
        val before = ActionTimeouts.source
        ActionTimeouts.source = { 1_000L }
        try {
            assertEquals(ActionTimeouts.BAR_MS, ActionTimeouts.millis(ActionTimeouts.BAR_MS))
        } finally {
            ActionTimeouts.source = before
        }
    }

    @Test
    fun `a new message replaces the shown one`() = runTest {
        val host = SnackbarHostState()
        val first = async { host.showTimed("Ensimmäinen", "Kumoa") }
        runCurrent()
        launch { host.showTimed("Toinen", "Kumoa") }
        runCurrent()
        assertEquals(SnackbarResult.Dismissed, first.await())
        assertEquals("Toinen", host.currentSnackbarData?.visuals?.message)
        host.currentSnackbarData?.dismiss()
    }
}
