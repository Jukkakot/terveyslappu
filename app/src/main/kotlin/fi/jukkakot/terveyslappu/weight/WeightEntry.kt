package fi.jukkakot.terveyslappu.weight

import java.time.Instant
import java.time.ZoneOffset

/** One weight read from Health Connect. [own]: written by this app, so it may be deleted here. */
data class WeightEntry(
    val id: String,
    val time: Instant,
    val zoneOffset: ZoneOffset?,
    val kg: Double,
    val sourcePackage: String,
    val own: Boolean,
)

/** Builds an entry from a record's fields; [ownPackage] is this app's package name. */
fun weightEntry(
    id: String,
    time: Instant,
    zoneOffset: ZoneOffset?,
    kg: Double,
    sourcePackage: String,
    ownPackage: String,
) = WeightEntry(id, time, zoneOffset, kg, sourcePackage, own = sourcePackage == ownPackage)

/** Newest first, as the list shows them. */
fun List<WeightEntry>.newestFirst(): List<WeightEntry> = sortedByDescending { it.time }
