package fi.jukkakot.terveyslappu

import android.app.Application
import android.os.Build
import android.util.Log
import fi.jukkakot.terveyslappu.log.AppLog
import fi.jukkakot.terveyslappu.log.CrashHandler
import fi.jukkakot.terveyslappu.log.Level
import fi.jukkakot.terveyslappu.log.LogFile
import java.io.File
import java.time.Instant
import androidx.core.net.toUri
import fi.jukkakot.terveyslappu.backup.PrefsBackupStore
import fi.jukkakot.terveyslappu.backup.WeightBackup
import fi.jukkakot.terveyslappu.backup.writeExport
import fi.jukkakot.terveyslappu.profile.ProfilePrefs
import fi.jukkakot.terveyslappu.listen.AndroidListenAlarm
import fi.jukkakot.terveyslappu.listen.AndroidListenDevice
import fi.jukkakot.terveyslappu.listen.AndroidListenRadio
import fi.jukkakot.terveyslappu.listen.ListenController
import fi.jukkakot.terveyslappu.listen.ListenPrefs
import fi.jukkakot.terveyslappu.raw.RawArchive
import fi.jukkakot.terveyslappu.scale.LastWeighInPrefs
import fi.jukkakot.terveyslappu.ui.AppearancePrefs
import fi.jukkakot.terveyslappu.weight.HealthConnectWeights
import fi.jukkakot.terveyslappu.weight.HistoryAccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class TerveysApp : Application() {

    /** App-wide work that must outlive a screen (the backup after a tile weigh-in). */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    lateinit var backup: WeightBackup
        private set

    /** Raw data behind the weights (scale packets, imported fields), in app storage. */
    val rawArchive: RawArchive by lazy { RawArchive(File(filesDir, "raw-archive.jsonl")) }

    /** The phone owner's details (owner-profile). */
    val profile: ProfilePrefs by lazy { ProfilePrefs(this) }

    /** The background listen trial (background-weigh-in); receivers and Asetukset share it. */
    val listen: ListenController by lazy {
        ListenController(ListenPrefs(this), AndroidListenDevice(this, LastWeighInPrefs(this)), AndroidListenRadio(this), AndroidListenAlarm(this))
    }

    /** The theme choice (app-appearance), read by every activity's theme. */
    val appearance: AppearancePrefs by lazy { AppearancePrefs(this) }

    override fun onCreate() {
        super.onCreate()
        AppLog.install(LogFile(File(filesDir, "logs"))) { level, line ->
            when (level) {
                Level.DEBUG -> Log.d(TAG, line)
                Level.INFO -> Log.i(TAG, line)
                Level.WARN -> Log.w(TAG, line)
                Level.ERROR -> Log.e(TAG, line)
            }
        }
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(Thread.getDefaultUncaughtExceptionHandler()))
        val weights = HealthConnectWeights(this)
        backup = WeightBackup(
            PrefsBackupStore(this),
            { uri, accept -> writeExport(this, weights, uri.toUri(), accept = accept) },
            Instant::now,
            appScope,
            recentOnly = { runCatching { weights.historyAccess() == HistoryAccess.NOT_GRANTED }.getOrDefault(false) },
        )
        // The widget and tile no longer show the latest weight; drop the old cache (weigh-in-button-redo).
        deleteSharedPreferences("last_weight")
        AppLog.i(
            "app.start", "",
            "version" to BuildConfig.VERSION_NAME,
            "commit" to BuildConfig.GIT_SHA,
            "device" to "${Build.MANUFACTURER} ${Build.MODEL}",
            "android" to Build.VERSION.RELEASE,
        )
        listen.onAppStart()
    }

    private companion object {
        const val TAG = "Terveyslappu"
    }
}
