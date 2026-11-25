## Why

A new name typed into the add search (Enter or "Lisää ”<text>” uutena") is logged at once without
kcal or amount, and the user then has to find the annos to fill it in. Typing a new name almost
always means the details come next.

## What Changes

- Enter or the Enter row on a name that is not a known ruoka (own, Valmiit ruuat by name or alias)
  no longer adds at once: it opens the new-annos editor (as ⋮ → Muokkaa does) with the name filled,
  with Lisää and Peruuta. Peruuta adds nothing and returns to the search with the text kept.
- The Enter row's line for such a name reads "täytä tiedot" instead of "ilman kcal".
- Known names, bare numbers (Pikalisäys), row taps and the clipboard are unchanged.

## Impact

- `ui/FoodViewModel.addTyped`, `ui/FoodSearch` (Enter row line), strings, help text.
- Spec: food-log "Adding from the search".
