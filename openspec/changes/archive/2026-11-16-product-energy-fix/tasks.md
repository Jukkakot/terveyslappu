# Tasks

## 1. Plan

- [x] 1.1 `food/FoodEdits.recalc` (design 1–3) and the "is a correction" check (before/after product energy).
- [x] 1.2 Unit tests: Heavier liquorice, Own values stay (130 follows; 150 and 131 stay), Decimal energy (12,5 × 3), Unit change, Amount only, Herkku only, rename only, Nothing else logged, items without kcal not in the plan, another product untouched, sums rounded after summing.

## 2. Saving, dialog, undo

- [x] 2.1 `ui/FoodViewModel`: fresh read before the plan (design 4), the dialog state for Tallenna and the new-item Lisää (design 5), chunked writes and the X/N failure bar with retry (design 6), Kumoa (design 7), log lines (design 8).
- [x] 2.2 Dialog composable (Material 3 `AlertDialog`), used by the item editor (Ruoka, sheet, Lisää annos dialog) and Ruokakirjaus's product editor; strings with `plurals`.
- [x] 2.3 Bars: the sheet line ("Päivitetty N kirjausta · Kumoa" in place of the edited item) and `AppSnackbar.showTimed` on Ruoka and Ruokakirjaus.
- [x] 2.4 Ruokakirjaus product editor: "Kirjattu N kertaa / 1 kerran" under the energy (nothing when 0 or unknown).
- [x] 2.5 Tests with the fake `foodRecords`: all / future / cancel, undo (incl. an item changed again stays), failure + retry, read failure → future; UI smoke: dialog shows on a correction with other items and not on an amount-only save; plural for 1.

## 3. Finish

- [x] 3.1 `./gradlew test lint assembleDebug`
- [x] 3.2 Ohje's food section: one sentence that correcting a product's energy can update the days the app can read; roadmap backlog line "Merge two products" (later idea); CLAUDE.md log events (`food.product.energy`); bump `appVersion` to the next MINOR. (appVersion was already 1.54 on this base; left at 1.54 as instructed.)

## Implementation notes (choices the spec left open)

- Yritä uudelleen recalculates only kcal (items of the product's old and new key, from the items read now); a rename or Herkku of the same save that failed is not retried.
- Kumoa of a new item's Lisää removes the added item; Kumoa restores the edited item as it was and only the kcal of recalculated items.
- A read failure before the dialog logs `food.product.energy choice=future reason=read`.
- The correction counts only for an own product (one in `products.json`); a base product's first save is not a correction.
