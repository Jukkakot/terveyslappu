## MODIFIED Requirements

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

## ADDED Requirements

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
