## MODIFIED Requirements

### Requirement: Save the scale weight
The app SHALL write the decided weight to Health Connect as a weight measured by a scale, with the
time of the stable reading, and SHALL show the saved weight with the choices "Poista" and "Valmis"
for about ten seconds before the dialog closes; a tap on the dialog SHALL keep it open until the
user closes it. Once a weight is decided, the save and what follows it SHALL finish even when the
dialog is recreated (rotation, theme or language change) or closed, and a recreated dialog SHALL
show the same result instead of weighing again. While the weight is being saved, tapping outside
the dialog and Back SHALL not close it.

#### Scenario: Saved
- **WHEN** the stable weight is decided
- **THEN** it is written to Health Connect within a second
- **AND** the dialog shows the weight, "Tallennettu", "Poista" and "Valmis" and closes after about
  ten seconds

#### Scenario: Keep the result open
- **WHEN** the user taps the dialog while it shows the saved weight
- **THEN** it no longer closes by itself

#### Scenario: Done
- **WHEN** the user taps Valmis
- **THEN** the dialog closes at once

#### Scenario: Same weigh-in seen twice
- **WHEN** the same stable reading would be saved a second time in the same weigh-in
- **THEN** Health Connect keeps one weight, not two

#### Scenario: Turned while saving
- **GIVEN** the dialog is saving 82.5 kg
- **WHEN** the phone is turned and the dialog is recreated
- **THEN** 82.5 kg is saved once and the dialog shows it as saved, without scanning again

#### Scenario: Tap outside while saving
- **WHEN** the user taps outside the dialog or presses Back while it says it is saving
- **THEN** the dialog stays open until the weight is saved

#### Scenario: Save fails
- **WHEN** writing to Health Connect fails
- **THEN** the dialog stays open, says the save failed and offers to retry, and the cause is logged

### Requirement: Discard the weigh-in
From the saved result the app SHALL let the user throw the weigh-in away: "Poista" SHALL delete
the just-saved weight and its raw data without a confirmation and close the dialog at once. Once
Poista is tapped the dialog SHALL no longer close by itself, and the deletion SHALL finish even when
the dialog is closed or recreated meanwhile.

#### Scenario: Discard
- **GIVEN** the dialog shows 82.5 kg as saved
- **WHEN** the user taps Poista
- **THEN** 82.5 kg and its raw data are removed from the phone and the dialog closes

#### Scenario: Poista just before the auto-close
- **GIVEN** the saved result is about to close by itself
- **WHEN** the user taps Poista and deleting takes a moment
- **THEN** the dialog waits for the deletion instead of closing, and the weight is removed

#### Scenario: Discard fails
- **WHEN** deleting the saved weight fails
- **THEN** the dialog stays on the saved weight, says the deletion failed, and the cause is logged
