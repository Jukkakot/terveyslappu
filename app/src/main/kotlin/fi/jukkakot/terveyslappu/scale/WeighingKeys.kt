package fi.jukkakot.terveyslappu.scale

import java.time.Instant

/**
 * One weighing, one save (background-weigh-in design 6): the company id of the last saved or
 * discarded weighing and when, shared by the dialog and the background save. [claim] is a
 * compare-and-set under one process-wide lock: whoever claims a key first saves it, the other
 * ignores packets with that key for [WeighIn.IGNORE_MS]. A claim whose write failed stays claimed.
 */
class WeighingKeys(private val store: LastWeighInStore, private val now: () -> Instant = Instant::now) {

    /** True when [key] belongs to a weighing saved or discarded within the ignore time. */
    fun isTaken(key: Int): Boolean = synchronized(LOCK) { taken(key, now()) }

    /** The key still lingering, if any. */
    fun lingering(): Int? = synchronized(LOCK) {
        store.load()?.takeIf { (_, at) -> at.plusMillis(WeighIn.IGNORE_MS).isAfter(now()) }?.first
    }

    /** Claims [key] for saving; false when it was already claimed lately (someone else saves it). */
    fun claim(key: Int): Boolean = synchronized(LOCK) {
        val at = now()
        if (taken(key, at)) return false
        store.save(key, at)
        true
    }

    /** Restarts the ignore time of [key] (a discarded weighing's broadcast keeps lingering). */
    fun touch(key: Int) = synchronized(LOCK) { store.save(key, now()) }

    private fun taken(key: Int, at: Instant): Boolean {
        val (last, lastAt) = store.load() ?: return false
        return last == key && lastAt.plusMillis(WeighIn.IGNORE_MS).isAfter(at)
    }

    private companion object {
        val LOCK = Any()
    }
}

/** The weigh-in dialog is in the foreground: the background saves nothing meanwhile (design 6). */
object WeighInForeground {
    @Volatile
    var shown: Boolean = false
}
