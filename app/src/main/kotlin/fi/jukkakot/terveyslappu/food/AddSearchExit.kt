package fi.jukkakot.terveyslappu.food

import java.time.LocalDate

/** Where leaving the add search goes (food-log "Leaving the search"). */
sealed interface AddSearchTarget {
    /** Ruoka on [day] with [uuids] (the items' own client ids, in the order added) highlighted. */
    data class Ruoka(val day: LocalDate, val uuids: List<String>) : AddSearchTarget

    /** Back to the screen the search was opened from, as it was. */
    data object Origin : AddSearchTarget
}

/** The one rule for leaving the add search, shared by the app and the widget's activity. */
object AddSearchExit {
    /**
     * [added] = the uuids counted as added in this search (queued adds minus failed writes, Kumoa and
     * deletes); with any, Ruoka on [day] with them highlighted, else the origin.
     */
    fun target(day: LocalDate, added: Collection<String>): AddSearchTarget =
        if (added.isEmpty()) AddSearchTarget.Origin else AddSearchTarget.Ruoka(day, added.toList())
}
