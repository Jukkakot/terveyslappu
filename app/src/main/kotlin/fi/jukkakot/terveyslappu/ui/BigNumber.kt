package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fi.jukkakot.terveyslappu.chart.QuantityTone

/**
 * The big-number style (app-appearance "Big numbers"): semibold, figures of equal width, slightly
 * tight; [base] gives the size (displayMedium on cards and the hero, displaySmall on small cards).
 */
internal fun TextStyle.bigNumber(): TextStyle = copy(
    fontWeight = FontWeight.SemiBold,
    fontFeatureSettings = "tnum",
    letterSpacing = (-1).sp,
    // Tight line: the number needs no leading of its own (the card's plot gets the room).
    lineHeight = fontSize,
    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both),
)

/**
 * A main number with its unit on the same line, never split: the unit (smaller, regular, muted)
 * keeps its size and the number shrinks to fit ([TextAutoSize]) instead of wrapping.
 */
@Composable
internal fun BigNumber(
    value: String,
    unit: String?,
    modifier: Modifier = Modifier,
    tag: String? = null,
    base: TextStyle = MaterialTheme.typography.displayMedium,
    color: Color = MaterialTheme.colorScheme.onSurface,
    unitColor: Color = Color.Unspecified,
    leading: (@Composable RowScope.() -> Unit)? = null,
) {
    val style = base.bigNumber().copy(color = color)
    Row(modifier, verticalAlignment = Alignment.Bottom) {
        leading?.invoke(this)
        BasicText(
            value,
            style = style,
            maxLines = 1,
            softWrap = false,
            autoSize = TextAutoSize.StepBased(minFontSize = 14.sp, maxFontSize = style.fontSize, stepSize = 1.sp),
            modifier = Modifier.weight(1f, fill = false).alignByBaseline().then(if (tag != null) Modifier.testTag(tag) else Modifier),
        )
        if (unit != null) {
            Spacer(Modifier.width(4.dp))
            Text(unit, style = MaterialTheme.typography.titleMedium, color = unitColor.takeOrElse { MaterialTheme.colorScheme.onSurfaceVariant }, maxLines = 1, softWrap = false, modifier = Modifier.alignByBaseline())
        }
    }
}

/**
 * A card title's round tile (app-appearance "Card look"): the quantity's colour at 16 % (dark) /
 * 12 % (light) with its icon in full colour; null [tone] = a neutral tile with a muted icon.
 */
@Composable
internal fun IconTile(icon: Int, tone: QuantityTone?, modifier: Modifier = Modifier) {
    val dark = isDarkTheme()
    val colour = tone?.let { quantityColour(it) } ?: MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier.size(28.dp).clip(CircleShape).background(colour.copy(alpha = if (dark) 0.16f else 0.12f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = colour, modifier = Modifier.size(16.dp))
    }
}
