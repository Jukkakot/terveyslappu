## MODIFIED Requirements

### Requirement: Pick a history file
The main screen menu SHALL offer "Tuo historia", which opens the system file picker for a text file
or an old binary Excel file (`.xls`) and reads the chosen file without any other permission.

#### Scenario: Pick a file
- **WHEN** the user taps Tuo historia and picks a CSV file
- **THEN** the app reads it and shows the preview

#### Scenario: Pick an Excel file
- **WHEN** the user taps Tuo historia and picks an `.xls` file exported by OKOK
- **THEN** the app reads its first sheet and shows the preview

#### Scenario: Picker cancelled
- **WHEN** the user closes the picker without choosing
- **THEN** nothing happens

## ADDED Requirements

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
