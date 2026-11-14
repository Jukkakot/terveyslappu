package fi.jukkakot.terveyslappu.scale

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The same-weight rule (scale-weigh-in "Ignore the same weight repeated"); made-up weights (NFR-1). */
class SameWeightTest {
    private var now = Instant.parse("2026-11-14T05:00:00Z")
    private val keys = WeighingKeys(MemoryLastWeighIn()) { now }
    private val scale = "AA:BB:CC:DD:EE:FF"

    private fun after(seconds: Long) {
        now = now.plusSeconds(seconds)
    }

    @Test
    fun `nothing is ignored before a weight was remembered`() {
        assertFalse(keys.sameWeight(82.5, scale))
    }

    @Test
    fun `the same weight is ignored within 30 s and a repeat restarts the time`() {
        keys.remember(82.5, scale)
        after(5)
        assertTrue(keys.sameWeight(82.5, scale))
        // Every 20 s for two minutes: each ignored hearing keeps it going.
        repeat(6) {
            after(20)
            assertTrue(keys.sameWeight(82.5, scale))
        }
    }

    @Test
    fun `within 0,1 kg counts as the same, further away does not`() {
        keys.remember(82.5, scale)
        assertTrue(keys.sameWeight(82.6, scale))
        assertTrue(keys.sameWeight(82.45, scale))
        assertFalse(keys.sameWeight(82.7, scale))
        assertFalse(keys.sameWeight(81.9, scale))
    }

    @Test
    fun `quiet for 30 s ends the ignoring`() {
        keys.remember(82.5, scale)
        after(40)
        assertFalse(keys.sameWeight(82.5, scale))
    }

    @Test
    fun `another scale is not ignored`() {
        keys.remember(82.5, scale)
        assertFalse(keys.sameWeight(82.5, "11:22:33:44:55:66"))
    }

    @Test
    fun `an unknown address falls back to the remembered scale`() {
        val last = MemoryLastWeighIn()
        last.saveScale(KnownScale(scale, null))
        val k = WeighingKeys(last) { now }
        k.remember(82.5)
        assertTrue(k.sameWeight(82.5, scale))
        assertFalse(k.sameWeight(82.5, "11:22:33:44:55:66"))
    }
}
