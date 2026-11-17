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
    singleLine: Boolean = false,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        if (direction != 0) {
            val icon = if (direction < 0) R.drawable.ic_arrow_down else R.drawable.ic_arrow_up
            val tag = if (direction < 0) "arrowDown" else "arrowUp"
            Icon(painterResource(icon), contentDescription = null, tint = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color, modifier = Modifier.size(14.dp).testTag(tag))
            Spacer(Modifier.width(2.dp))
        }
        Text(text, style = style, color = color, fontWeight = fontWeight, maxLines = if (singleLine) 1 else Int.MAX_VALUE, softWrap = !singleLine)
    }
}

/**
 * A row's or card's change colour: the muted text tone for a gain, a loss and zero alike
 * (app-appearance "Quantity colours": a change is never mint or green). Statistics use the main tone.
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun changeColor(direction: Int): Color = MaterialTheme.colorScheme.onSurfaceVariant
