package fi.jukkakot.terveyslappu.scale

import android.content.Context
import androidx.core.content.edit
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The remembered last weight in prefs (everyday-feedback design 2); made-up weights (NFR-1). */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class LastWeighInPrefsTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `prefs from before the save time read the last hearing as the save time`() {
        context.getSharedPreferences("weighin", Context.MODE_PRIVATE).edit(commit = true) {
            clear()
            putString("weight_kg", "82.5").putString("weight_address", "AA:BB").putLong("weight_at", 1_000L)
        }
        val last = LastWeighInPrefs(context).weight()!!
        assertEquals(Instant.ofEpochMilli(1_000L), last.at)
        assertFalse(last.deleted)
    }

    @Test
    fun `the save time and the delete survive a round trip`() {
        val store = LastWeighInPrefs(context)
        store.saveWeight(LastWeight(82.5, null, Instant.ofEpochMilli(5_000L), Instant.ofEpochMilli(2_000L), deleted = true))
        val last = LastWeighInPrefs(context).weight()!!
        assertEquals(Instant.ofEpochMilli(5_000L), last.heardAt)
        assertEquals(Instant.ofEpochMilli(2_000L), last.at)
        assertTrue(last.deleted)
    }
}
