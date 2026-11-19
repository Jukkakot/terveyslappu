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
   empty `addedIds` list; every add (tap, Enter, editor Lisää, Lisää kaikki) appends the written
   record ids; Kumoa and deletes remove them. A pure rule `AddSearchExit.target(addedIds, origin)`
   returns `Ruoka(day)` or `Origin`. On exit to Ruoka the ids become `FoodUiState.highlight`
   (a set) read by `FoodScreen`; it is cleared when Ruoka is left or a new search opens. The old
   single-item highlight and `sheetKeyboard`/fold state are removed.

4. **Origin.** `MainViewModel` records which `Screen` opened the search (HOME, HISTORY, FOOD). Exit
   with adds calls the existing `openFoodDay(day)` path (MainActivity), so back from Ruoka follows
   `MainUiState.back()` as when Ruoka is opened from the front page.

5. **Widget/shortcut entry stays `FoodAddActivity`** (fast start, no front page first), now with a
   full-screen opaque theme (no dialog window, `adjustResize`). On exit with adds it starts
   `MainActivity` with an intent extra for "Ruoka on <day>, highlight <ids>" (handled like
   `openFoodDay` + highlight) and finishes; without adds it just finishes. Alternative (open
   `MainActivity` directly from the widget) rejected: slower first frame and the front page would
   flash.

6. **Keyboard.** The field requests focus once access is GRANTED; back with the keyboard up hides
   it (the library's default); scrolling the list hides it. After the editor the keyboard comes
   back only if it was up before (existing `beforeEditor` logic).

## Risks / Trade-offs

- [Material 3 search bar API differs between versions] → check the BOM's material3 version
  first; decision 1's fallback keeps the library field.
- [Highlight ids vs. Health Connect record ids after a re-read] → keep the ids the write returned;
  Ruoka matches on `FoodItem.id`.
- [Widget path: MainActivity may already be running on another screen] → the extra is handled in
  `onNewIntent` too (as `openFoodDay`), any open search or editor in the app closes.

## Migration Plan

UI only; nothing stored changes. Prefs keys for the fold state (if any) are simply no longer read.
