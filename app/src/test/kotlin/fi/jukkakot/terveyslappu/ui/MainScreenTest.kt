package fi.jukkakot.terveyslappu.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.chart.Period
import fi.jukkakot.terveyslappu.weight.HistoryAccess
import fi.jukkakot.terveyslappu.weight.weightEntry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w411dp-h891dp")
class MainScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private val now = Instant.now()
    private fun weight(id: String, daysAgo: Long, kg: Double) =
        weightEntry(id, now.minusSeconds(daysAgo * 86_400 + 3_600), null, kg, "me", "me")

    @Test
    fun `without permission the screen asks for it`() {
        var asked = false
        compose.setContent {
            TerveysTheme {
                MainScreen(MainUiState(access = Access.NEEDS_PERMISSION), "0.1.1-test", MainActions(onRequestPermission = { asked = true }))
            }
        }
        compose.onNodeWithText("Anna lupa").performClick()
        assertTrue(asked)
        compose.onNodeWithTag("weightField").assertDoesNotExist()
    }

    @Test
    fun `chart, stats and the period's weights are shown and a chip changes the period`() {
        val weights = listOf(weight("1", 0, 82.4), weight("2", 3, 82.9), weight("3", 20, 83.5))
        var period by mutableStateOf<Period>(Period.WEEK)
        compose.setContent {
            TerveysTheme {
                MainScreen(
                    MainUiState(access = Access.GRANTED, weightText = "82,4", weights = weights, period = period),
                    "0.1.1-test",
                    MainActions(onPeriod = { period = it }),
                )
            }
        }
        compose.onNodeWithTag("save").assertIsEnabled()
        compose.onNodeWithTag("chart").assertExists()
        compose.onNodeWithTag("stats").assertExists()
        compose.onNodeWithTag("delete-2").assertExists()
        compose.onNodeWithTag("delete-3").assertDoesNotExist()
        compose.onNodeWithTag("period-30").performClick()
        compose.onNodeWithTag("period-30").assertIsSelected()
        compose.onNodeWithTag("delete-3").assertExists()
        compose.onNodeWithTag("menu").performClick()
        compose.onNodeWithTag("menuImport").assertExists()
        compose.onNodeWithTag("menuExport").assertExists()
    }

    @Test
    fun `an empty period has no chart and a long one without history offers access`() {
        var granted = false
        compose.setContent {
            TerveysTheme {
                MainScreen(
                    MainUiState(access = Access.GRANTED, weights = emptyList(), period = Period.YEAR, history = HistoryAccess.NOT_GRANTED),
                    "0.1.1-test",
                    MainActions(onGrantHistory = { granted = true }),
                )
            }
        }
        compose.onNodeWithTag("chart").assertDoesNotExist()
        compose.onNodeWithTag("stats").assertDoesNotExist()
        compose.onNodeWithTag("historyGrant").performClick()
        assertTrue(granted)
    }

    @Test
    fun `long-press on a weight deletes at once`() {
        var deleted: String? = null
        compose.setContent {
            TerveysTheme {
                MainScreen(
                    MainUiState(access = Access.GRANTED, weights = listOf(weight("1", 0, 82.4))),
                    "0.1.1-test",
                    MainActions(onDelete = { deleted = it.id }),
                )
            }
        }
        compose.onNodeWithTag("delete-1").performClick()
        assertEquals("1", deleted)
        deleted = null
        compose.onNodeWithText("82,4 kg").performTouchInput { longClick() }
        assertEquals("1", deleted)
    }

    @Test
    fun `an impossible weight disables saving`() {
        compose.setContent {
            TerveysTheme {
                MainScreen(MainUiState(access = Access.GRANTED, weightText = "7", weights = emptyList()), "0.1.1-test", MainActions())
            }
        }
        compose.onNodeWithTag("save").assertIsNotEnabled()
    }
}
