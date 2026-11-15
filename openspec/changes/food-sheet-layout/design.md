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

## Risks / Trade-offs

- [Lisää kaikki skips the preview's day choice] → the card adds to the day shown; another day goes
  through Muokkaa.
- [A wrong-JSON surprise import] → Kumoa and the visible item lines before the tap.
