package fi.jukkakot.terveyslappu.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import fi.jukkakot.terveyslappu.food.Amount
import fi.jukkakot.terveyslappu.food.AmountUnit
import fi.jukkakot.terveyslappu.food.FoodItem
import fi.jukkakot.terveyslappu.food.Product
import fi.jukkakot.terveyslappu.ui.Access
import fi.jukkakot.terveyslappu.ui.ChartFixtures
import fi.jukkakot.terveyslappu.ui.ColourChoice
import fi.jukkakot.terveyslappu.ui.FoodAccess
import fi.jukkakot.terveyslappu.ui.FoodUiState
import fi.jukkakot.terveyslappu.ui.MainActions
import fi.jukkakot.terveyslappu.ui.MainScreen
import fi.jukkakot.terveyslappu.ui.MainUiState
import fi.jukkakot.terveyslappu.ui.Screen
import fi.jukkakot.terveyslappu.ui.SettingsPage
import fi.jukkakot.terveyslappu.ui.TerveysTheme
import fi.jukkakot.terveyslappu.ui.ThemeMode
import fi.jukkakot.terveyslappu.burn.BurnUiState
import fi.jukkakot.terveyslappu.chart.Period
import fi.jukkakot.terveyslappu.steps.StepsAccess
import fi.jukkakot.terveyslappu.steps.StepsUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * The developer's screenshot tour (visual-identity design 9): the main screens at 360 × 780 dp in
 * Tumma and Vaalea × Terveyslappu and Puhelimen värit, with made-up data (NFR-1). Excluded from
 * `./gradlew test`; run with
 * `./gradlew testDebugUnitTest -Pscreenshots --tests '*ScreenshotTour*'`; PNGs land in
 * `app/build/outputs/roborazzi/`.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w360dp-h780dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ScreenshotTour {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val f = ChartFixtures
    private val out = File(System.getProperty("screenshots.out") ?: "build/outputs/roborazzi")

    private val food = FoodUiState(
        access = FoodAccess.GRANTED,
        items = f.foodItems + listOf(item("Rahka", 250.0, 165.0, 1), item("Keitetty täysjyvämakaroni", 200.0, 300.0, 2), item("Jäätelö", 130.0, 260.0, 3, treat = true)),
        products = listOf(Product("Rahka"), Product("Jäätelö", treat = true)),
        day = f.today,
    )
    private val steps = StepsUiState(access = StepsAccess.GRANTED, days = f.stepDays)
    private val burn = BurnUiState(access = StepsAccess.GRANTED, days = f.burnDays)

    private fun item(name: String, grams: Double, kcal: Double, pos: Int, treat: Boolean = false): FoodItem {
        val t = f.today.atTime(8 + pos, 0).atZone(f.zone).toInstant()
        return FoodItem("tour-$pos", f.today, 10 + pos, name, kcal, Amount(grams, AmountUnit.G), treat, t, f.zone.rules.getOffset(t), true)
    }

    private val base = MainUiState(access = Access.GRANTED, weights = f.weights.sortedByDescending { it.time }, period = Period.WEEK)

    private val screens: List<Pair<String, MainUiState>> = listOf(
        "etusivu" to base,
        "historia-paino" to base.copy(screen = Screen.HISTORY),
        "historia-syoty" to base.copy(screen = Screen.HISTORY, historyOnEnergy = true),
        "historia-askeleet" to base.copy(screen = Screen.HISTORY, historyOnSteps = true),
        "historia-kulutus" to base.copy(screen = Screen.HISTORY, historyOnBurn = true),
        "ruoka" to base.copy(screen = Screen.FOOD),
        "ulkoasu" to base.copy(screen = Screen.SETTINGS, settingsPage = SettingsPage.APPEARANCE),
    )

    @Composable
    private fun Shown(state: MainUiState, dark: Boolean, colours: ColourChoice) {
        TerveysTheme(mode = if (dark) ThemeMode.DARK else ThemeMode.LIGHT, colours = colours) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                MainScreen(state, "0.0.0-tour", MainActions(), food = food, steps = steps, burn = burn, themeMode = if (dark) ThemeMode.DARK else ThemeMode.LIGHT, colourChoice = colours)
            }
        }
    }

    @Test
    fun tour() {
        val current = androidx.compose.runtime.mutableStateOf(Triple(base, true, ColourChoice.BRAND))
        compose.setContent { current.value.let { Shown(it.first, it.second, it.third) } }
        for (colours in ColourChoice.entries) {
            for (dark in listOf(true, false)) {
                for ((name, state) in screens) {
                    current.value = Triple(state, dark, colours)
                    compose.waitForIdle()
                    val file = File(out, "${colours.name.lowercase()}-${if (dark) "tumma" else "vaalea"}-$name.png")
                    compose.onRoot().captureRoboImage(file.absolutePath)
                }
            }
        }
    }
}
