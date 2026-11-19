package fi.jukkakot.terveyslappu.auto

import fi.jukkakot.terveyslappu.weight.WeightEntry
import fi.jukkakot.terveyslappu.weight.scaleClientId
import org.junit.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Which weight the dialog shows instead of scanning (everyday-feedback design 4); made-up weights (NFR-1). */
class RecentAutoTest {
    private val now = Instant.parse("2026-11-19T05:14:00Z")

    private fun scale(secondsAgo: Long, kg: Double = 82.5): WeightEntry {
        val time = now.minusSeconds(secondsAgo)
        return WeightEntry("hc-$secondsAgo", time, null, kg, "app", own = true, clientRecordId = scaleClientId(time), fromScale = true)
    }

    private val all = { _: Long -> true }

    @Test
    fun `the newest automatic weight within two minutes is picked`() {
        val older = scale(100, 82.7)
        val newer = scale(30, 82.5)
        assertEquals(newer, RecentAuto.pick(listOf(older, newer), now, all, emptySet()))
    }

    @Test
    fun `older than two minutes is not picked`() {
        assertNull(RecentAuto.pick(listOf(scale(180)), now, all, emptySet()))
        assertEquals(120L, now.epochSecond - RecentAuto.pick(listOf(scale(120)), now, all, emptySet())!!.time.epochSecond)
    }

    @Test
    fun `a dialog weight, a questioned weight and a manual entry are not picked`() {
        val dialog = scale(30)
        assertNull(RecentAuto.pick(listOf(dialog), now, { false }, emptySet()))
        assertNull(RecentAuto.pick(listOf(dialog), now, all, setOf(dialog.clientRecordId!!)))
        val manual = dialog.copy(clientRecordId = "terveyslappu-1234", fromScale = false)
        assertNull(RecentAuto.pick(listOf(manual), now, all, emptySet()))
    }
}
