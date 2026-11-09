package fi.jukkakot.terveyslappu.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.history.HistoryParser
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.Instant
import java.time.ZoneId
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class ImportDialogTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `preview shows what was found and imports on confirm`() {
        val parsed = HistoryParser.parse("Date;Weight\n2024-03-01;82,4\n2024-03-02;82,0\n", ZoneId.of("Europe/Helsinki"), Instant.now())
        var confirmed = false
        compose.setContent { TerveysTheme { ImportDialog(ImportState.Preview(parsed), { confirmed = true }, {}) } }
        compose.onNodeWithTag("importFound").assertExists()
        compose.onNodeWithTag("importConfirm").performClick()
        assertTrue(confirmed)
    }

    @Test
    fun `empty result offers no import`() {
        val parsed = HistoryParser.parse("nothing here\n", ZoneId.of("Europe/Helsinki"), Instant.now())
        compose.setContent { TerveysTheme { ImportDialog(ImportState.Preview(parsed, fillsProfile = true), {}, {}) } }
        compose.onNodeWithTag("importProfile").assertExists()
        compose.onNodeWithTag("importConfirm").assertDoesNotExist()
    }
}
