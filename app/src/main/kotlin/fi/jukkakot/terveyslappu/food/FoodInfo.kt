package fi.jukkakot.terveyslappu.food

import java.time.LocalDateTime

/** One line of a food row's ⓘ bubble (food-log "Item row buttons"); the UI picks strings and formats. */
sealed interface InfoLine {
    data class AmountLine(val amount: Amount) : InfoLine
    data class KcalLine(val kcal: Double?) : InfoLine
    data class EnergyLine(val energy: Energy) : InfoLine
    data object TreatLine : InfoLine
    data class TimeLine(val time: LocalDateTime) : InfoLine
}

object FoodInfo {
    /**
     * The bubble's lines in order: amount, kcal (always), the own ruoka's Kalorit, Herkku, logging time
     * (only when [FoodItem.timed]); each only when known. [own] is the own ruoka by name key, or null.
     */
    fun lines(item: FoodItem, own: Product?): List<InfoLine> = buildList {
        item.amount?.let { add(InfoLine.AmountLine(it)) }
        add(InfoLine.KcalLine(item.kcal))
        own?.energy?.let { add(InfoLine.EnergyLine(it)) }
        if (item.treat) add(InfoLine.TreatLine)
        if (item.timed) add(InfoLine.TimeLine(item.time.atOffset(item.zoneOffset).toLocalDateTime()))
    }

    /** The own ruoka matching [item]'s name (the app's name key), never a Valmis ruoka. */
    fun ownProduct(item: FoodItem, products: List<Product>): Product? {
        val key = FoodNames.key(item.name)
        return products.firstOrNull { it.key == key }
    }
}
