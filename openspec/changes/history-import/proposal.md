# Proposal

## Why

The user has about four years of weights in the OKOK app and wants them in Health Connect next to
the new scale weigh-ins, so Samsung Health shows one continuous history and OKOK can be dropped.
The export's exact format is still unknown (Q2), so the import has to read common text table
layouts and show what it understood before anything is written.

## What Changes

- Main screen menu gets "Tuo historia": the user picks a file with the system file picker.
- The app reads delimited text tables (comma, semicolon, tab), finds the date (and optional time)
  column and the weight column by header names or by the values, and understands the common date
  formats and kg/lb weights with a comma or dot decimal.
- A preview shows how many weights were found, their date range, a few example rows and how many
  rows were skipped and why. Nothing is written before the user confirms.
- Confirming writes the weights to Health Connect with their original times, in batches, with
  progress. Importing the same file again does not create duplicates.
- The log records what the importer detected and the skipped lines, so a file it cannot read can
  be fixed from a shared log.

## Capabilities

### New Capabilities
- `history-import`: reading a weight history file, previewing it and writing it to Health
  Connect without duplicates.

### Modified Capabilities
- (none) The menu entry belongs to the new capability.

## Impact

- Parts: main screen (menu entry, preview dialog), Health Connect (bulk write), log.
- No new permissions or libraries: the system file picker gives read access to the one file.
- Excel files (.xlsx) are out of scope; if the OKOK export turns out to be one, it is saved as CSV
  first or a follow-up adds it.
