package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.derived.Availability
import fi.jukkakot.terveyslappu.derived.BmiClass
import fi.jukkakot.terveyslappu.derived.Derived
import fi.jukkakot.terveyslappu.derived.Field
import fi.jukkakot.terveyslappu.derived.LatestDerived
import fi.jukkakot.terveyslappu.derived.Quantity
import fi.jukkakot.terveyslappu.derived.Tone

/** Tone colours tuned for text on the card in each theme; the class word always goes with them. */
@Composable
fun toneColor(tone: Tone): Color {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return when (tone) {
        Tone.GREEN -> if (dark) Color(0xFF81C995) else Color(0xFF1E7B34)
        Tone.YELLOW -> if (dark) Color(0xFFFDD663) else Color(0xFF8A5A00)
        Tone.RED -> if (dark) Color(0xFFF28B82) else Color(0xFFB3261E)
    }
}

/** The BMI chart's class bands: the tone faded so the line stays readable. */
@Composable
fun bmiBands(): List<ChartBand> = Derived.BANDS.map { ChartBand(it.from, it.to, toneColor(it.tone).copy(alpha = 0.14f)) }

fun BmiClass.word(): Int = when (this) {
    BmiClass.UNDERWEIGHT -> R.string.bmi_underweight
    BmiClass.NORMAL -> R.string.bmi_normal
    BmiClass.OVERWEIGHT -> R.string.bmi_overweight
    BmiClass.OBESE -> R.string.bmi_obese
    BmiClass.SEVERE -> R.string.bmi_severe
    BmiClass.MORBID -> R.string.bmi_morbid
}

fun Quantity.title(): Int = when (this) {
    Quantity.WEIGHT -> R.string.quantity_weight
    Quantity.BMI -> R.string.quantity_bmi
}

/** A value of this quantity as shown in stats and dots: "82,0 kg", "25,3". */
fun Quantity.format(value: Double): String = when (this) {
    Quantity.WEIGHT -> "${formatKg(value)} kg"
    Quantity.BMI -> formatKg(value)
}

/** A change: "−1,0 kg", "−0,3". */
fun Quantity.changeText(delta: Double): String = when (this) {
    Quantity.WEIGHT -> "${formatChange(delta)} kg"
    Quantity.BMI -> formatChange(delta)
}

/** A y-axis label: "98 kg", "25". */
fun Quantity.axis(value: Double): String = when (this) {
    Quantity.WEIGHT -> "%.0f kg".format(value)
    Quantity.BMI -> "%.0f".format(value)
}

/** A dot's value: "82,6 kg", "BMI 25,3". */
fun Quantity.point(value: Double): String = if (this == Quantity.BMI) "BMI ${formatKg(value)}" else format(value)

/** Why [what] cannot be shown: "BMI vaatii pituuden", or the under-18 text. */
@Composable
fun reasonText(what: Int, state: Availability): String = when (state) {
    Availability.Ok -> ""
    Availability.Minor -> stringResource(R.string.derived_minor)
    is Availability.Missing -> {
        val names = state.fields.map { field -> stringResource(when (field) { Field.HEIGHT -> R.string.derived_field_height }) }
        stringResource(R.string.derived_needs, stringResource(what), names.joinToString(", "))
    }
}

/**
 * The home row under the card (home-screen "Simple home screen"): the BMI number in its tone, no
 * class word on screen (it stays in the screen reader text), and the normal range; or the reason
 * that opens Omat tiedot.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DerivedRow(row: LatestDerived, onOpenProfile: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val reasons = buildList {
        if (row.bmiState != Availability.Ok) add(reasonText(R.string.quantity_bmi, row.bmiState))
        if (row.rangeState != Availability.Ok && row.rangeState != row.bmiState) add(reasonText(R.string.quantity_range, row.rangeState))
    }.distinct()
    Column(
        Modifier.fillMaxWidth().testTag("derived"),
    ) {
        if (row.bmi != null || row.normalRange != null) {
            FlowRow(
                Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                row.bmi?.let { bmi ->
                    val text = stringResource(R.string.derived_bmi, formatKg(bmi))
                    val described = row.bmiClass?.let { "$text, ${stringResource(it.word())}" } ?: text
                    Text(
                        text,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = row.tone?.let { toneColor(it) } ?: Color.Unspecified,
                        modifier = Modifier.testTag("derivedBmi").semantics { contentDescription = described },
                    )
                }
                row.normalRange?.let {
                    Text(stringResource(R.string.derived_range, it.first, it.last), style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
                }
            }
        }
        reasons.forEach { ReasonLine(it, onOpenProfile) }
    }
}

/** A reason that opens Omat tiedot. */
@Composable
internal fun ReasonLine(text: String, onOpenProfile: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = stringResource(R.string.derived_open_profile), onClick = onOpenProfile)
            .heightIn(min = 44.dp)
            .padding(vertical = 6.dp)
            .testTag("derivedReason"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
        Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
    }
}
