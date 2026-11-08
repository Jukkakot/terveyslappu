package fi.jukkakot.terveyslappu.history

import kotlinx.coroutines.CancellationException

sealed interface WriteResult {
    data class Done(val count: Int) : WriteResult
    /** [saved] weights (from the start of the list) are in Health Connect; retry from there. */
    data class Failed(val saved: Int, val error: Exception) : WriteResult
}

/**
 * Writes imported weights in batches (design decision 5). Retrying from [write]'s `from` is safe
 * because every record has a deterministic client id, so a repeated batch is an upsert.
 */
class HistoryWriter(
    private val insert: suspend (List<ImportedWeight>) -> Unit,
    private val batchSize: Int = BATCH_SIZE,
) {
    suspend fun write(weights: List<ImportedWeight>, from: Int = 0, onProgress: (saved: Int) -> Unit = {}): WriteResult {
        var saved = from
        for (batch in weights.drop(from).chunked(batchSize)) {
            try {
                insert(batch)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                return WriteResult.Failed(saved, e)
            }
            saved += batch.size
            onProgress(saved)
        }
        return WriteResult.Done(saved)
    }

    companion object {
        const val BATCH_SIZE = 200
    }
}
