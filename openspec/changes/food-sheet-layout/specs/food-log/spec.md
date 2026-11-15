## MODIFIED Requirements

### Requirement: Adding an item
"Lisää annos" (on the front page and on the Ruoka screen) SHALL open the add sheet, adding to the
day shown (today from the front page). From the top the sheet SHALL show: a header "Lisää annos ·
<day>" (tänään, eilen or the date) with a ⋮ menu holding Kopioi tuontiohje; the clipboard card
when it is offered ("Clipboard offer"); the field with a search icon and the hint "Hae ruokaa tai
kirjoita kcal"; the list; and at the bottom a quiet text button "Tuo ruoka-annokset". The keyboard
SHALL rise at once, except while the clipboard card is shown (then it rises when the field is
tapped). With the field empty the list SHALL be headed "Usein tähän aikaan" and show up to eight
rows, one per product: the products most often logged within two hours of the current clock time
in the last 30 days, then the most often logged products. Each row SHALL show the product's name
and under it its usual amount and the kcal that amount gives ("200 g · 130 kcal"; with no usual
amount the energy, "140 kcal / 100 g"; "200 g · ei kcal" or "ei kcal" when the energy is
unknown), and the treat sign for a Herkku product. While typing the list SHALL be headed
"Tuotteet" and show up to five matching products in the same row form, own products before base
products, and as the last row "Lisää ”<text>” uutena" with a line telling what it adds (for a
bare number "Pikalisäys <N> kcal"; for other text "ilman kcal" or the known product's kcal);
tapping it SHALL do what Enter does. Tapping a product row SHALL add that product at once at the
end of the day with its usual amount and show a line "<name> <amount> · <kcal> · Muuta · Kumoa";
Muuta SHALL open the item's editor, Kumoa SHALL take it back. Every product row SHALL also have an
edit button that opens an editor for a new item of that product: the same editor and rules as Muuta
(name, amount and unit, energy, Herkku; changes reach the product as after Tallenna), filled as a
tap would add it, with Lisää and Peruuta instead of Tallenna and Poista: Lisää SHALL add the item
as edited, show the same "Muuta · Kumoa" line and return to the open add field; Peruuta SHALL
return to the field and add nothing (as does closing the editor). Leaving the item editor by
Tallenna, Peruuta or Poista SHALL return to the open add field, and the line SHALL then show the
edited item (none after Poista). Pressing enter SHALL add exactly the typed text: a bare number as
an item "Pikalisäys" with that many kcal, any other text as an item of that name (its product's
kcal when the name is a known product, otherwise without kcal). The field SHALL stay open for the
next item.

#### Scenario: Usual breakfast in one tap
- **GIVEN** on most mornings around 7 the user logged Rahka 200 g
- **WHEN** the user taps Lisää annos at 7.20
- **THEN** under "Usein tähän aikaan" Rahka is among the first rows, reading "Rahka · 200 g · 130
  kcal", and one tap logs Rahka 200 g with its kcal

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

#### Scenario: What enter does
- **WHEN** the user types "kana"
- **THEN** the last row reads "Lisää ”kana” uutena" and tapping it adds what Enter would

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
JSON that has not been imported or skipped, the app SHALL offer it. In the sheet (above the field)
and on the Ruoka screen (at the top of the day) the offer SHALL be a card: a title "Leikepöydällä"
with the kcal total the preview would show, then each item on its own line with name, amount,
treat sign and kcal (at most five lines, then "+<N> muuta"), then a filled primary button "Lisää
kaikki", an outlined "Muokkaa" and a ✕ button (Ohita). "Lisää kaikki" SHALL save the content as
the preview's Lisää would, to the day the sheet or Ruoka shows, without opening the preview, and
show "<N> annosta lisätty · Kumoa". "Muokkaa" SHALL open the preview. When the JSON holds only
products the card SHALL list the products with their energy and its button SHALL read "Tallenna
tuotteet". On the front page the offer SHALL be a notice above the cards reading "Tuo
leikepöydältä: <N> annosta · <kcal> kcal" ("1 annos"; without the kcal part when no item has kcal;
"<N> tuotetta" / "1 tuote" for products only) with Ohita; tapping it SHALL open the preview. The
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
- **THEN** the notice "Tuo leikepöydältä: 4 annosta · … kcal" is shown above the cards, and tapping
  it opens the preview

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

## ADDED Requirements

### Requirement: Removing a product from the sheet
Each row of an own product in the sheet's list SHALL have a red delete button next to the edit
button. It SHALL remove the product (as removing it under Ruokakirjaus in Asetukset does: logged
items keep their values) and show "<name> poistettu · Kumoa" in the sheet's line; Kumoa SHALL
bring the product back as it was. Base products SHALL have no delete button.

#### Scenario: Remove a one-off
- **WHEN** the user taps the delete button on Lumipallo
- **THEN** Lumipallo is gone from the list, earlier Lumipallo items stay on their days, and Kumoa
  brings it back

#### Scenario: Base product
- **WHEN** the user types "kaurapuu" and Kaurapuuro is a base product not yet used
- **THEN** its row has the edit button but no delete button
