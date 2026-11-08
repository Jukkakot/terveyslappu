# Proposal

## Why

Health Connect stores weights only on the phone; losing or replacing the phone loses the history
unless something copies it out (roadmap phase 4, still open). The user prefers an active, visible
sync to a silent backup that could break unnoticed (product.md). An automatic copy of the export
file into a place the user picks (Google Drive through the system picker), with its status on the
main screen, gives that today without a server (NFR-2) and is read back by the history import.

## What Changes

- Opt-in "Varmuuskopio": the user picks one CSV file (any storage the system picker offers); the
  app keeps write access to it.
- The app rewrites the file with all weights (export format) after every change it makes: manual
  save, scale weigh-in, import, delete; and when the main screen opens and the last backup is
  older than a day (catches weights other apps wrote).
- The main screen shows the backup status: off, running, last success with time and count, or
  failure with a retry. A backup older than three days is highlighted.
- The menu can back up now, change the file and turn the backup off.

## Capabilities

### New Capabilities
- `weight-backup`: the automatic backup file, its triggers and its visible status.

### Modified Capabilities
- (none) Saving, weigh-in, import and delete keep their behaviour; the backup runs after them.

## Impact

- Parts: main screen (status card, menu entries), weigh-in dialog (triggers a backup after
  saving), Health Connect (reads all weights, history permission from weight-export), app storage
  (backup settings in preferences), log.
- No new permissions or libraries.
- Roadmap phase 4 gets an interim answer; Samsung Cloud and Health Connect's own export stay
  possible later.
