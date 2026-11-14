## ADDED Requirements

### Requirement: Paste from an AI chat
The Lisää annos sheet SHALL offer "Liitä tekoälyltä", which reads the clipboard text and finds one
JSON value in it, also when it is inside a code block or surrounded by other text. The value SHALL
be an object with an `items` list (what was eaten) and/or a `products` list (products to keep), or
a bare list of items. A product SHALL have `name` and `kcal` and MAY have `per` (`100g`, `kpl` or
`annos`; `annos` is kept as a piece), `amount`/`unit` (its usual amount) and `herkku`; it SHALL be
saved as an own product, also when no item uses it. Each item SHALL have `name` (text,
required), and MAY have `amount` (number > 0), `unit` (`g` or `kpl`, default `g` when an amount is
given), `kcal` (the item's total, number >= 0) and `herkku` (true/false). Unknown keys SHALL be
ignored. An item without a name SHALL be skipped and counted in the preview.

Before anything is saved, the sheet SHALL show a preview: every item with its amount and kcal, the
sum, and the number of skipped items. "Lisää" SHALL add the items in their order at the end of the
day shown, as logged items written to Health Connect like any item, and SHALL show a line
"<N> annosta lisätty · Kumoa"; Kumoa SHALL remove all of them. Each item's name SHALL become or
match a product like a typed name; a product without energy SHALL learn it from an item that has
both an amount and kcal (kcal per 100 g for grams, per piece for kpl), and its last used amount
SHALL become the item's amount. A product's existing energy and category SHALL NOT change; an item
of a known Herkku product SHALL be a treat whatever `herkku` says. Text with no usable JSON SHALL
show a short reason and add nothing.

The sheet SHALL also offer "Kopioi ohje tekoälylle", which copies a short Finnish instruction that
asks for exactly this JSON shape, with an example.

#### Scenario: A day from the chat
- **GIVEN** the clipboard holds the chat's answer with a code block
  `{"items":[{"name":"Kaurapuuro","amount":300,"unit":"g","kcal":210},{"name":"Pulla","amount":1,"unit":"kpl","kcal":250,"herkku":true}]}`
- **WHEN** the user taps Liitä tekoälyltä and then Lisää
- **THEN** Kaurapuuro 300 g 210 kcal and Pulla 1 kpl 250 kcal (herkku) are the last items of the
  day shown, and the preview had read 460 kcal

#### Scenario: Undo the paste
- **WHEN** the user taps Kumoa on "2 annosta lisätty"
- **THEN** both items are gone and the day is as before

#### Scenario: New product learns its energy
- **GIVEN** no product Kaurapuuro
- **WHEN** Kaurapuuro 300 g 210 kcal is pasted
- **THEN** a product Kaurapuuro 70 kcal / 100 g, usually 300 g, is suggested next time

#### Scenario: Known product keeps its values
- **GIVEN** a product Rahka 65 kcal / 100 g
- **WHEN** Rahka 200 g 150 kcal is pasted
- **THEN** the item reads 150 kcal and the product stays 65 kcal / 100 g

#### Scenario: Products to keep
- **GIVEN** the pasted JSON has `"products":[{"name":"Lohikeitto","kcal":380,"per":"annos"}]`
- **WHEN** the user adds the paste
- **THEN** Lohikeitto 380 kcal / kpl is an own product and one tap logs it next time

#### Scenario: Item without kcal
- **WHEN** an item `{"name":"Kahvi"}` is pasted
- **THEN** Kahvi is added without kcal and counted as "1 ilman kcal"

#### Scenario: No JSON
- **GIVEN** the clipboard holds only chat text
- **WHEN** the user taps Liitä tekoälyltä
- **THEN** a short reason is shown and nothing is added
