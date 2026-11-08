package fi.jukkakot.terveyslappu.weight

import java.time.Instant

/** What the weight field holds, after [parseWeight]. */
sealed interface WeightInput {
    data object Empty : WeightInput
    /** Not a number with at most one decimal. */
    data object Invalid : WeightInput
    data object OutOfRange : WeightInput
    data class Valid(val kg: Double) : WeightInput
}

const val MIN_KG = 20.0
const val MAX_KG = 300.0

private val weightPattern = Regex("""^\d{1,3}(\.\d)?$""")

/** Reads `82,4` or `82.4` as 82.4 kg; accepts 20.0–300.0 kg with at most one decimal. */
fun parseWeight(text: String): WeightInput {
    val normalised = text.trim().replace(',', '.')
    if (normalised.isEmpty()) return WeightInput.Empty
    if (!weightPattern.matches(normalised)) return WeightInput.Invalid
    val kg = normalised.toDouble()
    return if (kg in MIN_KG..MAX_KG) WeightInput.Valid(kg) else WeightInput.OutOfRange
}

/** A weight cannot be saved for a moment after [now]. */
fun isInFuture(time: Instant, now: Instant): Boolean = time.isAfter(now)
