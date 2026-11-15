package fi.jukkakot.terveyslappu.home

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * An intent from outside the front page (home-card-layout design 4): a new intent to the main
 * activity, the Lisää annos dialog, the weigh-in dialog, a notification action. The front page ends
 * its edit mode and drops the draft on each. Process-wide, like `WeighInForeground`.
 */
object HomeEditSignal {
    private val events = MutableSharedFlow<String>(extraBufferCapacity = 8)

    val outside: SharedFlow<String> = events

    fun outsideIntent(from: String) {
        events.tryEmit(from)
    }
}
