package fi.jukkakot.terveyslappu.raw

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

/**
 * The raw data behind one weight (scale-weigh-in "Keep the raw scale reading"): keyed by the
 * weight's time in epoch [second], with the [source] that produced it (`scale`, `okok`, ...) and
 * its named [fields] exactly as received, in order. Nothing here is calculated.
 */
@Serializable
data class RawEntry(val second: Long, val source: String, val fields: Map<String, String>)

/** Where raw entries are kept; [RawArchive] on the phone, a map in tests. */
interface RawStore {
    /** Adds [entry], replacing one with the same second. */
    fun add(entry: RawEntry)

    fun addAll(entries: Collection<RawEntry>) = entries.forEach(::add)

    /** Removes and returns the entry of [second], or null when there is none. */
    fun remove(second: Long): RawEntry?

    fun get(second: Long): RawEntry?

    /** Every entry, oldest first. */
    fun all(): List<RawEntry>
}

/** Keeps entries in memory, for tests and as the base of [RawArchive]. */
open class MemoryRawStore : RawStore {
    protected val entries = sortedMapOf<Long, RawEntry>()

    @Synchronized override fun add(entry: RawEntry) {
        entries[entry.second] = entry
        changed()
    }

    @Synchronized override fun addAll(entries: Collection<RawEntry>) {
        if (entries.isEmpty()) return
        entries.forEach { this.entries[it.second] = it }
        changed()
    }

    @Synchronized override fun remove(second: Long): RawEntry? = entries.remove(second)?.also { changed() }

    @Synchronized override fun get(second: Long): RawEntry? = entries[second]

    @Synchronized override fun all(): List<RawEntry> = entries.values.toList()

    protected open fun changed() {}
}

/**
 * The archive in app storage: one JSON object per line (kotlinx.serialization), loaded once and
 * rewritten whole on every change (a few hundred short lines a year). An unreadable line is
 * skipped, so one bad write never loses the rest.
 */
class RawArchive(private val file: File) : MemoryRawStore() {
    private val json = Json { ignoreUnknownKeys = true }

    init {
        if (file.exists()) {
            file.readLines().filter { it.isNotBlank() }.forEach { line ->
                runCatching { json.decodeFromString(RawEntry.serializer(), line) }.getOrNull()?.let { entries[it.second] = it }
            }
        }
    }

    override fun changed() {
        file.parentFile?.mkdirs()
        val tmp = File(file.path + ".tmp")
        tmp.writeText(entries.values.joinToString("") { json.encodeToString(RawEntry.serializer(), it) + "\n" })
        if (!tmp.renameTo(file)) {
            file.delete()
            check(tmp.renameTo(file)) { "cannot replace ${file.name}" }
        }
    }
}

/**
 * The export's `raw` column: `source=<source>|name=value|...`. A `\` or `|` inside a value is
 * escaped with `\`; only the first `=` of a pair splits it, so values may hold `=`.
 */
object RawFormat {
    fun encode(entry: RawEntry): String =
        (listOf(SOURCE to entry.source) + entry.fields.toList()).joinToString("|") { (k, v) -> "${escape(k)}=${escape(v)}" }

    /** The entry of a `raw` cell for the weight at [second], or null when the cell is empty or has no source. */
    fun decode(text: String, second: Long): RawEntry? {
        if (text.isBlank()) return null
        val pairs = LinkedHashMap<String, String>()
        for (part in split(text.trim())) {
            val eq = part.indexOf('=')
            if (eq <= 0) continue
            pairs[unescape(part.substring(0, eq))] = unescape(part.substring(eq + 1))
        }
        val source = pairs.remove(SOURCE) ?: return null
        return RawEntry(second, source, pairs)
    }

    private const val SOURCE = "source"

    private fun escape(text: String) = text.replace("\\", "\\\\").replace("|", "\\|")

    /** Splits on `|` not preceded by an escaping `\`; the parts stay escaped. */
    private fun split(text: String): List<String> {
        val parts = mutableListOf<String>()
        val current = StringBuilder()
        var i = 0
        while (i < text.length) {
            val c = text[i]
            when {
                c == '\\' && i + 1 < text.length -> {
                    current.append(c).append(text[i + 1])
                    i++
                }
                c == '|' -> {
                    parts += current.toString()
                    current.clear()
                }
                else -> current.append(c)
            }
            i++
        }
        parts += current.toString()
        return parts
    }

    private fun unescape(text: String): String {
        val out = StringBuilder()
        var i = 0
        while (i < text.length) {
            if (text[i] == '\\' && i + 1 < text.length) i++
            out.append(text[i])
            i++
        }
        return out.toString()
    }
}
