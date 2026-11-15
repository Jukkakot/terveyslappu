# Tasks

## 1. Sheet layout

- [x] 1.1 `ui/FoodSheet.AddFoodContent`: full-height sheet; header "<day> · <total> kcal" + summary line (reuse the Ruoka screen's total/summary helpers); clipboard card under it; the day's items with `FoodScreen.FoodRow` in a scrolling list (tap opens the item editor; "Ei vielä annoksia" when empty; "Ladataan…" / read-failed line while unread)
- [x] 1.2 Highlight the item added last in this sheet (tonal background) and scroll the list to it
- [x] 1.3 Add panel docked at the bottom on a raised surface: sheet line, heading "Lisää – usein tähän aikaan", up to eight chips from `Suggestions.atEmpty` in at most three wrapped rows ("+ name kcal"; tap adds; long press and an accessibility action open the new-item editor), field last
- [x] 1.4 While typing: Tuotteet rows (existing `SuggestionRow` with edit/delete, `EnterRowView`) replace the day list and chips above the field; Enter clears the field and keeps the keyboard up
- [x] 1.5 Keyboard stays down on open (drop the auto focus); the field gets focus only on tap
- [x] 1.6 Move "Tuo ruoka-annokset" into the ⋮ menu next to Kopioi tuontiohje
- [x] 1.7 Strings: "Lisää – usein tähän aikaan", "Ei vielä annoksia" (every locale in use)
- [x] 1.8a Restructure `FoodSheetFrame`: pinned header, scrolling day list, non-scrolling add panel with imePadding; drop the card-tied focus `LaunchedEffect`
- [x] 1.8 Smoke tests: the sheet shows the day's items and chips; a chip tap adds and the item appears in the day list; typing shows Tuotteet and hides the chips; Tuo ruoka-annokset reachable from ⋮. Update existing sheet tests for the moved field and keyboard

## 2. Finish

- [x] 2.1 `./gradlew test lint assembleDebug`
- [x] 2.2 Update the `openspec/context/` files touched (roadmap entry if any); bump MINOR `appVersion` in `gradle.properties`

## Notes (implementer decisions the spec did not settle)

- Day-list rows have no trailing edit/delete buttons (tap opens the editor; Poista is in the editor), matching the spec's row description.
- The sheet's ModalBottomSheet is still draggable as a whole (M3 offers no per-region drag); the panel is not specially excluded.
- Chip rows drop to fewer rows by a dp estimate of the header, two list rows and the panel (`AddFoodContent`), not by measuring.
- "Keyboard back after an editor" is `FoodUiState.sheetKeyboard`, set from the IME inset just before an editor opens and consumed on return.
- `saveEdit` leaves the line as it was unless the edited item is the line's own item.
- The old "Usein tähän aikaan" heading string and the empty-field hint (`food_add_empty_hint`) were removed; with no chips nothing is shown.
