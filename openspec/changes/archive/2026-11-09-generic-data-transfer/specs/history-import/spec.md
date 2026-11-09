## MODIFIED Requirements

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

## REMOVED Requirements

### Requirement: Restore owner details
**Reason**: Owner details are static and no longer carried in the data file (generic-data-transfer).
**Migration**: An older file's `#profile;` line is ignored (data-file "Older files"); the user
enters the details in Asetukset → Omat tiedot.
