## MODIFIED Requirements

### Requirement: Export to a chosen file
Asetukset SHALL offer "Vie painot", which asks where to save a file named
`terveyslappu-painot-<date>.csv` with the system picker and writes the export there.

#### Scenario: Export
- **WHEN** the user taps Vie painot and picks a folder
- **THEN** the file is written and the app says how many weights it holds

#### Scenario: Picker cancelled
- **WHEN** the user closes the picker
- **THEN** nothing is written
