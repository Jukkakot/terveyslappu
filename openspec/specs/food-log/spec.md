# food-log Specification

## Purpose
TBD - created by archiving change food-log. Update Purpose after archive.

## Requirements

### Requirement: Ruoka day screen
The app SHALL have a Ruoka screen for one day, opening on today, with arrows to earlier days (none
after today). It SHALL list the day's items in eating order, each on one line with its name
(shortened with "…" when it does not fit) and kcal, without the amount or a logging time (both are
in the row's ⓘ bubble, see "Item row buttons"). On top it SHALL
show the day's total kcal with the treats' share apart, and the number of items without kcal
("1 760 kcal · herkkuja 230 kcal · 1 ilman kcal"). A day with nothing logged SHALL show only a short
line on how to add, without any warning, count or judgement. The screen SHALL show no budget,
target, remaining calories, streak or meal names. The top bar SHALL have a "Viikko" link that opens
Historia on Energia for 1 vk; back from there SHALL return to the Ruoka screen.

#### Scenario: A day in order
- **GIVEN** today the user logged Rahka 200 g at 7.10, Pullo 1 kpl at 10.05 and Pizza (no kcal) at 12.30
- **WHEN** the Ruoka screen opens
- **THEN** the three items are listed in that order, each on one line without amount or time, and
  the top reads the total of Rahka and Pullo and "1 ilman kcal"

#### Scenario: Missed day
- **WHEN** the user moves to a day with nothing logged
- **THEN** only the how-to-add line is shown

#### Scenario: From the day to the week
- **WHEN** the user taps Viikko on the Ruoka screen
- **THEN** Historia opens on Energia with 1 vk, and tapping a day in the Energia day list opens that
  day on Ruoka

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
generic estimates, so the user does not start from nothing. While "Valmiit ruuat" is on (the
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
- **GIVEN** Valmiit ruuat is off and no own product Kaurapuuro
- **WHEN** the user types "kaurapuu"
- **THEN** no product is suggested and the last row reads "Lisää ”kaurapuu” uutena" without kcal

### Requirement: Editing, order and delete
Tapping a logged item SHALL open its editor with clear labelled fields: amount (with its unit),
kcal, Herkku, and Poista. Herkku belongs to the product: switching it SHALL change the product
and all its logged items, also earlier ones, and the totals and the chart follow. Delete SHALL happen at once with a Kumoa line. The Ruoka
screen SHALL offer "Järjestä", where items are moved by dragging; the order SHALL be saved when
leaving it. Järjestä SHALL be started from a ⋮ button ("Lisää toimintoja") at the end of Ruoka's
top bar, whose menu item reads "Järjestä annokset"; there SHALL be no unlabelled icon for it. The
⋮ SHALL be shown under the rule that showed the sort icon: the nutrition permission granted, the
day loaded and more than one annos on that day. While
Järjestä is on, the top bar SHALL show "Valmis" in place of Viikko and the ⋮ button. Earlier days
SHALL be added to and edited the same way as today.

#### Scenario: Delete with undo
- **WHEN** the user deletes Pullo and taps Kumoa
- **THEN** Pullo is back in its place with the same amount and kcal

#### Scenario: Fix the order
- **GIVEN** the user logged the evening snack in advance in the morning
- **WHEN** they open ⋮ → Järjestä annokset and drag it last
- **THEN** it stays last, and its logging time is kept

#### Scenario: Catch up yesterday
- **WHEN** the user moves to yesterday and adds Lasagne
- **THEN** it is the last item of yesterday, counted in yesterday's total, with no time shown

#### Scenario: Top bar at large text
- **GIVEN** a 360 dp phone at 200 % text size and a day with two or more annokset
- **WHEN** Ruoka is shown
- **THEN** back, the title Ruoka, Viikko and the ⋮ button are all visible and none is cut off

#### Scenario: One annos
- **GIVEN** a day with a single annos
- **WHEN** Ruoka is shown
- **THEN** there is no ⋮ button (nothing to order)

### Requirement: Treats apart
A product in category Herkku SHALL carry a small herkku sign on its items, and the day total, the
chart and the statistics SHALL show the treats' share apart from base food, so the user sees
whether the excess comes from ordinary meals or from treats. Herkku SHALL NOT be red or styled as
a warning. There SHALL be no other categories.

The treat share text ("herkkuja 230 kcal", "herkkuja keskiarvo 230 kcal/pv", the Energia statistics'
Herkut value) SHALL be drawn in the treat tone of the Energia bars wherever it is shown: the Ruoka
summary line, the home food card, the Energia list's day and
group rows and the Energia statistics. Only the treat part takes the tone; the other parts of the
same line keep their present colour, and size and weight stay unchanged. A zero or missing
value ("0 kcal/pv", "–") keeps the normal colour. The chart marker card and legends keep their
treat dot or swatch with normal text.

#### Scenario: Day with a treat
- **GIVEN** today 1 530 kcal of Ruoka items and Suklaapala 230 kcal (Herkku)
- **THEN** the day reads 1 760 kcal · herkkuja 230 kcal, and its bar has a 230 kcal treat part

#### Scenario: Treat text in the treat tone
- **GIVEN** today has a 230 kcal treat share and one item without kcal
- **THEN** the Ruoka summary line shows "herkkuja 230 kcal" in the treat tone of the Energia bars
  and "1 ilman kcal" in the header's present colour

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
Food and kcal SHALL be shown only inside the app, its add search opened from the widget or
shortcut included: no widget face, tile, shortcut label or notification SHALL show what was eaten,
how many items or how many kcal. A widget or shortcut that only opens the add search is allowed.

#### Scenario: Nothing outside
- **WHEN** the user logs food
- **THEN** no notification appears and no widget changes

#### Scenario: Dialog is inside
- **WHEN** the user adds an annos in the search opened from the widget
- **THEN** the search shows its bar, and the widget itself shows nothing about it

### Requirement: Omat ruuat in Asetukset
Asetukset's Omat ruuat sub-screen SHALL start with a switch "Valmiit ruuat" (on by default)
with the line "Sovelluksen mukana tulevat yleiset ruuat ehdotuksissa", which turns the base
products on and off at once. It SHALL then list the own products with their category and energy,
and SHALL let the user change the name, energy, usual amount and category, and remove a product
(from suggestions; logged items keep their values) with the product editor's red "Poista ruoka"
or the row's ⋮ → Poista,
which shows "<name> poistettu · Kumoa"; Kumoa SHALL bring the product back as it was. Its row in Asetukset SHALL read the count of
own products.

#### Scenario: See what usual dishes contain
- **WHEN** the user opens Omat ruuat
- **THEN** Jauhelihakastike shows 140 kcal / 100 g, usually 250 g

#### Scenario: Undo a product removal
- **WHEN** the user removes Jauhelihakastike under Omat ruuat and taps Kumoa
- **THEN** Jauhelihakastike is listed again with 140 kcal / 100 g, usually 250 g

#### Scenario: Turn base products off
- **WHEN** the user turns Valmiit ruuat off
- **THEN** typing in the Lisää annos search suggests only own products, and the setting is kept
  after restarting the app

### Requirement: Food from an AI chat
The app SHALL take a day's food as JSON text, whatever app wrote it, in two ways: "Tuo
ruoka-annokset" in the Lisää annos search reads the clipboard, and sharing text from another app
(such as an AI chat) to the app opens the same preview, titled "Tuo ruoka-annokset". The JSON SHALL
be found also inside a code block or surrounded by other text. Text with no usable JSON SHALL show
a short reason and add nothing. The import's buttons, titles and messages SHALL NOT name an AI as
the source.

#### Scenario: Shared from the chat
- **GIVEN** the AI chat's answer holds a code block with items
- **WHEN** the user shares the answer to the app
- **THEN** the app opens the preview of those items

#### Scenario: Pasted
- **WHEN** the user copies the answer and taps Tuo ruoka-annokset
- **THEN** the same preview opens

#### Scenario: No JSON
- **GIVEN** the text holds only chat text
- **THEN** a short reason is shown and nothing is added

### Requirement: Clipboard offer
When the front page, the Ruoka screen or the add search (in the app or opened from the widget or
shortcut) opens or the app resumes on one of them, and the clipboard holds usable food JSON that
has not been imported or skipped, the app SHALL offer it. In the add search (under the top bar,
above the rows) and on the Ruoka screen (at the top of the day) the offer SHALL be a card: a title
"Leikepöydällä" with the kcal total the preview would show, then each item on its own line with
name, amount, treat sign and kcal (at most five lines, then "+<N> muuta"), then a filled primary
button "Lisää kaikki", an outlined "Muokkaa" and a ✕ button (Ohita). "Lisää kaikki" SHALL save the
content as the preview's Lisää would, to the day the search or Ruoka shows, without opening the
preview, and show "Lisättiin <N> annosta · Kumoa"; in the search those annokset count as added in
this search. "Muokkaa" SHALL open the preview. When the JSON holds only products the card SHALL
list the products with their energy and its button SHALL read "Tallenna ruuat". On the front page
the same card SHALL be shown above the cards, adding to today. The offer SHALL be shown again on
every open and resume while the same content is on the clipboard, until the user saves that
content (Lisää kaikki or the preview's Lisää) or taps Ohita or ✕; after either, that content
SHALL NOT be offered again, on any screen, also when it is copied again; Kumoa of the import SHALL
NOT bring it back. The example JSON that Kopioi tuontiohje puts on the clipboard SHALL NOT be
offered. Content counts as the same when its JSON is the same, whatever text surrounds it. Content
imported by sharing it to the app or by Tuo ruoka-annokset SHALL count as imported too. Peruuta on
the preview SHALL leave the content offered. Clipboard text without food JSON SHALL show nothing.
In the search opened from the widget or shortcut the clipboard SHALL be read when its window gains
focus. The app SHALL read the clipboard's text only when the clipboard has changed since the app
last read it (Android may then show its own "pasted" notice once per new copy) and when the offer
is used, and SHALL NOT read it anywhere else or in the background. The offer SHALL NOT be shown on
Ruoka without the food permission, before the day is read or in Järjestä, nor in the front page's
edit mode.

#### Scenario: Copied in the chat
- **GIVEN** the user copied the chat's answer with food JSON of 3 items
- **WHEN** they open Lisää annos
- **THEN** the card lists the 3 items and the total under the top bar, and one tap on Lisää kaikki
  adds them to today with "Lisättiin 3 annosta · Kumoa"

#### Scenario: Fix before adding
- **WHEN** the user taps Muokkaa on the card
- **THEN** the preview opens with the same items

#### Scenario: Offered on the front page
- **GIVEN** the user copied food JSON with 4 items
- **WHEN** they open the app on the front page
- **THEN** the card with the 4 items is shown above the cards, and Lisää kaikki adds them to today

#### Scenario: Still offered after looking elsewhere
- **GIVEN** the user copied food JSON and the front page already offered it
- **WHEN** they open Lisää annos from the widget
- **THEN** the search offers the same content

#### Scenario: Not offered after import
- **GIVEN** the user added the offered content with Lisää kaikki
- **WHEN** they open Lisää annos without copying anything new
- **THEN** no offer is shown

#### Scenario: Cancelled preview
- **WHEN** the user opens the preview from the offer and taps Peruuta
- **THEN** the offer is shown again on the next open

#### Scenario: Offered once
- **GIVEN** the user tapped ✕ on the card
- **WHEN** they open Lisää annos without copying anything new
- **THEN** no offer is shown

#### Scenario: Import undone
- **GIVEN** the user saved the offered content and tapped Kumoa
- **WHEN** they open the Ruoka screen without copying anything new
- **THEN** no offer is shown

#### Scenario: Import instructions copied
- **WHEN** the user taps Kopioi tuontiohje and opens the Ruoka screen
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
unusable numbers SHALL be skipped and counted in the preview. "Kopioi tuontiohje" in the
Lisää annos search SHALL copy the Finnish instruction for the writer of the JSON (such as an AI
chat), which states this shape, the purpose of every field and an example. The instruction SHALL
ask a product's energy for the dish alone, without side dishes, so the product can be logged later
with other sides.

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
Herkku) and each product (name, energy, Herkku), and remove items or products, before saving. Removing a row SHALL show "<name> poistettu ·
Kumoa" in the preview; Kumoa SHALL put the row back in its place.
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

#### Scenario: Undo a removed row
- **WHEN** the user removes Pulla from the preview and taps Kumoa
- **THEN** Pulla is back in the same place and the sum includes it again

### Requirement: Saving an AI import
"Lisää" SHALL add the items in their order at the end of the chosen day, as logged items written to
Health Connect like any item, and SHALL show a line "Lisättiin <N> annosta · Kumoa"; Kumoa SHALL
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
  ruuat" is on, from the base product of that name or alias. The new own product SHALL then take
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
- **WHEN** the user taps Kumoa on "Lisättiin 2 annosta"
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
  and Valmiit ruuat on
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

### Requirement: Widget and shortcut for adding
The app SHALL offer a 1x1 home-screen widget "Lisää annos" in the same round style and colours as
the Punnitse widget, with a cutlery icon, and a launcher shortcut "Lisää annos" after Punnitse on
the app icon. The widget SHALL never change with what was logged and SHALL show no state mark.
There SHALL be no Quick Settings tile for food.

#### Scenario: From the widget
- **WHEN** the user taps the Lisää annos widget
- **THEN** the add search opens over the whole screen with the keyboard up

#### Scenario: From the shortcut
- **WHEN** the user long-presses the app icon and taps Lisää annos
- **THEN** the same search opens

### Requirement: Adding from the home screen
A tap on the widget or the shortcut SHALL open the add search (as in "Adding from the search") over
the whole screen, adding to today, in the app's theme and without first showing the app's front
page, logging its NFR-8 timings (food.sheet.timing) as before. It SHALL work as in the app:
suggestions, search, Enter, the bars with Kumoa, and it SHALL stay open for the next annos.
Leaving it SHALL:
- when at least one annos added in it counts as added ("Leaving the search"): open the app on the
  Ruoka screen of that day. The app's Ruoka SHALL read the day only after the search's pending
  writes have finished, and the highlight SHALL match annokset by the added item's own client id
  and apply as they appear. Back from there follows the app's own back rules;
- otherwise: close it and show what was behind it, normally the home screen.

A tap on the widget or the shortcut while a search is open SHALL leave it as it was when its day is
the same date; when the date has changed it SHALL start a fresh search for today. Adds are already
saved, so nothing is lost.

#### Scenario: Breakfast from the home screen
- **GIVEN** on most mornings around 7 the user logged Rahka 200 g
- **WHEN** they tap the Lisää annos widget at 7.20, tap Rahka and tap back
- **THEN** Rahka 200 g is added to today and the app shows Ruoka with Rahka highlighted

#### Scenario: Back right after an add
- **WHEN** the user taps a suggestion in the search opened from the widget and taps back at once
- **THEN** the app shows Ruoka with that annos present and highlighted

#### Scenario: Several items in a row
- **WHEN** the user types "kahvi", presses Enter, then taps Pulla, then taps back
- **THEN** both are added to today in that order and Ruoka shows both highlighted

#### Scenario: Nothing added
- **WHEN** the user opens the search from the widget and taps back without adding
- **THEN** the home screen is shown again and the app does not open

#### Scenario: Closing at once
- **WHEN** the user taps a suggestion and leaves the search immediately
- **THEN** the annos is still in Health Connect

#### Scenario: Opened again while open
- **WHEN** the user taps the widget again while the search is open on the same date
- **THEN** the search stays as it was

#### Scenario: Opened again after midnight
- **WHEN** the user taps the widget while a search from yesterday is open
- **THEN** a fresh search for today opens

### Requirement: Undoing Ohita
Tapping ✕ (Ohita) on the clipboard card SHALL show "Ohitettu · Kumoa"; Kumoa SHALL make the content
offered again, as if it had never been skipped.

#### Scenario: Skipped by mistake
- **WHEN** the user taps ✕ on the card and then Kumoa
- **THEN** the card is shown again with the same items

### Requirement: Item row buttons
Every logged item row on the Ruoka screen SHALL be one line: the name with its herkku sign, the
kcal ("ilman kcal" when missing), an ⓘ button and one ⋮ button, in that order. The row keeps the
56 dp minimum height of the app's list rows.

The ⓘ button (named "Tiedot: <name>") SHALL open an info bubble (Material 3 rich tooltip) anchored
to the button, with the name as its title and these lines, each only when known:
- the amount with its unit ("200 g", "1 kpl");
- the kcal ("180 kcal"), or "ilman kcal";
- the ruoka's Kalorit ("90 kcal / 100 g", "250 kcal / kpl", whole kcal), taken from the own ruoka
  whose name matches the item's (the app's name key; own ruuat only, never a Valmis ruoka); left
  out when there is none (e.g. removed) or it has no Kalorit;
- "Herkku" when the item is a treat (the item's own flag, the same as the row's sign);
- the logging time "kirjattu klo 7.10" (in the item's own time offset) only when the item has a
  real logging time; annokset added afterwards to an earlier day and imports without a time show
  no time line.
The kcal line is always there, so the bubble is never empty. The shown kcal is the item's stored
kcal, never recomputed from amount × Kalorit. The title shows the full name, wrapped, never
shortened. The bubble has no caret, sits above ⓘ (below when there is no room) inside the
window, and may cover other parts of the screen.

Tapping outside the bubble, back, tapping ⓘ again, or scrolling or dragging the list SHALL close
it, and only close it: that tap or back opens no editor and presses nothing. The bubble also
closes when its item disappears; if the item changes while open, its lines follow. The bubble has
no buttons and changes nothing; opening or closing it SHALL leave the highlight as it was.
Long-press on ⓘ does nothing extra. TalkBack SHALL announce the bubble's title and lines once when
it opens. ⓘ appears only on annos rows on Ruoka, not on ruoka rows or paino rows.

The ⋮ button (named "Toiminnot: <name>") opens a menu with "Muokkaa" (opens the item's editor) and
"Poista" (in the theme's error colour) that deletes the item at once and shows "<name> poistettu ·
Kumoa" as Ruoka's Kumoa bar; Kumoa SHALL bring the item back in its place. Poista SHALL act exactly
as the editor's Poista. On a narrow row the name SHALL be shortened first; kcal, ⓘ and ⋮ never
shrink. Tapping elsewhere on the row SHALL still open the editor. Järjestä rows are one line too,
with only their own move buttons (no ⓘ or ⋮).

#### Scenario: Amount from the info button
- **GIVEN** today has Rahka 200 g, 180 kcal, logged at 7.10, and the own ruoka Rahka has 90 kcal / 100 g
- **WHEN** the user taps ⓘ on Rahka
- **THEN** a bubble titled Rahka shows "200 g", "180 kcal", "90 kcal / 100 g" and "kirjattu klo 7.10",
  and tapping outside it closes it with nothing changed

#### Scenario: Item without amount or kcal
- **GIVEN** Pizza was logged today at 12.30 with no amount and no kcal, and the ruoka Pizza has no Kalorit
- **WHEN** the user taps ⓘ on Pizza
- **THEN** the bubble shows only "ilman kcal" and "kirjattu klo 12.30"

#### Scenario: Added afterwards to yesterday
- **GIVEN** the user added Lasagne to yesterday this morning
- **WHEN** they tap ⓘ on Lasagne
- **THEN** the bubble has no "kirjattu" line

#### Scenario: Bubble open in the sheet
- **GIVEN** an ⓘ bubble is open on Ruoka (the add search no longer lists annokset)
- **WHEN** the user presses back
- **THEN** the bubble closes and Ruoka stays as it was

#### Scenario: Delete from the sheet
- **GIVEN** the user came back from the add search with Tomaatti highlighted on Ruoka
- **WHEN** they tap ⋮ on Tomaatti and then Poista
- **THEN** Tomaatti is gone, the bar reads "Tomaatti poistettu · Kumoa", and Kumoa brings it back
  in its place without the highlight

#### Scenario: Edit from Ruoka
- **WHEN** the user taps ⋮ on Kurkku on the Ruoka screen and then Muokkaa
- **THEN** Kurkku's editor opens

#### Scenario: Menu closed without a choice
- **WHEN** the user opens the ⋮ menu on Kurkku and taps outside it
- **THEN** the menu closes and nothing changes

### Requirement: Correcting a product's energy
A product's energy SHALL count as corrected when it is saved, from an item's editor (Tallenna or
a new item's Lisää) or from Omat ruuat, with the same unit (kpl or 100 g) and a different kcal.
A unit change, a cleared energy, or a save that changes only the amount, Herkku or the name SHALL
NOT count. On a correction, every logged item of that product the app can read from Health
Connect, on any day, whose kcal came from the old energy SHALL be recalculated as amount × new
energy, except the item being edited, which always takes the editor's values. Renaming in the
item editor moves the item to the other product, whose energy alone applies. AI and data-file
imports SHALL never recalculate (they stay append-only).

#### Scenario: Heavier liquorice
- **GIVEN** Lakritsi is 10 kcal / kpl, logged 5 kpl (50 kcal) on three days and once with a typed
  total of 80 kcal and no amount
- **WHEN** the user changes Lakritsi to 12 kcal / kpl in Omat ruuat and taps Päivitä kaikki
- **THEN** the three items read 60 kcal, the 80 kcal item is unchanged, those days' totals rise
  by 10 kcal each, and "Päivitetty 3 annosta · Kumoa" shows

#### Scenario: Amount only
- **WHEN** the user opens a Lakritsi item, changes 5 kpl to 6 kpl and taps Tallenna
- **THEN** no dialog is shown and no other item changes

#### Scenario: Unit change
- **WHEN** the user changes Lakritsi from 10 kcal / kpl to 10 kcal / 100 g
- **THEN** no dialog is shown and earlier items keep their kcal

### Requirement: Which items follow
An item SHALL count as coming from the product's energy when it has an amount in the energy's unit
and its kcal differs from amount × old energy by less than 0,5 kcal. Items without an amount, in
the other unit, without kcal (they are filled as in "Kcal filled in later") or with any other kcal
(the user's own total, an AI import's value) SHALL NOT change and SHALL be counted as kept.

#### Scenario: Own values stay
- **GIVEN** Rahka 65 kcal / 100 g, an item 200 g at 130 kcal and AI-imported items 200 g at 150
  and at 131 kcal
- **WHEN** the user changes Rahka to 70 kcal / 100 g and taps Päivitä kaikki
- **THEN** the first item reads 140 kcal and the imported ones stay 150 and 131 kcal

#### Scenario: Decimal energy
- **GIVEN** Keksi 12,5 kcal / kpl and an item 3 kpl at 37,5 kcal
- **WHEN** the user changes Keksi to 15 kcal / kpl and taps Päivitä kaikki
- **THEN** the item reads 45 kcal

### Requirement: Confirming a correction
When a correction would change at least one other item, a dialog SHALL ask before anything is
written: title "Päivitetäänkö aiemmat annokset?", the line "<name>: <N> annosta <D> päivältä,
yhteensä <old> → <new> kcal" (the sums over those N items, rounded after summing), a line "<M>
itse annettua annosta pysyy ennallaan" when M > 0, and the buttons "Päivitä kaikki", "Vain
jatkossa" and Peruuta, with singular forms for 1. The dialog SHALL govern only the recalculation:
the product's new energy, a rename, Herkku and the filling of items without kcal SHALL happen as
today with Päivitä kaikki or Vain jatkossa. Vain jatkossa SHALL save without recalculating;
Peruuta SHALL write nothing and return to the editor. With no other item to change there SHALL be
no dialog.

#### Scenario: Only from now on
- **GIVEN** Lakritsi 10 kcal / kpl logged 5 kpl on three days
- **WHEN** the user changes it to 12 kcal / kpl and taps Vain jatkossa
- **THEN** earlier items keep 50 kcal and the next Lakritsi 5 kpl is 60 kcal

#### Scenario: Cancel
- **WHEN** the user taps Peruuta in the dialog
- **THEN** the editor is still open with the new value typed and nothing is saved

#### Scenario: Nothing else logged
- **GIVEN** a product logged only in the item being edited
- **WHEN** the user changes its energy and saves
- **THEN** no dialog is shown

### Requirement: Undoing a correction
After Päivitä kaikki a bar "Päivitetty <N> annosta · Kumoa" SHALL show where the edit was made
(in the add search as its bar, replacing the previous one; the app bar on Ruoka and in Omat
ruuat) for as long as such bars last. Kumoa SHALL undo the whole save: the ruoka's old energy, the
edited item as it was, and every recalculated item whose kcal is still amount × new energy (an
item changed again since stays). When the bar is gone Kumoa is gone. When writing the recalculated
items fails, the bar SHALL read "Päivitettiin <X>/<N> annosta · Yritä uudelleen"; a retry SHALL
recalculate from the items as they are now.

#### Scenario: Undo
- **WHEN** the user taps Kumoa on "Päivitetty 3 annosta"
- **THEN** Lakritsi is 10 kcal / kpl again and the three items read 50 kcal

### Requirement: Product use count
The product editor in Omat ruuat SHALL show under the energy field how many items of the product
are logged ("Lisätty 6 kertaa", "Lisätty 1 kerran"); it SHALL show nothing when the product is
not logged or the items are not known.

#### Scenario: Use count
- **GIVEN** Lakritsi logged on four items
- **WHEN** the user opens Lakritsi in Omat ruuat
- **THEN** the editor reads "Lisätty 4 kertaa" under the energy

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
- Style: bodyMedium in onSurfaceVariant, no icon, same in the add search, Ruoka and the front page.
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
and category. While Valmiit ruuat is off no base product is consulted.

#### Scenario: Alias typed
- **GIVEN** no own product Ranskalaiset, and the base product Ranskalaiset perunat 290 kcal / 100 g,
  Herkku, with the alias Ranskalaiset
- **WHEN** the user types "Ranskalaiset" and presses enter
- **THEN** an item Ranskalaiset with 290 kcal / 100 g at its usual amount is added, not a treat,
  and Ranskalaiset is an own product

### Requirement: Adding from the search
"Lisää annos" (in the ⋮ menu of the front page and Historia, and on the Ruoka screen) SHALL open
the add search over the whole screen, adding to the day shown (today from the front page and
Historia). It SHALL show only what can be added and this search's own adds (the marks and the
added strip below): no day list, no day total and no eaten annos. Tapping a ruoka row SHALL add that ruoka at once at the end of the day with its
usual amount; the field is then cleared and the search stays open for the next annos.

Details:
- Top bar: a back arrow ("Takaisin"), the field with a search icon and the hint "Hae ruokaa tai
  kirjoita kcal", a ✕ button ("Tyhjennä") while the field has text, and a ⋮ menu holding Kopioi
  tuontiohje and Tuo ruoka-annokset. The field has focus and the keyboard is up when the search
  opens, from every entry, once the permission is confirmed granted ("Search without permission").
- The line "Lisätään: <day>" (the day's title as elsewhere: Eilen, else the date like "Su 15.11.";
  bodyMedium, onSurfaceVariant) shows only when the search's day is not the current date, from the
  start (also when opened from Ruoka on a past day).
- The clipboard card, when offered ("Clipboard offer"), hidden while typing.
- Empty field: the heading "Usein tähän aikaan" and up to eight ruoka rows: the ruuat most often
  logged within two hours of the current clock time in the last 30 days, then the most often
  logged ruuat. With none, the heading is left out and a quiet line "Kirjoita ruuan nimi tai
  pelkkä kcal-määrä" shows, but only when the day and history were read successfully (see
  "Search messages and loading"). The ranking is computed when the search opens and stays fixed
  while it is open: adds do not reshuffle the rows; only a removed ruoka drops out, and Kumoa of
  its removal brings it back in its place.
- Typing: up to ten matching ruuat (own before Valmiit ruuat, best match first, directly under
  the top bar) and as the last row "Lisää ”<text>” uutena" with a line telling what it adds (for a
  bare number "Lisää pelkkä <N> kcal"; for a known ruoka its kcal, for a new name "täytä tiedot").
  Clearing the field (✕ or deleting the text) brings the empty-field content back. The list
  scrolls when it does not fit; scrolling it hides the keyboard.
- Every ruoka row starts with a + icon in the primary colour (TalkBack click label "Lisää <name>")
  and shows the ruoka's name (one line, "…" when it does not fit) with the herkku sign for a
  Herkku ruoka, under it the usual amount and its kcal ("200 g · 130 kcal"; with no usual amount
  the Kalorit "140 kcal / 100 g"; "200 g · ei kcal" or "ei kcal" when unknown), and ends with a ⋮
  button ("Removing a product from the search"). Rows have no dividers. The "Lisää … uutena" row
  starts with the same + icon and has no ⋮.
- Tapping "Lisää … uutena" does what Enter does. Enter adds exactly the typed text: a bare number
  as an annos "Pikalisäys" with that many kcal, a known ruoka's name (own, or Valmiit ruuat by name
  or alias) as an annos of that ruoka. A new name does not add at once: it opens the editor for a
  new annos (as ⋮ → Muokkaa below) with the name filled and no kcal, since the details come next;
  Lisää adds it as edited, Peruuta returns to the search with the typed text kept and adds nothing.
- After any add the keyboard stays as it was and the bar "<name> <amount> lisätty · Kumoa" shows
  ("Search messages and loading"); Kumoa removes that annos.
- Added mark: a ruoka row whose ruoka has annokset counted as added in this search ("Leaving the
  search") shows a mark at its end, just before its ⋮: "✓" for one annos, "×2", "×3" … for more,
  in the primary colour. The mark is not a control (a tap on it is a tap on the row, which adds
  again); TalkBack reads it as part of the row ("lisätty", "lisätty 2 kertaa"). An annos counts
  for the ruoka it was logged as, after the typed name was resolved to a ruoka (a row tap,
  Muokkaa → Lisää, Enter on a known name, the clipboard's Lisää kaikki), and names are compared
  as ruoka names are (case and surrounding spaces do not matter). A Valmiit ruuat row whose alias
  became an own ruoka under the typed name is marked too. Annokset of ruuat not listed in the
  rows count only in the strip. Kumoa, a failed write or a delete lowers the count; at zero the
  mark goes. The "Lisää … uutena" row has no mark.
- Added strip: while at least one annos counts as added in this search, a strip at the search's
  bottom (above the keyboard when it is up, the bar above the strip) reads "1 annos lisätty ·
  130 kcal" or "2 annosta lisätty · 465 kcal" (every annos counted as added in this whole search,
  also the clipboard's and Pikalisäys, with their known kcal), with
  "· 1 ilman kcal" added when some have no kcal and "· ei kcal" when none has any, and ends with
  a filled "Valmis" button that leaves the search as back does ("Leaving the search"). Numbers
  and their units do not break apart. The strip goes when the count drops to zero.
- The ⋮ menu's "Muokkaa" opens the editor for a new annos: the same editor and rules as an annos's
  Muokkaa (name, amount and unit, Kalorit, Herkku; changes reach the ruoka as after Tallenna),
  filled as a tap would add it, with Lisää and Peruuta instead of Tallenna and Poista. Lisää adds
  the annos as edited (with the same bar) and returns to the search; Peruuta, or closing the
  editor, returns to the search and adds nothing. The search returns with the field as it was and
  the keyboard up only if it was up before.
- The search cannot be opened from Järjestä, and Ruoka's own clipboard card is not shown while the
  search is open.

#### Scenario: Only addable things
- **GIVEN** today has Rahka and Lumipallo logged
- **WHEN** the user taps Lisää annos on Ruoka
- **THEN** the search opens over the whole screen with the keyboard up, "Usein tähän aikaan" rows
  each starting with +, and neither Rahka nor Lumipallo as an eaten annos or the day's total

#### Scenario: Usual breakfast in one tap
- **GIVEN** on most mornings around 7 the user logged Rahka 200 g
- **WHEN** the user opens Lisää annos at 7.20
- **THEN** among the first rows is "Rahka" with "200 g · 130 kcal", and one tap logs Rahka 200 g,
  clears nothing else and shows "Rahka 200 g lisätty · Kumoa"

#### Scenario: Adds stay visible
- **GIVEN** the search is open with Rahka (200 g, 130 kcal) and Kahvi (1 kpl, 5 kcal) among the rows
- **WHEN** the user taps Rahka, Kahvi and Kahvi again and the bars have faded
- **THEN** the Rahka row shows ✓, the Kahvi row shows ×2, and the strip reads "3 annosta lisätty ·
  140 kcal" with Valmis

#### Scenario: Undo lowers the count
- **GIVEN** the Kahvi row shows ×2
- **WHEN** the user taps Kumoa on the latest Kahvi bar
- **THEN** the Kahvi row shows ✓ and the strip counts one annos fewer

#### Scenario: Without kcal
- **WHEN** the user adds Rahka 130 kcal and "työpaikan pizza" without kcal
- **THEN** the strip reads "2 annosta lisätty · 130 kcal · 1 ilman kcal"

#### Scenario: Nothing added yet
- **WHEN** the search opens
- **THEN** no row has a mark and no strip shows

#### Scenario: Valmis
- **GIVEN** the user added Rahka from the front page's Lisää annos
- **WHEN** the user taps Valmis on the strip
- **THEN** the Ruoka screen shows today with Rahka highlighted, as back would

#### Scenario: Rows stay put
- **GIVEN** the empty field lists Rahka first and Kahvi second
- **WHEN** the user taps Kahvi
- **THEN** the rows keep their order, and a ruoka removed with Poista drops out while Kumoa
  brings it back in its place

#### Scenario: Past day from Ruoka
- **WHEN** the user opens Lisää annos on Ruoka showing yesterday
- **THEN** the line "Lisätään: Eilen" shows from the start

#### Scenario: Tap target label
- **WHEN** TalkBack reads a ruoka row
- **THEN** its click action is named "Lisää Rahka"

#### Scenario: Set the amount before adding
- **GIVEN** a ruoka Jauhelihakastike 140 kcal / 100 g, usually 250 g
- **WHEN** the user types "jauh", taps ⋮ → Muokkaa on its row, changes the amount to 350 g and taps
  Lisää
- **THEN** Jauhelihakastike 350 g, 490 kcal is logged, the search is shown again with the bar, and
  the next Jauhelihakastike starts at 350 g

#### Scenario: Edit and cancel
- **WHEN** the user opens the new-annos editor and then taps Peruuta
- **THEN** nothing is added and the search is as before

#### Scenario: What Enter does
- **WHEN** the user types "kana"
- **THEN** matching ruuat are listed under the field, the last row reads "Lisää ”kana” uutena",
  and tapping it adds what Enter would

#### Scenario: Only a number
- **WHEN** the user types 350 and presses Enter
- **THEN** an annos "Pikalisäys" with 350 kcal is logged, the field is cleared and the bar shows

#### Scenario: A whole day as one number
- **GIVEN** the user estimated yesterday at 2 200 kcal with an AI chat
- **WHEN** they move to yesterday on Ruoka, tap Lisää annos, type 2200 and press Enter
- **THEN** the search reads "Lisätään: Eilen", yesterday gets an annos "Pikalisäys" 2 200 kcal,
  and back shows yesterday on Ruoka with it highlighted

#### Scenario: Forgiving search
- **WHEN** the user types "jaulihak"
- **THEN** Jauhelihakastike is suggested (letters ä/a and ö/o equal, parts of compound words and
  one wrong letter from four typed letters match)

#### Scenario: Back with the keyboard up
- **WHEN** the keyboard is up and the user presses system back
- **THEN** the keyboard hides and the search stays open

#### Scenario: Long name on one line
- **WHEN** a ruoka "Keitetty täysjyvämakaroni" is listed on a narrow phone
- **THEN** the row shows the name on one line shortened with "…", the amount and kcal under it,
  and the ⋮ button in full

#### Scenario: Only a name
- **GIVEN** no ruoka is named "työpaikan pizza"
- **WHEN** the user types "työpaikan pizza" and presses Enter
- **THEN** the editor for a new annos opens with the name "työpaikan pizza", Lisää and Peruuta, and
  nothing is logged until Lisää

#### Scenario: Known name adds at once
- **GIVEN** an own ruoka Rahka 130 kcal
- **WHEN** the user types "rahka" and presses Enter
- **THEN** Rahka is logged at once with the bar "Rahka 200 g lisätty · Kumoa"

### Requirement: Leaving the search
Leaving the search (the back arrow, the added strip's Valmis, or system back when the keyboard is
down; back with the keyboard up first hides it) SHALL, when at least one annos added in this search is counted as added
(below), show the Ruoka screen on the search's day with those annokset highlighted ("Highlight on
Ruoka") and the list scrolled so the last of them is visible; otherwise it SHALL return to the
screen it was opened from, as it was.

An annos counts as added in this search from the moment its add is queued (also before the day has
been read or while it is being written). It stops counting when its write fails, when Kumoa removes
it and when it is deleted. Back with only failed adds therefore adds nothing.

Ruoka keeps its own back rule and origin. When the search was opened from Ruoka, leaving stays on
Ruoka and Ruoka's origin is unchanged. When it was opened from Historia, back from that Ruoka
returns to Historia on the tab that was open; when from the front page, back returns to the front
page.

Only the back arrow, Valmis and system back apply this rule (also in the search opened from the
home-screen widget or shortcut, "Adding from the home screen"). Home and the recents button only
send the search to the background. When the process was killed or the screen was recreated so that
the search session is lost, the next open is a new search and nothing is highlighted.

A bar showing when the user leaves the search SHALL be dropped, so its Kumoa is gone; the annos
can still be deleted on Ruoka.

Each leaving SHALL be logged as `food.search.end added=<count> via=back|done ms=` (ms since the
search opened; never names or kcal).

#### Scenario: A meal of several annokset
- **WHEN** the user taps Rahka, types "kahvi" and presses Enter, then taps back
- **THEN** both are logged in that order, the Ruoka screen shows the day with Rahka and kahvi
  highlighted and kahvi visible

#### Scenario: Undo in the search
- **WHEN** the user taps Rahka and then Kumoa on its bar
- **THEN** Rahka is not logged, and back with nothing else added returns where the search was opened

#### Scenario: Failed add only
- **WHEN** the user's only add fails with "Tallennus epäonnistui" and the user taps back
- **THEN** nothing is highlighted and the user returns where the search was opened

#### Scenario: Nothing added
- **WHEN** the user opens Lisää annos from the front page and taps back without adding
- **THEN** the front page is shown as before

#### Scenario: From the front page
- **WHEN** the user opens Lisää annos from the front page's ⋮ menu, adds Pulla and taps back
- **THEN** the Ruoka screen shows today with Pulla highlighted, and back from Ruoka returns to the
  front page

#### Scenario: From Historia
- **GIVEN** Historia is on its Syöty tab
- **WHEN** the user opens Lisää annos from its menu, adds Pulla, taps back and then back again on Ruoka
- **THEN** Ruoka showed Pulla highlighted and the second back shows Historia on the Syöty tab

#### Scenario: From Ruoka
- **GIVEN** the user opened Ruoka from the front page and Lisää annos from Ruoka
- **WHEN** the user adds Pulla and taps back, and then back again
- **THEN** Ruoka is shown with Pulla highlighted and the second back returns to the front page

#### Scenario: Home button
- **WHEN** the user adds Pulla in the search and presses the home button
- **THEN** the search is only sent to the background and no Ruoka screen is opened

#### Scenario: Session lost
- **GIVEN** the search was in the background and the system ended the app's process
- **WHEN** the user opens Lisää annos again
- **THEN** it is a new search and nothing is highlighted on Ruoka

#### Scenario: Bar dropped on leaving
- **WHEN** the user taps Pulla and leaves the search while "Pulla 1 kpl lisätty · Kumoa" shows
- **THEN** the bar is gone and Pulla can be deleted on Ruoka

#### Scenario: Leaving logged
- **WHEN** the user adds two annokset and taps Valmis
- **THEN** the log has `food.search.end added=2 via=done ms=…`

### Requirement: Highlight on Ruoka
The annokset added in a search SHALL be shown on Ruoka with a tonal background (the highlight).
The highlight SHALL be cleared by changing the day, by leaving Ruoka (to the front page, Historia
including Viikko, or Asetukset) and by opening Lisää annos again. It SHALL stay while an annos
editor or Järjestä is open and when the app goes to the background and returns. Editing a
highlighted annos keeps its highlight; deleting it drops it, and Kumoa of the delete brings the
annos back without the highlight.

#### Scenario: Cleared by leaving
- **GIVEN** Ruoka shows Pulla highlighted
- **WHEN** the user opens Historia and returns to Ruoka
- **THEN** Pulla is no longer highlighted

#### Scenario: Cleared by changing the day
- **WHEN** the user moves from the highlighted day to the previous day and back
- **THEN** nothing is highlighted

#### Scenario: Kept while editing
- **WHEN** the user opens a highlighted annos's editor, saves, and the app goes to the background and returns
- **THEN** the annos is still highlighted

#### Scenario: Delete and Kumoa
- **WHEN** the user deletes a highlighted annos and taps Kumoa
- **THEN** it is back in its place without the highlight

### Requirement: Search messages and loading
Every message of the add search SHALL be shown as the app's bar at the search's bottom (above the
keyboard when it is up): "<name> <amount> lisätty · Kumoa", "Lisättiin <N> annosta · Kumoa"
(the clipboard's Lisää kaikki: what that tap added, unlike the strip's count of the whole search),
"<name> poistettu · Kumoa", "Ohitettu · Kumoa", "Päivitetty <N> annosta · Kumoa",
"Päivitettiin <X>/<N> annosta · Yritä uudelleen", the clipboard-changed notice, the energy read
failure, "Tallennus epäonnistui" and "Päivän annoksia ei voitu lukea". The newest replaces the
previous one. A bar with Kumoa or Yritä uudelleen lasts as the app's Kumoa bars (about 4 seconds,
swipe away); a bar without an action lasts the app's normal short message length.

The add bar SHALL name what was added with its amount as the ruoka rows write it: "Rahka 200 g
lisätty", "Pulla 1 kpl lisätty"; a Pikalisäys with its kcal ("Pikalisäys 350 kcal lisätty"); an
annos without an amount with its name only ("työpaikan pizza lisätty"). A long name wraps to a
second line rather than cutting the amount off.

A failed write SHALL show "Tallennus epäonnistui" and keep the search open. The field SHALL be
usable at once. The day SHALL be read in the background when the search opens; an add made before
the day is read SHALL be placed at the end of the day once it has been read. A failed read SHALL
show "Päivän annoksia ei voitu lukea" once, typing and adding SHALL still work, and the day SHALL
be read again when the search resumes and before the next write.

While the day or history is loading, with the field empty, nothing SHALL show below the field
except a quiet line "Ladataan…". After a failed read the bar shows and no rows are shown. The line
"Kirjoita ruuan nimi tai pelkkä kcal-määrä" shows only when the read succeeded and there are no
suggestion rows.

#### Scenario: Add before the day is read
- **WHEN** the user types a name and presses Enter before the day has loaded
- **THEN** the annos is added at the end of the day once it has loaded, and counts as added from the tap

#### Scenario: Failed write
- **WHEN** Health Connect refuses a write
- **THEN** the bar "Tallennus epäonnistui" is shown, the search stays open and the annos no longer counts as added

#### Scenario: Day cannot be read
- **WHEN** reading the day fails
- **THEN** the bar "Päivän annoksia ei voitu lukea" is shown with no rows, typing still works, and an
  added annos is placed at the end after the day has been read again

#### Scenario: Loading
- **WHEN** the search opens and the day is still being read
- **THEN** only the line "Ladataan…" shows below the empty field

#### Scenario: Newest bar wins
- **WHEN** the user adds Pulla and then removes a ruoka with Poista
- **THEN** only "… poistettu · Kumoa" shows

#### Scenario: Bar names the amount
- **WHEN** the user taps Rahka, whose usual amount is 200 g
- **THEN** the bar reads "Rahka 200 g lisätty · Kumoa"

#### Scenario: Bar for a bare number
- **WHEN** the user types 350 and presses Enter
- **THEN** the bar reads "Pikalisäys 350 kcal lisätty · Kumoa"

### Requirement: Search without permission
The add search SHALL check the nutrition permission before showing its rows and focusing the field:
while checking it SHALL show only the top bar, without the keyboard. Without the permission it
SHALL show the explanation and a Salli button requesting it, and show the rows, focus the field
and raise the keyboard once granted. From every entry (the app, the widget and the shortcut) the
field gets focus and the keyboard comes up once the permission is confirmed granted.

#### Scenario: Permission missing
- **GIVEN** the nutrition permission is not granted
- **WHEN** the user taps the widget
- **THEN** the search explains why the permission is needed and offers Salli; once granted the
  rows show and the keyboard comes up

### Requirement: Search with permission denied or Health Connect missing
When the permission has been denied the add search SHALL show the same text and a button that
opens Health Connect's app permissions, and check again when the user returns. When Health Connect
is not available it SHALL show the existing text; the back arrow closes the search.

#### Scenario: Permission denied
- **GIVEN** the permission was denied
- **WHEN** the user taps the widget
- **THEN** the search offers a button to Health Connect's permissions and checks again on return

### Requirement: Search over midnight
The add search SHALL add to the day it was opened on. The date SHALL be re-checked when the search
resumes and on each add (there is no live ticker); when the day is then no longer the current
date, the line "Lisätään: Eilen" SHALL show, and leaving after an add SHALL show that day on
Ruoka. The "Usein tähän aikaan" ranking stays as computed when the search opened. A new open SHALL
use the new today.

#### Scenario: Open past midnight
- **GIVEN** the search was opened at 23.55
- **WHEN** the user adds an annos at 00.05
- **THEN** the annos goes to the earlier day and the search reads "Lisätään: Eilen"

### Requirement: Import from the home-screen search
"Tuo ruoka-annokset" and the card's "Muokkaa" in the search opened from the widget or the shortcut
SHALL close that search and open the app on the import preview over the front page, as when the
estimate is shared to the app. Any search or editor open in the app SHALL close. After Lisää or
Peruuta the user SHALL stay in the app. The card's "Lisää kaikki" SHALL save in the search itself
and keep it open.

#### Scenario: Import from the search
- **WHEN** the user taps Tuo ruoka-annokset in the search opened from the widget
- **THEN** the app opens on the import preview, as when the estimate is shared to the app

#### Scenario: After the import
- **WHEN** the user taps Lisää or Peruuta on the preview
- **THEN** the app stays open

#### Scenario: Offer in the search
- **GIVEN** the user copied food JSON with 4 items
- **WHEN** they open the search from the widget and tap Lisää kaikki
- **THEN** the 4 annokset are added to today, the search stays open and shows
  "Lisättiin 4 annosta · Kumoa"

### Requirement: Removing a product from the search
Each ruoka row in the add search SHALL end with a ⋮ button ("Toiminnot: <name>") whose menu offers
"Muokkaa" (the editor for a new annos, see "Adding from the search") and, for an own ruoka only, "Poista"
(in the theme's error colour). Poista SHALL remove the ruoka (as removing it under Omat ruuat in
Asetukset does: logged annokset keep their values), take it off the rows and show the bar
"<name> poistettu · Kumoa"; Kumoa SHALL bring the ruoka back as it was. Valmiit ruuat SHALL have
no Poista.

#### Scenario: Remove a one-off
- **WHEN** the user types "lumi" and taps ⋮ → Poista on Lumipallo
- **THEN** Lumipallo is gone from the rows, earlier Lumipallo annokset stay on their days, and
  Kumoa brings it back

#### Scenario: Valmis ruoka
- **WHEN** the user types "kaurapuu" and Kaurapuuro is a Valmis ruoka not yet used
- **THEN** its ⋮ menu offers Muokkaa but no Poista

### Requirement: Recent amounts in the editor
The item editor SHALL show under the amount field up to three chips ("200 g", "1,5 kpl"): the
distinct amounts of the same ruoka's other annokset (matched by key, all days) in the chosen kpl or
g, newest first by day then logging time. A tap SHALL fill the amount field and save nothing; the
chip equal to the field shows as chosen. With no such amounts, for Yhteensä and while loading
there SHALL be no chips row. Switching kpl and g keeps the field's number.

#### Scenario: Pick an earlier amount
- **GIVEN** Rahka was logged as 250 g yesterday, 200 g two days ago and 250 g three days ago, and
  today's Rahka annos is 150 g
- **WHEN** the user opens today's Rahka in the editor
- **THEN** chips "250 g" and "200 g" are shown in that order, and tapping "200 g" sets the amount
  to 200 and the result line to 200 g's kcal

#### Scenario: Unit follows the choice
- **GIVEN** Pulla was logged as 1 kpl, as 80 g and as 2 kpl (the annos being edited)
- **WHEN** the editor of the 2 kpl annos is on kpl and the user switches it to g
- **THEN** the chip "1 kpl" is replaced by "80 g" and the field still holds 2

#### Scenario: Skipped
- **GIVEN** an earlier annos of the ruoka is Yhteensä or is still loading
- **THEN** it adds no chip, and nothing shows while the annokset load

#### Scenario: Nothing to offer
- **GIVEN** a ruoka logged only once
- **WHEN** its annos is opened in the editor
- **THEN** the editor shows no chips row

#### Scenario: At most three
- **GIVEN** Kaurapuuro was logged with five different amounts
- **WHEN** its annos is opened in the editor
- **THEN** the three most recently logged distinct amounts are shown
