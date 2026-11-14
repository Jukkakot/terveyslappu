package fi.jukkakot.terveyslappu.scale

import java.time.Instant
import kotlin.math.abs

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

    /**
     * True when a stable [kg] from [address] repeats the last saved or deleted weight (within
     * [SAME_KG], whatever its company id): ignored while the last hearing is under [SAME_MS] old,
     * and each ignored hearing restarts that time (the scale repeats it every 20 s to 2 min).
     */
    fun sameWeight(kg: Double, address: String?): Boolean = synchronized(LOCK) {
        val last = store.weight() ?: return false
        val at = now()
        val sameScale = last.address == null || address == null || last.address.equals(address, ignoreCase = true)
        val same = sameScale && abs(last.kg - kg) <= SAME_KG + 1e-9 && last.heardAt.plusMillis(SAME_MS).isAfter(at)
        if (same) store.saveWeight(last.copy(heardAt = at))
        same
    }

    /** A weight was saved or deleted: its repeats are ignored from now on. [address] defaults to the remembered scale. */
    fun remember(kg: Double, address: String? = null) = synchronized(LOCK) {
        store.saveWeight(LastWeight(kg, address ?: store.scale()?.address, now()))
    }

    private fun taken(key: Int, at: Instant): Boolean {
        val (last, lastAt) = store.load() ?: return false
        return last == key && lastAt.plusMillis(WeighIn.IGNORE_MS).isAfter(at)
    }

    companion object {
        private val LOCK = Any()
        const val SAME_KG = 0.1
        const val SAME_MS = 30_000L
    }
}

/** The weigh-in dialog is in the foreground: the background saves nothing meanwhile (design 6). */
object WeighInForeground {
    @Volatile
    var shown: Boolean = false
}
