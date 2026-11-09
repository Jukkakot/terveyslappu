package fi.jukkakot.terveyslappu.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.chart.Period
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDate
import kotlin.test.assertEquals

/** Made-up weights only (NFR-1). */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w360dp-h780dp")
class RangeCalendarTest {
    @get:Rule
    val compose = createComposeRule()

    private val today = LocalDate.of(2026, 11, 9)
    private val days = mapOf(LocalDate.of(2026, 11, 3) to 82.4, LocalDate.of(2026, 11, 5) to 82.1)

    private fun show(historyMissing: Boolean = false, onPick: (Period.Custom) -> Unit = {}) {
        compose.setContent {
            TerveysTheme { RangeCalendarContent(null, days, historyMissing, {}, onPick, {}, today) }
        }
    }

    @Test
    fun `weighed days show their weight and a picked range is applied`() {
        var picked: Period.Custom? = null
        show(onPick = { picked = it })
        compose.onNodeWithTag("dayKg-2026-11-03", useUnmergedTree = true).assertTextEquals("82,4")
        compose.onNodeWithTag("dayKg-2026-11-04", useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithTag("day-2026-11-04").performClick()
        compose.onNodeWithTag("day-2026-11-09").performClick()
        compose.onNodeWithTag("calendarOk").performClick()
        assertEquals(Period.Custom(LocalDate.of(2026, 11, 4), today), picked)
    }

    @Test
    fun `the history notice shows only without access`() {
        show(historyMissing = true)
        compose.onNodeWithTag("historyGrant").assertIsDisplayed()
    }

    @Test
    fun `no history notice with access`() {
        show()
        compose.onNodeWithTag("historyGrant").assertDoesNotExist()
    }
}
