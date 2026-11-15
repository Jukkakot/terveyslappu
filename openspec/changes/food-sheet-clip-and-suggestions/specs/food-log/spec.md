## MODIFIED Requirements

### Requirement: Adding an item
"Lisää annos" (on the front page and on the Ruoka screen) SHALL open an add field with the keyboard
up, adding to the day shown (today from the front page). With the field empty it SHALL show up to
eight suggestions as a list, one row per product: the products most often logged within two hours
of the current clock time in the last 30 days, then the most often logged products. Each row SHALL
show the product's name, its usual amount and the kcal that amount gives ("200 g · 130 kcal";
"ei kcal" when the energy is unknown, no amount when there is none) and the treat sign for a
Herkku product. While typing it SHALL show up to five matching products in the same row form, own
products before base products. Tapping a row SHALL add that product at once at the end of the day
with its usual amount and show a line "<name> <amount> · <kcal> · Muuta · Kumoa"; Muuta SHALL open
the item's editor, Kumoa SHALL take it back. Every row SHALL also have an edit button that opens an
editor for a new item of that product, filled with its usual amount, its kcal and its Herkku, with
Lisää and Peruuta: Lisää SHALL add the item as edited (and the product remembers the amount, as
after any add), show the same "Muuta · Kumoa" line and return to the open add field; Peruuta SHALL
return to the field and add nothing. Leaving the item editor by Tallenna, Peruuta or Poista SHALL
return to the open add field, and the line SHALL then show the edited item (none after Poista).
Pressing enter SHALL add exactly the typed text: a bare number as an item "Pikalisäys" with that
many kcal, any other text as an item of that name (its product's kcal when the name is a known
product, otherwise without kcal). The field SHALL stay open for the next item.

#### Scenario: Usual breakfast in one tap
- **GIVEN** on most mornings around 7 the user logged Rahka 200 g
- **WHEN** the user taps Lisää annos at 7.20
- **THEN** Rahka is among the first rows, reading "Rahka · 200 g · 130 kcal", and one tap logs
  Rahka 200 g with its kcal

#### Scenario: Set the amount before adding
- **GIVEN** a product Jauhelihakastike 140 kcal / 100 g, usually 250 g
- **WHEN** the user taps the edit button on its row, changes the amount to 350 g and taps Lisää
- **THEN** Jauhelihakastike 350 g, 490 kcal is added, the line shows it with Muuta and Kumoa, the
  add field is open again, and the next Jauhelihakastike starts at 350 g

#### Scenario: Edit and cancel
- **WHEN** the user taps the edit button on a row and then Peruuta
- **THEN** nothing is added and the add field is open again

#### Scenario: Edit a typed match
- **WHEN** the user types "kaurapuu" and taps the edit button on Kaurapuuro
- **THEN** the editor opens with Kaurapuuro's typical portion, and Lisää adds it as edited

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

#### Scenario: Back to the field after editing
- **WHEN** the user taps Muuta, changes the amount and taps Tallenna
- **THEN** the add field is open again and the line shows the edited item

#### Scenario: Back to the field after cancelling or deleting
- **WHEN** the user taps Muuta and then Peruuta, or Poista
- **THEN** the add field is open again; after Poista no item line is shown

### Requirement: Clipboard offer
When the front page, the Ruoka screen or the Lisää annos sheet (in the app or opened from the
widget or shortcut) opens or the app resumes on one of them, and the clipboard holds usable food
JSON that has not been imported or skipped, the app SHALL offer it. In the sheet and on the Ruoka
screen the offer SHALL be a full-width button under the add field (on Ruoka at the top of the
day), reading "Tuo leikepöydältä: <N> annosta · <kcal> kcal" (the kcal of the items that have it;
without the kcal part when none has; "<N> tuotetta" when the JSON holds only products), with a
small Ohita next to it. On the front page it SHALL be a notice above the cards with the same text
and Ohita. Tapping the offer SHALL open the import preview. The offer SHALL be shown again on every
open and resume while the same content is on the clipboard, until the user saves that content on
the preview (Lisää) or taps Ohita on its offer; after either, that content SHALL NOT be offered
again, on any screen, also when it is copied again. Content counts as the same when its JSON is the
same, whatever text surrounds it. Content imported by sharing it to the app or by Tuo
ruoka-annokset SHALL count as imported too. Peruuta on the preview SHALL leave the content offered.
Clipboard text without food JSON SHALL show nothing. In the sheet opened from the widget or
shortcut the clipboard SHALL be read when its window first gains focus. The app SHALL read the
clipboard's text only when the clipboard has changed since the app last read it, and SHALL NOT read
it anywhere else or in the background.

#### Scenario: Copied in the chat
- **GIVEN** the user copied the chat's answer with food JSON of 6 items
- **WHEN** they open the Ruoka screen
- **THEN** the button "Tuo leikepöydältä: 6 annosta · … kcal" is shown, and tapping it opens the
  preview

#### Scenario: Offered on the front page
- **GIVEN** the user copied food JSON with 4 items
- **WHEN** they open the app on the front page
- **THEN** the notice "Tuo leikepöydältä: 4 annosta · … kcal" is shown above the cards, and tapping
  it opens the preview

#### Scenario: Still offered after looking elsewhere
- **GIVEN** the user copied food JSON and the front page already offered it
- **WHEN** they open the Lisää annos sheet from the widget
- **THEN** the sheet offers the same content

#### Scenario: Not offered after import
- **GIVEN** the user saved the offered content on the preview
- **WHEN** they open the Lisää annos sheet without copying anything new
- **THEN** no offer is shown

#### Scenario: Cancelled preview
- **WHEN** the user opens the preview from the offer and taps Peruuta
- **THEN** the offer is shown again on the next open

#### Scenario: Offered once
- **GIVEN** the user tapped Ohita on the offer
- **WHEN** they open the Lisää annos sheet without copying anything new
- **THEN** no offer is shown

#### Scenario: Other text
- **GIVEN** the clipboard holds a web address
- **WHEN** the Ruoka screen opens
- **THEN** no offer is shown

### Requirement: Import from the home-screen sheet
"Tuo ruoka-annokset" and the clipboard offer in the sheet opened from the widget or the shortcut
SHALL finish the sheet and open the app on the import preview over the front page, as when the
estimate is shared to the app. Any sheet or editor open in the app SHALL close. After Lisää or
Peruuta the user SHALL stay in the app.

#### Scenario: Import from the sheet
- **WHEN** the user taps Tuo ruoka-annokset in the sheet opened from the widget
- **THEN** the app opens on the import preview, as when the estimate is shared to the app

#### Scenario: After the import
- **WHEN** the user taps Lisää or Peruuta on the preview
- **THEN** the app stays open

#### Scenario: Offer in the sheet
- **GIVEN** the user copied food JSON with 4 items
- **WHEN** they open the sheet from the widget
- **THEN** the button "Tuo leikepöydältä: 4 annosta · … kcal" is shown under the field, and
  tapping it opens the app on the preview
