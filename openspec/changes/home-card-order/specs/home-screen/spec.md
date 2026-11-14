## ADDED Requirements

### Requirement: Card order
The front page SHALL show its feature cards (Paino, Ruoka, Askeleet) in the user's chosen order,
weight, food, steps by default. This replaces the sentence "The order SHALL be fixed for now" of
"Simple home screen". The weight card SHALL always span the full width; the food and steps cards
SHALL be half width and SHALL share one row when they are next to each other in the order, and a
half-width card without a partner SHALL span the full width. Notices SHALL stay above the cards.
The order SHALL be remembered between app starts, SHALL NOT be part of the data file or the backup,
and SHALL be independent of the card periods. A stored order that misses a card SHALL get it at
the end in default order; a stored entry that names no card SHALL be ignored.

#### Scenario: Default order
- **GIVEN** the user never changed the order
- **THEN** the weight card is on top and the food and steps cards share the row under it

#### Scenario: Steps first
- **GIVEN** the order steps, food, weight
- **THEN** the steps card is left and the food card right in the first row, and the weight card is full width under them

#### Scenario: Partnerless card
- **GIVEN** the order food, weight, steps
- **THEN** the food card spans the full width, then the weight card, then the steps card across the full width

#### Scenario: Remembered
- **GIVEN** the user moved steps above weight
- **WHEN** the app is started again
- **THEN** the front page shows that order

#### Scenario: Unknown cards in the stored order
- **GIVEN** the stored order names an unknown card and lacks steps
- **THEN** the unknown name is ignored and the steps card is shown last

### Requirement: Card edit mode
A button in the front page's top bar SHALL switch edit mode on and off; the button SHALL show a
check mark and read "Valmis" for assistive tools while editing. In edit mode each feature card SHALL
show a drag handle and move up and move down buttons, and the cards SHALL be reordered by dragging
the handle or by the buttons (the first card has no move up, the last no move down); every
change SHALL be saved at once. While editing, taps on the cards and their quick actions (Punnitse,
+ Annos, period menus) SHALL do nothing, and notices SHALL stay. Back SHALL leave edit mode before
it does anything else. A "Palauta oletusjärjestys" action SHALL restore the default order. Edit
mode SHALL not survive leaving the front page.

#### Scenario: Drag a card
- **GIVEN** edit mode with the order weight, food, steps
- **WHEN** the user drags the steps card above the food card
- **THEN** the order is weight, steps, food and is saved

#### Scenario: Move with buttons
- **WHEN** the user taps move up on the steps card
- **THEN** it swaps places with the card above it and the cards stay in edit mode

#### Scenario: Cards inert while editing
- **GIVEN** edit mode
- **WHEN** the user taps the weight card or its Punnitse button
- **THEN** nothing opens and no weigh-in starts

#### Scenario: Leave with back
- **GIVEN** edit mode
- **WHEN** the user presses back
- **THEN** edit mode ends and the front page stays

#### Scenario: Restore default
- **WHEN** the user taps Palauta oletusjärjestys
- **THEN** the order is weight, food, steps
