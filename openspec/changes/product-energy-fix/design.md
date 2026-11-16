# Design

## Context

- Items live only in Health Connect as own `NutritionRecord`s; the client id (`food/FoodRecordId`)
  carries position, amount, Herkku and timing, the record carries kcal (stored unrounded,
  `Energy.kcalOf`; rounded only for display). A rewrite with the same client id upserts in place
  (`FoodRecords.replace`).
- `HealthConnectFoods.own(now)` reads the own items as far as the read permissions reach.
- Products live in `products.json` (`ProductStore`); an item belongs to a product by its name key.
- Energy is edited in the item editor (`FoodEdits.editStart`/`edit`, which always passes the
  energy field, so "kcal given" is true on every save) and in Ruokakirjaus (`ui/ProductsPage`, kpl/g
  chips, empty field = no energy). Rename and Herkku already rewrite all items of a product; "Kcal
  filled in later" fills items without kcal (`FoodDay.fill`).

## Decisions

1. **Correction = same unit, different kcal** (exact Double compare of the product's energy before
   and after the save). Unit change, cleared energy, amount/Herkku/name-only saves: no
   recalculation, no dialog.
2. **"From the product's energy" is derived, not stored:** same product key, amount in the energy's
   unit, `|kcal − amount × old| < 0.5`. New kcal = amount × new, unrounded. Rejected: a stored
   flag (record id change, migration, old items would have none).
3. **One pure plan:** `FoodEdits.recalc(key, old, new, items, exceptUuid)` → changed items, kept
   count M, day count D, old/new sums (rounded after summing). Items without kcal are not in it
   (the existing fill handles them).
4. **Fresh data:** before building the plan, read `own()` under `foodWrites`; if the read fails,
   save as "Vain jatkossa" and show "Aiempia kirjauksia ei voitu lukea". Use count reads the
   loaded items (nothing shown when unknown).
5. **Dialog before writing**, from both editors including the new-item editor's Lisää (the dialog
   shows before the editor closes; Peruuta returns to it). Choice only gates the recalculation;
   product save, rename, Herkku and fills happen as today.
6. **Writing:** chunks of 500 records under `foodWrites` in `TerveysApp.appScope`; on a failure
   the bar reads "Päivitettiin X/N kirjausta · Yritä uudelleen"; retry rebuilds the plan from fresh
   items with the kept old energy (already updated items no longer match and are skipped).
7. **Kumoa:** re-reads under `foodWrites`; restores the product's old energy if it still holds the
   new one, the edited item as before the save, and only recalculated items whose kcal is still
   amount × new. Available while the bar shows (sheet line in the sheet and Lisää annos dialog,
   `AppSnackbar.showTimed` on Ruoka and Ruokakirjaus).
8. **Log:** `food.product.energy choice=all|future|cancel items= kept= days=`,
   `food.product.energy ok done= of= ms=` / `failed done= of= cause=` / `undo items=` (counts only).
9. **Plurals** via `plurals` resources for every count text.

## Risks / Trade-offs

- A hand-typed kcal that equals amount × old energy within 0,5 kcal is treated as derived;
  harmless (it equalled the product's value).
- History beyond the read permission's reach is not recalculated; Ohje says the correction reaches
  the days the app can read.
