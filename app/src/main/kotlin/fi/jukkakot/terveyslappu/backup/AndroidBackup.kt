package fi.jukkakot.terveyslappu.backup

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.edit
import androidx.core.net.toUri
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.TerveysApp
import fi.jukkakot.terveyslappu.data.DataFormat
import fi.jukkakot.terveyslappu.data.DataSet
import fi.jukkakot.terveyslappu.data.ExportRow
import fi.jukkakot.terveyslappu.data.FoodData
import fi.jukkakot.terveyslappu.data.JsonData
import fi.jukkakot.terveyslappu.food.FoodItem
import fi.jukkakot.terveyslappu.food.HealthConnectFoods
import fi.jukkakot.terveyslappu.log.AppLog
import fi.jukkakot.terveyslappu.weight.HealthConnectWeights
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId

/** Backup status in SharedPreferences (app-private; left out of Android backup and transfer: the file permission does not move). */
class PrefsBackupStore(context: Context) : BackupStore {
    private val prefs = context.getSharedPreferences("backup", Context.MODE_PRIVATE)

    override fun load() = BackupStatus(
        uri = prefs.getString(URI, null),
        lastOkAt = prefs.getLong(OK_AT, 0).takeIf { it > 0 }?.let(Instant::ofEpochMilli),
        lastCount = prefs.getInt(COUNT, 0),
        lastFailedAt = prefs.getLong(FAILED_AT, 0).takeIf { it > 0 }?.let(Instant::ofEpochMilli),
        recentOnly = prefs.getBoolean(RECENT_ONLY, false),
        refused = prefs.getBoolean(REFUSED, false),
    )

    override fun save(status: BackupStatus) = prefs.edit {
        putString(URI, status.uri)
        putLong(OK_AT, status.lastOkAt?.toEpochMilli() ?: 0)
        putInt(COUNT, status.lastCount)
        putLong(FAILED_AT, status.lastFailedAt?.toEpochMilli() ?: 0)
        putBoolean(RECENT_ONLY, status.recentOnly)
        putBoolean(REFUSED, status.refused)
    }

    private companion object {
        const val URI = "uri"
        const val OK_AT = "okAt"
        const val COUNT = "count"
        const val FAILED_AT = "failedAt"
        const val RECENT_ONLY = "recentOnly"
        const val REFUSED = "refused"
    }
}

/** Name of the app that wrote a weight, for the list and the export; the package when unknown. */
fun Context.appLabel(packageName: String): String = try {
    packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString()
} catch (_: PackageManager.NameNotFoundException) {
    packageName
}

/** Every readable weight as export rows; [ownLabel] names this app's own weights. */
suspend fun exportRows(context: Context, weights: HealthConnectWeights): List<ExportRow> {
    val ownLabel = context.getString(R.string.recent_this_app)
    val labels = HashMap<String, String>()
    val app = context.applicationContext as TerveysApp
    val archive = app.rawArchive
    val marks = app.marks.ids()
    return weights.all(Instant.now()).map {
        ExportRow(
            it.time, it.zoneOffset, it.kg,
            if (it.own) ownLabel else labels.getOrPut(it.sourcePackage) { context.appLabel(it.sourcePackage) },
            raw = if (it.own) archive.get(it.time.epochSecond) else null,
            check = it.own && it.clientRecordId in marks,
        )
    }
}

/**
 * This app's foods for the data file. Without the nutrition permission (or when the read fails) the
 * foods part of the file at [uri] is kept, so a backup never writes an empty part over foods it
 * could not read (food-log review decision).
 */
private suspend fun exportFoods(context: Context, uri: Uri, zone: ZoneId): List<FoodItem> {
    val hc = HealthConnectFoods(context)
    val read = runCatching { if (hc.granted()) hc.own(Instant.now()) else null }
        .onFailure { AppLog.e("food.read", it, "export") }
        .getOrNull()
    if (read != null) return read
    return withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val text = input.readBytes().decodeToString()
                when (DataFormat.detect(text)) {
                    DataFormat.JSON -> JsonData.read(text, Instant.now(), zone).foods.foods
                    DataFormat.CSV -> FoodData.readCsv(text, zone).foods
                }
            }
        }.getOrNull().orEmpty()
    }
}

/**
 * Writes the data file in [format] to [uri] (rewrites the whole file); returns the weight count,
 * or null without writing when [accept] refuses that count.
 */
suspend fun writeExport(
    context: Context,
    weights: HealthConnectWeights,
    uri: Uri,
    format: DataFormat = DataFormat.CSV,
    accept: (count: Int) -> Boolean = { true },
): Int? {
    val rows = exportRows(context, weights)
    if (!accept(rows.size)) return null
    val zone = ZoneId.systemDefault()
    val foods = exportFoods(context, uri, zone)
    val products = (context.applicationContext as TerveysApp).products.all()
    val text = format.write(DataSet(rows, foods, products), zone)
    withContext(Dispatchers.IO) {
        context.contentResolver.openOutputStream(uri, "wt")?.use { it.write(text.toByteArray(Charsets.UTF_8)) }
            ?: error("no output stream")
    }
    return rows.size
}

private const val RW = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

/** Keeps access to the picked backup file across restarts; false when the provider refuses. */
fun Context.keepAccess(uri: Uri): Boolean = try {
    contentResolver.takePersistableUriPermission(uri, RW)
    true
} catch (e: SecurityException) {
    AppLog.e("backup.enable", e, "no persistable permission")
    false
}

fun Context.releaseAccess(uri: String) {
    runCatching { contentResolver.releasePersistableUriPermission(uri.toUri(), RW) }
        .onFailure { AppLog.w("backup.disable", "release failed", "error" to it.toString()) }
}
