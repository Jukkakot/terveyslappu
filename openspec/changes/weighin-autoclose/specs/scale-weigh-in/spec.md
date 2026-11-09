## MODIFIED Requirements

### Requirement: Weigh-in dialog
The app SHALL offer a small weigh-in dialog over whatever is on screen, which starts scanning at
once, tells the user to step on the scale and shows the live weight while it settles. From the start
of scanning the dialog SHALL show how many seconds are left before it closes, counting down from 30,
also while a changing weight is shown. When no weight has been saved when the count reaches zero,
the app SHALL stop scanning and close the dialog by itself, without a further message. When
Bluetooth is off, the dialog SHALL first guide the user to turn it on; the count starts only when
scanning starts.

#### Scenario: Step on the scale
- **WHEN** the dialog is open and the user steps on the scale
- **THEN** the changing weight is shown until the stable weight is saved

#### Scenario: Countdown
- **WHEN** the dialog has been scanning for 2 seconds
- **THEN** it shows 28 seconds left

#### Scenario: No scale heard
- **WHEN** no weight is saved within 30 seconds of scanning
- **THEN** scanning stops and the dialog closes by itself

#### Scenario: Bluetooth off
- **GIVEN** Bluetooth is off
- **WHEN** the dialog opens
- **THEN** it asks to turn Bluetooth on and shows no countdown until scanning starts

#### Scenario: Closed early
- **WHEN** the user closes the dialog before a weight is saved
- **THEN** scanning stops and nothing is saved
