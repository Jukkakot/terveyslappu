## MODIFIED Requirements

### Requirement: Export to a chosen file
Asetukset SHALL offer "Vie tiedot", which asks where to save a file named
`terveyslappu-<date>.csv` with the system picker and writes the app's data file there, holding
every data kind the app carries.

#### Scenario: Export
- **WHEN** the user taps Vie tiedot and picks a folder
- **THEN** the file is written and the app says how many weights it holds

#### Scenario: Picker cancelled
- **WHEN** the user closes the picker
- **THEN** nothing is written

### Requirement: File content
The file SHALL be the app's data file. Its `weights` table SHALL hold every weight in Health
Connect from every app, oldest first, one row each with date (`yyyy-MM-dd`), time (`HH:mm:ss`, the
record's own offset), weight in kg with a comma decimal, the writing app's name and a `raw` column
holding the raw data the phone keeps for that weight as `name=value` pairs separated by `|` (empty
when none), separated by semicolons under the header row `date;time;weight_kg;source;raw`.
When any owner detail is set, the file SHALL hold the `#profile;` line with the set details as
`name=value` pairs separated by `|` (`birth_year`, `height_cm`, `gender` = `male` or `female`);
unset details SHALL be left out. The file SHALL hold the `#settings;` line with the theme and the
chart period.

#### Scenario: Row format
- **GIVEN** an 82.4 kg weight at 1 March 2024 07:12 written by this app by hand
- **THEN** the row is `2024-03-01;07:12:00;82,4;Terveyslappu;`

#### Scenario: Scale row
- **GIVEN** an 82.5 kg scale weigh-in with raw data kept
- **THEN** its `raw` column holds the packet hex and the scale's fields, e.g.
  `source=scale|packet=0339...|key=12c0|weight_raw=825|decimals=1|unit=kg|stable=1|impedance=5123|...`

#### Scenario: Owner details line
- **GIVEN** the owner's birth year is 1990 and height 180 cm, gender not chosen
- **THEN** the file holds the line `#profile;birth_year=1990|height_cm=180`

#### Scenario: No owner details
- **GIVEN** no owner detail is set
- **THEN** the file has no `#profile;` line

#### Scenario: Settings line
- **GIVEN** the theme is Järjestelmä
- **THEN** the file's `#settings;` line has `theme=system` and the chosen chart period

#### Scenario: Read back
- **WHEN** an exported file is imported with Tuo tiedot
- **THEN** the same weights and times are found, and their raw data is kept on the phone again

#### Scenario: Older export
- **WHEN** a file without the `raw` column is imported
- **THEN** the weights are imported as before
