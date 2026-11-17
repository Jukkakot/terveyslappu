# Tasks

## 1. Import plan

- [ ] 1.1 Rewrite the item loop of `AiImport.plan` (`food/AiPaste.kt`) per design 1: own product by
      key, base only for a missing kcal (as a new own product under the JSON name), item amount +
      kcal always sets energy and usual amount, item treat = JSON herkku only.
- [ ] 1.2 Add `EnergyChange` and `AiImportPlan.energyChanges` per design 2 (unit or rounded kcal
      differs; a product without energy before is not a change).
- [ ] 1.3 Unit tests (made-up values) for every scenario of "Saving an AI import" and "Import shows
      changed products" (incl. last item wins, `products` list then item, base off = no fill),
      and update the existing tests that expected a known product's energy to stay.

## 2. Preview and card

- [ ] 2.1 `ui/AiImportPreview`: the change lines above the sum, from the plan of the current
      preview (follow edits and removals).
- [ ] 2.2 `ClipOffer.changes` from the dry-run plan in `ClipOffers.offer`; `ui/FoodClipboard`
      (`ClipOfferCard`) shows at most three lines, then "+<N> muuta tuotetta".
- [ ] 2.3 Log `changed=<count>` on `food.import ok`.

## 3. Valmiit tuotteet switch

- [ ] 3.1 `FoodPrefs` (prefs file `food`, key `baseProducts`, default true), singleton in
      `TerveysApp`; add `food.xml` to both lists in `data_extraction_rules.xml` and to
      `DataTransferRulesTest`.
- [ ] 3.2 `FoodViewModel.base` = empty when off; the sheet (`MainActivity`, `FoodAddActivity`)
      reads base from the view model so the switch applies at once.
- [ ] 3.3 `ui/ProductsPage`: the switch row first, with its line; log `food.base set on=`.
- [ ] 3.4 Smoke test: with the switch off, typing suggests no base product.

## 4. Finish

- [ ] 4.1 Update the CLAUDE.md map line for `food/` (`FoodPrefs`, the plan's energy changes).
- [ ] 4.2 Bump `appVersion` to 1.56; `./gradlew test lint assembleDebug`.
