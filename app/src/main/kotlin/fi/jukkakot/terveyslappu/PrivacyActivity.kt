package fi.jukkakot.terveyslappu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.res.stringResource
import fi.jukkakot.terveyslappu.ui.TerveysTheme

/** Health Connect opens this when the user asks why the app wants weight access. */
class PrivacyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TerveysTheme {
                AlertDialog(
                    onDismissRequest = ::finish,
                    title = { Text(stringResource(R.string.privacy_title)) },
                    text = { Text(stringResource(R.string.privacy_body)) },
                    confirmButton = { TextButton(onClick = ::finish) { Text(stringResource(R.string.ok)) } },
                )
            }
        }
    }
}
