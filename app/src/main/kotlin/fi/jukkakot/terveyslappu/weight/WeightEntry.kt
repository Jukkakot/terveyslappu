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
    /** The writer's own id (this app: upsert key), kept so an undo writes the same record back. */
    val clientRecordId: String? = null,
    /** Measured by a scale (device type), so an undo keeps that origin. */
    val fromScale: Boolean = false,
)

/** Builds an entry from a record's fields; [ownPackage] is this app's package name. */
fun weightEntry(
    id: String,
    time: Instant,
    zoneOffset: ZoneOffset?,
    kg: Double,
    sourcePackage: String,
    ownPackage: String,
    clientRecordId: String? = null,
    fromScale: Boolean = false,
) = WeightEntry(id, time, zoneOffset, kg, sourcePackage, own = sourcePackage == ownPackage, clientRecordId, fromScale)

/** Newest first, as the list shows them. */
fun List<WeightEntry>.newestFirst(): List<WeightEntry> = sortedByDescending { it.time }
