# Proposal

## Why

The user logs foods three ways: pieces of a stored product ("5 kpl lakritsia"), grams of a product
stored per 100 g, or a kcal total they counted themselves. When they find a stored product's
energy was wrong (a liquorice piece weighs more than they thought), they fix it once and expect
every day they ate it to follow. Today a product energy change reaches only future items; earlier
days keep the wrong kcal, so the history misleads ("Data reads one way only", user 2026-11-16).

## What Changes

- When a product's energy (kcal per piece or per 100 g) changes, from the item editor or from
  Ruokakirjaus, the earlier logged items of that product whose kcal came from the product's
  energy are recalculated (amount × new energy), in the app and in Health Connect.
- Items whose kcal the user gave themselves (a kcal total without an amount, or a kcal that
  differs from amount × old energy, e.g. an AI import's own value) are left as they are.
- Before changing earlier days the app asks once, showing what changes: "Päivitä kaikki",
  "Vain jatkossa" (today's behaviour) or Peruuta. After an update "Päivitetty N kirjausta · Kumoa".
- The product editor shows how many times the product is logged, so its reach is visible.
- Later idea, not now: merging two products ("Lumipallo" and "lumipallo iso") into one.

## Capabilities

### New Capabilities

### Modified Capabilities
- `food-log`: new "Correcting a product's energy"; "Ruokakirjaus in Asetukset" shows the use count.

## Impact

- App screens: the item editor (Ruoka, the add sheet), Ruokakirjaus's product editor, Ruoka day
  totals, Energia and own charts (follow the new kcal). Health Connect: updates own
  `NutritionRecord`s. Not the scale, widgets or build/CI.
- Code: `food/FoodEdits` (the earlier-items plan), `food/HealthConnectFoods` (batch update),
  `ui/FoodViewModel`, `ui/FoodSheet` (editor), `ui/ProductsPage` (product editor, dialog).
