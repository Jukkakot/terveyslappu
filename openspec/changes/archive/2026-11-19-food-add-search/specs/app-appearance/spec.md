## MODIFIED Requirements

### Requirement: Undo messages
Every message with Kumoa in the app (deleted item, added item, import, removed product, weights)
shown as a bar at the bottom SHALL show for about 4 seconds, SHALL go away at once when swiped
sideways, when another message comes or when the screen changes, and SHALL sit above the screen's
bottom buttons without covering them. The add search shows its messages as the same timed bar at
its bottom, above the keyboard when it is up. Other bar messages keep their length but can be
swiped away too.

#### Scenario: Gone in time
- **WHEN** the user deletes an item on Ruoka and does nothing
- **THEN** the "Kumoa" message is gone after about 4 seconds

#### Scenario: Swiped away
- **WHEN** the user swipes the message sideways
- **THEN** it disappears at once and nothing is undone

### Requirement: Delete and Kumoa
Every delete in the app (a weight, a food item, a product, an own chart, a row in the AI preview)
and skipping the clipboard offer SHALL happen at once, without a confirmation, and SHALL offer
Kumoa: the timed bar of "Undo messages", also in the add search. Kumoa SHALL restore
exactly what was removed. Every delete control (a row menu's "Poista", an editor's "Poista" or
"Poista ruoka" / "Poista kaavio" button) SHALL be drawn in the theme's error colour; no list row
SHALL show a red button of its own.

#### Scenario: Chart deleted
- **WHEN** the user taps Poista kaavio in the chart editor
- **THEN** the chart is gone at once, without a question, and "Kaavio poistettu · Kumoa" is shown

#### Scenario: Red everywhere
- **WHEN** the user opens the Historia list, Ruoka or Omat ruuat
- **THEN** no row shows a red control; the ⋮ menu's Poista and the editors' delete buttons are red

### Requirement: List rows
Every list row (Historia's weights, Syöty, Askeleet and Kulutus days, Ruoka's items, Omat ruuat's
foods, the AI preview's rows and the add search's ruoka rows) SHALL be at least 56 dp
high, with its content 16 dp from the screen's edge. A row whose thing can be edited or deleted
SHALL end with one ⋮ button (44 dp touch target, named "Toiminnot: <name>" for TalkBack) that opens
a menu next to it with "Muokkaa" (when it can be edited) and "Poista" (when it can be deleted, in
the error colour, acting as "Delete and Kumoa"), in that order, plus any action the row already has (a questioned weight's menu: Hyväksy, then Poista; its tap
still opens its dialog). The AI preview's item and product rows both offer Muokkaa and Poista.
Controls that are not list rows keep their look (Asetukset's Garmin "Poista" text button, the
own chart tab's edit button). A row that can be edited SHALL also open its editor on tap,
except where a tap does something else (the add search's ruoka rows add at once). A row with nothing to
edit or delete SHALL show no ⋮ button, and its values SHALL end at the same edge as the rows that
have one.

#### Scenario: Same edge
- **WHEN** the user switches between Ruoka and Historia
- **THEN** the rows' text starts at the same distance from the edge and the rows are equally high

#### Scenario: Ruoka row
- **WHEN** the Ruoka screen lists Rahka 250 g and the user taps its ⋮
- **THEN** a menu offers Muokkaa and a red Poista; Poista deletes it with "Rahka poistettu · Kumoa"

#### Scenario: Weight row
- **WHEN** Historia lists a weight and the user taps its ⋮
- **THEN** the menu offers only a red Poista

#### Scenario: Day row
- **WHEN** Historia's Syöty list shows a day or a week
- **THEN** the row has no ⋮ button

#### Scenario: Products
- **WHEN** the user opens Asetukset → Omat ruuat and taps a product's ⋮
- **THEN** the menu offers Muokkaa and a red Poista
