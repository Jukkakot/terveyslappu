package fi.jukkakot.terveyslappu.ui

import fi.jukkakot.terveyslappu.history.ParsedHistory
import fi.jukkakot.terveyslappu.history.SheetText
import fi.jukkakot.terveyslappu.history.SkipReason
import fi.jukkakot.terveyslappu.log.AppLog

/** The history import dialog's state (history-import spec); null in [MainUiState] = no dialog. */
sealed interface ImportState {
    data object Reading : ImportState
    data class Preview(val parsed: ParsedHistory) : ImportState
    data class Writing(val parsed: ParsedHistory, val saved: Int) : ImportState
    data class Failed(val parsed: ParsedHistory, val saved: Int) : ImportState
    /** The file could not be opened or read at all. */
    data object ReadFailed : ImportState
}

/** Logs what the parser decided, so a file it cannot read can be supported from a shared log. */
fun logParsed(parsed: ParsedHistory, bytes: Int, ms: Long, sheet: SheetText? = null, format: String = if (sheet == null) "text" else "xls") {
    val layout = parsed.layout
    AppLog.i(
        "import.read", if (layout == null && format != "json") "no columns" else "ok",
        "format" to format,
        "unknown" to parsed.unknownKinds.joinToString(",").ifEmpty { null },
        "okok" to sheet?.okok,
        "keptColumns" to sheet?.keptColumns,
        "profileCells" to sheet?.profileCells,
        "bytes" to bytes,
        "ms" to ms,
        "header" to (layout?.header?.replace(' ', '_') ?: "-"),
        "sep" to (layout?.separator?.let { if (it == '\t') "tab" else it.toString() } ?: "-"),
        "dateCol" to layout?.dateCol,
        "timeCol" to layout?.timeCol,
        "weightCol" to layout?.weightCol,
        "pounds" to layout?.pounds,
        "jin" to layout?.jin,
        "dayFirst" to layout?.dayFirst,
        "ambiguous" to layout?.ambiguousOrder,
        "ok" to parsed.weights.size,
        "skipped" to parsed.skippedCounts().entries.joinToString(",") { "${it.key.name}:${it.value}" }.ifEmpty { "0" },
    )
    for (row in parsed.skipped.take(MAX_LOGGED_SKIPS)) {
        AppLog.d("import.skip", "", "line" to row.line, "reason" to row.reason.name, "raw" to row.raw)
    }
}

private const val MAX_LOGGED_SKIPS = 20

/** Finnish label resource for a skip reason. */
fun SkipReason.label(): Int = when (this) {
    SkipReason.UNREADABLE_DATE -> fi.jukkakot.terveyslappu.R.string.import_skip_date
    SkipReason.UNREADABLE_WEIGHT -> fi.jukkakot.terveyslappu.R.string.import_skip_weight
    SkipReason.OUT_OF_RANGE -> fi.jukkakot.terveyslappu.R.string.import_skip_range
    SkipReason.FUTURE -> fi.jukkakot.terveyslappu.R.string.import_skip_future
    SkipReason.DUPLICATE -> fi.jukkakot.terveyslappu.R.string.import_skip_duplicate
}
