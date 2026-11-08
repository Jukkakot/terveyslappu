package fi.jukkakot.terveyslappu.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.backup.BackupStatus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.Instant
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class BackupCardTest {
    @get:Rule
    val compose = createComposeRule()

    private val now = Instant.parse("2026-11-08T20:00:00Z")

    @Test
    fun `off offers to turn the backup on`() {
        var enabled = false
        compose.setContent { TerveysTheme { BackupCard(BackupStatus(), now, { enabled = true }, {}) } }
        compose.onNodeWithTag("backupAction").performClick()
        assertTrue(enabled)
    }

    @Test
    fun `failure offers a retry`() {
        var retried = false
        val failed = BackupStatus(uri = "content://x", lastOkAt = now.minusSeconds(7200), lastFailedAt = now.minusSeconds(60))
        compose.setContent { TerveysTheme { BackupCard(failed, now, {}, { retried = true }) } }
        compose.onNodeWithTag("backupAction").performClick()
        assertTrue(retried)
    }

    @Test
    fun `recent success needs no button`() {
        val ok = BackupStatus(uri = "content://x", lastOkAt = now.minusSeconds(60), lastCount = 12)
        compose.setContent { TerveysTheme { BackupCard(ok, now, {}, {}) } }
        compose.onNodeWithTag("backupAction").assertDoesNotExist()
    }
}
