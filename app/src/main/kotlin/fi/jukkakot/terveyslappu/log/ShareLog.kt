package fi.jukkakot.terveyslappu.log

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import fi.jukkakot.terveyslappu.R
import java.io.File

/** The share-sheet intent for the log [files], readable by the receiving app through our FileProvider. */
fun shareLogIntent(context: Context, files: List<File>): Intent {
    val uris = files.map { FileProvider.getUriForFile(context, "${context.packageName}.files", it) }
    val send = if (uris.size == 1) {
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uris.single())
        }
    } else {
        Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "text/plain"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
        }
    }.apply {
        putExtra(Intent.EXTRA_SUBJECT, "terveyslappu app.log")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    return Intent.createChooser(send, context.getString(R.string.log_share_chooser))
}
