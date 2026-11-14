package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.backup.BackupStatus
import fi.jukkakot.terveyslappu.ui.MainUiState
import fi.jukkakot.terveyslappu.weight.weightEntry
import java.time.Instant
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** "?" weights in the list, the home card and the chart's input; made-up weights (NFR-1). */
class QuestionedDataTest {
    private val zone = ZoneId.of("Europe/Helsinki")
    private val now = Instant.parse("2026-11-09T07:05:00Z")
    private fun weight(id: String, daysAgo: Long, kg: Double) = weightEntry(id, now.minusSeconds(daysAgo * 86_400), null, kg, "me", "me")

    private val list = listOf(weight("a", 2, 82.0), weight("q", 1, 61.3), weight("b", 0, 82.4))

    @Test
    fun `the list shows the questioned weight without a change and the next change skips it`() {
        val nodes = ListGroups.build(list, zone, questioned = setOf("q")).filterIsInstance<ListNode.Weight>()
        val byId = nodes.associateBy { it.entry.id }
        assertTrue(byId.getValue("q").questioned)
        assertNull(byId.getValue("q").change)
        assertEquals(0.4, byId.getValue("b").change!!, 1e-9)
        assertFalse(byId.getValue("b").questioned)
    }

    @Test
    fun `a group counts a questioned weight but never uses it as first or last`() {
        val month = (0L..20L).map { weight("w$it", it, 82.0 + it / 10.0) } + weight("q", 0, 61.3).copy(time = now.plusSeconds(60))
        val group = ListGroups.build(month, zone, flatMax = 5, questioned = setOf("q")).filterIsInstance<ListNode.Group>().first()
        assertEquals(22, month.size)
        assertTrue(group.count >= 2)
        assertEquals("w0", group.last!!.id)
        assertEquals(group.count - 1, group.acceptedCount)
        val path = ListGroups.pathTo(ListGroups.build(month, zone, flatMax = 5, questioned = setOf("q")), "q")!!
        assertEquals("m2026-11", path.first())
    }

    @Test
    fun `the home card skips the questioned weight, the three latest include it`() {
        val summary = HomeSummary.of(list.reversed(), now, questioned = setOf("q"))!!
        assertEquals("b", summary.latest.id)
        assertEquals(0.4, summary.change!!, 1e-9)
        assertEquals(listOf("a", "b"), summary.spark.map { it.id })
        assertEquals(listOf("b", "q", "a"), summary.recent.map { it.id })
        assertEquals(KgRange(80.0, 85.0), summary.sparkScale)
    }

    @Test
    fun `charts, stats and the scale use the accepted weights only`() {
        val state = MainUiState(weights = list, questioned = setOf("q"))
        assertEquals(listOf("a", "b"), state.accepted!!.map { it.id })
        assertEquals("q", state.newestQuestioned!!.id)
        val stats = periodStats(state.accepted!!)!!
        assertEquals(82.0, stats.min)
        val data = ChartData.of(Period.WEEK, state.accepted!!, now, zone)!!
        assertEquals(2, data.entries.size)
        assertEquals(80.0, data.minY)
        assertNull(MainUiState(weights = list).newestQuestioned)
    }

    @Test
    fun `a change made with the app closed backs up at the next start whatever the day`() {
        val today = BackupStatus(uri = "content://x", lastOkAt = now.minusSeconds(3_600))
        assertFalse(today.isDue(now, zone))
        assertTrue(today.isDue(now, zone, changedWhileClosed = true))
        assertFalse(BackupStatus().isDue(now, zone, changedWhileClosed = true))
    }
}
