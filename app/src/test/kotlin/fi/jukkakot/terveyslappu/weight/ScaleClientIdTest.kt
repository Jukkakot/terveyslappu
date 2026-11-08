package fi.jukkakot.terveyslappu.weight

import org.junit.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ScaleClientIdTest {
    @Test
    fun `same reading second gives the same id, so a rewrite is an upsert`() {
        val t = Instant.parse("2026-11-08T05:30:12.100Z")
        assertEquals(scaleClientId(t), scaleClientId(t.plusMillis(800)))
        assertNotEquals(scaleClientId(t), scaleClientId(t.plusSeconds(1)))
    }
}
