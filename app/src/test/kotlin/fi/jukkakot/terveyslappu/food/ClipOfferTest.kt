package fi.jukkakot.terveyslappu.food

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The clipboard offer rule, made-up foods only (NFR-1). */
class ClipOfferTest {
    private val json = """{"items":[{"name":"Kaurapuuro","amount":300,"unit":"g","kcal":210},{"name":"Kahvi","kcal":5}]}"""

    private fun parsed(text: String) = (AiPaste.parse(text) as AiResult.Ok).parsed

    @Test
    fun `same json in different chat text gives the same key`() {
        val a = ClipOffers.key(parsed("Tässä arvio:\n```json\n$json\n```"))
        val b = ClipOffers.key(parsed("Toinen teksti ympärillä $json kiitos"))
        val reordered = """{ "items" : [ {"kcal":210, "unit":"g", "amount":300, "name":"Kaurapuuro"}, {"kcal":5,"name":"Kahvi"} ] }"""
        assertEquals(a, b)
        assertEquals(a, ClipOffers.key(parsed(reordered)))
        assertEquals(16, a.length)
    }

    @Test
    fun `changed kcal gives another key`() {
        assertNotEquals(ClipOffers.key(parsed(json)), ClipOffers.key(parsed(json.replace("210", "211"))))
    }

    @Test
    fun `cached timestamp skips the read`() {
        val offer = ClipOffer("k1", 2, 215, false)
        assertEquals(ClipStep.Known(offer), ClipOffers.step(true, 100, ClipRead(100, offer), emptyList()))
        assertEquals(ClipStep.Known(null), ClipOffers.step(true, 100, ClipRead(100, null), emptyList()))
        assertIs<ClipStep.Read>(ClipOffers.step(true, 200, ClipRead(100, offer), emptyList()))
        assertIs<ClipStep.Read>(ClipOffers.step(true, 200, null, emptyList()))
    }

    @Test
    fun `done key hides the offer`() {
        val offer = ClipOffer("k1", 2, 215, false)
        assertEquals(ClipStep.Known(null), ClipOffers.step(true, 100, ClipRead(100, offer), listOf("k1")))
        assertNull(ClipOffers.shown(offer, listOf("k1")))
        assertEquals(offer, ClipOffers.shown(offer, listOf("k2")))
    }

    @Test
    fun `zero timestamp reads and no text offers nothing`() {
        assertIs<ClipStep.Read>(ClipOffers.step(true, 0, ClipRead(0, null), emptyList()))
        assertEquals(ClipStep.Known(null), ClipOffers.step(false, 100, null, emptyList()))
    }

    @Test
    fun `done list keeps the newest fifty`() {
        var done = emptyList<String>()
        repeat(55) { done = ClipOffers.markDone(done, "k$it") }
        assertEquals(50, done.size)
        assertEquals("k5", done.first())
        assertEquals("k54", done.last())
        assertEquals(listOf("a", "b"), ClipOffers.markDone(listOf("b", "a"), "b"))
    }

    @Test
    fun `items offer counts and sums kcal`() {
        val offer = ClipOffers.from("Tässä:\n```json\n$json\n```")!!
        assertEquals(2, offer.count)
        assertEquals(215, offer.kcal)
        assertEquals(false, offer.productsOnly)
    }

    @Test
    fun `items without kcal have no total`() {
        val offer = ClipOffers.from("""{"items":[{"name":"Tuntematon ruoka"},{"name":"Toinen ruoka"}]}""")!!
        assertEquals(2, offer.count)
        assertNull(offer.kcal)
    }

    @Test
    fun `kcal comes from a known product like the preview`() {
        val own = listOf(Product("Rahka", false, Energy(65.0, AmountUnit.G), Amount(200.0, AmountUnit.G)))
        assertEquals(130, ClipOffers.from("""{"items":[{"name":"Rahka"}]}""", own)?.kcal)
    }

    @Test
    fun `products only counts products`() {
        val offer = ClipOffers.from("""{"products":[{"name":"Lohikeitto","kcal":75,"per":"100g"}]}""")
        assertEquals(1, offer?.count)
        assertTrue(offer?.productsOnly == true)
        assertNull(offer?.kcal)
    }

    @Test
    fun `other text offers nothing`() {
        assertNull(ClipOffers.from("https://example.com/a"))
        assertNull(ClipOffers.from("""{"items":[]}"""))
    }
}
