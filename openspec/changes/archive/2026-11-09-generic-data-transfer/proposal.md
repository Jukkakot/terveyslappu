# Proposal

## Why

Asetukset offers "Vie painot" and "Tuo painot", built around weights only. The app will keep more
kinds of measured data later (steps, goal weight…), and the user wants to move that raw data
between apps and phones through the same export, import and backup, in the formats other apps
read: CSV and JSON.

## What Changes

- Asetukset: "Vie painot" / "Tuo painot" become **Vie tiedot** / **Tuo tiedot** under the section
  title **Tiedot**.
- **Vie tiedot** asks for the format in a small dialog, **CSV** or **JSON**, then saves
  `terveyslappu-export-<yyyyMMdd-HHmmss-SSS>.csv` or `.json` (millisecond
  timestamp, so names never collide) with the system picker.
- **Tuo tiedot** reads both formats (recognised from the content), plus the OKOK `.xls` and other
  history tables as before.
- One data model with a section (CSV) or key (JSON) per data kind; today the only kind is the
  weights with their raw data. A kind added later gets its own section/key and travels through
  export, import and backup without new buttons. Unknown kinds in a file from a newer version are
  skipped and the preview says so.
- **BREAKING (small):** owner details (birth year, height, gender) and app settings are no longer
  carried: they are static and quickly re-entered. An old file's `#profile;` line is ignored.
- Backup: keeps writing CSV (the existing backup file stays valid), now through the same writer,
  so new kinds are backed up too. It runs after every change to the carried data, and whenever the
  app opens and no backup has succeeded yet today (instead of "older than 24 hours"). No
  background job.

## Capabilities

### New Capabilities
- `data-file`: the app's data file in CSV and JSON: data kinds, the two layouts, unknown kinds and
  older files.

### Modified Capabilities
- `weight-export`: becomes "Vie tiedot" with a CSV/JSON choice; owner details line dropped.
- `history-import`: entry becomes "Tuo tiedot"; reads both formats; preview notes unknown parts;
  owner details no longer restored.
- `weight-backup`: same writer (CSV); daily check by calendar day on app open.
- `home-screen`: Asetukset section "Painot" renamed "Tiedot" with the new button names.

## Impact

- App screens: Asetukset (names, format dialog), import preview (unknown parts note).
- Code: new pure `data/` package (data model, CSV and JSON writer/reader); `history/CsvExport` and
  the `#profile;` handling in `HistoryParser` move behind it; `profile/ProfileLine` removed;
  `backup/` uses the new writer; strings. JSON via kotlinx.serialization (already used by the raw archive),
  no new dependency.
- Health Connect: unchanged. Scale, widget, build/CI: untouched.
