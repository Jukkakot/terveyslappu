# app-appearance Specification

## Purpose
TBD - created by archiving change main-screen-refresh. Update Purpose after archive.

## Requirements

### Requirement: Theme choice
Asetukset SHALL offer a theme choice with Järjestelmä, Vaalea and Tumma. Järjestelmä SHALL be the
default and follow the phone's light/dark setting; the colours SHALL come from the phone's own colour
palette in every choice. The choice SHALL be remembered and apply to every screen of the app (home,
Historia, Asetukset, the weigh-in dialog, manual entry).

#### Scenario: Default
- **GIVEN** the user has never chosen a theme and the phone is in dark mode
- **WHEN** the app opens
- **THEN** it is dark

#### Scenario: Choose light
- **WHEN** the user picks Vaalea while the phone is in dark mode
- **THEN** the app turns light at once and stays light after a restart

#### Scenario: Back to system
- **WHEN** the user picks Järjestelmä
- **THEN** the app follows the phone's setting again

### Requirement: Undo messages
Every message with Kumoa in the app (deleted item, added item, import, removed product, weights)
shown as a bar at the bottom SHALL show for about 4 seconds, SHALL go away at once when swiped
sideways, when another message comes or when the screen changes, and SHALL sit above the screen's
bottom buttons without covering them. While a sheet is open, its messages SHALL appear as the line
inside the sheet (which stays until the next action, as today) and no bar SHALL be drawn over the
sheet. Other bar messages keep their length but can be swiped away too.

#### Scenario: Gone in time
- **WHEN** the user deletes an item on Ruoka and does nothing
- **THEN** the "Kumoa" message is gone after about 4 seconds

#### Scenario: Swiped away
- **WHEN** the user swipes the message sideways
- **THEN** it disappears at once and nothing is undone

### Requirement: Delete and Kumoa
Every delete in the app (a weight, a food item, a product, an own chart, a row in the AI preview)
and skipping the clipboard offer SHALL happen at once, without a confirmation, and SHALL offer
Kumoa: the timed bar of "Undo messages", or the line inside an open sheet. Kumoa SHALL restore
exactly what was removed. Every delete control (a row's trash button, an editor's "Poista" or
"Poista tuote" / "Poista kaavio" button) SHALL be drawn in the theme's error colour.

#### Scenario: Chart deleted
- **WHEN** the user taps Poista kaavio in the chart editor
- **THEN** the chart is gone at once, without a question, and "Kaavio poistettu · Kumoa" is shown

#### Scenario: Red everywhere
- **WHEN** the user opens the item editor, the product editor or the Historia list
- **THEN** each delete control is red

### Requirement: Buttons
A screen's main action SHALL be a filled button, a secondary action beside it an outlined button,
and other actions text buttons. Dialogs SHALL use text buttons, with a delete action (red) on the
left and Peruuta before the confirming action (Tallenna, Lisää) on the right.

#### Scenario: Ruoka
- **WHEN** the Ruoka screen is shown
- **THEN** "Lisää annos" at the bottom is a filled button

#### Scenario: Editor order
- **WHEN** the item editor opens for a logged item
- **THEN** Poista is on the left in red, and Peruuta and Tallenna on the right in that order

### Requirement: List rows
Every list row (Historia's weights, Energia and Askeleet days, Ruoka's items, Ruokakirjaus's
products, the AI preview's rows, the sheet's suggestions) SHALL be at least 56 dp high, with its
content 16 dp from the screen's edge. A row whose thing can be edited SHALL show a pencil button
that opens its editor, and a row whose thing can be deleted SHALL show a red trash button that
deletes it (as "Delete and Kumoa"); the pencil SHALL come before the trash at the row's end. A row
that can be edited SHALL also open its editor on tap, except where a tap does something else (the
sheet's suggestions add at once). A row with nothing to edit or delete SHALL show neither.

#### Scenario: Same edge
- **WHEN** the user switches between Ruoka and Historia
- **THEN** the rows' text starts at the same distance from the edge and the rows are equally high

#### Scenario: Ruoka row
- **WHEN** the Ruoka screen lists Rahka 250 g
- **THEN** its row shows a pencil and a red trash; the pencil opens the item editor and the trash
  deletes it with "Rahka poistettu · Kumoa"

#### Scenario: Weight row
- **WHEN** Historia lists a weight
- **THEN** its row shows a red trash and no pencil

#### Scenario: Products
- **WHEN** the user opens Asetukset → Ruokakirjaus
- **THEN** each product row shows a pencil and a red trash
