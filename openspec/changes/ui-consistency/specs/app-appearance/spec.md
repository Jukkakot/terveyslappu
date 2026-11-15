## ADDED Requirements

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
content 16 dp from the screen's edge. A row that can be edited SHALL open its editor on tap; a
pencil button SHALL be shown only where a tap does something else.

#### Scenario: Same edge
- **WHEN** the user switches between Ruoka and Historia
- **THEN** the rows' text starts at the same distance from the edge and the rows are equally high
