package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import fi.jukkakot.terveyslappu.chart.ChartData
import fi.jukkakot.terveyslappu.chart.TabCharts
import java.time.LocalDate
import java.time.ZoneId

/**
 * Paino and BMI (weight-chart "Point details", "Crosshair on every Historia chart"): the period
 * range line over the library chart of [TabCharts.weight] or, with [bmi], [TabCharts.bmi] with the
 * class bands. A tap or drag chooses the day of the nearest weight ([onDay]).
 */
@Composable
fun WeightChart(
    data: ChartData,
    description: String,
    modifier: Modifier = Modifier,
    bmi: Boolean = false,
    crosshair: LocalDate? = null,
    onDay: (LocalDate?) -> Unit = {},
    zone: ZoneId = ZoneId.systemDefault(),
) {
    val today = LocalDate.now(zone)
    val spec = remember(data, bmi, zone, today) { if (bmi) TabCharts.bmi(data, zone, today) else TabCharts.weight(data, zone, today) }
    Column(modifier.fillMaxWidth()) {
        RangeLine(data.from, data.to, "chartRange")
        HistoryChart(spec, crosshair, onDay, description, tag = "chart", plotTag = { _, _ -> "plot-weight" })
    }
}
