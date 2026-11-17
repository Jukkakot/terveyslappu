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

/** Marker card texts (weight-chart "Crosshair on every Historia chart", "Point details"); made-up values (NFR-1). */
class TabReadoutsTest {
    private val zone = ZoneId.of("Europe/Helsinki")
    private val today = LocalDate.of(2026, 11, 15)

    /** Finnish number formats use no-break spaces; compare with plain ones. */
    private fun plain(s: String) = s.replace(" ", " ").replace(" ", " ")

    /** The card as "head | name value | name value". */
    private fun text(card: ReadoutCard) = plain((listOf(card.head) + card.rows.map { "${it.name} ${it.value}" }).joinToString(" | "))

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
    fun `Paino has the time in the head and the day's last weight, or ei painoa`() {
        val day = LocalDate.of(2024, 9, 15)
        val entries = listOf(
            weightEntry("a", day.atTime(7, 10).atZone(zone).toInstant(), null, 97.9, "me", "me"),
            weightEntry("b", day.atTime(8, 33).atZone(zone).toInstant(), null, 97.6, "me", "me"),
        )
        val data = ChartData.of(Period.All, entries, today.atStartOfDay(zone).toInstant(), zone)!!
        val kg = { v: Double -> "${String.format(java.util.Locale.forLanguageTag("fi"), "%.1f", v)} kg" }
        assertEquals("Su 15.9.2024 · 8.33 | paino 97,6 kg", text(TabReadouts.weight(data, day, zone, today, label = kg)))
        assertEquals("Ma 16.9.2024 | paino ei painoa", text(TabReadouts.weight(data, day.plusDays(1), zone, today, label = kg)))
        assertEquals("Su 15.9.2024 · 8.33 | BMI 30,1", text(TabReadouts.weight(data, day, zone, today, name = TabReadouts.BMI) { "30,1" }))
    }

    private fun food(day: LocalDate, kcal: Double, treat: Boolean = false, pos: Int = 1): FoodItem {
        val t = day.atTime(12, 0).atZone(zone).toInstant()
        return FoodItem("$day-$pos", day, pos, "Ruoka", kcal, Amount(1.0, AmountUnit.KPL), treat, t, zone.rules.getOffset(t), true)
    }

    @Test
    fun `Syoty has ruoka and herkut rows, a week its average`() {
        val day = LocalDate.of(2026, 11, 12)
        val daily = EnergySeries.of(listOf(food(day, 1530.0), food(day, 230.0, treat = true, pos = 2)), Period.WEEK, today)
        assertEquals("To 12.11. | syöty 1 760 kcal | herkut 230 kcal", text(TabReadouts.energy(daily, day, today)))
        assertEquals("Pe 13.11. | syöty ei annoksia", text(TabReadouts.energy(daily, day.plusDays(1), today)))
        val weekly = EnergySeries.of(listOf(food(LocalDate.of(2026, 11, 10), 1800.0), food(day, 2000.0)), Period.Last(365), today)
        assertEquals("Vk 46 · 9.–15.11. | syöty keskim. 1 900 kcal/pv", text(TabReadouts.energy(weekly, LocalDate.of(2026, 11, 9), today)))
        // Any day of the week (the key is the week's first day inside the period) finds its bar.
        assertEquals("Vk 46 · 9.–15.11. | syöty keskim. 1 900 kcal/pv", text(TabReadouts.energy(weekly, LocalDate.of(2026, 11, 11), today)))
        assertEquals("Vk 45 · 2.–8.11. | syöty ei annoksia", text(TabReadouts.energy(weekly, LocalDate.of(2026, 11, 2), today)))
    }

    @Test
    fun `Askeleet has steps or ei tietoa, a week its average`() {
        val day = LocalDate.of(2026, 11, 12)
        val daily = StepsSeries.of(listOf(StepsDay(day, 9202)), Period.WEEK, today)
        assertEquals("To 12.11. | askeleet 9 202 askelta", text(TabReadouts.steps(daily, day, today)))
        assertEquals("Ke 11.11. | askeleet ei tietoa", text(TabReadouts.steps(daily, day.minusDays(1), today)))
        val weekly = StepsSeries.of(listOf(StepsDay(LocalDate.of(2026, 11, 10), 8000), StepsDay(day, 8400)), Period.Last(365), today)
        assertEquals("Vk 46 · 9.–15.11. | askeleet keskim. 8 200 askelta/pv", text(TabReadouts.steps(weekly, LocalDate.of(2026, 11, 9), today)))
    }

    @Test
    fun `Kulutus has one row - split, kokonais, active only or no data`() {
        val day = LocalDate.of(2026, 11, 12)
        val series = BurnSeries.of(
            listOfNotNull(BurnDay.of(day, 2946, 465), BurnDay.of(day.minusDays(1), 2300, null), BurnDay.of(day.minusDays(3), null, 465)),
            Period.WEEK,
            today,
        )
        assertEquals("To 12.11. | kulutus 2 946 kcal · aktiivinen 465 kcal · lepo 2 481 kcal", text(TabReadouts.burn(series, day, today)))
        assertEquals("Ke 11.11. | kulutus 2 300 kcal", text(TabReadouts.burn(series, day.minusDays(1), today)))
        assertEquals("Ma 9.11. | kulutus 465 kcal · aktiivinen 465 kcal", text(TabReadouts.burn(series, day.minusDays(3), today)))
        assertEquals("Ti 10.11. | kulutus ei tietoa", text(TabReadouts.burn(series, day.minusDays(2), today)))
    }

    @Test
    fun `own chart rows name each data with its value or what is missing`() {
        val day = LocalDate.of(2026, 11, 12)
        val readout = Readout(
            day,
            listOf(
                SeriesKind.WEIGHT to DayValue(day, 82.4),
                SeriesKind.ENERGY to null,
                SeriesKind.BURN to DayValue(day, 2300.0, active = 450.0),
                SeriesKind.STEPS to null,
            ),
        )
        assertEquals(
            "To 12.11. | paino 82,4 kg | syöty ei annoksia | kulutus 2 300 kcal · aktiivinen 450 kcal | askeleet ei tietoa",
            text(TabReadouts.custom(readout, today) { ToneRef.PRIMARY }),
        )
    }

    @Test
    fun `the announced text joins the head and rows`() {
        val card = ReadoutCard("Su 8.11. · 7.05", listOf(ReadoutRow(ToneRef.PRIMARY, "paino", "82,6 kg")))
        assertEquals("Su 8.11. · 7.05, paino 82,6 kg", card.text)
    }
}
