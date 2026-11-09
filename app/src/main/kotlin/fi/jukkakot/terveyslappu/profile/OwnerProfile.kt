package fi.jukkakot.terveyslappu.profile

enum class Gender { MALE, FEMALE }

/** The phone owner's details (owner-profile spec); every field optional, one set per phone. */
data class OwnerProfile(val birthYear: Int? = null, val heightCm: Int? = null, val gender: Gender? = null) {
    val isEmpty: Boolean get() = birthYear == null && heightCm == null && gender == null

    /** Names of the fields that differ from [other] (for the log, never the values). */
    fun changedFields(other: OwnerProfile): List<String> = buildList {
        if (birthYear != other.birthYear) add("birthYear")
        if (heightCm != other.heightCm) add("height")
        if (gender != other.gender) add("gender")
    }
}

/** Plausible values only (owner-profile "Plausible values only"). */
object ProfileRules {
    const val MIN_BIRTH_YEAR = 1900
    const val MIN_HEIGHT_CM = 50
    const val MAX_HEIGHT_CM = 250

    fun birthYearOk(year: Int, currentYear: Int): Boolean = year in MIN_BIRTH_YEAR..currentYear
    fun heightOk(cm: Int): Boolean = cm in MIN_HEIGHT_CM..MAX_HEIGHT_CM

    /** Age this year for [birthYear]. */
    fun age(birthYear: Int, currentYear: Int): Int = currentYear - birthYear
}

/** What a typed field means: empty clears it, [Valid] keeps it, [Invalid] is shown with the range. */
sealed interface FieldInput {
    data object Empty : FieldInput
    data class Valid(val value: Int) : FieldInput
    data object Invalid : FieldInput
}

fun parseBirthYear(text: String, currentYear: Int): FieldInput = parseField(text) { ProfileRules.birthYearOk(it, currentYear) }

fun parseHeight(text: String): FieldInput = parseField(text, ProfileRules::heightOk)

private fun parseField(text: String, ok: (Int) -> Boolean): FieldInput {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return FieldInput.Empty
    val value = trimmed.toIntOrNull() ?: return FieldInput.Invalid
    return if (ok(value)) FieldInput.Valid(value) else FieldInput.Invalid
}
