package fi.jukkakot.terveyslappu.ui

import fi.jukkakot.terveyslappu.backup.BackupStatus
import fi.jukkakot.terveyslappu.listen.ListenSettings
import fi.jukkakot.terveyslappu.listen.ListenStatus
import fi.jukkakot.terveyslappu.listen.ListenUi
import fi.jukkakot.terveyslappu.profile.Gender
import fi.jukkakot.terveyslappu.profile.OwnerProfile
import org.junit.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SettingsSummaryTest {
    private val now = Instant.parse("2026-11-14T10:00:00Z")
    private fun label(g: Gender) = if (g == Gender.MALE) "Mies" else "Nainen"

    @Test
    fun `profile line lists the set fields in order and skips the others`() {
        assertEquals("180 cm · 1990 · Mies", SettingsSummary.profile(OwnerProfile(heightCm = 180, birthYear = 1990, gender = Gender.MALE), ::label, "none"))
        assertEquals("180 cm · Mies", SettingsSummary.profile(OwnerProfile(heightCm = 180, gender = Gender.MALE), ::label, "none"))
        assertEquals("none", SettingsSummary.profile(OwnerProfile(), ::label, "none"))
    }

    private val on = ListenSettings(enabled = true)

    @Test
    fun `listen line is off when disabled`() {
        assertEquals(ListenLine.Off, SettingsSummary.listen(ListenUi(pending = 2)))
    }

    @Test
    fun `listen line picks pending, then blocked, then battery, then notifications`() {
        val all = ListenUi(settings = on, status = ListenStatus.BluetoothOff, batteryRestricted = true, notificationsOff = true, pending = 2)
        assertEquals(ListenLine.Pending(2), SettingsSummary.listen(all))
        assertEquals(ListenLine.Blocked(ListenStatus.BluetoothOff), SettingsSummary.listen(all.copy(pending = 0)))
        assertEquals(ListenLine.Battery, SettingsSummary.listen(all.copy(pending = 0, status = ListenStatus.Listening)))
        assertEquals(ListenLine.Notifications, SettingsSummary.listen(all.copy(pending = 0, status = ListenStatus.Listening, batteryRestricted = false)))
        val ok = SettingsSummary.listen(ListenUi(settings = on, status = ListenStatus.Listening))
        assertIs<ListenLine.On>(ok)
        assertFalse(ok.warn)
    }

    @Test
    fun `backup line follows the backup group's states`() {
        val t = now.minusSeconds(3600)
        val enabled = BackupStatus(uri = "content://x")
        assertEquals(BackupLine.Off, SettingsSummary.backup(BackupStatus(), now))
        assertEquals(BackupLine.Running, SettingsSummary.backup(enabled.copy(running = true), now))
        assertEquals(BackupLine.Never, SettingsSummary.backup(enabled, now))
        assertEquals(BackupLine.Ok(t, 5), SettingsSummary.backup(enabled.copy(lastOkAt = t, lastCount = 5), now))
        assertTrue(SettingsSummary.backup(enabled.copy(lastOkAt = now.minusSeconds(4 * 86_400L), lastCount = 5), now).warn)
        assertTrue(SettingsSummary.backup(enabled.copy(lastFailedAt = t), now).warn)
        assertFalse(SettingsSummary.backup(BackupStatus(), now).warn)
    }
}
