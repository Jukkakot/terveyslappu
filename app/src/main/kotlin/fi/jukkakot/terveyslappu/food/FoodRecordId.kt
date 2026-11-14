package fi.jukkakot.terveyslappu.food

import java.math.BigDecimal

/**
 * The client record id of a food item (design 6): `tl-food-<pos>-<g|k|n><amount>-<t|m>-<c|d>-<uuid>`.
 * `g` grams, `k` pieces, `n` no amount (decimals with "."); `t` Herkku, `m` Ruoka; `c` the record's
 * time is the logging time, `d` only the day. Health Connect alone rebuilds the day from it.
 */
object FoodRecordId {
    const val PREFIX = "tl-food-"

    data class Parts(val pos: Int, val amount: Amount?, val treat: Boolean, val timed: Boolean, val uuid: String)

    fun encode(parts: Parts): String = buildString {
        append(PREFIX).append(parts.pos).append('-')
        val amount = parts.amount
        if (amount == null) append('n') else append(amount.unit.code).append(number(amount.value))
        append('-').append(if (parts.treat) 't' else 'm')
        append('-').append(if (parts.timed) 'c' else 'd')
        append('-').append(parts.uuid)
    }

    fun encode(item: FoodItem): String = encode(Parts(item.pos ?: 1, item.amount, item.treat, item.timed, item.uuid))

    /** Null for any id this codec did not write (shown as Ruoka in time order, design 7). */
    fun decode(id: String?): Parts? {
        if (id == null || !id.startsWith(PREFIX)) return null
        val f = id.removePrefix(PREFIX).split('-', limit = 5)
        if (f.size != 5) return null
        val pos = f[0].toIntOrNull()?.takeIf { it > 0 } ?: return null
        val amount = when {
            f[1] == "n" -> null
            f[1].length > 1 -> {
                val unit = AmountUnit.entries.firstOrNull { it.code == f[1][0] } ?: return null
                val value = f[1].substring(1).toDoubleOrNull()?.takeIf { it > 0 && it.isFinite() } ?: return null
                Amount(value, unit)
            }
            else -> return null
        }
        val treat = when (f[2]) {
            "t" -> true
            "m" -> false
            else -> return null
        }
        val timed = when (f[3]) {
            "c" -> true
            "d" -> false
            else -> return null
        }
        if (f[4].isEmpty()) return null
        return Parts(pos, amount, treat, timed, f[4])
    }

    /** 250.0 → `250`, 1.5 → `1.5`. */
    fun number(value: Double): String = BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()
}
