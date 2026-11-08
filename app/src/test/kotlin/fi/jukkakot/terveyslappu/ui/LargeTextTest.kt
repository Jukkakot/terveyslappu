package fi.jukkakot.terveyslappu.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.backup.BackupStatus
import fi.jukkakot.terveyslappu.weight.weightEntry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.Instant

/** ui rule 20: the main screen keeps its actions on screen at 360 dp and 200 % text. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w360dp-h780dp", fontScale = 2.0f)
class LargeTextTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `daily actions stay visible with large text on a small phone`() {
        val rows = (1..15).map {
            WeightRow(weightEntry("$it", Instant.now().minusSeconds(it * 86_400L), null, 80.0 + it / 10.0, "me", "me"), null)
        }
        compose.setContent {
            TerveysTheme {
                MainScreen(
                    MainUiState(access = Access.GRANTED, weightText = "82,4", rows = rows),
                    "0.1.1-test",
                    MainActions(),
                    backup = BackupStatus(),
                )
            }
        }
        compose.onNodeWithTag("weighIn").assertIsDisplayed()
        compose.onNodeWithTag("save").assertIsDisplayed()
        compose.onNodeWithTag("weightField").assertIsDisplayed()
        compose.onNodeWithTag("backupAction").assertIsDisplayed()
        compose.onNodeWithTag("menu").assertIsDisplayed()
        compose.onNodeWithTag("weights").assertIsDisplayed()
    }
}
