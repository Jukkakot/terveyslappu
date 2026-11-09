# history-import Specification

## Purpose
Brings a weight history exported from another app (the OKOK scale app first) into Health Connect
with its original dates, after showing the user what will be imported.

## Requirements

### Requirement: Pick a history file
Asetukset SHALL offer "Tuo tiedot", which opens the system file picker for a text file (CSV or
JSON) or an old binary Excel file (`.xls`) and reads the chosen file without any other permission.
The format SHALL be recognised from the content, not the file name.

#### Scenario: Pick a file
- **WHEN** the user taps Tuo tiedot and picks a CSV file
- **THEN** the app reads it and shows the preview

#### Scenario: Pick a JSON file
- **WHEN** the user picks a JSON file exported by this app
- **THEN** the app reads it and shows the preview

#### Scenario: Pick an Excel file
- **WHEN** the user taps Tuo tiedot and picks an `.xls` file exported by OKOK
- **THEN** the app reads its first sheet and shows the preview

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
first and last three rows, the skipped count with reasons and, when the file has parts this app
version does not know, that they will not be used; it SHALL import only after confirmation.

#### Scenario: Confirm
- **WHEN** the user taps Tuo in the preview
- **THEN** the weights are written to Health Connect

#### Scenario: Cancel
- **WHEN** the user taps Peruuta
- **THEN** nothing is written

#### Scenario: Unknown parts
- **WHEN** the file has a data kind this app does not know
- **THEN** the preview says part of the file is from a newer app version and is not used, and the
  weights import as usual

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

### Requirement: OKOK Excel layout
The app SHALL read the OKOK International Excel export: skip the title rows before the header row,
find the date, time and weight columns from the header (`Date`, `Time`, `Weight(Kg)`), read values
stored as text, and give each `MM-DD` date its year from the range line
(`YYYY-MM-DD ~ YYYY-MM-DD(nDay)`): the newest row is in the end year, and the year drops by one
whenever the month-day goes up from one row to the next.

#### Scenario: Rows across a new year
- **GIVEN** the range line `2024-11-20 ~ 2025-01-05(47Day)` and rows `01-05`, `01-02`, `12-30`, `11-20`
- **WHEN** the file is read
- **THEN** the rows are 5.1.2025, 2.1.2025, 30.12.2024 and 20.11.2024

#### Scenario: Weight as text
- **WHEN** a row has date `03-01`, time `07:12` and weight `82.4` as text cells
- **THEN** it is read as 82.4 kg at 1 March 07:12 of its year

#### Scenario: Not an Excel file
- **WHEN** a file named `.xls` is not a binary Excel file
- **THEN** it is read as a text table as before

#### Scenario: Unreadable Excel file
- **WHEN** the binary Excel file is damaged or has no weight column
- **THEN** the preview says no weights were found and the log records why

### Requirement: Keep all OKOK columns
Every other column of an OKOK row and the profile from the title rows (gender, age, height) SHALL be
kept with that weight in the phone's raw-data archive exactly as written, under the file's own names,
without any calculation; only the weight SHALL be shown.

#### Scenario: All columns kept
- **WHEN** a row has `BMI` `24.10` and `Body Fat Percentage` `18.50` next to its weight
- **THEN** the archive keeps `BMI=24.10` and `Body Fat Percentage=18.50` and the file's gender, age
  and height with that weight, and the app shows only the weight
