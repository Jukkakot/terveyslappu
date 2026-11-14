package fi.jukkakot.terveyslappu.ui

import fi.jukkakot.terveyslappu.data.DataFormat
import fi.jukkakot.terveyslappu.weight.HistoryAccess
import org.junit.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class MainUiStateTest {
    private val t0 = Instant.parse("2026-11-08T07:00:00Z")

    @Test
    fun `every load stamps the time the charts take their now from`() {
        val first = MainUiState(loadFailed = true)
        assertNull(first.loadedAt)
        val a = first.loaded(emptyList(), HistoryAccess.GRANTED, t0)
        assertEquals(t0, a.loadedAt)
        assertFalse(a.loadFailed)
        val b = a.loaded(emptyList(), HistoryAccess.NOT_GRANTED, t0.plusSeconds(3600))
        assertEquals(t0.plusSeconds(3600), b.loadedAt)
        assertEquals(HistoryAccess.NOT_GRANTED, b.history)
    }

    @Test
    fun `every export format has its own pending action`() {
        for (format in DataFormat.entries) assertEquals(format, AfterHistory.export(format).format)
    }
}
