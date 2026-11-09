package fi.jukkakot.terveyslappu.history

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.DataFormatter
import java.io.ByteArrayInputStream

/** Old binary Excel files (`.xls`, BIFF8) read with Apache POI's HSSF reader (okok-xls-import). */
object XlsTable {
    /** The compound-document signature every `.xls` starts with. */
    private val SIGNATURE = byteArrayOf(0xD0, 0xCF, 0x11, 0xE0, 0xA1, 0xB1, 0x1A, 0xE1)

    private fun byteArrayOf(vararg values: Int) = ByteArray(values.size) { values[it].toByte() }

    /** True when [bytes] is a compound document, whatever the file is named. */
    fun isXls(bytes: ByteArray): Boolean =
        bytes.size >= SIGNATURE.size && SIGNATURE.indices.all { bytes[it] == SIGNATURE[it] }

    /**
     * The first sheet as rows of cells as Excel displays them (missing cells are empty, trailing
     * empties dropped). Throws when the file is damaged or not a workbook.
     */
    fun read(bytes: ByteArray): List<List<String>> = HSSFWorkbook(ByteArrayInputStream(bytes)).use { book ->
        val sheet = book.getSheetAt(0)
        val formatter = DataFormatter()
        val rows = mutableListOf<List<String>>()
        for (r in 0..sheet.lastRowNum) {
            val row = sheet.getRow(r)
            if (row == null || row.lastCellNum < 0) {
                rows += emptyList<String>()
                continue
            }
            val cells = (0 until row.lastCellNum).map { c -> row.getCell(c)?.let { formatter.formatCellValue(it).trim() } ?: "" }
            rows += cells.dropLastWhile { it.isEmpty() }
        }
        rows
    }
}
