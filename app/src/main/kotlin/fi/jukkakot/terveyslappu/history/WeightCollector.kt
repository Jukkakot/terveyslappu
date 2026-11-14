package fi.jukkakot.terveyslappu.history

import fi.jukkakot.terveyslappu.raw.RawEntry
import fi.jukkakot.terveyslappu.weight.MAX_KG
import fi.jukkakot.terveyslappu.weight.MIN_KG
import java.time.Instant

/**
 * The import's row rules for read weights (history-import "Skip rows that cannot be weights"):
 * range, future, and the later of two rows of the same second wins. Shared by tables and JSON.
 */
class WeightCollector(private val now: Instant) {
    private val skipped = mutableListOf<SkippedRow>()
    private val bySecond = LinkedHashMap<Long, Pair<ImportedWeight, String>>()

    fun skip(line: Int, reason: SkipReason, text: String) {
        skipped += SkippedRow(line, reason, text)
    }

    /** [raw] gives the weight's raw data for its epoch second, read only for kept weights. */
    fun add(line: Int, text: String, time: Instant, kg: Double, check: Boolean = false, raw: (Long) -> RawEntry?) {
        when {
            kg !in MIN_KG..MAX_KG -> skip(line, SkipReason.OUT_OF_RANGE, text)
            time.isAfter(now) -> skip(line, SkipReason.FUTURE, text)
            else -> {
                val second = time.epochSecond
                bySecond.remove(second)?.let { (old, oldText) -> skip(old.line, SkipReason.DUPLICATE, oldText) }
                bySecond[second] = ImportedWeight(time, kg, line, raw(second), check) to text
            }
        }
    }

    fun result(layout: Layout?) =
        ParsedHistory(bySecond.values.map { it.first }.sortedBy { it.time }, skipped.sortedBy { it.line }, layout)
}
