package fi.jukkakot.terveyslappu.scale

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StableWeightTest {

    private fun r(kg: Double, stable: Boolean) = ScaleReading(kg, stable, ScaleUnit.KG, 0.0)

    @Test
    fun `scale settles - two equal stable readings decide, timed at the first`() {
        val d = StableWeight()
        assertNull(d.onReading(r(81.2, false), 0))
        assertNull(d.onReading(r(82.0, false), 200))
        assertNull(d.onReading(r(82.4, false), 400))
        assertNull(d.onReading(r(82.5, true), 600))
        assertEquals(StableResult(82.5, 600), d.onReading(r(82.5, true), 800))
    }

    @Test
    fun `one stable reading and two quiet seconds decide`() {
        val d = StableWeight()
        assertNull(d.onReading(r(82.5, true), 1_000))
        assertNull(d.onTick(2_500))
        assertEquals(StableResult(82.5, 1_000), d.onTick(3_000))
    }

    @Test
    fun `unstable reading of the same weight counts as quiet`() {
        val d = StableWeight()
        d.onReading(r(82.5, true), 0)
        assertNull(d.onReading(r(82.5, false), 500))
        assertEquals(82.5, d.onReading(r(82.5, false), 2_100)?.kg)
    }

    @Test
    fun `a different reading after a stable one keeps waiting`() {
        val d = StableWeight()
        d.onReading(r(82.5, true), 0)
        assertNull(d.onReading(r(83.1, false), 300))
        assertNull(d.onTick(5_000))
        assertNull(d.result)
    }

    @Test
    fun `empty scale and impossible weights are never a result`() {
        val d = StableWeight()
        d.onReading(r(0.0, true), 0)
        d.onReading(r(0.0, true), 200)
        d.onReading(r(350.0, true), 400)
        d.onReading(r(350.0, true), 600)
        assertNull(d.onTick(10_000))
    }

    @Test
    fun `result stays once decided`() {
        val d = StableWeight()
        d.onReading(r(82.5, true), 0)
        d.onReading(r(82.5, true), 100)
        assertEquals(82.5, d.onReading(r(70.0, true), 200)?.kg)
    }
}
