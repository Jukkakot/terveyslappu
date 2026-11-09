## MODIFIED Requirements

### Requirement: Pick a history file
Asetukset SHALL offer "Tuo historia", which opens the system file picker for a text file or an old
binary Excel file (`.xls`) and reads the chosen file without any other permission.

#### Scenario: Pick a file
- **WHEN** the user taps Tuo historia and picks a CSV file
- **THEN** the app reads it and shows the preview

#### Scenario: Pick an Excel file
- **WHEN** the user taps Tuo historia and picks an `.xls` file exported by OKOK
- **THEN** the app reads its first sheet and shows the preview

#### Scenario: Picker cancelled
- **WHEN** the user closes the picker without choosing
- **THEN** nothing happens
