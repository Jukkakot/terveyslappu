# weight-export Specification

## Purpose
Gives the user their weights from Health Connect as a readable CSV file they keep themselves and
can import again on another phone.

## Requirements

### Requirement: Export to a chosen file
Asetukset SHALL offer "Vie tiedot", which first asks for the format, CSV or JSON, then asks where to
save a file named `terveyslappu-export-<yyyyMMdd-HHmmss-SSS>.csv` (or `.json`; local time to the
millisecond, so two exports never get the same name) with the system picker and writes the app's
data file there in that format.

#### Scenario: Export
- **WHEN** the user taps Vie tiedot, picks CSV and a folder
- **THEN** the CSV file is written and the app says how many weights it holds

#### Scenario: File name
- **WHEN** the export starts on 9 November 2026 at 07:12:05.123
- **THEN** the suggested name is `terveyslappu-export-20261109-071205-123.csv`

#### Scenario: Export as JSON
- **WHEN** the user picks JSON instead
- **THEN** a `.json` file with the same data is written

#### Scenario: Picker cancelled
- **WHEN** the user closes the format choice or the picker
- **THEN** nothing is written

### Requirement: File content
The file's weights SHALL be every weight in Health Connect from every app, oldest first. In CSV each
weight SHALL be one row of the `weights` table with date (`yyyy-MM-dd`), time (`HH:mm:ss`, the
record's own offset), weight in kg with a comma decimal, the writing app's name and a `raw` column
holding the raw data the phone keeps for that weight as `name=value` pairs separated by `|` (empty
when none), under the header row `date;time;weight_kg;source;raw`.

#### Scenario: Row format
- **GIVEN** an 82.4 kg weight at 1 March 2024 07:12 written by this app by hand
- **THEN** the CSV row is `2024-03-01;07:12:00;82,4;Terveyslappu;`

#### Scenario: Scale row
- **GIVEN** an 82.5 kg scale weigh-in with raw data kept
- **THEN** its `raw` column holds the packet hex and the scale's fields, e.g.
  `source=scale|packet=0339...|key=12c0|weight_raw=825|decimals=1|unit=kg|stable=1|impedance=5123|...`

#### Scenario: Owner details line
- **GIVEN** the owner's birth year and height are set
- **THEN** the file holds no owner details line

#### Scenario: No owner details
- **GIVEN** no owner detail is set
- **THEN** the CSV file starts with the version line

#### Scenario: Read back
- **WHEN** an exported file of either format is imported with Tuo tiedot
- **THEN** the same weights and times are found, and their raw data is kept on the phone again

#### Scenario: Older export
- **WHEN** a file without the `raw` column is imported
- **THEN** the weights are imported as before

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
