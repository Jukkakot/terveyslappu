package fi.jukkakot.terveyslappu.listen

import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The window-done record, the Done status and the widget states (background-weigh-in "Stop after the window's weight"). */
class DoneTest {
    private val zone = ZoneId.of("Europe/Helsinki")
    private val window = ListenWindow(LocalTime.of(6, 0), LocalTime.of(10, 0))
    private fun at(text: String): Instant = LocalDateTime.parse(text).atZone(zone).toInstant()
    private val start = at("2026-11-14T06:00").toEpochMilli()

    @Test
    fun `done holds from the save until the next window start, also outside the window`() {
        val done = WindowDone(start, setOf("a"))
        assertTrue(done.holds(window, at("2026-11-14T07:12"), zone))
        assertTrue(done.holds(window, at("2026-11-14T20:00"), zone))
        assertTrue(done.holds(window, at("2026-11-15T05:59"), zone))
        assertFalse(done.holds(window, at("2026-11-15T06:00"), zone))
    }

    @Test
    fun `an empty or missing record never holds`() {
        assertFalse(null.holds(window, at("2026-11-14T07:00"), zone))
        assertFalse(WindowDone(start, emptySet()).holds(window, at("2026-11-14T07:00"), zone))
    }

    private val facts = ListenFacts(
        enabled = true, hasScale = true, permission = true, bluetoothOn = true, powerSave = false,
        listenInPowerSave = true, armed = true, inside = true, nextStart = LocalTime.of(6, 0),
        done = true, nextStartTomorrow = true,
    )

    @Test
    fun `done comes before the blocked reasons, listening and resting`() {
        val done = ListenStatus.Done(LocalTime.of(6, 0), true)
        assertEquals(done, ListenStatusRule.status(facts))
        assertEquals(done, ListenStatusRule.status(facts.copy(inside = false)))
        assertEquals(done, ListenStatusRule.status(facts.copy(bluetoothOn = false)))
        assertEquals(done, ListenStatusRule.status(facts.copy(powerSave = true, listenInPowerSave = false)))
        // From the next window start (done no longer holds) the blocker shows again.
        assertEquals(ListenStatus.BluetoothOff, ListenStatusRule.status(facts.copy(done = false, bluetoothOn = false)))
        assertEquals(ListenStatus.PowerSavePause, ListenStatusRule.status(facts.copy(done = false, powerSave = true, listenInPowerSave = false)))
        assertIs<ListenStatus.Listening>(ListenStatusRule.status(facts.copy(done = false)))
        assertNull(ListenStatusRule.status(facts.copy(enabled = false)))
    }

    @Test
    fun `the widget shows Valmis also outside the window, otherwise as before`() {
        val done = ListenStatus.Done(LocalTime.of(6, 0), true)
        assertEquals(WidgetState.DONE, WidgetStateRule.of(done, inside = true))
        assertEquals(WidgetState.DONE, WidgetStateRule.of(done, inside = false))
        assertEquals(WidgetState.ON, WidgetStateRule.of(ListenStatus.Listening(LocalTime.of(10, 0)), inside = true))
        assertEquals(WidgetState.RESTING, WidgetStateRule.of(ListenStatus.Resting(LocalTime.of(6, 0)), inside = false))
        assertEquals(WidgetState.BLOCKED, WidgetStateRule.of(ListenStatus.BluetoothOff, inside = true))
        assertNull(WidgetStateRule.of(null, inside = true))
        assertEquals(WidgetState.DONE, WidgetStateRule.shown(WidgetState.DONE, 1, 10))
    }

    @Test
    fun `the widget and Asetukset draw the check while weighed`() {
        assertEquals(StateSymbol.CHECK, WidgetState.DONE.symbol())
        assertEquals(StateSymbol.TRIANGLE, WidgetState.BLOCKED.symbol())
        assertEquals(StateSymbol.DOT, WidgetState.ON.symbol())
        assertEquals(StateSymbol.DOT, WidgetState.RESTING.symbol())
        assertEquals(StateSymbol.CHECK, ListenStatus.Done(LocalTime.of(6, 0), true).symbol())
        assertEquals(StateSymbol.TRIANGLE, ListenStatus.BluetoothOff.symbol())
        assertEquals(StateSymbol.DOT, ListenStatus.Listening(LocalTime.of(10, 0)).symbol())
        // Bluetooth off after weighing: the check stays on the widget too.
        assertEquals(WidgetState.DONE, WidgetStateRule.of(ListenStatusRule.status(facts.copy(bluetoothOn = false)), inside = true))
    }
}
