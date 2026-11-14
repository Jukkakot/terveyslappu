package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R

/** Title and body string of each help section (help-page spec); automatic weigh-in is left out on purpose. */
private val helpSections = listOf(
    R.string.help_weigh_title to R.string.help_weigh_body,
    R.string.help_manual_title to R.string.help_manual_body,
    R.string.help_history_title to R.string.help_history_body,
    R.string.help_food_title to R.string.help_food_body,
    R.string.help_steps_title to R.string.help_steps_body,
    R.string.help_profile_title to R.string.help_profile_body,
    R.string.help_data_title to R.string.help_data_body,
    R.string.help_backup_title to R.string.help_backup_body,
    R.string.help_log_title to R.string.help_log_body,
)

/** Ohje: a light, scrollable presentation of the features (help-page spec); back returns Home. */
@Composable
internal fun HelpScreen(actions: MainActions) {
    Scaffold(topBar = { SubScreenBar(stringResource(R.string.help_title), onBack = actions.onBack) }) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
                .testTag("helpList"),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.help_intro),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp),
            )
            helpSections.forEach { (title, body) ->
                Surface(
                    shape = groupShape,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(title), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Text(stringResource(body), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
