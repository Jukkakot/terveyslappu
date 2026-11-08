package fi.jukkakot.terveyslappu.log

import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.File
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class ShareLogTest {
    @Test
    fun `both log files go to the share sheet`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val log = LogFile(File(context.filesDir, "logs/test") /* the app logs to logs/ itself */, maxBytesPerFile = 50)
        repeat(10) { log.append("line $it with some padding") }
        assertEquals(2, log.files().size, "rotated log")
        val chooser = shareLogIntent(context, log.files())
        val send = chooser.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)!!
        assertEquals(Intent.ACTION_SEND_MULTIPLE, send.action)
        assertEquals(2, send.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)!!.size)
    }
}
