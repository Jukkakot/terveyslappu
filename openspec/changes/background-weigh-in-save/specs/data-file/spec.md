## MODIFIED Requirements

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

## ADDED Requirements

### Requirement: Mark read back
When a file is read, a weight with `check` `?` SHALL be marked "?" on the phone, a weight without
it SHALL NOT be marked, and any other `check` value SHALL be read as no mark.

#### Scenario: Import a questioned weight
- **WHEN** a file whose 61.3 kg weight has `check` `?` is imported
- **THEN** 61.3 kg is in Health Connect and marked "?" in Historia
