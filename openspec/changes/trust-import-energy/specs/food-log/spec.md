## ADDED Requirements

### Requirement: Import shows changed products
When saving an import would change the energy of an existing own product (from an item, see
"Saving an AI import", or from the `products` list), the preview SHALL show, above the sum, one
line per such product: "<name>: <old> → <new>" with each energy in its unit (for example
"Rahka: 65 → 80 kcal / 100 g", or "Pulla: 250 kcal / kpl → 380 kcal / 100 g" when the unit
changes). When the category of an existing own product would change, its line SHALL read
"<name>: herkku" or "<name>: ei herkku" (joined with an energy change into one line, e.g.
"Lakritsi: 350 → 260 kcal / 100 g, ei herkku"). The clipboard card SHALL show the same lines as
a block after all its other lines.
Details:
- Card: at most three change lines, then "+<N> muuta muutosta"; also for products-only JSON.
- Style: bodyMedium in onSurfaceVariant, no icon, same in the sheet, Ruoka and the front page.
- Changed means the unit differs or the kcal rounded to whole numbers differs; smaller drifts and
  the usual amount are saved without a line. New products and products without energy before get
  no line.
- The lines and the preview's sum SHALL come from the same plan the save would run (items without
  kcal filled), and follow the preview's edits, removals and day.
- No dialog or extra confirmation.

#### Scenario: Reused product changes
- **GIVEN** a product Rahka 65 kcal / 100 g
- **WHEN** the clipboard holds an item Rahka 200 g 160 kcal
- **THEN** the card shows "Rahka: 65 → 80 kcal / 100 g" under the items

#### Scenario: Same energy
- **GIVEN** a product Rahka 65 kcal / 100 g
- **WHEN** the preview shows an item Rahka 200 g 130 kcal
- **THEN** no product line is shown

#### Scenario: Line follows an edit
- **GIVEN** the preview shows "Rahka: 65 → 80 kcal / 100 g"
- **WHEN** the user removes the Rahka item
- **THEN** the line is gone

### Requirement: Typed alias of a base product
When a typed name (Enter, the Enter row, or a name changed in the item editor) is not an own
product but matches a base product only by one of its aliases, the item SHALL be logged under the
typed name as a new own product that takes the base product's energy and usual amount but not its
name or category. Tapping a base product's own suggestion row SHALL still log it under its name
and category. While Valmiit tuotteet is off no base product is consulted.

#### Scenario: Alias typed
- **GIVEN** no own product Ranskalaiset, and the base product Ranskalaiset perunat 290 kcal / 100 g,
  Herkku, with the alias Ranskalaiset
- **WHEN** the user types "Ranskalaiset" and presses enter
- **THEN** an item Ranskalaiset with 290 kcal / 100 g at its usual amount is added, not a treat,
  and Ranskalaiset is an own product

## MODIFIED Requirements

### Requirement: Saving an AI import
"Lisää" SHALL add the items in their order at the end of the chosen day, as logged items written to
Health Connect like any item, and SHALL show a line "<N> annosta lisätty · Kumoa"; Kumoa SHALL
remove the added items and restore the products as they were. An import SHALL only add: it SHALL
NOT change or delete items already logged, and SHALL NOT touch weights or anything outside the
food log. The JSON's values SHALL win over the app's:
- Each item SHALL be logged under the own product of its JSON name (compared ignoring case, extra
  spaces and ä/a, ö/o; base names and aliases do not match), created under the JSON name (cleaned like a typed name) when there is none, and SHALL be
  its category follows the JSON (below). A base product SHALL NOT rename it or make it a treat.
- An item with both amount and kcal SHALL set its product's energy from the item (kcal per 100 g
  for grams, per piece for kpl) and its usual amount to the item's amount, also when the product
  already exists. When several items of one product carry both, the last one SHALL win, unless
  the import's `products` list holds that product: then the list's energy wins and the items only
  log their own kcal.
- An item without kcal SHALL take its kcal from its product's final energy in this import (after
  all items and the `products` list); when there is no own product of that name and "Valmiit
  tuotteet" is on, from the base product of that name or alias. The new own product SHALL then take
  that base product's energy and usual amount, and the item's name and category. The item SHALL
  keep its own amount; when its unit differs from the energy's unit it SHALL be logged without
  kcal. An item without amount SHALL use the product's usual amount.
- A product is always a treat or never (Herkku belongs to the product): every item SHALL set its
  product's category to its `herkku`, also for an existing product, under the same last-wins and
  `products`-list rules as the energy; the item is a treat exactly when its product ends up Herkku.
  The import stays append-only: earlier logged items of the product keep their sign, as they keep
  their kcal (switching Herkku by hand still changes them all).
- A product in the `products` list SHALL be saved as given: created when new, its energy, usual
  amount and category replaced when it exists.
Earlier logged items of a changed product SHALL keep their kcal.

#### Scenario: Undo the import
- **WHEN** the user taps Kumoa on "2 annosta lisätty"
- **THEN** both items are gone and the products are as before

#### Scenario: Earlier items untouched
- **GIVEN** today already holds Rahka 200 g
- **WHEN** an import with Kaurapuuro is added to today
- **THEN** Rahka stays as it was and Kaurapuuro is the last item

#### Scenario: New product learns its energy
- **GIVEN** no product Kaurapuuro
- **WHEN** Kaurapuuro 300 g 210 kcal is added
- **THEN** a product Kaurapuuro 70 kcal / 100 g, usually 300 g, is suggested next time

#### Scenario: Item keeps a known product's values
- **GIVEN** a product Rahka 65 kcal / 100 g
- **WHEN** an item Rahka 200 g 130 kcal is added
- **THEN** the item reads 130 kcal and the product stays 65 kcal / 100 g (usually 200 g from
  then on), with no change line

#### Scenario: Filled with the import's energy
- **GIVEN** a product Rahka 65 kcal / 100 g
- **WHEN** an import holds Rahka 100 g without kcal, then Rahka 200 g 160 kcal
- **THEN** the first item reads 80 kcal and the second 160 kcal

#### Scenario: Product list wins
- **WHEN** an import holds the product Lohikeitto 380 kcal per annos and an item Lohikeitto 400 g
  320 kcal
- **THEN** the item reads 320 kcal and Lohikeitto is 380 kcal / kpl

#### Scenario: Item updates a known product
- **GIVEN** a product Rahka 65 kcal / 100 g, usually 250 g
- **WHEN** an item Rahka 200 g 160 kcal is added
- **THEN** the item reads 160 kcal and Rahka is 80 kcal / 100 g, usually 200 g, from then on;
  earlier Rahka items keep their kcal

#### Scenario: Base product does not take over
- **GIVEN** no own product Ranskalaiset, and a base product Ranskalaiset perunat 290 kcal / 100 g,
  Herkku, with the alias Ranskalaiset
- **WHEN** an item Ranskalaiset 200 g 300 kcal without herkku is added
- **THEN** the item is Ranskalaiset 200 g 300 kcal, not a treat, and an own product Ranskalaiset
  150 kcal / 100 g is suggested next time

#### Scenario: Base product fills a missing kcal
- **GIVEN** no own product Uuniranskalaiset, the base product Uuniranskalaiset 130 kcal / 100 g,
  and Valmiit tuotteet on
- **WHEN** an item Uuniranskalaiset 200 g without kcal is added
- **THEN** the item reads 260 kcal and an own product Uuniranskalaiset 130 kcal / 100 g is created

#### Scenario: Category from the JSON
- **GIVEN** an own product Lakritsi in category Herkku
- **WHEN** an item Lakritsi 50 g 180 kcal without herkku is added
- **THEN** the preview shows "Lakritsi: ei herkku" among its lines (with the energy change), and
  after Lisää Lakritsi and its items are not treats

#### Scenario: Product list updates
- **GIVEN** a product Lohikeitto 300 kcal / kpl
- **WHEN** an import with product Lohikeitto 380 kcal per annos is added
- **THEN** Lohikeitto is 380 kcal / kpl from then on; earlier logged items keep their kcal

### Requirement: Base products
The app SHALL ship a set of about 300 generic Finnish foods and drinks (no brands): everyday home
dishes, breads, dairy, fruit and vegetables, treats, drinks and raw ingredients, with kcal per
100 g or per piece and a typical portion, taken from open sources (Open Food Facts, ODbL) and
generic estimates, so the user does not start from nothing. While "Valmiit tuotteet" is on (the
default) they SHALL be found by typing like own products, after them; logging one SHALL make it an
own product with its values, which the user can then change. While it is off, base products SHALL
NOT be suggested, SHALL NOT give the Enter row or a typed name a kcal, and SHALL NOT be used by an
import; own products made from them earlier SHALL stay. The sources and licences SHALL be named
in Ohje.

#### Scenario: First use
- **GIVEN** no own products yet
- **WHEN** the user types "kaurapuu"
- **THEN** Kaurapuuro is suggested with its kcal per 100 g, and one tap logs a typical portion

#### Scenario: Switched off
- **GIVEN** Valmiit tuotteet is off and no own product Kaurapuuro
- **WHEN** the user types "kaurapuu"
- **THEN** no product is suggested and the last row reads "Lisää ”kaurapuu” uutena" without kcal

### Requirement: Ruokakirjaus in Asetukset
Asetukset's Ruokakirjaus sub-screen SHALL start with a switch "Valmiit tuotteet" (on by default)
with the line "Sovelluksen mukana tulevat yleiset ruuat ehdotuksissa", which turns the base
products on and off at once. It SHALL then list the own products with their category and energy,
and SHALL let the user change the name, energy, usual amount and category, and remove a product
(from suggestions; logged items keep their values) with the product editor's red "Poista tuote",
which shows "<name> poistettu · Kumoa"; Kumoa SHALL bring the product back as it was. Its row in Asetukset SHALL read the count of
own products.

#### Scenario: See what usual dishes contain
- **WHEN** the user opens Ruokakirjaus
- **THEN** Jauhelihakastike shows 140 kcal / 100 g, usually 250 g

#### Scenario: Undo a product removal
- **WHEN** the user removes Jauhelihakastike under Ruokakirjaus and taps Kumoa
- **THEN** Jauhelihakastike is listed again with 140 kcal / 100 g, usually 250 g

#### Scenario: Turn base products off
- **WHEN** the user turns Valmiit tuotteet off
- **THEN** typing in the Lisää annos sheet suggests only own products, and the setting is kept
  after restarting the app
