package fi.jukkakot.terveyslappu.history

import org.junit.Test
import kotlin.test.assertEquals

class TableTest {
    @Test
    fun `semicolon file with comma decimals`() {
        val lines = listOf("Date;Weight", "2024-03-01;82,4", "2024-03-02;82,1")
        assertEquals(';', Table.detectSeparator(lines))
    }

    @Test
    fun `tab file`() {
        assertEquals('\t', Table.detectSeparator(listOf("a\tb\tc", "1\t2\t3")))
    }

    @Test
    fun `comma file with quoted decimals`() {
        val lines = listOf("date,weight", "2024-03-01,\"82,4\"")
        assertEquals(',', Table.detectSeparator(lines))
        assertEquals(listOf("2024-03-01", "82,4"), Table.split(lines[1], ','))
    }

    @Test
    fun `escaped quotes and trimming`() {
        assertEquals(listOf("a \"b\"", "c"), Table.split("\"a \"\"b\"\"\" ,  c ", ','))
    }
}
