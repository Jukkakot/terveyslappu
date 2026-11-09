## MODIFIED Requirements

### Requirement: Enter and save a weight
The app SHALL let the user enter a weight in kilograms with one decimal, plus a date and time that
default to the current moment, and save it to Health Connect as a weight record. The form SHALL be
a dialog opened from the main screen menu item "Lisää paino käsin"; the main screen itself SHALL
NOT show the form.

#### Scenario: Open the form
- **WHEN** the user opens menu ⋮ and taps Lisää paino käsin
- **THEN** a dialog shows the weight field, date, time and Tallenna, the date and time set to now

#### Scenario: Save a weight for now
- **WHEN** the user types 82,4 in the dialog and taps Tallenna without touching the date and time
- **THEN** a weight of 82.4 kg with the current date and time is written to Health Connect
- **AND** the dialog closes and the app confirms the save

#### Scenario: Save a weight for an earlier moment
- **WHEN** the user picks yesterday 07:30 in the dialog and saves 82,4
- **THEN** the weight is written with yesterday 07:30 as its time

#### Scenario: Cancel
- **WHEN** the user closes the dialog without saving
- **THEN** nothing is written

#### Scenario: Save fails
- **WHEN** writing to Health Connect fails
- **THEN** the dialog stays open with the typed weight and says the save failed
