# Proposal

## Why

Asetukset offers "Vie painot" and "Tuo painot", but the file already carries more than weights
(raw scale data, owner details) and the app will keep more kinds of data later (goal weight,
settings, steps…). Each new kind should travel through the same export, import and backup without
new buttons or a new file, so the user always moves *all* app data in one go.

## What Changes

- Asetukset: "Vie painot" / "Tuo painot" become **Vie tiedot** / **Tuo tiedot** under the section
  title **Tiedot**; the exported file is named `terveyslappu-<date>.csv`.
- One extensible file format (still a semicolon CSV a spreadsheet opens): a version line, then one
  section per data kind. A kind is either a `#<kind>;name=value|…` line (single values, like the
  owner details) or a `#table;<kind>` line followed by its own header and rows (lists, like the
  weights). New kinds are added in the app as new sections; the buttons, file and backup stay the
  same.
- Data kinds carried now: weights with their raw data (as today), owner details (as today, the
  `#profile;` line), and the app settings that belong to the user (theme, chart period).
  Device-bound state (backup file access, the last weigh-in, the log) is not carried.
- Import reads every known section, ignores unknown ones (a newer app's file still imports what
  this app knows, and says so), and the preview lists what the file holds before anything is
  written. Old files (plain weight table, optional `#profile;` line) and OKOK files import as
  before.
- The automatic backup writes the same file, so it carries every data kind too.

## Capabilities

### New Capabilities
- `data-file`: the app's own data file: version line, sections per data kind, which kinds are
  carried and how unknown sections and older files are handled.

### Modified Capabilities
- `weight-export`: becomes "Vie tiedot"; the file is the `data-file` with every data kind, file
  name changes.
- `history-import`: entry becomes "Tuo tiedot"; reads `data-file` sections and shows them in the
  preview; restores settings next to the owner details.
- `weight-backup`: the backup file holds every data kind, not only weights.
- `home-screen`: Asetukset section "Painot" renamed "Tiedot" with the new button names.

## Impact

- App screens: Asetukset (names, section title), import preview (lists the data kinds).
- Code: `history/CsvExport` and `history/HistoryParser` split into a section-based writer/reader,
  `profile/ProfileLine` becomes one section, new settings section over `AppearancePrefs` and
  `PeriodPrefs`; `backup/` uses the new writer; strings.
- Health Connect: unchanged (weights read and written as today).
- Scale, widget, build/CI: untouched.
- Compatibility: older app versions read the new file's weights (the weight table keeps its
  header) but ignore the new section lines; this app reads every older file.
