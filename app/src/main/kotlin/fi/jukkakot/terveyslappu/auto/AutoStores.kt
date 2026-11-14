package fi.jukkakot.terveyslappu.auto

import fi.jukkakot.terveyslappu.log.AppLog
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

/**
 * A value kept as one JSON file in app storage (never in Android backup: data_extraction_rules.xml
 * is an allow-list), or only in memory when [file] is null (tests). Rewritten whole through a temp
 * file; an unreadable file reads as [empty] and is logged.
 */
open class JsonFileValue<T>(private val file: File?, private val serializer: KSerializer<T>, private val empty: T) {
    private var value: T? = null

    @Synchronized
    fun get(): T {
        value?.let { return it }
        val read = file?.takeIf { it.exists() }?.let { f ->
            runCatching { json.decodeFromString(serializer, f.readText()) }
                .onFailure { AppLog.e("auto.store", it, "read", "file" to f.name) }
                .getOrNull()
        } ?: empty
        value = read
        return read
    }

    @Synchronized
    fun set(new: T) {
        value = new
        val f = file ?: return
        f.parentFile?.mkdirs()
        val tmp = File(f.path + ".tmp")
        tmp.writeText(json.encodeToString(serializer, new))
        if (!tmp.renameTo(f)) {
            f.delete()
            check(tmp.renameTo(f)) { "cannot replace ${f.name}" }
        }
    }

    /** Read-modify-write under the lock; returns the new value. */
    @Synchronized
    fun update(change: (T) -> T): T = change(get()).also(::set)

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}

/**
 * The "?" marks (design 5), keyed by the weight's own `clientRecordId`, holding only "?" weights
 * with their time and kg (the chained acceptance needs no Health Connect read).
 */
class MarkStore(file: File? = null) : JsonFileValue<List<Mark>>(file, ListSerializer(Mark.serializer()), emptyList()) {
    fun all(): List<Mark> = get()

    fun ids(): Set<String> = get().map { it.id }.toSet()

    fun add(marks: Collection<Mark>) {
        if (marks.isEmpty()) return
        val ids = marks.map { it.id }.toSet()
        update { old -> old.filter { it.id !in ids } + marks }
    }

    /** Removes the marks of [ids]; returns the removed ones (for undo). */
    fun remove(ids: Collection<String>): List<Mark> {
        val set = ids.toSet()
        val removed = get().filter { it.id in set }
        if (removed.isNotEmpty()) update { old -> old.filter { it.id !in set } }
        return removed
    }

    /** A foreground read: marks whose weight was not read are dropped; returns how many. */
    fun retain(readIds: Set<String>): Int {
        val orphans = get().count { it.id !in readIds }
        if (orphans > 0) update { old -> old.filter { it.id in readIds } }
        return orphans
    }
}

/** The reference store (design 4). */
class ReferenceStore(file: File? = null) : JsonFileValue<ReferenceState>(file, ReferenceState.serializer(), ReferenceState()) {
    fun reference(beforeMs: Long): Reference = ReferenceRule.reference(get(), beforeMs)

    fun accepted(pairs: Collection<RefPair>) {
        if (pairs.isNotEmpty()) update { ReferenceRule.accepted(it, pairs) }
    }

    fun removed(id: String) {
        update { ReferenceRule.removed(it, id) }
    }

    fun seed(pairs: Collection<RefPair>) = set(ReferenceRule.seeded(pairs))
}

/**
 * A decided automatic weight waiting to be written (design 3): kg, the deciding reading's time and
 * the phone's offset then, the packet for the raw entry, and how many writes failed (a weight with
 * [attempts] > 0 is a late save, whose notification names its time).
 */
@Serializable
data class PendingWeight(
    val kg: Double,
    val timeMs: Long,
    val offsetSeconds: Int,
    val companyId: Int,
    val data: String,
    val attempts: Int = 0,
)

/** The pending queue (design 3): kept until written, one per second of reading time. */
class PendingStore(file: File? = null) : JsonFileValue<List<PendingWeight>>(file, ListSerializer(PendingWeight.serializer()), emptyList()) {
    fun all(): List<PendingWeight> = get()

    fun count(): Int = get().size

    fun add(weight: PendingWeight) {
        update { old -> old.filter { it.timeMs / 1000 != weight.timeMs / 1000 } + weight }
    }

    fun remove(weight: PendingWeight) {
        update { old -> old.filter { it.timeMs / 1000 != weight.timeMs / 1000 } }
    }

    fun failed(weight: PendingWeight) {
        update { old -> old.map { if (it.timeMs / 1000 == weight.timeMs / 1000) it.copy(attempts = it.attempts + 1) else it } }
    }
}

/** Small flags of the automatic weigh-in (design 9: a change made in the background is backed up at the next app start). */
@Serializable
data class AutoFlags(val backupDue: Boolean = false)

class FlagStore(file: File? = null) : JsonFileValue<AutoFlags>(file, AutoFlags.serializer(), AutoFlags()) {
    var backupDue: Boolean
        get() = get().backupDue
        set(value) {
            if (value) AppLog.i("backup.due")
            update { it.copy(backupDue = value) }
        }

    /** Reads and clears the flag at once. */
    fun takeBackupDue(): Boolean {
        val due = get().backupDue
        if (due) update { it.copy(backupDue = false) }
        return due
    }
}
