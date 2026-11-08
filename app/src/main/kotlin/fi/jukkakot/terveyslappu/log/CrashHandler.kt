package fi.jukkakot.terveyslappu.log

/** Writes an uncaught error to the log synchronously (the process is about to die), then hands over. */
class CrashHandler(private val previous: Thread.UncaughtExceptionHandler?) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, error: Throwable) {
        runCatching {
            AppLog.writeNow(
                Level.ERROR, "app.crash", error.toString(),
                listOf("thread" to thread.name, "stack" to LogLine.stackOf(error)),
            )
        }
        previous?.uncaughtException(thread, error)
    }
}
