package fi.jukkakot.terveyslappu.derived

import fi.jukkakot.terveyslappu.chart.KgRange
import fi.jukkakot.terveyslappu.chart.WeightScale
import fi.jukkakot.terveyslappu.profile.Gender
import fi.jukkakot.terveyslappu.profile.OwnerProfile
import fi.jukkakot.terveyslappu.weight.WeightEntry
import java.time.ZoneId
import kotlin.math.roundToInt

/** Traffic-light tone of a BMI class (derived-values "BMI category with tone"). */
enum class Tone { GREEN, YELLOW, RED }

/** WHO adult BMI classes, lower bound inclusive; the UI gives each its Finnish word. */
enum class BmiClass(val from: Double) {
    UNDERWEIGHT(0.0), NORMAL(18.5), OVERWEIGHT(25.0), OBESE(30.0), SEVERE(35.0), MORBID(40.0);

    companion object {
        fun of(bmi: Double): BmiClass = entries.last { bmi >= it.from }
    }
}

/** A tone band of the BMI scale, [from] inclusive to [to] exclusive. */
data class ToneBand(val from: Double, val to: Double, val tone: Tone)

/** Owner details a derived value needs. */
enum class Field { HEIGHT, BIRTH_YEAR, GENDER }

/** Whether a derived value can be shown, and if not, why. */
sealed interface Availability {
    data object Ok : Availability
    /** Owner details missing, in [Field] order. */
    data class Missing(val fields: List<Field>) : Availability
    /** The owner was under 18: the adult classes and the equation do not apply. */
    data object Minor : Availability
}

/** The pure formulas behind BMI, its class, the normal range and the basal metabolic rate. */
object Derived {
    const val ADULT_AGE = 18
    private const val NORMAL_LOW = 18.5
    private const val NORMAL_HIGH = 25.0

    /** Borderline yellow below 18.5 starts here; under it is red. */
    private const val SEVERE_THIN = 17.0

    /** Tone bands across the whole BMI scale. */
    val BANDS: List<ToneBand> = listOf(
        ToneBand(0.0, SEVERE_THIN, Tone.RED),
        ToneBand(SEVERE_THIN, NORMAL_LOW, Tone.YELLOW),
        ToneBand(NORMAL_LOW, NORMAL_HIGH, Tone.GREEN),
        ToneBand(NORMAL_HIGH, 30.0, Tone.YELLOW),
        ToneBand(30.0, Double.MAX_VALUE, Tone.RED),
    )

    fun bmi(kg: Double, heightCm: Int): Double {
        val m = heightCm / 100.0
        return kg / (m * m)
    }

    /** The BMI as shown (one decimal), so the class agrees with the number on screen. */
    fun shownBmi(bmi: Double): Double = Math.round(bmi * 10) / 10.0

    fun bmiClass(bmi: Double): BmiClass = BmiClass.of(shownBmi(bmi))

    fun tone(bmi: Double): Tone {
        val shown = shownBmi(bmi)
        return BANDS.first { shown >= it.from && shown < it.to }.tone
    }

    /** Whole kilos giving BMI 18.5 and 25 at [heightCm]. */
    fun normalRange(heightCm: Int): IntRange {
        val m2 = (heightCm / 100.0).let { it * it }
        return (NORMAL_LOW * m2).roundToInt()..(NORMAL_HIGH * m2).roundToInt()
    }

    /** Mifflin-St Jeor in kcal/day, unrounded; [age] in years. */
    fun bmr(kg: Double, heightCm: Int, age: Int, gender: Gender): Double =
        10 * kg + 6.25 * heightCm - 5 * age + if (gender == Gender.MALE) 5 else -161

    /** Rounded to the nearest 10 kcal, as shown. */
    fun shownBmr(kcal: Double): Int = (Math.round(kcal / 10) * 10).toInt()

    /** Age in [year] from the birth year only. */
    fun age(birthYear: Int, year: Int): Int = year - birthYear

    /** Under 18 in [year]; unknown birth year counts as adult. */
    fun minor(profile: OwnerProfile, year: Int): Boolean =
        profile.birthYear?.let { age(it, year) < ADULT_AGE } ?: false
}

/** What Historia's tabs switch between (weight-chart "One quantity at a time"). */
enum class Quantity {
    WEIGHT, BMI, BMR;

    /** Owner details this quantity needs. */
    fun needs(): List<Field> = when (this) {
        WEIGHT -> emptyList()
        BMI -> listOf(Field.HEIGHT)
        BMR -> listOf(Field.HEIGHT, Field.BIRTH_YEAR, Field.GENDER)
    }

    /** Whether the value can be shown for a weight from [year]. */
    fun availability(profile: OwnerProfile, year: Int): Availability {
        val missing = needs().filter { profile.lacks(it) }
        return when {
            missing.isNotEmpty() -> Availability.Missing(missing)
            this == BMR && Derived.minor(profile, year) -> Availability.Minor
            else -> Availability.Ok
        }
    }

    /** The value of [entry] (kg, BMI or kcal/day unrounded), or null when it cannot be computed. */
    fun valueOf(entry: WeightEntry, profile: OwnerProfile, zone: ZoneId): Double? {
        if (this == WEIGHT) return entry.kg
        val year = entry.time.atZone(zone).year
        if (availability(profile, year) != Availability.Ok) return null
        val height = profile.heightCm!!
        return when (this) {
            WEIGHT -> entry.kg
            BMI -> Derived.bmi(entry.kg, height)
            BMR -> Derived.bmr(entry.kg, height, Derived.age(profile.birthYear!!, year), profile.gender!!)
        }
    }

    /** How many units one kg is worth, so the y-scale rule means the same on every tab. */
    fun perKg(profile: OwnerProfile): Double = when (this) {
        WEIGHT -> 1.0
        BMI -> profile.heightCm?.let { 1 / (it / 100.0).let { m -> m * m } } ?: 1.0
        BMR -> 10.0
    }

    /** The scale's rounding step. */
    val step: Double get() = if (this == BMR) 10.0 else 1.0

    fun scale(values: Collection<Double>, profile: OwnerProfile): KgRange =
        WeightScale.of(values, perKg(profile), step)

    /** Derived quantities have no per-week rate in the stats. */
    val hasRate: Boolean get() = this == WEIGHT
}

private fun OwnerProfile.lacks(field: Field): Boolean = when (field) {
    Field.HEIGHT -> heightCm == null
    Field.BIRTH_YEAR -> birthYear == null
    Field.GENDER -> gender == null
}

/** The home row (home-screen "Simple home screen"): derived values of the latest weight. */
data class LatestDerived(
    /** BMI, or null when [bmiState] is not Ok. */
    val bmi: Double?,
    /** Class and tone of [bmi]; null for a minor. */
    val bmiClass: BmiClass?,
    val tone: Tone?,
    val bmiState: Availability,
    /** Normal-weight range in kg; null when [rangeState] is not Ok. */
    val normalRange: IntRange?,
    val rangeState: Availability,
    /** Shown kcal/day; null when [bmrState] is not Ok. */
    val bmr: Int?,
    val bmrState: Availability,
) {
    companion object {
        fun of(latest: WeightEntry, profile: OwnerProfile, zone: ZoneId): LatestDerived {
            val year = latest.time.atZone(zone).year
            val bmi = Quantity.BMI.valueOf(latest, profile, zone)
            val minor = Derived.minor(profile, year)
            val bmiState = Quantity.BMI.availability(profile, year)
            // The range follows the height and the same under-18 rule as the class.
            val rangeState = if (bmiState == Availability.Ok && minor) Availability.Minor else bmiState
            val bmrState = Quantity.BMR.availability(profile, year)
            return LatestDerived(
                bmi = bmi,
                bmiClass = bmi?.takeUnless { minor }?.let { Derived.bmiClass(it) },
                tone = bmi?.takeUnless { minor }?.let { Derived.tone(it) },
                bmiState = bmiState,
                normalRange = if (rangeState == Availability.Ok) Derived.normalRange(profile.heightCm!!) else null,
                rangeState = rangeState,
                bmr = Quantity.BMR.valueOf(latest, profile, zone)?.let { Derived.shownBmr(it) },
                bmrState = bmrState,
            )
        }
    }
}
