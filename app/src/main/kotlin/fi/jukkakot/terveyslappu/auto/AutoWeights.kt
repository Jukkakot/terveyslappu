package fi.jukkakot.terveyslappu.auto

import fi.jukkakot.terveyslappu.listen.DecidedSink
import fi.jukkakot.terveyslappu.listen.HeardEvent
import fi.jukkakot.terveyslappu.log.AppLog
import fi.jukkakot.terveyslappu.raw.RawEntry
import fi.jukkakot.terveyslappu.raw.RawStore
import fi.jukkakot.terveyslappu.scale.ScalePacket
import fi.jukkakot.terveyslappu.scale.WeighIn
import fi.jukkakot.terveyslappu.scale.WeighingKeys
import fi.jukkakot.terveyslappu.scale.hexToBytes
import fi.jukkakot.terveyslappu.scale.toHex
import fi.jukkakot.terveyslappu.weight.scaleClientId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Instant
import java.time.ZoneId

/** Health Connect for the automatic weigh-in: writes and deletes only, never a read (design, research). */
interface AutoWriter {
    /** Writes [weight] as an automatically recorded scale weight; returns its `clientRecordId`. */
    suspend fun write(weight: PendingWeight): String

    /** Deletes this app's weight with [clientRecordId]. */
    suspend fun delete(clientRecordId: String)
}

/** One posted weigh-in notification (design 8): id from the weight's `clientRecordId`. */
data class AutoNote(
    val id: String,
    val kg: Double,
    val timeMs: Long,
    val questioned: Boolean,
    val text: NoteText,
)

/** Posts and closes the weigh-in notifications. */
interface AutoNotifier {
    /** Posts [note]; false when notifications are not allowed (the weight is saved all the same). */
    fun post(note: AutoNote): Boolean

    fun cancel(id: String)
}

/**
 * The automatic weigh-in's save, acceptance and delete (background-weigh-in, design 1-11).
 * Android-free: the receivers, the notification actions and Historia call it. A decided weight is
 * claimed and put in the pending queue first ([decided], under the scan's lock), then written by
 * [flush] within the time budget; whatever is left stays queued for the next weigh-in, the hourly
 * check or the app's first foreground read.
 */
class AutoWeights(
    private val keys: WeighingKeys,
    private val queue: PendingStore,
    private val marks: MarkStore,
    private val reference: ReferenceStore,
    private val flags: FlagStore,
    private val raw: RawStore,
    private val writer: AutoWriter,
    private val notifier: AutoNotifier,
    private val limit: () -> Double,
    private val changed: () -> Unit,
    private val dialogShown: () -> Boolean,
    private val elapsedMs: () -> Long,
    private val zone: () -> ZoneId = ZoneId::systemDefault,
    private val budgetMs: Long = BUDGET_MS,
    /** A weight of this app not marked "?" was saved or accepted: it can be the window's weight. */
    private val weightSaved: (id: String, timeMs: Long) -> Unit = { _, _ -> },
    /** A weight of this app was deleted. */
    private val weightRemoved: (id: String) -> Unit = {},
) : DecidedSink {

    /** A decided background weight: skipped while the dialog is in front or when its weighing was claimed. */
    override fun decided(weight: HeardEvent.Decided) {
        val key = "%04x".format(weight.companyId and 0xFFFF)
        if (dialogShown()) {
            AppLog.i("auto.skip", "", "reason" to "dialog", "key" to key)
            return
        }
        val same = try {
            keys.sameWeight(weight.kg, weight.address)
        } catch (e: Exception) {
            AppLog.e("auto.claim", e, "same")
            false
        }
        if (same) {
            AppLog.i("scale.ignore", "", "reason" to "same", "kg" to weight.kg, "src" to "bg")
            return
        }
        val claimed = try {
            keys.claim(weight.companyId)
        } catch (e: Exception) {
            AppLog.e("auto.claim", e, "failed")
            false
        }
        if (!claimed) {
            AppLog.i("auto.skip", "", "reason" to "key", "key" to key)
            return
        }
        keep("remember") { keys.remember(weight.kg, weight.address) }
        val offset = zone().rules.getOffset(Instant.ofEpochMilli(weight.atWallMs)).totalSeconds
        queue.add(PendingWeight(weight.kg, weight.atWallMs, offset, weight.companyId, weight.data.toHex()))
    }

    /**
     * Writes the pending queue within the time budget (design 3, 11). [reason]: `weighin` (right
     * after a decision), `check` (hourly), `app` (first foreground read). Returns how many were written.
     */
    suspend fun flush(reason: String): Int {
        val waiting = queue.all()
        if (waiting.isEmpty()) return 0
        val started = elapsedMs()
        var saved = 0
        var current: PendingWeight? = null
        val finished = withTimeoutOrNull(budgetMs) {
            for (weight in waiting) {
                current = weight
                if (save(weight)) saved++
            }
            current = null
            true
        }
        if (finished == null) {
            current?.let(queue::failed)
            AppLog.w("auto.timeout", "", "queued" to queue.count(), "ms" to elapsedMs() - started)
        }
        if (reason != WEIGHIN || waiting.any { it.attempts > 0 }) {
            val left = queue.count()
            AppLog.i("auto.retry", if (left == 0) "ok" else "failed", "count" to saved, "queued" to left, "reason" to reason)
        }
        return saved
    }

    private suspend fun save(weight: PendingWeight): Boolean {
        val started = elapsedMs()
        val ref = reference.reference(weight.timeMs)
        val questioned = Plausibility.questioned(ref, weight.kg, limit())
        val id = try {
            writer.write(weight)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            queue.failed(weight)
            AppLog.e("auto.save", e, "failed", "cause" to (e::class.simpleName ?: "error"), "queued" to queue.count(), "ms" to elapsedMs() - started)
            return false
        }
        val writeMs = elapsedMs() - started
        val second = weight.timeMs / 1000
        keep("raw") { raw.add(RawEntry(second, WeighIn.SOURCE, ScalePacket.rawFields(weight.companyId, weight.data.hexToBytes()) + (VIA to AUTO))) }
        if (questioned) keep("mark") { marks.add(listOf(Mark(id, weight.timeMs, weight.kg))) } else keep("reference") { reference.accepted(listOf(RefPair(id, weight.timeMs, weight.kg))) }
        keep("flag") { flags.backupDue = true }
        queue.remove(weight)
        if (!questioned) keep("done") { weightSaved(id, weight.timeMs) }
        val markMs = elapsedMs() - started - writeMs
        val late = if (weight.attempts > 0) Instant.ofEpochMilli(weight.timeMs) else null
        val note = AutoNote(id, weight.kg, weight.timeMs, questioned, AutoText.of(weight.kg, questioned, ref, late, zone()))
        val notifyStart = elapsedMs()
        val posted = keep("notify") { notifier.post(note) } ?: false
        if (posted) AppLog.i("auto.notify", "", "check" to check(questioned)) else AppLog.w("auto.notify", "denied")
        val notifyMs = elapsedMs() - notifyStart
        val widgetStart = elapsedMs()
        keep("changed", changed)
        AppLog.i(
            "auto.save", "ok",
            "kg" to weight.kg,
            "check" to check(questioned),
            "ref" to refText(ref),
            "late" to (late != null),
            "writeMs" to writeMs,
            "markMs" to markMs,
            "notifyMs" to notifyMs,
            "widgetMs" to elapsedMs() - widgetStart,
            "ms" to elapsedMs() - started,
        )
        return true
    }

    /**
     * Accepts the "?" weight [id] and the chain after it (design 7); [from] is `historia` or
     * `notification`. Closes their notifications. Returns the accepted ids (empty when [id] has no mark).
     */
    fun accept(id: String, from: String): List<String> {
        val all = marks.all()
        val anchor = all.firstOrNull { it.id == id } ?: return emptyList()
        val chain = listOf(anchor) + AcceptChain.chain(anchor, all, limit())
        marks.remove(chain.map { it.id })
        reference.accepted(chain.map { RefPair(it.id, it.timeMs, it.kg) })
        chain.forEach { keep("cancel") { notifier.cancel(it.id) } }
        chain.forEach { mark -> keep("done") { weightSaved(mark.id, mark.timeMs) } }
        AppLog.i("auto.confirm", "", "from" to from, "chain" to chain.size - 1)
        if (from == FROM_NOTIFICATION) flags.backupDue = true
        keep("changed", changed)
        return chain.map { it.id }
    }

    /**
     * Poista from the notification: deletes the weight with [id] at [timeMs] and its raw data. A
     * failed delete re-posts the notification saying so. Returns true when deleted.
     */
    suspend fun delete(id: String, timeMs: Long, kg: Double, questioned: Boolean): Boolean {
        try {
            writer.delete(id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLog.e("auto.action", e, "delete failed", "cause" to (e::class.simpleName ?: "error"))
            keep("notify") { notifier.post(AutoNote(id, kg, timeMs, questioned, NoteText("${AutoText.kg(kg)} kg", AutoText.DELETE_FAILED))) }
            return false
        }
        AppLog.i("auto.action", "delete ok")
        keep("remember") { keys.remember(kg) }
        keep("raw") { raw.remove(timeMs / 1000) }
        keep("mark") { marks.remove(listOf(id)) }
        keep("reference") { reference.removed(id) }
        keep("cancel") { notifier.cancel(id) }
        keep("done") { weightRemoved(id) }
        flags.backupDue = true
        keep("changed", changed)
        return true
    }

    /** The queue's size for Asetukset. */
    fun pending(): Int = queue.count()

    private fun refText(ref: Reference): String = when (ref) {
        Reference.Unknown -> "unknown"
        Reference.None -> "none"
        is Reference.Weight -> ref.kg.toString()
    }

    private fun check(questioned: Boolean) = if (questioned) "?" else "ok"

    /** Local bookkeeping trouble is logged and never fails the save: the weight in Health Connect is what counts. */
    private inline fun <T> keep(step: String, block: () -> T): T? = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        AppLog.e("auto.save", e, step)
        null
    }

    companion object {
        const val BUDGET_MS = 8_000L
        const val WEIGHIN = "weighin"
        const val FROM_HISTORIA = "historia"
        const val FROM_NOTIFICATION = "notification"
        const val VIA = "via"
        const val AUTO = "auto"

        /** The `clientRecordId` an automatic weight gets: the scale rule's, from the reading's second. */
        fun clientId(weight: PendingWeight): String = scaleClientId(Instant.ofEpochMilli(weight.timeMs))
    }
}
