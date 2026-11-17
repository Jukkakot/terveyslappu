# Design

## Context

`AiImport.plan` (`food/AiPaste.kt`) resolves each item's name with `resolveName` (own exact key,
then base name or alias), takes the resolved product's name and category, and lets an item teach
energy only to a product without one. A base alias match therefore renamed an item, made it a
treat and left a wrong energy on the product. Products are still matched by name (the JSON writer
knows no ids); what changes is which side wins.

## Decisions

1. **Plan** (`AiImport.plan`), in three passes:
   - The `products` list first, as today.
   - Energy pass over the items: product = `working[FoodNames.key(ai.name)]` (own or made earlier
     in this import); none → new `Product(FoodNames.clean(ai.name), treat = ai.treat)`, or, when
     the item has no kcal, base is on and a base product matches by name or alias, a new own
     product under the JSON name with the base energy and usual amount (`treat = ai.treat`). An
     item with amount and kcal sets energy (existing formula) and `lastAmount = ai.amount`, unless
     its key is in the `products` list. Last item wins.
   - Item pass: `name` = the product's stored name (case variants keep one spelling; the base name
     is never used), `treat = ai.treat` only; missing kcal filled from the product's final energy
     only when the item's unit (or, without amount, the usual amount's unit) matches the energy's
     unit; the item keeps its own amount.
   - Category: the energy pass also sets `product.treat = ai.treat` (same last-wins and
     `products`-list rules); the item's treat = the product's final treat. Earlier items are not
     rewritten (append-only). `EnergyChange` becomes `ProductChange(name, oldEnergy, newEnergy,
     oldTreat, newTreat)`; a line when either differs.
   The `before`/`changed` bookkeeping stays, so Kumoa restores updated products for free.
2. **Changed-product lines**: `AiImportPlan.energyChanges: List<EnergyChange(name, old, new)>` for
   keys whose `before` product had energy and whose final energy differs (unit, or `round(kcal)`).
   The preview does not run the plan today (it shows raw `AiItem`s): `FoodViewModel` runs
   `AiImport.plan` (own, base when on, chosen day) when the preview opens and on every `setAi` or
   day change and keeps it in the preview state; the sum and the lines come from it.
   `ClipOffers.offer` runs the plan also for products-only JSON; the card carries the lines in
   `ClipOffer.changes`. `ClipOffers.step` recomputes the offer from the cached parsed clip on every
   check (not `cached.offer`), so lines follow the switch and product edits.
3. **Base products switch**: `FoodPrefs` = prefs file `food`, key `baseProducts` (default true),
   StateFlow, singleton in `TerveysApp`; in cloud backup and device transfer
   (`data_extraction_rules.xml`, `DataTransferRulesTest`). `FoodUiState.baseOn` carries it;
   `FoodViewModel.base` is empty when off; the sheet takes base from the view model (not
   `TerveysApp.baseProducts` in `MainActivity`/`FoodAddActivity`), and `base` is in the `remember`
   keys of `TypedList`. With an empty base list `Suggestions.typing`, `EnterRow.of`, `resolveName`,
   `FoodEdits` and the import already behave as "no base products".
4. **Herkku in JSON** stays boolean (absent = not a treat), as the import instruction says. No
   prompt change, `ClipOffers.key` unchanged.
5. **Typed alias** (`food/FoodRules.kt`): `resolveTyped(name, own, base)`: own by key; base by exact
   name → `Suggestion.Base` as today; base by alias only → `Suggestion.Own(Product(clean(name),
   treat = false, energy = base energy, lastAmount = base usual))`. Used by Enter, `EnterRow.of`
   and the renames in `FoodEdits.edit`/`addEdited`.
6. **Log**: `changed=<count>` on `food.import.save ok` (every save path writes it) and
   `food.base set on=true|false`.
7. **Old data**: no migration.

## Implementation notes

- The switch is a plain row with a Switch on the Ruokakirjaus page (no change to Asetukset's list row).
- Preview rows still show the raw JSON values; only the sum and the change lines come from the plan.
- Change lines are built by `ProductChange.text()` (Finnish literals, like other pure text builders).
- A category-only change on a product without energy also gets a line ("<name>: herkku").

## Risks

- A sloppy JSON value now overwrites a good product energy. Mitigated by the visible change line
  and Kumoa; the user's stated preference is to trust the JSON.
