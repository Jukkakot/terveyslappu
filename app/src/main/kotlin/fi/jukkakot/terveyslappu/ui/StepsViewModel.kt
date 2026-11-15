package fi.jukkakot.terveyslappu.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fi.jukkakot.terveyslappu.TerveysApp
import fi.jukkakot.terveyslappu.burn.BurnController
import fi.jukkakot.terveyslappu.burn.BurnUiState
import fi.jukkakot.terveyslappu.burn.HealthConnectBurn
import fi.jukkakot.terveyslappu.log.AppLog
import fi.jukkakot.terveyslappu.steps.HealthConnectSteps
import fi.jukkakot.terveyslappu.steps.StepsAppNames
import fi.jukkakot.terveyslappu.steps.StepsChoice
import fi.jukkakot.terveyslappu.steps.StepsController
import fi.jukkakot.terveyslappu.steps.StepsLogger
import fi.jukkakot.terveyslappu.steps.StepsSourcesUi
import fi.jukkakot.terveyslappu.steps.StepsUiState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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

    private val garmin = (app as TerveysApp).garminHistory

    private val names = StepsAppNames(app)

    private val controller = StepsController(
        source = steps,
        store = (app as TerveysApp).stepsPrefs,
        scope = viewModelScope,
        log = AppStepsLogger,
        names = names::name,
        garmin = { garmin.days.value },
    )

    /** Kulutus (energy-burn design 3): read from the steps source in use, so it lives next to the steps controller. */
    val burnSource = HealthConnectBurn(app)

    private val burnController = BurnController(
        source = burnSource,
        scope = viewModelScope,
        log = AppStepsLogger,
        stepsSource = { controller.current() },
        names = names::name,
        garmin = { garmin.days.value },
    )

    init {
        // An import, Poista or Kumoa (MainViewModel) re-merges at once (garmin-steps-history design 6).
        viewModelScope.launch {
            garmin.days.drop(1).collect {
                controller.garminChanged()
                burnController.garminChanged()
            }
        }
        viewModelScope.launch { controller.state.map { it.source }.distinctUntilChanged().drop(1).collect { burnController.sourceChanged() } }
    }

    val burn: StateFlow<BurnUiState> = burnController.state

    fun showBurn(from: LocalDate?) = burnController.show(from)

    fun onBurnPermissionRequested() = burnController.onPermissionRequested()

    fun onBurnPermissionResult(granted: Set<String>) = burnController.onPermissionResult(burnSource.grantOf(granted))

    val state: StateFlow<StepsUiState> = controller.state
    val sources: StateFlow<StepsSourcesUi> = controller.sources

    fun show(from: LocalDate?) = controller.show(from)

    fun refresh() {
        controller.refresh()
        burnController.refresh()
    }

    fun onPermissionRequested() = controller.onPermissionRequested()

    fun onPermissionResult(granted: Set<String>) = controller.onPermissionResult(granted.containsAll(steps.permissions))

    fun setChoice(choice: StepsChoice) = controller.setChoice(choice)

    fun subScreenShown() = controller.subScreenShown()

    fun subScreenHidden() = controller.subScreenHidden()
}
