# food-log Specification

## Purpose
TBD - created by archiving change food-log. Update Purpose after archive.

## Requirements

### Requirement: Ruoka day screen
The app SHALL have a Ruoka screen for one day, opening on today, with arrows to earlier days (none
after today). It SHALL list the day's items in eating order, each with its name, amount and kcal,
and, for an item logged on the day itself, its logging time in small muted text. On top it SHALL
show the day's total kcal with the treats' share apart, and the number of items without kcal
("1 760 kcal · herkkuja 230 · 1 ilman kcal"). A day with nothing logged SHALL show only a short
line on how to add, without any warning, count or judgement. The screen SHALL show no budget,
target, remaining calories, streak or meal names.

#### Scenario: A day in order
- **GIVEN** today the user logged Rahka 200 g at 7.10, Pullo 1 kpl at 10.05 and Pizza (no kcal) at 12.30
- **WHEN** the Ruoka screen opens
- **THEN** the three items are listed in that order with their times, and the top reads the total
  of Rahka and Pullo and "1 ilman kcal"

#### Scenario: Missed day
- **WHEN** the user moves to a day with nothing logged
- **THEN** only the how-to-add line is shown

### Requirement: Adding an item
"Lisää annos" (on the front page and on the Ruoka screen) SHALL open an add field with the keyboard
up, adding to the day shown (today from the front page). With the field empty it SHALL show up to
eight suggestion buttons: the products most often logged within two hours of the current clock
time in the last 30 days, then the most often logged products. While typing it SHALL show up to
five matching products, own products before base products. Tapping a suggestion SHALL add that
product at once at the end of the day with its usual amount and show a line "<name> <amount> ·
<kcal> · Muuta · Kumoa"; Muuta SHALL open the item's editor, Kumoa SHALL take it back. Pressing
enter SHALL add exactly the typed text: a bare number as an item "Pikalisäys" with that many kcal,
any other text as an item of that name (its product's kcal when the name is a known product,
otherwise without kcal). The field SHALL stay open for the next item.

#### Scenario: Usual breakfast in one tap
- **GIVEN** on most mornings around 7 the user logged Rahka 200 g
- **WHEN** the user taps Lisää annos at 7.20
- **THEN** Rahka is among the first suggestion buttons, and one tap logs Rahka 200 g with its kcal

#### Scenario: Only a number
- **WHEN** the user types 350 and presses enter
- **THEN** an item "Pikalisäys" with 350 kcal is added

#### Scenario: A whole day as one number
- **GIVEN** the user estimated yesterday at 2 200 kcal with an AI chat
- **WHEN** they move to yesterday on Ruoka, tap Lisää annos, type 2200 and press enter
- **THEN** yesterday has an item "Pikalisäys" 2 200 kcal next to any other items, counted in
  Energia like any item

#### Scenario: Only a name
- **WHEN** the user types "työpaikan pizza", which is no product yet, and presses enter
- **THEN** an item "työpaikan pizza" without kcal is added and counted as "1 ilman kcal"

#### Scenario: Forgiving search
- **WHEN** the user types "jaulihak"
- **THEN** Jauhelihakastike is suggested (letters ä/a and ö/o equal, parts of compound words and
  one wrong letter from four typed letters match)

### Requirement: Products
Every distinct name the user logs SHALL become one product (names compared ignoring case and extra
spaces), with: the name, a category Ruoka or Herkku (Ruoka by default), and, once known, its energy
as kcal per piece (amount in kpl) or kcal per 100 g (amount in grams as eaten), and the last used
amount. Logging a product SHALL use its last used amount. A whole meal MAY be one product
("Arkiaamu", 350 kcal / kpl).

#### Scenario: Repeat with a count
- **GIVEN** a product Suklaapala 25 kcal / kpl
- **WHEN** the user logs it and changes the amount to 5 in its editor
- **THEN** the item is 5 kpl, 125 kcal, and the next Suklaapala starts at 5 kpl

#### Scenario: Grams as eaten
- **GIVEN** a product Jauhelihakastike 140 kcal / 100 g, last used 250 g
- **WHEN** the user logs it
- **THEN** the item is 250 g, 350 kcal

### Requirement: Kcal filled in later
An item without kcal SHALL be editable to get its kcal (per piece, per 100 g, or a total for the
item). Giving the energy form to an item of a product SHALL save it on the product and fill every
logged item of that product that has no kcal yet; items that already have kcal SHALL NOT change.

#### Scenario: Fill once
- **GIVEN** "työpaikan pizza" was logged without kcal on Monday and Wednesday
- **WHEN** the user gives Wednesday's item 800 kcal / kpl
- **THEN** both items show 800 kcal and the next "työpaikan pizza" is logged with 800 kcal

### Requirement: Base products
The app SHALL ship a set of about 300 generic Finnish foods and drinks (no brands): everyday home
dishes, breads, dairy, fruit and vegetables, treats, drinks and raw ingredients, with kcal per
100 g or per piece and a typical portion, taken from open sources (Open Food Facts, ODbL) and
generic estimates, so the user does not start from nothing. They SHALL be
found by typing like own products, after them; logging one SHALL make it an own product with its
values, which the user can then change. The sources and licences SHALL be named in Ohje.

#### Scenario: First use
- **GIVEN** no own products yet
- **WHEN** the user types "kaurapuu"
- **THEN** Kaurapuuro is suggested with its kcal per 100 g, and one tap logs a typical portion

### Requirement: Editing, order and delete
Tapping a logged item SHALL open its editor with clear labelled fields: amount (with its unit),
kcal, Herkku, and Poista. Herkku belongs to the product: switching it SHALL change the product
and all its logged items, also earlier ones, and the totals and the chart follow. Delete SHALL happen at once with a Kumoa line. The Ruoka
screen SHALL offer "Järjestä", where items are moved by dragging; the order SHALL be saved when
leaving it. Earlier days SHALL be added to and edited the same way as today.

#### Scenario: Delete with undo
- **WHEN** the user deletes Pullo and taps Kumoa
- **THEN** Pullo is back in its place with the same amount and kcal

#### Scenario: Fix the order
- **GIVEN** the user logged the evening snack in advance in the morning
- **WHEN** they open Järjestä and drag it last
- **THEN** it stays last, and its logging time is kept

#### Scenario: Catch up yesterday
- **WHEN** the user moves to yesterday and adds Lasagne
- **THEN** it is the last item of yesterday, counted in yesterday's total, with no time shown

### Requirement: Treats apart
A product in category Herkku SHALL carry a small herkku sign on its items, and the day total, the
chart and the statistics SHALL show the treats' share apart from base food, so the user sees
whether the excess comes from ordinary meals or from treats. Herkku SHALL NOT be red or styled as
a warning. There SHALL be no other categories.

#### Scenario: Day with a treat
- **GIVEN** today 1 530 kcal of Ruoka items and Suklaapala 230 kcal (Herkku)
- **THEN** the day reads 1 760 kcal · herkkuja 230, and its bar has a 230 kcal treat part

### Requirement: Food in Health Connect
Every logged item SHALL be written to Health Connect as a nutrition record with its name, its
energy when known, and an unknown meal type; its order, amount, product and treat category SHALL
be kept with the record, so the log can be rebuilt from Health Connect alone. An item logged on its
own day SHALL carry the logging time; an item added to an earlier day SHALL carry that day without
a shown time. The app SHALL show only the nutrition records it wrote. The nutrition permissions
SHALL be asked the first time the food log is used, never from a weigh-in; without them the food
log SHALL show the reason and a button to grant them, and the rest of the app SHALL work as before.

#### Scenario: Written
- **WHEN** the user logs Rahka 200 g (130 kcal) at 7.10
- **THEN** Health Connect holds a nutrition record "Rahka", 130 kcal, at 7.10

#### Scenario: Other apps' food
- **GIVEN** Samsung Health holds a nutrition record the user logged there
- **THEN** neither the Ruoka screen nor Energia shows it

#### Scenario: Permission denied
- **GIVEN** the user denied the nutrition permissions
- **WHEN** they tap Lisää annos
- **THEN** the app explains why the permission is needed and offers to grant it; weighing still works

### Requirement: Food only inside the app
Food and kcal SHALL be shown only inside the app: no widget, tile, shortcut or notification SHALL
show what was eaten or how many kcal.

#### Scenario: Nothing outside
- **WHEN** the user logs food
- **THEN** no notification appears and no widget changes

### Requirement: Ruokakirjaus in Asetukset
Asetukset's Ruokakirjaus sub-screen SHALL list the own products with their category and energy,
and SHALL let the user change the name, energy, usual amount and category, and remove a product
(from suggestions; logged items keep their values). Its row in Asetukset SHALL read the count of
own products.

#### Scenario: See what usual dishes contain
- **WHEN** the user opens Ruokakirjaus
- **THEN** Jauhelihakastike shows 140 kcal / 100 g, usually 250 g

### Requirement: Food from an AI chat
The app SHALL take a day's food as JSON text in two ways: "Liitä tekoälyltä" in the Lisää annos
sheet reads the clipboard, and sharing text from another app (such as an AI chat) to the app opens
the same preview. The JSON SHALL be found also inside a code block or surrounded by other text.
Text with no usable JSON SHALL show a short reason and add nothing.

#### Scenario: Shared from the chat
- **GIVEN** the AI chat's answer holds a code block with items
- **WHEN** the user shares the answer to the app
- **THEN** the app opens the preview of those items

#### Scenario: Pasted
- **WHEN** the user copies the answer and taps Liitä tekoälyltä
- **THEN** the same preview opens

#### Scenario: No JSON
- **GIVEN** the text holds only chat text
- **THEN** a short reason is shown and nothing is added

### Requirement: Clipboard offer
When the Ruoka screen or the Lisää annos sheet opens and the clipboard holds text copied after the
last offer, the app SHALL read it once, and when it holds usable food JSON SHALL show a line
"Leikepöydällä ruoka-arvio: <N> annosta · Näytä · Ohita". Näytä SHALL open the preview; Ohita SHALL
hide the line. The same clipboard content SHALL be offered only once, whatever the answer, and
clipboard text without food JSON SHALL show nothing. The app SHALL NOT read the clipboard anywhere
else or in the background.

#### Scenario: Copied in the chat
- **GIVEN** the user copied the chat's answer with food JSON
- **WHEN** they open the Ruoka screen
- **THEN** the line offers the 6 portions, and Näytä opens the preview

#### Scenario: Offered once
- **GIVEN** the user tapped Ohita on the offer
- **WHEN** they open the Lisää annos sheet without copying anything new
- **THEN** no offer is shown

#### Scenario: Other text
- **GIVEN** the clipboard holds a web address
- **WHEN** the Ruoka screen opens
- **THEN** no offer is shown

### Requirement: AI food JSON shape
The JSON SHALL be an object with an `items` list (what was eaten) and/or a `products` list
(products to keep), or a bare list of items. An item SHALL
have `name` and MAY have `amount` (> 0), `unit` (`g` or `kpl`, default `g` with an amount), `kcal`
(the item's total, >= 0) and `herkku` (true/false). A product SHALL have `name` and `kcal` and MAY
have `per` (`100g`, `kpl` or `annos`; `annos` is kept as a piece), `amount`/`unit` (its usual
amount) and `herkku`. Unknown keys (also a date) SHALL be ignored; an item or product without a name or with
unusable numbers SHALL be skipped and counted in the preview. "Kopioi ohje tekoälylle" in the
Lisää annos sheet SHALL copy the Finnish instruction for the AI, which states this shape, the
purpose of every field and an example. The instruction SHALL ask a product's energy for the dish
alone, without side dishes, so the product can be logged later with other sides.

#### Scenario: Items and products
- **GIVEN** the text holds
  `{"items":[{"name":"Kaurapuuro","amount":300,"unit":"g","kcal":210}],"products":[{"name":"Lohikeitto","kcal":380,"per":"annos"}]}`
- **THEN** the preview shows Kaurapuuro 300 g 210 kcal and the product Lohikeitto 380 kcal / kpl

#### Scenario: Item without kcal
- **WHEN** an item `{"name":"Kahvi"}` is added
- **THEN** Kahvi is logged without kcal and counted as "1 ilman kcal"

### Requirement: Preview and fine-tuning
Before anything is saved, the preview SHALL show every item with its amount and kcal, the sum, the
products to keep, the number of skipped entries, and the day the items go to: today by default;
tapping the day SHALL open a calendar to pick another day (not after today). The user SHALL be able to edit each item (name, amount, kcal,
Herkku) and each product (name, energy, Herkku), and remove items or products, before saving.
Peruuta SHALL save nothing.

#### Scenario: Fix one portion
- **GIVEN** the preview shows Pulla 1 kpl 250 kcal
- **WHEN** the user changes it to 2 kpl 500 kcal and taps Lisää
- **THEN** Pulla is logged 2 kpl 500 kcal and the sum read 500 kcal more than before

#### Scenario: Shared late in the evening
- **GIVEN** today already holds Rahka 200 g
- **WHEN** the user shares the day's estimate at 21.30
- **THEN** the items are added after Rahka on today

#### Scenario: Yesterday's estimate
- **WHEN** the user shares yesterday's estimate in the morning, taps the day and picks yesterday
- **THEN** the items are added at the end of yesterday, without a shown time

#### Scenario: Cancel
- **WHEN** the user taps Peruuta in the preview
- **THEN** nothing is logged and no product changes

### Requirement: Saving an AI import
"Lisää" SHALL add the items in their order at the end of the chosen day, as logged items written to
Health Connect like any item, and SHALL show a line "<N> annosta lisätty · Kumoa"; Kumoa SHALL
remove the added items and restore the products as they were. An import SHALL only add: it SHALL
NOT change or delete items already logged, and SHALL NOT touch weights or anything outside the
food log. Each item's name SHALL become or match a product like a typed name; a product without
energy SHALL learn it from an item with both amount and kcal (kcal per 100 g for grams, per piece
for kpl) and take the item's amount as its usual amount. An item SHALL NOT change a known
product's energy or category, and an item of a known Herkku product SHALL be a treat. A product in
the `products` list SHALL be saved as given: created when new, its energy, usual amount and
category replaced when it exists.

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
- **WHEN** an item Rahka 200 g 150 kcal is added
- **THEN** the item reads 150 kcal and the product stays 65 kcal / 100 g

#### Scenario: Product list updates
- **GIVEN** a product Lohikeitto 300 kcal / kpl
- **WHEN** an import with product Lohikeitto 380 kcal per annos is added
- **THEN** Lohikeitto is 380 kcal / kpl from then on; earlier logged items keep their kcal
