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
    /** The window's weight is saved: from that save until the next window start, also outside the window. */
    data class Done(val nextStart: LocalTime, val tomorrow: Boolean) : ListenStatus
    /** Inside the window, waiting for the scale; [until] is the window end, [tomorrow] when it is on the next date. */
    data class Listening(val until: LocalTime, val tomorrow: Boolean = false) : ListenStatus
    data class Resting(val startsAt: LocalTime, val tomorrow: Boolean = false) : ListenStatus
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
    /** A weight not marked "?" of this app is saved in the window whose next start has not come yet. */
    val done: Boolean = false,
    /** [nextStart] is on tomorrow's date (the status text says "huomenna"). */
    val nextStartTomorrow: Boolean = false,
    /** The current window's end and whether it is on tomorrow's date (the Listening line). */
    val windowEnd: LocalTime = LocalTime.of(10, 0),
    val windowEndTomorrow: Boolean = false,
)

object ListenStatusRule {
    /**
     * The first status that applies, or null while the trial is off (the section then shows no
     * status). Order: done (until the next window start, so a blocker shows again from then;
     * everyday-feedback design 9), blocked reasons, power saving pause, listening, resting.
     */
    fun status(f: ListenFacts): ListenStatus? = when {
        !f.enabled -> null
        f.done -> ListenStatus.Done(f.nextStart, f.nextStartTomorrow)
        !f.hasScale -> ListenStatus.NoScale
        !f.permission -> ListenStatus.PermissionMissing
        !f.healthWrite -> ListenStatus.HealthMissing
        !f.bluetoothOn -> ListenStatus.BluetoothOff
        !f.armed -> ListenStatus.Stopped
        f.powerSave && !f.listenInPowerSave -> ListenStatus.PowerSavePause
        f.inside -> ListenStatus.Listening(f.windowEnd, f.windowEndTomorrow)
        else -> ListenStatus.Resting(f.nextStart, f.nextStartTomorrow)
    }
}
