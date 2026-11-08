# weight-export Specification

## Purpose
Gives the user their weights from Health Connect as a readable CSV file they keep themselves and
can import again on another phone.

## Requirements

### Requirement: Export to a chosen file
The main screen menu SHALL offer "Vie painot", which asks where to save a file named
`terveyslappu-painot-<date>.csv` with the system picker and writes the export there.

#### Scenario: Export
- **WHEN** the user taps Vie painot and picks a folder
- **THEN** the file is written and the app says how many weights it holds

#### Scenario: Picker cancelled
- **WHEN** the user closes the picker
- **THEN** nothing is written

### Requirement: File content
The file SHALL hold every weight in Health Connect from every app, oldest first, one row each with
date (`yyyy-MM-dd`), time (`HH:mm:ss`, the record's own offset), weight in kg with a comma decimal
and the writing app's name, separated by semicolons under a header row.

#### Scenario: Row format
- **GIVEN** an 82.4 kg weight at 1 March 2024 07:12 written by this app
- **THEN** the row is `2024-03-01;07:12:00;82,4;Terveyslappu`

#### Scenario: Read back
- **WHEN** an exported file is imported with Tuo historia
- **THEN** the same weights and times are found

### Requirement: Whole history
The app SHALL ask for Health Connect's history permission before the first export when the phone
supports it, and SHALL say in the result when only the last 30 days could be read.

#### Scenario: History permission granted
- **WHEN** the user grants the history permission
- **THEN** the export includes weights older than 30 days

#### Scenario: Not granted or not supported
- **WHEN** the history permission is denied or not available
- **THEN** the export still runs and the result says older weights were not included

### Requirement: Export failures
A failed read or write SHALL leave a message for the user and the cause in the log.

#### Scenario: Write fails
- **WHEN** the chosen location cannot be written
- **THEN** the app says the export failed and logs the cause
