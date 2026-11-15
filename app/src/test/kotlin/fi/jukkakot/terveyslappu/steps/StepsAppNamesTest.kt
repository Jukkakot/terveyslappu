package fi.jukkakot.terveyslappu.steps

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class StepsAppNamesTest {
    private val names = StepsAppNames(ApplicationProvider.getApplicationContext())

    @Test
    fun `the phone is Puhelin and an unknown package keeps its name`() {
        assertEquals("Puhelin", names.name("phone"))
        assertEquals("com.example.not.installed", names.name("com.example.not.installed"))
    }
}
