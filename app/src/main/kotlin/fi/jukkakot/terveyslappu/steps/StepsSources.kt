package fi.jukkakot.terveyslappu.steps

import java.time.LocalDate

/**
 * A steps source = one app = a set of Health Connect origin packages (steps-source-and-axes
 * design 1). The phone's own steps (`android` before the June 2026 update, a device-specific
 * synthetic package name after it) are one app with id [StepsSourceRules.PHONE].
 */
data class StepsApp(val id: String, val packages: Set<String>)

/**
 * One app's last 30 days from a source check (design 2): [total] with today included (the pick and
 * the list order), [days] with steps, [averageExToday] the list's average per day with today left
 * out (null: no finished day with steps).
 */
data class StepsAppTotal(val app: StepsApp, val total: Long, val days: Int, val averageExToday: Long?)

/** The user's source choice (design 3), stored as `auto` / `all` / `app:<id>`. */
sealed interface StepsChoice {
    data object Auto : StepsChoice
    data object All : StepsChoice
    data class App(val id: String) : StepsChoice

    fun encode(): String = when (this) {
        Auto -> "auto"
        All -> "all"
        is App -> "app:$id"
    }

    companion object {
        /** Unknown, empty or corrupt → [Auto]. */
        fun decode(text: String?): StepsChoice = when {
            text == "all" -> All
            text != null && text.startsWith("app:") && text.length > 4 && text.substring(4).isNotBlank() -> App(text.substring(4))
            else -> Auto
        }
    }
}

/** What the Asetukset row, the sub-screen and the Askeleet source line say (design 5, 7). */
sealed interface StepsSourceLabel {
    /** Automaattinen; [id] = the app in use, null = none found (all apps are read). */
    data class Auto(val id: String?) : StepsSourceLabel

    /** A chosen app; [noRecent] null = unknown until today's check has run. */
    data class App(val id: String, val noRecent: Boolean?) : StepsSourceLabel

    data object All : StepsSourceLabel
}

/**
 * The source in use: [packages] null = no origin filter (Health Connect's merge); [key] changes
 * whenever the read's source does (stale results are dropped by it); [logName] = `all`, `phone` or
 * the package.
 */
data class StepsSourceState(val packages: Set<String>?, val key: String, val label: StepsSourceLabel) {
    val logName: String
        get() = when (label) {
            is StepsSourceLabel.Auto -> label.id ?: "all"
            is StepsSourceLabel.App -> label.id
            StepsSourceLabel.All -> "all"
        }

    /** The one app read, or null for all apps. */
    val appId: String?
        get() = when (label) {
            is StepsSourceLabel.Auto -> label.id
            is StepsSourceLabel.App -> label.id
            StepsSourceLabel.All -> null
        }
}

object StepsSourceRules {
    const val PHONE = "phone"
    const val ANDROID = "android"
    const val PHONE_PREFIX = "com.android.healthconnect.phone."

    /** The step apps the manifest can see (design 2), each always checked. */
    val KNOWN_APPS = listOf(
        "com.sec.android.app.shealth",
        "com.garmin.android.apps.connectmobile",
        "com.fitbit.FitbitMobile",
        "com.google.android.apps.fitness",
    )

    fun isPhone(origin: String, phoneSpn: String?): Boolean =
        origin == ANDROID || origin == phoneSpn || origin.startsWith(PHONE_PREFIX)

    /** Origins to apps (design 1): the phone's origins become one app [PHONE], every other origin its own. */
    fun group(origins: Set<String>, phoneSpn: String?): List<StepsApp> {
        val (phone, other) = origins.partition { isPhone(it, phoneSpn) }
        return buildList {
            if (phone.isNotEmpty()) add(StepsApp(PHONE, phone.toSet()))
            other.sorted().forEach { add(StepsApp(it, setOf(it))) }
        }
    }

    /**
     * The automatic pick (design 4): the most steps in 30 days, ties → lowest id; [current] stays
     * unless the best exceeds it by more than 10 %; nothing above 0 → null (read all apps).
     */
    fun pick(totals: List<StepsAppTotal>, current: String?): String? {
        val best = totals.filter { it.total > 0 }.sortedWith(compareByDescending<StepsAppTotal> { it.total }.thenBy { it.app.id }).firstOrNull()
            ?: return null
        if (current == null || current == best.app.id) return best.app.id
        val currentTotal = totals.firstOrNull { it.app.id == current }?.total ?: 0L
        if (currentTotal <= 0) return best.app.id
        return if (best.total > currentTotal * 1.10) best.app.id else current
    }

    /** Average per day with steps, today's steps (it is not over) left out of the total and the day count. */
    fun averageExToday(total: Long, days: Int, todaySteps: Long): Long? {
        val finishedDays = days - (if (todaySteps > 0) 1 else 0)
        if (finishedDays <= 0) return null
        return Math.round((total - todaySteps).toDouble() / finishedDays)
    }

    /** One app's 30-day figures from its daily totals (today included in [total]). */
    fun totals(app: StepsApp, days: List<StepsDay>, today: LocalDate): StepsAppTotal {
        val withSteps = days.filter { it.steps > 0 }
        val total = withSteps.sumOf { it.steps }
        val todaySteps = withSteps.filter { it.day == today }.sumOf { it.steps }
        return StepsAppTotal(app, total, withSteps.size, averageExToday(total, withSteps.size, todaySteps))
    }

    /** The source list's apps (design 7): steps in 30 days, most first; a chosen app without them last. */
    fun listed(apps: List<StepsAppTotal>, choice: StepsChoice): List<StepsAppTotal> {
        val recent = apps.filter { it.total > 0 }.sortedWith(compareByDescending<StepsAppTotal> { it.total }.thenBy { it.app.id })
        val chosen = (choice as? StepsChoice.App)?.id ?: return recent
        if (recent.any { it.app.id == chosen }) return recent
        val silent = apps.firstOrNull { it.app.id == chosen } ?: StepsAppTotal(StepsApp(chosen, setOf(chosen)), 0, 0, null)
        return recent + silent
    }

    /** The packages of app [id]: from the check when known, else the id itself (the phone: `android` + saved). */
    fun packagesOf(id: String, apps: List<StepsAppTotal>?, savedPhone: Set<String>): Set<String> =
        apps?.firstOrNull { it.app.id == id }?.app?.packages
            ?: if (id == PHONE) setOf(ANDROID) + savedPhone else setOf(id)

    /**
     * The source in use (design 5). [apps] = the latest check's totals (null: none in this run),
     * [checkedToday] = a check succeeded today in this run (only then is a chosen app's `noRecent` known).
     */
    fun resolve(
        choice: StepsChoice,
        autoPick: String?,
        apps: List<StepsAppTotal>?,
        checkedToday: Boolean,
        savedPhone: Set<String> = emptySet(),
    ): StepsSourceState = when (choice) {
        StepsChoice.All -> StepsSourceState(null, "all", StepsSourceLabel.All)
        StepsChoice.Auto -> {
            if (autoPick == null) {
                StepsSourceState(null, "auto:all", StepsSourceLabel.Auto(null))
            } else {
                val packages = packagesOf(autoPick, apps, savedPhone)
                StepsSourceState(packages, "auto:" + packages.sorted().joinToString(","), StepsSourceLabel.Auto(autoPick))
            }
        }
        is StepsChoice.App -> {
            val packages = packagesOf(choice.id, apps, savedPhone)
            val noRecent = if (checkedToday && apps != null) (apps.firstOrNull { it.app.id == choice.id }?.total ?: 0L) <= 0 else null
            StepsSourceState(packages, "app:" + packages.sorted().joinToString(","), StepsSourceLabel.App(choice.id, noRecent))
        }
    }
}
