package fi.jukkakot.terveyslappu.derived

import fi.jukkakot.terveyslappu.chart.ChartData
import fi.jukkakot.terveyslappu.chart.KgRange
import fi.jukkakot.terveyslappu.chart.Period
import fi.jukkakot.terveyslappu.chart.periodStats
import fi.jukkakot.terveyslappu.profile.Gender
import fi.jukkakot.terveyslappu.profile.OwnerProfile
import fi.jukkakot.terveyslappu.weight.weightEntry
import java.time.Instant
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Made-up owner details and weights only (NFR-1). */
class DerivedTest {
    private val zone = ZoneId.of("Europe/Helsinki")
    private val now = Instant.parse("2026-11-09T06:00:00Z")
    private fun at(daysAgo: Long, kg: Double) = weightEntry("$daysAgo", now.minusSeconds(daysAgo * 86_400), null, kg, "me", "me")
    private val man = OwnerProfile(birthYear = 1990, heightCm = 180, gender = Gender.MALE)

    @Test
    fun `bmi from height`() {
        assertEquals(25.3, Derived.shownBmi(Derived.bmi(82.0, 180)))
        assertEquals(25.3, Quantity.BMI.valueOf(at(0, 82.0), man, zone)!!, 0.05)
    }

    @Test
    fun `no height gives no bmi and names the height`() {
        val profile = OwnerProfile(birthYear = 1990)
        assertNull(Quantity.BMI.valueOf(at(0, 82.0), profile, zone))
        assertEquals(Availability.Missing(listOf(Field.HEIGHT)), Quantity.BMI.availability(profile, 2026))
    }

    @Test
    fun `classes and tones at the borders`() {
        assertEquals(BmiClass.UNDERWEIGHT to Tone.RED, Derived.bmiClass(16.9) to Derived.tone(16.9))
        assertEquals(BmiClass.UNDERWEIGHT to Tone.YELLOW, Derived.bmiClass(17.0) to Derived.tone(17.0))
        assertEquals(BmiClass.NORMAL to Tone.GREEN, Derived.bmiClass(18.5) to Derived.tone(18.5))
        assertEquals(BmiClass.NORMAL to Tone.GREEN, Derived.bmiClass(22.0) to Derived.tone(22.0))
        assertEquals(BmiClass.OVERWEIGHT to Tone.YELLOW, Derived.bmiClass(25.0) to Derived.tone(25.0))
        assertEquals(BmiClass.OVERWEIGHT to Tone.YELLOW, Derived.bmiClass(25.3) to Derived.tone(25.3))
        assertEquals(BmiClass.OBESE to Tone.RED, Derived.bmiClass(30.0) to Derived.tone(30.0))
        assertEquals(BmiClass.OBESE to Tone.RED, Derived.bmiClass(31.0) to Derived.tone(31.0))
        assertEquals(BmiClass.SEVERE to Tone.RED, Derived.bmiClass(35.0) to Derived.tone(35.0))
        assertEquals(BmiClass.MORBID to Tone.RED, Derived.bmiClass(40.0) to Derived.tone(40.0))
        // Classified as shown: 24.96 reads 25,0 and so is ylipaino.
        assertEquals(BmiClass.OVERWEIGHT, Derived.bmiClass(24.96))
    }

    @Test
    fun `normal range for 180 cm`() {
        assertEquals(60..81, Derived.normalRange(180))
    }

    @Test
    fun `bmr for a man and a woman`() {
        assertEquals(1770, Derived.shownBmr(Quantity.BMR.valueOf(at(0, 82.0), man, zone)!!))
        val woman = OwnerProfile(birthYear = 1990, heightCm = 165, gender = Gender.FEMALE)
        assertEquals(1290, Derived.shownBmr(Quantity.BMR.valueOf(at(0, 60.0), woman, zone)!!))
    }

    @Test
    fun `bmr uses the age in the weight's year`() {
        val old = at(365 * 2, 82.0) // 2024: age 34, 10 kcal more than in 2026
        assertEquals(1780, Derived.shownBmr(Quantity.BMR.valueOf(old, man, zone)!!))
    }

    @Test
    fun `gender not chosen leaves out bmr but not bmi`() {
        val profile = man.copy(gender = null)
        assertNull(Quantity.BMR.valueOf(at(0, 82.0), profile, zone))
        assertEquals(Availability.Missing(listOf(Field.GENDER)), Quantity.BMR.availability(profile, 2026))
        assertEquals(Availability.Ok, Quantity.BMI.availability(profile, 2026))
    }

    @Test
    fun `several missing are all named`() {
        val profile = OwnerProfile(heightCm = 180)
        assertEquals(Availability.Missing(listOf(Field.BIRTH_YEAR, Field.GENDER)), Quantity.BMR.availability(profile, 2026))
        assertEquals(
            Availability.Missing(listOf(Field.HEIGHT, Field.BIRTH_YEAR, Field.GENDER)),
            Quantity.BMR.availability(OwnerProfile(), 2026),
        )
    }

    @Test
    fun `under 18 keeps the bmi number but no class, range or bmr`() {
        val teen = OwnerProfile(birthYear = 2012, heightCm = 160, gender = Gender.FEMALE)
        val row = LatestDerived.of(at(0, 50.0), teen, zone)
        assertTrue(row.bmi != null)
        assertNull(row.bmiClass)
        assertNull(row.tone)
        assertNull(row.normalRange)
        assertEquals(Availability.Minor, row.rangeState)
        assertNull(row.bmr)
        assertEquals(Availability.Minor, row.bmrState)
    }

    @Test
    fun `home row with all details`() {
        val row = LatestDerived.of(at(0, 82.0), man, zone)
        assertEquals(25.3, Derived.shownBmi(row.bmi!!))
        assertEquals(BmiClass.OVERWEIGHT, row.bmiClass)
        assertEquals(Tone.YELLOW, row.tone)
        assertEquals(60..81, row.normalRange)
        assertEquals(1770, row.bmr)
    }

    @Test
    fun `home row without details names what is missing`() {
        val row = LatestDerived.of(at(0, 82.0), OwnerProfile(), zone)
        assertNull(row.bmi)
        assertEquals(Availability.Missing(listOf(Field.HEIGHT)), row.bmiState)
        assertEquals(Availability.Missing(listOf(Field.HEIGHT)), row.rangeState)
        assertEquals(Availability.Missing(listOf(Field.HEIGHT, Field.BIRTH_YEAR, Field.GENDER)), row.bmrState)
    }

    @Test
    fun `bmi stats have a change but no rate`() {
        val entries = listOf(at(28, 83.0), at(0, 82.0))
        val stats = periodStats(entries, { Quantity.BMI.valueOf(it, man, zone) }, rate = false)!!
        assertEquals(25.3, Derived.shownBmi(stats.latest))
        assertEquals(-0.3, Math.round(stats.change!! * 10) / 10.0)
        assertNull(stats.perWeek)
    }

    @Test
    fun `bmr series in the chart`() {
        val entries = listOf(at(2, 82.0), at(1, 82.5), at(0, 82.0))
        val data = ChartData.of(Period.WEEK, entries, now, zone, { Quantity.BMR.valueOf(it, man, zone) }, { Quantity.BMR.scale(it, man) })!!
        assertEquals(listOf(1770.0, 1775.0, 1770.0), data.ys.map { Math.round(it).toDouble() })
        assertEquals(0.0, data.minY % 10)
        assertTrue(data.maxY - data.minY >= 40)
    }

    @Test
    fun `steady bmi scale spans at least 1,2`() {
        val range = Quantity.BMI.scale(listOf(25.2, 25.3), man)
        assertTrue(range.span >= 1.2)
        assertEquals(KgRange(24.0, 26.0), range)
    }

    @Test
    fun `steady bmr scale spans at least 40 kcal in tens`() {
        assertEquals(KgRange(1750.0, 1800.0), Quantity.BMR.scale(listOf(1770.0, 1780.0), man))
    }
}
