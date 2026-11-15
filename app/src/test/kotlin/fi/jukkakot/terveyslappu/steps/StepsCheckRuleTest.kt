package fi.jukkakot.terveyslappu.steps

import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StepsCheckRuleTest {
    private val today = LocalDate.of(2026, 11, 15)
    private fun at(hhmm: String) = Instant.parse("2026-11-15T$hhmm:00Z")

    @Test
    fun `checked once a day`() {
        assertFalse(StepsCheckRule.due(today, today, null, at("12:00"), foreground = true))
        assertTrue(StepsCheckRule.due(today, today.minusDays(1), null, at("12:00"), foreground = true))
        assertTrue(StepsCheckRule.due(today, null, null, at("12:00"), foreground = true))
    }

    @Test
    fun `a failure at 10 is retried at 11 15 but not at 10 30`() {
        // A failed check does not write autoPickDay, so it stays yesterday.
        val day = today.minusDays(1)
        assertFalse(StepsCheckRule.due(today, day, at("10:00"), at("10:30"), foreground = true))
        assertTrue(StepsCheckRule.due(today, day, at("10:00"), at("11:15"), foreground = true))
    }

    @Test
    fun `never from the background`() {
        assertFalse(StepsCheckRule.due(today, null, null, at("12:00"), foreground = false))
    }

    @Test
    fun `sub-screen check not within a minute of the last one`() {
        assertTrue(StepsCheckRule.subScreenAllowed(null, at("12:00")))
        assertFalse(StepsCheckRule.subScreenAllowed(at("12:00"), Instant.parse("2026-11-15T12:00:40Z")))
        assertTrue(StepsCheckRule.subScreenAllowed(at("12:00"), at("12:01")))
    }
}
