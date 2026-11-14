package fi.jukkakot.terveyslappu.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.backup.BackupStatus
import fi.jukkakot.terveyslappu.chart.Period
import fi.jukkakot.terveyslappu.listen.ListenSettings
import fi.jukkakot.terveyslappu.listen.ListenUi
import fi.jukkakot.terveyslappu.profile.Gender
import fi.jukkakot.terveyslappu.profile.OwnerProfile
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
        profile: OwnerProfile = OwnerProfile(),
        onWeighIn: () -> Unit = {},
    ) {
        compose.setContent {
            var state by remember { mutableStateOf(start) }
            TerveysTheme {
                MainScreen(
                    state,
                    "0.1.1-test",
                    MainActions(
                        onScreen = { state = if (it == Screen.SETTINGS) state.withSettings(SettingsPage.LIST, Screen.HOME) else state.copy(screen = it) },
                        onPeriod = { state = state.copy(period = it) },
                        onHomePeriod = { state = state.copy(homeDays = it) },
                        onWeightText = { state = state.copy(weightText = it) },
                        onDelete = onDelete,
                        onGrantHistory = onGrantHistory,
                        onWeighIn = onWeighIn,
                        onBack = { state = state.back() },
                        onSettingsPage = { state = state.withSettings(it, Screen.SETTINGS) },
                        onOpenBackupSettings = { state = state.withSettings(SettingsPage.DATA, Screen.HOME) },
                        onOpenProfile = { state = state.withSettings(SettingsPage.PROFILE, state.screen) },
                        onAccept = { entry -> accepted = entry.id; state = state.copy(questioned = state.questioned - entry.id) },
                        onOpenQuestioned = { state = state.copy(screen = Screen.HISTORY, historyTarget = state.newestQuestioned?.id) },
                        onTargetShown = { state = state.copy(historyTarget = null) },
                    ),
                    backup = backup,
                    profile = profile,
                )
            }
        }
    }

    private var accepted: String? = null

    @Test
    fun `a questioned weight is listed whatever the period, accepted from its row, and the home notice opens Historia`() {
        // 61,3 kg a month old and marked "?": out of the week, still listed in Historia; the home card skips it.
        val list = weights + weight("q", 40, 61.3)
        show(MainUiState(access = Access.GRANTED, weights = list, questioned = setOf("q"), period = Period.WEEK))
        compose.onNodeWithTag("checkNotice").performClick()
        compose.onNodeWithTag("questioned-q").assertExists()
        compose.onNode(hasText("tarkista") and hasAnyAncestor(hasTestTag("questioned-q")), useUnmergedTree = true).assertExists()
        compose.onNodeWithTag("questioned-q").performClick()
        compose.onNodeWithTag("checkAccept").performClick()
        assertEquals("q", accepted)
        compose.onNodeWithTag("questioned-q").assertDoesNotExist()
    }

    @Test
    fun `a questioned latest weight is not listed on the home screen, only the notice shows`() {
        val list = weights + weight("q", 0, 61.3).copy(time = now)
        show(MainUiState(access = Access.GRANTED, weights = list, questioned = setOf("q")))
        compose.onNodeWithTag("recentQuestioned", useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithTag("checkNotice").assertExists()
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
        compose.onNodeWithTag("recent").assertDoesNotExist()
        compose.onNodeWithTag("change", useUnmergedTree = true).assertExists()
        compose.onNodeWithTag("latestCard").performClick()
        compose.onNodeWithTag("chart").assertExists()
        compose.onNodeWithTag("back").performClick()
        compose.onNodeWithTag("latestCard").assertExists()
        compose.onNodeWithTag("settings").performClick()
        compose.onNodeWithTag("settingsList").assertExists()
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithTag("latestCard").assertExists()
    }

    @Test
    fun `Ohje opens from the home help button, shows a section and back returns home`() {
        show(MainUiState(access = Access.GRANTED, weights = weights))
        compose.onNodeWithTag("help").performClick()
        compose.onNodeWithTag("helpList").assertExists()
        compose.onNodeWithText("Historia").assertExists()
        compose.onNodeWithTag("back").performClick()
        compose.onNodeWithTag("latestCard").assertExists()
    }

    @Test
    fun `an Asetukset row opens its sub-screen and back returns to the list`() {
        show(MainUiState(access = Access.GRANTED, weights = weights, screen = Screen.SETTINGS))
        for (tag in listOf("rowProfile", "rowListen", "rowData", "rowAppearance", "rowSupport")) compose.onNodeWithTag(tag).assertExists()
        compose.onNodeWithTag("rowSupport").performClick()
        compose.onNodeWithTag("shareLog").assertExists()
        compose.onNodeWithTag("back").performClick()
        compose.onNodeWithTag("settingsList").assertExists()
        compose.onNodeWithTag("back").performClick()
        compose.onNodeWithTag("latestCard").assertExists()
    }

    @Test
    fun `home period label switches the chart and does not open Historia`() {
        show(MainUiState(access = Access.GRANTED, weights = weights))
        compose.onNodeWithTag("sparkRange", useUnmergedTree = true).assertTextEquals("82,9 → 82,4 kg")
        compose.onNodeWithTag("homePeriod").performClick()
        compose.onNodeWithTag("homePeriod-30").performClick()
        compose.onNodeWithTag("latestCard").assertExists()
        compose.onNodeWithTag("chart").assertDoesNotExist()
        compose.onNodeWithTag("sparkRange", useUnmergedTree = true).assertTextEquals("83,6 → 82,4 kg")
    }

    @Test
    fun `no weights invites to weigh and shows no list`() {
        show(MainUiState(access = Access.GRANTED, weights = emptyList()))
        compose.onNodeWithTag("homeEmpty", useUnmergedTree = true).assertExists()
        compose.onNodeWithTag("recent").assertDoesNotExist()
    }

    @Test
    fun `Punnitse in the weight card opens the weigh-in, not Historia`() {
        var weighed = 0
        show(MainUiState(access = Access.GRANTED, weights = weights), onWeighIn = { weighed++ })
        compose.onNodeWithTag("weighIn").performClick()
        assertEquals(1, weighed)
        compose.onNodeWithTag("chart").assertDoesNotExist()
        compose.onNodeWithTag("latestCard").assertExists()
    }

    @Test
    fun `no weights still offers Punnitse`() {
        show(MainUiState(access = Access.GRANTED, weights = emptyList()))
        compose.onNodeWithTag("weighIn").assertExists()
    }

    @Test
    fun `front page shows only the weight and food cards, nothing of automatic weigh-in or BMI`() {
        compose.setContent {
            TerveysTheme {
                MainScreen(
                    MainUiState(access = Access.GRANTED, weights = weights),
                    "0.1.1-test",
                    MainActions(),
                    listen = ListenUi(settings = ListenSettings(enabled = true)),
                    profile = OwnerProfile(birthYear = 1990, heightCm = 180, gender = Gender.MALE),
                )
            }
        }
        compose.onNodeWithTag("latestCard").assertExists()
        compose.onNodeWithTag("foodCard").assertExists()
        compose.onNodeWithTag("listenCard").assertDoesNotExist()
        compose.onNodeWithTag("derived").assertDoesNotExist()
        compose.onNodeWithTag("derivedBmi", useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithTag("derivedReason").assertDoesNotExist()
    }

    @Test
    fun `backup notice only on trouble and it opens Asetukset`() {
        val failed = BackupStatus(uri = "content://x", lastOkAt = now.minusSeconds(7200), lastFailedAt = now.minusSeconds(60))
        show(MainUiState(access = Access.GRANTED, weights = weights), backup = failed)
        compose.onNodeWithTag("backupNotice").performClick()
        compose.onNodeWithTag("backupNow").assertExists()
        compose.onNodeWithTag("back").performClick()
        compose.onNodeWithTag("latestCard").assertExists()
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
        // Two weights in the week: the newer row shows its change, the older none; Muutos and Tahti get an arrow down.
        compose.onNodeWithTag("rowChange-1", useUnmergedTree = true).assertExists()
        compose.onNodeWithTag("rowChange-2", useUnmergedTree = true).assertDoesNotExist()
        assertEquals(2, compose.onAllNodes(hasTestTag("arrowDown") and hasAnyAncestor(hasTestTag("stats")), useUnmergedTree = true).fetchSemanticsNodes().size)
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
        compose.onNode(hasText("82,4 kg") and hasAnyAncestor(hasTestTag("weights")) and !hasAnyAncestor(hasTestTag("stats")))
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

    @Test
    fun `BMI tab switches the chart and stats, opening on Paino`() {
        val man = OwnerProfile(birthYear = 1990, heightCm = 180, gender = Gender.MALE)
        show(MainUiState(access = Access.GRANTED, weights = weights, period = Period.MONTH, screen = Screen.HISTORY), profile = man)
        compose.onNodeWithTag("page-WEIGHT").assertExists()
        compose.onNodeWithTag("tab-BMI").performClick()
        compose.onNodeWithTag("page-BMI").assertExists()
        compose.onNode(hasText("Viimeisin") and hasAnyAncestor(hasTestTag("stats"))).assertExists()
        compose.onNodeWithTag("chart").assertExists()
        compose.onNodeWithTag("delete-3").assertExists()
    }

    @Test
    fun `BMI tab without a height shows the reason and opens Omat tiedot`() {
        show(MainUiState(access = Access.GRANTED, weights = weights, screen = Screen.HISTORY))
        compose.onNodeWithTag("tab-BMI").performClick()
        compose.onNodeWithTag("quantityReason").assertExists()
        compose.onNodeWithTag("openProfile").performClick()
        compose.onNodeWithTag("height").assertExists()
        compose.onNodeWithTag("back").performClick()
        compose.onNodeWithTag("chart").assertExists()
    }

    @Test
    fun `a long period starts with closed years and a year opens on tap`() {
        val zone = java.time.ZoneId.systemDefault()
        val many = (2023..2025).flatMap { y ->
            (1..12).map { m -> weightEntry("$y-$m", java.time.LocalDate.of(y, m, 10).atTime(7, 5).atZone(zone).toInstant(), null, 85.0 + m / 10.0, "me", "me") }
        }
        show(MainUiState(access = Access.GRANTED, weights = many.reversed(), period = Period.All, screen = Screen.HISTORY))
        compose.onNodeWithTag("weights").performScrollToNode(hasTestTag("group-y2023"))
        compose.onNodeWithTag("delete-2023-6").assertDoesNotExist()
        compose.onNodeWithTag("group-m2023-06").assertDoesNotExist()
        compose.onNodeWithTag("group-y2023").performClick()
        compose.onNodeWithTag("weights").performScrollToNode(hasTestTag("group-m2023-06"))
        compose.onNodeWithTag("group-m2023-06").assertExists()
    }
}
