# Proposal: food-log

## Why

Meals vary from day to day and the calories of everyday portions are unknown, so the user cannot
tell whether everyday eating matches the goal (weight going down). A light food log for a learning
period, written to Health Connect like the weight, shows what the usual things contain and how
eating and weight move together (product.md "Food log: why" and "Food log: how it is used").

## What Changes

- **Lisää annos** on the front page (outlined, the weigh-in stays the only filled action) and on
  the Ruoka screen: an add field with suggestion buttons for what is usually eaten at this time of
  day, suggestions while typing, one tap adds with the usual amount and a Muuta · Kumoa line;
  enter adds a bare number as kcal or a name without kcal.
- **Products are learnt**: every name becomes a product with kcal per piece or per 100 g once
  known and its last used amount; kcal can be filled in later and fills the product's other items
  too. About 300 generic base products (dishes, breads, dairy, fruit, treats, drinks and raw
  ingredients) from open sources (Open Food Facts, ODbL, for packaged foods) and generic estimates so nothing starts from zero.
- **Ruoka day screen**: the day's items in eating order with small logging times, total with the
  treats' share and items without kcal, earlier days via arrows (catching up is normal), editor,
  delete with undo, Järjestä to drag the order. No meals, no clock times to enter, no budget,
  no judgement.
- **Herkku** is the only category besides Ruoka: base food and treats in their own colours show
  whether the excess comes from ordinary meals or from treats.
- Every item is a Health Connect nutrition record; food and kcal are never shown outside the app.
- Historia gets a third tab **Energia**: daily bars (base food / Herkku), a 7-day average line, the
  weight line under it on the same time axis, period statistics and a list of logged days.
- **Ruokakirjaus row** in Asetukset (before Ulkoasu); the list itself comes from settings-list.
- The data file (export, import, backup) carries the foods and the products. Ohje gets a food log
  section naming the base products' sources. Version 1.24.
- Not here: steps (own change next) and a combined weight/kcal/steps chart (backlog).

## Capabilities

### New Capabilities
- `food-log`: adding, products and base products, the day screen, editing and order, Herkku,
  Health Connect nutrition, food only inside the app, Ruokakirjaus in Asetukset.

### Modified Capabilities
- `weight-chart`: Historia's tabs gain Energia with its chart, statistics and list.
- `home-screen`: the front page gains "Syöty tänään" and Lisää annos.
- `data-file`: two new data kinds, `foods` and `products`.
- `help-page`: an Ohje section on the food log.

## Impact

- App screens: front page row and button, add sheet, Ruoka day screen and item editor, Historia
  Energia tab, Asetukset Ruokakirjaus row and sub-screen, Ohje text.
- Health Connect: READ_NUTRITION and WRITE_NUTRITION, asked on first food use only.
- App storage: own products (`products.json`); a shipped base-product asset (open sources, generic estimates).
- Data file, backup, import: new parts in CSV and JSON, version stays 2.
- Scale, widget, tile, background weigh-in: untouched. Build/CI: no new dependency.
