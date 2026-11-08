package fi.jukkakot.terveyslappu.log

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LogFileTest {
    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun `lines are appended oldest first`() {
        val log = LogFile(tmp.root)
        log.append("one")
        log.append("two")
        assertEquals(listOf("one", "two"), log.readLines())
        assertEquals(1, log.files().size)
    }

    @Test
    fun `rotation keeps the newest lines in two files`() {
        val log = LogFile(tmp.root, maxBytesPerFile = 100)
        repeat(32) { log.append("line %02d xxxxxxxx".format(it)) } // 17 bytes per line, rotates every 6th
        val files = log.files()
        assertEquals(listOf("app.log.1", "app.log"), files.map { it.name })
        assertTrue(files.all { it.length() <= 100 + 17 })
        val lines = log.readLines()
        assertEquals("line 31 xxxxxxxx", lines.last())
        assertTrue(lines.size < 32, "oldest lines dropped")
        assertEquals(lines.sorted(), lines, "still in order")
    }

    @Test
    fun `line format has time, level, event, message and fields`() {
        val line = LogLine.format(
            Instant.parse("2026-11-08T05:12:03.123Z"), ZoneOffset.ofHours(2), Level.INFO,
            "weight.save", "ok", listOf("kg" to 82.4, "note" to "a\nb"),
        )
        assertEquals("2026-11-08T07:12:03.123+02:00 I weight.save ok kg=82.4 note=a\\nb", line)
    }
}
