## MODIFIED Requirements

### Requirement: Enter and save a weight
The app SHALL let the user enter a weight in kilograms with one decimal, plus a date and time that
default to the current moment, and save it to Health Connect as a weight record. The form SHALL be
a dialog opened from the add button ("Lisää paino käsin") in the Historia top bar; no other screen
shows the form.

#### Scenario: Open the form
- **WHEN** the user taps the add button on Historia
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

### Requirement: Recent weights from Health Connect
The app SHALL show on Historia the weights this app wrote in the period chosen for the chart, read
from Health Connect, newest first, each with its date, time and value. Weights written by other apps
SHALL NOT be listed (user 2026-11-09: they are inaccurate hand entries).

#### Scenario: Weights from several apps
- **GIVEN** Health Connect holds weights written by this app and by another app
- **WHEN** Historia is shown
- **THEN** only this app's weights appear in the list

#### Scenario: List follows the period
- **WHEN** the user chooses the period 1 kk
- **THEN** the list shows this app's weights of the last 30 days

#### Scenario: Refresh after saving
- **WHEN** a weight is saved
- **THEN** it appears at the top of the list and on the home screen without leaving the screen

#### Scenario: Nothing yet
- **WHEN** this app has no weights in the chosen period
- **THEN** the list area says that there are no weights in the period

### Requirement: Version visible
The app SHALL show its version in Asetukset as `v<major>.<minor>.<build>`, where the build number
grows by one with every commit that changes the app and never resets, so a shared log and a report
can be matched to a build.

#### Scenario: Check the version
- **WHEN** the user opens Asetukset
- **THEN** the version is shown, e.g. "v1.0.24"

#### Scenario: Docs-only commit
- **WHEN** a commit changes only specs or documentation
- **THEN** the next build's number is the same as without that commit
