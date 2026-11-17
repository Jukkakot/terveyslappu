package fi.jukkakot.terveyslappu.ui

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

/** app-appearance "Theme choice": Värit next to Teema in the same prefs file. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class AppearancePrefsTest {
    @Test
    fun `missing or unknown colours mean Terveyslappu`() {
        ColourChoice.entries.forEach { assertEquals(it, ColourChoice.decode(it.name)) }
        assertEquals(ColourChoice.BRAND, ColourChoice.decode(null))
        assertEquals(ColourChoice.BRAND, ColourChoice.decode("x"))
    }

    @Test
    fun `an update keeps the stored theme and starts on Terveyslappu`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("appearance", Context.MODE_PRIVATE).edit().putString("theme", "LIGHT").commit()
        val prefs = AppearancePrefs(context)
        assertEquals(ThemeMode.LIGHT, prefs.mode.value)
        assertEquals(ColourChoice.BRAND, prefs.colours.value)
        prefs.set(ColourChoice.PHONE)
        assertEquals(ColourChoice.PHONE, AppearancePrefs(context).colours.value)
        assertEquals(ThemeMode.LIGHT, AppearancePrefs(context).mode.value)
    }
}
