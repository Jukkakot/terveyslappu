package fi.jukkakot.terveyslappu.history

import fi.jukkakot.terveyslappu.weight.importClientId
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertIs

class HistoryWriterTest {
    private val weights = (0 until 450).map { ImportedWeight(Instant.ofEpochSecond(1_700_000_000L + it * 86_400L), 80.0, it + 2) }

    @Test
    fun `writes in batches and reports progress`() = runTest {
        val batches = mutableListOf<Int>()
        val progress = mutableListOf<Int>()
        val result = HistoryWriter({ batches += it.size }).write(weights) { progress += it }
        assertEquals(listOf(200, 200, 50), batches)
        assertEquals(listOf(200, 400, 450), progress)
        assertEquals(WriteResult.Done(450), result)
    }

    @Test
    fun `failure reports saved count and retry continues from there`() = runTest {
        var calls = 0
        val written = mutableListOf<ImportedWeight>()
        val writer = HistoryWriter({ batch ->
            calls++
            if (calls == 2) throw IOException("made-up failure")
            written += batch
        })
        val failed = writer.write(weights)
        assertIs<WriteResult.Failed>(failed)
        assertEquals(200, failed.saved)
        assertEquals(WriteResult.Done(450), writer.write(weights, from = failed.saved))
        assertEquals(weights, written)
    }

    @Test
    fun `import ids are one per second, so a re-import is an upsert`() {
        val t = Instant.parse("2024-03-01T05:12:00.400Z")
        assertEquals(importClientId(t), importClientId(t.plusMillis(500)))
        assertEquals("terveyslappu-import-${t.epochSecond}", importClientId(t))
    }
}
