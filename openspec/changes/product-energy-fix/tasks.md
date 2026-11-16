# Tasks

## 1. Plan

- [ ] 1.1 `food/FoodEdits.recalc` (design 1, 2): changed items with new kcal, kept-own count, day count, old/new totals; the edited item excluded.
- [ ] 1.2 Unit tests: the spec scenarios (Heavier liquorice, Own values stay, Nothing earlier), grams and pieces, another unit stays, no amount stays, ±1 kcal tolerance, different product with the same unit untouched.

## 2. Saving, dialog, undo

- [ ] 2.1 `ui/FoodViewModel`: save flow with the dialog choice (Päivitä kaikki / Vain jatkossa / Peruuta), batch write under `foodWrites` in `TerveysApp.appScope`, Kumoa (design 4), failure line with Yritä uudelleen (design 5), log lines (design 7).
- [ ] 2.2 Dialog composable (Material 3 `AlertDialog`) used by the item editor and the Ruokakirjaus product editor; strings.
- [ ] 2.3 Ruokakirjaus product editor: "Kirjattu N kertaa" under the energy (none when 0).
- [ ] 2.4 Tests: view model with the fake `foodRecords` (all / future / cancel, undo, failure + retry); one UI smoke test that the dialog shows on an energy change with earlier items and not without.

## 3. Finish

- [ ] 3.1 `./gradlew test lint assembleDebug`
- [ ] 3.2 Ohje's food section: one sentence that correcting a product's energy can update earlier days; roadmap backlog line "Merge two products" (later idea); CLAUDE.md log events (`food.product.energy`); bump `appVersion` to the next MINOR.
