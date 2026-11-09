# weight-entry Specification

## Purpose
Lets the user save a body weight to Health Connect by hand and see the latest weights stored
there, so other health apps on the phone can show them.

## Requirements

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

### Requirement: Reject impossible weights
The app SHALL accept weights from 20,0 to 300,0 kg, with a comma or a dot as the decimal mark, and
SHALL NOT save anything else.

#### Scenario: Out of range
- **WHEN** the user enters 7 or 450
- **THEN** the save button stays disabled and the field tells the allowed range

#### Scenario: Dot or comma
- **WHEN** the user enters 82.4 or 82,4
- **THEN** both are read as 82.4 kg

#### Scenario: Future time
- **WHEN** the chosen date and time are later than now
- **THEN** the app does not save and says the time cannot be in the future

### Requirement: Recent weights from Health Connect
The app SHALL show on Historia the weights this app wrote in the period chosen for the chart, read
from Health Connect, newest first, each with its date, time and value; the date SHALL include the
year when the weight is not from the current year. Weights written by other apps SHALL NOT be listed
(user 2026-11-09: they are inaccurate hand entries).

#### Scenario: Weights from several apps
- **GIVEN** Health Connect holds weights written by this app and by another app
- **WHEN** Historia is shown
- **THEN** only this app's weights appear in the list

#### Scenario: List follows the period
- **WHEN** the user chooses the period 1 kk
- **THEN** the list shows this app's weights of the last 30 days

#### Scenario: Old weight
- **WHEN** the list shows a weight from 15.9.2024 at 8.33
- **THEN** its row reads "Su 15.9.2024 8.33"

#### Scenario: Refresh after saving
- **WHEN** a weight is saved
- **THEN** it appears at the top of the list and on the home screen without leaving the screen

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

#### Scenario: Other app's weight
- **GIVEN** another app wrote a weight
- **THEN** it is not listed, so the app offers no way to delete it

### Requirement: Health Connect access
The app SHALL ask for permission to read and write weight the first time it needs Health Connect,
and SHALL explain what is missing when access is denied or Health Connect is not available.

#### Scenario: First start
- **WHEN** the app starts without weight permissions
- **THEN** it shows a short explanation and a button that opens the Health Connect permission
  request for reading and writing weight

#### Scenario: Permission denied
- **WHEN** the user has denied the permissions
- **THEN** the screen says saving needs the permission and offers a button to Health Connect
  settings, and the weight form is disabled

#### Scenario: Health Connect unavailable
- **WHEN** Health Connect is not available on the phone or needs an update
- **THEN** the screen says so and offers to open the store page or settings where it is fixed

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
