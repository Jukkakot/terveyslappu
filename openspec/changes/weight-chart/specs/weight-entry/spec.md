## MODIFIED Requirements

### Requirement: Recent weights from Health Connect
The app SHALL show the weights this app wrote in the period chosen for the chart, read from Health
Connect, newest first, each with its date, time and value. Weights written by other apps SHALL NOT
be listed (user 2026-11-09: they are inaccurate hand entries).

#### Scenario: Weights from several apps
- **GIVEN** Health Connect holds weights written by this app and by another app
- **WHEN** the main screen is shown
- **THEN** only this app's weights appear in the list

#### Scenario: List follows the period
- **WHEN** the user chooses the period 1 kk
- **THEN** the list shows this app's weights of the last 30 days

#### Scenario: Refresh after saving
- **WHEN** a weight is saved
- **THEN** it appears at the top of the list without leaving the screen

#### Scenario: Nothing yet
- **WHEN** this app has no weights in the chosen period
- **THEN** the list area says that there are no weights in the period

### Requirement: Delete own entry
The app SHALL let the user delete a listed weight with a long-press or the row's delete button,
without a confirmation, and SHALL offer to undo the deletion.

#### Scenario: Delete a test weight
- **WHEN** the user long-presses a weight or taps its delete button
- **THEN** the weight is removed from Health Connect and from the list
- **AND** a message "Paino poistettu" with Kumoa is shown

#### Scenario: Undo
- **WHEN** the user taps Kumoa
- **THEN** the same weight is written back at the same time, marked as from the scale when it was
  a scale weight, and appears in the list again
