package fi.jukkakot.terveyslappu.burn

import fi.jukkakot.terveyslappu.chart.Period
import fi.jukkakot.terveyslappu.steps.GarminDay
import fi.jukkakot.terveyslappu.steps.GarminMerge
import fi.jukkakot.terveyslappu.steps.StepsSourceLabel
import fi.jukkakot.terveyslappu.steps.StepsSourceState
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Made-up kcal only (NFR-1). */
class BurnSeriesTest {
    private val today = LocalDate.of(2026, 11, 15) // a Sunday
    private fun d(n: Long) = today.minusDays(n)

    @Test
    fun `split, total-only and active-only days`() {
        val split = BurnDay.of(d(1), 2_300, 450)!!
        assertEquals(BurnKind.SPLIT, split.kind)
        assertEquals(1_850L, split.rest)
        val total = BurnDay.of(d(1), 2_200, null)!!
        assertEquals(BurnKind.TOTAL, total.kind)
        assertNull(total.rest)
        assertNull(total.active)
        val active = BurnDay.of(d(1), null, 300)!!
        assertEquals(BurnKind.ACTIVE, active.kind)
        assertEquals(300L, active.total)
        assertNull(active.rest)
        assertNull(BurnDay.of(d(1), null, null))
        assertNull(BurnDay.of(d(1), 0, 0))
    }

    @Test
    fun `total below active gives total = active and lepo 0`() {
        val day = BurnDay.of(d(1), 300, 450)!!
        assertEquals(450L, day.total)
        assertEquals(0L, day.rest)
        assertTrue(BurnDay.invalid(300, 450))
        assertFalse(BurnDay.invalid(2_300, 450))
    }

    @Test
    fun `a week, today drawn and counted but out of the averages`() {
        val days = (1L..6L).map { BurnDay.of(d(it), 2_300, 400)!! } + BurnDay.of(today, 900, 100)!!
        val series = BurnSeries.of(days, Period.Last(7), today)
        assertFalse(series.weekly)
        assertEquals(7, series.bars.size)
        assertEquals(2_300L, series.stats.averageTotal)
        assertEquals(400L, series.stats.averageActive)
        assertEquals(7, series.stats.dataDays)
        assertEquals(7, series.stats.periodDays)
        assertEquals(today, series.days.first().day)
    }

    @Test
    fun `a gap is no bar and the active average counts days with an active value`() {
        val days = listOf(BurnDay.of(d(1), 2_000, 500)!!, BurnDay.of(d(3), 2_400, null)!!)
        val series = BurnSeries.of(days, Period.Last(7), today)
        assertEquals(2, series.bars.size)
        assertEquals(2_200L, series.stats.averageTotal)
        assertEquals(500L, series.stats.averageActive)
        assertEquals(2, series.stats.dataDays)
    }

    @Test
    fun `weekly bars average each part over its own days, today left out`() {
        // Mon 9.11. .. Sun 15.11. (today); Mon 2.11. week has a kokonais day and a split day.
        val days = listOf(
            BurnDay.of(LocalDate.of(2026, 11, 2), 2_000, null)!!,
            BurnDay.of(LocalDate.of(2026, 11, 3), 2_400, 600)!!,
            BurnDay.of(LocalDate.of(2026, 11, 10), 2_100, 300)!!,
            BurnDay.of(today, 500, 50)!!,
            BurnDay.of(LocalDate.of(2026, 5, 1), 1_900, null)!!,
        )
        val series = BurnSeries.of(days, Period.Last(365), today)
        assertTrue(series.weekly)
        val w1 = series.bars.first { it.from == LocalDate.of(2026, 11, 2) }
        assertEquals(2_200L, w1.total)
        assertEquals(600L, w1.active)
        assertEquals(1_800L, w1.rest)
        assertEquals(BurnKind.SPLIT, w1.kind)
        val w2 = series.bars.first { it.from == LocalDate.of(2026, 11, 9) }
        assertEquals(2_100L, w2.total)
        assertEquals(BurnKind.TOTAL, series.bars.first().kind)
    }

    private val garmin = StepsSourceState(setOf(GarminMerge.GARMIN), "garmin", StepsSourceLabel.App(GarminMerge.GARMIN, false))
    private val phone = StepsSourceState(setOf("android"), "phone", StepsSourceLabel.App("phone", false))

    @Test
    fun `the import fills before Health Connect and the larger total wins with its split`() {
        val imported = listOf(
            GarminDay(LocalDate.of(2023, 7, 18), 8_000, 400, 1_800),
            GarminDay(d(2), 5_000, 300, 1_800),
            GarminDay(d(3), 5_000, 300, 1_800),
            GarminDay(d(4), 5_000),
            GarminDay(today, 100, 50, 900),
        )
        val hc = listOf(BurnDay.of(d(2), 2_000, null)!!, BurnDay.of(d(3), 2_500, 700)!!, BurnDay.of(today, 800, 40)!!)
        val merged = BurnMerge.merge(hc, imported, garmin, today)
        assertEquals(LocalDate.of(2023, 7, 18), merged.days.first().day)
        assertEquals(BurnDay(d(2), 2_100, 300, BurnKind.SPLIT), merged.days.first { it.day == d(2) })
        assertEquals(2_500L, merged.days.first { it.day == d(3) }.total)
        assertEquals(800L, merged.days.first { it.day == today }.total)
        assertTrue(merged.days.none { it.day == d(4) })
        assertEquals(2, merged.imported)
        assertEquals(LocalDate.of(2023, 7, 18), BurnSeries.of(merged.days, Period.All, today).from)
    }

    @Test
    fun `another source ignores the import`() {
        val merged = BurnMerge.merge(emptyList(), listOf(GarminDay(d(2), 5_000, 300, 1_800)), phone, today)
        assertTrue(merged.days.isEmpty())
        assertEquals(0, merged.imported)
    }

    @Test
    fun `home card, today and the average without today`() {
        val days = (1L..6L).map { BurnDay.of(d(it), 2_300, 450)!! } + BurnDay.of(today, 900, 100)!!
        val home = HomeBurn.of(days, 7, today)
        assertEquals(900L, home.today!!.total)
        assertEquals(2_300L, home.averageTotal)
        assertEquals(7, home.bars.size)
        assertNull(HomeBurn.of(days.dropLast(1), 7, today).today)
    }
}
