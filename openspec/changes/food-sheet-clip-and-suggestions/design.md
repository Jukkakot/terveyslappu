# Design

## Context

- The offer rule lives in `food/ClipOffer.kt` (`ClipOffers`, pure), the reading in
  `ui/FoodClipboard.checkOffer`, the state in `FoodViewModel` (`clipOffer`, prefs key `offered` =
  the last offered clip's `ClipDescription.timestamp`). Today any read marks the clip offered, which
  is why the widget sheet often shows nothing.
- Android 12+ shows a "pasted from clipboard" toast whenever an app reads another app's clip text,
  so reading the text on every open would toast every time. The description (mime type,
  timestamp) can be read without a toast.
- Suggestions: `Suggestions.atEmpty` (order rule unchanged) drawn as a `FlowRow` of chips in
  `ui/FoodSheet.AddFoodContent`; typed matches are already rows. The item editor `FoodEditor`
  edits a logged `FoodItem`.

## Goals / Non-Goals

**Goals:** offer until imported or skipped, without a paste toast on every open; one row form for
suggestions and matches; edit before adding.

**Non-Goals:** changing the suggestion order rule, the preview itself, or the item editor for
logged items.

## Decisions

1. **Content key = hash of the parsed JSON.** `ClipOffers.key(parsed: AiParsed): String` = SHA-256
   (hex, first 16 chars) of a canonical text of the parsed items and products (names, amounts,
   units, kcal, treat, product energy) in order. Surrounding chat text, whitespace and key order do
   not change it. Alternative (hash of the raw text) was rejected: the same JSON copied with
   different surrounding text would be offered again.
2. **Done keys** are kept in the existing clip prefs as a JSON list of at most 50 keys (oldest
   dropped). A key is added on Ohita and when the preview's Lisää saves (`addAi`), whatever opened
   the preview (offer, Tuo ruoka-annokset, share): `AiPreview` carries `sourceKey`. Kumoa of the
   import does not remove the key (simplest; the user can still use Tuo ruoka-annokset).
3. **Read only changed clips, cache the result.** Prefs keep the last read clip's timestamp and its
   result (key, count, kcal, productsOnly, or "no food JSON"). On open: description has no text
   → no offer; timestamp equals the cached one → use the cached result without reading; else read
   the text, parse, cache. An offer is shown when the result has a key that is not done. The text
   for the preview is read again on tap (a toast then is expected). A 0 timestamp (unknown) is
   always read, as a fallback.
4. **Offer object** `ClipOffer(key, count, kcal: Int?, productsOnly)`; the text is no longer kept.
   Tapping reads the clipboard text, parses it and opens the preview; if the clip has changed
   meanwhile, whatever food JSON it now holds opens (or the usual "no JSON" reason).
5. **Offer button** `ClipOfferButton`: a full-width tonal button (min 56 dp) with the text and a
   trailing small Ohita text button, placed right under the field in `AddFoodContent` (above the
   "Muuta · Kumoa" line) and at the top of the Ruoka day. The front page keeps `ClipOfferNotice`
   with the new text. Strings: `ai_clip_items` = "Tuo leikepöydältä: %1$d annosta · %2$s kcal",
   a no-kcal variant, and the products variant.
6. **Suggestion rows**: one composable `SuggestionRow(suggestion, onAdd, onEdit)` for both lists:
   name (body large), secondary line "200 g · 130 kcal" via existing formatters (kcal from the
   product's energy and usual amount), the treat sign, and a trailing pencil `IconButton` (48 dp,
   content description "Muokkaa ennen lisäystä"). Rows are 56 dp; the sheet already scrolls.
7. **Edit before adding** reuses `FoodEditor` in a draft mode: the view model builds the item that
   a tap would add (same code path as `addSuggestion`, without writing) and opens the editor with
   `draft = true`; the editor shows Lisää/Peruuta instead of Tallenna/Poista. Lisää runs the normal
   add with the edited amount/kcal/Herkku (Herkku changes the product as everywhere), logs
   `food.add from=… kind=edited`, and shows the "Muuta · Kumoa" line.
8. **Log**: `food.import.offer check read=true|false json=true|false done=true|false` on every
   check, `food.import.offer shown|open|skip count=` as now.

## Risks / Trade-offs

- [The same JSON copied again after import is never offered] → intended; Tuo ruoka-annokset and
  share still work.
- [Done-key list grows] → capped at 50.
- [Eight rows of 56 dp do not all fit above the keyboard] → the sheet scrolls; the order puts the
  likeliest first.
