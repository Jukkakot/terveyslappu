# Spec Delta

## MODIFIED Requirements

### Requirement: Delete own entry
The app SHALL let the user delete a weight that this app wrote with a long-press or the row's
delete button, without a confirmation, and SHALL offer to undo the deletion; weights written by other apps SHALL NOT be
deletable here.

#### Scenario: Delete a test weight
- **WHEN** the user long-presses a weight written by this app or taps its delete button
- **THEN** the weight is removed from Health Connect and from the list
- **AND** a message "Paino poistettu" with Kumoa is shown

#### Scenario: Undo
- **WHEN** the user taps Kumoa
- **THEN** the same weight is written back at the same time, marked as from the scale when it was
  a scale weight, and appears in the list again

#### Scenario: Other app's weight
- **WHEN** the user long-presses a weight written by another app
- **THEN** nothing is offered for deletion and the row has no delete button
