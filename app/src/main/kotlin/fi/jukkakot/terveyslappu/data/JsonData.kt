package fi.jukkakot.terveyslappu.data

import fi.jukkakot.terveyslappu.history.HistoryParser
import fi.jukkakot.terveyslappu.history.ParsedHistory
import fi.jukkakot.terveyslappu.history.SkipReason
import fi.jukkakot.terveyslappu.history.WeightCollector
import fi.jukkakot.terveyslappu.raw.RawEntry
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.put
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * The data file as JSON (data-file "JSON layout"): `{"app":"terveyslappu","version":2,"weights":[...]}`,
 * each weight `{"time":"2024-03-01T07:12:00+02:00","kg":82.4,"source":"…","raw":{"source":"scale",…}}`.
 */
object JsonData {
    private val json = Json { prettyPrint = true }
    private val known = setOf("app", "version", DataFile.WEIGHTS, FoodData.FOODS, FoodData.PRODUCTS)
    private val offsetTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    private const val RAW_SOURCE = "source"
    private const val CHECK = "check"

    fun write(set: DataSet, fallbackZone: ZoneId): String {
        val root = buildJsonObject {
            put("app", DataFile.APP)
            put("version", DataFile.VERSION)
            put(
                DataFile.WEIGHTS,
                buildJsonArray {
                    for (row in set.weights.sortedBy { it.time }) {
                        add(
                            buildJsonObject {
                                put("time", offsetTime.format(row.time.atZone(row.zoneOffset ?: fallbackZone).toOffsetDateTime()))
                                put("kg", BigDecimal.valueOf(row.kg).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toDouble())
                                put("source", row.source)
                                row.raw?.let { raw ->
                                    put(
                                        "raw",
                                        buildJsonObject {
                                            put(RAW_SOURCE, raw.source)
                                            raw.fields.forEach { (k, v) -> if (k != RAW_SOURCE) put(k, v) }
                                        },
                                    )
                                }
                                if (row.check) put(CHECK, HistoryParser.CHECK_MARK)
                            },
                        )
                    }
                },
            )
            with(FoodData) { putJson(set, fallbackZone) }
        }
        return json.encodeToString(JsonElement.serializer(), root) + "\n"
    }

    /**
     * Reads the weights with the same rules as a history table (range, future, duplicates); item
     * numbers stand in for line numbers. Unknown top-level keys are reported, not read. A file
     * that is not a JSON object gives no weights.
     */
    fun read(text: String, now: Instant, zone: ZoneId = ZoneId.systemDefault()): ParsedHistory {
        val root = runCatching { json.parseToJsonElement(text.removePrefix("\uFEFF")) }.getOrNull() as? JsonObject
            ?: return ParsedHistory(emptyList(), emptyList(), null)
        val collector = WeightCollector(now)
        val items = root[DataFile.WEIGHTS] as? JsonArray ?: JsonArray(emptyList())
        items.forEachIndexed { i, item ->
            val line = i + 1
            val text = item.toString()
            val obj = item as? JsonObject
            val time = (obj?.get("time") as? JsonPrimitive)?.takeIf { it.isString }?.content
                ?.let { runCatching { OffsetDateTime.parse(it).toInstant() }.getOrNull() }
            if (time == null) return@forEachIndexed collector.skip(line, SkipReason.UNREADABLE_DATE, text)
            val kg = (obj["kg"] as? JsonPrimitive)?.doubleOrNull
                ?: return@forEachIndexed collector.skip(line, SkipReason.UNREADABLE_WEIGHT, text)
            val check = (obj[CHECK] as? JsonPrimitive)?.takeIf { it.isString }?.content == HistoryParser.CHECK_MARK
            collector.add(line, text, time, kg, check) { second -> raw(obj["raw"], second) }
        }
        val unknown = root.keys.filter { it !in known }
        return collector.result(null).copy(unknownKinds = unknown, foods = FoodData.readJson(root, zone))
    }

    private fun raw(element: JsonElement?, second: Long): RawEntry? {
        val obj = element as? JsonObject ?: return null
        val fields = LinkedHashMap<String, String>()
        for ((k, v) in obj) fields[k] = (v as? JsonPrimitive)?.content ?: v.toString()
        val source = fields.remove(RAW_SOURCE) ?: return null
        return RawEntry(second, source, fields)
    }
}
