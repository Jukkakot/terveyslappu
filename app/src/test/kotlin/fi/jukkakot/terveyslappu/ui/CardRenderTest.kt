package fi.jukkakot.terveyslappu.ui

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.burn.BurnUiState
import fi.jukkakot.terveyslappu.chart.ChartLayout
import fi.jukkakot.terveyslappu.chart.ChartSeries
import fi.jukkakot.terveyslappu.chart.CustomChart
import fi.jukkakot.terveyslappu.chart.Period
import fi.jukkakot.terveyslappu.chart.SeriesKind
import fi.jukkakot.terveyslappu.chart.SeriesStyle
import fi.jukkakot.terveyslappu.home.CardSize
import fi.jukkakot.terveyslappu.home.HomeCard
import fi.jukkakot.terveyslappu.home.HomeLayout
import fi.jukkakot.terveyslappu.steps.StepsAccess
import fi.jukkakot.terveyslappu.steps.StepsUiState
import fi.jukkakot.terveyslappu.weight.WeightEntry
import fi.jukkakot.terveyslappu.weight.weightEntry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.Instant
import kotlin.test.assertTrue

/**
 * The front page cards drawn for real (Robolectric native graphics) in light and dark at every
 * size, saved as PNG under `app/build/chart-renders/cards/` for a by-eye review
 * (home-cards-on-chart-library tasks 0.1, 3.4). Asserts only that something was drawn. Made-up data.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w360dp-h780dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CardRenderTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val out = File(System.getProperty("chart.renders.out") ?: "build/chart-renders", "cards")
    private val f = ChartFixtures
    private val now: Instant = Instant.now()

    private class Case(val name: String, val card: HomeCard, val state: MainUiState, val steps: StepsUiState, val burn: BurnUiState, val food: FoodUiState)

    private var case by mutableStateOf<Case?>(null)
    private var dark by mutableStateOf(false)

    private val food = FoodUiState(access = FoodAccess.GRANTED, items = f.foodItems)
    private val steps = StepsUiState(access = StepsAccess.GRANTED, days = f.stepDays)
    private val burn = BurnUiState(access = StepsAccess.GRANTED, days = f.burnDays)

    private fun weights(days: Int): List<WeightEntry> = (0 until days).filter { it % 6 != 2 }.map { back ->
        weightEntry("w$back", now.minusSeconds(back * 86_400L + 3_600), null, 82.0 + back * 0.05 + ((back * 37) % 11 - 5) / 20.0, "me", "me")
    }

    private fun state(days: Long = 30, weights: List<WeightEntry> = weights(30), charts: List<CustomChart> = emptyList()) = MainUiState(
        access = Access.GRANTED,
        weights = weights,
        homeDays = days,
        foodDays = days,
        stepsDays = days,
        burnDays = days,
        charts = charts,
        loadedAt = now,
    )

    private fun case(name: String, id: String, size: CardSize, state: MainUiState = state()) = Case(name, HomeCard(id, size), state, steps, burn, food)

    private fun chart(name: String, layout: ChartLayout, vararg series: Pair<SeriesKind, SeriesStyle>, average: Boolean = true) =
        CustomChart(1, name, series.map { ChartSeries(it.first, it.second) }, layout, Period.MONTH, showAverage = average)

    private fun render(cases: List<Case>) {
        compose.setContent {
            val context = LocalContext.current
            MaterialTheme(colorScheme = if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)) {
                val c = case ?: return@MaterialTheme
                val width = if (c.card.size == CardSize.SMALL) 160.dp else 328.dp
                Box(Modifier.testTag("render").background(MaterialTheme.colorScheme.surface).padding(8.dp)) {
                    Box(Modifier.width(width).height(cardHeight(c.card.size, rowUnit()))) {
                        val data = HomeCardsData(c.state, MainActions(), c.food, FoodActions(), c.steps, StepsActions(), c.burn, BurnActions())
                        HomeCardView(c.card, data, Modifier.fillMaxSize())
                    }
                }
            }
        }
        for (c in cases) for (theme in listOf(false, true)) {
            case = c
            dark = theme
            compose.waitForIdle()
            save("${c.name}-${if (theme) "dark" else "light"}", compose.onNodeWithTag("render").captureToImage().asAndroidBitmap())
        }
    }

    private fun save(name: String, bitmap: Bitmap) {
        out.mkdirs()
        File(out, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val seen = HashSet<Int>()
        for (y in 0 until bitmap.height step 4) for (x in 0 until bitmap.width step 4) seen += bitmap.getPixel(x, y)
        assertTrue(seen.size > 3, "$name is not blank")
    }

    private fun sizes(name: String, id: String, state: MainUiState = state()) = CardSize.entries.map { case("$name-${it.name.lowercase()}", id, it, state) }

    @Test
    fun weight() = render(
        sizes("weight", HomeLayout.WEIGHT) + listOf(
            case("weight-90-small", HomeLayout.WEIGHT, CardSize.SMALL, state(90, weights(90))),
            case("weight-single-wide", HomeLayout.WEIGHT, CardSize.WIDE, state(weights = weights(1).map { weightEntry("one", now.minusSeconds(86_400 * 3), null, 82.4, "me", "me") })),
            case("weight-latest-now-wide", HomeLayout.WEIGHT, CardSize.WIDE, state(7, weights(7).drop(1) + weightEntry("now", now, null, 81.6, "me", "me"))),
        ),
    )

    @Test
    fun food() = render(sizes("food", HomeLayout.FOOD) + case("food-90-small", HomeLayout.FOOD, CardSize.SMALL, state(90)))

    @Test
    fun steps() = render(sizes("steps", HomeLayout.STEPS) + case("steps-90-small", HomeLayout.STEPS, CardSize.SMALL, state(90)))

    /** The fixture's Kulutus has kokonais and active-only days. */
    @Test
    fun burn() = render(sizes("burn", HomeLayout.BURN, state(14)) + case("burn-90-small", HomeLayout.BURN, CardSize.SMALL, state(90)))

    @Test
    fun ownCharts() {
        val stacked = chart("Allekkain", ChartLayout.STACKED, *f.three)
        val overlaid = chart("Päällekkäin", ChartLayout.OVERLAID, *f.three)
        val threeBars = chart("Kolme pylvästä", ChartLayout.OVERLAID, SeriesKind.ENERGY to SeriesStyle.BARS, SeriesKind.STEPS to SeriesStyle.BARS, SeriesKind.BURN to SeriesStyle.BARS)
        val lines = chart("Viivat", ChartLayout.OVERLAID, SeriesKind.WEIGHT to SeriesStyle.LINE, SeriesKind.STEPS to SeriesStyle.LINE, SeriesKind.ENERGY to SeriesStyle.BARS)
        val burnStacked = chart("Kulutus", ChartLayout.STACKED, SeriesKind.BURN to SeriesStyle.BARS, SeriesKind.WEIGHT to SeriesStyle.LINE)
        fun own(name: String, c: CustomChart) = CardSize.entries.map { size ->
            Case("$name-${size.name.lowercase()}", HomeCard(c.cardId, size), state(charts = listOf(c)), steps, burn, food)
        }
        render(own("own-stacked", stacked) + own("own-overlaid", overlaid) + own("own-three-bars", threeBars).drop(1) + own("own-average-lines", lines).drop(1) + own("own-burn-stacked", burnStacked).drop(2))
    }

    @Test
    fun editModeDimmed() {
        val state = state(charts = listOf(chart("Liike", ChartLayout.STACKED, *f.three))).let {
            it.copy(homeLayout = it.homeLayout.addChart(it.charts[0].cardId))
        }
        compose.setContent {
            val context = LocalContext.current
            MaterialTheme(colorScheme = if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)) {
                MainScreen(state, "0.1.1-test", MainActions(), food = food, steps = steps)
            }
        }
        compose.onNodeWithTag("homeSlot-food", useUnmergedTree = true).performTouchInput { longClick() }
        for (theme in listOf(false, true)) {
            dark = theme
            compose.waitForIdle()
            save("edit-dimmed-${if (theme) "dark" else "light"}", compose.onRoot().captureToImage().asAndroidBitmap())
        }
    }
}
