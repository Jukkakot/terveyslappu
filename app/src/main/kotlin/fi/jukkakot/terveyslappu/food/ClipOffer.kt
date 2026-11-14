package fi.jukkakot.terveyslappu.food

/** What the clipboard line offers: the text (kept so Näytä needs no second read) and how many entries it holds. */
data class ClipOffer(val text: String, val count: Int, val productsOnly: Boolean)

/** The clipboard offer rule (food-ai-import "Clipboard offer"); pure, the activity does the reading. */
object ClipOffers {
    /** Read the clip text only when it is text and newer than the last one offered (a 0 timestamp is unknown: never). */
    fun shouldRead(hasText: Boolean, clipTime: Long, lastOffered: Long): Boolean = hasText && clipTime > lastOffered

    /** The offer for [text], or null when it holds no usable food JSON. */
    fun from(text: String): ClipOffer? {
        val ok = AiPaste.parse(text) as? AiResult.Ok ?: return null
        val p = ok.parsed
        return if (p.items.isNotEmpty()) ClipOffer(text, p.items.size, false) else ClipOffer(text, p.products.size, true)
    }
}
