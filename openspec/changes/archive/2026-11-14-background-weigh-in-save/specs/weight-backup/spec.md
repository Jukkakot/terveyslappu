## MODIFIED Requirements

### Requirement: Automatic updates
While the backup is on, the app SHALL rewrite the file as the app's data file in CSV, holding every
data kind the export holds, after each change it makes to that data (a weight saved by hand, by the
dialog or automatically, imported, deleted, or a "?" weight accepted), and when the app opens and
no backup has succeeded yet that calendar day. A change made while the app was not open (an
automatic weigh-in, an action from its notification) SHALL be backed up the next time the app
opens, whatever day the last backup ran.

#### Scenario: After a weigh-in
- **WHEN** a scale weight is saved
- **THEN** the backup file is rewritten and holds the new weight

#### Scenario: After an automatic weigh-in
- **GIVEN** a backup succeeded today at 06:00
- **WHEN** a weight is saved automatically at 07:12 with the app closed and the app is opened at 08:00
- **THEN** the backup file is rewritten and holds the new weight with its mark

#### Scenario: After an acceptance in Historia
- **WHEN** the user accepts a "?" weight in Historia
- **THEN** the backup file is rewritten and the weight has no mark

#### Scenario: Daily
- **GIVEN** the last successful backup was yesterday at 23:00
- **WHEN** the app opens at 07:00
- **THEN** a backup runs

#### Scenario: Already backed up today
- **GIVEN** a backup succeeded today at 07:00 and nothing changed while the app was closed
- **WHEN** the app opens at 12:00
- **THEN** no backup runs

#### Scenario: Off
- **WHEN** the backup is off
- **THEN** no file is written
