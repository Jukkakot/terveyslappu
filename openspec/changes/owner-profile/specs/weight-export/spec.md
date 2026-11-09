## MODIFIED Requirements

### Requirement: File content
The file SHALL hold every weight in Health Connect from every app, oldest first, one row each with
date (`yyyy-MM-dd`), time (`HH:mm:ss`, the record's own offset), weight in kg with a comma decimal,
the writing app's name and a `raw` column holding the raw data the phone keeps for that weight as
`name=value` pairs separated by `|` (empty when none), separated by semicolons under a header row.
When any owner detail is set, the file SHALL start with one line before the header,
`#profile;` followed by the set details as `name=value` pairs separated by `|`
(`birth_year`, `height_cm`, `gender` = `male` or `female`); unset details SHALL be left out.

#### Scenario: Row format
- **GIVEN** an 82.4 kg weight at 1 March 2024 07:12 written by this app by hand
- **THEN** the row is `2024-03-01;07:12:00;82,4;Terveyslappu;`

#### Scenario: Scale row
- **GIVEN** an 82.5 kg scale weigh-in with raw data kept
- **THEN** its `raw` column holds the packet hex and the scale's fields, e.g.
  `source=scale|packet=0339...|key=12c0|weight_raw=825|decimals=1|unit=kg|stable=1|impedance=5123|...`

#### Scenario: Owner details line
- **GIVEN** the owner's birth year is 1990 and height 180 cm, gender not chosen
- **THEN** the file's first line is `#profile;birth_year=1990|height_cm=180`

#### Scenario: No owner details
- **GIVEN** no owner detail is set
- **THEN** the file starts with the header row

#### Scenario: Read back
- **WHEN** an exported file is imported with Tuo painot
- **THEN** the same weights and times are found, and their raw data is kept on the phone again

#### Scenario: Older export
- **WHEN** a file without the `raw` column is imported
- **THEN** the weights are imported as before
