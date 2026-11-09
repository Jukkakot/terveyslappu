package fi.jukkakot.terveyslappu.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.scale.WeighInState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class WeighInDialogTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `saved weight is shown`() {
        compose.setContent { TerveysTheme { WeighInDialog(WeighInState.Saved(82.5), WeighInActions()) } }
        compose.onNodeWithTag("weight", useUnmergedTree = true).assertExists()
        compose.onNodeWithTag("saved", useUnmergedTree = true).assertExists()
    }

    @Test
    fun `saved weight shows the change from the previous one`() {
        val change = fi.jukkakot.terveyslappu.scale.WeightChange(-0.3, java.time.Instant.now().minusSeconds(86_400))
        compose.setContent { TerveysTheme { WeighInDialog(WeighInState.Saved(82.5, change), WeighInActions()) } }
        compose.onNodeWithTag("change", useUnmergedTree = true).assertExists()
    }

    @Test
    fun `saved weight offers discard and done`() {
        var discarded = false
        var closed = false
        compose.setContent {
            TerveysTheme { WeighInDialog(WeighInState.Saved(82.5), WeighInActions(onDiscard = { discarded = true }, onClose = { closed = true })) }
        }
        compose.onNodeWithText("Poista").performClick()
        compose.onNodeWithText("Valmis").performClick()
        assertTrue(discarded && closed)
    }

    @Test
    fun `not found offers a retry`() {
        var retried = false
        compose.setContent {
            TerveysTheme { WeighInDialog(WeighInState.NotFound(heard = false), WeighInActions(onRetryScan = { retried = true })) }
        }
        compose.onNodeWithText("Yritä uudelleen").performClick()
        assertTrue(retried)
    }
}
