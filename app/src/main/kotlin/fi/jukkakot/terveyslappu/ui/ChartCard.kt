package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.chart.CustomChart
import fi.jukkakot.terveyslappu.chart.Period
import fi.jukkakot.terveyslappu.home.CardSize
import java.time.LocalDate
import java.time.ZoneId
import androidx.compose.ui.unit.dp

/** "1 kk", "Kaikki", "1.9.–15.10.": a chart's period as plain text. */
@Composable
internal fun periodText(period: Period): String = when (period) {
    is Period.Custom -> "${formatShortDay(period.from)}–${formatShortDay(period.to)}"
    else -> stringResource(period.label())
}

/**
 * An own chart on the front page (home-screen "Chart cards", custom-charts design 13): title with
 * the chart icon and chevron, the chart filling the free space, the period as a plain label. No
 * crosshair: a tap anywhere opens Historia on the chart's tab.
 */
@Composable
internal fun ChartCard(chart: CustomChart, data: HomeCardsData, size: CardSize, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    val chartData = rememberChartData(chart, data.state, data.food, data.steps, today, zone, data.burn)
    val states = chartStates(data.food, data.foodActions, data.steps, data.stepsActions, data.burn, data.burnActions)
    Column(
        modifier
            .clip(homeCardShape)
            .background(cardColour())
            .clickable(onClickLabel = stringResource(R.string.home_open_chart)) { data.actions.onOpenChart(chart.id) }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("chartCard-${chart.id}"),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Column(Modifier.testTag("chartCardTitle")) { CardTitle(R.drawable.ic_chart, chart.name, chevron = true, tone = null) }
        CustomChartView(chartData, size, states, modifier = Modifier.weight(1f).testTag("chartCardChart"))
        Text(
            periodText(chart.period),
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp).testTag("chartCardPeriod"),
        )
    }
}
