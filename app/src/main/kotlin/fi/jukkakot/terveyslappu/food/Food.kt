package fi.jukkakot.terveyslappu.food

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.roundToInt

/** The unit of a logged amount: grams as eaten or pieces. */
@Serializable
enum class AmountUnit(val code: Char) {
    @SerialName("g") G('g'),
    @SerialName("kpl") KPL('k'),
}

@Serializable
data class Amount(val value: Double, val unit: AmountUnit) {
    init {
        require(value > 0) { "amount must be positive" }
    }

    companion object {
        val ONE_PIECE = Amount(1.0, AmountUnit.KPL)
    }
}

/** A product's energy: kcal per 100 g (amounts in grams) or per piece (amounts in kpl). */
@Serializable
data class Energy(val kcal: Double, val per: AmountUnit) {
    /** The unit an amount needs for this energy. */
    val unit: AmountUnit get() = per

    /** kcal of [amount], or null when the amount's unit does not fit. */
    fun kcalOf(amount: Amount): Double? = when {
        amount.unit != per -> null
        per == AmountUnit.G -> kcal * amount.value / 100.0
        else -> kcal * amount.value
    }

    companion object {
        fun per100g(kcal: Double) = Energy(kcal, AmountUnit.G)
        fun perPiece(kcal: Double) = Energy(kcal, AmountUnit.KPL)
    }
}

/**
 * An own product (food-log "Products"): every distinct logged name is one. [energy] is null until
 * known; [lastAmount] is the amount the next log uses.
 */
@Serializable
data class Product(
    val name: String,
    val treat: Boolean = false,
    val energy: Energy? = null,
    val lastAmount: Amount? = null,
) {
    val key: String get() = FoodNames.key(name)

    /** The amount a new log of this product starts with (design: usual amount). */
    fun usualAmount(): Amount? = lastAmount ?: if (energy?.per == AmountUnit.KPL) Amount.ONE_PIECE else null
}

/** A shipped generic food (assets/base-products.json). */
data class BaseProduct(val name: String, val aliases: List<String>, val energy: Energy, val portion: Amount, val treat: Boolean) {
    /** The own product it becomes when first logged. */
    fun toProduct() = Product(name, treat, energy, portion)
}

/**
 * One logged item, as rebuilt from its Health Connect record. [uuid] is its identity; [pos] its place
 * in the day (null for a record whose id this app cannot read); [timed] whether [time] is the
 * logging time (shown) or only the day.
 */
data class FoodItem(
    val uuid: String,
    val day: LocalDate,
    val pos: Int?,
    val name: String,
    val kcal: Double?,
    val amount: Amount?,
    val treat: Boolean,
    val time: Instant,
    val zoneOffset: ZoneOffset,
    val timed: Boolean,
    /** Health Connect's record id, null before the item is written. */
    val recordId: String? = null,
) {
    val key: String get() = FoodNames.key(name)
}

/** The day's sums (food-log "Ruoka day screen"): known kcal, the treats' share, items without kcal. */
data class DaySum(val total: Int, val treat: Int, val withoutKcal: Int, val count: Int) {
    val logged: Boolean get() = count > withoutKcal

    companion object {
        fun of(items: Collection<FoodItem>) = DaySum(
            total = items.sumOf { it.kcal ?: 0.0 }.roundToInt(),
            treat = items.filter { it.treat }.sumOf { it.kcal ?: 0.0 }.roundToInt(),
            withoutKcal = items.count { it.kcal == null },
            count = items.size,
        )
    }
}

/** The fixed name of a bare-number item; never a product and never suggested. */
const val QUICK_ADD = "Pikalisäys"
