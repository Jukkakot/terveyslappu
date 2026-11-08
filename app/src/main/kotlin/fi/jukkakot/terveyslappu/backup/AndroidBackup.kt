package fi.jukkakot.terveyslappu.backup

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.edit
import androidx.core.net.toUri
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.history.CsvExport
import fi.jukkakot.terveyslappu.history.ExportRow
import fi.jukkakot.terveyslappu.log.AppLog
import fi.jukkakot.terveyslappu.weight.HealthConnectWeights
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId

/** Backup status in SharedPreferences (app-private, not backed up: allowBackup=false). */
class PrefsBackupStore(context: Context) : BackupStore {
    private val prefs = context.getSharedPreferences("backup", Context.MODE_PRIVATE)

    override fun load() = BackupStatus(
        uri = prefs.getString(URI, null),
        lastOkAt = prefs.getLong(OK_AT, 0).takeIf { it > 0 }?.let(Instant::ofEpochMilli),
        lastCount = prefs.getInt(COUNT, 0),
        lastFailedAt = prefs.getLong(FAILED_AT, 0).takeIf { it > 0 }?.let(Instant::ofEpochMilli),
    )

    override fun save(status: BackupStatus) = prefs.edit {
        putString(URI, status.uri)
        putLong(OK_AT, status.lastOkAt?.toEpochMilli() ?: 0)
        putInt(COUNT, status.lastCount)
        putLong(FAILED_AT, status.lastFailedAt?.toEpochMilli() ?: 0)
    }

    private companion object {
        const val URI = "uri"
        const val OK_AT = "okAt"
        const val COUNT = "count"
        const val FAILED_AT = "failedAt"
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
    return weights.all(Instant.now()).map {
        ExportRow(it.time, it.zoneOffset, it.kg, if (it.own) ownLabel else labels.getOrPut(it.sourcePackage) { context.appLabel(it.sourcePackage) })
    }
}

/** Writes all weights as the export CSV to [uri] (rewrites the whole file); returns the count. */
suspend fun writeExport(context: Context, weights: HealthConnectWeights, uri: Uri): Int {
    val rows = exportRows(context, weights)
    val text = CsvExport.write(rows, ZoneId.systemDefault())
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
