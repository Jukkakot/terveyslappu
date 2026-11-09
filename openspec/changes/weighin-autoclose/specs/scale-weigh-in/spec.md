## MODIFIED Requirements

### Requirement: Weigh-in dialog
The app SHALL offer a small weigh-in dialog over whatever is on screen, which starts scanning at
once, tells the user to step on the scale and shows the live weight while it settles. When no weight
has been saved 30 seconds after scanning started, the app SHALL stop scanning, close the dialog by
itself and show a short toast saying the weigh-in was stopped because no weight came.

#### Scenario: Step on the scale
- **WHEN** the dialog is open and the user steps on the scale
- **THEN** the changing weight is shown until the stable weight is saved

#### Scenario: No scale heard
- **WHEN** no weight is saved within 30 seconds of opening the dialog
- **THEN** scanning stops, the dialog closes by itself and a toast says no weight came

#### Scenario: Closed early
- **WHEN** the user closes the dialog before a weight is saved
- **THEN** scanning stops and nothing is saved
