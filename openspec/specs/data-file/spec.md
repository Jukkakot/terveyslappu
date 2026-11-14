# data-file Specification

## Purpose
TBD - created by archiving change generic-data-transfer. Update Purpose after archive.

## Requirements

### Requirement: Data kinds
The app's data file SHALL carry the measured data the app keeps, one part per data kind; today the
only kind is `weights`: every weight with its time, kg, the writing app and its raw data. Owner
details, app settings and state tied to the phone (backup access, the last weigh-in, the log,
Health Connect permissions) SHALL NOT be carried. A data kind the app starts keeping later SHALL be
added as its own part in both formats.

#### Scenario: Only data
- **GIVEN** owner details are set, the theme is Tumma and the backup is on
- **THEN** the file holds the weights and nothing about the owner details, the theme or the backup

### Requirement: CSV layout
The CSV file SHALL be UTF-8 with semicolon-separated values that a spreadsheet opens: a version
line `#terveyslappu;version=2`, then for each data kind with data a line `#table;<kind>`, its
header row and one row per item, until the next line starting with `#` or the end of the file.

#### Scenario: CSV with weights
- **GIVEN** two weights
- **THEN** the file is `#terveyslappu;version=2`, `#table;weights`,
  `date;time;weight_kg;source;raw` and the two weight rows

### Requirement: JSON layout
The JSON file SHALL be one UTF-8 object with `"app": "terveyslappu"`, `"version": 2` and one key per
data kind holding an array. Each weight SHALL be an object with `time` (ISO 8601 with the record's
offset, e.g. `2024-03-01T07:12:00+02:00`), `kg` (number), `source` (the writing app's name),
when kept, `raw` (an object of the raw data's names and values as text), and, only for a weight
marked "?", `"check": "?"`, oldest first.

#### Scenario: JSON weight
- **GIVEN** an 82.4 kg weight at 1 March 2024 07:12 (+02:00) written by this app by hand
- **THEN** its item is `{"time":"2024-03-01T07:12:00+02:00","kg":82.4,"source":"Terveyslappu"}`

#### Scenario: JSON questioned weight
- **GIVEN** a 61.3 kg automatic weight marked "?"
- **THEN** its item holds `"check":"?"` and an exported weight without the mark holds no `check`

### Requirement: Unknown parts
When reading either format, the app SHALL read every data kind it knows, SHALL skip unknown kinds
and unknown fields, and SHALL report the unknown kinds. A higher version number SHALL be read the
same way.

#### Scenario: File from a newer app
- **WHEN** a file has a `steps` part this app does not know
- **THEN** its weights are read, the steps are not counted as skipped weight rows, and `steps` is
  reported as not used

### Requirement: Older files
A CSV file without a version line SHALL be read as before this format: one weight table with or
without a header, an OKOK export or another history table. A `#profile;` first line from an older
export SHALL be ignored and not counted as a skipped row.

#### Scenario: Older export
- **WHEN** a file starts with `#profile;height_cm=180` and then `date;time;weight_kg;source;raw`
- **THEN** the weights are read and the owner details on the phone stay as they are

#### Scenario: Same data both ways
- **WHEN** the same weights are exported once as CSV and once as JSON and each is imported
- **THEN** both give the same weights, times and raw data

### Requirement: CSV time keeps its offset
The CSV time column SHALL carry the record's UTC offset (`07:12:00+03:00`), so the same instant
comes back in any zone, also in the repeated hour when clocks go back. The reader SHALL accept a
time with or without an offset; without one it is read in the phone's zone.

#### Scenario: Repeated hour
- **GIVEN** two weights at 03:30 local time one hour apart on the night clocks go back
- **THEN** the file has `03:30:00+03:00` and `03:30:00+02:00` and reading it gives both instants

#### Scenario: Older file
- **GIVEN** a time `07:12:00` with no offset
- **THEN** it is read in the phone's zone

### Requirement: Mark read back
When a file is read, a weight with `check` `?` SHALL be marked "?" on the phone, a weight without
it SHALL NOT be marked, and any other `check` value SHALL be read as no mark.

#### Scenario: Import a questioned weight
- **WHEN** a file whose 61.3 kg weight has `check` `?` is imported
- **THEN** 61.3 kg is in Health Connect and marked "?" in Historia
