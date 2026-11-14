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
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
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
    ) {
        compose.setContent {
            TerveysTheme { SettingsScreen(granted, ThemeMode.SYSTEM, profile, heightInHc, backup, "0.1.1-test", actions, SnackbarHostState()) }
        }
    }

    @Test
    fun `owner details keep a valid height and refuse an implausible one`() {
        var kept: OwnerProfile? = null
        show(granted = false, actions = MainActions(onProfile = { kept = it }))
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
        show(granted = false, profile = OwnerProfile(birthYear = 1990, heightCm = 180), heightInHc = false)
        compose.onNodeWithText("Ei tallennettu Health Connectiin", substring = true).assertExists()
    }

    @Test
    fun `groups import, export, backup and log with the version`() {
        var imported = false
        var exported: DataFormat? = null
        show(actions = MainActions(onImportHistory = { imported = true }, onExport = { exported = it }))
        compose.onNodeWithTag("import").performClick()
        compose.onNodeWithTag("export").performClick()
        compose.onNodeWithTag("format-JSON").performClick()
        assertTrue(imported)
        assertEquals(DataFormat.JSON, exported)
        compose.onNodeWithTag("shareLog").performScrollTo()
        compose.onNodeWithTag("copyLog").assertExists()
        compose.onNodeWithText("v0.1.1-test").assertExists()
    }

    @Test
    fun `without Health Connect only the theme and the log are offered`() {
        show(granted = false)
        compose.onNodeWithTag("import").assertDoesNotExist()
        compose.onNodeWithTag("backupStatus").assertDoesNotExist()
        compose.onNodeWithTag("shareLog").assertExists()
    }

    @Test
    fun `backup off offers to turn it on`() {
        var enabled = false
        show(actions = MainActions(onBackupEnable = { enabled = true }))
        compose.onNodeWithTag("backupEnable").performScrollTo().performClick()
        assertTrue(enabled)
        compose.onNodeWithTag("backupDisable").assertDoesNotExist()
    }

    @Test
    fun `failed backup offers a retry, a new file and turning off`() {
        var retried = false
        var off = false
        val failed = BackupStatus(uri = "content://x", lastOkAt = now.minusSeconds(7200), lastFailedAt = now.minusSeconds(60))
        show(failed, actions = MainActions(onBackupNow = { retried = true }, onBackupDisable = { off = true }))
        compose.onNodeWithTag("backupNow").performScrollTo().performClick()
        compose.onNodeWithTag("backupChange").assertExists()
        compose.onNodeWithTag("backupDisable").performScrollTo().performClick()
        assertTrue(retried && off)
    }

    @Test
    fun `listen trial needs a known scale, then shows its status and fix`() {
        var enabled: Boolean? = null
        var fix: ListenFix? = null
        var ui by mutableStateOf(ListenUi())
        val actions = MainActions(onListenEnabled = { enabled = it }, onListenFix = { fix = it })
        compose.setContent {
            TerveysTheme { SettingsScreen(false, ThemeMode.SYSTEM, OwnerProfile(), true, BackupStatus(), "0.1.1-test", actions, SnackbarHostState(), listen = ui) }
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
                SettingsScreen(true, app.appearance.mode.collectAsState().value, OwnerProfile(), true, BackupStatus(), "0.1.1-test", MainActions(onTheme = app.appearance::set), SnackbarHostState())
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
