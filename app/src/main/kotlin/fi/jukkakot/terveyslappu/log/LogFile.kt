package fi.jukkakot.terveyslappu.log

import java.io.File

/**
 * The on-phone log: lines appended to `app.log` in [dir]. When it passes [maxBytesPerFile] it is
 * renamed to `app.log.1` (replacing the older one) and a new file starts, so the two files hold
 * at most about twice [maxBytesPerFile] of the newest lines.
 */
class LogFile(private val dir: File, private val maxBytesPerFile: Long = DEFAULT_MAX_BYTES_PER_FILE) {

    val current: File get() = File(dir, "app.log")
    private val previous: File get() = File(dir, "app.log.1")

    @Synchronized
    fun append(line: String) {
        dir.mkdirs()
        current.appendText(line + "\n")
        if (current.length() > maxBytesPerFile) {
            previous.delete()
            current.renameTo(previous)
        }
    }

    /** Existing log files, oldest first (what "Jaa loki" sends). */
    @Synchronized
    fun files(): List<File> = listOf(previous, current).filter { it.exists() && it.length() > 0 }

    /** All lines, oldest first. */
    @Synchronized
    fun readLines(): List<String> = files().flatMap { it.readLines() }.filter { it.isNotEmpty() }

    companion object {
        /** Two files of 512 KB: the newest 1 MB of lines is kept (app-log spec). */
        const val DEFAULT_MAX_BYTES_PER_FILE = 512L * 1024
    }
}
