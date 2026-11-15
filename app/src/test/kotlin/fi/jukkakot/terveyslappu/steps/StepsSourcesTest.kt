package fi.jukkakot.terveyslappu.steps

import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Made-up step totals only (NFR-1). */
class StepsSourcesTest {
    private val today = LocalDate.of(2026, 11, 15)
    private val garmin = "com.garmin.android.apps.connectmobile"
    private val spn = "com.android.healthconnect.phone.abc123"

    private fun total(id: String, total: Long, days: Int = 30, avg: Long? = null) =
        StepsAppTotal(StepsApp(id, setOf(id)), total, days, avg)

    @Test
    fun `phone origins group into one app, others stay separate`() {
        val apps = StepsSourceRules.group(setOf("android", spn, "com.android.healthconnect.phone.other", garmin, "nl.appyhapps.healthsync"), spn)
        assertEquals(StepsApp("phone", setOf("android", spn, "com.android.healthconnect.phone.other")), apps.first())
        assertEquals(listOf(garmin, "nl.appyhapps.healthsync"), apps.drop(1).map { it.id })
        assertEquals(listOf(StepsApp(garmin, setOf(garmin))), StepsSourceRules.group(setOf(garmin), null))
        assertEquals(setOf("android", "x.spn"), StepsSourceRules.group(setOf("android", "x.spn"), "x.spn").single().packages)
    }

    @Test
    fun `most steps wins`() {
        assertEquals(garmin, StepsSourceRules.pick(listOf(total(garmin, 240_000), total("phone", 90_000)), null))
    }

    @Test
    fun `a near tie keeps the current app, a clear overtake switches`() {
        assertEquals("phone", StepsSourceRules.pick(listOf(total(garmin, 210_000), total("phone", 200_000)), "phone"))
        assertEquals(garmin, StepsSourceRules.pick(listOf(total(garmin, 150_000), total("phone", 100_000)), "phone"))
    }

    @Test
    fun `ties go to the lowest id, nothing gives null, an absent current counts as zero`() {
        assertEquals("a.app", StepsSourceRules.pick(listOf(total("b.app", 1000), total("a.app", 1000)), null))
        assertNull(StepsSourceRules.pick(emptyList(), "phone"))
        assertNull(StepsSourceRules.pick(listOf(total(garmin, 0)), null))
        assertEquals(garmin, StepsSourceRules.pick(listOf(total(garmin, 1000)), "gone.app"))
    }

    @Test
    fun `list order follows the totals while the averages can disagree`() {
        val phoneDays = (0 until 12).map { StepsDay(today.minusDays(1L + it), 10_000) }
        val garminDays = (0 until 30).map { StepsDay(today.minusDays(1L + it), 5_000) }
        val phone = StepsSourceRules.totals(StepsApp("phone", setOf("android")), phoneDays, today)
        val watch = StepsSourceRules.totals(StepsApp(garmin, setOf(garmin)), garminDays, today)
        val listed = StepsSourceRules.listed(listOf(phone, watch), StepsChoice.Auto)
        assertEquals(listOf(garmin, "phone"), listed.map { it.app.id })
        assertEquals(listOf(5_000L, 10_000L), listed.map { it.averageExToday })
        assertEquals(listOf(150_000L, 120_000L), listed.map { it.total })
    }

    @Test
    fun `today is left out of the average but counts in the total`() {
        val days = listOf(StepsDay(today, 1_000), StepsDay(today.minusDays(1), 8_000), StepsDay(today.minusDays(2), 6_000))
        val t = StepsSourceRules.totals(StepsApp(garmin, setOf(garmin)), days, today)
        assertEquals(15_000, t.total)
        assertEquals(3, t.days)
        assertEquals(7_000, t.averageExToday)
        assertNull(StepsSourceRules.averageExToday(500, 1, 500))
        assertEquals(4_000, StepsSourceRules.averageExToday(8_000, 2, 0))
    }

    @Test
    fun `a chosen app without recent steps is listed last`() {
        val listed = StepsSourceRules.listed(listOf(total(garmin, 0), total("phone", 50_000)), StepsChoice.App(garmin))
        assertEquals(listOf("phone", garmin), listed.map { it.app.id })
        val gone = StepsSourceRules.listed(listOf(total("phone", 50_000)), StepsChoice.App("gone.app"))
        assertEquals(listOf("phone", "gone.app"), gone.map { it.app.id })
        assertEquals(listOf("phone"), StepsSourceRules.listed(listOf(total(garmin, 0), total("phone", 1)), StepsChoice.Auto).map { it.app.id })
    }

    @Test
    fun `resolve per choice`() {
        val apps = listOf(StepsAppTotal(StepsApp("phone", setOf("android", spn)), 1000, 1, null), total(garmin, 0))
        val all = StepsSourceRules.resolve(StepsChoice.All, garmin, apps, checkedToday = true)
        assertNull(all.packages)
        assertEquals("all", all.logName)

        val autoNone = StepsSourceRules.resolve(StepsChoice.Auto, null, apps, checkedToday = true)
        assertNull(autoNone.packages)
        assertEquals(StepsSourceLabel.Auto(null), autoNone.label)

        val auto = StepsSourceRules.resolve(StepsChoice.Auto, "phone", apps, checkedToday = true)
        assertEquals(setOf("android", spn), auto.packages)
        assertEquals("phone", auto.logName)

        val present = StepsSourceRules.resolve(StepsChoice.App(garmin), null, apps, checkedToday = true)
        assertEquals(setOf(garmin), present.packages)
        assertEquals(StepsSourceLabel.App(garmin, true), present.label)

        val absent = StepsSourceRules.resolve(StepsChoice.App("gone.app"), null, apps, checkedToday = true)
        assertEquals(setOf("gone.app"), absent.packages)
        assertEquals(StepsSourceLabel.App("gone.app", true), absent.label)

        val phoneUnchecked = StepsSourceRules.resolve(StepsChoice.App("phone"), null, null, checkedToday = false, savedPhone = setOf(spn))
        assertEquals(setOf("android", spn), phoneUnchecked.packages)
        assertEquals(StepsSourceLabel.App("phone", null), phoneUnchecked.label)
        assertEquals(setOf("android"), StepsSourceRules.resolve(StepsChoice.App("phone"), null, null, false).packages)

        assertNull((StepsSourceRules.resolve(StepsChoice.App(garmin), null, apps, checkedToday = false).label as StepsSourceLabel.App).noRecent)
        assertEquals(false, (StepsSourceRules.resolve(StepsChoice.App("phone"), null, apps, checkedToday = true).label as StepsSourceLabel.App).noRecent)
    }

    @Test
    fun `different sources have different keys`() {
        val keys = listOf(
            StepsSourceRules.resolve(StepsChoice.All, null, null, false),
            StepsSourceRules.resolve(StepsChoice.Auto, null, null, false),
            StepsSourceRules.resolve(StepsChoice.Auto, garmin, null, false),
            StepsSourceRules.resolve(StepsChoice.App(garmin), null, null, false),
            StepsSourceRules.resolve(StepsChoice.App("phone"), null, null, false),
        ).map { it.key }
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun `choice encode and decode`() {
        for (choice in listOf(StepsChoice.Auto, StepsChoice.All, StepsChoice.App("phone"), StepsChoice.App(garmin))) {
            assertEquals(choice, StepsChoice.decode(choice.encode()))
        }
        assertEquals(StepsChoice.Auto, StepsChoice.decode(null))
        assertEquals(StepsChoice.Auto, StepsChoice.decode(""))
        assertEquals(StepsChoice.Auto, StepsChoice.decode("app:"))
        assertEquals(StepsChoice.Auto, StepsChoice.decode("garbage"))
    }
}
