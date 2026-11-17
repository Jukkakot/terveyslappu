# Proposal

## Why

A logged annos row is two lines tall (name, then the amount in small text), so a day does not fit
on one screen, and the amount is noise when reading the day (user 2026-11-17: both). The amount
should still be one tap away without opening the editor.

## What Changes

- Every annos row on the Ruoka screen and in the Lisää annos sheet's day list becomes one line:
  name (with the herkku sign), kcal, an ⓘ button and the ⋮ button. The amount line goes.
- ⓘ ("Tiedot: <name>") opens a small info bubble (Material 3 rich tooltip) with the annos's
  amount, kcal, the ruoka's Kalorit, Herkku and the logging time; tapping outside closes it.
- Järjestä rows also become one line (no ⓘ, no ⋮; their move buttons stay).
- The glossary gets ⓘ (Tiedot) next to ⋮ (Toiminnot).

Touches app screens only (Ruoka, Lisää annos sheet and dialog). No Health Connect, data, log or
widget change.

## Capabilities

### New Capabilities

### Modified Capabilities
- `food-log`: "Ruoka day screen" (row content) and "Item row buttons" (the ⓘ button and its bubble).

## Impact

`ui/FoodScreen.kt` (`FoodRow`, its callers on Ruoka and in Järjestä), `ui/FoodSheet.kt` (the
sheet's day list), strings, `openspec/context/product.md` glossary. Library: Material 3's
`TooltipBox` + `RichTooltip` (already in the Compose Material 3 dependency).
