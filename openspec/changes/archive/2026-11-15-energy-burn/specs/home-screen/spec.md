# Spec Delta

## MODIFIED Requirements

### Requirement: Card grid
The front page SHALL place its cards (Paino, Ruoka, Askeleet, Kulutus) in a two-column grid, filled row by
row in the user's order. Each card SHALL have its own size: small (half width, one row high), wide
(full width, one row high) or big (full width, two rows high). There SHALL be no narrow-tall size.
Two small cards that follow each other in the order SHALL share a row, the first one on the left; a
small card followed by a wide or big card, or last in the order, SHALL stay half width on the left
and the right half of its row SHALL stay empty. The grid SHALL NOT move a later card forward to fill
such a gap. All cards of one row SHALL have the same height, and a big card SHALL be as high as two
rows and the gap between them. The default layout SHALL be weight wide, food small, steps small
(weight on top, food and steps side by side under it). Notices (backup, "?" check, clipboard offer)
SHALL stay above the grid and SHALL NOT be part of the order. The order, the size of every card and
the hidden cards SHALL be remembered between app starts, SHALL be independent of the card periods,
and SHALL NOT be part of the data file or the backup file. A remembered layout that cannot be read
SHALL mean the default layout; a card the app does not know SHALL be ignored, and a card the stored
layout lacks (added in a later version) SHALL be shown last at its default size, except Kulutus,
which SHALL be hidden by default (in the default layout too) and is added from "Lisää kortti".

#### Scenario: Default layout
- **GIVEN** the user never edited the front page
- **THEN** the weight card spans the width on top and the food and steps cards share the row under it

#### Scenario: Two small cards pair
- **GIVEN** the order steps (small), food (small), weight (wide)
- **THEN** steps is left and food right in the first row, and weight spans the second row

#### Scenario: Lone small card
- **GIVEN** the order food (small), weight (wide), steps (small)
- **THEN** food sits in the left half of the first row with the right half empty, weight spans the
  second row, and steps sits in the left half of the third row

#### Scenario: Big card
- **GIVEN** weight is big and food and steps are small
- **THEN** the weight card spans the width and is as high as two rows, and the food and steps cards
  share the row under it

#### Scenario: Remembered
- **GIVEN** the user made steps big, moved it first and saved
- **WHEN** the app is started again
- **THEN** the front page shows steps big on top

#### Scenario: Unusable stored layout
- **GIVEN** the stored layout is empty or unreadable
- **THEN** the default layout is shown

#### Scenario: Unknown and later-added cards
- **GIVEN** the stored layout names an unknown card and lacks a card added in a later version
- **THEN** the unknown card is ignored and the added card is shown last at its default size

#### Scenario: Larger layouts scroll
- **GIVEN** all three cards big
- **THEN** the front page scrolls to show them

#### Scenario: Kulutus hidden by default
- **GIVEN** a layout stored before the Kulutus card existed
- **THEN** the front page looks as before and Lisää kortti offers Kulutus
