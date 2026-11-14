package fi.jukkakot.terveyslappu.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.food.ClipOffers
import fi.jukkakot.terveyslappu.log.AppLog

/** The food log's clipboard use, shared by the main screen and the Lisää annos dialog (food-ai-import). */
object FoodClipboard {
    /** The clipboard's text, empty when there is none (Tuo ruoka-annokset). */
    fun text(context: Context): String {
        val clip = context.getSystemService(ClipboardManager::class.java)?.primaryClip
        return if (clip != null && clip.itemCount > 0) clip.getItemAt(0).coerceToText(context).toString() else ""
    }

    /**
     * The clipboard offer: called only while Ruoka or Lisää annos is shown. The description is checked
     * first (no paste toast); the text is read only for a text clip newer than the last offered one.
     */
    fun checkOffer(context: Context, model: FoodViewModel) {
        val manager = context.getSystemService(ClipboardManager::class.java) ?: return
        val description = manager.primaryClipDescription ?: return
        val time = description.timestamp
        if (!ClipOffers.shouldRead(description.hasMimeType("text/*"), time, model.lastOfferedClip())) return
        model.offerClip(time, text(context))
    }

    /** Kopioi tuontiohje: assets/ai-food-prompt.txt as is. */
    fun copyPrompt(context: Context) {
        runCatching { context.assets.open("ai-food-prompt.txt").bufferedReader().use { it.readText() } }
            .onSuccess {
                context.getSystemService(ClipboardManager::class.java)?.setPrimaryClip(ClipData.newPlainText("terveyslappu ai prompt", it))
                AppLog.i("food.prompt.copy", "", "chars" to it.length)
                Toast.makeText(context, R.string.ai_prompt_copied, Toast.LENGTH_SHORT).show()
            }
            .onFailure {
                AppLog.e("food.prompt.copy", it, "failed")
                Toast.makeText(context, R.string.ai_prompt_failed, Toast.LENGTH_SHORT).show()
            }
    }
}
