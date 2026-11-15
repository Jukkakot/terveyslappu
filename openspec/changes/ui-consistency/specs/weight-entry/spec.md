## MODIFIED Requirements

### Requirement: Delete own entry
The app SHALL let the user delete a listed weight with the row's delete button (red), without a
confirmation, and SHALL offer to undo the deletion. A long-press on a row SHALL NOT delete it.

#### Scenario: Delete a test weight
- **WHEN** the user taps a weight's delete button
- **THEN** the weight is removed from Health Connect and from the list
- **AND** a message "Paino poistettu" with Kumoa is shown

#### Scenario: Long-press does nothing
- **WHEN** the user long-presses a weight row
- **THEN** the weight stays

#### Scenario: Undo
- **WHEN** the user taps Kumoa
- **THEN** the same weight is written back at the same time, marked as from the scale when it was
  a scale weight, and appears in the list again

#### Scenario: Other app's weight
- **GIVEN** another app wrote a weight
- **THEN** it is not listed, so the app offers no way to delete it
