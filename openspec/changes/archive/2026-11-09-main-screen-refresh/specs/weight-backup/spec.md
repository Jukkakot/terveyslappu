## MODIFIED Requirements

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
While the backup is on, the app SHALL rewrite the file with all weights in the export format after
each weight it saves, imports or deletes, and when the home screen opens and the last successful
backup is older than 24 hours.

#### Scenario: After a weigh-in
- **WHEN** a scale weight is saved
- **THEN** the backup file is rewritten and holds the new weight

#### Scenario: Daily
- **WHEN** the home screen opens 25 hours after the last backup
- **THEN** a backup runs

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
