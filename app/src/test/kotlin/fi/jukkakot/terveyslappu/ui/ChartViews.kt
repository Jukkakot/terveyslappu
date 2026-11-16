package fi.jukkakot.terveyslappu.ui

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction

/** The library chart views on screen, top to bottom (test helper). */
internal fun Activity.chartViews(): List<HistoryCombinedChart> {
    val found = mutableListOf<HistoryCombinedChart>()
    fun walk(v: View) {
        if (v is HistoryCombinedChart && v.isAttachedToWindow) found += v
        if (v is ViewGroup) for (i in 0 until v.childCount) walk(v.getChildAt(i))
    }
    walk(window.decorView)
    return found.sortedBy { IntArray(2).also { a -> it.getLocationOnScreen(a) }[1] }
}

/** The left axis labels as the chart writes them. */
internal fun HistoryCombinedChart.leftLabels(): List<String> =
    (0 until axisLeft.entryCount).map { axisLeft.valueFormatter.getFormattedValue(axisLeft.entries[it], axisLeft) }

/** The date labels as the chart writes them (inside the plot). */
internal fun HistoryCombinedChart.dateLabels(): List<String> {
    val vp = viewPortHandler
    val trans = getTransformer(com.github.mikephil.charting.components.YAxis.AxisDependency.LEFT)
    return (0 until xAxis.entryCount).mapNotNull { i ->
        val pts = floatArrayOf(xAxis.entries[i], 0f)
        trans.pointValuesToPixel(pts)
        if (vp.isInBoundsX(pts[0])) xAxis.valueFormatter.getFormattedValue(xAxis.entries[i], xAxis) else null
    }
}

/** The x of [day]'s centre in the view's pixels (a touch there in node-local coordinates). */
internal fun HistoryCombinedChart.pixelX(day: java.time.LocalDate): Float {
    val x = spec!!.x.of(day).toFloat()
    return getPixelForValues(x, axisLeft.axisMinimum, com.github.mikephil.charting.components.YAxis.AxisDependency.LEFT).x.toFloat()
}

/** The y in the middle of the plot. */
internal val HistoryCombinedChart.plotMiddle: Float get() = viewPortHandler.contentRect.centerY()

/** The text TalkBack would announce (the host's state description), or null. */
internal fun SemanticsNodeInteraction.announced(): String? =
    fetchSemanticsNode().config.let { if (SemanticsProperties.StateDescription in it) it[SemanticsProperties.StateDescription] else null }
