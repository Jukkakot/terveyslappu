package fi.jukkakot.terveyslappu.food

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The clipboard offer rule, made-up foods only (NFR-1). */
class ClipOfferTest {
    @Test
    fun `reads only new text`() {
        assertTrue(ClipOffers.shouldRead(true, 200, 100))
        assertFalse(ClipOffers.shouldRead(true, 100, 100))
        assertFalse(ClipOffers.shouldRead(true, 50, 100))
        assertFalse(ClipOffers.shouldRead(false, 200, 100))
        assertFalse(ClipOffers.shouldRead(true, 0, 0))
    }

    @Test
    fun `offers food json by item count`() {
        val text = "Tässä:\n```json\n{\"items\":[{\"name\":\"Kaurapuuro\",\"kcal\":210},{\"name\":\"Kahvi\"}]}\n```"
        assertEquals(ClipOffer(text, 2, false), ClipOffers.from(text))
    }

    @Test
    fun `products only counts products`() {
        val offer = ClipOffers.from("""{"products":[{"name":"Lohikeitto","kcal":75,"per":"100g"}]}""")
        assertEquals(1, offer?.count)
        assertTrue(offer?.productsOnly == true)
    }

    @Test
    fun `other text offers nothing`() {
        assertNull(ClipOffers.from("https://example.com/a"))
        assertNull(ClipOffers.from("""{"items":[]}"""))
    }
}
