package fi.jukkakot.terveyslappu.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.food.AiItem
import fi.jukkakot.terveyslappu.food.AiProduct
import fi.jukkakot.terveyslappu.food.ClipOffer
import fi.jukkakot.terveyslappu.food.Amount
import fi.jukkakot.terveyslappu.food.AmountUnit
import fi.jukkakot.terveyslappu.food.Energy
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDate
import kotlin.test.assertEquals

/** Smoke test of the AI import preview (food-ai-import), made-up foods only (NFR-1). */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w411dp-h891dp")
class AiImportPreviewTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `shows items and products, removes one and adds`() {
        var added = 0
        var cancelled = 0
        compose.setContent {
            var preview by remember {
                mutableStateOf(
                    AiPreview(
                        listOf(AiItem("Kaurapuuro", Amount(300.0, AmountUnit.G), 210.0, false), AiItem("Kahvi", null, null, false)),
                        listOf(AiProduct("Lohikeitto", Energy.perPiece(380.0), null, false)),
                        2,
                        LocalDate.now(),
                    ),
                )
            }
            TerveysTheme {
                AiImportPreview(preview, FoodActions(onAiEdit = { preview = it }, onAiAdd = { added++ }, onAiCancel = { cancelled++ }))
            }
        }
        compose.onNodeWithTag("aiItem-0").assertExists()
        compose.onNodeWithTag("aiItem-1").assertExists()
        compose.onNodeWithTag("aiProduct-0").assertExists()
        compose.onNodeWithTag("aiSkipped").assertExists()
        compose.onNodeWithTag("aiItem-0-remove").performClick()
        compose.waitForIdle()
        compose.onNodeWithTag("aiItem-1").assertDoesNotExist()
        compose.onNodeWithTag("aiAdd").performClick()
        assertEquals(1, added)
        assertEquals(0, cancelled)
    }

    @Test
    fun `clip offer line opens or skips`() {
        var opened = 0
        var skipped = 0
        compose.setContent {
            TerveysTheme { ClipOfferLine(ClipOffer("x", 6, false), FoodActions(onClipOpen = { opened++ }, onClipSkip = { skipped++ })) }
        }
        compose.onNodeWithTag("clipOffer").assertExists()
        compose.onNodeWithTag("clipOpen").performClick()
        compose.onNodeWithTag("clipSkip").performClick()
        assertEquals(1, opened)
        assertEquals(1, skipped)
    }
}
