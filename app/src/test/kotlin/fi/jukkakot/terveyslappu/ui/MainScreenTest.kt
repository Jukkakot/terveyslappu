package fi.jukkakot.terveyslappu.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.weight.weightEntry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.Instant
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class MainScreenTest {
    @get:Rule
    val compose = createComposeRule()

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
    fun `with permission a valid weight enables saving and the list shows sources`() {
        val time = Instant.now().minusSeconds(3_600)
        val rows = listOf(
            WeightRow(weightEntry("1", time, null, 82.4, "me", "me"), null),
            WeightRow(weightEntry("2", time.minusSeconds(86_400), null, 83.1, "other", "me"), "Garmin Connect"),
        )
        compose.setContent {
            TerveysTheme {
                MainScreen(MainUiState(access = Access.GRANTED, weightText = "82,4", rows = rows), "0.1.1-test", MainActions())
            }
        }
        compose.onNodeWithTag("save").assertIsEnabled()
        compose.onNodeWithTag("weighIn").assertExists()
        compose.onNodeWithText("Garmin Connect").assertExists()
        // Title bar and the own row's source.
        compose.onAllNodesWithText("Terveyslappu").assertCountEquals(2)
    }

    @Test
    fun `an impossible weight disables saving`() {
        compose.setContent {
            TerveysTheme {
                MainScreen(MainUiState(access = Access.GRANTED, weightText = "7", rows = emptyList()), "0.1.1-test", MainActions())
            }
        }
        compose.onNodeWithTag("save").assertIsNotEnabled()
    }
}
