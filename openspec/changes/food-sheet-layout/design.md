# Design

## Context

Mockups: canvas "Lisää annos -luonnokset" (https://claude.ai/artifact/Bnn1QYSZsTWjUKf3UrkWHg),
board A (layout, row icons, bottom Kumoa) with board B's card. The sheet is
`ui/FoodSheet.AddFoodContent`, shared by the in-app sheet and `FoodAddDialog`; the offer state and
done keys are in `FoodViewModel` (`clipOffer`, `food.import.offer`), the import plan in
`AiImport`, snackbars in `MainActivity` (`SnackbarDuration.Long` for actions = 10 s).

## Decisions

1. **Card data**: `ClipOffer` gains the parsed preview lines (name, amount text, kcal, treat) built
   from the same dry-run plan that gives the total, so the card and the preview agree. Lines are
   computed at check time from the cached parse; the clipboard text is read again only when "Lisää
   kaikki" or "Muokkaa" is tapped (as for the old tap), parsed, and its key must equal the offer's
   key, else the card is refreshed and nothing is saved.
2. **Lisää kaikki** = the preview's `addAi` path on a preview built from the parse with the sheet's
   (or Ruoka's) day; marks the key done, clears the offer, same "N annosta lisätty · Kumoa"
   message. In `FoodAddActivity` it runs in the sheet (no hand-over).
3. **Keyboard**: `AddFoodContent` requests focus on open only when `clipOffer == null`; when the
   card goes away (added or ✕) focus is requested then.
4. **Header ⋮**: a `DropdownMenu` with "Kopioi tuontiohje" only; the bottom row keeps "Tuo
   ruoka-annokset" as a `TextButton`.
5. **Enter row**: a row composable in the matches list with a plus icon in primary colour; its
   second line comes from the same rule that Enter uses (`FoodEdits`/`resolveName`), shown only
   when the trimmed query is non-empty.
6. **Delete product**: own product rows (`Suggestion.Own`) get a trailing `IconButton` with the
   trash icon in `colorScheme.error`; `ProductStore.remove` + `restore` for Kumoa via the existing
   sheet line (`SheetLine.Deleted` gains a product variant). Logged as `food.product.delete ok`.
7. **Undo messages**: `SnackbarHost` with a custom `Snackbar` wrapped in `SwipeToDismissBox`;
   action messages shown with `SnackbarDuration.Indefinite` and dismissed by a 4 s timer (Material's
   Short is 4 s but drops to indefinite with accessibility services; a timer keeps it bounded);
   the host is placed above the navigation bar and is not drawn while a modal sheet is open (the
   sheet uses its own line).
8. **Row icons**: pencil and trash 48 dp `IconButton`s, outside the row's clickable, in a fixed
   trailing column: treat sign, pencil, trash.

## Review decisions (2026-11-15)

- **Sheet line keeps today's rule** (no timer; it covers nothing): the 4 s and swipe apply to the
  app's snackbar only.
- **Import message in a sheet**: new `SheetLine.Imported(plan)` "<N> annosta lisätty · Kumoa" (or
  "<N> tuotetta tallennettu · Kumoa") whenever a sheet is open (in the app and from the widget);
  the next add replaces it. The snackbar shows it only on Ruoka.
- **Failed write**: store products and mark the key done only after a successful Health Connect
  insert, for Lisää kaikki and the preview's Lisää alike; on failure show the failed line
  (sheet) or snackbar (Ruoka), keep the offer, products untouched.
- **Not today**: when the target day is not today the card's button reads "Lisää kaikki ·
  <eilen|date>"; the sheet header "Lisää annos · <day>" replaces the "Lisätään päivälle <day>"
  label of "Sheet over midnight" (header shows "eilen" after midnight).
- **Card lines** are persisted with the cached read in `foodclip` prefs (JSON of lines); kcal
  recomputed against current products when shown.
- **Clipboard changed before a tap**: no write; card refreshed (or removed) and line "Leikepöytä
  muuttui".
- **Mixed JSON**: after the item lines one line "+ <N> tuotetta" (counts inside the five-line
  limit, shown last). Products-only lines "Lohikeitto · 380 kcal / kpl".
- **Enter row**: hidden when the cleaned query equals a listed product's name key; otherwise a
  sixth row after the matches; quoted text cleaned (`FoodNames.clean`); second line from
  `resolveName` (base names count as known).
- **Deleting an own product with a base twin**: the base row shows again afterwards; accepted.
  Muuta on a line of a deleted product recreates it; accepted.
- **Asetukset product removal** stays without Kumoa here; named in the consistency backlog item.
- **Snackbar**: dismissed when another message comes or the screen changes (not on outside taps);
  sits above Ruoka's bottom bar; dismissed when a sheet opens, messages during a sheet go to the
  sheet line; non-Kumoa snackbars keep their duration but are swipeable; weights' "Paino poistettu
  · Kumoa" also 4 s; showing a message first dismisses the current one; the 4 s timer is
  injectable for tests.
- **Keyboard** rises when the card goes away (added or ✕). The card shows only with the field
  (after access is granted). Lisää kaikki before the day is read appends once it loads. ✕ has
  content description "Ohita", 48 dp. Long names ellipsize in one line.
- The ⋮ menu has one entry for now; fine (more may join in the consistency pass).

## Risks / Trade-offs

- [Lisää kaikki skips the preview's day choice] → the card adds to the day shown; another day goes
  through Muokkaa.
- [A wrong-JSON surprise import] → Kumoa and the visible item lines before the tap.
