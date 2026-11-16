# Design

## Context

- Items live only in Health Connect as own `NutritionRecord`s; the client record id
  (`food/FoodRecordId`) carries position, amount, Herkku and timing, the record carries kcal.
  `HealthConnectFoods.own(now)` reads all own items; `insert` writes new versions of changed items
  (same uuid, new client id/version), as "Kcal filled in later" already does through
  `FoodEdits.edit` → `FoodPlan` (`changed` map, `FoodDay.fill`).
- Products live in `products.json` (`ProductStore`); an item belongs to a product by its name key.
- Product energy is edited in the item editor (`FoodEdits.edit`, "changes reach the product") and
  in Ruokakirjaus's product editor (`ui/ProductsPage`).

## Decisions

1. **"From the product's energy" is derived, not stored:** an item qualifies when its product key
   matches, it has an amount in the old energy's unit and `|kcal − round(amount × old energy)| ≤ 1`.
   No new field or record id change, so old items work at once. Rejected: a stored "derived" flag
   (record id format change, migration, and old items would have none).
2. **One pure plan:** `FoodEdits.recalc(product, old, new, all, except)` returns the changed items
   (new kcal), the kept-own count M, the day count D and the old/new totals for the dialog. Both
   editors call it before saving; the item being edited is excluded (its own edit rules apply).
3. **Dialog before writing:** only when the plan has ≥ 1 item. Päivitä kaikki → save the product
   and write all changed items in one `insert` batch under `foodWrites` in `TerveysApp.appScope`
   (survives leaving the screen). Vain jatkossa → save the product only. Peruuta → nothing.
4. **Kumoa:** keeps the old product and the old items in memory; Kumoa writes the old product and
   old kcal back the same way (a new version each). The bar uses `AppSnackbar.showTimed`.
5. **Failure:** a failed batch leaves the product at its new energy and shows "Aiempien kirjausten
   päivitys epäonnistui" with Yritä uudelleen (re-runs the same plan on fresh data); logged.
6. **Use count:** from the loaded items (`own` reads them all), counted per product key.
7. **Log:** `food.product.energy ok items= days= kept= ms=` / `failed cause=` / `undo`, and
   `food.product.energy choice=all|future|cancel` (counts only, never names or kcal).

## Risks / Trade-offs

- A hand-typed kcal that happens to equal amount × old energy is treated as derived; harmless
  (it equalled the product's value anyway).
- Rounding: the product energy may have decimals; the ±1 kcal tolerance covers rounding at
  logging time.
