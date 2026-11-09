## MODIFIED Requirements

### Requirement: File content
The file SHALL hold every weight in Health Connect from every app, oldest first, one row each with
date (`yyyy-MM-dd`), time (`HH:mm:ss`, the record's own offset), weight in kg with a comma decimal,
the writing app's name and a `raw` column holding the raw data the phone keeps for that weight as
`name=value` pairs separated by `|` (empty when none), separated by semicolons under a header row.

#### Scenario: Row format
- **GIVEN** an 82.4 kg weight at 1 March 2024 07:12 written by this app by hand
- **THEN** the row is `2024-03-01;07:12:00;82,4;Terveyslappu;`

#### Scenario: Scale row
- **GIVEN** an 82.5 kg scale weigh-in with raw data kept
- **THEN** its `raw` column holds the packet hex and the scale's fields, e.g.
  `packet=2033...|weight_raw=825|decimals=1|unit=kg|stable=1|impedance=5123`

#### Scenario: Read back
- **WHEN** an exported file is imported with Tuo historia
- **THEN** the same weights and times are found, and their raw data is kept on the phone again

#### Scenario: Older export
- **WHEN** a file without the `raw` column is imported
- **THEN** the weights are imported as before
