## MODIFIED Requirements

### Requirement: Automatic updates
While the backup is on, the app SHALL rewrite the file as the app's data file, holding every data
kind the export holds, after each weight it saves, imports or deletes, after owner details or
settings change, and when the home screen opens and the last successful backup is older than 24
hours.

#### Scenario: After a weigh-in
- **WHEN** a scale weight is saved
- **THEN** the backup file is rewritten and holds the new weight

#### Scenario: After a settings change
- **WHEN** the user changes the theme
- **THEN** the backup file is rewritten and holds the new theme

#### Scenario: Daily
- **WHEN** the home screen opens 25 hours after the last backup
- **THEN** a backup runs

#### Scenario: Off
- **WHEN** the backup is off
- **THEN** no file is written

### Requirement: Restorable
The backup file SHALL be readable by Tuo tiedot, so a new phone gets all the data back.

#### Scenario: New phone
- **WHEN** the backup file is imported with Tuo tiedot on another phone
- **THEN** the weights and times, the owner details and the settings in it are found
