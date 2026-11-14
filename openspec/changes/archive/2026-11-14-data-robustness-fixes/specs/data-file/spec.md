## ADDED Requirements

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
