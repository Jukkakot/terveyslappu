package fi.jukkakot.terveyslappu.ui

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.jukkakot.terveyslappu.R
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** home-screen "Units on the cards": no string a card shows may break between a number and its unit. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class UnitsOnCardsTest {
    /** Every string the weight, food, steps and Kulutus cards and own chart cards put a number and a unit in. */
    private val cardStrings = listOf(
        R.string.kg_value,
        R.string.home_spark_range,
        R.string.food_kcal,
        R.string.steps_value,
        R.string.steps_per_day,
        R.string.energy_per_day,
        R.string.home_food_average_long,
        R.string.home_steps_average_long,
        R.string.home_burn_average_long,
        R.string.food_day_treats,
        R.string.burn_active_line,
    )

    private val unit = Regex("(%\\d[$]s|\\d|→) (kg|kcal|askelta)")

    @Test
    fun `no card string breaks between a number and its unit`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        for (id in cardStrings) {
            val text = context.getString(id)
            assertFalse(unit.containsMatchIn(text), "breakable gap before a unit in \"$text\"")
            assertFalse(Regex("""/(pv|vk)""").containsMatchIn(text), "slash can break in \"$text\"")
        }
    }

    @Test
    fun `the range breaks only beside the arrow`() {
        val text = ApplicationProvider.getApplicationContext<android.content.Context>().getString(R.string.home_spark_range)
        assertEquals(2, text.count { it == ' ' }, "only the two spaces around the arrow: \"$text\"")
    }

    @Test
    fun `formatted numbers hold no breakable space`() {
        for (text in listOf(formatKcal(1878), formatKcal(12_345), formatSteps(7_517), formatSteps(123_456), formatKg(82.4))) {
            assertTrue(' ' !in text, "\"$text\" can break")
        }
    }
}
