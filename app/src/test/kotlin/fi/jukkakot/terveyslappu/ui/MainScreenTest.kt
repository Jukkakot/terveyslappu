package fi.jukkakot.terveyslappu.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.backup.BackupStatus
import fi.jukkakot.terveyslappu.chart.Period
import fi.jukkakot.terveyslappu.weight.HistoryAccess
import fi.jukkakot.terveyslappu.weight.WeightEntry
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
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val now = Instant.now()
    private fun weight(id: String, daysAgo: Long, kg: Double) =
        weightEntry(id, now.minusSeconds(daysAgo * 86_400 + 3_600), null, kg, "me", "me")

    private val weights = listOf(weight("1", 0, 82.4), weight("2", 3, 82.9), weight("3", 20, 83.5), weight("4", 25, 83.6))

    /** The screen with its own screen and dialog state, as the view model would keep it. */
    private fun show(
        start: MainUiState,
        backup: BackupStatus = BackupStatus(),
        onDelete: (WeightEntry) -> Unit = {},
        onGrantHistory: () -> Unit = {},
    ) {
        compose.setContent {
            var state by remember { mutableStateOf(start) }
            TerveysTheme {
                MainScreen(
                    state,
                    "0.1.1-test",
                    MainActions(
                        onScreen = { state = state.copy(screen = it) },
                        onPeriod = { state = state.copy(period = it) },
                        onWeightText = { state = state.copy(weightText = it) },
                        onDelete = onDelete,
                        onGrantHistory = onGrantHistory,
                    ),
                    backup = backup,
                )
            }
        }
    }

    @Test
    fun `without permission the home screen asks for it and settings stay reachable`() {
        var asked = false
        compose.setContent {
            TerveysTheme {
                MainScreen(MainUiState(access = Access.NEEDS_PERMISSION), "0.1.1-test", MainActions(onRequestPermission = { asked = true }))
            }
        }
        compose.onNodeWithTag("settings").assertExists()
        compose.onNodeWithTag("latestCard").assertDoesNotExist()
        compose.onNodeWithTag("weighIn").assertDoesNotExist()
        compose.onNode(hasText("Anna lupa") and hasClickAction()).performClick()
        assertTrue(asked)
    }

    @Test
    fun `card opens Historia, gear opens Asetukset, back returns home`() {
        show(MainUiState(access = Access.GRANTED, weights = weights))
        compose.onNodeWithTag("recent").assertExists()
        compose.onNodeWithTag("change", useUnmergedTree = true).assertExists()
        compose.onNodeWithTag("weighIn").assertExists()
        compose.onNodeWithTag("latestCard").performClick()
        compose.onNodeWithTag("chart").assertExists()
        compose.onNodeWithTag("back").performClick()
        compose.onNodeWithTag("latestCard").assertExists()
        compose.onNodeWithTag("settings").performClick()
        compose.onNodeWithTag("shareLog").assertExists()
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithTag("latestCard").assertExists()
    }

    @Test
    fun `no weights invites to weigh and shows no list`() {
        show(MainUiState(access = Access.GRANTED, weights = emptyList()))
        compose.onNodeWithTag("homeEmpty", useUnmergedTree = true).assertExists()
        compose.onNodeWithTag("recent").assertDoesNotExist()
    }

    @Test
    fun `backup notice only on trouble and it opens Asetukset`() {
        val failed = BackupStatus(uri = "content://x", lastOkAt = now.minusSeconds(7200), lastFailedAt = now.minusSeconds(60))
        show(MainUiState(access = Access.GRANTED, weights = weights), backup = failed)
        compose.onNodeWithTag("backupNotice").performClick()
        compose.onNodeWithTag("backupNow").assertExists()
    }

    @Test
    fun `a healthy backup shows nothing on the home screen`() {
        show(MainUiState(access = Access.GRANTED, weights = weights), backup = BackupStatus(uri = "content://x", lastOkAt = now.minusSeconds(60)))
        compose.onNodeWithTag("backupNotice").assertDoesNotExist()
    }

    @Test
    fun `Historia shows chart, stats and the period's weights and a chip changes the period`() {
        show(MainUiState(access = Access.GRANTED, weights = weights, period = Period.WEEK, screen = Screen.HISTORY))
        compose.onNodeWithTag("chart").assertExists()
        compose.onNodeWithTag("stats").assertExists()
        compose.onNodeWithTag("delete-2").assertExists()
        compose.onNodeWithTag("delete-3").assertDoesNotExist()
        compose.onNodeWithTag("period-30").performClick()
        compose.onNodeWithTag("period-30").assertIsSelected()
        compose.onNodeWithTag("delete-3").assertExists()
    }

    @Test
    fun `an empty period has no chart and a long one without history offers access`() {
        var granted = false
        show(
            MainUiState(access = Access.GRANTED, weights = emptyList(), period = Period.YEAR, history = HistoryAccess.NOT_GRANTED, screen = Screen.HISTORY),
            onGrantHistory = { granted = true },
        )
        compose.onNodeWithTag("chart").assertDoesNotExist()
        compose.onNodeWithTag("stats").assertDoesNotExist()
        compose.onNodeWithTag("historyGrant").performClick()
        assertTrue(granted)
    }

    @Test
    fun `long-press on a weight deletes at once`() {
        var deleted: String? = null
        show(MainUiState(access = Access.GRANTED, weights = listOf(weight("1", 0, 82.4)), screen = Screen.HISTORY), onDelete = { deleted = it.id })
        compose.onNodeWithTag("delete-1").performClick()
        assertEquals("1", deleted)
        deleted = null
        compose.onNode(hasText("82,4 kg") and hasAnyAncestor(hasTestTag("weights")))
            .performTouchInput { longClick() }
        assertEquals("1", deleted)
    }

    @Test
    fun `plus opens manual entry and the form is on no other screen`() {
        var opened = false
        compose.setContent {
            TerveysTheme {
                MainScreen(
                    MainUiState(access = Access.GRANTED, weights = weights, screen = Screen.HISTORY),
                    "0.1.1-test",
                    MainActions(onOpenEntry = { opened = true }),
                )
            }
        }
        compose.onNodeWithTag("weightField").assertDoesNotExist()
        compose.onNodeWithTag("addWeight").performClick()
        assertTrue(opened)
    }

    // The dialog window is left out: Robolectric never idles with a text field inside one.
    @Test
    fun `an impossible weight disables Tallenna`() {
        compose.setContent { TerveysTheme { ManualEntryContent(MainUiState(weightText = "7"), MainActions()) } }
        compose.onNodeWithTag("save").assertIsNotEnabled()
    }

    @Test
    fun `a valid weight can be saved and a failure is shown`() {
        var saved = false
        compose.setContent {
            TerveysTheme { ManualEntryContent(MainUiState(weightText = "82,4", saveFailed = true), MainActions(onSave = { saved = true })) }
        }
        compose.onNodeWithTag("entryFailed").assertExists()
        compose.onNodeWithTag("save").assertIsEnabled().performClick()
        assertTrue(saved)
    }
}
