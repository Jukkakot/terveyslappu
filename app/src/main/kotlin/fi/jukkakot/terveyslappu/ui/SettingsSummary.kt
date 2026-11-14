package fi.jukkakot.terveyslappu.ui

import fi.jukkakot.terveyslappu.backup.BackupStatus
import fi.jukkakot.terveyslappu.listen.ListenStatus
import fi.jukkakot.terveyslappu.listen.ListenUi
import fi.jukkakot.terveyslappu.profile.Gender
import fi.jukkakot.terveyslappu.profile.OwnerProfile
import java.time.Instant
import java.time.LocalTime

/**
 * The state line of each Asetukset row (settings-list design "Rows"). Pure: the rules choose what to
 * say, the screen turns a [ListenLine] or [BackupLine] into Finnish text.
 */
object SettingsSummary {
    /** Set fields in the order height, birth year, gender joined with " · "; [none] when nothing is set. */
    fun profile(profile: OwnerProfile, genderLabel: (Gender) -> String, none: String): String {
        val parts = listOfNotNull(
            profile.heightCm?.let { "$it cm" },
            profile.birthYear?.toString(),
            profile.gender?.let(genderLabel),
        )
        return if (parts.isEmpty()) none else parts.joinToString(" · ")
    }

    /** Automatic weigh-in: off, or the one most important thing, in the order of the design. */
    fun listen(ui: ListenUi): ListenLine {
        if (!ui.settings.enabled) return ListenLine.Off
        val status = ui.status
        return when {
            ui.pending > 0 -> ListenLine.Pending(ui.pending)
            status != null && status.isBlocked() -> ListenLine.Blocked(status)
            ui.batteryRestricted -> ListenLine.Battery
            ui.notificationsOff -> ListenLine.Notifications
            else -> ListenLine.On(ui.settings.start, ui.settings.end, status)
        }
    }

    /** Backup, reusing the backup group's states; [now] decides stale. */
    fun backup(status: BackupStatus, now: Instant): BackupLine = when {
        !status.enabled -> BackupLine.Off
        status.running -> BackupLine.Running
        status.failed -> BackupLine.Failed(status.lastFailedAt!!, status.refused)
        status.lastOkAt == null -> BackupLine.Never
        status.needsAttention(now) -> BackupLine.Stale(status.lastOkAt, status.lastCount)
        else -> BackupLine.Ok(status.lastOkAt, status.lastCount)
    }
}

sealed interface ListenLine {
    val warn: Boolean get() = false

    data object Off : ListenLine
    data class Pending(val count: Int) : ListenLine { override val warn get() = true }
    data class Blocked(val status: ListenStatus) : ListenLine { override val warn get() = true }
    data object Battery : ListenLine { override val warn get() = true }
    data object Notifications : ListenLine { override val warn get() = true }
    data class On(val start: LocalTime, val end: LocalTime, val status: ListenStatus?) : ListenLine
}

sealed interface BackupLine {
    val warn: Boolean get() = false

    data object Off : BackupLine
    data object Running : BackupLine
    data object Never : BackupLine
    data class Failed(val at: Instant, val refused: Boolean) : BackupLine { override val warn get() = true }
    data class Stale(val at: Instant, val count: Int) : BackupLine { override val warn get() = true }
    data class Ok(val at: Instant, val count: Int) : BackupLine
}

/** Blocked: not listening, not resting and not done for the day. */
internal fun ListenStatus.isBlocked() = this != ListenStatus.Listening && this !is ListenStatus.Resting && this !is ListenStatus.Done
