## MODIFIED Requirements

### Requirement: File content
The file's weights SHALL be every weight in Health Connect from every app, oldest first. In CSV each
weight SHALL be one row of the `weights` table with date (`yyyy-MM-dd`), time (`HH:mm:ss`, the
record's own offset), weight in kg with a comma decimal, the writing app's name, a `raw` column
holding the raw data the phone keeps for that weight as `name=value` pairs separated by `|` (empty
when none), and a `check` column holding `?` for a weight marked "?" (empty otherwise), under the
header row `date;time;weight_kg;source;raw;check`.

#### Scenario: Row format
- **GIVEN** an 82.4 kg weight at 1 March 2024 07:12 written by this app by hand
- **THEN** the CSV row is `2024-03-01;07:12:00;82,4;Terveyslappu;;`

#### Scenario: Scale row
- **GIVEN** an 82.5 kg scale weigh-in with raw data kept
- **THEN** its `raw` column holds the packet hex and the scale's fields, e.g.
  `source=scale|packet=0339...|key=12c0|weight_raw=825|decimals=1|unit=kg|stable=1|impedance=5123|...`

#### Scenario: Questioned row
- **GIVEN** a 61.3 kg automatic weigh-in marked "?"
- **THEN** its `raw` column holds `via=auto` among the fields and its `check` column holds `?`

#### Scenario: Owner details line
- **GIVEN** the owner's birth year and height are set
- **THEN** the file holds no owner details line

#### Scenario: No owner details
- **GIVEN** no owner detail is set
- **THEN** the CSV file starts with the version line

#### Scenario: Read back
- **WHEN** an exported file of either format is imported with Tuo tiedot
- **THEN** the same weights and times are found, their raw data is kept on the phone again, and
  "?" weights are marked again

#### Scenario: Older export
- **WHEN** a file without the `raw` or the `check` column is imported
- **THEN** the weights are imported as before, without marks
