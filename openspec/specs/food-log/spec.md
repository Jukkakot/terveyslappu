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

### Requirement: Adding an item
"Lisää annos" (in the ⋮ menu of the front page and Historia, and on the Ruoka screen) SHALL open the add sheet, adding to the
day shown (today from the front page). The sheet SHALL take the full height and show the day it
adds to, with adding kept apart at the bottom.

From the top the sheet SHALL show: a header "<day> · <total> kcal" (Tänään, Eilen or the date;
"<day>" alone while the day is unread or empty) with a ⋮ menu holding Kopioi tuontiohje and Tuo
ruoka-annokset; under it the Ruoka screen's summary line without the total ("herkkuja 230 kcal · 1 ilman
kcal", parts with zero left out, the line left out when both are zero); the clipboard card when it
is offered ("Clipboard offer"); then the day's items in eating order as compact rows (name on one line, shortened with "…" when it does not fit, treat sign, amount, kcal; no logging time), scrolling, with "Ei vielä annoksia" when the day
has none. Tapping an item SHALL open its editor ("Editing, order and delete"). The item added last
in this sheet SHALL be highlighted with a tonal background, and the list SHALL scroll to show it.

At the bottom the sheet SHALL show the add panel on a surface of its own tone (surfaceContainerHigh,
with a 1 dp outlineVariant line along its top edge), so it reads apart from the day list. A small
handle centred on the panel's top edge SHALL fold and unfold the panel ("Folding the add panel"
below). Unfolded, the panel shows: a message
line only when "Sheet messages and loading" has one, then with
the field empty the heading "Lisää – usein tähän aikaan" and up to eight chips, one per product:
the products most often logged within two hours of the current clock time in the last 30 days,
then the most often logged products, in at most three wrapped rows (chips that do not fit are left
out). A chip SHALL read "+ <name> <kcal> kcal" with the kcal of its usual amount, in a slightly smaller type than the other labels (labelMedium), or "+ <name>" when the
energy is unknown; it SHALL show no treat sign. Last, the field with a search icon and the hint
"Hae ruokaa tai kirjoita kcal".

Tapping a chip SHALL add that product at once at the end of the day with its usual amount, show it
in the day list, highlighted and scrolled into view; no confirmation line is shown (the row's ⋮ menu replaces Muuta and
Kumoa). A long press on a chip SHALL open the editor for a new item
of that product (below); the chip's accessibility actions SHALL offer the same.

While typing, the list "Omat ruuat" SHALL cover the day list and the chips, directly above the field:
up to five matching products, own products before base products, each row with the product's name
and under it its usual amount and the kcal that amount gives ("200 g · 130 kcal"; with no usual
amount the energy, "140 kcal / 100 g"; "200 g · ei kcal" or "ei kcal" when the energy is unknown),
the treat sign for a Herkku product, and a ⋮ button (see "Removing a product from the sheet"); and as the last row "Lisää ”<text>”
uutena" with a line telling what it adds (for a bare number "Lisää pelkkä <N> kcal"; for other text
"ilman kcal" or the known product's kcal); tapping it SHALL do what Enter does. Tapping a product
row SHALL add it as a chip does. Clearing the field SHALL bring the day list and the chips back.

The editor for a new item (a match row's ⋮ → Muokkaa or a chip's long press) SHALL be the same
editor and rules as Muuta (name, amount and unit, energy, Herkku; changes reach the product as
after Tallenna), filled as a tap would add it, with Lisää and Peruuta instead of Tallenna and
Poista: Lisää SHALL add the item as edited, show it highlighted in the day list and return to the
sheet; Peruuta SHALL return to the sheet and add nothing (as does closing the editor). Leaving the
item editor by Tallenna, Peruuta or Poista SHALL return to the sheet with the field as it was, and
the line SHALL then show the edited item (none after Poista). Pressing enter SHALL add exactly the
typed text: a bare number as an item "Pikalisäys" with that many kcal, any other text as an item of
that name (its product's kcal when the name is a known product, otherwise without kcal), then
clear the field and keep the keyboard up for the next item.

Details:
- Header: "<day>" is `dayTitle` capitalised (Tänään, Eilen, date) and names the sheet's day
  relative to the current date (after midnight a sheet opened yesterday reads "Eilen · …"). It
  shows "· <total> kcal" whenever the day has at least one item, also "· 0 kcal".
- Layout: the header and summary line stay pinned; the add panel never scrolls and sits directly
  above the keyboard when it is up; only the day list shrinks. When the day list would be shorter
  than two rows, the chips drop to fewer rows (down to one). A drag down on the sheet's handle, or on the
  day list at its top, closes the sheet; the panel and its handle never close the sheet.
- Clipboard card: the first entry of the scrolling day list (it scrolls away), hidden while typing.
- While typing, the Omat ruuat rows replace the day list (header and summary stay), bottom-aligned
  just above the line and the field, best match nearest the field and "Lisää … uutena" closest to
  it; they scroll when they do not fit.
- Highlight: only the newest add in this sheet (for Lisää kaikki the last of its items); it stays
  until the next add or the sheet closes; Kumoa or Poista of it clears it; editing other items does
  not move it; the list scrolls it into view also with the keyboard up.
- The line: editing an item from the day list that was not the line's item leaves the line as it
  was; Poista of any item shows "<name> poistettu · Kumoa", and Kumoa restores it in its place.
- Chips: filled in rank order, stopping at the first chip that does not fit (no backfill); a long
  name is ellipsized at about 60 % of the panel width; the kcal carries its unit ("145 kcal"). With no chips the
  heading and chip area are left out. The accessibility action of a chip is "Muokkaa ennen
  lisäämistä".
- Keyboard: a chip tap keeps the keyboard as it was; scrolling the day list hides it; after an
  editor the keyboard comes back only if it was up before; back first closes the keyboard.
- From the Ruoka screen the sheet is the same; Ruoka's own clipboard card is not shown while the
  sheet is open, and the sheet cannot be opened from Järjestä.

Folding the add panel:
- A drag down on the panel's handle, or on the panel's heading and chip area, folds the panel: the
  heading, chips and field go, the keyboard goes down, and only the handle (with the message line
  above it when there is one, so Kumoa stays reachable) is left, so the day list grows. A drag up
  on the handle, or a tap on it, unfolds the panel without opening the keyboard; a tap on the
  handle of an unfolded panel folds it. The change animates briefly.
- Folded or not lasts until the sheet closes; every open starts unfolded. Adding, deleting and
  editing do not change it. A drag down on a folded panel does nothing.
- The handle shows whenever the field is empty (with or without chips) and is hidden while typing;
  with the keyboard up it works the same.
- The handle is at least 48 dp tall as a touch target; TalkBack names it "Piilota lisäys" or
  "Näytä lisäys" and its click toggles.

#### Scenario: The day first
- **GIVEN** today the user logged Rahka 250 g at 7.10 and Jäätelö 130 g (Herkku, 260 kcal) at 19.05
- **WHEN** they open the sheet from the widget
- **THEN** the header reads "Tänään · 405 kcal", the line under it "herkkuja 260 kcal", both items are
  listed without their times, the keyboard is down, and the chips sit above the field at the bottom

#### Scenario: Usual breakfast in one tap
- **GIVEN** on most mornings around 7 the user logged Rahka 200 g
- **WHEN** the user taps Lisää annos at 7.20
- **THEN** among the first chips is "+ Rahka 130 kcal", and one tap logs Rahka 200 g with its kcal,
  shows it highlighted at the end of the day list and raises the header total by 130

#### Scenario: Set the amount before adding
- **GIVEN** a product Jauhelihakastike 140 kcal / 100 g, usually 250 g
- **WHEN** the user long-presses its chip (or types "jauh" and taps ⋮ → Muokkaa on its row),
  changes the amount to 350 g and taps Lisää
- **THEN** Jauhelihakastike 350 g, 490 kcal is added and highlighted in the day list, and
  the next Jauhelihakastike starts at 350 g

#### Scenario: Edit and cancel
- **WHEN** the user opens the new-item editor and then taps Peruuta
- **THEN** nothing is added and the sheet is as before

#### Scenario: Edit a typed match
- **WHEN** the user types "kaurapuu" and taps ⋮ → Muokkaa on Kaurapuuro
- **THEN** the editor opens with Kaurapuuro's typical portion, and Lisää adds it as edited

#### Scenario: What enter does
- **WHEN** the user types "kana"
- **THEN** the Omat ruuat rows replace the day list and the chips above the field, the last row reads
  "Lisää ”kana” uutena", and tapping it adds what Enter would

#### Scenario: Only a number
- **WHEN** the user types 350 and presses enter
- **THEN** an item "Pikalisäys" with 350 kcal is added, the field is cleared and the day list shows it

#### Scenario: A whole day as one number
- **GIVEN** the user estimated yesterday at 2 200 kcal with an AI chat
- **WHEN** they move to yesterday on Ruoka, tap Lisää annos, type 2200 and press enter
- **THEN** yesterday has an item "Pikalisäys" 2 200 kcal next to any other items, counted in
  Energia like any item, and the sheet's header reads "Eilen · …"

#### Scenario: Only a name
- **WHEN** the user types "työpaikan pizza", which is no product yet, and presses enter
- **THEN** an item "työpaikan pizza" without kcal is added and the line under the header counts
  "1 ilman kcal"

#### Scenario: Forgiving search
- **WHEN** the user types "jaulihak"
- **THEN** Jauhelihakastike is suggested (letters ä/a and ö/o equal, parts of compound words and
  one wrong letter from four typed letters match)

#### Scenario: Edit an eaten item in the sheet
- **WHEN** the user taps Banaani in the sheet's day list, changes it to 2 kpl and taps Tallenna
- **THEN** the sheet shows Banaani 2 kpl with the new kcal and the new total

#### Scenario: Back to the field after editing
- **WHEN** the user taps Muuta, changes the amount and taps Tallenna
- **THEN** the sheet is shown again and the line shows the edited item

#### Scenario: Back to the field after cancelling or deleting
- **WHEN** the user taps Muuta and then Peruuta, or Poista
- **THEN** the sheet is shown again; after Poista no item line is shown

#### Scenario: Fold to look at the day
- **GIVEN** the sheet is open with chips showing and the day has eight items
- **WHEN** the user drags the panel's handle down
- **THEN** the heading, chips and field go, only the handle stays at the bottom, more day rows
  show, and the sheet stays open

#### Scenario: Unfold again
- **WHEN** the user taps the handle of a folded panel
- **THEN** the heading, chips and field come back, with the keyboard down

#### Scenario: Next open starts unfolded
- **GIVEN** the user folded the panel and closed the sheet
- **WHEN** they open the sheet again
- **THEN** the chips and the field show

#### Scenario: Long name on one line
- **WHEN** the day holds "Keitetty täysjyvämakaroni" 200 g, 300 kcal on a narrow phone
- **THEN** the row shows the name on one line shortened with "…", "200 g" under it, and the kcal
  and the ⋮ button in full

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
Food and kcal SHALL be shown only inside the app, the app's own dialog included: no widget face,
tile, shortcut label or notification SHALL show what was eaten, how many items or how many kcal. A
widget or shortcut that only opens the add field is allowed.

#### Scenario: Nothing outside
- **WHEN** the user logs food
- **THEN** no notification appears and no widget changes

#### Scenario: Dialog is inside
- **WHEN** the user adds an item in the sheet opened from the widget
- **THEN** the sheet shows the item, and the widget itself shows nothing about it

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
When the front page, the Ruoka screen or the Lisää annos sheet (in the app or opened from the
widget or shortcut) opens or the app resumes on one of them, and the clipboard holds usable food
JSON that has not been imported or skipped, the app SHALL offer it. In the sheet (above the field)
and on the Ruoka screen (at the top of the day) the offer SHALL be a card: a title "Leikepöydällä"
with the kcal total the preview would show, then each item on its own line with name, amount,
treat sign and kcal (at most five lines, then "+<N> muuta"), then a filled primary button "Lisää
kaikki", an outlined "Muokkaa" and a ✕ button (Ohita). "Lisää kaikki" SHALL save the content as
the preview's Lisää would, to the day the sheet or Ruoka shows, without opening the preview, and
show "<N> annosta lisätty · Kumoa". "Muokkaa" SHALL open the preview. When the JSON holds only
products the card SHALL list the products with their energy and its button SHALL read "Tallenna
ruuat". On the front page the same card SHALL be shown above the cards, adding to today. The
offer SHALL be shown again on every open and resume while the same content is on the clipboard,
until the user saves that content (Lisää kaikki or the preview's Lisää) or taps Ohita or ✕; after
either, that content SHALL NOT be offered again, on any screen, also when it is copied again; Kumoa
of the import SHALL NOT bring it back. The example JSON that Kopioi tuontiohje puts on the
clipboard SHALL NOT be offered. Content counts as the same when its JSON is the same, whatever text
surrounds it. Content imported by sharing it to the app or by Tuo ruoka-annokset SHALL count as
imported too. Peruuta on the preview SHALL leave the content offered. Clipboard text without food
JSON SHALL show nothing. In the sheet opened from the widget or shortcut the clipboard SHALL be
read when its window gains focus. The app SHALL read the clipboard's text only when the clipboard
has changed since the app last read it (Android may then show its own "pasted" notice once per new
copy) and when the offer is used, and SHALL NOT read it anywhere else or in the background. The
offer SHALL NOT be shown on Ruoka without the food permission, before the day is read or in
Järjestä, nor in the front page's edit mode.

#### Scenario: Copied in the chat
- **GIVEN** the user copied the chat's answer with food JSON of 3 items
- **WHEN** they open the Lisää annos sheet
- **THEN** the card lists the 3 items and the total, the keyboard is down, and one tap on Lisää
  kaikki adds them to today with "3 annosta lisätty · Kumoa"

#### Scenario: Fix before adding
- **WHEN** the user taps Muokkaa on the card
- **THEN** the preview opens with the same items

#### Scenario: Offered on the front page
- **GIVEN** the user copied food JSON with 4 items
- **WHEN** they open the app on the front page
- **THEN** the card with the 4 items is shown above the cards, and Lisää kaikki adds them to today

#### Scenario: Still offered after looking elsewhere
- **GIVEN** the user copied food JSON and the front page already offered it
- **WHEN** they open the Lisää annos sheet from the widget
- **THEN** the sheet offers the same content

#### Scenario: Not offered after import
- **GIVEN** the user added the offered content with Lisää kaikki
- **WHEN** they open the Lisää annos sheet without copying anything new
- **THEN** no offer is shown

#### Scenario: Cancelled preview
- **WHEN** the user opens the preview from the offer and taps Peruuta
- **THEN** the offer is shown again on the next open

#### Scenario: Offered once
- **GIVEN** the user tapped ✕ on the card
- **WHEN** they open the Lisää annos sheet without copying anything new
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
- **THEN** the add field opens as a sheet over the home screen

#### Scenario: From the shortcut
- **WHEN** the user long-presses the app icon and taps Lisää annos
- **THEN** the same sheet opens over the home screen

### Requirement: Adding from the home screen
A tap on the widget or the shortcut SHALL open the add field (as in "Adding an item") as a sheet
over whatever is on screen, without the app's main screen behind it, adding to today, in the app's
theme. The sheet SHALL work as on Ruoka: suggestions, search, enter, the highlighted new row, and it SHALL stay open for the next item. Closing the sheet (back, a tap outside or swiping
it down) SHALL show what was behind it, normally the home screen.

#### Scenario: Breakfast from the home screen
- **GIVEN** on most mornings around 7 the user logged Rahka 200 g
- **WHEN** they tap the Lisää annos widget at 7.20 and tap Rahka
- **THEN** Rahka 200 g is added to today, the sheet stays open with the Rahka row highlighted, and closing it shows the home screen again

#### Scenario: Several items in a row
- **WHEN** the user types "kahvi", presses enter, then taps Pulla
- **THEN** both are added to today in that order, and the app's Ruoka screen shows them when opened

#### Scenario: Closing at once
- **WHEN** the user taps a suggestion and closes the sheet immediately
- **THEN** the item is still in Health Connect

#### Scenario: Opened again while open
- **WHEN** the user taps the widget again while the sheet is open
- **THEN** the sheet stays as it was

### Requirement: Sheet messages and loading
A failed write SHALL show "Tallennus epäonnistui" as a line at the top of the add panel and keep the sheet open. A delete SHALL show "<name> poistettu · Kumoa" in the same place;
closing the sheet drops it. With no message the line is left out. The field SHALL be usable at once, but the keyboard SHALL stay down
until the user taps the field. Until the day has been read the day list's place SHALL show one
quiet "Ladataan…" (onSurfaceVariant, bodyMedium), with no empty text, the chip area left out and
the header "<day>" alone; a failed read SHALL show its line there instead, with no chips, and the
day SHALL be read again when the sheet resumes and before the next write.

#### Scenario: Add before the day is read
- **WHEN** the user types a name and presses enter before the day has loaded
- **THEN** the item is added at the end of the day once it has loaded

#### Scenario: Failed write
- **WHEN** Health Connect refuses a write
- **THEN** the line "Tallennus epäonnistui" is shown and the sheet stays open

#### Scenario: Day cannot be read
- **WHEN** reading the day fails
- **THEN** a short line in the day list's place says so, typing still works, and an added item is
  placed at the end after the day has been read again

### Requirement: Sheet without permission
The sheet SHALL check the nutrition permission before showing the day and the add panel: while
checking it SHALL show only the sheet frame, without the keyboard. Without the permission it SHALL
show the explanation and a Salli button requesting it in the sheet, and show the day and the add
panel once granted.

#### Scenario: Permission missing
- **GIVEN** the nutrition permission is not granted
- **WHEN** the user taps the widget
- **THEN** the sheet explains why the permission is needed and offers Salli; once granted the day
  and the add panel are shown

### Requirement: Sheet with permission denied or Health Connect missing
When the permission has been denied the sheet SHALL show the same text and a button that opens
Health Connect's app permissions, and check again when the user returns. When Health Connect is
not available it SHALL show the existing text and a close button.

#### Scenario: Permission denied
- **GIVEN** the permission was denied
- **WHEN** the user taps the widget
- **THEN** the sheet offers a button to Health Connect's permissions and checks again on return

### Requirement: Sheet over midnight
The sheet SHALL add to the day it was opened on. After midnight its header SHALL name that day
relative to the new date ("Eilen · <total> kcal"), and no separate label is shown. A new open SHALL
use the new today.

#### Scenario: Open past midnight
- **GIVEN** the sheet was opened at 23.55
- **WHEN** the user adds an item at 00.05
- **THEN** the item goes to the earlier day and the header reads "Eilen · …"

### Requirement: Import from the home-screen sheet
"Tuo ruoka-annokset" and the card's "Muokkaa" in the sheet opened from the widget or the shortcut
SHALL finish the sheet and open the app on the import preview over the front page, as when the
estimate is shared to the app. Any sheet or editor open in the app SHALL close. After Lisää or
Peruuta the user SHALL stay in the app. The card's "Lisää kaikki" SHALL save in the sheet itself
and keep the sheet open.

#### Scenario: Import from the sheet
- **WHEN** the user taps Tuo ruoka-annokset in the sheet opened from the widget
- **THEN** the app opens on the import preview, as when the estimate is shared to the app

#### Scenario: After the import
- **WHEN** the user taps Lisää or Peruuta on the preview
- **THEN** the app stays open

#### Scenario: Offer in the sheet
- **GIVEN** the user copied food JSON with 4 items
- **WHEN** they open the sheet from the widget and tap Lisää kaikki
- **THEN** the 4 items are added to today, the sheet stays open over the home screen and shows
  "4 annosta lisätty · Kumoa"

### Requirement: Removing a product from the sheet
Each Omat ruuat row (shown while typing) SHALL end with a ⋮ button whose menu offers "Muokkaa" (the
editor for a new item, as above) and, for an own product only, "Poista" (in the theme's error
colour). Poista SHALL remove the product (as removing it under Omat ruuat in Asetukset does: logged
items keep their values) and show "<name> poistettu · Kumoa" in the sheet's line; Kumoa SHALL bring
the product back as it was. Base products SHALL have no Poista. Chips SHALL have no delete.

#### Scenario: Remove a one-off
- **WHEN** the user types "lumi" and taps ⋮ → Poista on Lumipallo
- **THEN** Lumipallo is gone from the list and the chips, earlier Lumipallo items stay on their days,
  and Kumoa brings it back

#### Scenario: Base product
- **WHEN** the user types "kaurapuu" and Kaurapuuro is a base product not yet used
- **THEN** its ⋮ menu offers Muokkaa but no Poista

### Requirement: Undoing Ohita
Tapping ✕ (Ohita) on the clipboard card SHALL show "Ohitettu · Kumoa"; Kumoa SHALL make the content
offered again, as if it had never been skipped.

#### Scenario: Skipped by mistake
- **WHEN** the user taps ✕ on the card and then Kumoa
- **THEN** the card is shown again with the same items

### Requirement: Item row buttons
Every logged item row in the add sheet's day list and on the Ruoka screen SHALL be one line: the
name with its herkku sign, the kcal ("ilman kcal" when missing), an ⓘ button and one ⋮ button, in
that order. The row keeps the 56 dp minimum height of the app's list rows.

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
it, and only close it: that tap or back opens no editor, presses nothing and does not close the
sheet. The bubble also closes when its item disappears; if the item changes while open, its lines
follow. The bubble has no buttons and changes nothing; opening or closing it SHALL leave the
sheet's field focus, keyboard and highlight as they were. Long-press on ⓘ does nothing extra.
TalkBack SHALL announce the bubble's title and lines once when it opens. ⓘ appears only on annos
rows (Ruoka and the sheet's day list), not on Omat ruuat or paino rows.

The ⋮ button (named "Toiminnot: <name>") opens a menu with "Muokkaa" (opens the item's editor) and
"Poista" (in the theme's error colour) that deletes the item at once and shows "<name> poistettu ·
Kumoa" (in the sheet in its line, on Ruoka as its Kumoa bar); Kumoa SHALL bring the item back in its
place. Poista SHALL act exactly as the editor's Poista (in the sheet it clears the highlight of that
item, replaces the line and keeps the keyboard as it was). On a narrow row the name SHALL be
shortened first; kcal, ⓘ and ⋮ never shrink. Tapping elsewhere on the row SHALL still open
the editor. Järjestä rows are one line too, with only their own move buttons (no ⓘ or ⋮).

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
- **GIVEN** the sheet's field has focus and the keyboard is up
- **WHEN** the user taps ⓘ on Tomaatti and then back
- **THEN** the bubble closes, the sheet stays open and the keyboard and field are as before

#### Scenario: Delete from the sheet
- **WHEN** the user taps ⋮ on Tomaatti in the sheet's day list and then Poista
- **THEN** Tomaatti is gone from the day, the header total drops, the line reads "Tomaatti poistettu
  · Kumoa", and Kumoa brings it back in its place

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
(in the add sheet and the Lisää annos dialog on the sheet's line, in place of the edited item
"Adding an item" shows there; the app bar on Ruoka and in
Omat ruuat) for as long as such bars last. Kumoa SHALL undo the whole save: the product's old
energy, the edited item as it was, and every recalculated item whose kcal is still amount × new
energy (an item changed again since stays). When the bar is gone Kumoa is gone. When writing the
recalculated items fails, the bar SHALL read "Päivitettiin <X>/<N> annosta · Yritä uudelleen";
a retry SHALL recalculate from the items as they are now.

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
