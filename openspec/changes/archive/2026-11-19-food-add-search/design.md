# Design

## Context

See proposal.md (Why). Today `AddFoodSheet` (ui/FoodSheet.kt) is a `ModalBottomSheet` hosted by
`MainScreen` when `FoodUiState.sheetDay != null`, and by `FoodAddActivity` (dialog theme
`Theme.Terveyslappu.WeighIn`, own task, `singleTask`) through `ui/FoodAddDialog`. Both share
`FoodViewModel` and `FoodActions`. The sheet holds the day list, the folding add panel, chips, the
typed list and the message line.

## Goals / Non-Goals

- Goals: one full-screen search used by the app and the widget; addable things only; a clear exit
  to Ruoka after adding.
- Non-goals: the ranking of suggestions, the name matching (`FoodRules`), the editor, the AI
  import plan and the clipboard rules stay as they are.

## Decisions

1. **Material 3 full-screen search bar.** Use Compose Material 3's search bar in its expanded
   full-screen form (`SearchBar`/`ExpandedFullScreenSearchBar` with `SearchBarDefaults.InputField`,
   whichever the BOM's material3 offers) instead of hand-building a screen: it brings the
   standard top bar, focus, back handling and insets. Our content goes in its content slot. If the
   library's expanded bar cannot host the clipboard card or the bar (snackbar) cleanly, fall back
   to a `Scaffold` with `SearchBarDefaults.InputField` in the top bar, still the library's field.
   Alternative (keep `ModalBottomSheet` at full height) rejected: it is what the user found odd.

2. **One search content composable** (`AddFoodSearch`, replacing `AddFoodContent`/`FoodSheetFrame`)
   used by `MainScreen` and `FoodAddDialog`. Rows: one `AddRow` for suggestions and matches (+
   leading icon, name + herkku sign, supporting text from `suggestionDetail`, `RowMenu` ⋮), the
   `EnterRowView` restyled with the same + icon. Chips, `PanelHandle`, `foldDrag`, `DayList`,
   `SheetHeader` and the message line go. The bar is `ui/AppSnackbar.showTimed` (Kumoa 4 s,
   swipe away) in the search's own `SnackbarHost`.

3. **Search session in `FoodViewModel`.** Opening sets `searchDay` (renamed from `sheetDay`) and an
   empty `addedIds` set of the items' own client ids (uuid); every add (tap, Enter, editor Lisää,
   Lisää kaikki) adds its uuid at the moment the add is queued (before the day is read or the
   write ends); a failed write, Kumoa and a delete remove it. A pure rule
   `AddSearchExit.target(addedIds, origin)` returns `Ruoka(day)` or `Origin`. On exit to Ruoka
   the ids become `FoodUiState.highlight` (a set) read by `FoodScreen`; cleared by changing the
   day, leaving Ruoka (front page, Historia incl. Viikko, Asetukset) and a new search; kept while
   an editor or Järjestä is open and over background/return; an edit keeps it, a delete drops it
   (Kumoa restores without it). The old single-item highlight and `sheetKeyboard`/fold state go.
   Session state is in memory only: after process death or a lost activity the next open is new
   and nothing is highlighted. Home/recents only background it; only back arrow/system back run
   the exit rule.
   The empty-field ranking is computed once at open and kept in the session; a removed ruoka is
   filtered out, and the undo of its removal puts it back in its ranked place. The date is
   re-checked on resume and on each add (no ticker); the ranking is not recomputed then.
   Every message is a bar event through `AppSnackbar.showTimed` (action bars: Kumoa length;
   others: the short message length); leaving the search drops the current bar.

4. **Origin.** `MainViewModel` records which `Screen` opened the search (HOME, HISTORY, FOOD). Exit
   with adds calls the existing `openFoodDay(day)` path (MainActivity), so back from Ruoka follows
   `MainUiState.back()`. Caveat: today `MainUiState.openFood` resets `foodOrigin` to HOME when
   already on FOOD, and `back()` forces `historyOnEnergy`; the implementation must not do either
   for this exit: from Ruoka the exit just stays on Ruoka with the origin unchanged, from Historia
   back returns to the tab that was open, from the front page back returns there.

5. **Widget/shortcut entry stays `FoodAddActivity`** (fast start, no front page first), now with a
   full-screen opaque theme (no dialog window, `adjustResize`). On exit with adds it starts
   `MainActivity` with an intent extra for "Ruoka on <day>, highlight <uuids>" (handled like
   `openFoodDay` + highlight) and finishes; without adds it just finishes. The app's Ruoka reads
   the day only after the search's pending writes have finished (the writes run under the shared
   write lock) and applies the highlight, matching by uuid, as the annokset appear. A widget tap
   while a search is open for the same date leaves it; for another date it starts a fresh search. Alternative (open
   `MainActivity` directly from the widget) rejected: slower first frame and the front page would
   flash.

6. **Keyboard.** From every entry (app, widget, shortcut) the field requests focus once access is
   confirmed GRANTED (reverses the old widget behaviour on purpose); back with the keyboard up hides
   it (the library's default); scrolling the list hides it. After the editor the keyboard comes
   back only if it was up before (existing `beforeEditor` logic).

## Risks / Trade-offs

- [Material 3 search bar API differs between versions] → check the BOM's material3 version
  first; decision 1's fallback keeps the library field.
- [Highlight ids vs. Health Connect record ids after a re-read] → the highlight uses the item's
  own client id (uuid), which survives re-reads; Ruoka matches annokset by it, not by ids the
  write returned or `FoodItem.id`.
- [Widget path: MainActivity may already be running on another screen] → the extra is handled in
  `onNewIntent` too (as `openFoodDay`), any open search or editor in the app closes.

## Migration Plan

UI only; nothing stored changes. Prefs keys for the fold state (if any) are simply no longer read.
