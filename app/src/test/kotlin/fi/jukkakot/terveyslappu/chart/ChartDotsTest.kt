package fi.jukkakot.terveyslappu.chart

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChartDotsTest {
    @Test
    fun `dots fit with ten dp per weight`() {
        assertTrue(ChartDots.fit(7, 300f))
        assertTrue(ChartDots.fit(30, 300f))
        assertFalse(ChartDots.fit(31, 300f))
        assertFalse(ChartDots.fit(90, 300f))
        assertTrue(ChartDots.fit(1, 5f))
    }
}
