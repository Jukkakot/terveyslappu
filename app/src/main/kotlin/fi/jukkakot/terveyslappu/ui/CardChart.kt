package fi.jukkakot.terveyslappu.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import fi.jukkakot.terveyslappu.chart.ChartSpec
import fi.jukkakot.terveyslappu.chart.PanelSpec

/**
 * One front page card's chart on the library (home-cards-on-chart-library design 5): a
 * [HistoryCombinedChart] in card mode bound to [panel] of [spec]. It takes no touch, so the card's
 * tap, long-press into edit mode and drag work over it; [modifier] carries the card's content
 * description and test tag. The caller remembers [spec] (design 6): a recomposition with the same
 * spec and colours does not rebind. Labels, legend and messages stay in Compose around it.
 */
@Composable
internal fun CardChart(spec: ChartSpec, modifier: Modifier = Modifier, panel: PanelSpec = spec.drawn.first()) {
    val colors = rememberChartColors()
    val texts = rememberChartTexts()
    val textDp = chartTextDp()
    val style = remember(colors, texts, textDp) { PanelStyle(colors, texts, textDp, dates = false, legend = false, card = true) }
    AndroidView(
        factory = { context -> HistoryCombinedChart(context).also { it.card = true } },
        modifier = modifier,
        // Reused in the lazy grid for another card: the next update binds afresh.
        onReset = { it.spec = null },
        update = { it.bind(spec, panel, style) },
    )
}
