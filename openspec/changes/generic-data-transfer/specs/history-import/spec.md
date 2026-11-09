## MODIFIED Requirements

### Requirement: Pick a history file
Asetukset SHALL offer "Tuo tiedot", which opens the system file picker for a text file or an old
binary Excel file (`.xls`) and reads the chosen file without any other permission. The app's own
data file and other apps' weight histories SHALL both be accepted.

#### Scenario: Pick a file
- **WHEN** the user taps Tuo tiedot and picks a CSV file
- **THEN** the app reads it and shows the preview

#### Scenario: Pick an Excel file
- **WHEN** the user taps Tuo tiedot and picks an `.xls` file exported by OKOK
- **THEN** the app reads its first sheet and shows the preview

#### Scenario: Picker cancelled
- **WHEN** the user closes the picker without choosing
- **THEN** nothing happens

### Requirement: Preview before import
The app SHALL show, before writing anything, the number of weights, the first and last date, the
first and last three rows, the skipped count with reasons, the other data kinds the file holds that
will be restored (owner details, settings), and the parts of the file this app version cannot use;
it SHALL import only after confirmation. A file holding no weights but other known data SHALL still
be importable.

#### Scenario: Confirm
- **WHEN** the user taps Tuo in the preview
- **THEN** the weights are written to Health Connect and the other data kinds are restored

#### Scenario: Cancel
- **WHEN** the user taps Peruuta
- **THEN** nothing is written or changed

#### Scenario: Unknown parts
- **WHEN** the file has a section this app does not know
- **THEN** the preview says part of the file is from a newer app version and is not used, and the
  rest imports as usual

#### Scenario: Only settings
- **WHEN** the file has owner details and settings but no weights
- **THEN** the preview offers to restore them

## ADDED Requirements

### Requirement: Restore settings
When an imported data file holds a `#settings;` line, the app SHALL, on confirming the import, set
the theme and the chart period from it; values the app does not know SHALL be ignored and leave
that setting as it is.

#### Scenario: New phone
- **GIVEN** the phone's theme is Järjestelmä
- **WHEN** the user imports a file with `#settings;theme=dark` and confirms
- **THEN** the app turns dark

#### Scenario: Unknown value
- **WHEN** the line has `theme=purple`
- **THEN** the theme stays as it was
