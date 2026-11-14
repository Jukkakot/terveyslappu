## ADDED Requirements

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
theme. The sheet SHALL work as on Ruoka: suggestions, search, enter, the "<name> · Muuta · Kumoa"
line, and it SHALL stay open for the next item. Closing the sheet (back, a tap outside or swiping
it down) SHALL show what was behind it, normally the home screen.

#### Scenario: Breakfast from the home screen
- **GIVEN** on most mornings around 7 the user logged Rahka 200 g
- **WHEN** they tap the Lisää annos widget at 7.20 and tap Rahka
- **THEN** Rahka 200 g is added to today, the sheet stays open with "Rahka 200 g · 130 kcal ·
  Muuta · Kumoa", and closing it shows the home screen again

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
A failed write SHALL show "Tallennus epäonnistui" as a line inside the sheet, in the place of the
"Muuta · Kumoa" line, and keep the sheet open. A delete SHALL show its Kumoa in the same place;
closing the sheet drops it. The field and keyboard SHALL be up at once; suggestions SHALL appear
when the day has been read, with a quiet "Ladataan…" until then and no empty hint.

#### Scenario: Add before the day is read
- **WHEN** the user types a name and presses enter before the day has loaded
- **THEN** the item is added at the end of the day once it has loaded

#### Scenario: Failed write
- **WHEN** Health Connect refuses a write
- **THEN** the line "Tallennus epäonnistui" is shown and the sheet stays open

#### Scenario: Day cannot be read
- **WHEN** reading the day fails
- **THEN** a short line says so, typing still works, and an added item is placed at the end after
  the day has been read again

### Requirement: Sheet without permission
The sheet SHALL check the nutrition permission before showing the field: while checking it SHALL
show only the sheet frame, without the keyboard. Without the permission it SHALL show the
explanation and a Salli button requesting it in the sheet, and open the field with the keyboard once
granted.

#### Scenario: Permission missing
- **GIVEN** the nutrition permission is not granted
- **WHEN** the user taps the widget
- **THEN** the sheet explains why the permission is needed and offers Salli; once granted the add
  field is shown with the keyboard

### Requirement: Sheet with permission denied or Health Connect missing
When the permission has been denied the sheet SHALL show the same text and a button that opens
Health Connect's app permissions, and check again when the user returns. When Health Connect is
not available it SHALL show the existing text and a close button.

#### Scenario: Permission denied
- **GIVEN** the permission was denied
- **WHEN** the user taps the widget
- **THEN** the sheet offers a button to Health Connect's permissions and checks again on return

### Requirement: Sheet over midnight
The sheet SHALL add to the day it was opened on. After midnight it SHALL show the existing label
"Lisätään päivälle <day>", and a new open SHALL use the new today.

#### Scenario: Open past midnight
- **GIVEN** the sheet was opened at 23.55
- **WHEN** the user adds an item at 00.05
- **THEN** the item goes to the earlier day and the label "Lisätään päivälle <day>" is shown

### Requirement: Import from the home-screen sheet
"Tuo ruoka-annokset" and the clipboard offer's Näytä in the sheet opened from the widget or the
shortcut SHALL finish the sheet and open the app on the import preview over the front page, as when
the estimate is shared to the app. Any sheet or editor open in the app SHALL close. After Lisää or
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
- **THEN** the line "Leikepöydällä ruoka-annoksia: 4" is shown above the suggestions, and Näytä
  opens the app on the preview

## MODIFIED Requirements

### Requirement: Adding an item
"Lisää annos" (on the front page and on the Ruoka screen) SHALL open an add field with the keyboard
up, adding to the day shown (today from the front page). With the field empty it SHALL show up to
eight suggestion buttons: the products most often logged within two hours of the current clock
time in the last 30 days, then the most often logged products. While typing it SHALL show up to
five matching products, own products before base products. Tapping a suggestion SHALL add that
product at once at the end of the day with its usual amount and show a line "<name> <amount> ·
<kcal> · Muuta · Kumoa"; Muuta SHALL open the item's editor, Kumoa SHALL take it back. Leaving the
editor by Tallenna, Peruuta or Poista SHALL return to the open add field, and the line SHALL then
show the edited item (none after Poista). Pressing enter SHALL add exactly the typed text: a bare
number as an item "Pikalisäys" with that many kcal, any other text as an item of that name (its
product's kcal when the name is a known product, otherwise without kcal). The field SHALL stay
open for the next item.

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

#### Scenario: Back to the field after editing
- **WHEN** the user taps Muuta, changes the amount and taps Tallenna
- **THEN** the add field is open again and the line shows the edited item

#### Scenario: Back to the field after cancelling or deleting
- **WHEN** the user taps Muuta and then Peruuta, or Poista
- **THEN** the add field is open again; after Poista no item line is shown

### Requirement: Clipboard offer
When the front page, the Ruoka screen or the Lisää annos sheet (in the app or opened from the
widget or shortcut) opens or the app resumes on one of them, and the clipboard holds text copied
after the last offer, the app SHALL read it once, and when it holds usable food JSON SHALL show a
line "Leikepöydällä ruoka-annoksia: <N> · Näytä · Ohita" (on the front page above the cards, with
the other notices; in the sheet above the suggestions). In the sheet opened from the widget or
shortcut the clipboard SHALL be read when its window first gains focus. Näytä SHALL open the
preview; Ohita SHALL hide the line. The same clipboard content SHALL be offered only once, whatever
the answer and on whichever screen, and clipboard text without food JSON SHALL show nothing. The
app SHALL NOT read the clipboard anywhere else or in the background.

#### Scenario: Copied in the chat
- **GIVEN** the user copied the chat's answer with food JSON
- **WHEN** they open the Ruoka screen
- **THEN** the line offers the 6 portions, and Näytä opens the preview

#### Scenario: Offered on the front page
- **GIVEN** the user copied food JSON with 4 items
- **WHEN** they open the app on the front page
- **THEN** the line "Leikepöydällä ruoka-annoksia: 4" is shown above the cards, and Näytä opens the preview

#### Scenario: Offered once
- **GIVEN** the user tapped Ohita on the offer
- **WHEN** they open the Lisää annos sheet without copying anything new
- **THEN** no offer is shown

#### Scenario: Other text
- **GIVEN** the clipboard holds a web address
- **WHEN** the Ruoka screen opens
- **THEN** no offer is shown

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
