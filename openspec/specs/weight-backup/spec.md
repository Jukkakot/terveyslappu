# weight-backup Specification

## Purpose
Keeps a copy of all weights in a file the user chose outside the phone's Health Connect, updated
automatically, with its state always visible so a broken backup is noticed.

## Requirements

### Requirement: Turn the backup on
Asetukset SHALL offer to turn the backup on; the user picks or creates one CSV file with the system
picker and the app keeps write access to it across restarts.

#### Scenario: First backup
- **WHEN** the user taps Ota käyttöön in Asetukset and creates `terveyslappu-varmuuskopio.csv` in
  Google Drive
- **THEN** the app writes all weights there at once and the status shows the time and count

#### Scenario: Picker cancelled
- **WHEN** the user closes the picker
- **THEN** the backup stays off

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

### Requirement: Visible status
Asetukset SHALL show the backup state: off with a button to turn it on, running, the last success
with its time and weight count, or the last failure with a retry button. A last success older than
three days SHALL be shown as a warning there and as a notice on the home screen; a failure SHALL also
show that notice.

#### Scenario: Failure
- **WHEN** writing the file fails (for example the file was deleted)
- **THEN** Asetukset shows the failure time and Yritä uudelleen, the home screen shows a notice that
  opens Asetukset, and the cause is in the log

#### Scenario: Stale
- **WHEN** the last success is four days old
- **THEN** the status is shown in the warning colour and the home screen shows the notice

### Requirement: Manage the backup
Asetukset SHALL offer, while the backup is on, to back up now, to change the file and to turn the
backup off; turning it off SHALL release the file access and keep the file as it is.

#### Scenario: Turn off
- **WHEN** the user turns the backup off
- **THEN** no further writes happen and the status shows off

### Requirement: Restorable
The backup file SHALL be readable by Tuo tiedot, so a new phone gets the data back.

#### Scenario: New phone
- **WHEN** the backup file is imported with Tuo tiedot on another phone
- **THEN** the weights, times and raw data in it are found

### Requirement: Backup does not silently shrink
When the history permission is not granted, the backup status SHALL say the copy holds recent
weights only (like the export does). An automatic backup that would write fewer than half of the
last successful count SHALL be refused: the file is left as it was, the status is failed with the
reason, and `backup.run refused reason=shrink` is logged. "Varmuuskopioi nyt" is not guarded.

#### Scenario: Permission missing
- **GIVEN** the last backup held 100 weights and the history permission is revoked
- **WHEN** an automatic backup would write 20
- **THEN** it is refused and the file is unchanged
