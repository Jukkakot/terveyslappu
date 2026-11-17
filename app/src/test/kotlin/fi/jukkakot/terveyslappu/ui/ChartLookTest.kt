package fi.jukkakot.terveyslappu.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.LineDataSet
import fi.jukkakot.terveyslappu.chart.BarParts
import fi.jukkakot.terveyslappu.chart.CardCharts
import fi.jukkakot.terveyslappu.chart.ChartAxis
import fi.jukkakot.terveyslappu.chart.ChartPoint
import fi.jukkakot.terveyslappu.chart.TabCharts
import fi.jukkakot.terveyslappu.chart.ToneRef
import fi.jukkakot.terveyslappu.chart.HomeFood
import fi.jukkakot.terveyslappu.home.CardSize
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** app-appearance "Chart look" (visual-identity tasks 4.2–4.4). Made-up values. */
class BarPartsTest {
    private val today = LocalDate.of(2026, 11, 17)
    private fun day(back: Long) = today.minusDays(back)

    @Test
    fun `a Kulutus week mixing split, total-only and active-only days keeps the library's colour order`() {
        val points = listOf(
            ChartPoint(0.0, 2300.0, day(2), listOf(1900.0, 400.0)),
            ChartPoint(1.0, 2200.0, day(1)),
            ChartPoint(2.0, 500.0, today, listOf(0.0, 500.0)),
        )
        val parts = BarParts.of(points, listOf(ToneRef.REST, ToneRef.BURN), today)
        assertTrue(parts.all { it.size == 2 }, "one stack size for the set")
        // A one-part day: a zero part under it; past days dimmed, today full.
        assertEquals(listOf(0.0, 2200.0), parts[1].map { it.value })
        assertEquals(ToneRef.REST.alpha(ToneRef.PAST_ALPHA), parts[1][1].tone)
        assertEquals(listOf(ToneRef.REST.alpha(ToneRef.PAST_ALPHA), ToneRef.BURN.alpha(ToneRef.PAST_ALPHA)), parts[0].map { it.tone })
        // Today's zero lepo goes under the active part, which is then the top one (rounded).
        assertEquals(listOf(0.0, 500.0), parts[2].map { it.value })
        assertEquals(ToneRef.BURN, parts[2][1].tone)
        assertEquals(6, BarParts.tones(parts).size)
    }

    @Test
    fun `a Syoty week with treats dims the past and moves a zero treat under the food`() {
        val points = (6L downTo 0L).map { back ->
            val treat = if (back % 2 == 0L) 200.0 else 0.0
            ChartPoint((6 - back).toDouble(), 1500.0 + treat, day(back), listOf(1500.0, treat))
        }
        val parts = BarParts.of(points, listOf(ToneRef.FOOD, ToneRef.TREAT), today)
        assertEquals(listOf(0.0, 1500.0), parts[1].map { it.value })
        assertEquals(ToneRef.FOOD.alpha(ToneRef.PAST_ALPHA), parts[1][1].tone)
        assertEquals(listOf(ToneRef.FOOD, ToneRef.TREAT), parts.last().map { it.tone })
    }

    @Test
    fun `nothing is dimmed without a bar today or for weekly bars`() {
        val week = ChartAxis(day(6), today)
        assertNull(BarParts.todayOf(week, listOf(day(1)), today))
        assertNull(BarParts.todayOf(ChartAxis(day(90), today, weekly = true), listOf(today), today))
        assertEquals(today, BarParts.todayOf(week, listOf(today), today))
        val parts = BarParts.of(listOf(ChartPoint(0.0, 3.0, day(1))), listOf(ToneRef.STEPS), null)
        assertEquals(ToneRef.STEPS, parts.single()[1].tone)
    }
}

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w360dp-h780dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ChartLookTest {
    private val f = ChartFixtures
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val dark = ChartColors(0xFF4DF0B8.toInt(), 0, 0, 0xFFAAAAAA.toInt(), 0x14FFFFFF, AndroidColor.WHITE, dark = true)

    private fun style(card: Boolean = false) = PanelStyle(dark, plainTexts, 11f, dates = !card, legend = !card, card = card)

    private fun bound(spec: fi.jukkakot.terveyslappu.chart.ChartSpec, card: Boolean = false, width: Int = 640, height: Int = 400): HistoryCombinedChart =
        HistoryCombinedChart(context).apply {
            this.card = card
            measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY))
            layout(0, 0, width, height)
            bind(spec, spec.panels.single(), style(card))
        }

    @Test
    fun `the dark weight line has a fill, a glow and a ring, still picks the weight and adds no legend entry`() {
        val spec = TabCharts.weight(f.weightMonth, f.zone, f.today)
        val chart = bound(spec)
        val labels = chart.data!!.lineData!!.dataSets.map { it.label }
        assertEquals(listOf("line-fill", "line-glow"), labels.take(2))
        assertTrue(labels.last() == "line-ring", "$labels")
        assertEquals(spec.legend.size, chart.legend.entries.size)
        val day = f.today.minusDays(9)
        val h = chart.highlightsFor(day).single()
        val set = chart.data!!.getDataByIndex(h.dataIndex).getDataSetByIndex(h.dataSetIndex)!!
        assertEquals("line", set.label)
        assertTrue(set is LineDataSet<*>)
    }

    @Test
    fun `a stacked bar draws with rounded tops and a 1 dp card bar stays visible`() {
        val stacked = bound(TabCharts.burn(f.burnTwoWeeks, f.today))
        assertTrue((stacked.data!!.barData!!.dataSets.first() as BarDataSet<*>).barCornerRadius > 0f)
        drawn(stacked)
        val items = (0 until 90L).map { back -> f.foodItems.first().copy(uuid = "x$back", day = f.today.minusDays(back), kcal = 1200.0 + back) }
        val card = bound(CardCharts.food(HomeFood.of(items, 90, f.today), CardSize.SMALL)!!, card = true, width = 200, height = 120)
        drawn(card)
    }

    private fun drawn(chart: HistoryCombinedChart) {
        val bitmap = Bitmap.createBitmap(chart.width, chart.height, Bitmap.Config.ARGB_8888)
        chart.draw(Canvas(bitmap))
        var coloured = 0
        for (y in 0 until bitmap.height step 2) for (x in 0 until bitmap.width step 2) if (AndroidColor.alpha(bitmap.getPixel(x, y)) > 0) coloured++
        assertTrue(coloured > 20, "nothing drawn")
    }
}
