# Proposal

## Why

Health Connect keeps data only on the phone, and moving to a new phone is still undecided
(roadmap phase 4). A readable CSV export (backlog) gives the user a copy they own today, can open
in a spreadsheet and can bring back with the history import, without deciding the sync question.

## What Changes

- Main screen menu gets "Vie painot": the user picks where to save a CSV file with the system
  picker (Drive, Downloads, …).
- The export holds every weight in Health Connect, from every app and the whole history, oldest
  first: date, time, weight, the app that wrote it.
- Reading more than the last 30 days needs Health Connect's history permission; the app asks for
  it at the first export and says so when only recent weights could be read.
- The file is in the layout the history import reads, so a new phone can import it.

## Capabilities

### New Capabilities
- `weight-export`: writing all weights from Health Connect to a CSV file chosen by the user.

### Modified Capabilities
- (none)

## Impact

- Parts: main screen (menu entry, progress/result message), Health Connect (paged read of all
  weights, history permission), log.
- New permission: `READ_HEALTH_DATA_HISTORY` (Health Connect, optional; asked only for export).
- No new libraries.
