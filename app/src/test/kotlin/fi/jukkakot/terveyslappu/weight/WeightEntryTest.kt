package fi.jukkakot.terveyslappu.weight

import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WeightEntryTest {
    private val own = "fi.jukkakot.terveyslappu"
    private val time = Instant.parse("2026-11-08T05:30:00Z")

    @Test
    fun `own records are recognised by package`() {
        val mine = weightEntry("a", time, ZoneOffset.ofHours(2), 82.4, own, own)
        val garmin = weightEntry("b", time, null, 81.9, "com.garmin.android.apps.connectmobile", own)
        assertTrue(mine.own)
        assertFalse(garmin.own)
        assertEquals(82.4, mine.kg)
        assertEquals("com.garmin.android.apps.connectmobile", garmin.sourcePackage)
    }

    @Test
    fun `list is newest first`() {
        val older = weightEntry("old", time.minusSeconds(86_400), null, 83.0, own, own)
        val newer = weightEntry("new", time, null, 82.0, own, own)
        assertEquals(listOf("new", "old"), listOf(older, newer).newestFirst().map { it.id })
    }
}
