package fi.jukkakot.terveyslappu.listen

/** The widget's automatic weigh-in mark (background-weigh-in "Widget status"); [key] is what the log and prefs keep. */
enum class WidgetState(val key: String) {
    ON("on"),
    DONE("done"),
    RESTING("resting"),
    BLOCKED("blocked"),
    ;

    companion object {
        fun of(key: String?): WidgetState? = entries.firstOrNull { it.key == key }
    }
}

/** The widget follows the Asetukset status (design 10). Pure. */
object WidgetStateRule {
    /**
     * Null while the feature is off ([status] null: the widget looks as before). Valmis from the
     * window's saved weight until the next window start, also outside the window. Otherwise outside
     * the window always Lepää; inside, Kuuntelee while listening, else Estetty (a blocked reason, Tauolla or
     * Pysäytetty). The warnings (battery, notifications) are not part of the status, so they never
     * give Estetty.
     */
    fun of(status: ListenStatus?, inside: Boolean): WidgetState? = when {
        status == null -> null
        status is ListenStatus.Done -> WidgetState.DONE
        !inside -> WidgetState.RESTING
        status == ListenStatus.Listening -> WidgetState.ON
        status is ListenStatus.Resting -> WidgetState.RESTING
        else -> WidgetState.BLOCKED
    }

    /** A stored state at a redraw: once the stored window end has passed, Kuuntelee becomes Lepää. */
    fun shown(stored: WidgetState?, windowEndMs: Long?, nowMs: Long): WidgetState? =
        if (stored == WidgetState.ON && windowEndMs != null && nowMs >= windowEndMs) WidgetState.RESTING else stored
}
