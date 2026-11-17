# Tasks

## 1. Import plan

- [ ] 1.1 Rewrite `AiImport.plan` (`food/AiPaste.kt`) per design 1 (three passes).
- [ ] 1.2 `ProductChange` and `AiImportPlan.changes` per design 1–2 (energy and category).
- [ ] 1.3 Unit tests (made-up values) for every scenario of "Saving an AI import" and "Import shows
      changed products" (incl. last item wins, `products` list wins, unit mismatch logs without
      kcal, base off = no fill); update tests that expected a known product's energy to stay.

## 2. Preview and card

- [ ] 2.1 `FoodViewModel` keeps the preview's plan (design 2); `ui/AiImportPreview` shows the sum
      and the change lines above it from that plan.
- [ ] 2.2 `ClipOffer.changes` from the dry-run plan (also products-only); `ClipOffers.step`
      recomputes from the cached parsed clip; `ui/ClipOfferCard` shows the block after its other
      lines, at most three, then "+<N> muuta muutosta".
- [ ] 2.3 Log `changed=<count>` on `food.import.save ok`.

## 3. Base products

- [ ] 3.1 `FoodPrefs` (design 3) in `TerveysApp`; `food.xml` in both lists of
      `data_extraction_rules.xml` and in `DataTransferRulesTest`.
- [ ] 3.2 `FoodUiState.baseOn`, `FoodViewModel.base`; the sheet in `MainActivity` and
      `FoodAddActivity` reads base from the view model; `base` in `TypedList`'s keys.
- [ ] 3.3 `ui/ProductsPage`: the switch row first, with its line; log `food.base set on=`.
- [ ] 3.4 Ohje: one sentence that Valmiit ruuat can be hidden in Asetukset → Ruokakirjaus.
- [ ] 3.5 `resolveTyped` (design 5) for Enter, the Enter row and editor renames, with unit tests.
- [ ] 3.6 Smoke test: with the switch off, typing suggests no base product.

## 4. Finish

- [ ] 4.1 CLAUDE.md: the `food/` map line (`FoodPrefs`, energy changes, `resolveTyped`) and the log
      list (`changed=`, `food.base set`).
- [ ] 4.2 Bump `appVersion` to 1.56; `./gradlew test lint assembleDebug`.
