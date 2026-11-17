package fi.jukkakot.terveyslappu.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.home.CardSize
import fi.jukkakot.terveyslappu.home.HomeCard
import fi.jukkakot.terveyslappu.home.HomeLayout
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** home-screen "Card titles": the quantity's name in full on a small card at 130 % text. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi", fontScale = 1.3f)
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
class CardTitlesTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private var card by mutableStateOf(HomeCard(HomeLayout.WEIGHT, CardSize.SMALL))

    @Test
    fun `small cards show their titles in full at 130 percent text`() {
        compose.setContent {
            TerveysTheme {
                val state = MainUiState(access = Access.GRANTED, weights = HomeFixtures.weights)
                Box(Modifier.width(160.dp).height(cardHeight(CardSize.SMALL, rowUnit()))) {
                    HomeCardView(card, HomeCardsData(state, MainActions(), HomeFixtures.food, FoodActions(), HomeFixtures.steps, StepsActions(), HomeFixtures.burn, BurnActions()), Modifier.fillMaxSize())
                }
            }
        }
        val titles = mapOf(HomeLayout.WEIGHT to "Paino", HomeLayout.FOOD to "Syöty", HomeLayout.STEPS to "Askeleet", HomeLayout.BURN to "Kulutus")
        for ((id, title) in titles) {
            card = HomeCard(id, CardSize.SMALL)
            compose.waitForIdle()
            val node = compose.onNodeWithTag("cardTitle", useUnmergedTree = true).fetchSemanticsNode()
            assertEquals(title, node.config.getOrNull(SemanticsProperties.Text)?.joinToString())
            val layouts = mutableListOf<TextLayoutResult>()
            node.config[SemanticsActions.GetTextLayoutResult].action?.invoke(layouts)
            layouts.single().let { assertTrue(it.size.width + 1 >= it.multiParagraph.intrinsics.maxIntrinsicWidth, "$title is cut: ${it.size.width} < ${it.multiParagraph.intrinsics.maxIntrinsicWidth}") }
        }
    }
}
