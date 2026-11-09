## MODIFIED Requirements

### Requirement: Save the scale weight
The app SHALL write the decided weight to Health Connect as a weight measured by a scale, with the
time of the stable reading, and SHALL show the saved weight with the choices "Poista",
"Punnitse uudelleen" and "Valmis" for about ten seconds before the dialog closes; a tap on the
dialog SHALL keep it open until the user closes it.

#### Scenario: Saved
- **WHEN** the stable weight is decided
- **THEN** it is written to Health Connect within a second
- **AND** the dialog shows the weight, "Tallennettu", "Poista", "Punnitse uudelleen" and "Valmis"
  and closes after about ten seconds

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

## ADDED Requirements

### Requirement: Discard the weigh-in
From the saved result the app SHALL let the user throw the weigh-in away: "Poista" SHALL delete
the just-saved weight and its raw data without a confirmation and without scanning again, show
"Punnitus poistettu" with "Kumoa", and close the dialog after about five seconds.

#### Scenario: Discard
- **GIVEN** the dialog shows 82.5 kg as saved
- **WHEN** the user taps Poista
- **THEN** 82.5 kg and its raw data are removed, no scan starts, and the dialog shows
  "Punnitus poistettu" with Kumoa
- **AND** the dialog closes after about five seconds and the weight stays deleted

#### Scenario: Undo the discard
- **WHEN** the user taps Kumoa before the dialog closes
- **THEN** the same weight is written back at the same time as a scale weight with its raw data,
  and the dialog shows it as saved again

#### Scenario: Discard fails
- **WHEN** deleting the saved weight fails
- **THEN** the dialog stays on the saved weight, says the deletion failed, and the cause is logged
