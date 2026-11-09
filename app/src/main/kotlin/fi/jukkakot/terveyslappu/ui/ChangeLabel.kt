package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R

/**
 * A change number with a small arrow up (gain) or down (loss), none when it shows as ±0 — the one look
 * for every change in the app. [direction] comes from [shownDirection] so arrow and number agree.
 */
@Composable
fun ChangeLabel(
    text: String,
    direction: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        if (direction != 0) {
            val icon = if (direction < 0) R.drawable.ic_arrow_down else R.drawable.ic_arrow_up
            val tag = if (direction < 0) "arrowDown" else "arrowUp"
            Icon(painterResource(icon), contentDescription = null, tint = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color, modifier = Modifier.size(14.dp).testTag(tag))
            Spacer(Modifier.width(2.dp))
        }
        Text(text, style = style, color = color, fontWeight = fontWeight)
    }
}

/** Gain and loss colours that judge neither direction (no red/green): palette tones, zero neutral. */
@Composable
fun changeColor(direction: Int): Color = when {
    direction > 0 -> MaterialTheme.colorScheme.tertiary
    direction < 0 -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}
