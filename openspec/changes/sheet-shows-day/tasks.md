# Tasks

## 1. Sheet layout

- [ ] 1.1 `ui/FoodSheet.AddFoodContent`: full-height sheet; header "<day> · <total> kcal" + summary line (reuse the Ruoka screen's total/summary helpers); clipboard card under it; the day's items with `FoodScreen.FoodRow` in a scrolling list (tap opens the item editor; "Ei vielä annoksia" when empty; "Ladataan…" / read-failed line while unread)
- [ ] 1.2 Highlight the item added last in this sheet (tonal background) and scroll the list to it
- [ ] 1.3 Add panel docked at the bottom on a raised surface: sheet line, heading "Lisää – usein tähän aikaan", up to eight chips from `Suggestions.atEmpty` in at most three wrapped rows ("+ name kcal"; tap adds; long press and an accessibility action open the new-item editor), field last
- [ ] 1.4 While typing: Tuotteet rows (existing `SuggestionRow` with edit/delete, `EnterRowView`) replace the day list and chips above the field; Enter clears the field and keeps the keyboard up
- [ ] 1.5 Keyboard stays down on open (drop the auto focus); the field gets focus only on tap
- [ ] 1.6 Move "Tuo ruoka-annokset" into the ⋮ menu next to Kopioi tuontiohje
- [ ] 1.7 Strings: "Lisää – usein tähän aikaan", "Ei vielä annoksia" (every locale in use)
- [ ] 1.8a Restructure `FoodSheetFrame`: pinned header, scrolling day list, non-scrolling add panel with imePadding; drop the card-tied focus `LaunchedEffect`
- [ ] 1.8 Smoke tests: the sheet shows the day's items and chips; a chip tap adds and the item appears in the day list; typing shows Tuotteet and hides the chips; Tuo ruoka-annokset reachable from ⋮. Update existing sheet tests for the moved field and keyboard

## 2. Finish

- [ ] 2.1 `./gradlew test lint assembleDebug`
- [ ] 2.2 Update the `openspec/context/` files touched (roadmap entry if any); bump MINOR `appVersion` in `gradle.properties`
