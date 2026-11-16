package fi.jukkakot.terveyslappu.ui

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.chart.ChartLayout
import fi.jukkakot.terveyslappu.chart.ChartSpec
import fi.jukkakot.terveyslappu.chart.CustomChartSpec
import fi.jukkakot.terveyslappu.chart.TabCharts
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.LocalDate
import kotlin.test.assertTrue

/**
 * Every Historia chart drawn for real (Robolectric native graphics) in light and dark, saved as
 * PNG under `app/build/chart-renders/` for review (charts-on-mpandroidchart tasks). Asserts only
 * that something was drawn. Made-up data.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w360dp-h780dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ChartRenderTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val out = File(System.getProperty("chart.renders.out") ?: "build/chart-renders")
    private val f = ChartFixtures
    private val today = f.today

    private var dark by mutableStateOf(false)

    private fun render(name: String, content: @Composable () -> Unit) {
        compose.setContent {
            val context = LocalContext.current
            MaterialTheme(colorScheme = if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)) {
                Box(Modifier.testTag("render").width(360.dp).background(MaterialTheme.colorScheme.surfaceContainer).padding(12.dp)) { content() }
            }
        }
        for (theme in listOf(false, true)) {
            dark = theme
            compose.waitForIdle()
            val bitmap = compose.onNodeWithTag("render").captureToImage().asAndroidBitmap()
            out.mkdirs()
            File(out, "$name-${if (theme) "dark" else "light"}.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            assertTrue(colours(bitmap) > 3, "$name is not blank")
        }
    }

    private fun colours(bitmap: Bitmap): Int {
        val seen = HashSet<Int>()
        for (y in 0 until bitmap.height step 4) for (x in 0 until bitmap.width step 4) seen += bitmap.getPixel(x, y)
        return seen.size
    }

    private fun show(name: String, spec: ChartSpec, chosen: LocalDate? = null) = render(name) { HistoryChart(spec, chosen, {}, "render") }

    @Test
    fun paino() = show("paino", TabCharts.weight(f.weightMonth, f.zone, today), today.minusDays(9))

    @Test
    fun bmi() = show("bmi", TabCharts.bmi(f.bmiMonth, f.zone, today), today.minusDays(2))

    @Test
    fun energia() = show("energia", TabCharts.energy(f.energyWeek, today), today.minusDays(3))

    @Test
    fun askeleetWeekly() = show("askeleet-weekly", TabCharts.steps(f.stepsHalfYear, today), f.stepsHalfYear.bars[10].from)

    @Test
    fun askeleet() = show("askeleet", TabCharts.steps(f.stepsWeek, today), today.minusDays(1))

    @Test
    fun kulutus() = show("kulutus", TabCharts.burn(f.burnTwoWeeks, today), today.minusDays(5))

    @Test
    fun markerFirstDay() = show("marker-first-day", TabCharts.weight(f.weightMonth, f.zone, today), today.minusDays(29))

    @Test
    fun markerLastDay() = show("marker-last-day", TabCharts.weight(f.weightMonth, f.zone, today), today)

    @Test
    fun ownStacked3() {
        val data = f.data(f.chart(ChartLayout.STACKED, *f.three, average = true))
        show("own-stacked-3", CustomChartSpec.of(data, today) { true }, today.minusDays(6))
    }

    @Test
    fun ownOverlaid3() {
        val data = f.data(f.chart(ChartLayout.OVERLAID, *f.three, average = true))
        show("own-overlaid-3", CustomChartSpec.of(data, today) { true }, today.minusDays(6))
    }
}
