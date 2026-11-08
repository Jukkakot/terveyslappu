package fi.jukkakot.terveyslappu.log

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

enum class Level(val letter: Char) { DEBUG('D'), INFO('I'), WARN('W'), ERROR('E') }

/** One log line: `2026-11-08T07:12:03.123+02:00 I weight.save ok kg=82.4`. */
object LogLine {
    private val timeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")

    fun format(
        time: Instant,
        zone: ZoneId,
        level: Level,
        event: String,
        message: String,
        fields: List<Pair<String, Any?>> = emptyList(),
    ): String = buildString {
        append(timeFormat.format(time.atZone(zone)))
        append(' ').append(level.letter)
        append(' ').append(event)
        if (message.isNotEmpty()) append(' ').append(message.replace('\n', ' '))
        for ((key, value) in fields) append(' ').append(key).append('=').append(value.toString().replace("\n", "\\n"))
    }

    fun stackOf(error: Throwable): String = error.stackTraceToString().trim()
}

/**
 * The app's logger. Lines go to Logcat at once and to the [LogFile] on a background thread;
 * [writeNow] writes synchronously (crash handler). Not installed (tests): lines are dropped.
 */
object AppLog {
    @Volatile
    private var file: LogFile? = null

    @Volatile
    private var echo: (Level, String) -> Unit = { _, _ -> }

    private var executor: ExecutorService? = null

    val logFile: LogFile? get() = file

    fun install(logFile: LogFile, logcat: (Level, String) -> Unit) {
        file = logFile
        echo = logcat
        executor = Executors.newSingleThreadExecutor { Thread(it, "app-log").apply { isDaemon = true } }
    }

    fun d(event: String, message: String = "", vararg fields: Pair<String, Any?>) = log(Level.DEBUG, event, message, fields.toList())
    fun i(event: String, message: String = "", vararg fields: Pair<String, Any?>) = log(Level.INFO, event, message, fields.toList())
    fun w(event: String, message: String = "", vararg fields: Pair<String, Any?>) = log(Level.WARN, event, message, fields.toList())

    fun e(event: String, error: Throwable?, message: String = "", vararg fields: Pair<String, Any?>) {
        val all = fields.toList() + listOfNotNull(error?.let { "error" to it.toString() }, error?.let { "stack" to LogLine.stackOf(it) })
        log(Level.ERROR, event, message, all)
    }

    fun writeNow(level: Level, event: String, message: String, fields: List<Pair<String, Any?>>) {
        val line = line(level, event, message, fields)
        echo(level, line)
        file?.append(line)
    }

    /** Waits until queued lines are in the file (before sharing it). */
    fun flush() {
        executor?.submit {}?.runCatching { get(2, TimeUnit.SECONDS) }
    }

    private fun log(level: Level, event: String, message: String, fields: List<Pair<String, Any?>>) {
        val line = line(level, event, message, fields)
        echo(level, line)
        val target = file ?: return
        executor?.execute { runCatching { target.append(line) } }
    }

    private fun line(level: Level, event: String, message: String, fields: List<Pair<String, Any?>>) =
        LogLine.format(Instant.now(), ZoneId.systemDefault(), level, event, message, fields)
}
