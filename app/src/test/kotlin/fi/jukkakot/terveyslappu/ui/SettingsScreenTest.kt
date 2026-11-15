package fi.jukkakot.terveyslappu.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import fi.jukkakot.terveyslappu.listen.ListenSettings
import fi.jukkakot.terveyslappu.listen.ListenStatus
import fi.jukkakot.terveyslappu.listen.ListenUi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.TerveysApp
import fi.jukkakot.terveyslappu.backup.BackupStatus
import fi.jukkakot.terveyslappu.data.DataFormat
import fi.jukkakot.terveyslappu.profile.Gender
import fi.jukkakot.terveyslappu.profile.OwnerProfile
import fi.jukkakot.terveyslappu.steps.StepsAccess
import fi.jukkakot.terveyslappu.steps.StepsChoice
import fi.jukkakot.terveyslappu.steps.StepsSourceRow
import fi.jukkakot.terveyslappu.steps.StepsSourceRules
import fi.jukkakot.terveyslappu.steps.StepsSourcesUi
import fi.jukkakot.terveyslappu.steps.StepsUiState
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.assertTextContains
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w411dp-h891dp")
class SettingsScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private val now = Instant.now()

    private fun show(
        backup: BackupStatus = BackupStatus(),
        granted: Boolean = true,
        actions: MainActions = MainActions(),
        profile: OwnerProfile = OwnerProfile(),
        heightInHc: Boolean = true,
        page: SettingsPage = SettingsPage.LIST,
        steps: StepsUiState = StepsUiState(),
        sources: StepsSourcesUi = StepsSourcesUi(),
        stepsActions: StepsActions = StepsActions(),
    ) {
        compose.setContent {
            TerveysTheme {
                SettingsScreen(page, granted, ThemeMode.SYSTEM, profile, heightInHc, backup, "0.1.1-test", actions, SnackbarHostState(), steps = steps, stepsSources = sources, stepsActions = stepsActions)
            }
        }
    }

    private val garmin = "com.garmin.android.apps.connectmobile"

    @Test
    fun `the list has seven rows in order and Askeleet reads the stored pick`() {
        val stored = StepsSourceRules.resolve(StepsChoice.Auto, garmin, null, checkedToday = false)
        show(steps = StepsUiState(access = StepsAccess.GRANTED), sources = StepsSourcesUi(source = stored, sourceName = "Garmin Connect"))
        val tops = listOf("rowProfile", "rowListen", "rowData", "rowFood", "rowSteps", "rowAppearance", "rowSupport").map {
            compose.onNodeWithTag(it).assertExists().getBoundsInRoot().top
        }
        assertEquals(tops.sorted(), tops)
        compose.onNodeWithText("Automaattinen: Garmin Connect").assertExists()
    }

    @Test
    fun `Askeleet row says Ei lupaa without the permission`() {
        var access by mutableStateOf(StepsAccess.NEEDS)
        compose.setContent {
            TerveysTheme { SettingsScreen(SettingsPage.LIST, true, ThemeMode.SYSTEM, OwnerProfile(), true, BackupStatus(), "0.1.1-test", MainActions(), SnackbarHostState(), steps = StepsUiState(access = access)) }
        }
        compose.onNodeWithText("Ei lupaa").assertExists()
        access = StepsAccess.DENIED
        compose.onNodeWithText("Ei lupaa").assertExists()
        access = StepsAccess.CHECKING
        compose.onNodeWithText("Ei lupaa").assertDoesNotExist()
    }

    @Test
    fun `the source sub-screen lists the apps and a tap picks one`() {
        var picked: StepsChoice? = null
        var shown = 0
        val rows = listOf(
            StepsSourceRow(garmin, "Garmin Connect", 150_000, 5_000, null),
            StepsSourceRow("phone", "Puhelin", 120_000, 10_000, null),
        )
        show(
            page = SettingsPage.STEPS,
            steps = StepsUiState(access = StepsAccess.GRANTED),
            sources = StepsSourcesUi(rows = rows, checked = true, pickName = "Garmin Connect"),
            stepsActions = StepsActions(onChoice = { picked = it }, onSourceShown = { shown++ }),
        )
        assertEquals(1, shown)
        compose.onNodeWithTag("stepsSource-auto").assertIsSelected()
        compose.onNodeWithText("Nyt: Garmin Connect").assertExists()
        assertEquals(2, compose.onAllNodesWithText("askelta/pv", substring = true).fetchSemanticsNodes().size)
        assertTrue(compose.onNodeWithTag("stepsSource-$garmin").getBoundsInRoot().top < compose.onNodeWithTag("stepsSource-phone").getBoundsInRoot().top)
        compose.onNodeWithTag("stepsSource-phone").performClick()
        assertEquals(StepsChoice.App("phone"), picked)
        compose.onNodeWithTag("stepsSource-all").assertExists()
    }

    @Test
    fun `the source sub-screen without the permission gives the reason`() {
        var grants = 0
        show(page = SettingsPage.STEPS, steps = StepsUiState(access = StepsAccess.NEEDS), stepsActions = StepsActions(onGrant = { grants++ }))
        compose.onNodeWithTag("stepsReason").assertExists()
        compose.onNodeWithTag("stepsSourceList").assertDoesNotExist()
        assertEquals(0, grants)
        compose.onNodeWithTag("stepsGrant").performClick()
        assertEquals(1, grants)
    }

    @Test
    fun `the Garmin history row shows the range and deletes, also without the permission`() {
        var deleted = 0
        val summary = fi.jukkakot.terveyslappu.steps.GarminSummary(java.time.LocalDate.of(2023, 7, 18), java.time.LocalDate.of(2026, 11, 8), 1206)
        show(
            page = SettingsPage.STEPS,
            steps = StepsUiState(access = StepsAccess.NEEDS),
            sources = StepsSourcesUi(garmin = summary),
            stepsActions = StepsActions(onGarminDelete = { deleted++ }),
        )
        compose.onNodeWithTag("garminHistoryLine").assertTextContains("18.7.2023 – 8.11.2026", substring = true)
        compose.onNodeWithTag("garminNoKcal").assertDoesNotExist()
        compose.onNodeWithTag("garminDelete").performClick()
        assertEquals(1, deleted)
    }

    @Test
    fun `a history without kcal says to import the ZIP again`() {
        val summary = fi.jukkakot.terveyslappu.steps.GarminSummary(java.time.LocalDate.of(2023, 7, 18), java.time.LocalDate.of(2026, 11, 8), 1206, kcal = false)
        show(page = SettingsPage.STEPS, steps = StepsUiState(access = StepsAccess.GRANTED), sources = StepsSourcesUi(garmin = summary))
        compose.onNodeWithTag("garminNoKcal").assertExists()
    }

    @Test
    fun `without an import the Garmin row gives the hint and no Poista`() {
        show(page = SettingsPage.STEPS, steps = StepsUiState(access = StepsAccess.GRANTED))
        compose.onNodeWithTag("garminHistoryLine").assertExists()
        compose.onNodeWithTag("garminDelete").assertDoesNotExist()
    }

    @Test
    fun `owner details keep a valid height and refuse an implausible one`() {
        var kept: OwnerProfile? = null
        show(granted = false, actions = MainActions(onProfile = { kept = it }), page = SettingsPage.PROFILE)
        compose.onNodeWithTag("height").performTextInput("25")
        compose.onNodeWithTag("height").performImeAction()
        assertEquals(null, kept)
        compose.onNodeWithTag("height").performTextClearance()
        compose.onNodeWithTag("height").performTextInput("180")
        compose.onNodeWithTag("height").performImeAction()
        assertEquals(OwnerProfile(heightCm = 180), kept)
        compose.onNodeWithTag("gender-FEMALE").performClick()
        assertEquals(OwnerProfile(gender = Gender.FEMALE), kept)
    }

    @Test
    fun `a saved height that is not in Health Connect says so`() {
        show(granted = false, profile = OwnerProfile(birthYear = 1990, heightCm = 180), heightInHc = false, page = SettingsPage.PROFILE)
        compose.onNodeWithText("Ei tallennettu Health Connectiin", substring = true).assertExists()
    }

    @Test
    fun `Tiedot ja varmuuskopio page groups import and export`() {
        var imported = false
        var exported: DataFormat? = null
        show(actions = MainActions(onImportHistory = { imported = true }, onExport = { exported = it }), page = SettingsPage.DATA)
        compose.onNodeWithTag("import").performClick()
        compose.onNodeWithTag("export").performClick()
        compose.onNodeWithTag("format-JSON").performClick()
        assertTrue(imported)
        assertEquals(DataFormat.JSON, exported)
    }

    @Test
    fun `Tuki page has the log actions and the version`() {
        show(page = SettingsPage.SUPPORT)
        compose.onNodeWithTag("shareLog").assertExists()
        compose.onNodeWithTag("copyLog").assertExists()
        compose.onNodeWithText("v0.1.1-test").assertExists()
    }

    @Test
    fun `without Health Connect the data row is missing and its page falls back to the list`() {
        show(granted = false)
        compose.onNodeWithTag("rowData").assertDoesNotExist()
        compose.onNodeWithTag("rowSupport").assertExists()
        compose.onNodeWithTag("import").assertDoesNotExist()
    }

    @Test
    fun `the data page without Health Connect shows the list`() {
        show(granted = false, page = SettingsPage.DATA)
        compose.onNodeWithTag("settingsList").assertExists()
        compose.onNodeWithTag("backupStatus").assertDoesNotExist()
    }

    @Test
    fun `backup off offers to turn it on`() {
        var enabled = false
        show(actions = MainActions(onBackupEnable = { enabled = true }), page = SettingsPage.DATA)
        compose.onNodeWithTag("backupEnable").performScrollTo().performClick()
        assertTrue(enabled)
        compose.onNodeWithTag("backupDisable").assertDoesNotExist()
    }

    @Test
    fun `failed backup offers a retry, a new file and turning off`() {
        var retried = false
        var off = false
        val failed = BackupStatus(uri = "content://x", lastOkAt = now.minusSeconds(7200), lastFailedAt = now.minusSeconds(60))
        show(failed, actions = MainActions(onBackupNow = { retried = true }, onBackupDisable = { off = true }), page = SettingsPage.DATA)
        compose.onNodeWithTag("backupNow").performScrollTo().performClick()
        compose.onNodeWithTag("backupChange").assertExists()
        compose.onNodeWithTag("backupDisable").performScrollTo().performClick()
        assertTrue(retried && off)
    }

    @Test
    fun `automatic weigh-in shows the limit, warnings and the pending queue`() {
        var limit = 0.0
        val ui = ListenUi(
            settings = ListenSettings(enabled = true),
            hasScale = true,
            status = ListenStatus.HealthMissing,
            notificationsOff = true,
            pending = 1,
        )
        compose.setContent {
            TerveysTheme {
                SettingsScreen(SettingsPage.LISTEN, true, ThemeMode.SYSTEM, OwnerProfile(), true, BackupStatus(), "0.1.1-test", MainActions(onListenSettings = { limit = it.limit }), SnackbarHostState(), listen = ui)
            }
        }
        compose.onNodeWithTag("listenPage").assertExists()
        compose.onNodeWithTag("listenStatus-fix").performScrollTo().assertExists()
        compose.onNodeWithTag("listenNotifications-fix").performScrollTo().assertExists()
        compose.onNodeWithTag("listenPending").performScrollTo().assertExists()
        compose.onNodeWithTag("listenLimitMore").performScrollTo().performClick()
        assertEquals(5.5, limit)
    }

    @Test
    fun `listen trial needs a known scale, then shows its status and fix`() {
        var enabled: Boolean? = null
        var fix: ListenFix? = null
        var ui by mutableStateOf(ListenUi())
        val actions = MainActions(onListenEnabled = { enabled = it }, onListenFix = { fix = it })
        compose.setContent {
            TerveysTheme { SettingsScreen(SettingsPage.LISTEN, false, ThemeMode.SYSTEM, OwnerProfile(), true, BackupStatus(), "0.1.1-test", actions, SnackbarHostState(), listen = ui) }
        }
        compose.onNodeWithTag("listenSwitch").performScrollTo().assertIsNotEnabled()
        ui = ListenUi(ListenSettings(enabled = true), hasScale = true, status = ListenStatus.BluetoothOff, batteryRestricted = true)
        compose.onNodeWithTag("listenSwitch").performScrollTo().assertIsEnabled().performClick()
        assertEquals(false, enabled)
        compose.onNodeWithTag("listenStatus-fix").performScrollTo().performClick()
        assertEquals(ListenFix.BLUETOOTH, fix)
        compose.onNodeWithTag("listenBattery").assertExists()
    }

    @Test
    fun `picking Tumma turns the app dark at once`() {
        val app = ApplicationProvider.getApplicationContext<TerveysApp>()
        var background = Color.Unspecified
        compose.setContent {
            TerveysTheme {
                background = MaterialTheme.colorScheme.background
                SettingsScreen(SettingsPage.APPEARANCE, true, app.appearance.mode.collectAsState().value, OwnerProfile(), true, BackupStatus(), "0.1.1-test", MainActions(onTheme = app.appearance::set), SnackbarHostState())
            }
        }
        compose.onNodeWithTag("theme-LIGHT").performClick()
        compose.waitForIdle()
        assertTrue(background.luminance() > 0.5f)
        compose.onNodeWithTag("theme-DARK").performClick()
        compose.waitForIdle()
        assertTrue(background.luminance() < 0.5f)
        assertEquals(ThemeMode.DARK, app.appearance.mode.value)
        app.appearance.set(ThemeMode.SYSTEM)
    }
}
