# app-data-transfer Specification

## Purpose
Moves the app's own data on the phone (settings, owner details, raw-data archive) to a new phone
through Android's built-in backup and phone-to-phone transfer, next to the weights' backup file.

## Requirements

### Requirement: Carry the app's own data
The app SHALL let Android's phone-to-phone transfer and cloud backup carry its owner details,
appearance choice, chart and home periods, and the raw-data archive, and restore them when the
app is installed on the new phone.

#### Scenario: New Samsung phone with Smart Switch
- **GIVEN** the old phone has owner details, the dark theme and a raw-data archive
- **WHEN** the user moves to a new phone with Smart Switch and opens the app there
- **THEN** Omat tiedot, the theme, the periods and the archive are as on the old phone

#### Scenario: Weights still come from the backup file
- **GIVEN** the app data was restored on the new phone
- **WHEN** the user has not yet run Tuo tiedot
- **THEN** the app shows no weights of its own until the backup file is imported or Health
  Connect holds them

### Requirement: Keep phone-specific data out
The app SHALL NOT carry its log, the previous weigh-in's lingering-broadcast state, or the backup
file setting. On the new phone the backup is off until the user picks the file again.

#### Scenario: Backup file after a move
- **WHEN** the app is opened for the first time on the new phone
- **THEN** Asetukset shows the backup as off, not as failing

#### Scenario: Fresh log
- **WHEN** the user shares the log on the new phone
- **THEN** it holds only that phone's events

### Requirement: Encrypted cloud copy only
The cloud backup SHALL only be made when Android can encrypt it end to end; without that only
the phone-to-phone transfer carries the data.

#### Scenario: No screen lock
- **GIVEN** the phone has no screen lock
- **WHEN** Android runs its cloud backup
- **THEN** the app's data is not uploaded
