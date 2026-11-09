## ADDED Requirements

### Requirement: One file for all app data
The app's data file SHALL carry every data kind the app keeps for the user, so export, import and
backup always move all of it at once. It SHALL be a UTF-8 text file a spreadsheet opens, with
semicolon-separated values, made of a version line followed by one section per data kind that has
any data. A data kind SHALL be either a value section, one line `#<kind>;` followed by
`name=value` pairs separated by `|`, or a table section, a line `#table;<kind>` followed by a
header row and one row per item until the next line starting with `#` or the end of the file.

#### Scenario: File layout
- **GIVEN** the owner's height is 180 cm, the theme is Tumma and there are two weights
- **THEN** the file is, in this order:
  ```
  #terveyslappu;version=2
  #profile;height_cm=180
  #settings;theme=dark|chart_period=<period>
  #table;weights
  date;time;weight_kg;source;raw
  <two weight rows>
  ```

#### Scenario: Empty kind left out
- **GIVEN** no owner detail is set
- **THEN** the file has no `#profile;` line

### Requirement: Data kinds carried
The file SHALL carry these data kinds:
- `profile` (value section): the owner details, as defined by the owner details line of the
  weight export (`birth_year`, `height_cm`, `gender`).
- `settings` (value section): the user's own app choices: `theme` (`system`, `light`, `dark`) and
  `chart_period` (the chosen chart period).
- `weights` (table section): every weight with its raw data, as defined by the weight export's
  row format.

State tied to this phone (the backup file's access, the last weigh-in, the log, Health Connect
permissions) SHALL NOT be carried. A data kind the app starts keeping later SHALL be added to the
file as its own section.

#### Scenario: Device state not carried
- **GIVEN** the backup is on
- **THEN** the file holds nothing about the backup file or its access

### Requirement: Read sections
When reading the file, the app SHALL read every section it knows, SHALL ignore unknown sections and
unknown names inside known value sections, and SHALL report the unknown section kinds so the user
can be told part of the file was not used. A file with a version higher than the app knows SHALL
still be read the same way.

#### Scenario: File from a newer app
- **WHEN** a file has a `#table;steps` section this app does not know
- **THEN** its weights, owner details and settings are read, the steps rows are not counted as
  skipped weight rows, and the reader reports `steps` as not used

#### Scenario: Unknown setting
- **WHEN** the `#settings;` line has `goal_kg=75` that this app does not know
- **THEN** the known settings are read and `goal_kg` is ignored

### Requirement: Older files
A file without a version line SHALL be read as before this format: an optional `#profile;` first
line followed by one weight table with its header (or none), or any other history table such as an
OKOK export.

#### Scenario: Older export
- **WHEN** a file starts with `#profile;height_cm=180` and then `date;time;weight_kg;source;raw`
- **THEN** the owner details and the weights are read as before

#### Scenario: OKOK file
- **WHEN** an OKOK `.xls` or another history CSV is read
- **THEN** it is read as a weight history as before
