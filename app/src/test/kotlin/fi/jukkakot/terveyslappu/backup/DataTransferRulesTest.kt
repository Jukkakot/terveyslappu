package fi.jukkakot.terveyslappu.backup

import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.assertEquals

/** Android backup and phone-to-phone transfer carry exactly the allow-list, never the log or the backup URI. */
class DataTransferRulesTest {
    private val rules = DocumentBuilderFactory.newInstance()
        .newDocumentBuilder()
        .parse(File("src/main/res/xml/data_extraction_rules.xml"))
        .documentElement

    private val expected = setOf(
        "sharedpref:profile.xml",
        "sharedpref:appearance.xml",
        "sharedpref:chart.xml",
        "file:raw-archive.jsonl",
        "file:products.json",
    )

    private fun includes(section: String): Set<String> {
        val element = rules.getElementsByTagName(section).item(0) as Element
        val nodes = element.getElementsByTagName("include")
        return (0 until nodes.length).map {
            val include = nodes.item(it) as Element
            "${include.getAttribute("domain")}:${include.getAttribute("path")}"
        }.toSet()
    }

    @Test
    fun cloudBackupCarriesOnlyTheAllowListAndNeedsEncryption() {
        assertEquals(expected, includes("cloud-backup"))
        val cloud = rules.getElementsByTagName("cloud-backup").item(0) as Element
        assertEquals("true", cloud.getAttribute("disableIfNoEncryptionCapabilities"))
    }

    @Test
    fun deviceTransferCarriesOnlyTheAllowList() {
        assertEquals(expected, includes("device-transfer"))
    }
}
