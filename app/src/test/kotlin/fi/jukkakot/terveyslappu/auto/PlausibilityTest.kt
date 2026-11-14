package fi.jukkakot.terveyslappu.auto

import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Made-up weights only (NFR-1). */
class PlausibilityTest {
    private val day = 86_400_000L
    private val monday = RefPair("a", 1 * day, 82.0)

    private fun known(vararg pairs: RefPair) = ReferenceRule.seeded(pairs.toList())

    @Test
    fun `within, exactly and over the limit`() {
        val ref = Reference.Weight(82.0, 0)
        assertFalse(Plausibility.questioned(ref, 86.5, 5.0))
        assertFalse(Plausibility.questioned(ref, 87.0, 5.0))
        assertFalse(Plausibility.questioned(ref, 77.0, 5.0))
        assertTrue(Plausibility.questioned(ref, 87.1, 5.0))
        assertTrue(Plausibility.questioned(ref, 61.3, 5.0))
    }

    @Test
    fun `unknown marks, a known empty reference accepts`() {
        assertTrue(Plausibility.questioned(ReferenceRule.reference(ReferenceState(), 5 * day), 82.5, 5.0))
        assertEquals(Reference.None, ReferenceRule.reference(known(), 5 * day))
        assertFalse(Plausibility.questioned(Reference.None, 82.5, 5.0))
    }

    @Test
    fun `an earlier questioned weight is never the reference`() {
        // 61.3 kg on Tuesday was marked "?", so it never joined the accepted pairs.
        val state = known(monday)
        assertEquals(Reference.Weight(82.0, monday.timeMs), ReferenceRule.reference(state, 3 * day))
        assertFalse(Plausibility.questioned(ReferenceRule.reference(state, 3 * day), 82.4, 5.0))
    }

    @Test
    fun `the reference is the latest accepted pair before the new weight`() {
        val tuesday = RefPair("b", 2 * day, 82.4)
        val state = known(monday, tuesday)
        assertEquals(82.4, (ReferenceRule.reference(state, 3 * day) as Reference.Weight).kg)
        assertEquals(82.0, (ReferenceRule.reference(state, 2 * day) as Reference.Weight).kg)
        // A delete falls back to the previous pair without a read.
        assertEquals(82.0, (ReferenceRule.reference(ReferenceRule.removed(state, "b"), 3 * day) as Reference.Weight).kg)
    }

    @Test
    fun `the list keeps the latest ten and an accepted pair replaces its id`() {
        var state = known()
        for (i in 1..12) state = ReferenceRule.accepted(state, listOf(RefPair("p$i", i * day, 80.0 + i / 10.0)))
        assertEquals(ReferenceRule.KEEP, state.pairs.size)
        assertEquals("p3", state.pairs.first().id)
        state = ReferenceRule.accepted(state, listOf(RefPair("p12", 12 * day, 90.0)))
        assertEquals(ReferenceRule.KEEP, state.pairs.size)
        assertEquals(90.0, state.pairs.last().kg)
        // Accepting while unknown keeps the state unknown until a foreground read.
        assertEquals(Reference.Unknown, ReferenceRule.reference(ReferenceRule.accepted(ReferenceState(), listOf(monday)), 5 * day))
    }

    @Test
    fun `limit snaps to half kilos within range`() {
        assertEquals(0.5, Plausibility.limit(0.0))
        assertEquals(20.0, Plausibility.limit(25.0))
        assertEquals(3.5, Plausibility.limit(3.4))
    }

    private fun mark(id: String, d: Int, kg: Double) = Mark(id, d * day, kg)

    @Test
    fun `chain accepts later marks within the limit and stops at the first outside`() {
        val marks = listOf(mark("a", 1, 85.0), mark("b", 2, 84.2), mark("c", 3, 83.9), mark("d", 4, 61.3), mark("e", 5, 83.8))
        assertEquals(listOf("b", "c"), AcceptChain.chain(marks[0], marks, 5.0).map { it.id })
    }

    @Test
    fun `chain of one and none`() {
        val single = mark("a", 1, 85.0)
        assertEquals(emptyList(), AcceptChain.chain(single, listOf(single), 5.0))
        val far = listOf(single, mark("b", 2, 61.3))
        assertEquals(emptyList(), AcceptChain.chain(single, far, 5.0))
        // Earlier marks are not part of the chain.
        assertEquals(emptyList(), AcceptChain.chain(far[1], far, 30.0))
    }
}

class AutoTextTest {
    private val zone = ZoneId.of("Europe/Helsinki")

    @Test
    fun `normal, questioned, no reference and late texts`() {
        val ref = Reference.Weight(82.8, 0)
        assertEquals(NoteText("82,5 kg tallennettu", "−0,3 kg edellisestä"), AutoText.of(82.5, false, ref, null, zone))
        assertEquals(NoteText("82,9 kg tallennettu", "+0,1 kg edellisestä"), AutoText.of(82.9, false, ref, null, zone))
        assertEquals(
            NoteText("61,3 kg – tarkista", "Poikkeaa 21,5 kg edellisestä hyväksytystä"),
            AutoText.of(61.3, true, ref, null, zone),
        )
        assertEquals(NoteText("82,5 kg tallennettu", null), AutoText.of(82.5, true, Reference.Unknown, null, zone))
        assertEquals(NoteText("82,5 kg tallennettu", null), AutoText.of(82.5, false, Reference.None, null, zone))
        val late = Instant.parse("2026-11-14T05:12:00Z")
        assertEquals("82,5 kg tallennettu klo 7.12", AutoText.of(82.5, false, ref, late, zone).title)
    }
}
