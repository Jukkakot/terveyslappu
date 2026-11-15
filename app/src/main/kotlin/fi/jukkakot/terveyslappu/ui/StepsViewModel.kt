package fi.jukkakot.terveyslappu.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fi.jukkakot.terveyslappu.TerveysApp
import fi.jukkakot.terveyslappu.log.AppLog
import fi.jukkakot.terveyslappu.steps.HealthConnectSteps
import fi.jukkakot.terveyslappu.steps.StepsAppNames
import fi.jukkakot.terveyslappu.steps.StepsChoice
import fi.jukkakot.terveyslappu.steps.StepsController
import fi.jukkakot.terveyslappu.steps.StepsLogger
import fi.jukkakot.terveyslappu.steps.StepsSourcesUi
import fi.jukkakot.terveyslappu.steps.StepsUiState
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

/** Writes the steps lines to the app log. */
val AppStepsLogger = StepsLogger { ok, event, message, error, fields ->
    if (ok) AppLog.i(event, message, *fields.toTypedArray()) else AppLog.e(event, error, message, *fields.toTypedArray())
}

/**
 * Steps read from Health Connect (steps design 5, steps-source-and-axes design 4, 5): the logic is
 * [StepsController]; this wires Health Connect, the `steps` prefs, readable names and the log.
 * Re-read in full on every resume and whenever the home screen or Askeleet is shown.
 */
class StepsViewModel(app: Application) : AndroidViewModel(app) {
    val steps = HealthConnectSteps(app)

    private val controller = StepsController(
        source = steps,
        store = (app as TerveysApp).stepsPrefs,
        scope = viewModelScope,
        log = AppStepsLogger,
        names = StepsAppNames(app)::name,
    )

    val state: StateFlow<StepsUiState> = controller.state
    val sources: StateFlow<StepsSourcesUi> = controller.sources

    fun show(from: LocalDate?) = controller.show(from)

    fun refresh() = controller.refresh()

    fun onPermissionRequested() = controller.onPermissionRequested()

    fun onPermissionResult(granted: Set<String>) = controller.onPermissionResult(granted.containsAll(steps.permissions))

    fun setChoice(choice: StepsChoice) = controller.setChoice(choice)

    fun subScreenShown() = controller.subScreenShown()

    fun subScreenHidden() = controller.subScreenHidden()
}
