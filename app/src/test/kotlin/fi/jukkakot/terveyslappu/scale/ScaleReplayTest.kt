package fi.jukkakot.terveyslappu.scale

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Replays every `.replay` file in the `scale-replays` test resources (NFR-7). A fixture is a shared weigh-in log's
 * `scale.packet` lines with made-up weights (NFR-1) and a first line `# expect kg=82.5` or
 * `# expect none`.
 */
class ScaleReplayTest {
    @Test
    fun `every fixture reaches its expected weight`() {
        val dir = File(checkNotNull(javaClass.classLoader?.getResource("scale-replays")) { "no fixtures" }.toURI())
        val files = dir.listFiles { f -> f.extension == "replay" }.orEmpty().sortedBy { it.name }
        assertTrue(files.isNotEmpty(), "no fixtures found")
        for (file in files) {
            val lines = file.readLines()
            val expect = lines.first().removePrefix("# expect").trim()
            val result = PacketLog.replay(lines)
            val actual = result?.let { "kg=${it.kg}" } ?: "none"
            assertEquals(expect, actual, file.name)
        }
    }
}
