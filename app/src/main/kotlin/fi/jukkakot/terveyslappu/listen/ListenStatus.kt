package fi.jukkakot.terveyslappu.listen

import java.time.LocalTime

/** The automatic weigh-in's one status line (background-weigh-in "Trial status"). */
sealed interface ListenStatus {
    data object NoScale : ListenStatus
    data object PermissionMissing : ListenStatus
    /** Health Connect weight write access is missing: no scan runs. */
    data object HealthMissing : ListenStatus
    data object BluetoothOff : ListenStatus
    /** Turned on, but nothing is armed: the system stopped the app by force; opening it re-arms. */
    data object Stopped : ListenStatus
    data object PowerSavePause : ListenStatus
    data object Listening : ListenStatus
    data class Resting(val startsAt: LocalTime) : ListenStatus
}

/** What the status is chosen from. */
data class ListenFacts(
    val enabled: Boolean,
    val hasScale: Boolean,
    val permission: Boolean,
    val healthWrite: Boolean = true,
    val bluetoothOn: Boolean,
    val powerSave: Boolean,
    val listenInPowerSave: Boolean,
    val armed: Boolean,
    val inside: Boolean,
    val nextStart: LocalTime,
)

object ListenStatusRule {
    /**
     * The first status that applies, or null while the trial is off (the section then shows no
     * status). Order: blocked reasons, power saving pause, listening, resting.
     */
    fun status(f: ListenFacts): ListenStatus? = when {
        !f.enabled -> null
        !f.hasScale -> ListenStatus.NoScale
        !f.permission -> ListenStatus.PermissionMissing
        !f.healthWrite -> ListenStatus.HealthMissing
        !f.bluetoothOn -> ListenStatus.BluetoothOff
        !f.armed -> ListenStatus.Stopped
        f.powerSave && !f.listenInPowerSave -> ListenStatus.PowerSavePause
        f.inside -> ListenStatus.Listening
        else -> ListenStatus.Resting(f.nextStart)
    }
}
