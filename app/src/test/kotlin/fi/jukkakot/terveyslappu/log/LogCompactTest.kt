package fi.jukkakot.terveyslappu.log

import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LogCompactTest {
    private val now = Instant.parse("2026-11-14T10:00:00Z")
    private val zone = ZoneOffset.ofHours(2)

    private fun build(lines: List<String>) = LogCompact.build(lines, "1.0.5", "abc1234", now, zone)

    private fun packet(time: String, data: String, rssi: Int, key: String = "12c0") =
        "$time D scale.packet t=1 key=$key data=$data rssi=$rssi name=Test addr=AA:BB"

    @Test
    fun `header has version, commit and local copy time`() {
        val result = build(listOf("2026-11-14T11:00:00.000+02:00 I app.start"))
        assertEquals("terveyslappu v1.0.5 commit=abc1234 copied=2026-11-14T12:00:00+02:00", result.text.lines().first())
        assertEquals(1, result.lines)
    }

    @Test
    fun `lines older than 24 hours are dropped`() {
        val result = build(
            listOf(
                "2026-11-13T11:59:00.000+02:00 I old",
                "2026-11-13T12:01:00.000+02:00 I recent",
                "2026-11-14T11:00:00.000+02:00 I newest",
            ),
        )
        assertEquals(listOf("2026-11-13T12:01:00.000+02:00 I recent", "2026-11-14T11:00:00.000+02:00 I newest"), result.text.lines().drop(1))
    }

    @Test
    fun `same key and data collapse with count and rssi range`() {
        val out = LogCompact.collapsePackets(
            listOf(
                "2026-11-14T11:00:00.000+02:00 I weight.save ok",
                packet("2026-11-14T11:00:01.000+02:00", "aa01", -60),
                packet("2026-11-14T11:00:02.000+02:00", "aa01", -72),
                packet("2026-11-14T11:00:03.000+02:00", "aa01", -65),
                packet("2026-11-14T11:00:04.000+02:00", "aa02", -61),
                packet("2026-11-14T11:00:05.000+02:00", "aa01", -61),
            ),
        )
        assertEquals(4, out.size)
        assertEquals(packet("2026-11-14T11:00:01.000+02:00", "aa01", -60) + " x3 rssi=-72..-60", out[1])
        assertEquals(packet("2026-11-14T11:00:04.000+02:00", "aa02", -61), out[2])
    }

    @Test
    fun `a different key breaks the run`() {
        val out = LogCompact.collapsePackets(
            listOf(
                packet("2026-11-14T11:00:01.000+02:00", "aa01", -60, key = "12c0"),
                packet("2026-11-14T11:00:02.000+02:00", "aa01", -60, key = "12c1"),
            ),
        )
        assertEquals(2, out.size)
    }

    @Test
    fun `too long keeps the newest lines and counts the rest`() {
        val lines = (0 until 2000).map { "2026-11-14T11:00:00.000+02:00 I filler n=%04d ${"x".repeat(50)}".format(it) }
        val result = build(lines)
        val text = result.text.lines()
        assertTrue(result.text.length <= LogCompact.MAX_CHARS)
        assertTrue(text[1].matches(Regex("""\.\.\. \d+ older lines left out""")))
        assertEquals(lines.last(), text.last())
        val dropped = text[1].split(' ')[1].toInt()
        assertEquals(2000, dropped + result.lines)
        assertEquals(lines[dropped], text[2], "kept lines are the newest, contiguous")
    }

    @Test
    fun `short logs have no notice`() {
        assertTrue(build(listOf("2026-11-14T11:00:00.000+02:00 I a")).text.lines().none { it.startsWith("...") })
    }
}
