## MODIFIED Requirements

### Requirement: Save the scale weight
The app SHALL write the decided weight to Health Connect as a weight measured by a scale, with the
time of the stable reading, and SHALL show the saved weight with the choices "Punnitse uudelleen"
and "Valmis" for about ten seconds before the dialog closes; a tap on the dialog SHALL keep it
open until the user closes it.

#### Scenario: Saved
- **WHEN** the stable weight is decided
- **THEN** it is written to Health Connect within a second
- **AND** the dialog shows the weight, "Tallennettu", "Punnitse uudelleen" and "Valmis" and closes
  after about ten seconds

#### Scenario: Keep the result open
- **WHEN** the user taps the dialog while it shows the saved weight
- **THEN** it no longer closes by itself

#### Scenario: Done
- **WHEN** the user taps Valmis
- **THEN** the dialog closes at once

#### Scenario: Same weigh-in seen twice
- **WHEN** the same stable reading would be saved a second time in the same weigh-in
- **THEN** Health Connect keeps one weight, not two

#### Scenario: Save fails
- **WHEN** writing to Health Connect fails
- **THEN** the dialog stays open, says the save failed and offers to retry, and the cause is logged

### Requirement: One-tap start
The weigh-in dialog SHALL open with one tap from a Quick Settings tile, a home-screen widget, a
launcher shortcut on the app icon and a button on the main screen. The widget and the tile SHALL
show only the Punnitse action, never a weight; the widget SHALL fit one home-screen cell.

#### Scenario: From the tile
- **WHEN** the user taps the Punnitse tile in Quick Settings
- **THEN** the panel collapses and the weigh-in dialog opens without the main screen

#### Scenario: From the widget
- **WHEN** the user taps the Punnitse widget on the home screen
- **THEN** the weigh-in dialog opens without the main screen

#### Scenario: No weight before weighing
- **GIVEN** weights have been saved earlier
- **WHEN** the user looks at the widget or the tile
- **THEN** neither shows a weight or a day, only Punnitse

#### Scenario: From the main screen
- **WHEN** the user taps Punnitse on the main screen
- **THEN** the weigh-in dialog opens and the list shows the new weight after it closes

## ADDED Requirements

### Requirement: Weigh again
From the saved result the app SHALL let the user weigh again: "Punnitse uudelleen" SHALL delete the
just-saved weight from Health Connect without a confirmation and start a new scan, and until a new
weight is saved the dialog SHALL say the previous weight was deleted and offer "Kumoa".

#### Scenario: Weigh again
- **GIVEN** the dialog shows 82.5 kg as saved
- **WHEN** the user taps Punnitse uudelleen
- **THEN** 82.5 kg is removed from Health Connect and the dialog asks the user to step on the scale
- **AND** it shows "Edellinen poistettu" with Kumoa

#### Scenario: Undo
- **WHEN** the user taps Kumoa while the new scan runs or after it failed
- **THEN** scanning stops, the same weight is written back at the same time as a scale weight, and
  the dialog shows it as saved again

#### Scenario: New weight saved
- **WHEN** the new scan saves 82.1 kg
- **THEN** the dialog shows 82.1 kg as saved and no longer offers Kumoa

#### Scenario: Closed without a new weight
- **WHEN** the user closes the dialog after Punnitse uudelleen and before a new weight is saved
- **THEN** the deleted weight stays deleted

#### Scenario: Delete fails
- **WHEN** deleting the saved weight fails
- **THEN** the dialog stays on the saved weight, says the deletion failed, and the cause is logged

## REMOVED Requirements

### Requirement: Latest weight on the widget and tile
**Reason**: The user does not want to see the previous weight before weighing.
**Migration**: The widget and tile show only Punnitse; the latest weight is on the main screen and
in the weigh-in result.
