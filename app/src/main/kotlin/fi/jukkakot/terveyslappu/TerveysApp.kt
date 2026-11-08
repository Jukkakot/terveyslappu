package fi.jukkakot.terveyslappu

import android.app.Application
import android.os.Build
import android.util.Log
import fi.jukkakot.terveyslappu.log.AppLog
import fi.jukkakot.terveyslappu.log.CrashHandler
import fi.jukkakot.terveyslappu.log.Level
import fi.jukkakot.terveyslappu.log.LogFile
import java.io.File

class TerveysApp : Application() {
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
        AppLog.i(
            "app.start", "",
            "version" to BuildConfig.VERSION_NAME,
            "device" to "${Build.MANUFACTURER} ${Build.MODEL}",
            "android" to Build.VERSION.RELEASE,
        )
    }

    private companion object {
        const val TAG = "Terveyslappu"
    }
}
