# Design

## Context

`AiImport.plan` (`food/AiPaste.kt`) resolves each item's name with `resolveName` (own exact key,
then base name or alias), takes the resolved product's name and category, and lets an item teach
energy only to a product without one. A base alias match therefore renamed an item, made it a
treat and left a wrong energy on the product. Products are still matched by name (the JSON writer
knows no ids); what changes is which side wins.

## Decisions

1. **Plan per item** (in `AiImport.plan`):
   - `key = FoodNames.key(ai.name)`; product = `working[key]` (own or earlier in this import).
   - None and `ai.kcal == null` and base enabled: look up the base product by name/alias
     (`resolveName` with an empty own list) and copy its energy and usual amount into a new
     `Product(FoodNames.clean(ai.name), treat = ai.treat, ...)`.
   - None otherwise: `Product(FoodNames.clean(ai.name), treat = ai.treat)`.
   - `ai.amount != null && ai.kcal != null`: set energy from the item (existing formula) and
     `lastAmount = ai.amount`, whether or not the product had energy.
   - Item: `name` = the own product's stored name (so case variants keep one spelling), which for
     a new product is the cleaned JSON name; the base name is never used. `treat = ai.treat`
     only. An existing own product's `treat` is untouched.
   - Missing kcal fill stays as today (`energy.kcalOf`).
   The `before`/`changed` bookkeeping stays, so Kumoa restores updated products for free.
2. **Changed-product lines** come from the plan: add `energyChanges: List<EnergyChange(name, old:
   Energy, new: Energy)>` to `AiImportPlan`, for keys whose `before` product existed with energy
   and whose final energy differs (unit differs or `round(kcal)` differs). An own product without
   energy that learns one is not a change (it is new knowledge, nothing is overwritten). Preview
   rebuilds the plan on every edit already (dry-run for the card in `ClipOffers.offer`), so the
   lines follow edits. The card carries them in `ClipOffer.changes`.
3. **Base products switch**: `FoodPrefs` = prefs file `food`, key `baseProducts` (default true),
   singleton in `TerveysApp`; included in cloud backup and device transfer (update
   `data_extraction_rules.xml` and `DataTransferRulesTest`). `FoodViewModel.base` returns
   `emptyList()` when off, and the sheet takes `base` from the view model's state so a switch takes
   effect at once (not `TerveysApp.baseProducts` directly in `MainActivity`/`FoodAddActivity`).
   With an empty base list the existing `Suggestions.typing`, `EnterRow.of`, `resolveName`,
   `FoodEdits` and the import already behave as "no base products".
4. **Herkku**: the reader's `flag` stays boolean (absent = false); the import instruction already
   says "Jätä pois tavalliselta ruualta", so absent means "not a treat". No prompt change.
5. **Log**: `food.import ok ... changed=<count>` (counts only) and `food.base set on=true|false`.

## Risks

- A sloppy JSON value now overwrites a good product energy. Mitigated by the visible change line
  and Kumoa; the user's stated preference is to trust the JSON.
