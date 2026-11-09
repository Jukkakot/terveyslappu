## MODIFIED Requirements

### Requirement: Automatic updates
While the backup is on, the app SHALL rewrite the file as the app's data file in CSV, holding every
data kind the export holds, after each change it makes to that data (a weight saved, imported or
deleted), and when the app opens and no backup has succeeded yet that calendar day.

#### Scenario: After a weigh-in
- **WHEN** a scale weight is saved
- **THEN** the backup file is rewritten and holds the new weight

#### Scenario: Daily
- **GIVEN** the last successful backup was yesterday at 23:00
- **WHEN** the app opens at 07:00
- **THEN** a backup runs

#### Scenario: Already backed up today
- **GIVEN** a backup succeeded today at 07:00
- **WHEN** the app opens at 12:00
- **THEN** no backup runs

#### Scenario: Off
- **WHEN** the backup is off
- **THEN** no file is written

### Requirement: Restorable
The backup file SHALL be readable by Tuo tiedot, so a new phone gets the data back.

#### Scenario: New phone
- **WHEN** the backup file is imported with Tuo tiedot on another phone
- **THEN** the weights, times and raw data in it are found
