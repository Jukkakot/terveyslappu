package fi.jukkakot.terveyslappu.raw

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Made-up raw data (NFR-1). */
class RawArchiveTest {
    @get:Rule
    val folder = TemporaryFolder()

    private fun entry(second: Long, impedance: String = "5123") =
        RawEntry(second, "scale", linkedMapOf("packet" to "0339", "key" to "12c0", "impedance" to impedance))

    @Test
    fun `entries survive a reload in order`() {
        val file = folder.root.resolve("raw.jsonl")
        RawArchive(file).apply {
            add(entry(20))
            add(entry(10))
        }
        val reloaded = RawArchive(file)
        assertEquals(listOf(entry(10), entry(20)), reloaded.all())
        assertEquals(listOf("packet", "key", "impedance"), reloaded.get(20)!!.fields.keys.toList())
    }

    @Test
    fun `same second replaces, remove returns the entry`() {
        val file = folder.root.resolve("raw.jsonl")
        val archive = RawArchive(file)
        archive.add(entry(10))
        archive.add(entry(10, impedance = "0"))
        assertEquals(entry(10, impedance = "0"), archive.remove(10))
        assertNull(archive.remove(10))
        assertEquals(emptyList(), RawArchive(file).all())
    }

    @Test
    fun `a broken line does not lose the others`() {
        val file = folder.root.resolve("raw.jsonl")
        RawArchive(file).addAll(listOf(entry(10), entry(20)))
        file.appendText("{not json\n")
        assertEquals(2, RawArchive(file).all().size)
    }

    @Test
    fun `raw cell round trip with escapes`() {
        val odd = RawEntry(5, "okok", linkedMapOf("a" to "x|y", "b" to "back\\slash", "c" to "k=v"))
        assertEquals(odd, RawFormat.decode(RawFormat.encode(odd), 5))
        assertNull(RawFormat.decode("", 5))
        assertNull(RawFormat.decode("key=12c0", 5))
    }
}
