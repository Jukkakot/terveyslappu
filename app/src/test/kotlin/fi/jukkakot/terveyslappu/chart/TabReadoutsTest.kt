package fi.jukkakot.terveyslappu.chart

import fi.jukkakot.terveyslappu.burn.BurnDay
import fi.jukkakot.terveyslappu.burn.BurnSeries
import fi.jukkakot.terveyslappu.food.Amount
import fi.jukkakot.terveyslappu.food.AmountUnit
import fi.jukkakot.terveyslappu.food.EnergySeries
import fi.jukkakot.terveyslappu.food.FoodItem
import fi.jukkakot.terveyslappu.steps.StepsDay
import fi.jukkakot.terveyslappu.steps.StepsSeries
import fi.jukkakot.terveyslappu.weight.weightEntry
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals

/** Crosshair readout texts (weight-chart "Crosshair on every Historia chart"); made-up values (NFR-1). */
class TabReadoutsTest {
    private val zone = ZoneId.of("Europe/Helsinki")
    private val today = LocalDate.of(2026, 11, 15)

    /** Finnish number formats use no-break spaces; compare with plain ones. */
    private fun plain(s: String) = s.replace("\u00A0", " ").replace("\u202F", " ")

    @Test
    fun `the day head has the weekday and the year only when not this year`() {
        assertEquals("Ma 9.11.", TabReadouts.day(LocalDate.of(2026, 11, 9), today))
        assertEquals("Su 15.9.2024", TabReadouts.day(LocalDate.of(2024, 9, 15), today))
    }

    @Test
    fun `the week head names the week and its days`() {
        assertEquals("Vk 46 · 9.–15.11.", TabReadouts.week(LocalDate.of(2026, 11, 9), today))
        assertEquals("Vk 49 · 30.11.–6.12.", TabReadouts.week(LocalDate.of(2026, 11, 30), today))
        assertEquals("Vk 1 · 30.12.–5.1.2025", TabReadouts.week(LocalDate.of(2024, 12, 30), today))
    }

    @Test
    fun `weight shows the day's last weight with its time or ei painoa`() {
        val day = LocalDate.of(2024, 9, 15)
        val entries = listOf(
            weightEntry("a", day.atTime(7, 10).atZone(zone).toInstant(), null, 97.9, "me", "me"),
            weightEntry("b", day.atTime(8, 33).atZone(zone).toInstant(), null, 97.6, "me", "me"),
        )
        val data = ChartData.of(Period.All, entries, today.atStartOfDay(zone).toInstant(), zone)!!
        assertEquals("Su 15.9.2024 · 8.33 · 97,6 kg", TabReadouts.weight(data, day, zone, today) { "${String.format(java.util.Locale.forLanguageTag("fi"), "%.1f", it)} kg" })
        assertEquals("Ma 16.9.2024 · ei painoa", TabReadouts.weight(data, day.plusDays(1), zone, today) { "$it" })
    }

    private fun food(day: LocalDate, kcal: Double, treat: Boolean = false, pos: Int = 1): FoodItem {
        val t = day.atTime(12, 0).atZone(zone).toInstant()
        return FoodItem("$day-$pos", day, pos, "Ruoka", kcal, Amount(1.0, AmountUnit.KPL), treat, t, zone.rules.getOffset(t), true)
    }

    @Test
    fun `Syoty shows the day's kcal and treats, a week its average`() {
        val day = LocalDate.of(2026, 11, 12)
        val daily = EnergySeries.of(listOf(food(day, 1530.0), food(day, 230.0, treat = true, pos = 2)), Period.WEEK, today)
        assertEquals("To 12.11. · 1 760 kcal · herkut 230 kcal", plain(TabReadouts.energy(daily, day, today)))
        assertEquals("Pe 13.11. · ei kirjauksia", TabReadouts.energy(daily, day.plusDays(1), today))
        val weekly = EnergySeries.of(listOf(food(LocalDate.of(2026, 11, 10), 1800.0), food(day, 2000.0)), Period.Last(365), today)
        assertEquals("Vk 46 · 9.–15.11. · keskim. 1 900 kcal/pv", plain(TabReadouts.energy(weekly, LocalDate.of(2026, 11, 9), today)))
        assertEquals("Vk 45 · 2.–8.11. · ei kirjauksia", TabReadouts.energy(weekly, LocalDate.of(2026, 11, 2), today))
    }

    @Test
    fun `Askeleet shows steps or ei tietoa, a week its average`() {
        val day = LocalDate.of(2026, 11, 12)
        val daily = StepsSeries.of(listOf(StepsDay(day, 9202)), Period.WEEK, today)
        assertEquals("To 12.11. · 9 202 askelta", plain(TabReadouts.steps(daily, day, today)))
        assertEquals("Ke 11.11. · ei tietoa", TabReadouts.steps(daily, day.minusDays(1), today))
        val weekly = StepsSeries.of(listOf(StepsDay(LocalDate.of(2026, 11, 10), 8000), StepsDay(day, 8400)), Period.Last(365), today)
        assertEquals("Vk 46 · 9.–15.11. · keskim. 8 200 askelta/pv", plain(TabReadouts.steps(weekly, LocalDate.of(2026, 11, 9), today)))
    }

    @Test
    fun `Kulutus shows total, active and lepo`() {
        val day = LocalDate.of(2026, 11, 12)
        val series = BurnSeries.of(listOfNotNull(BurnDay.of(day, 2300, 450), BurnDay.of(day.minusDays(1), 2100, null)), Period.WEEK, today)
        assertEquals("To 12.11. · 2 300 kcal · aktiivinen 450 · lepo 1 850", plain(TabReadouts.burn(series, day, today)))
        assertEquals("Ke 11.11. · 2 100 kcal", plain(TabReadouts.burn(series, day.minusDays(1), today)))
        assertEquals("Ti 10.11. · ei tietoa", TabReadouts.burn(series, day.minusDays(2), today))
    }
}
