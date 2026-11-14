package fi.jukkakot.terveyslappu.auto

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

/** A weigh-in notification's two lines; [text] null = title only. */
data class NoteText(val title: String, val text: String?)

/**
 * The automatic weigh-in notification's texts (background-weigh-in "Weigh-in notification"),
 * Finnish number format. Pure.
 */
object AutoText {
    private val finnish = Locale.forLanguageTag("fi")
    private val clock = DateTimeFormatter.ofPattern("H.mm", finnish)

    fun kg(value: Double): String = String.format(finnish, "%.1f", value)

    /** "+0,4", "−0,3", "±0,0" (the list's change format). */
    fun change(delta: Double): String {
        val tenths = (delta * 10).roundToLong()
        val text = String.format(finnish, "%.1f", abs(tenths) / 10.0)
        return when {
            tenths < 0 -> "−$text"
            tenths > 0 -> "+$text"
            else -> "±$text"
        }
    }

    /**
     * [late]: the weighing time when the weight was written later from the pending queue, added as
     * "klo 7.12". Without a reference weight only the title is shown.
     */
    fun of(kg: Double, questioned: Boolean, reference: Reference, late: Instant?, zone: ZoneId): NoteText {
        val at = late?.let { " klo ${clock.format(it.atZone(zone))}" } ?: ""
        val ref = reference as? Reference.Weight
        if (questioned && ref != null) {
            return NoteText("${kg(kg)} kg$at – tarkista", "Poikkeaa ${kg(abs(kg - ref.kg))} kg edellisestä hyväksytystä")
        }
        val title = "${kg(kg)} kg tallennettu$at"
        return NoteText(title, ref?.let { "${change(kg - it.kg)} kg edellisestä" })
    }

    const val DELETE_FAILED = "Poisto epäonnistui"
}
