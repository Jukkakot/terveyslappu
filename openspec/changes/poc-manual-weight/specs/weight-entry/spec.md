# Spec Delta

## Purpose

Lets the user save a body weight to Health Connect by hand and see the latest weights stored
there, so other health apps on the phone can show them.

## ADDED Requirements

### Requirement: Enter and save a weight
The app SHALL let the user enter a weight in kilograms with one decimal, plus a date and time that
default to the current moment, and save it to Health Connect as a weight record.

#### Scenario: Save a weight for now
- **WHEN** the user types 82,4 and taps Tallenna without touching the date and time
- **THEN** a weight of 82.4 kg with the current date and time is written to Health Connect
- **AND** the app confirms the save and clears the weight field

#### Scenario: Save a weight for an earlier moment
- **WHEN** the user picks yesterday 07:30 and saves 82,4
- **THEN** the weight is written with yesterday 07:30 as its time

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
The app SHALL show the weights of the last 30 days read from Health Connect, newest first, each
with its date, time, value and the name of the app that wrote it.

#### Scenario: Weights from several apps
- **GIVEN** Health Connect holds weights written by this app and by another app
- **WHEN** the main screen is shown
- **THEN** both appear in the list, each marked with its source app

#### Scenario: Refresh after saving
- **WHEN** a weight is saved
- **THEN** it appears at the top of the list without leaving the screen

#### Scenario: Nothing yet
- **WHEN** Health Connect has no weights for the last 30 days
- **THEN** the list area says that there are no weights yet

### Requirement: Delete own entry
The app SHALL let the user delete a weight that this app wrote, after a confirmation; weights
written by other apps SHALL NOT be deletable here.

#### Scenario: Delete a test weight
- **WHEN** the user long-presses a weight written by this app and confirms
- **THEN** the weight is removed from Health Connect and from the list

#### Scenario: Other app's weight
- **WHEN** the user long-presses a weight written by another app
- **THEN** nothing is offered for deletion

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
The app SHALL show its version name (build number and commit) on the main screen menu, so a
shared log and a report can be matched to a build.

#### Scenario: Check the version
- **WHEN** the user opens the menu
- **THEN** the version name is shown
