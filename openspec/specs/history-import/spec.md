# history-import Specification

## Purpose
Brings a weight history exported from another app (the OKOK scale app first) into Health Connect
with its original dates, after showing the user what will be imported.

## Requirements

### Requirement: Pick a history file
The main screen menu SHALL offer "Tuo historia", which opens the system file picker for a text
file and reads the chosen file without any other permission.

#### Scenario: Pick a file
- **WHEN** the user taps Tuo historia and picks a CSV file
- **THEN** the app reads it and shows the preview

#### Scenario: Picker cancelled
- **WHEN** the user closes the picker without choosing
- **THEN** nothing happens

### Requirement: Read the table
The app SHALL read text tables separated by comma, semicolon or tab, with or without a header row,
and SHALL find the date column, an optional separate time column and the weight column from the
header names (Finnish, English, Chinese) or, without a header, from the values.

#### Scenario: Header with date, time and weight
- **WHEN** the file starts with `Date;Time;Weight(kg);BMI` and rows like `2024-03-01;07:12;82,4;24,1`
- **THEN** each row is read as 82.4 kg at 1 March 2024 07:12

#### Scenario: No header
- **WHEN** the rows are like `01.03.2024 07:12,82.4`
- **THEN** the date-time and weight columns are found from the values

#### Scenario: Pounds
- **WHEN** the weight column header says lb
- **THEN** the weights are converted to kilograms

#### Scenario: Jin
- **WHEN** the weight column header says jin or 斤 (and not 公斤)
- **THEN** the weights are halved to kilograms

### Requirement: Date formats
The app SHALL understand ISO dates (`2024-03-01`, with optional `T`/space time and seconds),
`d.M.yyyy`, `yyyy/M/d`, `d/M/yyyy` and `M/d/yyyy`, and seconds or milliseconds since 1970. Times
without a zone SHALL be read in the phone's time zone.

#### Scenario: Day and month order from the data
- **WHEN** slash dates in the file include `13/02/2024`
- **THEN** the whole file is read day first

#### Scenario: Ambiguous slash dates
- **WHEN** no slash date in the file has a first number above 12
- **THEN** the file is read day first and the preview shows example dates so the user can check

### Requirement: Skip rows that cannot be weights
The app SHALL skip rows whose date or weight cannot be read, weights outside 20–300 kg, times in
the future and repeated rows of the same moment, and SHALL count each reason.

#### Scenario: Mixed file
- **WHEN** a file has 10 good rows, one empty weight and one weight of 0
- **THEN** the preview offers 10 weights and says 2 rows were skipped

#### Scenario: Nothing usable
- **WHEN** no row can be read
- **THEN** the preview says no weights were found and offers no import

### Requirement: Preview before import
The app SHALL show, before writing anything, the number of weights, the first and last date, the
first and last three rows and the skipped count with reasons, and import only after confirmation.

#### Scenario: Confirm
- **WHEN** the user taps Tuo in the preview
- **THEN** the weights are written to Health Connect

#### Scenario: Cancel
- **WHEN** the user taps Peruuta
- **THEN** nothing is written

### Requirement: Write without duplicates
The app SHALL write the weights to Health Connect with their original times in batches, show the
progress, and SHALL NOT create a second record when the same weight and moment is imported again.

#### Scenario: Import twice
- **WHEN** the same file is imported a second time
- **THEN** Health Connect still holds one record per row

#### Scenario: Failure midway
- **WHEN** a batch fails
- **THEN** the app says how many weights were saved, logs the cause and offers to retry, and the
  retry does not duplicate the saved ones

### Requirement: Import in the log
The app SHALL log the file's header line, the detected separator, columns and date order, the
counts, the skipped reasons with up to 20 skipped lines raw, and the import duration.

#### Scenario: Unreadable file shared
- **WHEN** an import finds nothing and the user shares the log
- **THEN** the log shows the header and sample lines needed to support the format
