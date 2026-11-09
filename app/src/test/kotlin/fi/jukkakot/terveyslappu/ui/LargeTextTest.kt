package fi.jukkakot.terveyslappu.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.backup.BackupStatus
import fi.jukkakot.terveyslappu.weight.weightEntry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.Instant

/** ui rule 20: each screen keeps its actions on screen at 360 dp and 200 % text. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w360dp-h780dp", fontScale = 2.0f)
class LargeTextTest {
    @get:Rule
    val compose = createComposeRule()

    private val weights = (0..14).map {
        weightEntry("$it", Instant.now().minusSeconds(it * 43_200L + 60), null, 80.0 + it / 10.0, "me", "me")
    }

    private fun show(screen: Screen) {
        compose.setContent {
            TerveysTheme {
                MainScreen(MainUiState(access = Access.GRANTED, weights = weights, screen = screen), "0.1.1-test", MainActions())
            }
        }
    }

    @Test
    fun `home keeps the card, the weigh-in button and settings visible`() {
        show(Screen.HOME)
        compose.onNodeWithTag("weighIn").assertIsDisplayed()
        compose.onNodeWithTag("latestCard").assertIsDisplayed()
        compose.onNodeWithTag("change", useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithTag("settings").assertIsDisplayed()
    }

    @Test
    fun `Historia keeps back, add and the list visible`() {
        show(Screen.HISTORY)
        compose.onNodeWithTag("back").assertIsDisplayed()
        compose.onNodeWithTag("addWeight").assertIsDisplayed()
        compose.onNodeWithTag("weights").assertIsDisplayed()
    }

    @Test
    fun `Asetukset reaches every group`() {
        compose.setContent {
            TerveysTheme { SettingsScreen(true, ThemeMode.SYSTEM, fi.jukkakot.terveyslappu.profile.OwnerProfile(birthYear = 1990), false, BackupStatus(), "0.1.1-test", MainActions(), SnackbarHostState()) }
        }
        compose.onNodeWithTag("theme-SYSTEM").assertIsDisplayed()
        compose.onNodeWithTag("gender-NONE").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("backupEnable").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("shareLog").performScrollTo().assertIsDisplayed()
    }
}
