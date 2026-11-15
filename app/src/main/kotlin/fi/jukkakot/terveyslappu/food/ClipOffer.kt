package fi.jukkakot.terveyslappu.food

import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.roundToInt

/**
 * One line of the clipboard card: an item with the amount, kcal and treat sign the import would
 * write, or (products-only JSON) a product with its [energy].
 */
data class ClipLine(val name: String, val amount: Amount?, val kcal: Double?, val treat: Boolean, val energy: Energy? = null)

/**
 * What the clipboard offer shows: the content [key], how many entries, the kcal total the preview
 * would show (null when no item has kcal), whether the JSON holds only products, and the card's
 * [lines]; [more] entries past them ("+<N> muuta") and, for mixed JSON, its [products] count
 * ("+ <N> tuotetta", shown last, inside the [ClipOffers.MAX_LINES] limit).
 */
data class ClipOffer(
    val key: String,
    val count: Int,
    val kcal: Int?,
    val productsOnly: Boolean,
    val lines: List<ClipLine> = emptyList(),
    val more: Int = 0,
    val products: Int = 0,
)

/** The last clip the app read: its timestamp and what it held (null = no food JSON). */
data class ClipRead(val time: Long, val offer: ClipOffer?)

/** What a clipboard check does next. */
sealed interface ClipStep {
    /** The clip changed (or its time is unknown): read its text. */
    data object Read : ClipStep

    /** Known without reading: the offer to show, or null for none. */
    data class Known(val offer: ClipOffer?) : ClipStep
}

/** The clipboard offer rule (food-log "Clipboard offer"); pure, the activity does the reading. */
object ClipOffers {
    /** Done keys kept (FIFO). */
    const val MAX_DONE = 50

    /** The card's lines, the "+ <N> tuotetta" line of mixed JSON included. */
    const val MAX_LINES = 5

    /**
     * Content key: SHA-256 (hex, 16 chars) of a canonical text of the parsed items and products, so
     * the surrounding chat text, whitespace and key order do not change it.
     */
    fun key(parsed: AiParsed): String {
        val text = buildString {
            parsed.items.forEach { i ->
                append("i|").append(FoodNames.key(i.name)).append('|').append(i.amount?.value).append('|').append(i.amount?.unit)
                    .append('|').append(i.kcal).append('|').append(i.treat).append('\n')
            }
            parsed.products.forEach { p ->
                append("p|").append(FoodNames.key(p.name)).append('|').append(p.energy.kcal).append('|').append(p.energy.per)
                    .append('|').append(p.usual?.value).append('|').append(p.usual?.unit).append('|').append(p.treat).append('\n')
            }
        }
        val digest = MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }.take(16)
    }

    /**
     * The offer for parsed food JSON: lines and kcal are what the preview's Lisää would write to
     * [day], planned against [own] and [base] without writing.
     */
    fun offer(parsed: AiParsed, own: List<Product>, base: List<BaseProduct>, day: LocalDate = LocalDate.now()): ClipOffer {
        val key = key(parsed)
        if (parsed.items.isEmpty()) {
            val lines = parsed.products.take(MAX_LINES).map { ClipLine(it.name, it.usual, null, it.treat, it.energy) }
            return ClipOffer(key, parsed.products.size, null, true, lines, parsed.products.size - lines.size)
        }
        val zone = ZoneId.systemDefault()
        val plan = AiImport.plan(parsed.items, parsed.products, day, emptyList(), own, base, Instant.now(), zone) { "" }
        val kcals = plan.items.mapNotNull { it.kcal }
        val limit = if (parsed.products.isEmpty()) MAX_LINES else MAX_LINES - 1
        val lines = plan.items.take(limit).map { ClipLine(it.name, it.amount, it.kcal, it.treat) }
        return ClipOffer(
            key, parsed.items.size, if (kcals.isEmpty()) null else kcals.sum().roundToInt(), false,
            lines, plan.items.size - lines.size, parsed.products.size,
        )
    }

    /** The offer for [text], or null when it holds no usable food JSON. */
    fun from(text: String, own: List<Product> = emptyList(), base: List<BaseProduct> = emptyList()): ClipOffer? =
        parsed(text)?.let { offer(it, own, base) }

    /** The usable food JSON in [text], or null. */
    fun parsed(text: String): AiParsed? = (AiPaste.parse(text) as? AiResult.Ok)?.parsed

    /**
     * The check on open: a clip without text offers nothing; the same timestamp as the cached read
     * uses its result without reading; anything else (a 0 timestamp included) is read.
     */
    fun step(hasText: Boolean, clipTime: Long, cached: ClipRead?, done: Collection<String>): ClipStep = when {
        !hasText -> ClipStep.Known(null)
        clipTime != 0L && cached != null && cached.time == clipTime -> ClipStep.Known(shown(cached.offer, done))
        else -> ClipStep.Read
    }

    /** The offer unless its content was imported or skipped. */
    fun shown(offer: ClipOffer?, done: Collection<String>): ClipOffer? = offer?.takeUnless { it.key in done }

    /** [key] added to the done list, oldest dropped past [MAX_DONE]. */
    fun markDone(done: List<String>, key: String): List<String> = (done.filter { it != key } + key).takeLast(MAX_DONE)
}
