## ADDED Requirements

### Requirement: Card order
The front page SHALL show its feature cards (Paino, Ruoka, Askeleet) in the user's chosen order,
weight, food, steps by default. Each card has a width: weight is full width, food and steps are
half width. Half cards SHALL pair left to right in the order, and a half card left without a
partner SHALL span the full width and use its long forms. Notices SHALL stay above the cards. The
order SHALL be remembered, SHALL NOT be part of the data file or the backup, and SHALL be
independent of the card periods.

#### Scenario: Default order
- **GIVEN** the user never changed the order
- **THEN** the weight card is on top and the food and steps cards share the row under it

#### Scenario: Steps first
- **GIVEN** the order steps, food, weight
- **THEN** the steps card is left and the food card right in the first row, and the weight card is full width under them

#### Scenario: Partnerless card
- **GIVEN** the order food, weight, steps
- **THEN** the food card, the weight card and the steps card each span the full width, and the food and steps cards show their long forms

#### Scenario: Remembered
- **GIVEN** the user moved steps above weight
- **WHEN** the app is started again
- **THEN** the front page shows that order

#### Scenario: Unusable stored order
- **GIVEN** the stored order is empty or unreadable
- **THEN** the default order is shown

#### Scenario: Unknown and missing cards
- **GIVEN** the stored order names an unknown card and lacks a card added later
- **THEN** the unknown name is ignored and the missing card is shown last, in default order

### Requirement: Card edit mode
A sort button in the front page's top bar (reads "Järjestä kortit") SHALL start edit mode. In
edit mode the three cards SHALL be shown as a full-width list of compact rows in pure order, with
no pairing, each with the card's title, move up and move down buttons and a drag handle. A visible
"Valmis" text button SHALL leave edit mode. Every change SHALL be saved at once. Notices SHALL
stay visible and tappable. A "Palauta oletusjärjestys" action SHALL restore the default order and
be disabled when the order already is the default. Back SHALL leave edit mode first. Edit mode
SHALL survive rotation and SHALL end when the screen changes or the app goes to background.

#### Scenario: Compact rows
- **GIVEN** the order food, weight, steps
- **WHEN** the user starts edit mode
- **THEN** three full-width rows are listed in that order, one card per row

#### Scenario: Drag a row
- **GIVEN** edit mode with the order weight, food, steps
- **WHEN** the user drags the steps row above the food row and drops it
- **THEN** the order is weight, steps, food and is saved and logged once

#### Scenario: Move with buttons
- **WHEN** the user taps move up on the steps row
- **THEN** it swaps places with the row above it, the order is saved and logged at once, and edit mode stays (the first row has no move up, the last no move down)

#### Scenario: Announced position
- **GIVEN** a screen reader
- **THEN** each row reads "<card>, <position>/<count>" and after a move the new position is announced

#### Scenario: Notices stay
- **GIVEN** a backup notice and edit mode
- **WHEN** the user taps the notice
- **THEN** it acts as outside edit mode

#### Scenario: Leave
- **GIVEN** edit mode
- **WHEN** the user taps "Valmis" or presses back
- **THEN** the real cards are shown in the new order and the front page stays

#### Scenario: Rotation and leaving
- **GIVEN** edit mode
- **WHEN** the device is rotated
- **THEN** edit mode stays; when the app goes to background or another screen opens it ends

#### Scenario: Restore default
- **GIVEN** a changed order
- **WHEN** the user taps Palauta oletusjärjestys
- **THEN** the order is weight, food, steps, and the action is disabled
