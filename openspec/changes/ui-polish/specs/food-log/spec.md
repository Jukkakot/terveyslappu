## MODIFIED Requirements

### Requirement: Editing, order and delete
Tapping a logged item SHALL open its editor with clear labelled fields: amount (with its unit),
kcal, Herkku, and Poista. Herkku belongs to the product: switching it SHALL change the product
and all its logged items, also earlier ones, and the totals and the chart follow. Delete SHALL happen at once with a Kumoa line. The Ruoka
screen SHALL offer "Järjestä", where items are moved by dragging; the order SHALL be saved when
leaving it. Järjestä SHALL be started from a ⋮ button ("Lisää toimintoja") at the end of Ruoka's
top bar, whose menu item reads "Järjestä annokset"; there SHALL be no unlabelled icon for it. While
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
- **GIVEN** a 360 dp phone at 200 % text size
- **WHEN** Ruoka is shown
- **THEN** back, the title Ruoka, Viikko and the ⋮ button are all visible and none is cut off

## ADDED Requirements

### Requirement: Recent amounts in the editor
The item editor, also when an annos is edited before adding, SHALL show under the amount field up
to three chips ("200 g", "1,5 kpl"): the distinct amounts of the ruoka's other annokset in the chosen
kpl or g, most recently logged first. A tap SHALL put the amount in the field, saving nothing; the
chip equal to the field SHALL show as chosen. With Yhteensä or no such amounts, no chips row.

#### Scenario: Pick an earlier amount
- **GIVEN** Rahka was logged as 250 g yesterday, 200 g two days ago and 250 g three days ago, and
  today's Rahka annos is 150 g
- **WHEN** the user opens today's Rahka in the editor
- **THEN** chips "250 g" and "200 g" are shown in that order, and tapping "200 g" sets the amount
  to 200 and the result line to 200 g's kcal

#### Scenario: Unit follows the choice
- **GIVEN** Pulla was logged as 1 kpl and as 80 g
- **WHEN** the editor of a Pulla annos is on kpl and the user switches it to g
- **THEN** the chip "1 kpl" is replaced by "80 g"

#### Scenario: Nothing to offer
- **GIVEN** a ruoka logged only once
- **WHEN** its annos is opened in the editor
- **THEN** the editor shows no chips row

#### Scenario: At most three
- **GIVEN** Kaurapuuro was logged with five different amounts
- **WHEN** its annos is opened in the editor
- **THEN** the three most recently logged distinct amounts are shown
