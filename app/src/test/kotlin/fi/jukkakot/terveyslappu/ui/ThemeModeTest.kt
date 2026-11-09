package fi.jukkakot.terveyslappu.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ThemeModeTest {
    @Test
    fun `system follows the phone, the others force`() {
        assertTrue(ThemeMode.SYSTEM.isDark(systemDark = true))
        assertFalse(ThemeMode.SYSTEM.isDark(systemDark = false))
        assertFalse(ThemeMode.LIGHT.isDark(systemDark = true))
        assertTrue(ThemeMode.DARK.isDark(systemDark = false))
    }

    @Test
    fun `stored value round-trips and unknown or missing means system`() {
        ThemeMode.entries.forEach { assertEquals(it, ThemeMode.decode(it.name)) }
        assertEquals(ThemeMode.SYSTEM, ThemeMode.decode(null))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.decode("sepia"))
    }
}
