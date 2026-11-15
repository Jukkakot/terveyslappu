package fi.jukkakot.terveyslappu

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.health.connect.HealthConnectManager
import android.os.Bundle
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fi.jukkakot.terveyslappu.log.AppLog
import fi.jukkakot.terveyslappu.ui.FoodAccess
import fi.jukkakot.terveyslappu.ui.FoodActions
import fi.jukkakot.terveyslappu.ui.FoodAddDialog
import fi.jukkakot.terveyslappu.ui.FoodClipboard
import fi.jukkakot.terveyslappu.ui.FoodDialogActions
import fi.jukkakot.terveyslappu.ui.FoodViewModel
import fi.jukkakot.terveyslappu.ui.TerveysTheme

/**
 * The Lisää annos dialog (food-widget), opened by [FoodAddWidget] and the launcher shortcut: the
 * app's add sheet over whatever is on screen, in its own task like the weigh-in dialog. It adds to
 * the day it opened on; closing the sheet finishes it, and only the import buttons open the app.
 */
class FoodAddActivity : ComponentActivity() {

    private val model: FoodViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(PermissionController.createRequestPermissionResultContract()) { granted ->
        model.onPermissionResult(granted)
    }

    /** NFR-8 timings from the tap: the field usable, the day read (logged once per open). */
    private var openedAt = 0L
    private var fieldMs = -1L
    private var timingLogged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logOpen(intent)
        // home-card-layout design 4: the front page's edit mode ends and drops its draft.
        if (savedInstanceState == null) fi.jukkakot.terveyslappu.home.HomeEditSignal.outsideIntent("foodAdd")
        // A recreated dialog (rotation) keeps its state and logs no new timings.
        timingLogged = savedInstanceState != null
        model.startDialog(from(intent))
        setContent {
            TerveysTheme {
                val food by model.state.collectAsStateWithLifecycle()
                // Closing the sheet (back, a tap outside, a swipe) finishes the dialog; the editor replaces the sheet without closing it.
                LaunchedEffect(food.sheetDay == null && food.editing == null) {
                    if (food.sheetDay == null && food.editing == null) finish()
                }
                LaunchedEffect(food.access) {
                    if (food.access == FoodAccess.GRANTED && fieldMs < 0) fieldMs = elapsed()
                }
                LaunchedEffect(food.items != null || food.loadFailed) {
                    if ((food.items != null || food.loadFailed) && !timingLogged && fieldMs >= 0) {
                        timingLogged = true
                        AppLog.i("food.sheet.timing", "", "field" to fieldMs, "read" to elapsed(), "ok" to (food.items != null))
                    }
                }
                FoodAddDialog(
                    food,
                    model.base,
                    FoodActions(
                        onCloseSheet = model::closeSheet,
                        onQuery = model::onQuery,
                        onPick = model::addSuggestion,
                        onEditPick = model::editDraft,
                        onAddDraft = model::addDraft,
                        onEnter = model::addTyped,
                        onUndoAdd = model::undoAdd,
                        onUndoDelete = model::undoDelete,
                        onEdit = model::openEditor,
                        onCloseEditor = model::closeEditor,
                        onSave = model::saveEdit,
                        onDelete = model::delete,
                        onPasteAi = { handOver(FoodClipboard.text(this), "paste") },
                        onCopyAiPrompt = { FoodClipboard.copyPrompt(this, model) },
                        onClipOpen = {
                            val text = FoodClipboard.text(this)
                            if (model.clipMatches(text)) {
                                model.clipOpened()
                                handOver(text, "paste")
                            }
                        },
                        onClipSkip = model::skipClipOffer,
                        onUndoClipSkip = model::undoClipSkip,
                        // Lisää kaikki saves in the sheet itself; the sheet stays open (food-log "Import from the home-screen sheet").
                        onClipAddAll = { day -> model.addAllFromClip(FoodClipboard.text(this), day) },
                        onUndoImport = model::undoImport,
                        onDeleteProduct = model::deleteProduct,
                        onUndoDeleteProduct = model::undoDeleteProduct,
                    ),
                    FoodDialogActions(
                        onGrant = {
                            model.onPermissionRequested()
                            launchSafely { permissionLauncher.launch(model.permissions) }
                        },
                        onOpenSettings = {
                            launchSafely { startActivity(Intent(HealthConnectManager.ACTION_MANAGE_HEALTH_PERMISSIONS).putExtra(Intent.EXTRA_PACKAGE_NAME, packageName)) }
                        },
                        onFocused = ::checkClip,
                    ),
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        fi.jukkakot.terveyslappu.home.HomeEditSignal.outsideIntent("foodAdd")
        // A second tap while open keeps the sheet as it is (design 11).
        logOpen(intent)
    }

    override fun onResume() {
        super.onResume()
        // The permission may have changed in Health Connect's settings (design 9).
        model.refresh()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) checkClip()
    }

    /** The clipboard is checked on every focus gain (Android 10+ allows the read only then); text is read only for a changed clip. */
    private fun checkClip() = FoodClipboard.checkOffer(this, model)

    /** Tuo ruoka-annokset and Näytä: the app opens on the preview over the front page (design 5). */
    private fun handOver(text: String, from: String) {
        launchSafely { startActivity(MainActivity.importIntent(this, text, from)) }
        model.closeSheet()
        finish()
    }

    private fun logOpen(intent: Intent?) {
        openedAt = SystemClock.elapsedRealtime()
        AppLog.i("food.sheet.open", "", "from" to from(intent))
    }

    private fun elapsed(): Long = SystemClock.elapsedRealtime() - openedAt

    private fun from(intent: Intent?): String = intent?.getStringExtra(EXTRA_FROM) ?: "unknown"

    private fun launchSafely(launch: () -> Unit) {
        try {
            launch()
        } catch (e: ActivityNotFoundException) {
            AppLog.e("intent.open", e, "food.sheet")
            Toast.makeText(this, R.string.open_failed, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        /** Which start opened the dialog, for the log: widget or shortcut. */
        const val EXTRA_FROM = "from"

        /** A running dialog is reused (singleTask), so a second tap keeps its state. */
        fun intent(context: Context, from: String): Intent =
            Intent(context, FoodAddActivity::class.java)
                .putExtra(EXTRA_FROM, from)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
