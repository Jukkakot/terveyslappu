## MODIFIED Requirements

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
