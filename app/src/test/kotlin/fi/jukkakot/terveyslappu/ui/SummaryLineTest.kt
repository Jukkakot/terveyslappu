package fi.jukkakot.terveyslappu.ui

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SummaryLineTest {
    private val tone = Color(0xFF123456)

    @Test
    fun treatPartAloneTakesTheColour() {
        val line = summaryLine("1 760 kcal", "herkkuja 230 kcal", "1 ilman kcal", tone)
        assertEquals("1 760 kcal · herkkuja 230 kcal · 1 ilman kcal", line.text)
        assertEquals(1, line.spanStyles.size)
        val span = line.spanStyles.single()
        assertEquals(tone, span.item.color)
        assertEquals("herkkuja 230 kcal", line.text.substring(span.start, span.end))
    }

    @Test
    fun noTreatMeansNoSpan() {
        val line = summaryLine(null, null, "1 ilman kcal", tone)
        assertEquals("1 ilman kcal", line.text)
        assertTrue(line.spanStyles.isEmpty())
    }
}
