## ADDED Requirements

### Requirement: Import shows changed products
When saving an import would change the energy of an existing own product (from an item, see
"Saving an AI import", or from the `products` list), the preview SHALL show, above the sum, one
line per such product: "<name>: <old> → <new>" with each energy in its unit (for example
"Rahka: 65 → 80 kcal / 100 g", or "Pulla: 250 kcal / kpl → 380 kcal / 100 g" when the unit
changes). The clipboard card SHALL show the same lines under its items (at most three, then
"+<N> muuta tuotetta"). An energy counts as changed when its unit differs or its kcal rounded to
whole numbers differs. New products and unchanged products SHALL get no line. The lines SHALL
follow the preview's edits and removals. There SHALL be no dialog or extra confirmation.

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

## MODIFIED Requirements

### Requirement: Saving an AI import
"Lisää" SHALL add the items in their order at the end of the chosen day, as logged items written to
Health Connect like any item, and SHALL show a line "<N> annosta lisätty · Kumoa"; Kumoa SHALL
remove the added items and restore the products as they were. An import SHALL only add: it SHALL
NOT change or delete items already logged, and SHALL NOT touch weights or anything outside the
food log. The JSON's values SHALL win over the app's:
- Each item SHALL be logged under the own product of its JSON name (names compared as for typed
  names), created under the JSON name (cleaned like a typed name) when there is none, and SHALL be
  a treat exactly when its `herkku` is true. A base product SHALL NOT rename it or make it a
  treat.
- An item with both amount and kcal SHALL set its product's energy from the item (kcal per 100 g
  for grams, per piece for kpl) and its usual amount to the item's amount, also when the product
  already exists. When several items of one product carry both, the last one SHALL win.
- An item without kcal SHALL take its kcal from its own product's energy as before; when there is
  no own product of that name and "Valmiit tuotteet" is on, from the base product of that name or
  alias. The new own product SHALL then take that base product's energy and usual amount, and the
  item's name and category.
- An existing product's category SHALL NOT change by an item; a new product SHALL take the item's
  category.
- A product in the `products` list SHALL be saved as given: created when new, its energy, usual
  amount and category replaced when it exists. An item of the same name after it SHALL still set
  the energy when it has amount and kcal.
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
- **THEN** the item reads 130 kcal and the product stays 65 kcal / 100 g, with no change line

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

#### Scenario: Treat only from the JSON
- **GIVEN** an own product Lakritsi in category Herkku
- **WHEN** an item Lakritsi 50 g 180 kcal without herkku is added
- **THEN** the item is not a treat and Lakritsi stays in category Herkku

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
